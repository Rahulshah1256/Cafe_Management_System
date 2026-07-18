import http from '../api/http';

export const LoyaltyService = {
  getBalance: () => http.get('/loyalty/balance'),
};
