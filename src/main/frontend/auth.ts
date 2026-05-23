import { configureAuth } from '@vaadin/hilla-react-auth';
import { UserInfoService } from 'Frontend/generated/endpoints.js';
import client from 'Frontend/generated/connect-client.default.js';

// ─── Silent Session Renewal ───────────────────────────────────────────────────

/** Mutex: prevents duplicate concurrent refresh calls */
let refreshPromise: Promise<boolean> | null = null;

/**
 * Attempts to renew the access token using the HttpOnly refresh-token cookie.
 * Returns true on success (new token stored in localStorage), false otherwise.
 */
export async function refreshAccessToken(): Promise<boolean> {
  if (refreshPromise) {
    return refreshPromise;
  }
  refreshPromise = (async () => {
    try {
      const response = await fetch('/api/auth/refresh', {
        method: 'POST',
        credentials: 'include', // sends the HttpOnly refreshToken cookie
      });
      if (response.ok) {
        const data = await response.json();
        localStorage.setItem('accessToken', data.accessToken);
        return true;
      } else {
        localStorage.removeItem('accessToken');
        return false;
      }
    } catch {
      localStorage.removeItem('accessToken');
      return false;
    } finally {
      refreshPromise = null;
    }
  })();
  return refreshPromise;
}

/** Clears auth state and redirects the user to /login */
export function handleAuthFailure(): void {
  localStorage.removeItem('accessToken');
  if (window.location.pathname !== '/login') {
    window.location.href = '/login';
  }
}

// ─── Hilla ConnectClient Middleware ──────────────────────────────────────────

client.middlewares.push(async (context, next) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    context.request.headers.set('Authorization', `Bearer ${token}`);
  }

  let response = await next(context);

  if (response.status === 401) {
    const refreshed = await refreshAccessToken();
    if (refreshed) {
      const newToken = localStorage.getItem('accessToken');
      if (newToken) {
        context.request.headers.set('Authorization', `Bearer ${newToken}`);
      }
      response = await next(context);
    } else {
      handleAuthFailure();
    }
  }

  return response;
});

// ─── Auth Provider ───────────────────────────────────────────────────────────

const auth = configureAuth(UserInfoService.getUserInfo, {
  getRoles: (userInfo) => (userInfo.roles as string[]) || [],
});

export const useAuth = auth.useAuth;
export const AuthProvider = auth.AuthProvider;

// ─── Authenticated Fetch Helper ──────────────────────────────────────────────

/**
 * A drop-in replacement for `fetch` that automatically attaches the current
 * access token and silently retries once after a 401 by refreshing the session.
 */
export const authenticatedFetch = async (
  input: RequestInfo | URL,
  init?: RequestInit,
): Promise<Response> => {
  const buildHeaders = () => {
    const headers = new Headers(
      input instanceof Request ? input.headers : undefined,
    );
    if (init?.headers) {
      new Headers(init.headers).forEach((value, key) =>
        headers.set(key, value),
      );
    }
    const token = localStorage.getItem('accessToken');
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }
    return headers;
  };

  const firstInput = input instanceof Request ? input.clone() : input;
  let response = await fetch(firstInput, {
    ...init,
    headers: buildHeaders(),
    credentials: 'include',
  });

  if (response.status === 401) {
    const refreshed = await refreshAccessToken();
    if (refreshed) {
      const retryInput = input instanceof Request ? input.clone() : input;
      response = await fetch(retryInput, {
        ...init,
        headers: buildHeaders(),
        credentials: 'include',
      });
    } else {
      handleAuthFailure();
    }
  }

  return response;
};
