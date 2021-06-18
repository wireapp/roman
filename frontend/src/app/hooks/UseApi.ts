import { useState } from 'react';

import { Configuration, DefaultApi, RequestContext, ResponseContext } from '../generated';
import useUser from './UseUser';

export const romanBasePath = process.env.BASE_PATH ?? 'http://localhost:8080';

/**
 * Hook that gives access to DefaultApi.
 */
export default function useApi(): DefaultApi {
  const [api, setApi] = useState<DefaultApi | null>(null);
  const [, , deleteUser] = useUser();

  if (api == null) {
    const defaultApi = new DefaultApi(
      new Configuration({
        basePath: romanBasePath,
        middleware: [{
          pre: async (context: RequestContext) => {
            // TODO this is fix for wrong swagger in the Roman
            context.init.credentials = 'include';
          },
          post: async (context: ResponseContext) => {
            // delete user if the response status from BE is unauthorized
            // todo do this in more react way
            if (context.response.status === 401) {
              deleteUser(); // delete user from the storage
              window.location.replace(window.origin); // and redirect to homepage -> that will redirect to login page
            }
          }
        }]
      })
    );
    setApi(defaultApi);
    return defaultApi;
  } else {
    return api;
  }
}



