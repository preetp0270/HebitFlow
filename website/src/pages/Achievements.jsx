import { useEffect, useState } from 'react';
import { getAchievements } from '../api/user';

export default function Achievements() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getAchievements()
      .then(({ data }) => setList(data.data.achievements || []))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p>Loading…</p>;

  return (
    <div>
      <h1 className="page-title">Achievements</h1>
      <p className="page-subtitle">Milestones you’ve unlocked</p>

      {!list.length ? (
        <div className="empty-state card">
          <h3>No achievements yet</h3>
          <p>Create habits and keep streaks to unlock awards.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: '0.75rem' }}>
          {list.map((a) => (
            <div key={a._id} className="card" style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
              <div style={{ fontSize: '2rem' }}>🏆</div>
              <div>
                <strong>{a.title}</strong>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>{a.description}</p>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>
                  Unlocked {new Date(a.unlockedAt).toLocaleDateString()}
                </p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
