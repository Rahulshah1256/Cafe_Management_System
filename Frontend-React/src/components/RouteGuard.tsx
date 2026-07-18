import { ReactNode, useEffect, useRef } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useSnackbar } from 'notistack';
import { getHomePathForRole, getTokenPayload, isAuthenticated } from '../auth/auth';
import { GlobalConstants } from '../shared/globalConstants';

interface Props {
  expectedRole: string[];
  children: ReactNode;
}

// Every valid account role in the system - kept in one place so RouteGuard and Sidebar
// agree on what counts as a "known" role vs. a corrupt/stale token that should be logged out.
const KNOWN_ROLES = ['user', 'admin', 'delivery'];

/**
 * Mirrors the Angular RouteGuardService.
 * - No/invalid token -> redirect to home.
 * - Valid token but role not permitted -> snackbar + redirect to dashboard.
 */
export default function RouteGuard({ expectedRole, children }: Props) {
  const navigate = useNavigate();
  const location = useLocation();
  const { enqueueSnackbar } = useSnackbar();
  const warned = useRef(false);

  const tokenPayload = getTokenPayload();

  useEffect(() => {
    if (
      tokenPayload &&
      KNOWN_ROLES.includes(tokenPayload.role) &&
      !expectedRole.includes(tokenPayload.role) &&
      !warned.current
    ) {
      warned.current = true;
      enqueueSnackbar(GlobalConstants.unauthroized, {
        variant: 'error',
        anchorOrigin: { horizontal: 'center', vertical: 'top' },
        autoHideDuration: 2000,
      });
    }
  }, [location.pathname]);

  if (!tokenPayload) {
    localStorage.clear();
    return <Navigate to="/" replace />;
  }

  const role = tokenPayload.role;
  if (!KNOWN_ROLES.includes(role)) {
    localStorage.clear();
    return <Navigate to="/" replace />;
  }

  if (isAuthenticated() && expectedRole.includes(role)) {
    return <>{children}</>;
  }

  return <Navigate to={getHomePathForRole(role)} replace />;
}
