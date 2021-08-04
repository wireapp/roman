import {ProvideAuth} from '../hooks/UseAuth';
import {HashRouter, Route, Switch} from 'react-router-dom';
import LoginPage from '../pages/login/LoginPage';
import React from 'react';
import HomePage from '../pages/home/HomePage';
import PrivateRoute from './PrivateRoute';
import RegistrationPage from "../pages/register/RegistrationPage";

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
          <Route path={routes.register}>
            <RegistrationPage/>
          </Route>

          <PrivateRoute path={routes.home}>
            <HomePage/>
          </PrivateRoute>
        </Switch>
      </HashRouter>
    </ProvideAuth>
  );
}
