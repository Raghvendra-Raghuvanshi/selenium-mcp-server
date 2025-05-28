#!/usr/bin/env node

import puppeteer from 'puppeteer';

async function testGoogleSearch() {
  console.log('🚀 Starting browser automation test...');
  
  const browser = await puppeteer.launch({
    headless: false,
    defaultViewport: null,
    args: [
      '--no-sandbox',
      '--disable-setuid-sandbox',
      '--disable-dev-shm-usage',
      '--disable-accelerated-2d-canvas',
      '--no-first-run',
      '--no-zygote',
      '--disable-gpu'
    ]
  });

  const page = await browser.newPage();
  
  try {
    console.log('📍 Navigating to Google...');
    await page.goto('https://www.google.com', { waitUntil: 'networkidle2' });
    
    console.log('📸 Taking snapshot of page elements...');
    const elements = await page.evaluate(() => {
      const doc = document;
      const interactiveElements = doc.querySelectorAll(
        'a, button, input, select, textarea, [onclick], [role="button"], [tabindex]'
      );
      
      return Array.from(interactiveElements).slice(0, 10).map((el, index) => {
        const tagName = el.tagName.toLowerCase();
        const text = el.textContent?.trim().slice(0, 50) || '';
        const type = el.getAttribute('type') || '';
        const placeholder = el.getAttribute('placeholder') || '';
        const name = el.getAttribute('name') || '';
        
        return {
          ref: `element-${index}`,
          tag: tagName,
          text: text,
          type: type,
          placeholder: placeholder,
          name: name
        };
      });
    });
    
    console.log('🔍 Found elements:');
    elements.forEach(el => {
      console.log(`  - ${el.ref}: ${el.tag}${el.type ? `[${el.type}]` : ''} "${el.text || el.placeholder || el.name}"`);
    });
    
    // Find the search input
    const searchInput = elements.find(el => el.name === 'q' || el.placeholder?.toLowerCase().includes('search'));
    
    if (searchInput) {
      console.log(`✅ Found search input: ${searchInput.ref}`);
      
      console.log('⌨️  Typing "Tom Cruise"...');
      
      // Focus and clear the search input
      await page.evaluate((index) => {
        const doc = document;
        const interactiveElements = doc.querySelectorAll(
          'a, button, input, select, textarea, [onclick], [role="button"], [tabindex]'
        );
        
        const targetElement = interactiveElements[index];
        if (targetElement) {
          targetElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
          targetElement.focus();
          if (targetElement.tagName === 'INPUT' || targetElement.tagName === 'TEXTAREA') {
            targetElement.value = '';
          }
        }
      }, parseInt(searchInput.ref.replace('element-', '')));
      
      await new Promise(resolve => setTimeout(resolve, 500));
      
      // Type the search text
      await page.keyboard.type('Tom Cruise');
      
      console.log('🔍 Submitting search...');
      await page.keyboard.press('Enter');
      
      // Wait for search results
      await page.waitForNavigation({ waitUntil: 'networkidle2' });
      
      const title = await page.title();
      console.log(`✅ Search completed! Page title: ${title}`);
      
    } else {
      console.log('❌ Could not find search input');
    }
    
  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    console.log('🔄 Closing browser...');
    await browser.close();
  }
}

testGoogleSearch().catch(console.error);
