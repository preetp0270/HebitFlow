import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createHabit, getHabit, updateHabit } from '../api/habits';

const ICONS = ['🎯', '💧', '📚', '🏃', '🧘', '🥗', '💪', '😴', '✍️', '🎵'];
const COLORS = ['#6366F1', '#22C55E', '#F59E0B', '#EF4444', '#06B6D4', '#8B5CF6', '#EC4899'];
const DAYS = [
  { v: 0, l: 'Sun' },
  { v: 1, l: 'Mon' },
  { v: 2, l: 'Tue' },
  { v: 3, l: 'Wed' },
  { v: 4, l: 'Thu' },
  { v: 5, l: 'Fri' },
  { v: 6, l: 'Sat' },
];

export default function HabitForm() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: '',
    description: '',
    icon: '🎯',
    color: '#6366F1',
    frequencyType: 'DAILY',
    selectedDays: [],
    targetPerWeek: 3,
    reminderEnabled: false,
    reminderTime: '09:00',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!isEdit) return;
    getHabit(id)
      .then(({ data }) => {
        const h = data.data.habit;
        setForm({
          name: h.name,
          description: h.description || '',
          icon: h.icon || '🎯',
          color: h.color || '#6366F1',
          frequencyType: h.frequencyType || 'DAILY',
          selectedDays: h.selectedDays || [],
          targetPerWeek: h.targetPerWeek || 3,
          reminderEnabled: !!h.reminderEnabled,
          reminderTime: h.reminderTime || '09:00',
        });
      })
      .catch(() => setError('Failed to load habit'));
  }, [id, isEdit]);

  const set = (key, value) => setForm((f) => ({ ...f, [key]: value }));

  const toggleDay = (d) => {
    setForm((f) => ({
      ...f,
      selectedDays: f.selectedDays.includes(d)
        ? f.selectedDays.filter((x) => x !== d)
        : [...f.selectedDays, d].sort(),
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.name.trim()) {
      setError('Name is required');
      return;
    }
    setLoading(true);
    setError('');
    try {
      if (isEdit) {
        await updateHabit(id, form);
      } else {
        await createHabit(form);
      }
      navigate(isEdit ? `/habits/${id}` : '/habits');
    } catch (err) {
      setError(err.response?.data?.message || 'Save failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1 className="page-title">{isEdit ? 'Edit habit' : 'New habit'}</h1>
      <p className="page-subtitle">{isEdit ? 'Update your habit' : 'Create a new habit to track'}</p>

      <form className="card" onSubmit={handleSubmit} style={{ maxWidth: 520 }}>
        <div className="form-group">
          <label>Name *</label>
          <input value={form.name} onChange={(e) => set('name', e.target.value)} required maxLength={100} />
        </div>
        <div className="form-group">
          <label>Description</label>
          <textarea
            value={form.description}
            onChange={(e) => set('description', e.target.value)}
            rows={2}
            maxLength={500}
          />
        </div>
        <div className="form-group">
          <label>Icon</label>
          <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap' }}>
            {ICONS.map((ic) => (
              <button
                key={ic}
                type="button"
                onClick={() => set('icon', ic)}
                style={{
                  fontSize: '1.3rem',
                  width: 40,
                  height: 40,
                  borderRadius: 8,
                  border: form.icon === ic ? '2px solid var(--primary)' : '1px solid var(--border)',
                  background: form.icon === ic ? 'color-mix(in srgb, var(--primary) 15%, transparent)' : 'var(--bg)',
                }}
              >
                {ic}
              </button>
            ))}
          </div>
        </div>
        <div className="form-group">
          <label>Color</label>
          <div style={{ display: 'flex', gap: '0.4rem' }}>
            {COLORS.map((c) => (
              <button
                key={c}
                type="button"
                onClick={() => set('color', c)}
                style={{
                  width: 32,
                  height: 32,
                  borderRadius: '50%',
                  background: c,
                  border: form.color === c ? '3px solid var(--text)' : '2px solid transparent',
                }}
              />
            ))}
          </div>
        </div>
        <div className="form-group">
          <label>Frequency</label>
          <select value={form.frequencyType} onChange={(e) => set('frequencyType', e.target.value)}>
            <option value="DAILY">Every day</option>
            <option value="SELECTED_DAYS">Selected days</option>
            <option value="WEEKLY_TARGET">X times per week</option>
          </select>
        </div>
        {form.frequencyType === 'SELECTED_DAYS' && (
          <div className="form-group">
            <label>Days</label>
            <div style={{ display: 'flex', gap: '0.35rem', flexWrap: 'wrap' }}>
              {DAYS.map((d) => (
                <button
                  key={d.v}
                  type="button"
                  onClick={() => toggleDay(d.v)}
                  className="btn"
                  style={{
                    padding: '0.4rem 0.6rem',
                    background: form.selectedDays.includes(d.v) ? 'var(--primary)' : 'var(--bg)',
                    color: form.selectedDays.includes(d.v) ? 'white' : 'var(--text)',
                    border: '1px solid var(--border)',
                  }}
                >
                  {d.l}
                </button>
              ))}
            </div>
          </div>
        )}
        {form.frequencyType === 'WEEKLY_TARGET' && (
          <div className="form-group">
            <label>Times per week</label>
            <input
              type="number"
              min={1}
              max={7}
              value={form.targetPerWeek}
              onChange={(e) => set('targetPerWeek', Number(e.target.value))}
            />
          </div>
        )}
        <div className="form-group">
          <label>
            <input
              type="checkbox"
              checked={form.reminderEnabled}
              onChange={(e) => set('reminderEnabled', e.target.checked)}
              style={{ marginRight: 8 }}
            />
            Enable reminder (shown on Android)
          </label>
        </div>
        {form.reminderEnabled && (
          <div className="form-group">
            <label>Reminder time</label>
            <input type="time" value={form.reminderTime} onChange={(e) => set('reminderTime', e.target.value)} />
          </div>
        )}
        {error && <p className="error-msg">{error}</p>}
        <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1rem' }}>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Saving…' : isEdit ? 'Save changes' : 'Create habit'}
          </button>
          <button type="button" className="btn btn-ghost" onClick={() => navigate(-1)}>
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
