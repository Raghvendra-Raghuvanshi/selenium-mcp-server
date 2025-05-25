package com.selenium.mcp.server;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MCP server that communicates via Server-Sent Events (SSE).
 */
public class SSEServer extends MCPServer {
    private static final Logger logger = LoggerFactory.getLogger(SSEServer.class);
    private final String host;
    private final int port;
    private final Queue<String> messageQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private Undertow server;
    private HttpServerExchange exchange;

    public SSEServer(ServerConfig config, String host, int port) {
        super(config);
        this.host = host;
        this.port = port;
    }

    @Override
    public void start() throws Exception {
        logger.info("Starting Selenium MCP server with SSE transport on {}:{}", host, port);

        // Create Undertow server
        server = Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(getHandler())
                .build();

        // Start server
        server.start();

        logger.info("Selenium MCP server started on http://{}:{}/sse", host, port);

        // Keep server running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            logger.info("Server interrupted");
        } finally {
            // Clean up resources
            try {
                browserManager.close();
            } catch (Exception e) {
                logger.error("Error closing browser manager", e);
            }

            server.stop();
        }
    }

    private HttpHandler getHandler() {
        return exchange -> {
            if (exchange.getRequestPath().equals("/sse")) {
                handleSSE(exchange);
            } else if (exchange.getRequestPath().equals("/")) {
                handleRoot(exchange);
            } else {
                exchange.setStatusCode(404);
                exchange.getResponseSender().send("Not Found");
            }
        };
    }

    private void handleSSE(HttpServerExchange exchange) {
        if (exchange.getRequestMethod().toString().equals("GET")) {
            // Handle GET request for SSE connection
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/event-stream");
            exchange.getResponseHeaders().put(Headers.CACHE_CONTROL, "no-cache");
            exchange.getResponseHeaders().put(Headers.CONNECTION, "keep-alive");
            exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

            exchange.setPersistent(true);
            exchange.dispatch(exchange.getIoThread());

            // Set up SSE connection
            this.exchange = exchange;
            connected.set(true);

            // Send ready message
            sendMessage("{\"type\":\"ready\"}");

            // Set up close listener
            exchange.addExchangeCompleteListener((ex, nextListener) -> {
                logger.info("SSE connection closed");
                connected.set(false);
                this.exchange = null;
                nextListener.proceed();
            });

            // Send any queued messages
            while (!messageQueue.isEmpty()) {
                String message = messageQueue.poll();
                sendSSEMessage(exchange, message);
            }
        } else if (exchange.getRequestMethod().toString().equals("POST")) {
            // Handle POST request for sending messages
            exchange.getRequestReceiver().receiveFullString((ex, message) -> {
                if (message != null && !message.trim().isEmpty()) {
                    logger.debug("Received message: {}", message);
                    try {
                        handleMessage(message);
                    } catch (Exception e) {
                        logger.error("Error handling message", e);
                    }
                }
                exchange.endExchange();
            });
        } else {
            // Method not allowed
            exchange.setStatusCode(405);
            exchange.getResponseSender().send("Method Not Allowed");
        }
    }

    private void handleRoot(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
        exchange.getResponseSender().send(
                "<html><head><title>Selenium MCP Server</title></head>" +
                "<body><h1>Selenium MCP Server</h1>" +
                "<p>Server is running. Connect to <code>/sse</code> endpoint for SSE transport.</p>" +
                "</body></html>"
        );
    }

    @Override
    protected void sendMessage(String message) {
        if (connected.get() && exchange != null) {
            sendSSEMessage(exchange, message);
        } else {
            // Queue message for later
            messageQueue.add(message);
        }
    }

    private void sendSSEMessage(HttpServerExchange exchange, String message) {
        try {
            logger.debug("Sending SSE message: {}", message);
            String formattedMessage = "data: " + message + "\n\n";
            exchange.getResponseSender().send(ByteBuffer.wrap(formattedMessage.getBytes()));
        } catch (Exception e) {
            logger.error("Error sending SSE message", e);
        }
    }
}
