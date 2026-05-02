import request from '@/utils/request'

function normalizePageParams(params = {}) {
  const { pageNum, pageSize, ...rest } = params
  return {
    ...rest,
    ...(pageNum !== undefined ? { page: pageNum } : {}),
    ...(pageSize !== undefined ? { size: pageSize } : {})
  }
}

export const orderApi = {
  createOrder: data => request.post('/order/create', data),
  payOrder: orderId => request.post(`/order/${orderId}/pay`),
  getMyOrders: params => request.get('/order/list', { params: normalizePageParams(params) }),
  getOrderDetail: orderId => request.get(`/order/${orderId}`),
  cancelOrder: orderId => request.post(`/order/${orderId}/cancel`),
  getAllOrders: params => request.get('/order/admin/list', { params: normalizePageParams(params) }),
  getStats: () => request.get('/order/admin/stats')
}
