import {useEffect, useState} from 'react';
import {useRequireAuth} from '../../hooks/UseAuth';
import {makeStyles} from '@material-ui/styles';
import ComponentOrPending from '../../modules/ComponentOrPending';
import Service from './components/Service';
import Header from './components/Header';
import {ServiceInformation} from "../../generated";
import {ServiceCreation} from "./components/ServiceCreation";

/**
 * Login Page, redirects to home after
 */
export default function HomePage() {
  const {api} = useRequireAuth();

  const [status, setStatus] = useState<'idle' | 'pending'>('idle');
  const [service, setService] = useState<ServiceInformation | undefined>(undefined);

  useEffect(() => {
    if (service) {
      return;
    }

    setStatus('pending');
    api.getService()
      .then(r => setService(r))
      .then(() => setStatus('idle'))
      .catch((e) => console.error(e)); // todo maybe some error handling
  }, [service, api]);

  const classes = useStyles();
  return (
    <ComponentOrPending status={status}>
      <div className={classes.page}>
        {service != null && (
          <>
            <div className={classes.information}>
              <Header/>
              {/* If service exists, show service data, otherwise show service dialog*/}
              {service.service ?
                <Service
                  serviceAccess={{
                    serviceCode: service.serviceCode,
                    serviceAuthentication: service.serviceAuthentication,
                    appKey: service.appKey
                  }}
                  info={{
                    name: service.service,
                    webhook: service.webhook,
                    setService
                  }}/>
                :
                <ServiceCreation
                  setService={setService}
                />
              }
            </div>
          </>
        )}
      </div>
    </ComponentOrPending>
  );
}

const useStyles = makeStyles(() => ({
    page: {
      display: 'flex',
      flexFlow: 'column',
      marginLeft: '10%',
      marginRight: '10%',
      marginTop: '2%',
      flexGrow: 1
    },
    information: {
      display: 'flex',
      flexFlow: 'column',
      padding: '20px',
      flexGrow: 1,
      alignSelf: 'center',
      width: '100%',
      maxWidth: '800px'
    }
  })
);
