# Selenium MCP Server Docker Image
FROM openjdk:11-jre-slim

# Install Chrome and dependencies
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    unzip \
    curl \
    && wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list \
    && apt-get update \
    && apt-get install -y google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

# Create app directory
WORKDIR /app

# Copy the JAR file
COPY target/selenium-mcp-0.0.1.jar /app/selenium-mcp.jar

# Create screenshots and pdfs directories
RUN mkdir -p /app/screenshots /app/pdfs

# Expose port
EXPOSE 8931

# Set environment variables
ENV DISPLAY=:99
ENV CHROME_BIN=/usr/bin/google-chrome
ENV CHROME_PATH=/usr/bin/google-chrome

# Run the server
CMD ["java", "-jar", "/app/selenium-mcp.jar", "--port", "8931", "--host", "0.0.0.0", "--browser", "chrome", "--headless"]
