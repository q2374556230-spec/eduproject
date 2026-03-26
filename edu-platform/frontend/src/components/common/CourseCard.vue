<template>
  <div class="course-card" @click="router.push(`/courses/${course.id}`)">
    <div class="cover-wrap">
      <el-image :src="course.coverImage" fit="cover" class="cover-img" lazy>
        <template #error>
          <div class="cover-placeholder">
            <el-icon size="32" color="#c0c4cc"><Picture /></el-icon>
          </div>
        </template>
      </el-image>
      <el-tag class="level-tag" :type="levelType(course.level)" size="small">
        {{ levelLabel(course.level) }}
      </el-tag>
    </div>
    <div class="card-body">
      <div class="course-title" :title="course.title">{{ course.title }}</div>
      <div class="course-teacher">
        <el-icon><Avatar /></el-icon>{{ course.teacherName }}
      </div>
      <div class="course-meta">
        <el-rate :model-value="course.rating" disabled text-color="#f7ba2a"
          :score-template="course.rating + ''" size="small" />
        <span class="student-count">{{ formatCount(course.studentCount) }}人学习</span>
      </div>
      <div class="course-footer">
        <span class="price">
          <template v-if="course.price > 0">¥{{ course.price }}</template>
          <template v-else><span style="color:#67c23a">免费</span></template>
        </span>
        <el-button type="primary" size="small" round @click.stop="handleEnroll">
          立即学习
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { orderApi } from '@/api/order'
import { ElMessage } from 'element-plus'

const props = defineProps({ course: { type: Object, required: true } })
const router = useRouter()
const userStore = useUserStore()

const levelMap = {
  beginner: { label: '入门', type: 'success' },
  intermediate: { label: '中级', type: 'warning' },
  advanced: { label: '高级', type: 'danger' }
}
const levelLabel = l => levelMap[l]?.label || l
const levelType = l => levelMap[l]?.type || ''
const formatCount = n => n >= 1000 ? (n / 1000).toFixed(1) + 'k' : n

async function handleEnroll() {
  if (!userStore.isLoggedIn) {
    router.push('/auth/login')
    return
  }
  try {
    const res = await orderApi.createOrder({ courseId: props.course.id })
    const orderNo = res.data.orderNo
    if (props.course.price === 0) {
      await orderApi.payOrder(orderNo)
      ElMessage.success('已成功加入学习')
    } else {
      router.push(`/orders`)
    }
  } catch {}
}
</script>

<style scoped>
.course-card {
  width: 280px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all .2s;
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
  flex-shrink: 0;
}
.course-card:hover { transform: translateY(-4px); box-shadow: 0 12px 32px rgba(0,0,0,.12); }
.cover-wrap { position: relative; height: 157px; }
.cover-img { width: 100%; height: 100%; }
.cover-placeholder {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  background: #f5f7fa;
}
.level-tag { position: absolute; top: 8px; right: 8px; }
.card-body { padding: 12px; }
.course-title {
  font-weight: 600; font-size: 14px; line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.course-teacher { color: #909399; font-size: 12px; display: flex; align-items: center; gap: 4px; margin-bottom: 8px; }
.course-meta { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.student-count { color: #909399; font-size: 12px; }
.course-footer { display: flex; align-items: center; justify-content: space-between; }
.price { font-size: 18px; font-weight: 700; color: #f56c6c; }
</style>
