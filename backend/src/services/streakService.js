const HabitCompletion = require('../models/HabitCompletion');
const { toDateOnly, addDays, isHabitScheduledOn, formatDateOnly } = require('../utils/dateHelpers');

/**
 * Calculate streak stats for a single habit.
 * For DAILY / SELECTED_DAYS: counts consecutive scheduled days completed (working backwards from today or last completion).
 * For WEEKLY_TARGET: simpler total-based approximation; still returns current/longest based on consecutive weeks meeting target where possible.
 */
async function calculateHabitStreak(habit, userId, asOfDate = null) {
  const today = toDateOnly(asOfDate || new Date());
  const completions = await HabitCompletion.find({
    habitId: habit._id,
    userId,
  })
    .select('date')
    .lean();

  const completedSet = new Set(
    completions.map((c) => formatDateOnly(c.date))
  );

  const totalCompleted = completions.length;

  // Build list of scheduled dates from startDate up to today (cap at ~2 years for performance)
  const start = toDateOnly(habit.startDate);
  const maxDays = 730;
  const scheduledDates = [];
  let cursor = new Date(start);
  let count = 0;
  while (cursor <= today && count < maxDays) {
    if (isHabitScheduledOn(habit, cursor)) {
      scheduledDates.push(formatDateOnly(cursor));
    }
    cursor = addDays(cursor, 1);
    count++;
  }

  // Current streak: walk backwards from the most recent scheduled day <= today
  let currentStreak = 0;
  for (let i = scheduledDates.length - 1; i >= 0; i--) {
    const d = scheduledDates[i];
    if (completedSet.has(d)) {
      currentStreak++;
    } else {
      // If this scheduled day is in the future relative to "today" we shouldn't reach here
      break;
    }
  }

  // Longest streak
  let longestStreak = 0;
  let run = 0;
  for (const d of scheduledDates) {
    if (completedSet.has(d)) {
      run++;
      longestStreak = Math.max(longestStreak, run);
    } else {
      run = 0;
    }
  }

  const scheduledCount = scheduledDates.length || 1;
  const completionPercentage = Math.round((totalCompleted / scheduledCount) * 100);

  return {
    currentStreak,
    longestStreak,
    totalCompleted,
    completionPercentage: Math.min(100, completionPercentage),
    scheduledCount,
  };
}

/**
 * Overall user streak stats across all active habits (simple aggregate).
 */
async function calculateUserStats(userId, habits) {
  let totalCompletions = 0;
  let bestStreak = 0;
  let currentBest = 0;
  const habitStats = [];

  for (const habit of habits) {
    const stats = await calculateHabitStreak(habit, userId);
    totalCompletions += stats.totalCompleted;
    bestStreak = Math.max(bestStreak, stats.longestStreak);
    currentBest = Math.max(currentBest, stats.currentStreak);
    habitStats.push({
      habitId: habit._id,
      name: habit.name,
      ...stats,
    });
  }

  // Most / least consistent by completion percentage
  let mostConsistent = null;
  let leastConsistent = null;
  if (habitStats.length > 0) {
    mostConsistent = habitStats.reduce((a, b) =>
      a.completionPercentage >= b.completionPercentage ? a : b
    );
    leastConsistent = habitStats.reduce((a, b) =>
      a.completionPercentage <= b.completionPercentage ? a : b
    );
  }

  return {
    totalCompletions,
    bestStreak,
    currentStreak: currentBest,
    mostConsistent,
    leastConsistent,
    habitStats,
  };
}

module.exports = {
  calculateHabitStreak,
  calculateUserStats,
};
