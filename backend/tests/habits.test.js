const request = require('supertest');
const mongoose = require('mongoose');
const { MongoMemoryServer } = require('mongodb-memory-server');
const app = require('../src/server');

let mongoServer;
let accessToken;
let userId;

beforeAll(async () => {
  mongoServer = await MongoMemoryServer.create();
  process.env.MONGODB_URI = mongoServer.getUri();
  process.env.JWT_SECRET = 'test_jwt_secret';
  process.env.JWT_REFRESH_SECRET = 'test_refresh_secret';
  process.env.NODE_ENV = 'test';
  await mongoose.connect(process.env.MONGODB_URI);

  const reg = await request(app).post('/api/auth/register').send({
    name: 'Habit User',
    email: 'habits@example.com',
    password: 'password123',
  });
  accessToken = reg.body.data.accessToken;
  userId = reg.body.data.user._id;
});

afterAll(async () => {
  await mongoose.disconnect();
  if (mongoServer) await mongoServer.stop();
});

const auth = () => ({ Authorization: `Bearer ${accessToken}` });

describe('Habits API', () => {
  let habitId;

  it('creates a daily habit', async () => {
    const res = await request(app)
      .post('/api/habits')
      .set(auth())
      .send({ name: 'Drink Water', frequencyType: 'DAILY', icon: '💧' });
    expect(res.status).toBe(201);
    expect(res.body.data.habit.name).toBe('Drink Water');
    habitId = res.body.data.habit._id;
  });

  it('lists habits', async () => {
    const res = await request(app).get('/api/habits').set(auth());
    expect(res.status).toBe(200);
    expect(res.body.data.habits.length).toBeGreaterThanOrEqual(1);
  });

  it('completes a habit for today', async () => {
    const res = await request(app)
      .post(`/api/habits/${habitId}/complete`)
      .set(auth())
      .send({});
    expect(res.status).toBe(201);
    expect(res.body.data.completion).toBeDefined();
  });

  it('prevents duplicate completion', async () => {
    const res = await request(app)
      .post(`/api/habits/${habitId}/complete`)
      .set(auth())
      .send({});
    expect(res.status).toBe(409);
  });

  it('returns today habits with progress', async () => {
    const res = await request(app).get('/api/habits/today').set(auth());
    expect(res.status).toBe(200);
    expect(res.body.data.progress).toBeDefined();
    expect(res.body.data.items.length).toBeGreaterThanOrEqual(1);
  });

  it('uncompletes a habit', async () => {
    const res = await request(app)
      .delete(`/api/habits/${habitId}/complete`)
      .set(auth())
      .send({});
    expect(res.status).toBe(200);
  });

  it('soft-deletes a habit', async () => {
    const res = await request(app)
      .delete(`/api/habits/${habitId}`)
      .set(auth());
    expect(res.status).toBe(200);
  });

  it('rejects access without token', async () => {
    const res = await request(app).get('/api/habits');
    expect(res.status).toBe(401);
  });
});

describe('Health check', () => {
  it('returns ok', async () => {
    const res = await request(app).get('/health');
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('ok');
  });
});
