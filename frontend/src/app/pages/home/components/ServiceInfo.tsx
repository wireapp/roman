import {makeStyles} from '@material-ui/styles';
import {Button, CircularProgress, Paper, TextField} from '@material-ui/core';
import {useState} from 'react';
import {useAuthContext} from '../../../hooks/UseAuth';
import {ServiceData} from '../../../types/TypeAliases';

export interface ServiceInfoProps {
  name: string
  webhook: string | undefined
  useServiceRefresh: (data: ServiceData | undefined) => void
}

/**
 * Editable part of the service data.
 */
export default function ServiceInfo(info: ServiceInfoProps) {
  const {api} = useAuthContext();

  const [serviceName, setServiceName] = useState(info.name);
  const [webHook, setWebHook] = useState(info.webhook);
  const [status, setStatus] = useState<'idle' | 'pending'>('idle');

  const handleSubmit = (e: any) => {
    e.preventDefault();

    setStatus('pending'); // set pending status to display circle
    api.updateService({body: {url: webHook, name: serviceName}})
      .then((r) => info.useServiceRefresh(r))
      .then(() => setStatus('idle')) // todo maybe show some modal with OK
      .catch(e => {
        console.log(e); // todo better error handling would be nice
        info.useServiceRefresh(undefined);
        setStatus('idle');
      });
  };

  const handleReset = (e: any) => {
    e.preventDefault();
    setServiceName(info.name);
    setWebHook(info.webhook);
  };

  const dataNotChanged = () => serviceName === info.name && webHook === info.webhook;

  const classes = useStyles();
  return (
    <Paper elevation={4} className={classes.paper}>
      <div className={classes.info}>
        <div className={classes.heading}>Service Information</div>

        <form className={classes.info} noValidate autoComplete="off">
          <TextField id="serviceName"
                     label="Service Name"
                     value={serviceName}
                     disabled={status === 'pending'}
                     onChange={e => setServiceName(e.target.value)}
                     helperText={'Name of the service as displayed in Wire.'}
          />
          <TextField id="webHook"
                     label="Webhook"
                     value={webHook}
                     disabled={status === 'pending'}
                     onChange={e => setWebHook(e.target.value)}
                     helperText={'URL which is called by Roman to send the message to the bot. '}
          />
          <div className={classes.buttons}>
            <Button variant="contained"
                    type="submit"
                    onClick={handleReset}
                    color="secondary"
                    disabled={status === 'pending' || dataNotChanged()}>
              <span>Reset</span>
            </Button>

            <Button variant="contained"
                    type="submit"
                    onClick={handleSubmit}
                    disabled={status === 'pending' || dataNotChanged()}>
              {status === 'pending'
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
