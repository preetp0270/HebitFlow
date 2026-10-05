import client from './client';

export const getStatistics = () => client.get('/api/statistics');
export const getWeekly = () => client.get('/api/statistics/weekly');
export const getMonthly = (year, month) =>
  client.get('/api/statistics/monthly', { params: { year, month } });
