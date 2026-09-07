import { useEffect, useState } from 'react';
import Navbar from '../components/Navbar';
import { useAuth } from '../hooks/useAuth';
import { createAccountManager, deleteUser, getUsers } from '../services/adminService';

function formatDate(value) {
  if (!value) return '—';
  return new Date(value).toLocaleString();
}

export default function AdminUsersPage() {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [deletingId, setDeletingId] = useState(null);
  const [deleteError, setDeleteError] = useState('');

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadUsers = () => {
    setLoading(true);
    setError('');
    getUsers()
      .then(setUsers)
      .catch(() => setError('Could not load users.'))
      .finally(() => setLoading(false));
  };

  useEffect(loadUsers, []);

  const handleDelete = async (target) => {
    if (!window.confirm(`Delete ${target.name} (${target.email})? This cannot be undone.`)) return;
    setDeleteError('');
    setDeletingId(target.id);
    try {
      await deleteUser(target.id);
      setUsers((prev) => prev.filter((u) => u.id !== target.id));
    } catch (err) {
      setDeleteError(err.response?.data?.message || 'Could not delete this user.');
    } finally {
      setDeletingId(null);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    setSubmitting(true);
    try {
      await createAccountManager({ name, email, password });
      setName('');
      setEmail('');
      setPassword('');
      loadUsers();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Could not create this account.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="mx-auto max-w-6xl px-6 py-8">
        <h1 className="text-2xl font-semibold text-gray-900">Users</h1>
        <p className="mt-1 text-sm text-gray-500">
          {users.length} user{users.length === 1 ? '' : 's'}
        </p>

        <div className="mt-6 rounded-lg border border-gray-200 bg-white p-5">
          <h2 className="text-sm font-semibold text-gray-900">Create Account Manager</h2>
          <form onSubmit={handleSubmit} className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-3">
            <div>
              <label htmlFor="new-user-name" className="block text-sm font-medium text-gray-700">
                Name
              </label>
              <input
                id="new-user-name"
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm text-black focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
            </div>
            <div>
              <label htmlFor="new-user-email" className="block text-sm font-medium text-gray-700">
                Email
              </label>
              <input
                id="new-user-email"
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm text-black focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
            </div>
            <div>
              <label htmlFor="new-user-password" className="block text-sm font-medium text-gray-700">
                Temporary password
              </label>
              <input
                id="new-user-password"
                type="text"
                required
                minLength={8}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm text-black focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
            </div>

            {formError && <p className="sm:col-span-3 text-sm text-red-600">{formError}</p>}

            <div className="sm:col-span-3">
              <button
                type="submit"
                disabled={submitting}
                className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60"
              >
                {submitting ? 'Creating…' : 'Create account'}
              </button>
            </div>
          </form>
        </div>

        {loading && <p className="mt-8 text-sm text-gray-500">Loading users…</p>}
        {error && <p className="mt-8 text-sm text-red-600">{error}</p>}
        {deleteError && <p className="mt-8 text-sm text-red-600">{deleteError}</p>}

        {!loading && !error && (
          <div className="mt-6 overflow-x-auto rounded-lg border border-gray-200 bg-white">
            <table className="min-w-full divide-y divide-gray-200 text-sm">
              <thead>
                <tr className="text-left text-xs font-medium uppercase tracking-wide text-gray-500">
                  <th className="px-4 py-3">Name</th>
                  <th className="px-4 py-3">Email</th>
                  <th className="px-4 py-3">Role</th>
                  <th className="px-4 py-3">Created</th>
                  <th className="px-4 py-3" />
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {users.map((u) => (
                  <tr key={u.id}>
                    <td className="px-4 py-3 text-gray-900">{u.name}</td>
                    <td className="px-4 py-3 text-gray-600">{u.email}</td>
                    <td className="px-4 py-3 text-gray-600">{u.role}</td>
                    <td className="px-4 py-3 text-gray-600">{formatDate(u.createdAt)}</td>
                    <td className="px-4 py-3 text-right">
                      {u.email !== user?.email && (
                        <button
                          type="button"
                          onClick={() => handleDelete(u)}
                          disabled={deletingId === u.id}
                          className="text-sm text-red-600 hover:underline disabled:opacity-60"
                        >
                          {deletingId === u.id ? 'Deleting…' : 'Delete'}
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  );
}
