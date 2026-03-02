const { test, expect } = require('@playwright/test')

test('未登录访问受保护路由会跳转到登录页', async ({ page }) => {
  await page.goto('/student')
  await expect(page).toHaveURL(/\/login/)
})
