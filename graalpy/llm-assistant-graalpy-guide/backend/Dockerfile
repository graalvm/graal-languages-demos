FROM container-registry.oracle.com/os/oraclelinux:8

# Install system dependencies
RUN dnf update -y && \
    dnf install -y \
    curl \
    gcc-c++ \
    make \
    bash \
    git \
    unzip \
    zip \
    openssl-devel \
    bzip2-devel \
    libffi-devel \
    readline-devel \
    sqlite-devel \
    zlib-devel \
    patch

# Install SDKMAN
RUN curl -s "https://get.sdkman.io" | bash

SHELL ["/bin/bash", "-c"]

# Install GraalVM 23 using SDKMAN and set JAVA_HOME
RUN source "$HOME/.sdkman/bin/sdkman-init.sh" && \
    sdk install java 23-graal && \
    sdk use java 23-graal

ENV JAVA_HOME="/root/.sdkman/candidates/java/23-graal"
ENV PATH="$JAVA_HOME/bin:$PATH"

# Install Rust (for Pydantic dependency)
RUN curl https://sh.rustup.rs -sSf | bash -s -- -y
ENV PATH="/root/.cargo/bin:$PATH"

WORKDIR /app
COPY . .

EXPOSE 8080
RUN ./mvnw package -DskipTests
ENTRYPOINT ["java", "-jar", "/app/target/backend-0.1.jar"]