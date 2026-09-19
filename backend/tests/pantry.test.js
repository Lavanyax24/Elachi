const request = require('supertest');
const app = require('../server');

describe('Pantry and shopping list routes require auth', () => {
  it('GET /api/pantry without a token returns 401', async () => {
    const res = await request(app).get('/api/pantry');
    expect(res.statusCode).toBe(401);
  });

  it('POST /api/pantry without a token returns 401', async () => {
    const res = await request(app).post('/api/pantry').send({ name: 'Rice', quantity: 1, unit: 'kg' });
    expect(res.statusCode).toBe(401);
  });

  it('DELETE /api/pantry/:id without a token returns 401', async () => {
    const res = await request(app).delete('/api/pantry/00000000-0000-0000-0000-000000000000');
    expect(res.statusCode).toBe(401);
  });

  it('GET /api/shopping-list without a token returns 401', async () => {
    const res = await request(app).get('/api/shopping-list');
    expect(res.statusCode).toBe(401);
  });

  it('POST /api/shopping-list without a token returns 401', async () => {
    const res = await request(app).post('/api/shopping-list').send({ name: 'Eggs', quantity: 6, unit: 'pcs' });
    expect(res.statusCode).toBe(401);
  });

  it('POST /api/shopping-list/generate without a token returns 401', async () => {
    const res = await request(app).post('/api/shopping-list/generate').send({ recipeId: 'x' });
    expect(res.statusCode).toBe(401);
  });

  it('PATCH /api/shopping-list/:id without a token returns 401', async () => {
    const res = await request(app).patch('/api/shopping-list/00000000-0000-0000-0000-000000000000').send({ isBought: true });
    expect(res.statusCode).toBe(401);
  });
});