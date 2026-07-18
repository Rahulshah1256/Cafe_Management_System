import http from '../api/http';

const json = { headers: { 'Content-Type': 'application/json' } };

export const CouponService = {
  getAllCoupons: () => http.get('/coupon/get'),
  addCoupon: (data: any) => http.post('/coupon/add', data, json),
  updateCoupon: (id: any, data: any) => http.put('/coupon/update/' + id, data, json),
  deleteCoupon: (id: any) => http.delete('/coupon/delete/' + id),
};
