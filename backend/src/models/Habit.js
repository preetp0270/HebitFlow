const mongoose = require('mongoose');

const FREQUENCY_TYPES = ['DAILY', 'SELECTED_DAYS', 'WEEKLY_TARGET'];

const habitSchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true,
      index: true,
    },
    name: {
      type: String,
      required: [true, 'Habit name is required'],
      trim: true,
      maxlength: [100, 'Name cannot exceed 100 characters'],
    },
    description: {
      type: String,
      trim: true,
      maxlength: [500, 'Description cannot exceed 500 characters'],
      default: '',
    },
    icon: {
      type: String,
      default: '🎯',
    },
    color: {
      type: String,
      default: '#6366F1',
    },
    frequencyType: {
      type: String,
      enum: FREQUENCY_TYPES,
      required: true,
      default: 'DAILY',
    },
    // For SELECTED_DAYS: array of 0-6 (Sun-Sat)
    selectedDays: {
      type: [Number],
      default: [],
      validate: {
        validator(v) {
          return v.every((d) => d >= 0 && d <= 6);
        },
        message: 'selectedDays must contain values 0-6',
      },
    },
    // For WEEKLY_TARGET
    targetPerWeek: {
      type: Number,
      min: 1,
      max: 7,
      default: 1,
    },
    reminderEnabled: {
      type: Boolean,
      default: false,
    },
    reminderTime: {
      type: String, // "HH:mm"
      default: null,
    },
    startDate: {
      type: Date,
      default: () => {
        const d = new Date();
        d.setHours(0, 0, 0, 0);
        return d;
      },
    },
    isActive: {
      type: Boolean,
      default: true,
    },
  },
  {
    timestamps: true,
    toJSON: {
      transform(doc, ret) {
        delete ret.__v;
        return ret;
      },
    },
  }
);

habitSchema.index({ userId: 1, isActive: 1 });

module.exports = mongoose.model('Habit', habitSchema);
module.exports.FREQUENCY_TYPES = FREQUENCY_TYPES;
