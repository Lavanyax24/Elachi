const request = require('supertest');
const app = require('../server');

// These tests deliberately don't need a real database or Firebase project —
// every case here is rejected by requireAuth (or by users.js's own manual
// token check on /sync) before any of that is touched, which is exactly
// what keeps this suite safe to run in CI with no secrets configured.
describe('Users routes require auth', () => {
  it('POST /api/users/sync without a token returns 401', async () => {
    const res = await request(app).post('/api/users/sync').send({});
    expect(res.statusCode).toBe(401);
  });

  it('GET /api/users/me without a token returns 401', async () => {
    const res = await request(app).get('/api/users/me');
    expect(res.statusCode).toBe(401);
  });

  it('PATCH /api/users/me without a token returns 401', async () => {
    const res = await request(app).patch('/api/users/me').send({ bio: 'hi' });
    expect(res.statusCode).toBe(401);
  });

  it('DELETE /api/users/me without a token returns 401', async () => {
    const res = await request(app).delete('/api/users/me');
    expect(res.statusCode).toBe(401);
  });

  it('POST /api/users/me/notification-token without a token returns 401', async () => {
    const res = await request(app).post('/api/users/me/notification-token').send({ fcmToken: 'abc' });
    expect(res.statusCode).toBe(401);
  });

  it('GET /api/users/me/notification-preferences without a token returns 401', async () => {
    const res = await request(app).get('/api/users/me/notification-preferences');
    expect(res.statusCode).toBe(401);
  });

  it('a garbage bearer token on GET /api/users/me is rejected, not crashed on', async () => {
    const res = await request(app).get('/api/users/me').set('Authorization', 'Bearer not-a-real-token');
    expect(res.statusCode).toBe(401);
  });
});