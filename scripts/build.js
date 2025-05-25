#!/usr/bin/env node

import fs from 'fs';
import path from 'path';
import { execSync } from 'child_process';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

console.log('Building Selenium MCP Server...');

// Step 1: Build Java JAR if it doesn't exist
const jarPath = path.join(__dirname, '..', 'target', 'selenium-mcp-0.0.1.jar');
if (!fs.existsSync(jarPath)) {
  console.log('Building Java JAR...');
  try {
    execSync('mvn clean package', { stdio: 'inherit', cwd: path.join(__dirname, '..') });
  } catch (error) {
    console.error('Failed to build Java JAR:', error.message);
    process.exit(1);
  }
}

// Step 2: Create lib directory and copy JAR
const libDir = path.join(__dirname, '..', 'lib');
if (!fs.existsSync(libDir)) {
  fs.mkdirSync(libDir, { recursive: true });
}

const targetJarPath = path.join(libDir, 'selenium-mcp-0.0.1.jar');
fs.copyFileSync(jarPath, targetJarPath);
console.log('Copied JAR to lib directory');

// Step 3: Build TypeScript
console.log('Building TypeScript...');
try {
  execSync('npx tsc', { stdio: 'inherit', cwd: path.join(__dirname, '..') });
} catch (error) {
  console.error('Failed to build TypeScript:', error.message);
  process.exit(1);
}

console.log('Build complete!');
