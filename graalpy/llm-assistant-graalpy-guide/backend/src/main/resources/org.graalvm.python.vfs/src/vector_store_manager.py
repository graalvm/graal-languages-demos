import os
import logging
import oracledb
from langchain_community.vectorstores.oraclevs import OracleVS
from langchain_community.vectorstores.utils import DistanceStrategy
from langchain_ollama import OllamaEmbeddings

logger = logging.getLogger(__name__)

class VectorStoreManager:
    def __init__(self, table_name: str):
        self.oracle_connection = None
        self.cursor = None
        self.embedding_model = OllamaEmbeddings(
            model="snowflake-arctic-embed2"
        )
        self.table_name = table_name

    def __get_oracle_connection(self):
        """Ensure OracleDB connection is established and return it."""
        if self.oracle_connection is None:
            user = os.environ.get("USER")
            password = os.environ.get("PASSWORD")
            dsn = os.environ.get("DSN")

            if not all([user, password, dsn]):
                raise ValueError("Environment variables 'USER', 'PASSWORD', and 'DSN' must be set")

            try:
                self.oracle_connection = oracledb.connect(user=user, password=password, dsn=dsn)
            except oracledb.DatabaseError as e:
                raise ConnectionError(f"OracleDB Connection failed: {e}")

        return self.oracle_connection

    def get_cursor(self):
        """Ensure cursor is initialized and return it."""
        if self.cursor is None:
            connection = self.__get_oracle_connection()
            try:
                self.cursor = connection.cursor()
            except oracledb.DatabaseError as e:
                raise RuntimeError("Failed to create OracleDB cursor") from e

        return self.cursor

    def create_vector_store(self):
        """
        Create a vector store using OracleVS with the provided table name.
        """
        connection = self.__get_oracle_connection()
        return OracleVS(
            client=connection,
            embedding_function=self.embedding_model,
            table_name=self.table_name,
            distance_strategy=DistanceStrategy.COSINE,
        )




