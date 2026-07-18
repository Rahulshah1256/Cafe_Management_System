import http from '../api/http';

export const PaymentService = {
  getStatus: (uuid: string) => http.get('/payment/status/' + uuid),
  retryPayment: (uuid: string) => http.post('/payment/retry/' + uuid),
  refund: (uuid: string) => http.put('/payment/refund/' + uuid),
  verifyPayment: (payload: {
    billUuid: string;
    razorpayOrderId: string;
    razorpayPaymentId: string;
    razorpaySignature: string;
  }) => http.post('/payment/verify', payload),
};
