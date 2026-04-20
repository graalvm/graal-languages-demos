# Steps to run the Backend

## 1 Configure Oracle database

### a. Pull & Run Oracle Autonomous Database Free Container

*  Pull the Image

First, pull the Oracle Autonomous Database Free container from Oracle Container Registry:

```bash
docker pull container-registry.oracle.com/database/free:latest
```

*  Run the Container

Run the database with exposed ports:

```bash
docker run -d --name adb-free -p 1521:1521 -p 8443:8443 -e ORACLE_PWD=YourStrongPassword container-registry.oracle.com/database/free:latest 
```

* -p 1521:1521 → Oracle DB listener port
* -p 8443:8443 → For SQL Developer web access
* -e ORACLE_PWD=YourStrongPassword → Sets SYS & ADMIN password

### b. Create a New User

*  Access the Database

Log into the running container:

```bash
docker exec -it adb-free bash
```

Connect to sqlplus as sysdba and alter the session:

```bash
sqlplus / as sysdba
ALTER SESSION SET "_ORACLE_SCRIPT"=TRUE;
```

*  Create a User

Inside SQL*Plus, run:

```bash
CREATE USER your_username IDENTIFIED BY your_password;

-- Grant necessary privileges
GRANT CREATE SESSION, CREATE TABLE, ALTER ANY TABLE, DROP ANY TABLE TO your_username;
```

## 2 Configure the environment variables

You need to set the following environment variables for the project:


- **`GROQ_API_KEY`**: Your GROQ API key.
- **`COHERE_API_KEY`**: Your COHERE API key.
- **`USER`**: Your oracledb username.
- **`PASSWORD`**: Your oracledb password.
- **`DSN`**: The Data Source Name (e.g., `localhost:1521/free`).


You can configure them in the GraalPy context using
```java
.environment("KEY","VALUE")
```

or export them in your terminal

```shell
export GROQ_API_KEY=your_groq_key
export COHERE_API_KEY=your_cohere_key
export USER=your_username
export PASSWORD=your_password
export DSN=localhost:1521/free
```


## 3 Install Ollama Locally

You will also need to download [ollama](https://ollama.com/download) on your machine.
Then,pull the required models and start ollama server:

```shell
ollama run llama2.3 
ollama run snowflake-arctic-embed2
ollama serve
```


# 4 Run the application

Start the Micronaut application using:

```shell
./mvnw mn:run
```
This will start the application on port 8080.










