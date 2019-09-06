## Wire Bot API Proxy
It uses [lithium](https://github.com/wireapp/lithium) to utilize Wire Bot API

# API documentation:
https://services.zinfra.io/proxy/swagger

# Register as Wire Bot Developer
[register](https://services.zinfra.io/proxy/swagger#!/default/register)

# Login
[login](https://services.zinfra.io/proxy/swagger#!/default/login)

# Create a service
[create service](https://services.zinfra.io/proxy/swagger#!/default/create)
Only `name` is mandatory. Specify `url` if you want to use your _webhook_ to receive events from Wire Backend. Leave `url`
as `null` if you prefer _Webhooks_. `avatar` for your bot is optional and it is `Base64` encoded jpeg|png image. If
`avatar` filed is left as `null` default avatar is assigned for the Service.

After creating your Service the following json is returned:
```
{
  "email": "dejan@wire.com",
  "company": "ACME",
  "service": "ACME Integration",
  "service_code": "8d935243-828f-45d8-b52e-cdc1385334fc:d8371f5e-cd41-4528-a2bb-f3feefea160f",
  "service_authentication": "g_ZiEfOnMdVnbbpyKIWCVZIk",
  "app_key": "..."
}
```

Go to your Team Settings page and navigate to _Services_ tab. Add this `service_code` and enable this service for your team.
Now your team members should be able to see your _Service_ when they open _people picker_ and navigate to _services_ tab.

# Webhook
All requests coming from Wire to your Service's endpoint will have HTTP Header `Authorization` with value:
 `Bearer <service_authentication>`. Make sure you verify this value in your webhook implementation.
Wire will send events to the `url` you speficied when creating the Service. Your webhook should always return HTTP code `200`

# Websocket
In order to receive events via websocket connect to:
```
wss://services.zinfra.io/proxy/await/`<app_key>`
```

# Events that are sent from the Server to your endpoint (Webhook or Websocket)

- `bot_request`: When bot is added to a conversation ( 1-1 conversation or a group)
```
{
    "type": "conversation.bot_request",
    "botId": "493ede3e-3b8c-4093-b850-3c2be8a87a95",
    "userId": "4dfc5c70-dcc8-4d9e-82be-a3cbe6661107"
}
```

Your service must be available at the moment `bot_request` event is sent. It must respond with http code `200`. In case of Websocket implementation it is enough the socket is connected to the Proxy at that moment.

- `init`: If your Service responded with 200 to a `bot_request` another event is sent. `text` filed contains the name of the conversation your bot is being added
```
{
    "type": "conversation.init",
    "botId": "216efc31-d483-4bd6-aec7-4adc2da50ca5",
    "userId": "4dfc5c70-dcc8-4d9e-82be-a3cbe6661107",
    "token": "...",
    "text": "Bot Example Conversation"
}
```

- `new_text`: When text is posted in a conversation where this bot is present
```
{
    "type": "conversation.new_text",
    "botId": "216efc31-d483-4bd6-aec7-4adc2da50ca5",
    "userId": "4dfc5c70-dcc8-4d9e-82be-a3cbe6661107",
    "text": "Hi everybody!",
    "token": "..." // token
}
```
- `new_image`: When an image is posted in a conversation where this bot is present

```
{
    "type": "conversation.new_image",
    "botId": "216efc31-d483-4bd6-aec7-4adc2da50ca5",
    "userId": "4dfc5c70-dcc8-4d9e-82be-a3cbe6661107",
    "token": "...",
    "image": "..." // Base64 encoded image
}
```

If the event contains `token` field this `token` can be used to respond to this event by sending `Outgoing Message` like:

```
POST https://services.zinfra.io/proxy/conversation -d '{"type": "text", "text": "Hello!"}' -H'Authorization:<token>'
```

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


**Note:** `token` that comes with `conversation.init` events is _lifelong_. It should be stored for later usage. `token` that comes with other event types has lifespan of 20 seconds.

# Bot Example
[echo](https://github.com/dkovacevic/demo-proxy)