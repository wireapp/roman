import {useState} from 'react';

/**
 * Abstracted useState for the inputs.
 *
 * Use as:
 * const {value: someNamedValue, bind: bindValue} = useInput('')
 * <input {...bindValue}> </input>.
 */
export default function useInput<T>(
  initialValue: T,
  onChangeCallback: (value: T) => void = () => {
  }
) {
  const [value, setValue] = useState<T>(initialValue);

  return {
    value,
    setValue,
    reset: () => setValue(initialValue),
    bind: {
      value,
      onChange: (event: { target: { value: T } }) => {
        onChangeCallback(value)
        setValue(event.target.value);
      }
    }
  };
};
