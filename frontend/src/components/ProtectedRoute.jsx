import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

const HOME_BY_ROLE = {
  ADMIN: '/admin',
  ACCOUNT_MANAGER: '/dashboard',
};

// role is optional: omit it for "just needs to be logged in" routes, pass
// it for routes that also need a specific role. A logged-in user with the
// wrong role is bounced to their own home rather than to /login -- they
// are authenticated, just not authorized for this particular route.
export default function ProtectedRoute({ role }) {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (role && user.role !== role) {
    return <Navigate to={HOME_BY_ROLE[user.role] ?? '/login'} replace />;
  }

  return <Outlet />;
}
