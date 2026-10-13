import { useCallback, useEffect, useState } from 'react';
import * as api from './api.js';
import { STATUSES, label, StatsBar, TicketForm, TicketRow, Pagination } from './components.jsx';

const LIMIT = 5;

export default function App() {
  const [tickets, setTickets] = useState([]);
  const [pageInfo, setPageInfo] = useState({ page: 1, totalPages: 1 });
  const [stats, setStats] = useState(null);
  const [filter, setFilter] = useState(''); 
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const refresh = useCallback(async () => {
    try {
      const [list, s] = await Promise.all([
        api.listTickets({ status: filter, page, limit: LIMIT }),
        api.getStats(),
      ]);
     
      if (list.items.length === 0 && page > 1) {
        setPage(list.totalPages || 1);
        return;
      }
      setTickets(list.items);
      setPageInfo({ page: list.page, totalPages: list.totalPages });
      setStats(s);
      setError(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [filter, page]);

  useEffect(() => {
    setLoading(true);
    refresh();
  }, [refresh]);

  async function handleCreate(data) {
    await api.createTicket(data); 
    setFilter('');
    setPage(1); 
    await refresh();
  }

  async function handleStatusChange(id, status) {
    try {
      await api.updateTicket(id, { status });
      await refresh();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <main className="container">
      <h1>Mini Ticket Tracker</h1>
      <StatsBar stats={stats} />

      <div className="layout">
        <TicketForm onCreate={handleCreate} />

        <section className="card">
          <div className="list-header">
            <h2>Tickets</h2>
            <select
              aria-label="Filter by status"
              value={filter}
              onChange={(e) => { setFilter(e.target.value); setPage(1); }}
            >
              <option value="">All statuses</option>
              {STATUSES.map((s) => <option key={s} value={s}>{label(s)}</option>)}
            </select>
          </div>

          {error && <p className="error" role="alert">{error}</p>}
          {loading ? (
            <p className="muted">Loading…</p>
          ) : tickets.length === 0 ? (
            <p className="muted">No tickets yet.</p>
          ) : (
            <ul className="tickets">
              {tickets.map((t) => (
                <TicketRow key={t.id} ticket={t} onStatusChange={handleStatusChange} />
              ))}
            </ul>
          )}

          <Pagination page={pageInfo.page} totalPages={pageInfo.totalPages} onPage={setPage} />
        </section>
      </div>
    </main>
  );
}
