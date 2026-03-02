const { test, expect } = require('@playwright/test')

test('登录页可访问', async ({ page }) => {
  await page.goto('/login')
  await expect(page.locator('text=学生管理系统')).toBeVisible()
})
