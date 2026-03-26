<template>
  <div class="auth-layout">
    <div class="auth-card">
      <div class="auth-header">
        <router-link to="/" class="logo">
          <el-icon size="32" color="#409eff"><Monitor /></el-icon>
          <span>云课堂</span>
        </router-link>
        <p class="slogan">开启你的学习之旅</p>
      </div>
      <router-view />
    </div>
    <div class="auth-bg">
      <div class="bg-circle c1"></div>
      <div class="bg-circle c2"></div>
      <div class="bg-circle c3"></div>
    </div>
  </div>
</template>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}
.auth-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  width: 420px;
  box-shadow: 0 20px 60px rgba(0,0,0,.2);
  position: relative;
  z-index: 1;
}
.auth-header {
  text-align: center;
  margin-bottom: 32px;
}
.logo {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  font-size: 24px;
  font-weight: 700;
  color: #409eff;
}
.slogan {
  color: #909399;
  margin-top: 8px;
  font-size: 14px;
}
.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: .15;
  background: #fff;
}
.c1 { width: 300px; height: 300px; top: -100px; right: -100px; }
.c2 { width: 200px; height: 200px; bottom: -50px; left: -50px; }
.c3 { width: 150px; height: 150px; top: 50%; left: 10%; }
</style>
