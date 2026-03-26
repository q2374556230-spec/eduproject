<template>
  <div class="admin-dashboard">
    <h2 class="page-title">控制台概览</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <el-card class="stat-card" shadow="hover" :style="{ borderTop: `4px solid ${card.color}` }">
          <div class="stat-content">
            <div>
              <div class="stat-num">{{ card.value }}</div>
              <div class="stat-title">{{ card.title }}</div>
            </div>
            <el-icon :size="40" :color="card.color"><component :is="card.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" style="margin-top:24px">
      <el-col :span="12">
        <el-card shadow="never" class="quick-card">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-btns">
            <el-button type="primary" size="large" @click="router.push('/admin/courses')">
              <el-icon><Plus /></el-icon>添加课程
            </el-button>
            <el-button type="success" size="large" @click="router.push('/admin/users')">
              <el-icon><User /></el-icon>管理用户
            </el-button>
            <el-button type="warning" size="large" @click="router.push('/admin/stats')">
              <el-icon><TrendCharts /></el-icon>数据统计
            </el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="quick-card">
          <template #header><span>系统状态</span></template>
          <div class="sys-status">
            <div class="status-item" v-for="s in sysStatus" :key="s.name">
              <span class="s-name">{{ s.name }}</span>
              <el-tag :type="s.ok ? 'success' : 'danger'" size="small">
                {{ s.ok ? '正常' : '异常' }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderApi } from '@/api/order'

const router = useRouter()

const stats = ref({ totalOrders: 0, totalRevenue: 0, totalUsers: 0, totalCourses: 8 })

const statCards = ref([
  { title: '课程总数', value: '8', icon: 'Collection', color: '#409eff' },
  { title: '注册用户', value: '—', icon: 'User', color: '#67c23a' },
  { title: '订单总数', value: '—', icon: 'ShoppingCart', color: '#e6a23c' },
  { title: '总收入(元)', value: '—', icon: 'Money', color: '#f56c6c' }
])

const sysStatus = ref([
  { name: 'Gateway 服务', ok: true },
  { name: 'User 服务', ok: true },
  { name: 'Course 服务', ok: true },
  { name: 'Order 服务', ok: true },
  { name: 'Notification 服务', ok: true },
  { name: 'MySQL', ok: true },
  { name: 'Redis', ok: true },
  { name: 'RabbitMQ', ok: true }
])

async function loadStats() {
  try {
    const res = await orderApi.getStats()
    const d = res.data || {}
    statCards.value[1].value = d.totalUsers || '—'
    statCards.value[2].value = d.totalOrders || '—'
    statCards.value[3].value = d.totalRevenue ? `¥${d.totalRevenue}` : '—'
  } catch {}
}

onMounted(loadStats)
</script>

<style scoped>
.page-title { font-size: 22px; margin-bottom: 20px; }
.stat-row { margin-bottom: 8px; }
.stat-card { border-radius: 12px; }
.stat-content { display: flex; align-items: center; justify-content: space-between; }
.stat-num { font-size: 32px; font-weight: 700; color: #303133; }
.stat-title { color: #909399; margin-top: 4px; font-size: 14px; }
.quick-card { border-radius: 12px; height: 100%; }
.card-header { font-size: 16px; font-weight: 600; }
.quick-btns { display: flex; gap: 12px; flex-wrap: wrap; }
.sys-status { display: flex; flex-direction: column; gap: 8px; }
.status-item { display: flex; align-items: center; justify-content: space-between; padding: 4px 0; }
.s-name { color: #606266; font-size: 14px; }
</style>
