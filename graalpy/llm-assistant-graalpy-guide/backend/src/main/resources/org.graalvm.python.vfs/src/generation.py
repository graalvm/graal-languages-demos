from langchain_ollama import ChatOllama
from langchain_core.documents import Document
from langchain.chat_models import init_chat_model


class State:
    def __init__(self, question: str, context: str):
        self.question = question
        self.context = context
        self.category = None


class GenerateAnswer:
    def __init__(self):
        self.classifier_llm = ChatOllama(model="llama3.2", temperature=0)
        self.general_llm = init_chat_model("meta-llama/llama-4-scout-17b-16e-instruct", model_provider="groq")
        self.coding_llm = init_chat_model("llama3-70b-8192", model_provider="groq")

    def __classify_question(self, state: State) -> State:
        """Function to classify the question category"""
        prompt_template = f"""
            Classify the following user question into one of these categories:
            
                - "coding" (if the question asks for **writing, generating, debugging, fixing, troubleshooting, providing an example of** code, or contains an **extract of code**)
                - "general" (for all other questions, including programming concepts, comparisons, and explanations)
            
            Use "coding" for questions that involve:
                - Code generation, debugging, fixing issues, troubleshooting, code examples, or any other code-specific help.
                - Containing an **extract of code** (e.g., 'Here’s my code, can you help me fix this?')
            
            Use "general" for questions about:
                - Programming concepts, theory, comparisons, best practices, tools, software architecture, or non-code-related inquiries.
            
            User Question: "{state.question}"
            
            Respond with **only** the word "coding" or "general".
        """
        response = self.classifier_llm.invoke(prompt_template).content.strip().lower()
        state.category = response if response in {"coding", "general"} else "general"
        return state

    def __general_assistant(self, state: State) -> str:
        """Function to answer general questions"""
        custom_prompt = f"""
        You are GraalPy AI Assistant, specialized in providing accurate and helpful answers **strictly based on GraalPy-related context**.
        
        Rules:
        - If **no context** or **irrelevant context** is provided, respond **only** with:
            "Sorry, I don't have enough information to answer that."
        
        - **Do not** infer, assume, or generate responses beyond the provided GraalPy context.
        
        - **Do not** provide explanations about missing information—simply deny the response if no valid GraalPy context is available.
        
        - If context **is provided and relevant to GraalPy**, answer **clearly, concisely, and accurately**, ensuring the explanation is well-structured and contains all necessary details.
        
        - If the user greets you (e.g., "hello", "hi", "hey"), respond naturally with:
             "Hello!" or "Hi there! How can I assist you?"
        
        - If the user's query is **unrelated to GraalPy**, respond with:
            "I specialize in GraalPy. Please provide a GraalPy-related question."
        
        User Input:
        {state.question}
        
        Context:
        {state.context}
        
        Response:
        """
        messages = [{"role": "system", "content": custom_prompt}]
        response = self.general_llm.invoke(messages)
        return response.content

    def __coding_assistant(self, state: State) -> str:
        """Function to answer coding questions"""
        custom_prompt = f"""
        You are a coding assistant with expertise in GraalPy, Python, and Java. Your goal is to provide precise, functional, and well-structured responses to coding-related queries.
    
        Instructions:
        Use the provided context to enhance accuracy and relevance.
        
        Ensure that all provided code is executable, with all required imports and variable definitions included.
        
        Structure your response as follows:
        
        Solution Explanation: A clear, concise description of the approach.
        
        Required Imports: List all necessary imports.
        
        Complete Code Block: Provide a functional, optimized solution.
    
        User Question:
        {state.question}
        
        Context:
        {state.context}
        
        Response:
        """
        messages = [{"role": "system", "content": custom_prompt}]
        response = self.coding_llm.invoke(messages)
        return response.content

    def process_question(self, question: str, docs: Document) -> str:
        """Entry point to process a question and generate a response"""
        docs_content = "\n\n".join(doc.page_content for doc in docs)
        state = State(question, docs_content)
        self.__classify_question(state)
        if state.category == "coding":
            result = self.__coding_assistant(state)
        else:
            result = self.__general_assistant(state)
        return result
