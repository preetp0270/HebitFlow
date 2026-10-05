/**
 * Normalize a date to UTC midnight for consistent date-only storage.
 * Accepts Date or YYYY-MM-DD string.
 */
function toDateOnly(input) {
  if (!input) {
    const d = new Date();
    return new Date(Date.UTC(d.getUTCFullYear(), d.getUTCMonth(), d.getUTCDate()));
  }
  if (typeof input === 'string') {
    const [y, m, day] = input.split('-').map(Number);
    return new Date(Date.UTC(y, m - 1, day));
  }
  const d = new Date(input);
  return new Date(Date.UTC(d.getUTCFullYear(), d.getUTCMonth(), d.getUTCDate()));
}

/**
 * Format Date to YYYY-MM-DD
 */
function formatDateOnly(date) {
  const d = toDateOnly(date);
  const y = d.getUTCFullYear();
  const m = String(d.getUTCMonth() + 1).padStart(2, '0');
  const day = String(d.getUTCDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

/**
 * Get day of week 0=Sun ... 6=Sat in UTC
 */
function getUTCDay(date) {
  return toDateOnly(date).getUTCDay();
}

/**
 * Subtract days from a date-only value
 */
function addDays(date, days) {
  const d = toDateOnly(date);
  d.setUTCDate(d.getUTCDate() + days);
  return d;
}

/**
 * Check if a habit is scheduled on a given date
 */
function isHabitScheduledOn(habit, date) {
  const d = toDateOnly(date);
  const start = toDateOnly(habit.startDate);
  if (d < start) return false;

  const dayOfWeek = d.getUTCDay();

  switch (habit.frequencyType) {
    case 'DAILY':
      return true;
    case 'SELECTED_DAYS':
      return Array.isArray(habit.selectedDays) && habit.selectedDays.includes(dayOfWeek);
    case 'WEEKLY_TARGET':
      // Weekly target: any day counts toward the week target
      return true;
    default:
      return false;
  }
}

module.exports = {
  toDateOnly,
  formatDateOnly,
  getUTCDay,
  addDays,
  isHabitScheduledOn,
};
