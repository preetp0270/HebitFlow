# HabitFlow API Documentation

Base URL (dev): `http://localhost:5000`

All authenticated routes require header:

```http
Authorization: Bearer <accessToken>
```

## Health

### GET /health

No auth.

```json
{ "status": "ok", "timestamp": "..." }
```

---

## Auth

### POST /api/auth/register

```json
{ "name": "Ada", "email": "ada@example.com", "password": "secret12" }
```

**201** → `{ success, data: { user, accessToken, refreshToken } }`

### POST /api/auth/login

```json
{ "email": "ada@example.com", "password": "secret12" }
```

### POST /api/auth/logout

```json
{ "refreshToken": "..." }
```

### POST /api/auth/refresh

```json
{ "refreshToken": "..." }
```

**200** → new `accessToken` + `refreshToken` (rotation)

### GET /api/auth/me

Returns current user.

### POST /api/auth/forgot-password

```json
{ "email": "ada@example.com" }
```

In non-production, response may include `resetToken` for testing.

### POST /api/auth/reset-password

```json
{ "token": "...", "password": "newsecret" }
```

---

## Habits

### GET /api/habits/today

Optional `?date=YYYY-MM-DD`

Returns scheduled habits for the day, completion flags, streaks, progress.

### GET /api/habits

List active habits for the user.

### POST /api/habits

```json
{
  "name": "Drink Water",
  "description": "2L",
  "icon": "💧",
  "color": "#06B6D4",
  "frequencyType": "DAILY",
  "selectedDays": [],
  "targetPerWeek": 1,
  "reminderEnabled": true,
  "reminderTime": "10:00"
}
```

`frequencyType`: `DAILY` | `SELECTED_DAYS` | `WEEKLY_TARGET`  
`selectedDays`: 0=Sunday … 6=Saturday

### GET /api/habits/:id

Habit + streak stats.

### PUT /api/habits/:id

Partial update of allowed fields.

### DELETE /api/habits/:id

Soft-delete (`isActive: false`).

### POST /api/habits/:id/complete

Optional body `{ "date": "YYYY-MM-DD" }` (defaults to today UTC date).  
**409** if already completed that day.

### DELETE /api/habits/:id/complete

Undo completion for date.

### GET /api/habits/:id/history

Optional `?from=&to=`

---

## Statistics

### GET /api/statistics

Aggregates: total habits, completions, streaks, most/least consistent.

### GET /api/statistics/weekly

Last 7 days series: `{ date, scheduled, completed, percentage }`.

### GET /api/statistics/monthly

Query: `year`, `month` (1–12).

---

## Users

### GET /api/users/me  
### PUT /api/users/me

Body may include `name`, `timezone`, `themePreference`, `startOfWeek`, `avatar`.

### PUT /api/users/me/password

```json
{ "currentPassword": "...", "newPassword": "..." }
```

### DELETE /api/users/me

```json
{ "password": "..." }
```

Permanently deletes user data.

### GET /api/users/me/export  
### POST /api/users/me/import

Import accepts `{ habits: [...] }` (validated, limited).

---

## Achievements

### GET /api/achievements

List unlocked achievements; server re-evaluates conditions on read.
