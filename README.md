# Wire Bot API Proxy
Uses [lithium](https://github.com/wireapp/lithium) to utilize Wire Bot API

### API documentation:
https://services.wire.com/proxy/swagger

### Register as Wire Bot Developer
 - [register](https://services.wire.com/proxy/swagger#!/default/register)

### Login
 - [login](https://services.wire.com/proxy/swagger#!/default/login)

### Create a service
 - [create service](https://services.wire.com/proxy/swagger#!/default/create)

```
{
  "name": "My Cool Bot",
  "url": "https://my.server.com/webhook",
  "avatar": "..." // Base64 encoded image 
}
```

Only `name` is mandatory. Specify `url` if you want to use your _Webhook_ to receive events from Wire Backend.
Leave `url` _null_ if you prefer _Websocket_. `avatar` for your bot is optional and it is `Base64` encoded `jpeg`|`png` image. If
`avatar` field is left _null_ default avatar is assigned for the Service.

After creating your Service the following json is returned:
```
{
  "email": "dejan@wire.com",
  "company": "ACME",
  "service": "ACME Integration",
  "service_code": "8d935243-828f-45d8-b52e-cdc1385334fc:d8371f5e-cd41-4528-a2bb-f3feefea160f",
  "service_authentication": "g_ZiEfOnMdVnbbpyKIWCVZIk",
  "app_key": "..."  // Needed when connecting using a websocket
}
```

Go to your Team Settings page and navigate to _Services_ tab. Add this `service_code` and enable this service for your team.
Now your team members should be able to see your _Service_ when they open _people picker_ and navigate to _services_ tab.

### Webhook
In case `url` was specified when creating the service webhook will be used. All requests coming from Wire to your
Service's endpoint will have HTTP Header `Authorization` with value:
 `Bearer <service_authentication>`. Make sure you verify this value in your webhook implementation.
Wire will send events as `POST` HTTP request to the `url` you specified when creating the Service.
Your webhook should always return HTTP code `200` as the result.

### Websocket
In order to receive events via _Websocket_ connect to:

```
wss://services.wire.com/proxy/await/`<app_key>`
```

### Events that are sent as HTTP `POST` to your endpoint (Webhook or Websocket)

- `bot_request`: When bot is added to a conversation ( 1-1 conversation or a group)
```
{
    "type": "conversation.bot_request",
    "botId": "493ede3e-3b8c-4093-b850-3c2be8a87a95",  // Unique identifier for this bot
    "userId": "4dfc5c70-dcc8-4d9e-82be-a3cbe6661107", // User who requested this bot
    "handle": "dejan_wire", // username of the user who requested this bot
    "locale": "en_US"       // locale of the user who requested this bot
    "token": "..."          // Use this token to make outbound requests to the Wire API server
}
```

Your service must be available at the moment `bot_request` event is sent. It must respond with http code `200`.
 In case of Websocket implementation it is enough the socket is connected to the Proxy at that moment.

- `init`: If your Service responded with `200` to a `bot_request` another event is sent: `init`.
`text` field contains the name of the conversation your bot is being added to.
```
{
    "type": "conversation.init",
    "botId": "216efc31-d483-4bd6-aec7-4adc2da50ca5",
    "userId": "4dfc5c70-dcc8-4d9e-82be-a3cbe6661107", // User who added this bot into conversation
    "token": "...",                                   // Access token. Store this token so the bot can post back later
    "text": "Bot Example Conversation"                // Conversation name
}
```

- `new_text`: When text is posted in a conversation where this bot is present
```
{
    "type": "conversation.new_text",
    "botId": "216efc31-d483-4bd6-aec7-4adc2da50ca5",
    "userId": "4dfc5c70-dcc8-4d9e-82be-a3cbe6661107", // Author of this message
    "text": "Hi everybody!",
    "token": "..."                                    // Use this token to reply to this message - valid for 20 sec
}
```
- `new_image`: When an image is posted in a conversation where this bot is present

```
{
    "type": "conversation.new_image",
    "botId": "216efc31-d483-4bd6-aec7-4adc2da50ca5",
    "userId": "4dfc5c70-dcc8-4d9e-82be-a3cbe6661107",
    "token": "...", // Use this token to reply to this message - valid for 20 sec
    "image": "..."  // Base64 encoded image
}
```

- `new_image`: When an image is posted in a conversation where this bot is present

```
{
    "type": "conversation.new_image",
    "botId": "216efc31-d483-4bd6-aec7-4adc2da50ca5",
    "userId": "4dfc5c70-dcc8-4d9e-82be-a3cbe6661107",
    "token": "...", // Use this token to reply to this message - valid for 20 sec
    "image": "..."  // Base64 encoded image
}
```
### Posting back to Wire conversation

If the event contains `token` field this `token` can be used to respond to this event by sending `Outgoing Message` like:

Example:
```
POST https://services.wire.com/proxy/conversation -d '{"type": "text", "text": "Hello!"}' \
-H'Authorization:eyJhbGciOiJIUyPjcKUGUXXD_AXWVKTMI...'
```

In order to post text or an image as a bot into Wire conversation you need to send a `POST` request to `/conversation`
You must also specify the HTTP header as `Authorization: <token>` where `token` was obtained in `init` or other events
 like: `new_text` or `new_image`.

_Outgoing Message_ can be of 2 types:
- **Text message**
```
{
    "type": "text",
    "text": "Hello!"
}
```

- **Image message**
```
{
    "type": "image",
    "image": "..." // Base64 encoded image
}
```
Full description: https://services.wire.com/proxy/swagger#!/default/post

**Note:** `token` that comes with `conversation.init` events is _lifelong_. It should be stored for later usage. `token`
 that comes with other event types has lifespan of 20 seconds.

### Bot Example
- Echo bot in Java: https://github.com/dkovacevic/demo-proxy

## Build docker image from source code
docker build -t $DOCKER_USERNAME/roman:latest .

## Example of Docker run command
```
docker run \ 
-e APP_KEY='this_is_some_long_key' \  
-e DOMAIN='https://myproxy.mydomain.com' \  
-e BACKEND='https://prod-nginz-https.wire.com' \  
-e DB_URL='jdbc:postgresql://docker.for.mac.localhost/roman' \
-e DB_USER='postgres' \ 
-e DB_PASSWORD='secret' \
-p 80:8080 \
--name roman --rm $DOCKER_USERNAME/roman:latest
```                          

## Environment variables:

```         
LOG_LEVEL      # ERROR, WARN, INFO, DEBUG. INFO by default 
APP_KEY        # 32 alphanumeric key used to generate tokens 
DOMAIN         # Domain where your proxy will be exposed 
BACKEND        # Wire Backed API URL. `https://prod-nginz-https.wire.com` by default 
DB_URL         # Postgres URL. format: jdbc:postgresql://<HOST>:<PORT>/<DB_NAME>  
DB_USER        # Postgres user. null by defaul
DB_PASSWORD    # Postgres user's password. null by defaul  
```
