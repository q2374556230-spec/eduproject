import request from '@/utils/request'

export const orderApi = {
  // 创建订单
  createOrder: data => request.post('/order/create', data),
  // 支付订单
  payOrder: orderNo => request.post(`/order/pay/${orderNo}`),
  // 获取我的订单列表
  getMyOrders: params => request.get('/order/my', { params }),
  // 获取订单详情
  getOrderDetail: orderNo => request.get(`/order/${orderNo}`),
  // 取消订单
  cancelOrder: orderNo => request.post(`/order/cancel/${orderNo}`),
  // 管理员：获取所有订单
  getAllOrders: params => request.get('/order/admin/list', { params }),
  // 统计数据
  getStats: () => request.get('/order/admin/stats')
}
