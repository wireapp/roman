import ServiceAccessInfo, {ServiceAccessInfoProps} from './ServiceAccess';
import ServiceInfo, {ServiceInfoProps} from './ServiceInfo';
import {makeStyles} from '@material-ui/styles';


export default function Service({
                                  serviceAccess,
                                  info
                                }: { serviceAccess: ServiceAccessInfoProps, info: ServiceInfoProps }) {
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
