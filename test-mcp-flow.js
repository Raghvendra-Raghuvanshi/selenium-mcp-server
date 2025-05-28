#!/usr/bin/env node

import { PuppeteerBrowserAutomation } from './dist/index.js';

async function testMCPFlow() {
  console.log('🚀 Testing complete MCP browser automation flow...');
  
  const automation = new PuppeteerBrowserAutomation({
    headless: false
  });
  
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
    
    // Step 3: Find search box and type
    console.log('\n⌨️  Step 3: Type in search box...');
    
    // Try with element index first (from snapshot)
    try {
      const typeResult = await automation.type('Google search box', 'element-7', 'Tom Cruise', true);
      console.log('✅', typeResult);
    } catch (error) {
      console.log('❌ Element index failed, trying CSS selector...');
      
      // Fallback to CSS selector
      try {
        const typeResult = await automation.type('Google search box', 'textarea[name="q"]', 'Tom Cruise', true);
        console.log('✅', typeResult);
      } catch (error2) {
        console.log('❌ CSS selector failed, trying input selector...');
        
        // Another fallback
        const typeResult = await automation.type('Google search box', 'input[name="q"]', 'Tom Cruise', true);
        console.log('✅', typeResult);
      }
    }
    
    // Step 4: Wait and get final snapshot
    console.log('\n⏳ Step 4: Wait for results and get final snapshot...');
    await automation.waitFor({ time: 3 });
    
    const finalSnapshot = await automation.getSnapshot();
    console.log('✅ Final snapshot:');
    console.log(finalSnapshot.substring(0, 500) + '...');
    
  } catch (error) {
    console.error('❌ Error in test flow:', error.message);
  } finally {
    console.log('\n🔄 Cleaning up...');
    await automation.cleanup();
  }
}

testMCPFlow().catch(console.error);
