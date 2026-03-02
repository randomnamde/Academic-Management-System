const { test, expect } = require('@playwright/test')

test('多个受保护路由会重定向到登录页', async ({ page }) => {
  await page.goto('/student')
  await expect(page).toHaveURL(/\/login/)

  await page.goto('/analytics')
  await expect(page).toHaveURL(/\/login/)
})
