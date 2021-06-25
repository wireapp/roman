import TextField from '@material-ui/core/TextField';
import {Button, CircularProgress} from '@material-ui/core';
import {useState} from 'react';
import {makeStyles} from '@material-ui/styles';
import useApi from "../../hooks/UseApi";
import useInput from "../../hooks/UseInput";

/**
 * Registration page
 */
export default function RegistrationPage() {
  const api = useApi()

  const {value: name, bind: bindName} = useInput('');
  const {value: email, bind: bindEmail} = useInput('');
  const {value: password, bind: bindPassword} = useInput('');
  const [message, setMessage] = useState('')
  const [status, setStatus] = useState<'idle' | 'pending' | 'success' | 'error'>('idle');

  const handleRegister = (e: any) => {
    e.preventDefault();

    setStatus('pending'); // set pending status to display circle
    return api.registerBotProvider({body: {email, name, password}})
      .then((r) => {
        setMessage(r.message)
        setStatus('success');
      })
      .catch((e) => {
        console.error(e)
        setStatus('error')
      });
  };

  const classes = useStyles();
  const disabled = () => status === 'pending' || status === 'success'
  return (
    <div className={classes.page}>
      <form className={classes.form} noValidate autoComplete="off">
        <TextField required id="name"
                   label="Name"
                   disabled={disabled()}
                   {...bindName}/>
        <TextField required id="email"
                   error={status === 'error'}
                   type="email"
                   label="e-mail"
                   disabled={disabled()}
                   {...bindEmail}/>
        <TextField required id="password"
                   error={status === 'error'}
                   type="password"
                   label="Password"
                   disabled={disabled()}
                   {...bindPassword}/>
        <div>
          <Button variant="contained"
                  type="submit"
                  fullWidth
                  onClick={handleRegister}
                  disabled={!(name && email && password) || disabled()}>
            {status === 'pending'
              ? <CircularProgress size={'1.5rem'}/>
              : <span>Register</span>
            }
          </Button>
        </div>
        <div style={{visibility: message ? 'visible' : 'hidden'}}>
          {message}
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
