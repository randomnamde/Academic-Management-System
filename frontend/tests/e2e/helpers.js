const { expect } = require('@playwright/test')

async function loginAs(page, username, password) {
  await page.goto('/login')
  await page.getByLabel('用户名').fill(username)
  await page.getByLabel('密码').fill(password)
  await page.getByRole('button', { name: '登录' }).click()
  await expect(page).toHaveURL(/\/dashboard/)
}

async function seedAuth(page, userInfo) {
  await page.goto('/login')
  await page.evaluate((payload) => {
    document.cookie = 'token=e2e-token; path=/'
    localStorage.setItem('userInfo', JSON.stringify(payload))
    localStorage.setItem('sidebar', 'true')
  }, userInfo)
}

module.exports = { loginAs, seedAuth }
