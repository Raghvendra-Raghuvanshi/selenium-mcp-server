#!/bin/bash

# Test the Selenium MCP server
echo "Testing Selenium MCP server..."

# Start the server with SSE transport
java -jar target/selenium-mcp-0.0.1.jar --port 8931 &
SERVER_PID=$!

# Wait for the server to start
sleep 2

# Send a test message to the server
curl -N -H "Accept: text/event-stream" http://localhost:8931/sse &
CURL_PID=$!

# Wait a moment
sleep 2

# Send an initialize message
echo "Sending initialize message..."
curl -X POST -H "Content-Type: application/json" -d '{"type":"initialize","id":"test-1"}' http://localhost:8931/sse

# Wait a moment
sleep 2

# Send a browser_navigate message
echo "Sending browser_navigate message..."
curl -X POST -H "Content-Type: application/json" -d '{"type":"toolCall","id":"test-2","name":"browser_navigate","params":{"url":"https://www.google.com"}}' http://localhost:8931/sse

# Wait a moment
sleep 5

# Send a browser_snapshot message
echo "Sending browser_snapshot message..."
curl -X POST -H "Content-Type: application/json" -d '{"type":"toolCall","id":"test-3","name":"browser_snapshot","params":{}}' http://localhost:8931/sse

# Wait a moment
sleep 2

# Send a browser_close message
echo "Sending browser_close message..."
curl -X POST -H "Content-Type: application/json" -d '{"type":"toolCall","id":"test-4","name":"browser_close","params":{}}' http://localhost:8931/sse

# Wait a moment
sleep 2

# Clean up
kill $CURL_PID
kill $SERVER_PID

echo "Test completed."
