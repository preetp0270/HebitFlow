import { Link } from 'react-router-dom';

export default function Landing() {
  return (
    <div className="landing">
      <nav className="landing-nav">
        <div className="logo" style={{ fontWeight: 700, fontSize: '1.3rem', color: 'var(--primary)' }}>
          HabitFlow
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <Link to="/login" className="btn btn-ghost">
            Log in
          </Link>
          <Link to="/register" className="btn btn-primary">
            Get started
          </Link>
        </div>
      </nav>

      <section className="landing-hero">
        <h1>Build better habits, one day at a time</h1>
        <p>
          Track daily routines, maintain streaks, and see your progress across web and Android.
          Offline-first on mobile, always in sync.
        </p>
        <div className="landing-actions">
          <Link to="/register" className="btn btn-primary" style={{ padding: '0.85rem 1.6rem' }}>
            Start free
          </Link>
          <Link to="/login" className="btn btn-ghost" style={{ padding: '0.85rem 1.6rem' }}>
            I already have an account
          </Link>
        </div>
      </section>

      <section
        style={{
          maxWidth: 900,
          margin: '0 auto',
          padding: '2rem 1.5rem 4rem',
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
          gap: '1.25rem',
        }}
      >
        {[
          { icon: '🔥', title: 'Streaks that matter', desc: 'Scheduled-day streaks for daily, weekly, and selected-day habits.' },
          { icon: '📱', title: 'Web + Android', desc: 'Same account, same data. Complete a habit on phone, see it on desktop.' },
          { icon: '📊', title: 'Clear statistics', desc: 'Weekly and monthly progress without overwhelming charts.' },
          { icon: '🏆', title: 'Achievements', desc: 'Unlock milestones as you build consistency.' },
        ].map((f) => (
          <div key={f.title} className="card">
            <div style={{ fontSize: '1.75rem', marginBottom: '0.5rem' }}>{f.icon}</div>
            <h3 style={{ marginBottom: '0.35rem' }}>{f.title}</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>{f.desc}</p>
          </div>
        ))}
      </section>
    </div>
  );
}
