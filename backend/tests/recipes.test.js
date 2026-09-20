const request = require('supertest');
const app = require('../server');

describe('Recipe and book routes require auth', () => {
  it('GET /api/recipes without a token returns 401', async () => {
    const res = await request(app).get('/api/recipes');
    expect(res.statusCode).toBe(401);
  });

  it('GET /api/recipes/discover without a token returns 401', async () => {
    const res = await request(app).get('/api/recipes/discover');
    expect(res.statusCode).toBe(401);
  });

  it('GET /api/recipes/suggestions without a token returns 401', async () => {
    const res = await request(app).get('/api/recipes/suggestions');
    expect(res.statusCode).toBe(401);
  });

  it('GET /api/recipes/pantry-health without a token returns 401', async () => {
    const res = await request(app).get('/api/recipes/pantry-health');
    expect(res.statusCode).toBe(401);
  });

  it('POST /api/recipes without a token returns 401', async () => {
    const res = await request(app).post('/api/recipes').send({ title: 'Test' });
    expect(res.statusCode).toBe(401);
  });

  it('GET /api/recipes/:id without a token returns 401', async () => {
    const res = await request(app).get('/api/recipes/00000000-0000-0000-0000-000000000000');
    expect(res.statusCode).toBe(401);
  });

  it('DELETE /api/recipes/:id without a token returns 401', async () => {
    const res = await request(app).delete('/api/recipes/00000000-0000-0000-0000-000000000000');
    expect(res.statusCode).toBe(401);
  });

  it('GET /api/books without a token returns 401', async () => {
    const res = await request(app).get('/api/books');
    expect(res.statusCode).toBe(401);
  });

  it('POST /api/books without a token returns 401', async () => {
    const res = await request(app).post('/api/books').send({ name: 'Test Book' });
    expect(res.statusCode).toBe(401);
  });
});