import ContactsView from 'Frontend/views/contacts/ContactsView.js';
import MainLayout from 'Frontend/views/MainLayout.js';
import { lazy } from 'react';
import { createBrowserRouter, RouteObject } from 'react-router-dom';
import UserPortfolioView from "Frontend/views/userPortfolio/UserPortfolioView";

const UserDetailsView = lazy(async () => import('Frontend/views/userdetails/UserDetailsView.js'));

export const routes = [
  {
    element: <MainLayout />,
    handle: { title: 'hilla-folioman' },
    children: [
      { path: '/', element: <ContactsView />, handle: { title: 'Contacts' } },
      { path: '/userDetails', element: <UserDetailsView />, handle: { title: 'UserDetails' } },
      { path: '/userPortfolio', element: <UserPortfolioView />, handle: { title: 'UserPortfolio' } },
    ],
  },
] as RouteObject[];

export default createBrowserRouter(routes);