# HabitFlow Backend

Node.js + Express + MongoDB REST API for the HabitFlow platform.

## Quick start

```bash
cd backend
cp .env.example .env
# Edit .env with your MongoDB Atlas URI and JWT secrets
npm install
npm run dev
```

Server runs on `http://localhost:5000` by default.

## Scripts

| Command       | Description                |
|---------------|----------------------------|
| `npm start`   | Production start           |
| `npm run dev` | Development with nodemon   |
| `npm test`    | Run Jest tests             |

## Environment variables

See `.env.example`.

## API overview

- `GET /health` – health check (no auth)
- `POST /api/auth/register|login|logout|refresh|forgot-password|reset-password`
- `GET /api/auth/me`
- `GET|POST /api/habits`, `GET|PUT|DELETE /api/habits/:id`
- `POST|DELETE /api/habits/:id/complete`, `GET /api/habits/:id/history`
- `GET /api/habits/today`
- `GET /api/statistics`, `/weekly`, `/monthly`
- `GET|PUT /api/users/me`, password change, delete, export/import
- `GET /api/achievements`

All habit/user/statistics/achievement routes require `Authorization: Bearer <accessToken>`.

## Deployment (Render)

1. Create a Web Service on Render pointing at this repo, root directory `backend`.
2. Build: `npm install`
3. Start: `npm start`
4. Set environment variables in Render dashboard (`MONGODB_URI`, `JWT_SECRET`, `JWT_REFRESH_SECRET`, `CLIENT_URL`, `NODE_ENV=production`).

The server listens on `process.env.PORT`.
