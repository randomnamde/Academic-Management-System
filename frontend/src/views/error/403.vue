<template>
  <div class="error-page">
    <el-card class="error-card">
      <p class="error-kicker">权限提示</p>
      <h1>403</h1>
      <p class="error-title">当前账号没有访问该页面的权限</p>
      <p class="error-desc">如需继续访问，请联系管理员为你的角色分配对应权限后重试。</p>
      <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped lang="scss">
.error-page {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.error-card {
  text-align: center;
  width: min(500px, 100%);
  border-radius: var(--radius-pop);
  border: 1px solid color-mix(in srgb, var(--panel-border) 84%, transparent);
  background:
    radial-gradient(circle at 50% -10%, color-mix(in srgb, var(--accent-500) 14%, transparent), transparent 54%),
    var(--panel-glass);
  box-shadow: var(--shadow-panel);
}

.error-kicker {
  margin: 0 0 8px;
  letter-spacing: 0.14em;
  font-size: 12px;
  color: var(--text-secondary);
}

h1 {
  margin: 0 0 10px;
  font-size: 62px;
  color: var(--accent-700);
}

.error-title {
  margin: 0 0 10px;
  font-size: 18px;
  color: var(--text-primary);
}

.error-desc {
  margin: 0 0 20px;
  color: var(--text-secondary);
}
</style>
