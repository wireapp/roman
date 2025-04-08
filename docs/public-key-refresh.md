# Update bot public key

When registering a service with the Wire backend, the service needs to specify with certificate it will use for incoming TLS connections. The Wire backend will verify that the certificates match when making TLS requests to the service, and otherwise abort any connection if it doesn’t match.

Periodically, the certificates need to be updated.

## Requirements

- Wire Provider account.
- Roman instance *(you can use public Roman running [here](https://proxy.services.wire.com/))*, in this guide we will be
  using [internal staging Roman](https://roman.integrations.zinfra.io/swagger) with URL `https://roman.integrations.zinfra.io`.
    - Please note that Wire Staging environment is accessible only for the Wire development team and if you create the bot here, you won't
      be able to use it unless you have access to that environment.
- A bot already fully onboarded and running. If you don't have one, please check the [onboarding guide](onboarding.md).

You will need to make some API calls, use your favourite tool for that, e.g. [Postman](https://www.postman.com/) or [curl](https://curl.se/).
The following examples will use `curl` commands, but you can easily adapt them to your tool of choice.

## Commands
```bash
curl --request POST \
  --url https://prod-nginz-https.wire.com/v6/provider/login \
  --header 'content-type: application/json' \
  --data '{
  "email": "$PROVIDER_EMAIL",
  "password": "$PROVIDER_PASSWORD"
}'
```
Then take the `Set-cookie` value from the response headers and call:


```bash
curl --request GET \
--url https://prod-nginz-https.wire.com/v6/provider/services \
--header 'cookie: $COOKIE'
```
This will give you the list of services you have on the provider you have registered.

Then for each service you want to update with a new public key:
```bash
curl --request PUT \
--url https://prod-nginz-https.wire.com/v6/provider/services/${SERVICE_ID}/connection \
--header 'content-type: application/json' \
--header 'cookie: $COOKIE' \
--data '{
"password": "$PROVIDER_PASSWORD",
"public_keys": [
"-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3xtHqyZPlb0lxlnP0rNA\nJVmAjB1Tenl11brkkKihcJNRAYrnrT/6sPX4u2lVn/aPncUTjN8omL47MBct7qYV\n1VY4a5beOyNiVL0ZjZMuh07aL9Z2A4cu67tKZrCoGttn3jpSVlqoOtwEgW+Tpgpm\nKojcRC4DDXEZTEvRoi0RLzAyWCH/8hwWzXR7J082zmn0Ur211QVbOJN/62PAIWyj\nl5bLglp00AY5OnBHgRNwwRkBJIJLwgNm8u9+0ZplqmMGd3C/QFNngCOeRvFe+5g4\nqfO4/FOlbkM2kYFAi5KUowfG7cdMQELI+fe4v7yNsgrbMKhnIiLtDIU4wiQIRjbr\nZwIDAQAB\n-----END PUBLIC KEY-----\n"
]
```
