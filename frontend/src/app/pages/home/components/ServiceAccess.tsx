import {Divider, Paper, TextField} from '@material-ui/core';
import {makeStyles} from '@material-ui/styles';

export interface ServiceAccessInfoProps {
  serviceCode: string
  serviceAuthentication: string
  appKey: string
}

/**
 * The component displays unmodifiable data about the service.
 */
export default function ServiceAccessInfo(accessInfo: ServiceAccessInfoProps) {
  const classes = useStyles();
  return (
    <Paper elevation={4} className={classes.paper}>
      <div className={classes.info}>
        <div className={classes.heading}>Access Information</div>
        <TextField
          label="Service Code"
          multiline
          value={accessInfo.serviceCode}
          InputProps={{
            readOnly: true
          }}
          helperText={'Service code is used to enable the service in the team settings.'}
        />
        <Divider/>

        <TextField
          label="Authentication"
          value={accessInfo.serviceAuthentication}
          InputProps={{
            readOnly: true
          }}
          helperText={'Authentication code is sent to your service in the "Authorization" header with prefix Bearer from the Roman.'}
        />
        <Divider/>

        <TextField
          label="App Key"
          multiline
          value={accessInfo.appKey}
          InputProps={{
            readOnly: true
          }}
          helperText={'The bot can use this App Key to broadcast messages to Wire conversations.'}
        />
      </div>

    </Paper>
  );
}

const useStyles = makeStyles(() => ({
  paper: {
    margin: '10px'
  },
  info: {
    display: 'flex',
    flexFlow: 'column',
    margin: '5px',
    '& > div': {
      margin: '20px'
    }
  },
  heading: {
    fontWeight: 'bold'
  }
}));
