# HabitFlow

Full-stack habit tracker: **native Android**, **React website**, and **Node/Express + MongoDB** backend — one repository, shared API and data.

```text
HabitFlow/
├── android/          # Kotlin · Jetpack Compose · Room · offline-first
├── website/          # React · Vite · React Router
├── backend/          # Express · Mongoose · JWT
├── .github/workflows/deploy.yml
├── .gitignore
└── README.md
```

## Architecture

```text
Android ──┐
          ├──► Render (Express API) ──► MongoDB Atlas
Website ──┘
```

- **Source of truth** for online data: MongoDB via the backend.
- **Android** caches in Room and syncs when online (complete offline → push on reconnect).
- **Website** talks to the same REST API with JWT + refresh tokens.

## Features

| Area | Capabilities |
|------|----------------|
| Auth | Register, login, logout, refresh, forgot/reset password |
| Habits | CRUD, DAILY / SELECTED_DAYS / WEEKLY_TARGET, icons & colors |
| Completions | Per-date records, unique index, undo |
| Streaks | Backend streak service (scheduled days, not naive calendar) |
| Stats | Overall, weekly, monthly series |
| Achievements | First habit, streaks, completion milestones |
| Android | Compose UI, Today screen, Room, offline complete + sync, local reminders |
| Web | Landing, dashboard, habits, calendar, stats, profile, settings, export |
| Ops | Health check, rate limit, helmet, CORS, GitHub Actions, Render-ready |

## Local setup

### 1. MongoDB

Use [MongoDB Atlas](https://www.mongodb.com/atlas) (or local MongoDB). Copy the connection string.

### 2. Backend

```bash
cd backend
cp .env.example .env
# Set MONGODB_URI, JWT_SECRET, JWT_REFRESH_SECRET, CLIENT_URL
npm install
npm run dev
```

API: `http://localhost:5000`  
Health: `GET /health` → `{ "status": "ok" }`

```bash
npm test
```

### 3. Website

```bash
cd website
cp .env.example .env
# VITE_API_URL=http://localhost:5000
npm install
npm run dev
```

Open `http://localhost:5173`.

### 4. Android

1. Open the `android/` folder in **Android Studio** (Giraffe+).
2. Let Gradle sync (generates the Gradle wrapper if missing).
3. Emulator uses `http://10.0.2.2:5000/` as API base (see `BuildConfig.API_BASE_URL`).
4. Run the `app` configuration.

For a physical device, set the API URL to your machine’s LAN IP in `app/build.gradle.kts`.

## Environment variables

**Backend** (`backend/.env`):

| Variable | Description |
|----------|-------------|
| `PORT` | Default `5000` |
| `MONGODB_URI` | Atlas / local URI |
| `JWT_SECRET` | Access token secret |
| `JWT_REFRESH_SECRET` | Refresh token secret |
| `CLIENT_URL` | Website origin for CORS |
| `NODE_ENV` | `development` / `production` |

**Website** (`website/.env`):

| Variable | Description |
|----------|-------------|
| `VITE_API_URL` | Backend base URL (no trailing slash) |

Never commit `.env`, keystores, or API keys.

## API (summary)

All habit/user/stats routes need `Authorization: Bearer <accessToken>`.

| Method | Path | Notes |
|--------|------|--------|
| POST | `/api/auth/register` | `{ name, email, password }` |
| POST | `/api/auth/login` | `{ email, password }` |
| POST | `/api/auth/refresh` | `{ refreshToken }` |
| GET | `/api/auth/me` | Current user |
| GET | `/api/habits/today` | Today’s scheduled habits + progress |
| GET/POST | `/api/habits` | List / create |
| GET/PUT/DELETE | `/api/habits/:id` | Detail / update / soft-delete |
| POST/DELETE | `/api/habits/:id/complete` | Complete / undo (body optional `{ date }`) |
| GET | `/api/statistics` | Aggregates |
| GET | `/api/statistics/weekly` | Last 7 days |
| GET | `/api/statistics/monthly` | `?year=&month=` |
| GET | `/api/achievements` | Unlocked achievements |
| GET/PUT | `/api/users/me` | Profile |
| GET | `/api/users/me/export` | JSON export |
| GET | `/health` | No auth |

Success shape: `{ "success": true, "data": { ... } }`  
Error shape: `{ "success": false, "message": "..." }`

## Deployment

### Backend → Render

1. New **Web Service**, connect this repo, root directory `backend`.
2. Build: `npm install` · Start: `npm start`.
3. Set env vars in Render (same as `.env.example`, `NODE_ENV=production`).
4. Optional: Deploy Hook URL → GitHub secret `RENDER_DEPLOY_HOOK` so Actions can trigger deploys.

### Website

Build with production API URL:

```bash
VITE_API_URL=https://your-service.onrender.com npm run build
```

Host `website/dist` on Netlify, Vercel, Cloudflare Pages, or any static host. Set CORS `CLIENT_URL` on the backend to that origin.

### Android

- CI builds a **debug APK** when `gradlew` is present (open once in Android Studio to generate the wrapper).
- For Play Store: add release signing via GitHub Secrets; never commit keystores.

### GitHub Actions

`.github/workflows/deploy.yml`:

1. Backend install + Jest tests  
2. Website production build (artifact)  
3. Android APK attempt (artifact)  
4. Optional Render deploy hook on `main`

## Security notes

- Passwords hashed with **bcryptjs**
- JWT access + rotating refresh tokens stored server-side
- All habit/completion queries scoped to `req.user._id` from the token (never trust client `userId`)
- Rate limiting, Helmet, validation, no stack traces in production responses

## Testing

```bash
cd backend && npm test
```

Covers registration, login, habits, completions, duplicate prevention, health.

Android: `StreakLogicTest` sample unit test under `android/app/src/test`.

## Troubleshooting

| Issue | Fix |
|-------|-----|
| CORS errors | Set `CLIENT_URL` to the website origin |
| Android can’t reach API | Emulator → `10.0.2.2`; device → LAN IP; cleartext allowed for those hosts |
| Mongo connection fail | Check Atlas IP allowlist (`0.0.0.0/0` for Render) and URI |
| Token expired | Client refreshes via `/api/auth/refresh`; re-login if refresh revoked |

## Future improvements

- Full Compose calendar/stats parity with web  
- WorkManager periodic sync  
- Email delivery for password reset  
- WebSocket live updates  
- Play App Signing + AAB release pipeline  

## License

MIT — use and extend freely.
