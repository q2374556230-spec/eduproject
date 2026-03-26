<template>
  <div class="home-view">
    <!-- Banner 区域 -->
    <div class="banner">
      <div class="banner-content">
        <h1>探索无限学习可能</h1>
        <p>汇聚顶尖讲师，专业课程体系，助你成就职业梦想</p>
        <div class="banner-search">
          <el-input v-model="searchKeyword" placeholder="搜索感兴趣的课程..." size="large"
            clearable @keyup.enter="goSearch">
            <template #append>
              <el-button type="primary" @click="goSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
        <div class="banner-stats">
          <div class="stat-item"><span class="num">8+</span><span>精品课程</span></div>
          <div class="stat-item"><span class="num">5000+</span><span>在读学员</span></div>
          <div class="stat-item"><span class="num">6</span><span>课程分类</span></div>
          <div class="stat-item"><span class="num">4.8</span><span>平均评分</span></div>
        </div>
      </div>
    </div>

    <!-- 分类导航 -->
    <div class="section">
      <div class="section-title">
        <h2>课程分类</h2>
        <router-link to="/courses">查看全部 →</router-link>
      </div>
      <div class="category-grid">
        <div v-for="cat in courseStore.categories" :key="cat.id"
          class="category-card" @click="goCategory(cat.id)">
          <span class="cat-icon">{{ cat.icon }}</span>
          <span class="cat-name">{{ cat.name }}</span>
        </div>
      </div>
    </div>

    <!-- AI 推荐课程 -->
    <div class="section">
      <div class="section-title">
        <h2>
          <el-icon color="#409eff"><MagicStick /></el-icon>
          AI 智能推荐
        </h2>
        <el-button link @click="refreshRecommend" :loading="aiLoading">换一批</el-button>
      </div>
      <div v-if="aiLoading" class="skeleton-row">
        <el-skeleton v-for="i in 3" :key="i" :rows="4" animated style="width:360px" />
      </div>
      <div v-else class="course-row">
        <CourseCard v-for="c in courseStore.aiRecommendations" :key="c.id" :course="c" />
      </div>
    </div>

    <!-- 热门课程 -->
    <div class="section">
      <div class="section-title">
        <h2>热门课程</h2>
        <router-link to="/courses">查看全部 →</router-link>
      </div>
      <div v-if="courseStore.loading" class="skeleton-row">
        <el-skeleton v-for="i in 4" :key="i" :rows="4" animated style="width:280px" />
      </div>
      <div v-else class="course-row">
        <CourseCard v-for="c in courseStore.courseList" :key="c.id" :course="c" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCourseStore } from '@/stores/course'
import CourseCard from '@/components/common/CourseCard.vue'

const router = useRouter()
const courseStore = useCourseStore()
const searchKeyword = ref('')
const aiLoading = ref(false)

function goSearch() {
  router.push({ path: '/courses', query: { keyword: searchKeyword.value } })
}
function goCategory(id) {
  router.push({ path: '/courses', query: { categoryId: id } })
}
async function refreshRecommend() {
  aiLoading.value = true
  try { await courseStore.fetchAiRecommendations() }
  finally { aiLoading.value = false }
}

onMounted(async () => {
  await Promise.all([
    courseStore.fetchCategories(),
    courseStore.fetchCourseList({ pageNum: 1, pageSize: 8, sortBy: 'studentCount' }),
    courseStore.fetchAiRecommendations()
  ])
})
</script>

<style scoped>
.home-view { padding-bottom: 40px; }
.banner {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  border-radius: 16px;
  padding: 60px 40px;
  color: #fff;
  margin-bottom: 40px;
  text-align: center;
}
.banner h1 { font-size: 40px; font-weight: 700; margin-bottom: 12px; }
.banner p { font-size: 16px; color: rgba(255,255,255,.75); margin-bottom: 32px; }
.banner-search { max-width: 600px; margin: 0 auto 32px; }
.banner-stats { display: flex; justify-content: center; gap: 48px; margin-top: 32px; }
.stat-item { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.stat-item .num { font-size: 28px; font-weight: 700; color: #409eff; }
.stat-item span:last-child { font-size: 13px; color: rgba(255,255,255,.6); }

.section { margin-bottom: 40px; }
.section-title {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px;
}
.section-title h2 { display: flex; align-items: center; gap: 8px; font-size: 20px; }
.section-title a { color: #409eff; text-decoration: none; font-size: 14px; }

.category-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}
.category-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 12px;
  text-align: center;
  cursor: pointer;
  transition: all .2s;
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
}
.category-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(64,158,255,.2); }
.cat-icon { display: block; font-size: 28px; margin-bottom: 8px; }
.cat-name { font-size: 13px; color: #303133; }

.course-row { display: flex; gap: 20px; flex-wrap: wrap; }
.skeleton-row { display: flex; gap: 20px; }
</style>
