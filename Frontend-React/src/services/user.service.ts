import http from '../api/http';

const json = { headers: { 'Content-Type': 'application/json' } };

export const UserService = {
  signup: (data: any) => http.post('/user/signup', data, json),
  verifySignupOtp: (data: any) => http.post('/user/verifySignupOtp', data, json),
  resendSignupOtp: (data: any) => http.post('/user/resendSignupOtp', data, json),
  forgotPassword: (data: any) => http.post('/user/forgotPassword', data, json),
  login: (data: any) => http.post('/user/login', data, json),
  checkToken: () => http.get('/user/checkToken'),
  changePassword: (data: any) => http.post('/user/changePassword', data, json),
  getUsers: () => http.get('/user/get'),
  update: (data: any) => http.post('/user/update', data, json),
};
