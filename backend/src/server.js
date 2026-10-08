const dns = require('dns');
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const rateLimit = require('express-rate-limit');
const config = require('./config');
const connectDB = require('./config/db');
const errorHandler = require('./middleware/errorHandler');

const authRoutes = require('./routes/authRoutes');
const habitRoutes = require('./routes/habitRoutes');
const statisticsRoutes = require('./routes/statisticsRoutes');
const userRoutes = require('./routes/userRoutes');
const achievementRoutes = require('./routes/achievementRoutes');

try {
  dns.setServers(['8.8.8.8', '8.8.4.4']);
} catch {
  // ignore
}

const app = express();

app.use(
  helmet({
    crossOriginResourcePolicy: { policy: 'cross-origin' },
    crossOriginOpenerPolicy: { policy: 'same-origin-allow-popups' },
  })
);

const extraOrigins = (config.clientUrl || '')
  .split(',')
  .map((s) => s.trim().replace(/\/$/, ''))
  .filter((s) => s && s !== '*');

const staticAllowed = new Set([
  'https://hebit-flow.vercel.app',
  'http://localhost:5173',
  'http://localhost:3000',
  'http://127.0.0.1:5173',
  ...extraOrigins,
]);

function isOriginAllowed(origin) {
  if (!origin) return true;
  if (config.clientUrl === '*') return true;
  if (staticAllowed.has(origin)) return true;
  if (/^https:\/\/[a-z0-9-]+([.-][a-z0-9-]+)*\.vercel\.app$/i.test(origin)) return true;
  return false;
}

app.use(
  cors({
    origin(origin, callback) {
      if (isOriginAllowed(origin)) return callback(null, true);
      console.warn(`[CORS] Blocked origin: ${origin}`);
      return callback(null, false);
    },
    credentials: true,
    methods: ['GET', 'HEAD', 'PUT', 'PATCH', 'POST', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization'],
  })
);

app.use(express.json({ limit: '1mb' }));
app.use(express.urlencoded({ extended: true }));

if (config.nodeEnv !== 'test') {
  app.use(morgan(config.nodeEnv === 'production' ? 'combined' : 'dev'));
}

const limiter = rateLimit({
  windowMs: config.rateLimitWindowMs,
  max: config.rateLimitMax,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    success: false,
    message: 'Too many requests (rate limit). Wait a few minutes and try again.',
  },
});
// Generous limit on /api; health is outside this path
app.use('/api/', limiter);

app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// Lightweight keep-alive endpoint (used by self-ping on free hosts like Render)
app.get('/api/ping', (req, res) => {
  res.json({ success: true, pong: true, timestamp: new Date().toISOString() });
});

app.use('/api/auth', authRoutes);
app.use('/api/habits', habitRoutes);
app.use('/api/statistics', statisticsRoutes);
app.use('/api/users', userRoutes);
app.use('/api/achievements', achievementRoutes);

app.use((req, res) => {
  res.status(404).json({ success: false, message: 'Route not found' });
});

app.use(errorHandler);

if (require.main === module) {
  connectDB().then(() => {
    app.listen(config.port, '0.0.0.0', () => {
      console.log(`[Server] HabitFlow API running on port ${config.port} (${config.nodeEnv})`);
      console.log(`[CORS] CLIENT_URL=${config.clientUrl}`);

      // Keep-alive for free hosts (e.g. Render) that spin down after idle time.
      // Self-ping every 9 minutes so the service stays warm and avoids ~50s cold starts.
      const baseUrl =
        process.env.RENDER_EXTERNAL_URL ||
        process.env.APP_BASE_URL ||
        process.env.BACKEND_URL;
      if (!baseUrl) {
        console.warn(
          '[Keep-alive] Skipped: set RENDER_EXTERNAL_URL or APP_BASE_URL to your public API URL'
        );
        return;
      }

      const pingUrl = `${baseUrl.replace(/\/$/, '')}/api/ping`;
      const intervalMs = 9 * 60 * 1000; // 9 minutes (under typical 15-min free idle limit)

      setInterval(async () => {
        try {
          const res = await fetch(pingUrl);
          console.log(
            `[Keep-alive] ping ${res.status} at ${new Date().toLocaleTimeString()}`
          );
        } catch (e) {
          console.warn(`[Keep-alive] failed: ${e.message}`);
        }
      }, intervalMs);

      console.log(`[Keep-alive] Self-ping every 9 min → ${pingUrl}`);
    });
  });
}

module.exports = app;
