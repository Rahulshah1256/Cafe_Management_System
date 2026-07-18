import http from '../api/http';

const json = { headers: { 'Content-Type': 'application/json' } };

export const AddressService = {
  getMyAddresses: () => http.get('/address/get'),
  addAddress: (data: any) => http.post('/address/add', data, json),
  updateAddress: (id: any, data: any) => http.put('/address/update/' + id, data, json),
  deleteAddress: (id: any) => http.delete('/address/delete/' + id),
  setDefault: (id: any) => http.put('/address/default/' + id, {}, json),
};
