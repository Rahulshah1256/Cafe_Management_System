import http from '../api/http';

const json = { headers: { 'Content-Type': 'application/json' } };

export const StoreService = {
  getActiveStores: () => http.get('/store/list'),
  getAllStores: () => http.get('/store/all'),
  addStore: (data: any) => http.post('/store/add', data, json),
  updateStore: (id: any, data: any) => http.put('/store/update/' + id, data, json),
  deleteStore: (id: any) => http.delete('/store/delete/' + id),
};
