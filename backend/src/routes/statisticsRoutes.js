const express = require('express');
const { protect } = require('../middleware/auth');
const {
  getStatistics,
  getWeekly,
  getMonthly,
} = require('../controllers/statisticsController');

const router = express.Router();

router.use(protect);

router.get('/', getStatistics);
router.get('/weekly', getWeekly);
router.get('/monthly', getMonthly);

module.exports = router;
