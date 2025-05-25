package com.selenium.mcp.server;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Selenium MCP server.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            // Parse command line options
            CommandLineParser parser = new DefaultParser();
            Options options = createOptions();
            CommandLine cmd = parser.parse(options, args);

            // Display help if requested
            if (cmd.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("selenium-mcp", options);
                return;
            }

            // Determine transport type
            boolean useSSE = cmd.hasOption("port");
            int port = Integer.parseInt(cmd.getOptionValue("port", "8931"));
            String host = cmd.getOptionValue("host", "localhost");

            // Create server configuration
            ServerConfig config = createServerConfig(cmd);

            // Start the server
            if (useSSE) {
                logger.info("Starting Selenium MCP server with SSE transport on {}:{}", host, port);
                new SSEServer(config, host, port).start();
            } else {
                logger.info("Starting Selenium MCP server with stdio transport");
                new StdioServer(config).start();
            }
        } catch (Exception e) {
            logger.error("Failed to start Selenium MCP server", e);
            System.exit(1);
        }
    }

    private static Options createOptions() {
        Options options = new Options();

        // Help option
        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Display help information")
                .build());

        // Server options
        options.addOption(Option.builder()
                .longOpt("port")
                .hasArg()
                .argName("port")
                .desc("Port to listen on for SSE transport (default: 8931)")
                .build());
        options.addOption(Option.builder()
                .longOpt("host")
                .hasArg()
                .argName("host")
                .desc("Host to bind server to (default: localhost)")
                .build());

        // Browser options
        options.addOption(Option.builder()
                .longOpt("browser")
                .hasArg()
                .argName("browser")
                .desc("Browser to use (chrome, firefox, edge, safari)")
                .build());
        options.addOption(Option.builder()
                .longOpt("headless")
                .desc("Run browser in headless mode")
                .build());
        options.addOption(Option.builder()
                .longOpt("executable-path")
                .hasArg()
                .argName("path")
                .desc("Path to browser executable")
                .build());
        options.addOption(Option.builder()
                .longOpt("user-data-dir")
                .hasArg()
                .argName("path")
                .desc("Path to user data directory")
                .build());
        options.addOption(Option.builder()
                .longOpt("isolated")
                .desc("Keep browser profile in memory, do not save to disk")
                .build());

        // Viewport options
        options.addOption(Option.builder()
                .longOpt("viewport-size")
                .hasArg()
                .argName("size")
                .desc("Browser viewport size in pixels, e.g., \"1280,720\"")
                .build());

        // Capabilities
        options.addOption(Option.builder()
                .longOpt("caps")
                .hasArg()
                .argName("caps")
                .desc("Comma-separated list of capabilities to enable (tabs,pdf,history,wait,files,install)")
                .build());

        // Output options
        options.addOption(Option.builder()
                .longOpt("output-dir")
                .hasArg()
                .argName("path")
                .desc("Path to directory for output files")
                .build());

        return options;
    }

    private static ServerConfig createServerConfig(CommandLine cmd) {
        ServerConfig config = new ServerConfig();

        // Browser configuration
        config.setBrowserName(cmd.getOptionValue("browser", "chrome"));
        config.setHeadless(cmd.hasOption("headless"));
        config.setExecutablePath(cmd.getOptionValue("executable-path"));
        config.setUserDataDir(cmd.getOptionValue("user-data-dir"));
        config.setIsolated(cmd.hasOption("isolated"));

        // Viewport configuration
        String viewportSize = cmd.getOptionValue("viewport-size");
        if (viewportSize != null) {
            String[] dimensions = viewportSize.split(",");
            if (dimensions.length == 2) {
                try {
                    int width = Integer.parseInt(dimensions[0].trim());
                    int height = Integer.parseInt(dimensions[1].trim());
                    config.setViewportWidth(width);
                    config.setViewportHeight(height);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid viewport size format: {}", viewportSize);
                }
            }
        }

        // Capabilities
        String caps = cmd.getOptionValue("caps");
        if (caps != null) {
            for (String cap : caps.split(",")) {
                config.addCapability(cap.trim());
            }
        } else {
            // Enable all capabilities by default
            config.addCapability("tabs");
            config.addCapability("history");
            config.addCapability("wait");
            config.addCapability("files");
            config.addCapability("install");
        }

        // Output directory
        config.setOutputDir(cmd.getOptionValue("output-dir"));

        return config;
    }
}
