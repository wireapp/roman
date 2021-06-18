import React from 'react';
import Routing from './modules/Routing';
import { makeStyles } from '@material-ui/styles';


export default function App() {
  const classes = useStyles();
  return (
    <div className={classes.root}>
      <Routing/>
    </div>
  );
}
const useStyles = makeStyles({
  root: {
    display: 'flex',
    alignContent: 'center',
    justifyContent: 'center',
    textAlign: 'center',
    width: '100vw',
    height: '100vh',
    overflowX: 'hidden'
  }
});
