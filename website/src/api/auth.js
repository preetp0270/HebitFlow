import client from './client';

export const register = (data) => client.post('/api/auth/register', data);
export const login = (data) => client.post('/api/auth/login', data);
export const logout = (refreshToken) =>
  client.post('/api/auth/logout', { refreshToken });
export const getMe = () => client.get('/api/auth/me');
export const forgotPassword = (email) =>
  client.post('/api/auth/forgot-password', { email });
export const resetPassword = (token, password) =>
  client.post('/api/auth/reset-password', { token, password });
