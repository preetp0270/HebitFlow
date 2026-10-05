import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getHabit, getHabitHistory } from '../api/habits';

export default function HabitDetails() {
  const { id } = useParams();
  const [habit, setHabit] = useState(null);
  const [stats, setStats] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getHabit(id), getHabitHistory(id)])
      .then(([h, hist]) => {
        setHabit(h.data.data.habit);
        setStats(h.data.data.stats);
        setHistory(hist.data.data.completions || []);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <p>Loading…</p>;
  if (!habit) return <p>Habit not found</p>;

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <h1 className="page-title">
            <span style={{ marginRight: 8 }}>{habit.icon}</span>
            {habit.name}
          </h1>
          <p className="page-subtitle">{habit.description || habit.frequencyType}</p>
        </div>
        <Link to={`/habits/${id}/edit`} className="btn btn-ghost">
          Edit
        </Link>
      </div>

      {stats && (
        <div className="stats-grid">
          <div className="stat-card">
            <div className="value">{stats.currentStreak}</div>
            <div className="label">Current streak</div>
          </div>
          <div className="stat-card">
            <div className="value">{stats.longestStreak}</div>
            <div className="label">Longest streak</div>
          </div>
          <div className="stat-card">
            <div className="value">{stats.totalCompleted}</div>
            <div className="label">Total completions</div>
          </div>
          <div className="stat-card">
            <div className="value">{stats.completionPercentage}%</div>
            <div className="label">Completion rate</div>
          </div>
        </div>
      )}

      <div className="card">
        <h3 style={{ marginBottom: '0.75rem' }}>Recent history</h3>
        {!history.length ? (
          <p style={{ color: 'var(--text-muted)' }}>No completions yet</p>
        ) : (
          <ul style={{ listStyle: 'none' }}>
            {history.slice(0, 30).map((c) => (
              <li
                key={c._id}
                style={{
                  padding: '0.5rem 0',
                  borderBottom: '1px solid var(--border)',
                  display: 'flex',
                  justifyContent: 'space-between',
                }}
              >
                <span>✓ {c.date}</span>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                  {c.completedAt ? new Date(c.completedAt).toLocaleTimeString() : ''}
                </span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
