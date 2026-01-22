FROM ollama/ollama

RUN bash -c "\
    ollama serve & \
    sleep 5 && \
    ollama pull llama3.2 && \
    ollama pull snowflake-arctic-embed2 && \
    pkill ollama"

EXPOSE 11434

CMD ["serve"]
