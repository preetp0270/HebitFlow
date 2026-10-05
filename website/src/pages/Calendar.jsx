import { useEffect, useState } from 'react';
import { format, startOfMonth, endOfMonth, eachDayOfInterval, isSameMonth, isSameDay, addMonths, subMonths } from 'date-fns';
import { getMonthly } from '../api/statistics';
import { getToday } from '../api/habits';

export default function CalendarPage() {
  const [cursor, setCursor] = useState(new Date());
  const [series, setSeries] = useState([]);
  const [selected, setSelected] = useState(new Date());
  const [dayDetail, setDayDetail] = useState(null);
  const [loading, setLoading] = useState(true);

  const year = cursor.getFullYear();
  const month = cursor.getMonth() + 1;

  useEffect(() => {
    setLoading(true);
    getMonthly(year, month)
      .then(({ data }) => setSeries(data.data.series || []))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [year, month]);

  useEffect(() => {
    const dateStr = format(selected, 'yyyy-MM-dd');
    getToday(dateStr)
      .then(({ data }) => setDayDetail(data.data))
      .catch(() => setDayDetail(null));
  }, [selected]);

  const days = eachDayOfInterval({
    start: startOfMonth(cursor),
    end: endOfMonth(cursor),
  });

  const byDate = Object.fromEntries(series.map((s) => [s.date, s]));

  const startPad = startOfMonth(cursor).getDay();

  return (
    <div>
      <h1 className="page-title">Calendar</h1>
      <p className="page-subtitle">Your habit history</p>

      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <button className="btn btn-ghost" onClick={() => setCursor(subMonths(cursor, 1))}>
            ←
          </button>
          <strong>{format(cursor, 'MMMM yyyy')}</strong>
          <button className="btn btn-ghost" onClick={() => setCursor(addMonths(cursor, 1))}>
            →
          </button>
        </div>

        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(7, 1fr)',
            gap: 4,
            textAlign: 'center',
            fontSize: '0.8rem',
            color: 'var(--text-muted)',
            marginBottom: 4,
          }}
        >
          {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map((d) => (
            <div key={d}>{d}</div>
          ))}
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 4 }}>
          {Array.from({ length: startPad }).map((_, i) => (
            <div key={`pad-${i}`} />
          ))}
          {days.map((day) => {
            const key = format(day, 'yyyy-MM-dd');
            const info = byDate[key];
            const pct = info?.percentage ?? null;
            const isSelected = isSameDay(day, selected);
            let bg = 'var(--bg)';
            if (pct === 100) bg = 'color-mix(in srgb, var(--success) 35%, transparent)';
            else if (pct > 0) bg = 'color-mix(in srgb, var(--primary) 25%, transparent)';
            else if (info && info.scheduled > 0) bg = 'color-mix(in srgb, var(--danger) 15%, transparent)';

            return (
              <button
                key={key}
                type="button"
                onClick={() => setSelected(day)}
                style={{
                  aspectRatio: '1',
                  borderRadius: 8,
                  border: isSelected ? '2px solid var(--primary)' : '1px solid var(--border)',
                  background: bg,
                  color: isSameMonth(day, cursor) ? 'var(--text)' : 'var(--text-muted)',
                  fontSize: '0.85rem',
                  fontWeight: isSelected ? 700 : 400,
                }}
              >
                {format(day, 'd')}
              </button>
            );
          })}
        </div>
      </div>

      <div className="card">
        <h3 style={{ marginBottom: '0.75rem' }}>{format(selected, 'EEEE, MMM d')}</h3>
        {dayDetail?.items?.length ? (
          dayDetail.items.map((item) => (
            <div key={item.habit._id} className="habit-card" style={{ marginBottom: 8 }}>
              <div className="icon">{item.habit.icon}</div>
              <div className="info">
                <div className="name">{item.habit.name}</div>
                <div className="meta">{item.completed ? '✓ Completed' : '○ Not completed'}</div>
              </div>
            </div>
          ))
        ) : (
          <p style={{ color: 'var(--text-muted)' }}>
            {loading ? 'Loading…' : 'No habits scheduled this day'}
          </p>
        )}
      </div>
    </div>
  );
}
