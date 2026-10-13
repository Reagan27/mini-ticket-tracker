import { useState } from 'react';

export const STATUSES = ['open', 'in_progress', 'closed'];
export const PRIORITIES = ['low', 'medium', 'high'];
export const label = (v) => v.replace('_', ' ');

export function StatsBar({ stats }) {
  return (
    <div className="stats">
      {STATUSES.map((s) => (
        <div key={s} className={`stat stat-${s}`}>
          <span className="stat-count">{stats?.[s] ?? '–'}</span>
          <span className="stat-label">{label(s)}</span>
        </div>
      ))}
    </div>
  );
}

export function TicketForm({ onCreate }) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState('medium');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await onCreate({ title, description, priority });
      setTitle('');
      setDescription('');
      setPriority('medium');
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="card form" onSubmit={handleSubmit}>
      <h2>New ticket</h2>
      <label>
        Title *
        <input value={title} onChange={(e) => setTitle(e.target.value)} maxLength={200} required />
      </label>
      <label>
        Description
        <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={3} maxLength={2000} />
      </label>
      <label>
        Priority
        <select value={priority} onChange={(e) => setPriority(e.target.value)}>
          {PRIORITIES.map((p) => <option key={p} value={p}>{label(p)}</option>)}
        </select>
      </label>
      {error && <p className="error" role="alert">{error}</p>}
      <button type="submit" disabled={submitting || !title.trim()}>
        {submitting ? 'Creating…' : 'Create ticket'}
      </button>
    </form>
  );
}

export function TicketRow({ ticket, onStatusChange }) {
  return (
    <li className="ticket">
      <div className="ticket-main">
        <strong>{ticket.title}</strong>
        {ticket.description && <p>{ticket.description}</p>}
        <small>{new Date(ticket.createdAt).toLocaleString()}</small>
      </div>
      <span className={`badge priority-${ticket.priority}`}>{ticket.priority}</span>
      <select
        aria-label={`Status for ${ticket.title}`}
        className={`status status-${ticket.status}`}
        value={ticket.status}
        onChange={(e) => onStatusChange(ticket.id, e.target.value)}
      >
        {STATUSES.map((s) => <option key={s} value={s}>{label(s)}</option>)}
      </select>
    </li>
  );
}

export function Pagination({ page, totalPages, onPage }) {
  if (totalPages <= 1) return null;
  return (
    <div className="pagination">
      <button onClick={() => onPage(page - 1)} disabled={page <= 1}>← Prev</button>
      <span>Page {page} of {totalPages}</span>
      <button onClick={() => onPage(page + 1)} disabled={page >= totalPages}>Next →</button>
    </div>
  );
}
