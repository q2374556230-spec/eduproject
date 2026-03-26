import request from '@/utils/request'

export const notifyApi = {
  // 获取我的通知列表
  getMyNotifications: params => request.get('/notify/my', { params }),
  // 标记已读
  markAsRead: id => request.put(`/notify/${id}/read`),
  // 全部已读
  markAllAsRead: () => request.put('/notify/read-all'),
  // 未读数量
  getUnreadCount: () => request.get('/notify/unread-count')
}
