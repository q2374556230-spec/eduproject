import { defineStore } from 'pinia'
import { courseApi } from '@/api/course'

export const useCourseStore = defineStore('course', {
  state: () => ({
    categories: [],
    courseList: [],
    total: 0,
    loading: false,
    aiRecommendations: []
  }),

  actions: {
    async fetchCategories() {
      if (this.categories.length) return
      const res = await courseApi.getCategoryList()
      this.categories = res.data || []
    },

    async fetchCourseList(params) {
      this.loading = true
      try {
        const res = await courseApi.getCourseList(params)
        this.courseList = res.data?.records || []
        this.total = res.data?.total || 0
      } finally {
        this.loading = false
      }
    },

    async fetchAiRecommendations(params = {}) {
      const res = await courseApi.getAiRecommendations(params)
      this.aiRecommendations = res.data || []
    }
  }
})
