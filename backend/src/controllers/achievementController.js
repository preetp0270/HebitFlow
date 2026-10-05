const asyncHandler = require('../utils/asyncHandler');
const { getUserAchievements, evaluateAchievements } = require('../services/achievementService');

const listAchievements = asyncHandler(async (req, res) => {
  // Re-evaluate on fetch so newly met conditions appear
  await evaluateAchievements(req.user._id);
  const achievements = await getUserAchievements(req.user._id);
  res.json({ success: true, data: { achievements } });
});

module.exports = {
  listAchievements,
};
