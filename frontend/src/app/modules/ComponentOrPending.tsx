import { CircularProgress } from '@material-ui/core';

interface ComponentProps {
  status: 'pending' | string,
  children: JSX.Element
}

/**
 * Simple wrapper that shows progress bar if status == pending.
 */
export default function ComponentOrPending({ status, children }: ComponentProps): JSX.Element {
  return status === 'pending' ? (<CircularProgress/>) : children;
}
