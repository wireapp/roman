import {useRequireAuth} from "../../../hooks/UseAuth";
import {ServiceInformation} from "../../../generated";
import {makeStyles} from "@material-ui/styles";
import {useState} from "react";
import {Button, CircularProgress, Paper, TextField} from "@material-ui/core";
import useInput from "../../../hooks/UseInput";

interface Props {
  setService: (data: ServiceInformation | undefined) => void
}

/**
 * Form which allows user to register a service.
 */
export function ServiceCreation({setService}: Props) {
  const {api} = useRequireAuth();

  const [status, setStatus] = useState<'idle' | 'pending'>('idle');

  const {value: name, bind: bindName} = useInput('');
  const {value: url, bind: bindUrl} = useInput('');
  // TODO add support for the avatar
  const {value: summary, bind: bindSummary} = useInput('');

  const handleSubmit = (e: any) => {
    e.preventDefault();

    setStatus('pending'); // set pending status to display circle
    api.createNewService({body: {name, url, summary}})
      .then((r) => setService(r))
      .catch(e => {
        console.log(e); // todo better error handling would be nice
        setStatus('idle')
        setService(undefined);
      });
  };

  const classes = useStyles();
  const pendingStatus = status === 'pending'
  return (
    <Paper elevation={4} className={classes.paper}>
      <div className={classes.info}>
        <div className={classes.heading}>Create Service</div>
        {/* TODO maybe add some explanation what is going on */}
        <form className={classes.info} noValidate autoComplete="off">
          <TextField id="serviceName"
                     label="Service Name"
                     disabled={pendingStatus}
                     helperText={'Name of the service as displayed in Wire.'}
                     {...bindName}
          />
          <TextField id="webHook"
                     label="Webhook"
                     disabled={pendingStatus}
                     helperText={'URL which is called by Roman to send the message to the bot. '}
                     {...bindUrl}
          />
          <TextField id="summary"
                     label="Summary"
                     multiline
                     disabled={pendingStatus}
                     helperText={'Summary describing the bot in the Wire.'}
                     {...bindSummary}
          />
          <div className={classes.buttons}>
            <Button variant="contained"
                    type="submit"
                    onClick={handleSubmit}
                    disabled={pendingStatus || !name}>
              {pendingStatus
                ? <CircularProgress size={'1.5rem'}/>
                : <>Create Service</>
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
  buttons: {},
  heading: {
    fontWeight: 'bold'
  }
}));
