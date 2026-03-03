<template>
  <div class="forbidden-page">
    <el-card class="forbidden-card">
      <p class="error-kicker">权限提示</p>
      <h1>403</h1>
      <p class="error-title">当前账号没有访问该页面的权限</p>
      <p class="error-desc">如果你认为这是异常，请联系管理员开通权限后再试。</p>
      <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped lang="scss">
.forbidden-page {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.forbidden-card {
  text-align: center;
  width: min(460px, 100%);
  border-radius: 20px;
  border: 1px solid rgba(21, 88, 102, 0.2);
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.94), rgba(240, 249, 248, 0.88));
}

.error-kicker {
  margin: 0 0 8px;
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
