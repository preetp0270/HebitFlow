const express = require('express');
const { protect } = require('../middleware/auth');
const { listAchievements } = require('../controllers/achievementController');

const router = express.Router();

router.use(protect);
router.get('/', listAchievements);

module.exports = router;
