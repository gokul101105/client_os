import { createContext, useState } from 'react';
import { decodeJwt, isTokenExpired } from '../utils/jwt';
import { login as loginRequest } from '../services/authService';
import { TOKEN_KEY } from '../services/api';

export const AuthContext = createContext(null);

function readStoredUser() {
  const token = localStorage.getItem(TOKEN_KEY);
  if (!token || isTokenExpired(token)) {
    localStorage.removeItem(TOKEN_KEY);
    return null;
  }
  const claims = decodeJwt(token);
  return { token, email: claims?.sub ?? null, role: claims?.role ?? null };
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);

  const login = async (email, password) => {
    const { token } = await loginRequest(email, password);
    localStorage.setItem(TOKEN_KEY, token);
    const claims = decodeJwt(token);
    setUser({ token, email: claims?.sub ?? null, role: claims?.role ?? null });
    return { token, role: claims?.role ?? null };
  };

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
