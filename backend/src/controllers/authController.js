const crypto = require('crypto');
const User = require('../models/User');
const RefreshToken = require('../models/RefreshToken');
const ApiError = require('../utils/ApiError');
const asyncHandler = require('../utils/asyncHandler');
const {
  signAccessToken,
  signRefreshToken,
  verifyRefreshToken,
  getRefreshExpiryDate,
} = require('../utils/jwt');

const register = asyncHandler(async (req, res) => {
  const { name, email, password } = req.body;

  const existing = await User.findOne({ email: email.toLowerCase() });
  if (existing) {
    throw new ApiError(409, 'Email already registered');
  }

  const user = await User.create({ name, email, password });

  const accessToken = signAccessToken(user._id);
  const refreshToken = signRefreshToken(user._id);

  await RefreshToken.create({
    userId: user._id,
    token: refreshToken,
    expiresAt: getRefreshExpiryDate(),
    userAgent: req.headers['user-agent'],
  });

  res.status(201).json({
    success: true,
    data: {
      user,
      accessToken,
      refreshToken,
    },
  });
});

const login = asyncHandler(async (req, res) => {
  const { email, password } = req.body;

  const user = await User.findOne({ email: email.toLowerCase() }).select('+password');
  if (!user || !(await user.comparePassword(password))) {
    throw new ApiError(401, 'Invalid email or password');
  }

  const accessToken = signAccessToken(user._id);
  const refreshToken = signRefreshToken(user._id);

  await RefreshToken.create({
    userId: user._id,
    token: refreshToken,
    expiresAt: getRefreshExpiryDate(),
    userAgent: req.headers['user-agent'],
  });

  // Remove password from response
  user.password = undefined;

  res.json({
    success: true,
    data: {
      user,
      accessToken,
      refreshToken,
    },
  });
});

const logout = asyncHandler(async (req, res) => {
  const { refreshToken } = req.body;
  if (refreshToken) {
    await RefreshToken.deleteOne({ token: refreshToken });
  }
  res.json({ success: true, message: 'Logged out successfully' });
});

const refresh = asyncHandler(async (req, res) => {
  const { refreshToken } = req.body;
  if (!refreshToken) {
    throw new ApiError(400, 'Refresh token is required');
  }

  let decoded;
  try {
    decoded = verifyRefreshToken(refreshToken);
  } catch {
    throw new ApiError(401, 'Invalid or expired refresh token');
  }

  const stored = await RefreshToken.findOne({ token: refreshToken, userId: decoded.userId });
  if (!stored) {
    throw new ApiError(401, 'Refresh token revoked or invalid');
  }

  // Rotate: delete old, issue new
  await RefreshToken.deleteOne({ _id: stored._id });

  const user = await User.findById(decoded.userId);
  if (!user) {
    throw new ApiError(401, 'User no longer exists');
  }

  const newAccessToken = signAccessToken(user._id);
  const newRefreshToken = signRefreshToken(user._id);

  await RefreshToken.create({
    userId: user._id,
    token: newRefreshToken,
    expiresAt: getRefreshExpiryDate(),
    userAgent: req.headers['user-agent'],
  });

  res.json({
    success: true,
    data: {
      accessToken: newAccessToken,
      refreshToken: newRefreshToken,
    },
  });
});

const me = asyncHandler(async (req, res) => {
  res.json({
    success: true,
    data: { user: req.user },
  });
});

const forgotPassword = asyncHandler(async (req, res) => {
  const { email } = req.body;
  const user = await User.findOne({ email: email.toLowerCase() });

  // Always return success to avoid email enumeration
  if (!user) {
    return res.json({
      success: true,
      message: 'If that email exists, a reset link has been sent',
    });
  }

  const token = crypto.randomBytes(32).toString('hex');
  user.resetPasswordToken = crypto.createHash('sha256').update(token).digest('hex');
  user.resetPasswordExpires = Date.now() + 60 * 60 * 1000; // 1 hour
  await user.save({ validateBeforeSave: false });

  // In production you would send an email with the token.
  // For this project we return the token in non-production for testing.
  const response = {
    success: true,
    message: 'If that email exists, a reset link has been sent',
  };
  if (process.env.NODE_ENV !== 'production') {
    response.resetToken = token;
  }

  res.json(response);
});

const resetPassword = asyncHandler(async (req, res) => {
  const { token, password } = req.body;
  if (!token || !password) {
    throw new ApiError(400, 'Token and new password are required');
  }

  const hashed = crypto.createHash('sha256').update(token).digest('hex');
  const user = await User.findOne({
    resetPasswordToken: hashed,
    resetPasswordExpires: { $gt: Date.now() },
  }).select('+password');

  if (!user) {
    throw new ApiError(400, 'Invalid or expired reset token');
  }

  user.password = password;
  user.resetPasswordToken = undefined;
  user.resetPasswordExpires = undefined;
  await user.save();

  // Invalidate all refresh tokens
  await RefreshToken.deleteMany({ userId: user._id });

  res.json({
    success: true,
    message: 'Password reset successfully. Please log in.',
  });
});

module.exports = {
  register,
  login,
  logout,
  refresh,
  me,
  forgotPassword,
  resetPassword,
};
