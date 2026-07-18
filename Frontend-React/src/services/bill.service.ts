import http from '../api/http';

const json = { headers: { 'Content-Type': 'application/json' } };

export const BillService = {
  generateReport: (data: any) => http.post('/bill/generateReport', data, json),
  getPdf: (data: any) => http.post('/bill/getPdf', data, { responseType: 'blob' }),
  getBills: () => http.get('/bill/getBills'),
  delete: (id: any) => http.post('/bill/delete/' + id, json),
  updateOrderStatus: (id: any, status: string) => http.put('/bill/orderStatus/' + id, { status }, json),
  cancelOrder: (id: any) => http.put('/bill/cancel/' + id, {}, json),
};
