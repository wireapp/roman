import {ProvideAuth} from '../hooks/UseAuth';
import {BrowserRouter, HashRouter, Redirect, Route, Switch} from 'react-router-dom';
import LoginPage from '../pages/login/LoginPage';
// import UserProfilePage from '../pages/user/UserProfilePage';
// import HomePage from '../pages/home/HomePage';
import React from 'react';
import HomePage from '../pages/home/HomePage';
import PrivateRoute from './PrivateRoute';

export const routes = {
  home: '/',
  login: '/login',
  register: '/register',
  profile: '/profile'
};

/**
 * Creates routing in the application, should be on the top level.
 */
export default function Routing() {
  // if there's some problem with hashrouter you can try this approach
  // https://github.com/ReactTraining/history/issues/435
  return (
    <ProvideAuth>
      <HashRouter>
        <Switch>
          <Route path={routes.login}>
            <LoginPage/>
          </Route>

          {/*<PrivateRoute path={routes.profile}>*/}
          {/*  <UserProfilePage/>*/}
          {/*</PrivateRoute>*/}

          <PrivateRoute path={routes.home}>
            <HomePage/>
          </PrivateRoute>
        </Switch>
      </HashRouter>
      {/* redirect traffic to hashrouter */}
      <BrowserRouter>
        {!window.location.hash && (<Redirect to={`#${window.location.pathname}`}/>)}
      </BrowserRouter>
    </ProvideAuth>
  );
}
