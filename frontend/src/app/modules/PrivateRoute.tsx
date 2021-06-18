import { Redirect, Route, RouteProps } from 'react-router-dom';
import { useAuthContext } from '../hooks/UseAuth';
import { routes } from './Routing';

/**
 * Guarantees that the components are displayed only
 * when the user is authenticated.
 */
export default function PrivateRoute({ children, ...rest }: RouteProps) {
  const { user } = useAuthContext();
  return (
    <Route
      {...rest}
      render={({ location }) =>
        user ? (children) : (
          <Redirect
            to={{
              pathname: routes.login,
              state: { from: location }
            }}
          />
        )
      }
    />
  );
}
