import TextField from '@material-ui/core/TextField';
import { Button, CircularProgress } from '@material-ui/core';
import { useEffect, useState } from 'react';
import { useAuthContext } from '../../hooks/UseAuth';
import useRouter from '../../hooks/UseRouter';
import { makeStyles } from '@material-ui/styles';
import { routes } from '../../modules/Routing';

/**
 * Login Page, redirects to home after
 */
export default function LoginPage({ redirectAfterLogin = routes.home }) {
  const router = useRouter();
  const { user, login } = useAuthContext();

  // guarantee that the user will get redirected
  useEffect(() => {
    if (user) {
      // @ts-ignore sadly I haven't found any other way how to get the redirect
      const { from } = router.location.state ?? { from: { pathname: redirectAfterLogin } };
      router.replace(from);
    }
  }, [redirectAfterLogin, router, user]);

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [status, setStatus] = useState<'idle' | 'pending' | 'error'>('idle');

  const handleLogin = (e: any) => {
    e.preventDefault();

    setStatus('pending'); // set pending status to display circle
    return login({ email, password })
    .then(success => {
      setStatus(success ? 'idle' : 'error');
    })
    .catch(() => setStatus('error')); // catch unauthorized
  };

  const handleTyping = (e: any, change: (value: string) => void) => {
    setStatus('idle'); // reset error flag
    change(e.target.value);
  };

  const classes = useStyles();
  return (
    // we use hidden just as a precaution if the suer already exist
    // to not to "blick" with the login window
    <div className={classes.page} style={{ visibility: user === null ? 'visible' : 'hidden' }}>
      <form className={classes.form} noValidate autoComplete="off">
        <TextField required id="email"
                   error={status === 'error'}
                   type="email"
                   label="e-mail"
                   value={email}
                   disabled={status === 'pending'}
                   onChange={e => handleTyping(e, setEmail)}/>
        <TextField required id="password"
                   error={status === 'error'}
                   type="password"
                   label="password"
                   value={password}
                   disabled={status === 'pending'}
                   onChange={e => handleTyping(e, setPassword)}/>
        <div>
          <Button variant="contained"
                  type="submit"
                  fullWidth
                  onClick={handleLogin}
                  disabled={!(email && password) || status === 'pending'}>
            {status === 'pending'
              ? <CircularProgress size={'1.5rem'}/>
              : <span>Login</span>
            }
          </Button>
        </div>
        <div className={classes.infoBox}
             style={{ visibility: status === 'error' ? 'visible' : 'hidden' }}>
          Unauthorized!
        </div>
      </form>
    </div>
  );
}

const useStyles = makeStyles(() => ({
    page: {
      display: 'flex',
      flexFlow: 'column',
      alignContent: 'center',
      justifyContent: 'center'
    },
    form: {
      display: 'flex',
      flexFlow: 'column',
      '& > div': {
        margin: '10px',
        width: '25ch'
      }
    },
    infoBox: {
      color: 'red'
    }
  })
);
