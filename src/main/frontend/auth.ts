import { configureAuth } from '@vaadin/hilla-react-auth';
import { UserInfoService } from 'Frontend/generated/endpoints.js';
import client from 'Frontend/generated/connect-client.default.js';

client.middlewares.push(async (context, next) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    context.request.headers.set('Authorization', `Bearer ${token}`);
  }
  return await next(context);
});

const auth = configureAuth(UserInfoService.getUserInfo, {
  getRoles: (userInfo) => (userInfo.roles as string[]) || [],
});

export const useAuth = auth.useAuth;
export const AuthProvider = auth.AuthProvider;

export const authenticatedFetch = async (
  input: RequestInfo | URL,
  init?: RequestInit,
) => {
  const headers = new Headers(init?.headers);
  const token = localStorage.getItem('accessToken');
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  return fetch(input, { ...init, headers, credentials: 'include' });
};
