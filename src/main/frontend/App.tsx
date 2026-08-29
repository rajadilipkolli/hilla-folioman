import router from 'Frontend/routes.js';
import { RouterProvider } from 'react-router-dom';

import { AuthProvider } from 'Frontend/auth';

export default function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  );
}
