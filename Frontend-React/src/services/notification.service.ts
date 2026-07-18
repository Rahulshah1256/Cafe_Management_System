import http from '../api/http';

export const NotificationService = {
  getMyNotifications: () => http.get('/notification/get'),
  getUnreadCount: () => http.get('/notification/unreadCount'),
  markAsRead: (id: any) => http.put('/notification/read/' + id, {}),
  markAllAsRead: () => http.put('/notification/readAll', {}),
};
