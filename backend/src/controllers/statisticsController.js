const Habit = require('../models/Habit');
const HabitCompletion = require('../models/HabitCompletion');
const asyncHandler = require('../utils/asyncHandler');
const { toDateOnly, addDays, formatDateOnly, isHabitScheduledOn } = require('../utils/dateHelpers');
const { calculateUserStats, calculateHabitStreak } = require('../services/streakService');

const getStatistics = asyncHandler(async (req, res) => {
  const habits = await Habit.find({ userId: req.user._id, isActive: true });
  const stats = await calculateUserStats(req.user._id, habits);

  const totalHabits = habits.length;
  const overallPercentage =
    stats.habitStats.length === 0
      ? 0
      : Math.round(
          stats.habitStats.reduce((s, h) => s + h.completionPercentage, 0) /
            stats.habitStats.length
        );

  res.json({
    success: true,
    data: {
      totalHabits,
      totalCompletions: stats.totalCompletions,
      currentStreak: stats.currentStreak,
      bestStreak: stats.bestStreak,
      overallCompletionPercentage: overallPercentage,
      mostConsistent: stats.mostConsistent
        ? {
            habitId: stats.mostConsistent.habitId,
            name: stats.mostConsistent.name,
            percentage: stats.mostConsistent.completionPercentage,
          }
        : null,
      leastConsistent: stats.leastConsistent
        ? {
            habitId: stats.leastConsistent.habitId,
            name: stats.leastConsistent.name,
            percentage: stats.leastConsistent.completionPercentage,
          }
        : null,
    },
  });
});

const getWeekly = asyncHandler(async (req, res) => {
  const today = toDateOnly(new Date());
  // Last 7 days including today
  const days = [];
  for (let i = 6; i >= 0; i--) {
    days.push(addDays(today, -i));
  }

  const habits = await Habit.find({ userId: req.user._id, isActive: true });
  const from = days[0];
  const to = days[6];

  const completions = await HabitCompletion.find({
    userId: req.user._id,
    date: { $gte: from, $lte: to },
  }).lean();

  const byDate = {};
  for (const d of days) {
    byDate[formatDateOnly(d)] = { scheduled: 0, completed: 0 };
  }

  for (const habit of habits) {
    for (const d of days) {
      if (isHabitScheduledOn(habit, d)) {
        const key = formatDateOnly(d);
        byDate[key].scheduled += 1;
      }
    }
  }

  for (const c of completions) {
    const key = formatDateOnly(c.date);
    if (byDate[key]) {
      byDate[key].completed += 1;
    }
  }

  const series = days.map((d) => {
    const key = formatDateOnly(d);
    const { scheduled, completed } = byDate[key];
    return {
      date: key,
      scheduled,
      completed,
      percentage: scheduled === 0 ? 0 : Math.round((completed / scheduled) * 100),
    };
  });

  res.json({ success: true, data: { series } });
});

const getMonthly = asyncHandler(async (req, res) => {
  const today = toDateOnly(new Date());
  const year = parseInt(req.query.year, 10) || today.getUTCFullYear();
  const month = parseInt(req.query.month, 10) || today.getUTCMonth() + 1; // 1-12

  const start = new Date(Date.UTC(year, month - 1, 1));
  const end = new Date(Date.UTC(year, month, 0)); // last day of month
  if (end > today) end.setTime(today.getTime());

  const habits = await Habit.find({ userId: req.user._id, isActive: true });
  const completions = await HabitCompletion.find({
    userId: req.user._id,
    date: { $gte: start, $lte: end },
  }).lean();

  const days = [];
  let cursor = new Date(start);
  while (cursor <= end) {
    days.push(new Date(cursor));
    cursor = addDays(cursor, 1);
  }

  const byDate = {};
  for (const d of days) {
    byDate[formatDateOnly(d)] = { scheduled: 0, completed: 0 };
  }

  for (const habit of habits) {
    for (const d of days) {
      if (isHabitScheduledOn(habit, d)) {
        byDate[formatDateOnly(d)].scheduled += 1;
      }
    }
  }

  for (const c of completions) {
    const key = formatDateOnly(c.date);
    if (byDate[key]) byDate[key].completed += 1;
  }

  const series = days.map((d) => {
    const key = formatDateOnly(d);
    const { scheduled, completed } = byDate[key];
    return {
      date: key,
      scheduled,
      completed,
      percentage: scheduled === 0 ? 0 : Math.round((completed / scheduled) * 100),
    };
  });

  res.json({
    success: true,
    data: {
      year,
      month,
      series,
    },
  });
});

module.exports = {
  getStatistics,
  getWeekly,
  getMonthly,
};
