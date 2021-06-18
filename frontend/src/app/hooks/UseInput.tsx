import { useState } from 'react';

// https://rangle.io/blog/simplifying-controlled-inputs-with-hooks/
export default function useInput<T>(initialValue: T) {
  const [value, setValue] = useState<T>(initialValue);

  return {
    value,
    setValue,
    reset: () => setValue(initialValue),
    bind: {
      value,
      onChange: (event: { target: { value: T } }) => {
        setValue(event.target.value);
      }
    }
  };
};
