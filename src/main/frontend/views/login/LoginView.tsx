import { LoginOverlay } from '@vaadin/react-components/LoginOverlay.js';
import { useState } from 'react';
import { useAuth } from 'Frontend/auth';
import { useNavigate } from 'react-router-dom';

export default function LoginView() {
  const [error, setError] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async (e: any) => {
    const username = e.detail.username;
    const password = e.detail.password;

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({ username, password }),
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem('accessToken', data.accessToken);
        setError(false);
        window.location.href = '/';
      } else {
        setError(true);
      }
    } catch (error) {
      setError(true);
    }
  };

  return (
    <LoginOverlay
      opened
      error={error}
      onLogin={handleLogin}
      title="FolioMan"
      description="Manage your mutual fund portfolios"
    />
  );
}
