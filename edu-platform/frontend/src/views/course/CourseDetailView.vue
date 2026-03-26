<template>
  <div v-if="loading" class="detail-loading">
    <el-skeleton :rows="10" animated />
  </div>
  <div v-else-if="course" class="course-detail-view">
    <!-- 课程头部 -->
    <div class="detail-header">
      <div class="header-content">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/courses' }">课程广场</el-breadcrumb-item>
          <el-breadcrumb-item>{{ course.title }}</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="course-title">{{ course.title }}</h1>
        <p class="course-desc">{{ course.description }}</p>
        <div class="course-meta">
          <el-rate :model-value="course.rating" disabled size="small" />
          <span class="rating-num">{{ course.rating }}</span>
          <span class="divider">·</span>
          <el-icon><User /></el-icon>
          <span>{{ formatCount(course.studentCount) }} 名学员</span>
          <span class="divider">·</span>
          <el-icon><Clock /></el-icon>
          <span>{{ course.duration }} 分钟</span>
          <span class="divider">·</span>
          <el-tag :type="levelType(course.level)" size="small">{{ levelLabel(course.level) }}</el-tag>
        </div>
        <div class="teacher-info">
          <el-avatar :size="36" src="">{{ course.teacherName?.[0] }}</el-avatar>
          <span>讲师：{{ course.teacherName }}</span>
        </div>
        <div class="tags">
          <el-tag v-for="tag in courseTags" :key="tag" size="small" style="margin-right:4px">{{ tag }}</el-tag>
        </div>
      </div>
      <div class="enroll-card">
        <el-image :src="course.coverImage" fit="cover" style="width:100%;height:200px;border-radius:8px" />
        <div class="price-area">
          <span class="price">
            <template v-if="course.price > 0">¥{{ course.price }}</template>
            <template v-else><span style="color:#67c23a;font-size:22px">免费</span></template>
          </span>
        </div>
        <el-button type="primary" size="large" style="width:100%" :loading="enrollLoading" @click="handleEnroll">
          {{ enrolled ? '已加入学习' : '立即报名' }}
        </el-button>
        <div class="course-includes">
          <div><el-icon><VideoPlay /></el-icon> {{ course.duration }} 分钟视频</div>
          <div><el-icon><Trophy /></el-icon> 完课证书</div>
          <div><el-icon><Refresh /></el-icon> 永久学习权限</div>
        </div>
      </div>
    </div>

    <!-- 课程介绍 -->
    <div class="detail-body">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="课程介绍" name="intro">
          <div class="intro-content">
            <h3>课程简介</h3>
            <p>{{ course.description }}</p>
            <h3 style="margin-top:24px">适合人群</h3>
            <ul>
              <li>对 {{ course.title }} 感兴趣的初学者</li>
              <li>希望系统学习相关技能的开发者</li>
              <li>想要提升职场竞争力的职场人士</li>
            </ul>
          </div>
        </el-tab-pane>
        <el-tab-pane label="课程目录" name="outline">
          <el-timeline>
            <el-timeline-item v-for="(item, i) in outlineItems" :key="i" :timestamp="`第${i+1}章`">
              {{ item }}
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
  <el-empty v-else description="课程不存在" />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { courseApi } from '@/api/course'
import { orderApi } from '@/api/order'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const course = ref(null)
const loading = ref(true)
const enrollLoading = ref(false)
const enrolled = ref(false)
const activeTab = ref('intro')

const levelMap = { beginner: { label: '入门', type: 'success' }, intermediate: { label: '中级', type: 'warning' }, advanced: { label: '高级', type: 'danger' } }
const levelLabel = l => levelMap[l]?.label || l
const levelType = l => levelMap[l]?.type || ''
const formatCount = n => n >= 1000 ? (n / 1000).toFixed(1) + 'k' : n

const courseTags = computed(() => course.value?.tags?.split(',').filter(Boolean) || [])
const outlineItems = computed(() => {
  const title = course.value?.title || ''
  return [`${title} 基础入门`, '核心概念讲解', '实战项目一', '进阶技巧与最佳实践', '实战项目二', '综合总结与答疑']
})

async function loadCourse() {
  loading.value = true
  try {
    const res = await courseApi.getCourseById(route.params.id)
    course.value = res.data
  } finally {
    loading.value = false
  }
}

async function handleEnroll() {
  if (!userStore.isLoggedIn) { router.push('/auth/login'); return }
  if (enrolled.value) { router.push('/orders'); return }
  enrollLoading.value = true
  try {
    const res = await orderApi.createOrder({ courseId: course.value.id })
    if (course.value.price === 0) {
      await orderApi.payOrder(res.data.orderNo)
      enrolled.value = true
      ElMessage.success('报名成功！')
    } else {
      ElMessage.success('订单已创建，请前往订单页面完成支付')
      router.push('/orders')
    }
  } finally {
    enrollLoading.value = false
  }
}

onMounted(loadCourse)
</script>

<style scoped>
.detail-loading { padding: 24px; }
.course-detail-view {}
.detail-header {
  display: flex; gap: 40px;
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  padding: 32px;
  border-radius: 12px;
  color: #fff;
  margin-bottom: 32px;
}
.header-content { flex: 1; }
.course-title { font-size: 28px; font-weight: 700; margin: 16px 0 12px; line-height: 1.3; }
.course-desc { color: rgba(255,255,255,.75); font-size: 15px; margin-bottom: 16px; }
.course-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-bottom: 12px; font-size: 14px; }
.rating-num { color: #f7ba2a; font-weight: 600; }
.divider { color: rgba(255,255,255,.3); }
.teacher-info { display: flex; align-items: center; gap: 8px; font-size: 14px; margin-bottom: 12px; }
.tags { margin-top: 8px; }
.enroll-card {
  width: 320px; flex-shrink: 0;
  background: #fff; border-radius: 12px; padding: 16px;
  color: #303133; height: fit-content;
}
.price-area { padding: 16px 0; }
.price { font-size: 28px; font-weight: 700; color: #f56c6c; }
.course-includes { margin-top: 16px; }
.course-includes > div { display: flex; align-items: center; gap: 8px; padding: 6px 0; font-size: 13px; color: #606266; }
.detail-body { background: #fff; border-radius: 12px; padding: 24px; }
.intro-content h3 { font-size: 16px; margin-bottom: 12px; }
.intro-content p { color: #606266; line-height: 1.8; }
.intro-content ul { color: #606266; padding-left: 20px; line-height: 2; }
</style>
