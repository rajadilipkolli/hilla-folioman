import { AppLayout } from '@vaadin/react-components/AppLayout.js';
import { DrawerToggle } from '@vaadin/react-components/DrawerToggle.js';
import Placeholder from 'Frontend/components/placeholder/Placeholder.js';
import { useRouteMetadata } from 'Frontend/util/routing.js';
import { Suspense } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { Button } from '@vaadin/react-components/Button.js';
import { useAuth } from 'Frontend/auth';

const navLinkClasses = ({ isActive }: any) => {
  return `block rounded-m p-s ${isActive ? 'bg-primary-10 text-primary' : 'text-body'}`;
};

export default function MainLayout() {
  const currentTitle = useRouteMetadata()?.title ?? 'My App';
  const { state, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      try {
        await fetch('/api/auth/logout', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken }),
        });
      } catch (e) {
        console.error('Logout failed', e);
      }
    }
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    logout();
    navigate('/login');
  };

  return (
    <AppLayout primarySection="drawer">
      <div slot="drawer" className="flex flex-col justify-between h-full p-m">
        <header className="flex flex-col gap-m">
          <h1 className="text-l m-0">My App</h1>
          <nav>
            <NavLink className={navLinkClasses} to="/">
              Mutual Fund Schemes
            </NavLink>
            <NavLink className={navLinkClasses} to="/importmutualfunds">
              Import Mutual Funds
            </NavLink>
            <NavLink className={navLinkClasses} to="/userPortfolio">
              UserPortfolio
            </NavLink>
            <NavLink className={navLinkClasses} to="/rebalance">
              ReBalance Calculator
            </NavLink>
          </nav>
        </header>
      </div>

      <DrawerToggle slot="navbar" aria-label="Menu toggle"></DrawerToggle>
      <h2 slot="navbar" className="text-l m-0 flex-1">
        {currentTitle}
      </h2>

      <div slot="navbar" className="px-m flex items-center gap-s">
        {state.user ? (
          <>
            <span className="font-semibold text-s">
              Welcome, {state.user.username}
            </span>
            <Button onClick={handleLogout}>Log out</Button>
          </>
        ) : (
          <Button onClick={() => navigate('/login')}>Log in</Button>
        )}
      </div>

      <Suspense fallback={<Placeholder />}>
        <Outlet />
      </Suspense>
    </AppLayout>
  );
}
