import http from '../api/http';

const json = { headers: { 'Content-Type': 'application/json' } };

export const CategoryService = {
  add: (data: any) => http.post('/category/add', data, json),
  update: (data: any) => http.post('/category/update', data, json),
  getCategorys: () => http.get('/category/get'),
  getFilteredCategorys: () => http.get('/category/get?filterValue=true'),
};
