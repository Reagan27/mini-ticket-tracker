
async function request(path, options) {
  const res = await fetch(path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  const body = res.status === 204 ? null : await res.json().catch(() => null);
  if (!res.ok) {
    const detail = body?.details?.length ? `: ${body.details.join(', ')}` : '';
    throw new Error((body?.error || `Request failed (${res.status})`) + detail);
  }
  return body;
}

export const listTickets = ({ status, page, limit }) => {
  const params = new URLSearchParams({ page, limit });
  if (status) params.set('status', status);
  return request(`/tickets?${params}`);
};

export const createTicket = (ticket) =>
  request('/tickets', { method: 'POST', body: JSON.stringify(ticket) });

export const updateTicket = (id, changes) =>
  request(`/tickets/${id}`, { method: 'PATCH', body: JSON.stringify(changes) });

export const getStats = () => request('/tickets/stats');
