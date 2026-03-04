const { test, expect } = require('@playwright/test')

test('登录页可访问', async ({ page }) => {
  await page.goto('/login')
  await expect(page.getByRole('heading', { name: /教务管理平台/i })).toBeVisible()
  await expect(page.getByRole('button', { name: /^登录系统$/ })).toBeVisible()
})

test.describe('reduced motion', () => {
  test.use({ reducedMotion: 'reduce' })

  test('减少动态模式下登录弹窗仍可正常开关', async ({ page }) => {
    await page.goto('/login')
    await page.getByRole('button', { name: /^登录系统$/ }).click()
    await expect(page.getByRole('dialog', { name: /^登录系统$/ })).toBeVisible()
    await page.keyboard.press('Escape')
    await expect(page.getByRole('dialog', { name: /^登录系统$/ })).toBeHidden()
  })
})
