#!/usr/bin/env node

import { spawn } from 'child_process';
import { fileURLToPath } from 'url';
import path from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

async function testMCPProtocol() {
  console.log('🚀 Testing MCP protocol with proper initialization...');
  
  const serverProcess = spawn('node', ['dist/server.js'], {
    cwd: path.join(__dirname),
    stdio: ['pipe', 'pipe', 'pipe']
  });

  let responseBuffer = '';
  
  serverProcess.stdout.on('data', (data) => {
    responseBuffer += data.toString();
    console.log('📥 Server response:', data.toString().trim());
  });

  serverProcess.stderr.on('data', (data) => {
    console.log('📝 Server log:', data.toString().trim());
  });

  // Helper function to send JSON-RPC request
  function sendRequest(request) {
    const requestStr = JSON.stringify(request) + '\n';
    console.log('📤 Sending:', requestStr.trim());
    serverProcess.stdin.write(requestStr);
  }

  // Wait a bit for server to start
  await new Promise(resolve => setTimeout(resolve, 1000));

  try {
    // Step 1: Initialize the MCP connection
    console.log('\n🔧 Step 1: Initialize MCP connection...');
    sendRequest({
      jsonrpc: "2.0",
      id: 1,
      method: "initialize",
      params: {
        protocolVersion: "2024-11-05",
        capabilities: {
          tools: {}
        },
        clientInfo: {
          name: "test-client",
          version: "1.0.0"
        }
      }
    });

    // Wait for initialization response
    await new Promise(resolve => setTimeout(resolve, 2000));

    // Step 2: Send initialized notification
    console.log('\n✅ Step 2: Send initialized notification...');
    sendRequest({
      jsonrpc: "2.0",
      method: "notifications/initialized"
    });

    await new Promise(resolve => setTimeout(resolve, 1000));

    // Step 3: List tools
    console.log('\n📋 Step 3: List available tools...');
    sendRequest({
      jsonrpc: "2.0",
      id: 2,
      method: "tools/list"
    });

    await new Promise(resolve => setTimeout(resolve, 2000));

    // Step 4: Navigate to Google
    console.log('\n🌐 Step 4: Navigate to Google...');
    sendRequest({
      jsonrpc: "2.0",
      id: 3,
      method: "tools/call",
      params: {
        name: "browser_navigate",
        arguments: {
          url: "https://www.google.com"
        }
      }
    });

    await new Promise(resolve => setTimeout(resolve, 5000));

    // Step 5: Get page snapshot
    console.log('\n📸 Step 5: Get page snapshot...');
    sendRequest({
      jsonrpc: "2.0",
      id: 4,
      method: "tools/call",
      params: {
        name: "browser_snapshot",
        arguments: {}
      }
    });

    await new Promise(resolve => setTimeout(resolve, 3000));

    // Step 6: Type in search box
    console.log('\n⌨️  Step 6: Type in search box...');
    sendRequest({
      jsonrpc: "2.0",
      id: 5,
      method: "tools/call",
      params: {
        name: "browser_type",
        arguments: {
          element: "Google search box",
          ref: "element-7",
          text: "Tom Cruise",
          submit: true
        }
      }
    });

    await new Promise(resolve => setTimeout(resolve, 5000));

    console.log('\n✅ Test completed! Check the responses above.');

  } catch (error) {
    console.error('❌ Error during test:', error);
  } finally {
    console.log('\n🔄 Cleaning up...');
    serverProcess.kill();
  }
}

testMCPProtocol().catch(console.error);
