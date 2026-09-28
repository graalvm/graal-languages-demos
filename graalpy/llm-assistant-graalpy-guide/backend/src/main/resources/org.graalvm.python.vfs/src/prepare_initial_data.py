import re
import bs4
from typing import List, Optional
from langchain.chat_models import init_chat_model
from langchain_community.document_loaders import WebBaseLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain.schema import Document
from vector_store_manager import VectorStoreManager
import oracledb


class InitialData:
    def __init__(self, table_name):
        self.table_name = table_name
        self.vector_store_manager = VectorStoreManager(table_name)
        self.vector_store_manager.get_cursor()
        self.vector_store = self.vector_store_manager.create_vector_store()


    def load_data_from_url(self, url: str, class_name=None):
        """Loads web content from a URL."""
        loader = WebBaseLoader(
            web_paths=(url,),
            bs_kwargs=dict(
                parse_only=bs4.SoupStrainer(class_=(class_name))
            ),
        )
        return loader.load()

    def split_documents(self, documents: List[Document]) :
        """Splits documents into chunks."""
        if not documents:
            raise ValueError("Expected a non-empty list of documents")

        text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=800,
            chunk_overlap=150,
            length_function=len,
            is_separator_regex=False,
        )
        return text_splitter.split_documents(documents)

    def __split_file(self, text: str) -> List[dict]:
        """Splits a large text blob into separate file chunks using 'File:' headers."""
        pattern = r"=+\nFile: (.*?)\n=+\n(.*?)\n(?=={3,}|$)"
        matches = re.findall(pattern, text, re.DOTALL)

        return [
            {"file_name": file_name.strip(), "content": content.strip()}
            for file_name, content in matches
        ]

    def __extract_code_meaning(self, code_text: str) -> str:
        """Uses an LLM to summarize what a piece of code does."""
        model = init_chat_model("meta-llama/llama-4-scout-17b-16e-instruct", model_provider="groq")
        prompt = f"Summarize what this code does in simple terms:\n\n{code_text}"
        response = model.invoke(prompt)
        return response.content

    def __docs_from_file(self, file_path: str) -> List[Document]:
        """Processes a structured file, extracts summaries, and creates LangChain Document objects."""
        with open(file_path, "r", encoding="utf-8") as f:
            text = f.read()

        file_chunks = self.__split_file(text)
        structured_data = []

        for file in file_chunks:
            file_name = file["file_name"]
            content = file["content"]

            if file_name.endswith((".java", ".py")):
                explanation = self.__extract_code_meaning(content)
                structured_data.append({
                    "file": file_name,
                    "text": content,
                    "embedding": explanation
                })
            elif file_name.endswith(".md"):
                structured_data.append({
                    "file": file_name,
                    "text": content,
                    "embedding": content
                })

        return [
            Document(
                page_content=item["text"],
                metadata={
                    "source": item["file"],
                    "embedding": item["embedding"]
                }
            )
            for item in structured_data
        ]


    def __add_file_docs(self, vector_store, documents):
        """Function to add documents to the vector store with full content and embeddings."""
        texts = [doc.page_content for doc in documents]
        embeddings = [doc.metadata["embedding"] for doc in documents]
        metadatas = [{"source": doc.metadata["source"]} for doc in documents]

        vector_store.add_texts(texts=texts, metadatas=metadatas, embeddings=embeddings)

    def check_db_init(self):
        """
        Returns True if the table doesn't exist or exists but is empty.
        Returns False if the table exists and contains at least one row.
        """
        cursor = self.vector_store_manager.cursor
        try:
            cursor.execute(f"SELECT COUNT(*) FROM {self.table_name}")
            count = cursor.fetchone()[0]
            return count == 0
        except oracledb.DatabaseError as e:
            error_msg = str(e).lower()
            if "ora-00942" in error_msg or "table or view does not exist" in error_msg:
                return True
            else:
                raise RuntimeError("Failed to check if vector store is empty") from e



    def load_data_from_url_process(self, url, class_name):
        documents = self.load_data_from_url(url, class_name)
        chunks = self.split_documents(documents)
        self.vector_store.add_documents(chunks)


    def load_data_from_file_process(self, file_path):
        documents = self.__docs_from_file(file_path)
        self.__add_file_docs(self.vector_store, documents)


    def create_text_index(self):
        try:
            self.vector_store_manager.cursor.execute(f"CREATE INDEX text_index ON {self.table_name} (text) INDEXTYPE IS CTXSYS.CONTEXT")
        except:
            raise