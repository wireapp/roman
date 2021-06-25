import {useState} from 'react';
import {makeStyles} from '@material-ui/styles';
import useApi from "../../hooks/UseApi";
import {NewUser} from "../../generated";
import RegistrationSuccess from "./components/RegistrationSuccess";
import RegistrationForm from "./components/RegistrationForm";

/**
 * Registration page.
 */
export default function RegistrationPage() {
  const api = useApi()

  const [registered, setRegistered] = useState<string | undefined>(undefined)

  const register = async (registration: NewUser) => {
    try {
      const {message} = await api.registerBotProvider({body: registration})
      setRegistered(message)
    } catch (e) {
      console.error(e)
      throw new Error('An error occurred during registration, did you set valid email? ' +
        'Also the password must be at least 6 characters.')
    }
  };

  const classes = useStyles();
  return (
    <div className={classes.page}>
      {registered ?
        <RegistrationSuccess message={registered}/> :
        <RegistrationForm register={register}/>
      }
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
    warningBox: {
      color: 'red',
      textAlign: 'justify'
    },
    infoBox: {}
  })
);
