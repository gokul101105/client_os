import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import ClientDetailsPage from './pages/ClientDetailsPage';
import AdminUsersPage from './pages/AdminUsersPage';
import AdminRequestsPage from './pages/AdminRequestsPage';

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute role="ACCOUNT_MANAGER" />}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/clients/:id" element={<ClientDetailsPage />} />
      </Route>

      <Route element={<ProtectedRoute role="ADMIN" />}>
        <Route path="/admin" element={<Navigate to="/admin/requests" replace />} />
        <Route path="/admin/requests" element={<AdminRequestsPage />} />
        <Route path="/admin/users" element={<AdminUsersPage />} />
      </Route>

      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

export default App;
