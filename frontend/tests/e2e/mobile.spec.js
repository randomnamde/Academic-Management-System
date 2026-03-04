const { test, expect } = require('@playwright/test')

test('移动端侧栏抽屉可打开', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto('/login')
  await expect(page.getByRole('heading', { name: /教务管理平台/i })).toBeVisible()
  await expect(page.getByRole('button', { name: /^登录系统$/ })).toBeVisible()

  await page.getByRole('button', { name: /^登录系统$/ }).click()
  await expect(page.getByRole('dialog', { name: /^登录系统$/ })).toBeVisible()
})
