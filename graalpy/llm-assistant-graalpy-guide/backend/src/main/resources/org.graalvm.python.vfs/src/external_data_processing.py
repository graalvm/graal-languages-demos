import requests
from bs4 import BeautifulSoup
from langchain_ollama import ChatOllama
from langchain_ollama import OllamaEmbeddings
import logging
import cx_Oracle
from langchain.schema import Document

from vector_store_manager import VectorStoreManager
from prepare_initial_data import InitialData


logger = logging.getLogger(__name__)

class ExternalData:
    def __init__(self, table_name:str):
        self.table_name=table_name
        self.embedding_model = OllamaEmbeddings(model="snowflake-arctic-embed2")
        self.vector_store_manager = VectorStoreManager(table_name)
        self.vector_store_manager.get_cursor()
        self.initial_data_class = InitialData(table_name)




    def is_graalpy_related(self, url):
            """
            checks if url content related to graalPy or not
            """
            try:
                response = requests.get(
                    url,
                    headers={"User-Agent": "Mozilla/5.0"},
                    timeout=10)
                response.raise_for_status()


                soup = BeautifulSoup(response.text, "html.parser")
                text = soup.get_text()

                if not text or len(text.split()) < 20:
                    return False

                max_length = 5000
                text = text[:max_length]

                prompt = f"""
                    Check this content and verify if it's related to GraalPy or not.
                    Answer with **only** the word "True" or "False".
                    
                    Content: {text}
                """

                llm = ChatOllama(
                    model="llama3.2",
                    temperature=0
                )

                response = llm.invoke(prompt).content.strip().lower()

                return response == "true"


            except requests.exceptions.RequestException as e:
                logger.error(f"Error fetching URL: {e}")
                return False



    def __check_source(self, url):
        """
        Checks if the URL is NOT already present in the metadata JSON column
        of the given table.
        """
        cursor = self.vector_store_manager.cursor
        try:
            query = f"""
                SELECT COUNT(*) FROM {self.table_name}
                WHERE JSON_VALUE(metadata, '$.source') = :url
            """
            cursor.execute(query, {"url": url})
            count = cursor.fetchone()[0]
            return count == 0
        except cx_Oracle.DatabaseError as e:
            logger.error(f"Database error during query: {e}")



    def __is_new_chunk(self, chunk, vector_store):
        """
        Checks if the chunks Not already exists in the vector store
        Returns True if it's a new chunk, false otherwise
        """
        embedding = self.embedding_model.embed_query(chunk.page_content)
        results = vector_store.similarity_search_by_vector_with_relevance_scores(embedding, k=1)
        return not results or results[0][1] < 0.9



    def add_url(self, url):
        """the process to add new url"""
        if not self.__check_source(url) :
            return False
        docs = self.initial_data_class.load_data_from_url(url)
        chunks = self.initial_data_class.split_documents(docs)
        vector_store = self.vector_store_manager.create_vector_store()
        chunks_to_store=[]
        for chunk in chunks:
            # store only the chunks that are not in the vector store
            if self.__is_new_chunk(chunk, vector_store):
                chunks_to_store.append(chunk)

        vector_store.add_documents(chunks_to_store)
        return True


    def add_new_text(self, text):
        if text is None:
            raise ValueError("Expected a Text")
        documents =  [Document(page_content=text)]
        chunks = self.initial_data_class.split_documents(documents)
        vector_store = self.vector_store_manager.create_vector_store()
        vector_store.add_documents(chunks)












