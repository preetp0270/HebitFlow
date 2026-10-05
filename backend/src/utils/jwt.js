const jwt = require('jsonwebtoken');
const config = require('../config');

function signAccessToken(userId) {
  return jwt.sign({ userId }, config.jwtSecret, {
    expiresIn: config.jwtExpiresIn,
  });
}

function signRefreshToken(userId) {
  return jwt.sign({ userId }, config.jwtRefreshSecret, {
    expiresIn: config.jwtRefreshExpiresIn,
  });
}

function verifyAccessToken(token) {
  return jwt.verify(token, config.jwtSecret);
}

function verifyRefreshToken(token) {
  return jwt.verify(token, config.jwtRefreshSecret);
}

function getRefreshExpiryDate() {
  // Parse simple durations like 7d, 15m
  const expiresIn = config.jwtRefreshExpiresIn;
  const match = expiresIn.match(/^(\d+)([dhms])$/);
  const now = new Date();
  if (!match) {
    now.setDate(now.getDate() + 7);
    return now;
  }
  const value = parseInt(match[1], 10);
  const unit = match[2];
  switch (unit) {
    case 'd':
      now.setDate(now.getDate() + value);
      break;
    case 'h':
      now.setHours(now.getHours() + value);
      break;
    case 'm':
      now.setMinutes(now.getMinutes() + value);
      break;
    case 's':
      now.setSeconds(now.getSeconds() + value);
      break;
    default:
      now.setDate(now.getDate() + 7);
  }
  return now;
}

module.exports = {
  signAccessToken,
  signRefreshToken,
  verifyAccessToken,
  verifyRefreshToken,
  getRefreshExpiryDate,
};
