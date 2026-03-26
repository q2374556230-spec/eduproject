<template>
  <div class="notify-panel">
    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="4" animated />
    </div>
    <template v-else>
      <div class="notify-toolbar">
        <el-button size="small" @click="markAll" :disabled="!unreadItems.length">全部已读</el-button>
      </div>
      <el-empty v-if="!notifications.length" description="暂无通知" />
      <el-timeline v-else>
        <el-timeline-item
          v-for="item in notifications"
          :key="item.id"
          :type="item.isRead ? '' : 'primary'"
          :timestamp="formatTime(item.createdAt)"
          placement="top"
        >
          <el-card class="notify-card" :class="{ unread: !item.isRead }" shadow="hover">
            <div class="notify-title">{{ item.title }}</div>
            <div class="notify-content">{{ item.content }}</div>
            <el-button v-if="!item.isRead" link size="small" @click="markRead(item)">
              标记已读
            </el-button>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { notifyApi } from '@/api/notify'
import dayjs from 'dayjs'

const notifications = ref([])
const loading = ref(false)

const unreadItems = computed(() => notifications.value.filter(n => !n.isRead))

async function loadNotifications() {
  loading.value = true
  try {
    const res = await notifyApi.getMyNotifications({ pageNum: 1, pageSize: 20 })
    notifications.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

async function markRead(item) {
  await notifyApi.markAsRead(item.id)
  item.isRead = 1
}

async function markAll() {
  await notifyApi.markAllAsRead()
  notifications.value.forEach(n => n.isRead = 1)
}

function formatTime(t) {
  return dayjs(t).format('MM-DD HH:mm')
}

onMounted(loadNotifications)
</script>

<style scoped>
.notify-panel { padding: 8px 0; }
.notify-toolbar { margin-bottom: 16px; text-align: right; }
.notify-card { margin-bottom: 4px; }
.notify-card.unread { border-left: 3px solid #409eff; }
.notify-title { font-weight: 600; margin-bottom: 4px; }
.notify-content { color: #606266; font-size: 13px; }
.loading-wrap { padding: 16px; }
</style>
