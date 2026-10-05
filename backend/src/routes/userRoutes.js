const express = require('express');
const { body } = require('express-validator');
const validate = require('../middleware/validate');
const { protect } = require('../middleware/auth');
const {
  getProfile,
  updateProfile,
  changePassword,
  deleteAccount,
  exportData,
  importData,
} = require('../controllers/userController');

const router = express.Router();

router.use(protect);

router.get('/me', getProfile);
router.put('/me', updateProfile);

router.put(
  '/me/password',
  [
    body('currentPassword').notEmpty().withMessage('Current password is required'),
    body('newPassword').isLength({ min: 6 }).withMessage('New password must be at least 6 characters'),
  ],
  validate,
  changePassword
);

router.delete(
  '/me',
  [body('password').notEmpty().withMessage('Password is required for account deletion')],
  validate,
  deleteAccount
);

router.get('/me/export', exportData);
router.post('/me/import', importData);

module.exports = router;
