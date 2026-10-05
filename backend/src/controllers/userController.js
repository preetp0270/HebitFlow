const User = require('../models/User');
const Habit = require('../models/Habit');
const HabitCompletion = require('../models/HabitCompletion');
const Achievement = require('../models/Achievement');
const RefreshToken = require('../models/RefreshToken');
const ApiError = require('../utils/ApiError');
const asyncHandler = require('../utils/asyncHandler');

const getProfile = asyncHandler(async (req, res) => {
  res.json({ success: true, data: { user: req.user } });
});

const updateProfile = asyncHandler(async (req, res) => {
  const allowed = ['name', 'avatar', 'timezone', 'themePreference', 'startOfWeek'];
  for (const key of allowed) {
    if (req.body[key] !== undefined) {
      req.user[key] = req.body[key];
    }
  }
  await req.user.save();
  res.json({ success: true, data: { user: req.user } });
});

const changePassword = asyncHandler(async (req, res) => {
  const { currentPassword, newPassword } = req.body;
  if (!currentPassword || !newPassword) {
    throw new ApiError(400, 'Current and new password are required');
  }
  if (newPassword.length < 6) {
    throw new ApiError(400, 'New password must be at least 6 characters');
  }

  const user = await User.findById(req.user._id).select('+password');
  if (!(await user.comparePassword(currentPassword))) {
    throw new ApiError(401, 'Current password is incorrect');
  }

  user.password = newPassword;
  await user.save();

  // Invalidate other sessions
  await RefreshToken.deleteMany({ userId: user._id });

  res.json({ success: true, message: 'Password changed successfully' });
});

const deleteAccount = asyncHandler(async (req, res) => {
  const { password } = req.body;
  if (!password) {
    throw new ApiError(400, 'Password confirmation is required');
  }

  const user = await User.findById(req.user._id).select('+password');
  if (!(await user.comparePassword(password))) {
    throw new ApiError(401, 'Incorrect password');
  }

  const userId = user._id;
  await HabitCompletion.deleteMany({ userId });
  await Habit.deleteMany({ userId });
  await Achievement.deleteMany({ userId });
  await RefreshToken.deleteMany({ userId });
  await User.deleteOne({ _id: userId });

  res.json({ success: true, message: 'Account deleted permanently' });
});

const exportData = asyncHandler(async (req, res) => {
  const [habits, completions, achievements] = await Promise.all([
    Habit.find({ userId: req.user._id }).lean(),
    HabitCompletion.find({ userId: req.user._id }).lean(),
    Achievement.find({ userId: req.user._id }).lean(),
  ]);

  res.json({
    success: true,
    data: {
      exportedAt: new Date().toISOString(),
      habits,
      completions,
      achievements,
    },
  });
});

const importData = asyncHandler(async (req, res) => {
  const { habits, completions } = req.body;
  if (!Array.isArray(habits)) {
    throw new ApiError(400, 'Invalid import data: habits must be an array');
  }

  // Very conservative import: only create new habits owned by this user
  // Do not trust client-provided IDs or userIds
  const createdHabits = [];
  for (const h of habits.slice(0, 100)) {
    // hard limit
    if (!h.name || typeof h.name !== 'string') continue;
    const habit = await Habit.create({
      userId: req.user._id,
      name: String(h.name).slice(0, 100),
      description: String(h.description || '').slice(0, 500),
      icon: h.icon || '🎯',
      color: h.color || '#6366F1',
      frequencyType: ['DAILY', 'SELECTED_DAYS', 'WEEKLY_TARGET'].includes(h.frequencyType)
        ? h.frequencyType
        : 'DAILY',
      selectedDays: Array.isArray(h.selectedDays) ? h.selectedDays.filter((d) => d >= 0 && d <= 6) : [],
      targetPerWeek: Math.min(7, Math.max(1, Number(h.targetPerWeek) || 1)),
      reminderEnabled: !!h.reminderEnabled,
      reminderTime: h.reminderTime || null,
      isActive: true,
    });
    createdHabits.push(habit);
  }

  res.json({
    success: true,
    data: {
      importedHabits: createdHabits.length,
      message: 'Habits imported. Completions are not auto-imported for safety; use completions API if needed.',
    },
  });
});

module.exports = {
  getProfile,
  updateProfile,
  changePassword,
  deleteAccount,
  exportData,
  importData,
};
