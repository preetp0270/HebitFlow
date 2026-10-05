const mongoose = require('mongoose');

const habitCompletionSchema = new mongoose.Schema(
  {
    habitId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Habit',
      required: true,
      index: true,
    },
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true,
      index: true,
    },
    // Date only (YYYY-MM-DD) stored as Date at midnight UTC for indexing
    date: {
      type: Date,
      required: true,
    },
    completedAt: {
      type: Date,
      default: Date.now,
    },
  },
  {
    timestamps: true,
  }
);

// Prevent duplicate completions for same habit + date
habitCompletionSchema.index(
  { habitId: 1, date: 1 },
  { unique: true }
);

habitCompletionSchema.index({ userId: 1, date: 1 });

module.exports = mongoose.model('HabitCompletion', habitCompletionSchema);
