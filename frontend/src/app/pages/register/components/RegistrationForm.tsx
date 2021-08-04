import {NewUser} from "../../../generated";
import useInput from "../../../hooks/UseInput";
import {useState} from "react";
import TextField from "@material-ui/core/TextField";
import {Button, CircularProgress} from "@material-ui/core";
import {makeStyles} from "@material-ui/styles";
import {routes} from "../../../modules/Routing";

interface Props {
  register: (registration: NewUser) => Promise<void>
}

/**
 * Form which allows user to register a bot provider account.
 */
export default function RegistrationForm({register}: Props) {
  const [status, setStatus] = useState<'idle' | 'pending' | 'success' | 'error'>('idle');
  const handleTyping = () => setStatus('idle')
  const [message, setMessage] = useState('')

  const {value: name, bind: bindName} = useInput('', handleTyping);
  const {value: email, bind: bindEmail} = useInput('', handleTyping);
  const {value: password, bind: bindPassword} = useInput('', handleTyping);

  const handleRegister = (e: any) => {
    e.preventDefault();
    setStatus('pending'); // set pending status to display circle
    return register({email, name, password})
      .catch(e => {
        setMessage(e.message)
        setStatus('error')
      })
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
        <div className={classes.buttons}>
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
          <Button
            variant="outlined"
            fullWidth
            href={`#${routes.login}`}>
            Or login
          </Button>
        </div>
        <div className={status === 'error' ? classes.warningBox : classes.infoBox}
             style={{visibility: message ? 'visible' : 'hidden'}}>
          {message}
        </div>
      </form>
    </div>
  );
}

const useStyles = makeStyles(() => ({
    page: {},
    form: {
      display: 'flex',
      flexFlow: 'column',
      '& > div': {
        margin: '10px',
        width: '25ch'
      }
    },
    warningBox: {
      color: 'red',
      textAlign: 'justify'
    },
    buttons: {
      '& > button': {
        marginBottom: '10px'
      }
    },
    infoBox: {}
  })
);
