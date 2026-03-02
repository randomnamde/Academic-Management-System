const { defineConfig, devices } = require('@playwright/test')

const localChromePath = process.env.PLAYWRIGHT_CHROME_PATH || 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe'

module.exports = defineConfig({
  testDir: './tests/e2e',
  timeout: 60 * 1000,
  expect: { timeout: 5000 },
  fullyParallel: false,
  retries: 1,
  reporter: [['list']],
  use: {
    baseURL: 'http://127.0.0.1:3000',
    trace: 'on-first-retry'
  },
  webServer: {
    command: 'npm run serve -- --port 3000',
    port: 3000,
    reuseExistingServer: true,
    timeout: 120 * 1000
  },
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        launchOptions: {
          executablePath: localChromePath
        }
      }
    }
  ]
})
