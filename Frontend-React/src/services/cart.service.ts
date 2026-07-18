import http from '../api/http';

const json = { headers: { 'Content-Type': 'application/json' } };

export const CartService = {
  getCart: () => http.get('/cart/get'),
  addItem: (data: { productId: any; quantity: any }) => http.post('/cart/add', data, json),
  updateItem: (itemId: any, data: { quantity: any }) => http.put('/cart/item/' + itemId, data, json),
  removeItem: (itemId: any) => http.delete('/cart/item/' + itemId),
  clearCart: () => http.delete('/cart/clear'),
  applyCoupon: (code: string) => http.post('/cart/coupon/apply', { code }, json),
  removeCoupon: () => http.delete('/cart/coupon/remove'),
  checkout: (data: { addressId: any; paymentMethod: string; deliveryInstructions?: string; useLoyaltyPoints?: boolean }) =>
    http.post('/cart/checkout', data, json),
};
