import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { format } from 'date-fns';
import { useAuth } from '../context/AuthContext';
import { getToday, completeHabit, uncompleteHabit } from '../api/habits';
import { getStatistics } from '../api/statistics';

function greeting() {
  const h = new Date().getHours();
  if (h < 12) return 'Good morning';
  if (h < 17) return 'Good afternoon';
  return 'Good evening';
}

export default function Dashboard() {
  const { user } = useAuth();
  const [today, setToday] = useState(null);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [toggling, setToggling] = useState(null);

  const load = async () => {
    try {
      const [t, s] = await Promise.all([getToday(), getStatistics()]);
      setToday(t.data.data);
      setStats(s.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const toggle = async (item) => {
    const id = item.habit._id;
    setToggling(id);
    try {
      if (item.completed) {
        await uncompleteHabit(id);
      } else {
        await completeHabit(id);
      }
      await load();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update');
    } finally {
      setToggling(null);
    }
  };

  if (loading) return <p>Loading…</p>;

  const progress = today?.progress || { completed: 0, total: 0, percentage: 0 };

  return (
    <div>
      <h1 className="page-title">
        {greeting()}, {user?.name?.split(' ')[0] || 'there'} 👋
      </h1>
      <p className="page-subtitle">{format(new Date(), 'EEEE, MMMM d')}</p>

      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <strong>Today&apos;s Progress</strong>
          <span style={{ color: 'var(--primary)', fontWeight: 700 }}>{progress.percentage}%</span>
        </div>
        <div className="progress-bar">
          <div style={{ width: `${progress.percentage}%` }} />
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
          <span>
            {progress.completed} / {progress.total} completed
          </span>
          {stats && <span>🔥 Current streak: {stats.currentStreak} days</span>}
        </div>
      </div>

      {stats && (
        <div className="stats-grid">
          <div className="stat-card">
            <div className="value">{stats.currentStreak}</div>
            <div className="label">Current streak</div>
          </div>
          <div className="stat-card">
            <div className="value">{stats.bestStreak}</div>
            <div className="label">Best streak</div>
          </div>
          <div className="stat-card">
            <div className="value">{stats.totalCompletions}</div>
            <div className="label">Total completions</div>
          </div>
          <div className="stat-card">
            <div className="value">{stats.overallCompletionPercentage}%</div>
            <div className="label">Overall</div>
          </div>
        </div>
      )}

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
        <h2 style={{ fontSize: '1.15rem' }}>Today&apos;s Habits</h2>
        <Link to="/habits/new" className="btn btn-primary">
          + Add
        </Link>
      </div>

      {!today?.items?.length ? (
        <div className="empty-state card">
          <h3>No habits for today</h3>
          <p>Start with one small habit and build from there.</p>
          <Link to="/habits/new" className="btn btn-primary" style={{ marginTop: '1rem' }}>
            Create habit
          </Link>
        </div>
      ) : (
        today.items.map((item) => (
          <div key={item.habit._id} className="habit-card">
            <div className="icon" style={{ background: `${item.habit.color}22` }}>
              {item.habit.icon || '🎯'}
            </div>
            <div className="info">
              <div className="name">{item.habit.name}</div>
              <div className="meta">🔥 {item.currentStreak} day streak</div>
            </div>
            <button
              className={`habit-check ${item.completed ? 'done' : ''}`}
              disabled={toggling === item.habit._id}
              onClick={() => toggle(item)}
              aria-label={item.completed ? 'Mark incomplete' : 'Mark complete'}
            >
              {item.completed ? '✓' : ''}
            </button>
          </div>
        ))
      )}
    </div>
  );
}
