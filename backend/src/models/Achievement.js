const mongoose = require('mongoose');

const ACHIEVEMENT_TYPES = [
  'FIRST_HABIT',
  'STREAK_7',
  'STREAK_30',
  'STREAK_100',
  'COMPLETIONS_100',
  'COMPLETIONS_500',
  'PERFECT_WEEK',
  'PERFECT_MONTH',
];

const achievementSchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true,
      index: true,
    },
    type: {
      type: String,
      enum: ACHIEVEMENT_TYPES,
      required: true,
    },
    title: {
      type: String,
      required: true,
    },
    description: {
      type: String,
      required: true,
    },
    unlockedAt: {
      type: Date,
      default: Date.now,
    },
    // Optional reference to habit that triggered it
    habitId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Habit',
      default: null,
    },
  },
  {
    timestamps: true,
  }
);

// One of each type per user
achievementSchema.index({ userId: 1, type: 1 }, { unique: true });

module.exports = mongoose.model('Achievement', achievementSchema);
module.exports.ACHIEVEMENT_TYPES = ACHIEVEMENT_TYPES;
