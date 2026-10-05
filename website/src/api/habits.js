import client from './client';

export const getHabits = () => client.get('/api/habits');
export const getToday = (date) =>
  client.get('/api/habits/today', { params: date ? { date } : {} });
export const getHabit = (id) => client.get(`/api/habits/${id}`);
export const createHabit = (data) => client.post('/api/habits', data);
export const updateHabit = (id, data) => client.put(`/api/habits/${id}`, data);
export const deleteHabit = (id) => client.delete(`/api/habits/${id}`);
export const completeHabit = (id, date) =>
  client.post(`/api/habits/${id}/complete`, date ? { date } : {});
export const uncompleteHabit = (id, date) =>
  client.delete(`/api/habits/${id}/complete`, { data: date ? { date } : {} });
export const getHabitHistory = (id, from, to) =>
  client.get(`/api/habits/${id}/history`, { params: { from, to } });
