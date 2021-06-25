import {makeStyles} from '@material-ui/styles';
import {Button, CircularProgress, Paper, TextField} from '@material-ui/core';
import {useState} from 'react';
import {useAuthContext} from '../../../hooks/UseAuth';
import {ServiceInformation} from "../../../generated";
import useInput from "../../../hooks/UseInput";

export interface ServiceInfoProps {
  name: string
  webhook: string | undefined
  setService: (data: ServiceInformation | undefined) => void
}

/**
 * Editable part of the service data.
 */
export default function ServiceInfo({name, webhook, setService}: ServiceInfoProps) {
  const {api} = useAuthContext();
  const [status, setStatus] = useState<'idle' | 'pending'>('idle');

  const {value: serviceName, reset: resetServiceName, bind: bindServiceName} = useInput(name)
  const {value: serviceWebHook, reset: resetWebHook, bind: bindWebHook} = useInput(webhook)

  const handleSubmit = (e: any) => {
    e.preventDefault();

    setStatus('pending'); // set pending status to display circle
    api.updateService({body: {url: serviceWebHook, name: serviceName}})
      .then((r) => setService(r))
      .then(() => setStatus('idle')) // todo maybe show some modal with OK
      .catch(e => {
        console.log(e); // todo better error handling would be nice
        setService(undefined);
        setStatus('idle');
      });
  };

  const handleReset = (e: any) => {
    e.preventDefault();
    resetServiceName()
    resetWebHook()
  };

  const dataNotChanged = serviceName === name && serviceWebHook === webhook;
  const statusPending = status === 'pending'
  const classes = useStyles();
  return (
    <Paper elevation={4} className={classes.paper}>
      <div className={classes.info}>
        <div className={classes.heading}>Service Information</div>

        <form className={classes.info} noValidate autoComplete="off">
          <TextField id="serviceName"
                     label="Service Name"
                     disabled={statusPending}
                     helperText={'Name of the service as displayed in Wire.'}
                     {...bindServiceName}
          />
          <TextField id="webHook"
                     label="Webhook"
                     disabled={statusPending}
                     helperText={'URL which is called by Roman to send the message to the bot. '}
                     {...bindWebHook}
          />
          <div className={classes.buttons}>
            <Button variant="contained"
                    type="submit"
                    onClick={handleReset}
                    color="secondary"
                    disabled={statusPending || dataNotChanged}>
              <span>Reset</span>
            </Button>

            <Button variant="contained"
                    type="submit"
                    onClick={handleSubmit}
                    disabled={statusPending || dataNotChanged}>
              {statusPending
                ? <CircularProgress size={'1.5rem'}/>
                : <span>Update & Save</span>
              }
            </Button>
          </div>
        </form>
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
  buttons: {
    display: 'flex',
    justifyContent: 'space-evenly',
    '& > button': {
      width: '19ch'
    }
  },
  heading: {
    fontWeight: 'bold'
  }
}));
