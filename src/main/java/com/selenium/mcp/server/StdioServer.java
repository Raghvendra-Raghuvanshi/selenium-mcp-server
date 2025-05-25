package com.selenium.mcp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * MCP server that communicates via standard input/output.
 */
public class StdioServer extends MCPServer {
    private static final Logger logger = LoggerFactory.getLogger(StdioServer.class);
    private final PrintWriter stdout;

    public StdioServer(ServerConfig config) {
        super(config);
        this.stdout = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
    }

    @Override
    public void start() throws Exception {
        logger.info("Starting Selenium MCP server with stdio transport");
        
        // Send ready message
        sendMessage("{\"type\":\"ready\"}");
        
        // Read from stdin
        try (BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = stdin.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                logger.debug("Received message: {}", line);
                handleMessage(line);
            }
        } catch (Exception e) {
            logger.error("Error reading from stdin", e);
            throw e;
        } finally {
            // Clean up resources
            try {
                browserManager.close();
            } catch (Exception e) {
                logger.error("Error closing browser manager", e);
            }
        }
    }

    @Override
    protected void sendMessage(String message) {
        logger.debug("Sending message: {}", message);
        stdout.println(message);
        stdout.flush();
    }
}
