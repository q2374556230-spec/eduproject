import request from '@/utils/request'

function normalizePageParams(params = {}) {
  const { pageNum, pageSize, ...rest } = params
  return {
    ...rest,
    ...(pageNum !== undefined ? { page: pageNum } : {}),
    ...(pageSize !== undefined ? { size: pageSize } : {})
  }
}

export const userApi = {
  login: data => request.post('/user/login', {
    username: data.username || data.account,
    password: data.password
  }),
  register: data => request.post('/user/register', data),
  logout: () => request.post('/user/logout'),
  getProfile: () => request.get('/user/profile'),
  updateProfile: data => request.put('/user/profile', data),
  changePassword: data => request.put('/user/password', data),
  getUserList: params => request.get('/user/admin/list', { params: normalizePageParams(params) }),
  updateUserStatus: (id, status) => request.put(`/user/admin/${id}/status`, null, { params: { status } })
}
