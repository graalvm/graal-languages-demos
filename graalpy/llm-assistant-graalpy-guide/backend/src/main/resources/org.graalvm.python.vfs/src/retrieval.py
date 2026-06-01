import yake
import re
from langchain.schema import Document
import cohere
import json
import os
import logging
from langchain_ollama import OllamaEmbeddings
from vector_store_manager import VectorStoreManager

logger = logging.getLogger(__name__)
co = cohere.Client(os.getenv("COHERE_API_KEY"))


class Retrieval:
    def __init__(self, table_name: str):
        self.embedding_model = OllamaEmbeddings(model="snowflake-arctic-embed2")
        self.table_name = table_name
        self.vector_store_manager = VectorStoreManager(table_name)
        self.vector_store_manager.get_cursor()


    def __extract_keywords(self, query, num_results):
        """
        Extracts keywords from a string using YAKE.

        @param query: The string from which keywords should be extracted.
        @param num_results: The number of keywords/phrases to return.

        @returns: List of keywords/phrases with their scores.
        """
        language = "en"
        max_ngram_size = 1
        windowSize = 1

        kw_extractor = yake.KeywordExtractor(
            lan=language,
            n=max_ngram_size,
            windowsSize=windowSize,
            top=num_results,
            features=None
        )
        keywords = kw_extractor.extract_keywords(query.strip().lower())
        return sorted(keywords, key=lambda kw: kw[1])


    def __retrieve_documents_by_keywords(self, query, num_results):
        """
        Retrieves documents from the database that have the highest density of matching keywords.

        @param table_name: The table in the database containing the documents.
        @param query: The input query to extract keywords from.
        @param num_results: The number of results to return.

        @returns: List of Document objects matching the keywords.
        """
        num_keywords = 4
        keywords = self.__extract_keywords(query, num_keywords)
        if not keywords:
            return []

        search_sql = f"""
            SELECT id, text, metadata, SCORE(1)
            FROM {self.table_name}
            WHERE CONTAINS(text, :query_keywords, 1) > 0
            ORDER BY SCORE(1) DESC
        """

        stemmed_keywords = []
        splitter = re.compile(r'[^a-zA-Z0-9_+\-/]')
        for keyword in keywords:
            words = splitter.split(keyword[0])
            stemmed_keywords.append(" ".join(words).strip())

        formatted_keywords = " OR ".join(f'"{kw}"' for kw in stemmed_keywords)

        cursor = self.vector_store_manager.cursor
        try:
            cursor.execute(search_sql, {"query_keywords": formatted_keywords})
        except Exception as e:
            logger.exception(f"Keyword search query failed: {e}")
            raise

        rows = cursor.fetchall()
        documents = []
        for row in rows:
            clob_data = row[1].read() if hasattr(row[1], "read") else row[1]
            clob_data = clob_data.decode('utf-8') if isinstance(clob_data, bytes) else clob_data

            metadata = row[2] if isinstance(row[2], dict) else {}
            if 'README' in metadata.get('source', '').upper() or 'README' in clob_data.upper():
                continue #ignore readme files

            documents.append(Document(id=row[0], page_content=clob_data, metadata=metadata))

        if len(documents) > num_results:
            return documents[:num_results]
        else:
            return documents



    def __retrieve_documents_by_vector_similarity(self, query, num_results):
        """
        Retrieves the most similar documents from the database based upon semantic similarity.
        """
        query_embedding = self.embedding_model.embed_query(query)
        query_embedding_str = json.dumps(query_embedding)

        search_sql = f"""
        SELECT id, text, metadata,
               vector_distance(embedding, :embedding) as distance
        FROM {self.table_name}
        ORDER BY distance
        FETCH FIRST {num_results} ROWS ONLY
        """

        cursor = self.vector_store_manager.cursor
        try:
            cursor.execute(search_sql, {'embedding': query_embedding_str})
        except Exception as e:
            logger.error(f"SQL Execution failed: {e}")
            raise

        rows = cursor.fetchall()
        if not rows:
            return []

        documents = []
        for row in rows:
            clob_data = row[1].read() if hasattr(row[1], "read") else row[1]
            clob_data = clob_data.decode('utf-8') if isinstance(clob_data, bytes) else clob_data

            metadata = row[2] if isinstance(row[2], dict) else {}
            documents.append(Document(id=row[0], page_content=clob_data, metadata=metadata))

        return documents



    def __deduplicate_documents(self, *args):
        """
        Combines lists of documents returning a union of the lists, with duplicates removed (based upon an 'id' match).
        Arguments:
            Any number of arrays of Document objects
        @returns: Array<Documents> Single array of documents containing no duplicates
        """
        seen_ids = set()
        documents = []

        for document_list in args:
            for document in document_list:
                if document.id not in seen_ids:
                    documents.append(document)
                    seen_ids.add(document.id)

        return documents

    def __rerank_documents(self, documents, query, num_results=5):
        """
        Rerank documents using Cohere's Rerank API to assess relevance.

        @param documents: List of Document objects to rerank.
        @param query: The query used for the original search.
        @param num_results: The number of top documents to return after reranking.

        @returns: List of reranked documents, limited to the top `num_results`.
        """
        valid_documents = [
            {"id": doc.id, "text": doc.page_content.strip()}
            for doc in documents if doc.page_content.strip() != ""
        ]

        if not valid_documents:
            return []

        try:
            response = co.rerank(
                model="rerank-v3.5",
                query=query,
                documents=valid_documents,
                top_n=num_results
            )

            reranked_documents = []
            for result in response.results:
                index = result.index
                reranked_documents.append(documents[index])

            return reranked_documents[:num_results]

        except cohere.error.CohereAPIError as e:
            raise RuntimeError("Reranking failed due to Cohere API error") from e

    def hybrid_search(self, query, num_results=5):
        """
        Perform a hybrid search by combining keyword-based search and vector similarity search, then rerank the results.

        @param query: The search query.
        @param num_results: The number of results to return.
        @param table_name: The database table where documents are stored.

        @returns: List of reranked documents.
        """
        keyword_documents = self.__retrieve_documents_by_keywords(query, num_results)
        vector_documents = self.__retrieve_documents_by_vector_similarity(query, num_results)
        combined_documents = self.__deduplicate_documents(keyword_documents, vector_documents)
        return self.__rerank_documents(combined_documents, query, num_results)
