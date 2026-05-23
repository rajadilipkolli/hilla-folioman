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

/** Checks if the JWT is expired by reading the exp claim */
export function isTokenExpired(token: string): boolean {
  try {
    const payloadBase64 = token.split('.')[1];
    if (!payloadBase64) return true;
    // Handle base64url characters
    const base64 = payloadBase64.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join(''),
    );
    const payload = JSON.parse(jsonPayload);
    if (!payload.exp) return false;
    return payload.exp < Date.now() / 1000;
  } catch {
    return true;
  }
}

// ─── Hilla ConnectClient Middleware ──────────────────────────────────────────

client.middlewares.push(async (context, next) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    if (isTokenExpired(token)) {
      handleAuthFailure();
      // Return a dummy rejected promise to halt request execution
      return Promise.reject(new Error('Token expired'));
    }
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
      if (isTokenExpired(token)) {
        handleAuthFailure();
        throw new Error('Token expired');
      }
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
