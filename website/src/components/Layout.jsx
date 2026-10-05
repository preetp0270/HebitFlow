import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const links = [
  { to: '/dashboard', label: 'Today', icon: '☀️' },
  { to: '/habits', label: 'Habits', icon: '📋' },
  { to: '/calendar', label: 'Calendar', icon: '📅' },
  { to: '/statistics', label: 'Stats', icon: '📊' },
  { to: '/achievements', label: 'Awards', icon: '🏆' },
  { to: '/profile', label: 'Profile', icon: '👤' },
  { to: '/settings', label: 'Settings', icon: '⚙️' },
];

export default function Layout() {
  const { user, logout } = useAuth();

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <div className="logo">HabitFlow</div>
        {links.map((l) => (
          <NavLink key={l.to} to={l.to} className={({ isActive }) => (isActive ? 'active' : '')}>
            <span>{l.icon}</span> {l.label}
          </NavLink>
        ))}
        <div style={{ marginTop: 'auto', padding: '0.75rem 0.5rem' }}>
          <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
            {user?.name}
          </div>
          <button className="btn btn-ghost" style={{ width: '100%' }} onClick={logout}>
            Log out
          </button>
        </div>
      </aside>

      <main className="main">
        <Outlet />
      </main>

      <nav className="mobile-nav">
        {links.slice(0, 5).map((l) => (
          <NavLink key={l.to} to={l.to} className={({ isActive }) => (isActive ? 'active' : '')}>
            <span style={{ fontSize: '1.2rem' }}>{l.icon}</span>
            {l.label}
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
