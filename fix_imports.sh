#!/bin/bash

# Add missing import for ObjectNode to all Java files
find src/main/java -name "*.java" -exec grep -l "ObjectNode" {} \; | xargs -I{} sed -i '' '4i\
import com.fasterxml.jackson.databind.node.ObjectNode;
' {}

# Fix the BrowserManager.java file
sed -i '' 's/driver.executeScript(script, url != null ? url : "about:blank");/((JavascriptExecutor) driver).executeScript(script, url != null ? url : "about:blank");/g' src/main/java/com/selenium/mcp/server/BrowserManager.java

# Fix the BrowserPdfSaveTool.java file
sed -i '' 's/driver.executeScript(/((JavascriptExecutor) driver).executeScript(/g' src/main/java/com/selenium/mcp/server/tools/utility/BrowserPdfSaveTool.java

# Fix the SSEServer.java file
sed -i '' 's/Headers.ACCESS_CONTROL_ALLOW_ORIGIN/new HttpString("Access-Control-Allow-Origin")/g' src/main/java/com/selenium/mcp/server/SSEServer.java
