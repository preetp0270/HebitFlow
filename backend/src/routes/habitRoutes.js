const express = require('express');
const { body, param } = require('express-validator');
const validate = require('../middleware/validate');
const { protect } = require('../middleware/auth');
const {
  listHabits,
  getHabit,
  createHabit,
  updateHabit,
  deleteHabit,
  completeHabit,
  uncompleteHabit,
  getHabitHistory,
  getTodayHabits,
} = require('../controllers/habitController');

const router = express.Router();

router.use(protect);

router.get('/today', getTodayHabits);
router.get('/', listHabits);

router.post(
  '/',
  [
    body('name').trim().notEmpty().withMessage('Name is required').isLength({ max: 100 }),
    body('frequencyType')
      .optional()
      .isIn(['DAILY', 'SELECTED_DAYS', 'WEEKLY_TARGET'])
      .withMessage('Invalid frequency type'),
  ],
  validate,
  createHabit
);

router.get('/:id', [param('id').isMongoId()], validate, getHabit);
router.put('/:id', [param('id').isMongoId()], validate, updateHabit);
router.delete('/:id', [param('id').isMongoId()], validate, deleteHabit);

router.post('/:id/complete', [param('id').isMongoId()], validate, completeHabit);
router.delete('/:id/complete', [param('id').isMongoId()], validate, uncompleteHabit);
router.get('/:id/history', [param('id').isMongoId()], validate, getHabitHistory);

module.exports = router;
