import {makeStyles} from "@material-ui/styles";
import {Button, Paper} from "@material-ui/core";
import {routes} from "../../../modules/Routing";

interface Props {
  message: string
}

/**
 * Component that displays page that informs user about the successful registration.
 */
export default function RegistrationSuccess({message}: Props) {
  const classes = useStyles()
  return (
    <div>
      <Paper className={classes.paper} elevation={4}>
        <div className={classes.messageText}>{message}</div>

        <div>Please confirm the registration mail and then log in.</div>
      </Paper>

      <Button
        className={classes.loginButton}
        variant="contained"
        href={routes.login}>
        Login
      </Button>
    </div>
  )
}

const useStyles = makeStyles(() => ({
  container: {
    display: 'flex',
    flexFlow: 'column',
    alignContent: 'center',
    justifyContent: 'center'
  },
  paper: {
    padding: '30px',
    margin: '20px'
  },
  loginButton: {
    margin: '20px',
    maxWidth: '25ch',
    padding: '10px'
  },
  messageText: {
    padding: '10px'
  }
  })
);
