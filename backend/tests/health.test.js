const request = require('supertest');
const app = require('../server');

describe('Health check', () => {
  it('GET / returns ok status', async () => {
    const res = await request(app).get('/');
    expect(res.statusCode).toBe(200);
    expect(res.body.status).toBe('ok');
  });

  it('GET /health returns ok status', async () => {
    const res = await request(app).get('/health');
    expect(res.statusCode).toBe(200);
    expect(res.body).toEqual({ status: 'ok' });
  });

  it('unknown routes return 404 with a JSON error', async () => {
    const res = await request(app).get('/api/this-route-does-not-exist');
    expect(res.statusCode).toBe(404);
    expect(res.body).toHaveProperty('error');
  });
});

describe('Auth is required on protected routes', () => {
  it('GET /api/users/me without a token returns 401', async () => {
    const res = await request(app).get('/api/users/me');
    expect(res.statusCode).toBe(401);
  });

  it('GET /api/recipes without a token returns 401', async () => {
    const res = await request(app).get('/api/recipes');
    expect(res.statusCode).toBe(401);
  });

  it('a garbage bearer token is rejected, not crashed on', async () => {
    const res = await request(app).get('/api/users/me').set('Authorization', 'Bearer not-a-real-token');
    expect(res.statusCode).toBe(401);
  });
});