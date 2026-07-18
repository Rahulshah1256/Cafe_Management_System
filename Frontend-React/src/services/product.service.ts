import http from '../api/http';

const json = { headers: { 'Content-Type': 'application/json' } };

export const ProductService = {
  add: (data: any) => http.post('/product/add', data, json),
  update: (data: any) => http.post('/product/update', data, json),
  getProducts: () => http.get('/product/get'),
  getRecommendations: () => http.get('/product/recommendations'),
  updateStatus: (data: any) => http.post('/product/updateProductStatus', data, json),
  delete: (id: any) => http.post('/product/delete/' + id, json),
  getProductByCategory: (id: any) => http.get('/product/getByCategory/' + id),
  getById: (id: any) => http.get('/product/getProductById/' + id),
  search: (params: Record<string, any>) => http.get('/product/search', { params }),
};
