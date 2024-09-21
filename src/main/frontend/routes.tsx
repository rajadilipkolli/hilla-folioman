import MainLayout from 'Frontend/views/MainLayout.js';
import { lazy } from 'react';
import { createBrowserRouter, RouteObject } from 'react-router-dom';
import UserPortfolioView from "Frontend/views/userPortfolio/UserPortfolioView";
import MfSchemesView from './views/mfschemes/MfSchemesView';

const UserDetailsView = lazy(async () => import('Frontend/views/userdetails/UserDetailsView.js'));

export const routes = [
  {
    element: <MainLayout />,
    handle: { title: 'hilla-folioman' },
    children: [
      { path: '/userDetails', element: <UserDetailsView />, handle: { title: 'Import Mutual Funds' } },
      { path: '/userPortfolio', element: <UserPortfolioView />, handle: { title: 'UserPortfolio' } },
      { path: '/', element: <MfSchemesView />, handle: {title: 'Mutual Fund Schemes'}},
    ],
  },
] as RouteObject[];

export default createBrowserRouter(routes);