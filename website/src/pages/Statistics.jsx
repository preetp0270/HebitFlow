import { useEffect, useState } from 'react';
import { getStatistics, getWeekly } from '../api/statistics';

export default function Statistics() {
  const [stats, setStats] = useState(null);
  const [weekly, setWeekly] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getStatistics(), getWeekly()])
      .then(([s, w]) => {
        setStats(s.data.data);
        setWeekly(w.data.data.series || []);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p>Loading…</p>;
  if (!stats) return <p>Failed to load statistics</p>;

  const maxPct = Math.max(...weekly.map((d) => d.percentage), 1);

  return (
    <div>
      <h1 className="page-title">Statistics</h1>
      <p className="page-subtitle">Your consistency at a glance</p>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="value">{stats.overallCompletionPercentage}%</div>
          <div className="label">Overall completion</div>
        </div>
        <div className="stat-card">
          <div className="value">{stats.totalHabits}</div>
          <div className="label">Active habits</div>
        </div>
        <div className="stat-card">
          <div className="value">{stats.totalCompletions}</div>
          <div className="label">Total completions</div>
        </div>
        <div className="stat-card">
          <div className="value">{stats.currentStreak}</div>
          <div className="label">Current streak</div>
        </div>
        <div className="stat-card">
          <div className="value">{stats.bestStreak}</div>
          <div className="label">Best streak</div>
        </div>
      </div>

      {(stats.mostConsistent || stats.leastConsistent) && (
        <div className="card" style={{ marginBottom: '1.5rem' }}>
          {stats.mostConsistent && (
            <p style={{ marginBottom: '0.5rem' }}>
              <strong>Most consistent:</strong> {stats.mostConsistent.name} ({stats.mostConsistent.percentage}%)
            </p>
          )}
          {stats.leastConsistent && (
            <p>
              <strong>Needs attention:</strong> {stats.leastConsistent.name} ({stats.leastConsistent.percentage}%)
            </p>
          )}
        </div>
      )}

      <div className="card">
        <h3 style={{ marginBottom: '1rem' }}>Last 7 days</h3>
        <div style={{ display: 'flex', alignItems: 'flex-end', gap: 8, height: 140 }}>
          {weekly.map((d) => (
            <div key={d.date} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 }}>
              <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>{d.percentage}%</span>
              <div
                style={{
                  width: '100%',
                  height: `${Math.max(4, (d.percentage / maxPct) * 100)}px`,
                  background: d.percentage === 100 ? 'var(--success)' : 'var(--primary)',
                  borderRadius: '4px 4px 0 0',
                  minHeight: 4,
                }}
              />
              <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                {d.date.slice(5)}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
