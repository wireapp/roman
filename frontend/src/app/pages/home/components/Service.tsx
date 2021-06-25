import ServiceAccessInfo, {ServiceAccessInfoProps} from './ServiceAccess';
import ServiceInfo, {ServiceInfoProps} from './ServiceInfo';
import {makeStyles} from '@material-ui/styles';

interface Props {
  serviceAccess: ServiceAccessInfoProps,
  info: ServiceInfoProps
}

/**
 * Displays data about the service.
 */
export default function Service({serviceAccess, info}: Props) {
  const classes = useStyles();
  return (
    <div className={classes.service}>
      <ServiceInfo {...info}/>
      <ServiceAccessInfo {...serviceAccess}/>
    </div>
  );
}

const useStyles = makeStyles(() => ({
  service: {
    display: 'flex',
    flexFlow: 'column'
  },
  heading: {
    fontSize: 'large',
    fontWeight: 'bold'
  }
}));
