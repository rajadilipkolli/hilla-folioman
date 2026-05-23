import { useEffect, useState } from 'react';
import { Avatar } from '@vaadin/react-components/Avatar.js';
import { Button } from '@vaadin/react-components/Button.js';
import { useNavigate } from 'react-router-dom';

interface PortfolioSummaryItemDTO {
  name: string;
  value: number;
  xirr?: number;
}

interface PortfoliosDTO {
  mutualfunds: PortfolioSummaryItemDTO[];
}

interface UserProfileDTO {
  username: string;
  firstname: string | null;
  lastname: string | null;
  email: string;
  portfolios: PortfoliosDTO;
}

export default function UserProfileView() {
  const [userProfile, setUserProfile] = useState<UserProfileDTO | null>(null);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const token = localStorage.getItem('accessToken');
        const headers: Record<string, string> = {
          'Content-Type': 'application/json',
        };
        if (token) {
          headers['Authorization'] = `Bearer ${token}`;
        }
        const response = await fetch('/api/user', { headers });
        if (!response.ok) {
          throw new Error('Failed to fetch user profile');
        }
        const data = await response.json();
        setUserProfile(data);
      } catch (err: any) {
        setError(err.message);
      }
    };
    fetchProfile();
  }, []);

  if (error) {
    return (
      <div className="flex h-full w-full items-center justify-center p-xl">
        <div className="bg-error-10 text-error p-l rounded-m">
          <h2 className="text-xl font-bold m-0">Error Loading Profile</h2>
          <p className="mt-s m-0">{error}</p>
        </div>
      </div>
    );
  }

  if (!userProfile) {
    return (
      <div className="flex h-full w-full items-center justify-center p-xl text-secondary">
        Loading your profile...
      </div>
    );
  }

  const totalValue =
    userProfile.portfolios?.mutualfunds?.reduce(
      (sum, mf) => sum + mf.value,
      0,
    ) || 0;

  return (
    <div className="p-m sm:p-xl flex flex-col gap-l items-center w-full max-w-full overflow-hidden box-border">
      <div className="bg-contrast-5 p-m sm:p-l rounded-l w-full max-w-screen-lg shadow-s flex flex-col sm:flex-row justify-between items-start sm:items-center gap-m border border-contrast-10 box-border">
        <div className="flex items-center gap-m min-w-0">
          <Avatar name={userProfile.firstname || userProfile.username} />
          <div className="flex flex-col min-w-0">
            <h2 className="text-xl sm:text-2xl font-bold m-0 text-header truncate">
              Welcome back, {userProfile.firstname || userProfile.username}!
            </h2>
            <span className="text-s text-secondary truncate">
              {userProfile.email}
            </span>
          </div>
        </div>
        <div className="flex flex-col items-start sm:items-end flex-shrink-0">
          <span className="text-xs font-semibold text-secondary uppercase tracking-wider">
            Total Portfolio Value
          </span>
          <span className="text-xl sm:text-2xl font-bold text-primary whitespace-nowrap">
            {totalValue.toLocaleString('en-IN', {
              style: 'currency',
              currency: 'INR',
              maximumFractionDigits: 0,
            })}
          </span>
        </div>
      </div>

      <div className="w-full max-w-screen-lg flex flex-col gap-m mt-m box-border">
        <h3 className="text-xl font-bold m-0 text-header">Your Portfolios</h3>
        {userProfile.portfolios?.mutualfunds?.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-m">
            {userProfile.portfolios.mutualfunds.map((mf, i) => (
              <div
                key={i}
                className="bg-contrast-5 p-m rounded-m flex flex-col justify-between shadow-xs border border-contrast-10 gap-m">
                <span className="font-medium text-body break-words">
                  {mf.name}
                </span>
                <div className="flex justify-between items-center w-full">
                  {mf.xirr !== null && mf.xirr !== undefined ? (
                    <span className="text-sm font-semibold px-s py-xs rounded bg-primary-10 text-primary whitespace-nowrap">
                      XIRR: {(mf.xirr * 100).toFixed(2)}%
                    </span>
                  ) : (
                    <div />
                  )}
                  <span className="font-bold text-success whitespace-nowrap">
                    {mf.value.toLocaleString('en-IN', {
                      style: 'currency',
                      currency: 'INR',
                      maximumFractionDigits: 0,
                    })}
                  </span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center p-xl border border-dashed border-contrast-30 rounded-m bg-contrast-5 gap-m">
            <span className="text-body text-secondary">
              No portfolios found.
            </span>
            <Button
              theme="primary"
              onClick={() => navigate('/importmutualfunds')}>
              Go to Import Mutual Funds
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
