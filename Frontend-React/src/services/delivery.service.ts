import http from '../api/http';

export const DeliveryService = {
  registerPartner: (data: any) => http.post('/delivery/partners', data),
  getAllPartners: () => http.get('/delivery/partners'),
  getAvailablePartners: () => http.get('/delivery/partners/available'),
  updateAvailability: (status: string) => http.put('/delivery/availability', { status }),
  assignPartner: (billId: any, partnerEmail: string) =>
    http.put('/delivery/assign/' + billId, { partnerEmail }),
  getMyDeliveries: () => http.get('/delivery/my-deliveries'),
  completeDelivery: (billId: any) => http.put('/delivery/complete/' + billId, {}),
};
