# Building a Standalone GraalPy Application with Oracle Database Connectivity

This demo shows a small [Flask](https://flask.palletsprojects.com/) application running on **GraalPy** and connecting to a local [Oracle Database Free](https://www.oracle.com/database/free/get-started/) instance using the **python-oracledb** driver.

> The [python-oracledb driver](https://oracle.github.io/python-oracledb/) is the open-source Python module allowing Python programs to connect directly to Oracle Database with no extra libraries needed.

This project provides a local baseline application for **GraalPy** that can also be compiled into a native standalone executable.
The application exposes one `GET` HTTP endpoint, runs `select 'Hello from Oracle' from dual`,  and returns the result as JSON.

## Prerequisites

* [GraalPy 25.0.2](https://www.graalvm.org/python/python-developers/docs/#installation)
* [GraalVM 25.0.2](https://www.graalvm.org/downloads/)
* Docker installed and running

## Start Oracle Database Free

1. Log in to Oracle Container Registry:
   ```bash
   docker login container-registry.oracle.com
   ```

   > If you do not want to log in to Oracle Container Registry, you can use the community-maintained Docker Hub image [gvenzl/oracle-free](https://hub.docker.com/r/gvenzl/oracle-free) instead.

2. Pull the Oracle Database Free image:
   ```bash
   docker pull container-registry.oracle.com/database/free:latest
   ```

3. Start the database:
   ```bash
   docker run -d \
     --name oracle-free \
     -p 1521:1521 \
     -e ORACLE_PWD=oraclepwd \
     container-registry.oracle.com/database/free:latest
   ```

4. Wait for the database to become ready:
   ```bash
   docker logs -f oracle-free
   ```
   Wait until the logs show a readiness message such as `DATABASE IS READY TO USE!`, then press `Ctrl+C`.

5. Create the application user:
   ```bash
   docker exec -i oracle-free sqlplus system/oraclepwd@FREEPDB1 < create-app-user.sql
   ```

Default values for `ORACLE_USER`, `ORACLE_PASSWORD`, `ORACLE_DSN`, and `PORT` are already specified in the application, so no further configuration is necessary for this demo.

## Run from Source with GraalPy

1. Create a GraalPy virtual environment:
   ```bash
   ${GRAALPY:-graalpy} -m venv target/venv
   ```

2. Install the Python dependencies:
   ```bash
   target/venv/bin/graalpy -m pip install -r requirements.txt
   ```

3. Start the application:
   ```bash
   target/venv/bin/graalpy app.py
   ```

4. Test the endpoint from another terminal:
   ```bash
   curl http://localhost:8080/
   ```

   You should see this response:
   ```json
   {"message":"Hello from Oracle"}
   ```

## Build a Native Standalone Executable

For building a Python standalone application, native executable, ensure `JAVA_HOME` and `PATH` are set to the necessary GraalVM version.

The GraalPy command to build a standalone executable is:
```bash
graalpy -m standalone native \
  --module app.py \
  --output target/standalone-app \
  --venv target/venv
```

For convenience, the script _build-native.sh_ is provided for you: it creates a virtual environment in _target/venv_, installs Flask and other required libraries, and builds a native standalone executable.

1. Run it:
   ```bash
   ./build-native.sh
   ```
   Building a native executable takes a few minutes.
   The file is created at `target/standalone-app`.

2. Start this native application:
   ```bash
   ./target/standalone-app
   ```

3. Test it the same way as before:
   ```bash
   curl http://localhost:8080/
   ```

## Next Steps

This demo showed a Flask application packaged as a GraalPy native executable, connecting to Oracle Database using the `python-oracledb` driver.
Next, you can extend it by adding CRUD endpoints, implementing database interactions with parameterized queries, and introducing connection pooling for more realistic usage.