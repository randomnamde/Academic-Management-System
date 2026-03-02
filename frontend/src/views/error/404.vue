<template>
  <div class="notfound-page">
    <el-card class="notfound-card">
      <p class="error-kicker">Navigation</p>
      <h1>404</h1>
      <p class="error-title">页面不存在或已被移动</p>
      <p class="error-desc">请检查地址是否正确，或返回首页继续使用系统。</p>
      <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped lang="scss">
.notfound-page {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.notfound-card {
  text-align: center;
  width: min(460px, 100%);
  border-radius: 20px;
  border: 1px solid rgba(21, 88, 102, 0.2);
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.94), rgba(240, 249, 248, 0.88));
}

.error-kicker {
  margin: 0 0 8px;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  font-size: 12px;
  color: #4f8190;
}

h1 {
  margin: 0 0 10px;
  font-size: 62px;
  color: #156f6b;
}

.error-title {
  margin: 0 0 10px;
  font-size: 18px;
  color: #1f4a5b;
}

.error-desc {
  margin: 0 0 20px;
  color: #5c7481;
}
</style>
