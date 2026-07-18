import http from '../api/http';

export const DashboardService = {
  getDetails: () => http.get('/dashboard/details'),
  getAnalytics: () => http.get('/dashboard/analytics'),
  getKitchenQueue: () => http.get('/dashboard/kitchen-queue'),
};
