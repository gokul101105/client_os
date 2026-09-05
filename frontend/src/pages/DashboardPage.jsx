import { useEffect, useState } from 'react';
import Navbar from '../components/Navbar';
import ClientCard from '../components/ClientCard';
import { getClients } from '../services/clientService';

export default function DashboardPage() {
  const [clients, setClients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    getClients()
      .then((data) => {
        if (!cancelled) setClients(data);
      })
      .catch(() => {
        if (!cancelled) setError('Could not load clients.');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="mx-auto max-w-6xl px-6 py-8">
        <h1 className="text-2xl font-semibold text-gray-900">Clients</h1>
        <p className="mt-1 text-sm text-gray-500">
          {clients.length} client{clients.length === 1 ? '' : 's'}
        </p>

        {loading && <p className="mt-8 text-sm text-gray-500">Loading clients…</p>}
        {error && <p className="mt-8 text-sm text-red-600">{error}</p>}

        {!loading && !error && clients.length === 0 && (
          <p className="mt-8 text-sm text-gray-500">No clients yet.</p>
        )}

        {!loading && !error && clients.length > 0 && (
          <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {clients.map((client) => (
              <ClientCard key={client.id} client={client} />
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
