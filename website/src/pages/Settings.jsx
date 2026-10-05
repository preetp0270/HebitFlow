import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { changePassword, deleteAccount, exportData } from '../api/user';

export default function Settings() {
  const { logout } = useAuth();
  const [currentPassword, setCurrent] = useState('');
  const [newPassword, setNew] = useState('');
  const [deletePw, setDeletePw] = useState('');
  const [msg, setMsg] = useState('');
  const [err, setErr] = useState('');

  const handleChangePw = async (e) => {
    e.preventDefault();
    setErr('');
    setMsg('');
    try {
      await changePassword({ currentPassword, newPassword });
      setMsg('Password changed. Please log in again.');
      setTimeout(() => logout(), 1500);
    } catch (error) {
      setErr(error.response?.data?.message || 'Failed');
    }
  };

  const handleExport = async () => {
    try {
      const { data } = await exportData();
      const blob = new Blob([JSON.stringify(data.data, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `habitflow-export-${new Date().toISOString().slice(0, 10)}.json`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      setErr('Export failed');
    }
  };

  const handleDelete = async (e) => {
    e.preventDefault();
    if (!window.confirm('Permanently delete your account and all data? This cannot be undone.')) return;
    try {
      await deleteAccount(deletePw);
      logout();
    } catch (error) {
      setErr(error.response?.data?.message || 'Delete failed');
    }
  };

  return (
    <div>
      <h1 className="page-title">Settings</h1>
      <p className="page-subtitle">Account and data</p>

      {err && <p className="error-msg">{err}</p>}
      {msg && <p className="success-msg">{msg}</p>}

      <div className="card" style={{ marginBottom: '1.25rem', maxWidth: 420 }}>
        <h3 style={{ marginBottom: '0.75rem' }}>Change password</h3>
        <form onSubmit={handleChangePw}>
          <div className="form-group">
            <label>Current password</label>
            <input type="password" value={currentPassword} onChange={(e) => setCurrent(e.target.value)} required />
          </div>
          <div className="form-group">
            <label>New password</label>
            <input type="password" value={newPassword} onChange={(e) => setNew(e.target.value)} required minLength={6} />
          </div>
          <button type="submit" className="btn btn-primary">
            Update password
          </button>
        </form>
      </div>

      <div className="card" style={{ marginBottom: '1.25rem', maxWidth: 420 }}>
        <h3 style={{ marginBottom: '0.75rem' }}>Data</h3>
        <button className="btn btn-ghost" onClick={handleExport}>
          Export data (JSON)
        </button>
      </div>

      <div className="card" style={{ maxWidth: 420, borderColor: 'var(--danger)' }}>
        <h3 style={{ marginBottom: '0.75rem', color: 'var(--danger)' }}>Danger zone</h3>
        <form onSubmit={handleDelete}>
          <div className="form-group">
            <label>Confirm password to delete account</label>
            <input type="password" value={deletePw} onChange={(e) => setDeletePw(e.target.value)} required />
          </div>
          <button type="submit" className="btn btn-danger">
            Delete account
          </button>
        </form>
      </div>
    </div>
  );
}
