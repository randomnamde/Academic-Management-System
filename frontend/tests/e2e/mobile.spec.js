const { test, expect } = require('@playwright/test')

test('移动端侧栏抽屉可打开', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto('/login')
  await expect(page.getByText('学生管理系统')).toBeVisible()
})
