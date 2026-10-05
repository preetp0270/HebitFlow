const Habit = require('../models/Habit');
const HabitCompletion = require('../models/HabitCompletion');
const ApiError = require('../utils/ApiError');
const asyncHandler = require('../utils/asyncHandler');
const { toDateOnly, formatDateOnly, isHabitScheduledOn } = require('../utils/dateHelpers');
const { calculateHabitStreak } = require('../services/streakService');
const { evaluateAchievements } = require('../services/achievementService');

const listHabits = asyncHandler(async (req, res) => {
  const habits = await Habit.find({ userId: req.user._id, isActive: true }).sort({
    createdAt: -1,
  });
  res.json({ success: true, data: { habits } });
});

const getHabit = asyncHandler(async (req, res) => {
  const habit = await Habit.findOne({
    _id: req.params.id,
    userId: req.user._id,
  });
  if (!habit) {
    throw new ApiError(404, 'Habit not found');
  }

  const stats = await calculateHabitStreak(habit, req.user._id);
  res.json({ success: true, data: { habit, stats } });
});

const createHabit = asyncHandler(async (req, res) => {
  const {
    name,
    description,
    icon,
    color,
    frequencyType,
    selectedDays,
    targetPerWeek,
    reminderEnabled,
    reminderTime,
    startDate,
  } = req.body;

  const habit = await Habit.create({
    userId: req.user._id,
    name,
    description: description || '',
    icon: icon || '🎯',
    color: color || '#6366F1',
    frequencyType: frequencyType || 'DAILY',
    selectedDays: selectedDays || [],
    targetPerWeek: targetPerWeek || 1,
    reminderEnabled: !!reminderEnabled,
    reminderTime: reminderTime || null,
    startDate: startDate ? toDateOnly(startDate) : undefined,
  });

  await evaluateAchievements(req.user._id);

  res.status(201).json({ success: true, data: { habit } });
});

const updateHabit = asyncHandler(async (req, res) => {
  const habit = await Habit.findOne({
    _id: req.params.id,
    userId: req.user._id,
  });
  if (!habit) {
    throw new ApiError(404, 'Habit not found');
  }

  const allowed = [
    'name',
    'description',
    'icon',
    'color',
    'frequencyType',
    'selectedDays',
    'targetPerWeek',
    'reminderEnabled',
    'reminderTime',
    'startDate',
    'isActive',
  ];

  for (const key of allowed) {
    if (req.body[key] !== undefined) {
      if (key === 'startDate') {
        habit[key] = toDateOnly(req.body[key]);
      } else {
        habit[key] = req.body[key];
      }
    }
  }

  await habit.save();
  res.json({ success: true, data: { habit } });
});

const deleteHabit = asyncHandler(async (req, res) => {
  const habit = await Habit.findOne({
    _id: req.params.id,
    userId: req.user._id,
  });
  if (!habit) {
    throw new ApiError(404, 'Habit not found');
  }

  // Soft delete + remove future-related completions optional; we keep history
  habit.isActive = false;
  await habit.save();

  // Optionally hard-delete completions: await HabitCompletion.deleteMany({ habitId: habit._id });
  res.json({ success: true, message: 'Habit deleted' });
});

const completeHabit = asyncHandler(async (req, res) => {
  const habit = await Habit.findOne({
    _id: req.params.id,
    userId: req.user._id,
    isActive: true,
  });
  if (!habit) {
    throw new ApiError(404, 'Habit not found');
  }

  const date = toDateOnly(req.body.date || new Date());
  const today = toDateOnly(new Date());
  if (date > today) {
    throw new ApiError(400, 'Cannot complete a habit for a future date');
  }

  if (!isHabitScheduledOn(habit, date)) {
    throw new ApiError(400, 'Habit is not scheduled on this date');
  }

  try {
    const completion = await HabitCompletion.create({
      habitId: habit._id,
      userId: req.user._id,
      date,
      completedAt: new Date(),
    });

    const stats = await calculateHabitStreak(habit, req.user._id);
    await evaluateAchievements(req.user._id);

    res.status(201).json({
      success: true,
      data: { completion, stats },
    });
  } catch (err) {
    if (err.code === 11000) {
      throw new ApiError(409, 'Habit already completed for this date');
    }
    throw err;
  }
});

const uncompleteHabit = asyncHandler(async (req, res) => {
  const habit = await Habit.findOne({
    _id: req.params.id,
    userId: req.user._id,
  });
  if (!habit) {
    throw new ApiError(404, 'Habit not found');
  }

  const date = toDateOnly(req.body.date || new Date());
  const result = await HabitCompletion.findOneAndDelete({
    habitId: habit._id,
    userId: req.user._id,
    date,
  });

  if (!result) {
    throw new ApiError(404, 'No completion found for this date');
  }

  const stats = await calculateHabitStreak(habit, req.user._id);
  res.json({ success: true, data: { stats }, message: 'Completion removed' });
});

const getHabitHistory = asyncHandler(async (req, res) => {
  const habit = await Habit.findOne({
    _id: req.params.id,
    userId: req.user._id,
  });
  if (!habit) {
    throw new ApiError(404, 'Habit not found');
  }

  const { from, to } = req.query;
  const filter = { habitId: habit._id, userId: req.user._id };
  if (from || to) {
    filter.date = {};
    if (from) filter.date.$gte = toDateOnly(from);
    if (to) filter.date.$lte = toDateOnly(to);
  }

  const completions = await HabitCompletion.find(filter).sort({ date: -1 }).lean();
  res.json({
    success: true,
    data: {
      completions: completions.map((c) => ({
        ...c,
        date: formatDateOnly(c.date),
      })),
    },
  });
});

/**
 * Today's habits with completion status
 */
const getTodayHabits = asyncHandler(async (req, res) => {
  const today = toDateOnly(req.query.date || new Date());
  const habits = await Habit.find({ userId: req.user._id, isActive: true });

  const scheduled = habits.filter((h) => isHabitScheduledOn(h, today));
  const completions = await HabitCompletion.find({
    userId: req.user._id,
    date: today,
    habitId: { $in: scheduled.map((h) => h._id) },
  }).lean();

  const completedIds = new Set(completions.map((c) => c.habitId.toString()));

  const items = [];
  for (const habit of scheduled) {
    const stats = await calculateHabitStreak(habit, req.user._id, today);
    items.push({
      habit,
      completed: completedIds.has(habit._id.toString()),
      currentStreak: stats.currentStreak,
      longestStreak: stats.longestStreak,
    });
  }

  const completedCount = items.filter((i) => i.completed).length;
  const total = items.length;
  const percentage = total === 0 ? 0 : Math.round((completedCount / total) * 100);

  res.json({
    success: true,
    data: {
      date: formatDateOnly(today),
      items,
      progress: {
        completed: completedCount,
        total,
        percentage,
      },
    },
  });
});

module.exports = {
  listHabits,
  getHabit,
  createHabit,
  updateHabit,
  deleteHabit,
  completeHabit,
  uncompleteHabit,
  getHabitHistory,
  getTodayHabits,
};
