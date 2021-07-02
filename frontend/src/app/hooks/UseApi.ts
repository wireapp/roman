import {Configuration, DefaultApi, RequestContext, ResponseContext} from '../generated';

// the localhost is here just as a template for local development
export const romanBasePath = process.env.REACT_APP_BASE_PATH ?? `${window.origin}${window.location.pathname}`;

const getApiPath = (path: string) => {
  const pathWithSlash = path.endsWith('/') ? '' : '/'
  return `${path}${pathWithSlash}api`
}

const api = new DefaultApi(
  new Configuration({
    basePath: getApiPath(romanBasePath),
    middleware: [{
      pre: async (context: RequestContext) => {
        // TODO this is fix for wrong swagger in the Roman
        context.init.credentials = 'include';
      },
      post: async (context: ResponseContext) => {
        // delete user if the response status from BE is unauthorized
        // todo do this in more react way
        if (context.response.status === 401) {
          localStorage.clear(); // delete user from the storage
        }
      }
    }]
  })
);

/**
 * Hook that gives access to DefaultApi.
 */
export default function useApi(): DefaultApi {
  return api
}



