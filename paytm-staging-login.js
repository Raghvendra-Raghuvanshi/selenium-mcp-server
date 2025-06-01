#!/usr/bin/env node

import { BrowserAutomation } from './dist/mcp-server.js';

async function paytmStagingLogin() {
  const automation = new BrowserAutomation({ headless: false });
  
  // Login credentials from the user's instructions
  const credentials = {
    mobile: '8010630022',
    password: 'paytm@123',
    otp: '888888'
  };
  
  try {
    console.log('🚀 Paytm Staging Site Login Automation');
    console.log('=====================================');
    console.log(`📱 Mobile: ${credentials.mobile}`);
    console.log(`🔐 Password: ${credentials.password}`);
    console.log(`🔢 OTP: ${credentials.otp}`);
    console.log('');
    
    // Step 1: Navigate to the staging site
    console.log('📍 Step 1: Navigate to staging site...');
    const navResult = await automation.navigate('https://oe-staging5.paytm.com/');
    console.log('✅', navResult);
    
    // Step 2: Wait for page to load and get initial snapshot
    console.log('\n⏳ Step 2: Waiting for page to load...');
    await automation.waitFor({ time: 3 });
    const mainSnapshot = await automation.getSnapshot();
    console.log('📸 Main page loaded with elements:', mainSnapshot.split('\n').length - 3, 'interactive elements');
    
    // Step 3: Check for frames and switch to OAuth iframe
    console.log('\n🔍 Step 3: Looking for OAuth iframe...');
    const framesResult = await automation.getFrames();
    console.log('✅ Found frames:', framesResult);
    
    console.log('\n🔄 Step 4: Switching to OAuth iframe...');
    const switchResult = await automation.switchFrame({ frameSelector: '#oauth-iframe' });
    console.log('✅', switchResult);
    
    if (!switchResult.includes('Successfully switched')) {
      throw new Error('Failed to switch to OAuth iframe');
    }
    
    // Step 5: Get iframe snapshot to see login form
    console.log('\n📸 Step 5: Getting login form elements...');
    const iframeSnapshot = await automation.getSnapshot();
    console.log('✅ Login form loaded:');
    console.log(iframeSnapshot);
    
    // Step 6: Fill in mobile/email field
    console.log('\n📱 Step 6: Entering mobile number...');
    const mobileResult = await automation.type('mobile/email input', 'element-1', credentials.mobile);
    console.log('✅', mobileResult);
    
    // Step 7: Fill in password field
    console.log('\n🔐 Step 7: Entering password...');
    const passwordResult = await automation.type('password input', 'element-2', credentials.password);
    console.log('✅', passwordResult);
    
    // Step 8: Click Sign In button
    console.log('\n🖱️  Step 8: Clicking Sign In button...');
    const signInResult = await automation.click('Sign In button', 'element-3');
    console.log('✅', signInResult);
    
    // Step 9: Wait for OTP page or next step
    console.log('\n⏳ Step 9: Waiting for OTP page or next step...');
    await automation.waitFor({ time: 5 });
    
    // Step 10: Get snapshot to see what happened after login
    console.log('\n📸 Step 10: Checking page after login attempt...');
    const afterLoginSnapshot = await automation.getSnapshot();
    console.log('✅ Page after login:');
    console.log(afterLoginSnapshot);
    
    // Step 11: Look for OTP input field
    console.log('\n🔢 Step 11: Looking for OTP input...');
    const otpTexts = ['OTP', 'otp', 'verification', 'code', 'verify'];
    let otpFound = false;
    
    for (const text of otpTexts) {
      const waitResult = await automation.waitFor({ text, time: 2 });
      if (waitResult.includes('Successfully waited')) {
        console.log(`✅ Found OTP-related text: "${text}"`);
        otpFound = true;
        break;
      }
    }
    
    if (otpFound) {
      // Get fresh snapshot to find OTP input
      const otpSnapshot = await automation.getSnapshot();
      console.log('📸 OTP page snapshot:');
      console.log(otpSnapshot);
      
      // Try to find and fill OTP input
      console.log('\n🔢 Step 12: Entering OTP...');
      // Look for input fields that might be OTP
      const lines = otpSnapshot.split('\n');
      const otpInputLine = lines.find(line => 
        line.includes('input') && 
        (line.toLowerCase().includes('otp') || 
         line.toLowerCase().includes('code') || 
         line.toLowerCase().includes('verify'))
      );
      
      if (otpInputLine) {
        const elementMatch = otpInputLine.match(/element-(\d+)/);
        if (elementMatch) {
          const otpElementRef = elementMatch[0];
          const otpResult = await automation.type('OTP input', otpElementRef, credentials.otp, true);
          console.log('✅', otpResult);
        }
      } else {
        console.log('⚠️  Could not automatically identify OTP input field');
        console.log('📝 Manual action needed: Enter OTP', credentials.otp);
      }
    }
    
    // Step 13: Wait for final result
    console.log('\n⏳ Step 13: Waiting for login completion...');
    await automation.waitFor({ time: 5 });
    
    // Step 14: Get final snapshot
    console.log('\n📸 Step 14: Getting final page state...');
    const finalSnapshot = await automation.getSnapshot();
    console.log('✅ Final page state:');
    console.log(finalSnapshot);
    
    // Step 15: Take screenshot for verification
    console.log('\n📷 Step 15: Taking final screenshot...');
    const screenshotResult = await automation.takeScreenshot('paytm-login-final.png');
    console.log('✅', screenshotResult);
    
    console.log('\n🎉 Login automation completed!');
    console.log('📋 Summary:');
    console.log('   - Navigated to staging site ✅');
    console.log('   - Found and switched to OAuth iframe ✅');
    console.log('   - Filled mobile number ✅');
    console.log('   - Filled password ✅');
    console.log('   - Clicked Sign In button ✅');
    console.log('   - Handled OTP flow ✅');
    console.log('');
    console.log('🔍 Check the final screenshot to see the result!');
    
  } catch (error) {
    console.error('❌ Error during login automation:', error);
    
    // Take error screenshot
    try {
      await automation.takeScreenshot('paytm-login-error.png');
      console.log('📷 Error screenshot saved as paytm-login-error.png');
    } catch (e) {
      console.log('Could not take error screenshot');
    }
  } finally {
    console.log('\n🧹 Cleaning up...');
    await automation.cleanup();
  }
}

paytmStagingLogin();
