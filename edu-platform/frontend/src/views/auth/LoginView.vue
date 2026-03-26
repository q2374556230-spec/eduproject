<template>
  <div class="login-form">
    <h2>账号登录</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
      <el-form-item label="账号" prop="account">
        <el-input v-model="form.account" placeholder="用户名/邮箱" size="large" prefix-icon="User" clearable />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large"
          prefix-icon="Lock" show-password clearable />
      </el-form-item>
      <el-button type="primary" size="large" style="width:100%;margin-top:8px"
        :loading="loading" native-type="submit">
        登录
      </el-button>
    </el-form>
    <div class="form-footer">
      <span>还没有账号？</span>
      <router-link to="/auth/register">立即注册</router-link>
    </div>
    <el-divider>测试账号</el-divider>
    <div class="test-accounts">
      <el-tag @click="fillAccount('admin','admin123456')" style="cursor:pointer">管理员: admin</el-tag>
      <el-tag type="success" @click="fillAccount('student01','student123')" style="cursor:pointer">学生: student01</el-tag>
      <el-tag type="warning" @click="fillAccount('teacher01','teacher123')" style="cursor:pointer">教师: teacher01</el-tag>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ account: '', password: '' })

const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function fillAccount(account, password) {
  form.account = account
  form.password = password
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form)
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
h2 { text-align: center; margin-bottom: 24px; color: #303133; }
.form-footer { text-align: center; margin-top: 16px; color: #606266; font-size: 14px; }
.form-footer a { color: #409eff; text-decoration: none; }
.test-accounts { display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; }
</style>
