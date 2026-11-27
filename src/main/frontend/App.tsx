import router from 'Frontend/routes.js';
import { RouterProvider } from 'react-router';

import { AuthProvider } from 'Frontend/auth';

export default function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  );
}
