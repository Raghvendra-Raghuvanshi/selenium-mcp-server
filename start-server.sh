#!/bin/bash

# Selenium MCP Server Startup Script
# This script starts the Selenium MCP server as a standalone HTTP service

PORT=${1:-8931}
HOST=${2:-0.0.0.0}

echo "Starting Selenium MCP Server..."
echo "Host: $HOST"
echo "Port: $PORT"
echo "Access URL: http://$HOST:$PORT/sse"
echo ""
echo "Teams can connect using this URL in their Cursor MCP configuration."
echo ""

# Start the server
java -jar target/selenium-mcp-0.0.1.jar \
  --port $PORT \
  --host $HOST \
  --browser chrome \
  --headless

echo "Server stopped."
