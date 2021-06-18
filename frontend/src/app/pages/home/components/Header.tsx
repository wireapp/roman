import { makeStyles } from '@material-ui/styles';
import { Button } from '@material-ui/core';
import { useRequireAuth } from '../../../hooks/UseAuth';
import { romanBasePath } from '../../../hooks/UseApi';

export default function Header() {
  const { user, logout } = useRequireAuth();
  const styles = useStyles();
  return (
    <div className={styles.header}>
      <Button href={`${romanBasePath}/swagger`} target="_blank">Wire Roman Swagger</Button>

      <Button onClick={logout}>Logout - {user}</Button>
    </div>
  );
}

const useStyles = makeStyles(() => ({
    header: {
      display: 'flex',
      flexFlow: 'row',
      justifyContent: 'space-between'
    }
  })
);

