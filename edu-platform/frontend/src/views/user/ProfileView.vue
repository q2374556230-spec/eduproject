<template>
  <div class="profile-view">
    <el-row :gutter="24">
      <!-- 左侧头像 -->
      <el-col :span="6">
        <el-card class="avatar-card" shadow="never">
          <div class="avatar-wrap">
            <el-avatar :size="96" :src="userStore.avatar">{{ userStore.username[0] }}</el-avatar>
            <div class="username">{{ userStore.username }}</div>
            <el-tag :type="roleType(userStore.userInfo?.role)">{{ roleLabel(userStore.userInfo?.role) }}</el-tag>
          </div>
          <el-menu :default-active="activeSection" @select="activeSection = $event" class="profile-menu">
            <el-menu-item index="info">个人信息</el-menu-item>
            <el-menu-item index="password">修改密码</el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 右侧内容 -->
      <el-col :span="18">
        <!-- 个人信息 -->
        <el-card v-if="activeSection === 'info'" shadow="never" class="content-card">
          <template #header><span>个人信息</span></template>
          <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-width="80px">
            <el-form-item label="用户名">
              <el-input :model-value="userStore.username" disabled />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input :model-value="userStore.userInfo?.email" disabled />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="infoForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="infoForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="个人简介" prop="bio">
              <el-input v-model="infoForm.bio" type="textarea" :rows="3" placeholder="介绍一下自己..." />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="infoLoading" @click="saveInfo">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 修改密码 -->
        <el-card v-if="activeSection === 'password'" shadow="never" class="content-card">
          <template #header><span>修改密码</span></template>
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="pwdLoading" @click="savePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const activeSection = ref('info')
const infoFormRef = ref()
const pwdFormRef = ref()
const infoLoading = ref(false)
const pwdLoading = ref(false)

const infoForm = reactive({ realName: '', phone: '', bio: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const roleMap = { admin: { label: '管理员', type: 'danger' }, teacher: { label: '讲师', type: 'warning' }, student: { label: '学员', type: 'success' } }
const roleLabel = r => roleMap[r]?.label || '学员'
const roleType = r => roleMap[r]?.type || 'info'

const infoRules = {
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) callback(new Error('两次密码不一致'))
  else callback()
}
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 6, message: '新密码至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirm, trigger: 'blur' }]
}

async function saveInfo() {
  const valid = await infoFormRef.value.validate().catch(() => false)
  if (!valid) return
  infoLoading.value = true
  try {
    await userApi.updateProfile(infoForm)
    userStore.updateUserInfo(infoForm)
    ElMessage.success('保存成功')
  } finally {
    infoLoading.value = false
  }
}

async function savePassword() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  pwdLoading.value = true
  try {
    await userApi.changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
  } finally {
    pwdLoading.value = false
  }
}

onMounted(async () => {
  const res = await userApi.getProfile()
  const info = res.data
  Object.assign(infoForm, { realName: info.realName || '', phone: info.phone || '', bio: info.bio || '' })
})
</script>

<style scoped>
.profile-view { padding-bottom: 40px; }
.avatar-card { border-radius: 12px; }
.avatar-wrap { text-align: center; padding: 24px 0; }
.username { font-size: 18px; font-weight: 600; margin: 12px 0 8px; }
.profile-menu { border-right: none; margin-top: 8px; }
.content-card { border-radius: 12px; }
</style>
