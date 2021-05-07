# Wire Bot API Proxy
Uses [lithium](https://github.com/wireapp/lithium) to utilize Wire Bot API

### API documentation:
[swagger](https://proxy.services.wire.com/swagger#/default)

### Register as Wire Bot Developer
 - [register](https://proxy.services.wire.com/swagger#!/default/register)
```
 {
  "name": "ACME Ltd.",
  "email": "acme@email.com",
  "password": "S3cr3t!"
}
```

### Login
 - [login](https://proxy.services.wire.com/swagger#!/default/login)
```
 {
  "email": "acme@email.com",
  "password": "S3cr3t!"
}
```

### Create a service
 - [create service](https://proxy.services.wire.com/swagger#!/default/create)

```
{
  "name": "My Cool Bot",    
  "summary": "Short summary of this cool bot" // Optional
  "url": "https://my.server.com/webhook",     // Optional: Leave as null if you prefere websockets
  "avatar": "..."                             // Optional: Base64 encoded image 
}
```

Only `name` is mandatory. Specify `url` if you want to use your _Webhook_ to receive events from Wire Backend.
Leave `url` _null_ if you prefer _Websocket_. `avatar` for your bot is optional and it is `Base64` encoded `jpeg`|`png` image. If
`avatar` field is left _null_ default avatar is assigned for the Service.

After creating your Service the following json is returned:
```
{
  "email": "acme@email.com",
  "company": "ACME Ltd.",
  "service": "My Cool Bot",
  "service_code": "8d935243-828f-45d8-b52e-cdc1385334fc:d8371f5e-cd41-4528-a2bb-f3feefea160f",
  "service_authentication": "g_ZiEfOnMdVnbbpyKIWCVZIk",
  "app_key": "..."  // Needed when connecting using a websocket
}
```

Go to your _Team Settings_ page and navigate to _Services_ tab. Add this `service_code` and enable this service for your team.
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
wss://proxy.services.wire.com/await/`<app_key>`
```

### Events that are sent as HTTP `POST` to your endpoint (Webhook or Websocket)

- `conversation.bot_request` When bot is added to a conversation ( 1-1 conversation or a group)
```
{
    "type": "conversation.bot_request",
    "botId": "493ede3e-3b8c-4093-b850-3c2be8a87a95",  // Unique identifier for this bot
    "userId": "4dfc5c70-dcc8-4d9e-82be-a3cbe6661107", // User who requested this bot  
    "conversationId": "5dfc5c70-dcc8-4d9e-82be-a3cbe6661106", // ConversationId 
    "conversation": "Bot Example Conversation"                // Conversation name
    "handle": "dejan_wire",  // username of the user who requested this bot
    "locale": "en_US",       // locale of the user who requested this bot    
    "token": "..."           // Access token. Store this token so the bot can post back later
}
```

Your service must be available at the moment `bot_request` event is sent. It must respond with http code `200`.
 In case of Websocket implementation it is enough the socket is connected to the Proxy at that moment.

- `conversation.init` If your Service responded with `200` to a `bot_request` another event is sent: `init`.
`text` field contains the name of the conversation your bot is being added to.
```
{
    "type": "conversation.init",
    "botId": "216efc31-d483-4bd6-aec7-4adc2da50ca5",
    "userId": "4dfc5c70-dcc8-4d9e-82be-a3cbe6661107", // User who originally created this conversation    
    "conversationId": "5dfc5c70-dcc8-4d9e-82be-a3cbe6661106", // ConversationId 
    "token": "...",                                   // Use this token to reply to this message - valid for 20 sec
    "conversation": "Bot Example Conversation"        // Conversation name
}
```

- `conversation.new_text` When text is posted in a conversation where this bot is present
```
{
    "type": "conversation.new_text",
    "botId": "216efc31-d483-4bd6-aec7-4adc2da50ca5",
    "userId": "4dfc5c70-dcc8-4d9e-82be-a3cbe6661107",         // Author of this message      
    "conversationId": "5dfc5c70-dcc8-4d9e-82be-a3cbe6661106", // ConversationId 
    "messageId" : "baf93012-23f2-429e-b76a-b7649514da4d",     
    "token": "..."                                           // Use this token to reply to this message - valid for 20 sec
    "refMessageId" : "caf93012-23f2-429e-b76a-b7649511db2e", // reference msgId in case of a Reply, Reaction,.. (can be null)
    "text": {
        "data": Hi everybody!"
    }
}
```
- `conversation.image.preview` When new image preview is posted in a conversation where this bot is present
```
{
    "type": "conversation.image.preview",
    "botId": "216efc31-d483-4bd6-aec7-4adc2da50ca5",
    "userId": "4dfc5c70-dcc8-4d9e-82be-a3cbe6661107", 
    "messageId" : "baf93012-23f2-429e-b76a-b7649514da4d",   
    "conversationId": "5dfc5c70-dcc8-4d9e-82be-a3cbe6661106", // ConversationId 
    "token": "...",           // Use this token to reply to this message - valid for 20 sec    
    "attachment" : {
        "size": 4096,             // Size in bytes    
        "mimeType": "image/jpeg", // Mime type of this image   
    }       
}
```

- `conversation.file.preview` When a file preview is posted in a conversation where this bot is present
```
{
    "type": "conversation.file.preview",
    "botId": "216efc31-d483-4bd6-aec7-4adc2da50ca5",
    "userId": "4dfc5c70-dcc8-4d9e-82be-a3cbe6661107", 
    "messageId" : "baf93012-23f2-429e-b76a-b7649514da4d", 
    "conversationId": "5dfc5c70-dcc8-4d9e-82be-a3cbe6661106", // ConversationId 
    "token": "...",                 // Use this token to reply to this message - valid for 20 sec 
    "attachment" : { 
        "size": 4096,                   // Size in bytes    
        "mimeType": "application/pdf",  // Mime type of this file   
        "name": "plan.pdf",             // Filename
    }
}
```

- `conversation.audio.preview` When preview of an audio recording is posted in a conversation where this bot is present
```
{
    "type": "conversation.audio.preview",
    "botId": "216efc31-d483-4bd6-aec7-4adc2da50ca5",
    "userId": "4dfc5c70-dcc8-4d9e-82be-a3cbe6661107", 
    "messageId" : "baf93012-23f2-429e-b76a-b7649514da4d",      
    "conversationId": "5dfc5c70-dcc8-4d9e-82be-a3cbe6661106", // ConversationId 
    "token": "...",      // Use this token to reply to this message - valid for 20 sec  
    "attachment" : {
        "size": 4096,                           // Size in bytes    
        "mimeType": "audio/mp3",                // Mime type of this file  
        "duration": 79000,                      // Duration of the recording in mills  
        "levels": { 123, 62, 124, 255, ... },   // Loudness levels normalized to [0, 256]
        "name": "Fortunate song",               // Filename    
    }
}
```

- `conversation.asset.data` When an asset is ready to be downloaded/forwarded
```
{
    "type": "conversation.asset.data",
    "botId": "216efc31-d483-4bd6-aec7-4adc2da50ca5",
    "userId": "4dfc5c70-dcc8-4d9e-82be-a3cbe6661107", 
    "messageId" : "baf93012-23f2-429e-b76a-b7649514da4d",      
    "conversationId": "5dfc5c70-dcc8-4d9e-82be-a3cbe6661106", // ConversationId 
    "token": "...",      // Use this token to reply to this message - valid for 20 sec 
    "attachment" : {
        "size": 4096,                          // Size in bytes    
        "mimeType": "audio/mp3",               // Mime type of this file  
        "duration": 79000,                     // Duration of the recording in mills  
        "levels": { 123, 62, 124, 255, ... },  // Loudness levels normalized to [0, 256]
        "name": "Fortunate song",              // Filename  
        "meta" : { 
                "assetId": "3-cef231a2-23f2-429e-b76a-b7649594d3fe",
                "assetToken": "...",          // Optional
                "sha256": "...",              // Base64 encoded SHA256 digest of the file
                "otrKey": "..."               // Base64 encoded otr key used to encrypt the file
        }
    }
}
```

- `conversation.poll.action` When the user clicks the button in the Poll

```
{
  "botId" : "11b040df-7335-462e-bf93-c7a5adaa7e79",
  "userId" : "2e06e56f-7e99-41e9-b3ba-185669bd52c1",
  "messageId" : "7d9badd8-11ad-4f96-b214-6526dc19a976",
  "type" : "conversation.poll.action",
  "token" : "eyJhbGciOiJIUzM4NCJ9...",
  "poll" : {
    "id" : "24166f23-3477-4f2f-a7ca-44863d456fc8",
    "offset" : 1
  }
}
```
### Posting back to Wire conversation

If the event contains `token` field this `token` can be used to respond to this event by sending `Outgoing Message` like:

Example:
```
POST https://proxy.services.wire.com/conversation -d '{"type": "text", "text": {"data": "Hello!"} }' \
-H'Authorization: Bearer eyJhbGciOiJIUyPjcKUGUXXD_AXWVKTMI...'
```

In order to post text or an image as a bot into Wire conversation you need to send a `POST` request to `/conversation`
You must also specify the HTTP header as `Authorization:Bearer <token>` where `token` was obtained in `init` or other events
 like: `new_text` or `new_image`.

_Outgoing Message_ can be of 4 types:
- **Text message**
```
{
    "type": "text",
    "text": { 
      "data": "Hello!"
    }
}
```

- **Image message**
```
{
    "type": "attachment",
    "attachment": {  "mimeType" : "image/jpeg", "data" : "..." } 
}     
```

- **Create Poll message** - To create new Poll
```
{
  "type" : "poll",
  "text" : {
    "data" : "This is a poll"
  },
  "poll" : {
    "id" : "88d0dcc1-1e27-4bab-9416-a736ae4b6a3e",
    "type" : "create",
    "buttons" : [ "First", "Second" ]
  }
}
```   

- **Poll Action Confirmation** - To confirm the Poll Answer was recorded
```
{
  "type" : "poll",
  "poll" : {       
    "id" : "24166f23-3477-4f2f-a7ca-44863d456fc8",
    "type" : "confirmation",
    "offset" : 1,
    "userId" : "2e06e56f-7e99-41e9-b3ba-185669bd52c1"
  }
}
```
Full description: https://proxy.services.wire.com/swagger#!/default/post

**Note:** `token` that comes with `conversation.init` events is _lifelong_. It should be stored for later usage. `token`
 that comes with other event types has lifespan of 20 seconds.

### Bot Examples
- Echo bot in Java: https://github.com/dkovacevic/demo-proxy
- Poll bot in Kotlin: https://github.com/wireapp/poll-bot

## Build docker image from source code
docker build -t $DOCKER_USERNAME/roman:latest .

## Example of Docker run command
```
docker run \     
-e LD_LIBRARY_PATH='/opt/wire/lib' \
-e APP_KEY='this_is_some_long_key' \  
-e PROXY_DOMAIN='https://myproxy.mydomain.com' \  
-e WIRE_API_HOST='https://prod-nginz-https.wire.com' \  
-e DB_URL='jdbc:postgresql://docker.for.mac.localhost/roman' \
-e DB_USER='postgres' \ 
-e DB_PASSWORD='secret' \
-p 80:8080 \
--name roman --rm $DOCKER_USERNAME/roman:latest
```                          

## Environment variables:

```         
LOG_LEVEL       # ERROR, WARN, INFO, DEBUG. INFO by default 
APP_KEY         # 32 alphanumeric key used to generate tokens 
PROXY_DOMAIN    # Domain where your proxy will be exposed 
WIRE_API_HOST   # Wire Backed API URL. `https://prod-nginz-https.wire.com` by default 
DB_URL          # Postgres URL. format: jdbc:postgresql://<HOST>:<PORT>/<DB_NAME>  
DB_USER         # Postgres user. null by defaul
DB_PASSWORD     # Postgres user's password. null by defaul  
LD_LIBRARY_PATH # Runtime libraries are here
```
