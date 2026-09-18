/**
 * Unit tests for the streak calculation logic.
 * Mirrors the algorithm in src/routes/cookSessions.js:
 *   - Same day as last cook   → streak unchanged
 *   - Yesterday                → streak + 1
 *   - Anything older / no date → streak resets to 1
 *
 * Written as a pure-function replica so the tests run without a
 * database or a live server.
 */

function calculateNewStreak(lastCookedDate, currentStreak, today) {
  if (!lastCookedDate) return 1;
  if (lastCookedDate === today) return currentStreak;

  const yesterday = new Date(new Date(today).getTime() - 86400000)
    .toISOString()
    .slice(0, 10);

  if (lastCookedDate === yesterday) return currentStreak + 1;
  return 1;
}

describe('Streak calculation', () => {
  const today = '2026-09-18';
  const yesterday = '2026-09-17';
  const longAgo = '2026-09-10';

  it('starts a new streak at 1 when there is no previous cook', () => {
    expect(calculateNewStreak(null, 0, today)).toBe(1);
  });

  it('increments the streak by 1 when the last cook was yesterday', () => {
    expect(calculateNewStreak(yesterday, 5, today)).toBe(6);
  });

  it('leaves the streak unchanged when the last cook was today', () => {
    expect(calculateNewStreak(today, 5, today)).toBe(5);
  });

  it('resets the streak to 1 when the last cook was more than a day ago', () => {
    expect(calculateNewStreak(longAgo, 12, today)).toBe(1);
  });

  it('increments from zero correctly (first ever cook yesterday)', () => {
    expect(calculateNewStreak(yesterday, 0, today)).toBe(1);
  });

  it('handles a long streak reset after a gap', () => {
    expect(calculateNewStreak('2026-08-01', 42, today)).toBe(1);
  });
});