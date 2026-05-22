import { Button } from '@vaadin/react-components/Button.js';
import { PasswordField } from '@vaadin/react-components/PasswordField.js';
import { TextField } from '@vaadin/react-components/TextField.js';
import { Notification } from '@vaadin/react-components/Notification.js';
import { useState } from 'react';
import { useAuth } from 'Frontend/auth';
import { useNavigate } from 'react-router-dom';

export default function LoginView() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async () => {
    setLoading(true);
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

        window.location.href = '/userPortfolio';
      } else {
        const errorData = await response.json();
        Notification.show(errorData.detail || 'Login failed', {
          position: 'top-center',
          theme: 'error',
        });
      }
    } catch (error) {
      Notification.show('An error occurred during login.', {
        position: 'top-center',
        theme: 'error',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center p-xl gap-m w-full">
      <h2>Login</h2>
      <TextField
        label="Username"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        className="w-full max-w-sm"
      />
      <PasswordField
        label="Password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        onKeyDown={(e) => e.key === 'Enter' && handleLogin()}
        className="w-full max-w-sm"
      />
      <Button
        theme="primary"
        onClick={handleLogin}
        disabled={loading}
        className="w-full max-w-sm">
        {loading ? 'Logging in...' : 'Log in'}
      </Button>
    </div>
  );
}
