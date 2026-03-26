<template>
  <div class="user-manage">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-input v-model="keyword" placeholder="搜索用户名/邮箱..." clearable
        prefix-icon="Search" style="width:280px" @change="loadUsers" />
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="users" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="70">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar">{{ row.username[0] }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="角色" width="90">
          <template #default="{ row }">
            <el-tag :type="roleType(row.role)" size="small">{{ roleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total"
          layout="total, prev, pager, next" background @current-change="loadUsers" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/user'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const users = ref([])
const loading = ref(false)
const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const roleMap = { admin: { label: '管理员', type: 'danger' }, teacher: { label: '讲师', type: 'warning' }, student: { label: '学员', type: 'success' } }
const roleLabel = r => roleMap[r]?.label || r
const roleType = r => roleMap[r]?.type || 'info'
const formatTime = t => dayjs(t).format('YYYY-MM-DD HH:mm')

async function loadUsers() {
  loading.value = true
  try {
    const res = await userApi.getUserList({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value })
    users.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await userApi.updateUserStatus(row.id, newStatus)
  row.status = newStatus
  ElMessage.success(newStatus === 1 ? '用户已启用' : '用户已禁用')
}

onMounted(loadUsers)
</script>

<style scoped>
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.page-header h2 { font-size: 22px; }
.table-card { border-radius: 12px; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
