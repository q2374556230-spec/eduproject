import request from '@/utils/request'

export const courseApi = {
  // 获取课程列表
  getCourseList: params => request.get('/course/list', { params }),
  // 获取课程详情
  getCourseById: id => request.get(`/course/${id}`),
  // 获取课程分类
  getCategoryList: () => request.get('/course/category/list'),
  // AI推荐课程
  getAiRecommendations: () => request.get('/course/ai/recommend'),
  // 管理员：创建课程
  createCourse: data => request.post('/course/admin', data),
  // 管理员：更新课程
  updateCourse: (id, data) => request.put(`/course/admin/${id}`, data),
  // 管理员：删除课程
  deleteCourse: id => request.delete(`/course/admin/${id}`),
  // 管理员：发布课程
  publishCourse: id => request.put(`/course/admin/${id}/publish`),
  // 管理员：下架课程
  unpublishCourse: id => request.put(`/course/admin/${id}/unpublish`)
}
