import useLocalStorage from './UseLocalStorage';

export default function useUser() {
  return useLocalStorage<string>('user');
}
