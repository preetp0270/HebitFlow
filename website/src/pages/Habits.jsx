import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getHabits, deleteHabit } from '../api/habits';

export default function Habits() {
  const [habits, setHabits] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    try {
      const { data } = await getHabits();
      setHabits(data.data.habits);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Delete "${name}"? This cannot be undone easily.`)) return;
    try {
      await deleteHabit(id);
      setHabits((h) => h.filter((x) => x._id !== id));
    } catch (err) {
      alert(err.response?.data?.message || 'Delete failed');
    }
  };

  if (loading) return <p>Loading…</p>;

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
        <div>
          <h1 className="page-title">Habits</h1>
          <p className="page-subtitle">Manage all your habits</p>
        </div>
        <Link to="/habits/new" className="btn btn-primary">
          + New habit
        </Link>
      </div>

      {!habits.length ? (
        <div className="empty-state card">
          <h3>No habits yet</h3>
          <p>Start with one small habit and build from there.</p>
          <Link to="/habits/new" className="btn btn-primary" style={{ marginTop: '1rem' }}>
            Create habit
          </Link>
        </div>
      ) : (
        habits.map((h) => (
          <div key={h._id} className="habit-card">
            <div className="icon" style={{ background: `${h.color}22` }}>
              {h.icon || '🎯'}
            </div>
            <div className="info">
              <Link to={`/habits/${h._id}`} className="name" style={{ color: 'inherit' }}>
                {h.name}
              </Link>
              <div className="meta">
                {h.frequencyType.replace('_', ' ')}
                {h.description ? ` · ${h.description.slice(0, 40)}` : ''}
              </div>
            </div>
            <Link to={`/habits/${h._id}/edit`} className="btn btn-ghost" style={{ padding: '0.4rem 0.7rem' }}>
              Edit
            </Link>
            <button
              className="btn btn-ghost"
              style={{ padding: '0.4rem 0.7rem', color: 'var(--danger)' }}
              onClick={() => handleDelete(h._id, h.name)}
            >
              Delete
            </button>
          </div>
        ))
      )}
    </div>
  );
}
