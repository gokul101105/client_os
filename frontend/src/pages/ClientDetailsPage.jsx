import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { getClientById } from '../services/clientService';

const TABS = ['Overview', 'Documents', 'AI Assistant', 'Health'];

function formatDate(value) {
  if (!value) return '—';
  return new Date(value).toLocaleString();
}

export default function ClientDetailsPage() {
  const { id } = useParams();
  const [client, setClient] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('Overview');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');
    getClientById(id)
      .then((data) => {
        if (!cancelled) setClient(data);
      })
      .catch(() => {
        if (!cancelled) setError('Could not load this client.');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="mx-auto max-w-6xl px-6 py-8">
        <Link to="/dashboard" className="text-sm text-blue-600 hover:underline">
          ← Back to clients
        </Link>

        {loading && <p className="mt-6 text-sm text-gray-500">Loading client…</p>}
        {error && <p className="mt-6 text-sm text-red-600">{error}</p>}

        {!loading && !error && client && (
          <>
            <div className="mt-4 flex items-center justify-between gap-4">
              <div>
                <h1 className="text-2xl font-semibold text-gray-900">{client.name}</h1>
                <p className="mt-1 text-sm text-gray-500">{client.industry || 'No industry set'}</p>
              </div>
              <span className="shrink-0 rounded-full bg-blue-50 px-3 py-1 text-sm font-medium text-blue-700">
                {client.plan}
              </span>
            </div>

            <div className="mt-6 border-b border-gray-200">
              <nav className="-mb-px flex gap-6">
                {TABS.map((tab) => (
                  <button
                    key={tab}
                    type="button"
                    onClick={() => setActiveTab(tab)}
                    className={`border-b-2 px-1 py-3 text-sm font-medium ${
                      activeTab === tab
                        ? 'border-blue-600 text-blue-600'
                        : 'border-transparent text-gray-500 hover:text-gray-700'
                    }`}
                  >
                    {tab}
                  </button>
                ))}
              </nav>
            </div>

            <div className="mt-6">
              {activeTab === 'Overview' && (
                <dl className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
                  <Field label="Account manager" value={client.accountManagerName} />
                  <Field label="Health score" value={client.healthScore ?? '—'} />
                  <Field label="Open issues" value={client.openIssuesCount ?? 0} />
                  <Field label="Last activity" value={formatDate(client.lastActivityDate)} />
                  <Field label="Client since" value={formatDate(client.createdAt)} />
                </dl>
              )}
              {activeTab === 'Documents' && <Placeholder text="Documents will appear here in a later module." />}
              {activeTab === 'AI Assistant' && <Placeholder text="AI Assistant is not wired up yet." />}
              {activeTab === 'Health' && <Placeholder text="Detailed health analytics are coming in a later module." />}
            </div>
          </>
        )}
      </main>
    </div>
  );
}

function Field({ label, value }) {
  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4">
      <dt className="text-xs font-medium uppercase tracking-wide text-gray-500">{label}</dt>
      <dd className="mt-1 text-sm text-gray-900">{value}</dd>
    </div>
  );
}

function Placeholder({ text }) {
  return (
    <div className="rounded-lg border border-dashed border-gray-300 bg-white p-8 text-center text-sm text-gray-500">
      {text}
    </div>
  );
}
