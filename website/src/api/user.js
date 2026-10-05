import client from './client';

export const updateProfile = (data) => client.put('/api/users/me', data);
export const changePassword = (data) => client.put('/api/users/me/password', data);
export const deleteAccount = (password) =>
  client.delete('/api/users/me', { data: { password } });
export const exportData = () => client.get('/api/users/me/export');
export const importData = (data) => client.post('/api/users/me/import', data);
export const getAchievements = () => client.get('/api/achievements');
