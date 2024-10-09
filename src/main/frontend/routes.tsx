import MainLayout from 'Frontend/views/MainLayout.js';
import { lazy } from 'react';
import { createBrowserRouter, RouteObject } from 'react-router-dom';
import UserPortfolioView from "Frontend/views/userPortfolio/UserPortfolioView";
import ReBalanceView from "Frontend/views/rebalance/ReBalanceView";
import MfSchemesView from "Frontend/views/mfschemes/MfSchemesView";

const ImportMutualFundsView = lazy(async () => import('Frontend/views/importmutualfunds/ImportMutualFundsView'));

export const routes = [
  {
    element: <MainLayout />,
    handle: { title: 'hilla-folioman' },
    children: [
      { path: '/importmutualfunds', element: <ImportMutualFundsView />, handle: { title: 'Import Mutual Funds' } },
      { path: '/userPortfolio', element: <UserPortfolioView />, handle: { title: 'UserPortfolio' } },
      { path: '/rebalance', element: <ReBalanceView />, handle: { title: 'ReBalance Calculator' } },
      { path: '/', element: <MfSchemesView />, handle: {title: 'Mutual Fund Schemes'}},
    ],
  },
] as RouteObject[];

export default createBrowserRouter(routes);
