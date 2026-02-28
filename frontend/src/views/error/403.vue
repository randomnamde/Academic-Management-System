<template>
  <div class="forbidden-page">
    <el-card class="forbidden-card">
      <h1>403</h1>
      <p>无权限访问该页面</p>
      <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.forbidden-page {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.forbidden-card {
  text-align: center;
  width: 360px;
}

h1 {
  margin: 0 0 8px;
  font-size: 56px;
  color: #6f5de7;
}

p {
  margin: 0 0 18px;
  color: #4a3a7a;
}
</style>
