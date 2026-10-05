const { verifyAccessToken } = require('../utils/jwt');
const ApiError = require('../utils/ApiError');
const User = require('../models/User');

/**
 * Protect routes – requires valid Bearer access token.
 * Attaches req.user (full user doc without password).
 */
const protect = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      throw new ApiError(401, 'Not authorized. Please log in.');
    }

    const token = authHeader.split(' ')[1];
    if (!token) {
      throw new ApiError(401, 'Not authorized. Please log in.');
    }

    let decoded;
    try {
      decoded = verifyAccessToken(token);
    } catch (err) {
      if (err.name === 'TokenExpiredError') {
        throw new ApiError(401, 'Access token expired. Please refresh.');
      }
      throw new ApiError(401, 'Invalid access token.');
    }

    const user = await User.findById(decoded.userId);
    if (!user) {
      throw new ApiError(401, 'User no longer exists.');
    }

    req.user = user;
    next();
  } catch (error) {
    next(error);
  }
};

module.exports = { protect };
