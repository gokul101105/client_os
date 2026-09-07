import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const isAdmin = user?.role === 'ADMIN';

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const navLinkClass = ({ isActive }) =>
    `text-sm font-medium ${isActive ? 'text-blue-600' : 'text-gray-500 hover:text-gray-700'}`;

  return (
    <header className="border-b border-gray-200 bg-white">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
        <div className="flex items-center gap-6">
          <Link to={isAdmin ? '/admin' : '/dashboard'} className="text-lg font-semibold text-gray-900">
            ClientOS
          </Link>
          {isAdmin && (
            <nav className="flex items-center gap-4">
              <NavLink to="/admin/requests" className={navLinkClass}>
                Requests
              </NavLink>
              <NavLink to="/admin/users" className={navLinkClass}>
                Users
              </NavLink>
            </nav>
          )}
        </div>
        <div className="flex items-center gap-4">
          {user?.email && <span className="text-sm text-gray-500">{user.email}</span>}
          <button
            onClick={handleLogout}
            className="rounded-md border border-gray-300 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            Log out
          </button>
        </div>
      </div>
    </header>
  );
}
