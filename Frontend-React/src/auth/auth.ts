import { jwtDecode } from 'jwt-decode';

export interface TokenPayload {
  role?: string;
  sub?: string;
  [key: string]: any;
}

export function getToken(): string | null {
  return localStorage.getItem('token');
}

export function getTokenPayload(): TokenPayload | null {
  const token = getToken();
  if (!token) {
    return null;
  }
  try {
    return jwtDecode<TokenPayload>(token);
  } catch {
    return null;
  }
}

export function getUserRole(): string {
  return getTokenPayload()?.role ?? '';
}

// Landing page after login / when a role tries to access a route it's not permitted for.
export function getHomePathForRole(role: string): string {
  return role === 'delivery' ? '/cafe/delivery' : '/cafe/dashboard';
}

export function isAuthenticated(): boolean {
  return !!getToken();
}
