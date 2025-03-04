FROM ubuntu:22.04

# Prevent interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install basic dependencies
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    git \
    gcc \
    g++ \
    make \
    binutils \
    zlib1g-dev \
    libstdc++-10-dev \
    default-jdk \
    && rm -rf /var/lib/apt/lists/*

# Install GraalVM
ENV GRAALVM_VERSION="22.3.2"
ENV JAVA_VERSION="java17"
ENV GRAALVM_PKG="graalvm-ce-${JAVA_VERSION}-linux-aarch64-${GRAALVM_VERSION}.tar.gz"

RUN cd /tmp && \
    wget https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-${GRAALVM_VERSION}/${GRAALVM_PKG} && \
    tar -xzf ${GRAALVM_PKG} && \
    mv graalvm-ce-${JAVA_VERSION}-${GRAALVM_VERSION} /opt/graalvm && \
    rm ${GRAALVM_PKG}

# Set JAVA_HOME and PATH
ENV JAVA_HOME="/opt/graalvm"
ENV PATH="$JAVA_HOME/bin:$PATH"

# Install native-image
RUN gu install native-image

# Install Node.js
RUN curl -fsSL https://deb.nodesource.com/setup_16.x | bash - && \
    apt-get install -y nodejs

# Install pre-built hsdis
COPY hsdis/hsdis-aarch64.so /opt/graalvm/lib/
RUN chmod +x /opt/graalvm/lib/hsdis-aarch64.so

# Set up application directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src
COPY frontend frontend

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the application
CMD ["./mvnw", "spring-boot:run"]
