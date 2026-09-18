/**
 * Unit tests for the pantry match scoring logic.
 * Mirrors the algorithm in src/routes/recipes.js (loadFullRecipe +
 * /suggestions): matchPercent = ingredients held / total ingredients * 100.
 *
 * Matching is case-insensitive and trims whitespace, matching the
 * server-side LOWER(TRIM(name)) comparison.
 */

function calculateMatchPercent(recipeIngredients, pantryNames) {
  if (recipeIngredients.length === 0) return 0;

  const normalizedPantry = pantryNames.map((n) => n.trim().toLowerCase());
  const held = recipeIngredients.filter((i) =>
    normalizedPantry.includes(i.trim().toLowerCase()),
  );
  return (held.length / recipeIngredients.length) * 100;
}

describe('Pantry match scoring', () => {
  it('returns 100 when all ingredients are in the pantry', () => {
    const recipe = ['flour', 'eggs', 'milk'];
    const pantry = ['flour', 'eggs', 'milk', 'sugar'];
    expect(calculateMatchPercent(recipe, pantry)).toBe(100);
  });

  it('returns 0 when nothing is in the pantry', () => {
    const recipe = ['flour', 'eggs', 'milk'];
    const pantry = ['rice', 'pasta'];
    expect(calculateMatchPercent(recipe, pantry)).toBe(0);
  });

  it('returns 50 when half the ingredients are in the pantry', () => {
    const recipe = ['flour', 'eggs', 'milk', 'sugar'];
    const pantry = ['flour', 'eggs', 'rice'];
    expect(calculateMatchPercent(recipe, pantry)).toBe(50);
  });

  it('is case-insensitive on both sides', () => {
    const recipe = ['Flour', 'Eggs'];
    const pantry = ['flour', 'eggs'];
    expect(calculateMatchPercent(recipe, pantry)).toBe(100);
  });

  it('returns 0 for an empty recipe (no ingredients)', () => {
    expect(calculateMatchPercent([], ['flour'])).toBe(0);
  });

  it('handles whitespace around ingredient names', () => {
    const recipe = ['  flour  ', 'eggs'];
    const pantry = ['flour', 'eggs'];
    expect(calculateMatchPercent(recipe, pantry)).toBe(100);
  });

  it('returns 25 when only a quarter of ingredients match', () => {
    const recipe = ['flour', 'eggs', 'milk', 'sugar'];
    const pantry = ['flour', 'rice'];
    expect(calculateMatchPercent(recipe, pantry)).toBe(25);
  });

  it('handles duplicate pantry entries without inflating the match', () => {
    const recipe = ['flour', 'eggs'];
    const pantry = ['flour', 'flour', 'flour', 'rice'];
    expect(calculateMatchPercent(recipe, pantry)).toBe(50);
  });
});