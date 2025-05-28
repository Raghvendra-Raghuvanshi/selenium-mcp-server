#!/usr/bin/env node

import { BrowserAutomation } from './dist/mcp-server.js';

async function testNewServer() {
  console.log('🚀 Testing new MCP server browser automation...');
  
  const automation = new BrowserAutomation({ headless: false });
  
  try {
    // Step 1: Navigate
    console.log('📍 Step 1: Navigate to Google...');
    const navResult = await automation.navigate('https://www.google.com');
    console.log('✅', navResult);
    
    // Step 2: Get snapshot
    console.log('\n📸 Step 2: Get page snapshot...');
    const snapshot = await automation.getSnapshot();
    console.log('✅ Snapshot result:');
    console.log(snapshot);
    
    // Step 3: Type in search box
    console.log('\n⌨️  Step 3: Type in search box...');
    const typeResult = await automation.type('Google search box', 'element-7', 'Tom Cruise', true);
    console.log('✅', typeResult);
    
    // Step 4: Wait and get final snapshot
    console.log('\n⏳ Step 4: Wait for results...');
    await automation.waitFor({ time: 3 });
    
    const finalSnapshot = await automation.getSnapshot();
    console.log('✅ Final snapshot:');
    console.log(finalSnapshot.substring(0, 500) + '...');
    
  } catch (error) {
    console.error('❌ Error in test:', error.message);
  } finally {
    console.log('\n🔄 Cleaning up...');
    await automation.cleanup();
  }
}

testNewServer().catch(console.error);
