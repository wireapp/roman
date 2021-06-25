import {makeStyles} from "@material-ui/styles";
import {Button, Paper} from "@material-ui/core";
import {routes} from "../../../modules/Routing";

interface Props {
  message: string
}

export default function RegistrationSuccess({message}: Props) {
  const classes = useStyles()
  return (
    <>
      <Paper className={classes.container}>
        <div className={classes.messageText}>{message}</div>

        <div>Please confirm the registration mail and then log in.</div>
      </Paper>

      <Button
        className={classes.loginButton}
        variant="outlined"
        href={routes.login}>
        Login
      </Button>
    </>
  )
}

const useStyles = makeStyles(() => ({
    container: {
      padding: '10px',
      margin: '20px'
    },
    loginButton: {
      margin: '20px'
    },
    messageText: {
      padding: '10px'
    }
  })
);
