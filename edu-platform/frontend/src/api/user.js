import request from '@/utils/request'

export const userApi = {
  // 登录
  login: data => request.post('/user/login', data),
  // 注册
  register: data => request.post('/user/register', data),
  // 登出
  logout: () => request.post('/user/logout'),
  // 获取当前用户信息
  getProfile: () => request.get('/user/profile'),
  // 更新用户信息
  updateProfile: data => request.put('/user/profile', data),
  // 修改密码
  changePassword: data => request.put('/user/password', data),
  // 管理员：获取用户列表
  getUserList: params => request.get('/user/admin/list', { params }),
  // 管理员：更新用户状态
  updateUserStatus: (id, status) => request.put(`/user/admin/${id}/status`, { status })
}
