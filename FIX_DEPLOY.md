# Deploy checklist (from last 3 chats)

## Render (backend)
1. Push updated `backend/src/server.js` (CORS allows https://hebit-flow.vercel.app)
2. Env:
   - CLIENT_URL=https://hebit-flow.vercel.app
   - NODE_ENV=production
   - MONGODB_URI, JWT_SECRET, JWT_REFRESH_SECRET
3. Redeploy
4. Verify:
   curl -sD - -o /dev/null -X POST https://hebitflow.onrender.com/api/auth/login \
     -H "Origin: https://hebit-flow.vercel.app" \
     -H "Content-Type: application/json" \
     -d '{"email":"x@y.com","password":"x"}' | grep -i access-control-allow-origin

## Vercel (website)
VITE_API_URL=https://hebitflow.onrender.com
Redeploy after env change.

## Android
API_BASE_URL = https://hebitflow.onrender.com/
Design: HabitForge Kinetic Minimal (Today / Calendar / Stats / Settings)
Edit/Delete: Settings → Manage habits
