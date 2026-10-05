const Achievement = require('../models/Achievement');
const Habit = require('../models/Habit');
const HabitCompletion = require('../models/HabitCompletion');
const { calculateHabitStreak } = require('./streakService');

const DEFINITIONS = {
  FIRST_HABIT: {
    title: 'First Habit',
    description: 'Created your first habit',
  },
  STREAK_7: {
    title: '7 Day Streak',
    description: 'Completed a habit for 7 scheduled days in a row',
  },
  STREAK_30: {
    title: '30 Day Streak',
    description: 'Completed a habit for 30 scheduled days in a row',
  },
  STREAK_100: {
    title: '100 Day Streak',
    description: 'Completed a habit for 100 scheduled days in a row',
  },
  COMPLETIONS_100: {
    title: '100 Completions',
    description: 'Completed habits 100 times in total',
  },
  COMPLETIONS_500: {
    title: '500 Completions',
    description: 'Completed habits 500 times in total',
  },
  PERFECT_WEEK: {
    title: 'Perfect Week',
    description: 'Completed all scheduled habits for 7 consecutive days',
  },
  PERFECT_MONTH: {
    title: 'Perfect Month',
    description: 'Completed all scheduled habits for 30 consecutive days',
  },
};

/**
 * Check and unlock achievements for a user. Idempotent.
 */
async function evaluateAchievements(userId, context = {}) {
  const unlocked = [];
  const existing = await Achievement.find({ userId }).select('type').lean();
  const have = new Set(existing.map((a) => a.type));

  const habitCount = await Habit.countDocuments({ userId, isActive: true });
  const totalCompletions = await HabitCompletion.countDocuments({ userId });

  // FIRST_HABIT
  if (!have.has('FIRST_HABIT') && habitCount >= 1) {
    unlocked.push(await createIfMissing(userId, 'FIRST_HABIT'));
  }

  // Completion milestones
  if (!have.has('COMPLETIONS_100') && totalCompletions >= 100) {
    unlocked.push(await createIfMissing(userId, 'COMPLETIONS_100'));
  }
  if (!have.has('COMPLETIONS_500') && totalCompletions >= 500) {
    unlocked.push(await createIfMissing(userId, 'COMPLETIONS_500'));
  }

  // Streak-based – check all active habits
  if (!have.has('STREAK_7') || !have.has('STREAK_30') || !have.has('STREAK_100')) {
    const habits = await Habit.find({ userId, isActive: true });
    for (const habit of habits) {
      const stats = await calculateHabitStreak(habit, userId);
      if (!have.has('STREAK_7') && stats.currentStreak >= 7) {
        unlocked.push(await createIfMissing(userId, 'STREAK_7', habit._id));
        have.add('STREAK_7');
      }
      if (!have.has('STREAK_30') && stats.currentStreak >= 30) {
        unlocked.push(await createIfMissing(userId, 'STREAK_30', habit._id));
        have.add('STREAK_30');
      }
      if (!have.has('STREAK_100') && stats.currentStreak >= 100) {
        unlocked.push(await createIfMissing(userId, 'STREAK_100', habit._id));
        have.add('STREAK_100');
      }
    }
  }

  // Perfect week / month are more expensive; skip in hot path unless requested
  // Can be expanded later with dedicated queries

  return unlocked.filter(Boolean);
}

async function createIfMissing(userId, type, habitId = null) {
  const def = DEFINITIONS[type];
  if (!def) return null;
  try {
    const doc = await Achievement.create({
      userId,
      type,
      title: def.title,
      description: def.description,
      habitId,
    });
    return doc;
  } catch (err) {
    // Duplicate key – already unlocked
    if (err.code === 11000) return null;
    throw err;
  }
}

async function getUserAchievements(userId) {
  return Achievement.find({ userId }).sort({ unlockedAt: -1 }).lean();
}

module.exports = {
  evaluateAchievements,
  getUserAchievements,
  DEFINITIONS,
};
