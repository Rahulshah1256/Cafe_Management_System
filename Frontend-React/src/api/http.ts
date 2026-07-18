import axios from 'axios';
import { environment } from '../environment';

const http = axios.create({
  baseURL: environment.apiUrl,
});

// Request interceptor: attach Bearer token (mirrors TokenInterceptorInterceptor)
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Normalizes the backend response body so a `message` field is always present.
// The Spring backend returns its text under the key `messag` (a typo in CafeUtils),
// while some endpoints use `message`. This keeps the frontend consistent.
function normalizeMessage(data: any): any {
  if (data && typeof data === 'object' && !(data instanceof Blob)) {
    if (data.message === undefined && data.messag !== undefined) {
      data.message = data.messag;
    }
  }
  return data;
}

// Response interceptor: normalize messages + handle 401/403 by clearing session.
http.interceptors.response.use(
  (response) => {
    normalizeMessage(response.data);
    return response;
  },
  (error) => {
    if (error?.response) {
      normalizeMessage(error.response.data);
    }
    const status = error?.response?.status;
    if (status === 401 || status === 403) {
      if (window.location.pathname !== '/') {
        localStorage.clear();
        window.location.assign('/');
      }
    }
    return Promise.reject(error);
  }
);

export default http;
