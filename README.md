# Wire Bot API Proxy

https://proxy.services.wire.com/#/login

Uses [lithium](https://github.com/wireapp/lithium) to utilize Wire Bot API.

### API documentation:

* Production - [swagger](https://proxy.services.wire.com/swagger#/default)
* Staging - [swagger](https://roman.integrations.zinfra.io/api/swagger#/default)

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
  "summary": "Short summary of this cool bot", // Optional
  "url": "https://my.server.com/webhook",     // Optional: Leave as null if you prefere websockets
  "avatar": "..."                             // Optional: Base64 encoded image 
}
```

Only `name` is mandatory. Specify `url` if you want to use your _Webhook_ to receive events from Wire Backend. Leave `url` _null_ if you
prefer _Websocket_. `avatar` for your bot is optional, and it is `Base64` encoded `jpeg`|`png` image. If `avatar` field is left _null_
default avatar is assigned for the Service.

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

Go to your _Team Settings_ page and navigate to _Services_ tab. Add this `service_code` and enable this service for your team. Now your team
members should be able to see your _Service_ when they open _people picker_ and navigate to _services_ tab.

### Webhook

In case `url` was specified when creating the service webhook will be used. All requests coming from Wire to your Service's endpoint will
have HTTP Header `Authorization` with value:
`Bearer <service_authentication>`. Make sure you verify this value in your webhook implementation. Wire will send events as `POST` HTTP
request to the `url` you specified when creating the Service. Your webhook should always return HTTP code `200` as the result.

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
    "conversationId": "5dfc5c70-dcc8-4d9e-82be-a3cbe6661106",  // ConversationId 
    "conversation": "Bot Example Conversation",                // Conversation name
    "handle": "dejan_wire",  // username of the user who requested this bot
    "locale": "en_US",       // locale of the user who requested this bot    
    "token": "..."           // Access token. Store this token so the bot can post back later
}
```

Your service must be available at the moment `bot_request` event is sent. It must respond with http code `200`. In case of Websocket
implementation it is enough the socket is connected to the Proxy at that moment.

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
    "token": "...",                                           // Use this token to reply to this message - valid for 20 sec
    "refMessageId" : "caf93012-23f2-429e-b76a-b7649511db2e", // reference msgId in case of a Reply, Reaction,.. (can be null)
    "text": {
        "data": "Hi everybody!"
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
        "size": 4096,               // Size in bytes    
        "mimeType": "image/jpeg",   // Mime type of this image 
        "width": 512,               // Resotion in pixels
        "height": 1024              // Resotion in pixels 
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
        "levels": [ 123, 62, 124, 255, ... ],   // Loudness levels normalized to [0, 255]
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
    "messageId" : "baf93012-23f2-429e-b76a-b7649514da4d",     // MessageID the same as the one from the Preview! 
    "conversationId": "5dfc5c70-dcc8-4d9e-82be-a3cbe6661106", // ConversationId 
    "token": "...",      // Use this token to reply to this message - valid for 20 sec 
    "attachment" : {
        "size": 4096,                          // Size in bytes    
        "mimeType": "audio/mp3",               // Mime type of this file  
        "duration": 79000,                     // Duration of the recording in mills  
        "levels": [ 123, 62, 124, 255, ... ],  // Loudness levels normalized to [0, 255]
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
curl -X POST https://proxy.services.wire.com/api/conversation -d '{"type": "text", "text": {"data": "Hello!"} }' \
-H'Authorization: Bearer eyJhbGciOiJIUyPjcKUGUXXD_AXWVKTMI...'
```

In order to post text, or an image as a bot into Wire conversation you need to send a `POST` request to `/conversation`
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
- Echo bot in Typescript: https://github.com/wireapp/echo-bot-roman-js
- Echo bot in Python: https://github.com/wireapp/echo-bot-roman
- Poll bot in Kotlin: https://github.com/wireapp/poll-bot

## Running Roman

The best way how to run Roman is to use Docker, another option is to run the Roman on native JVM.

## Requirements

- In order to actually being able to connect to the Wire backend, Roman's endpoints needs to run on HTTPS.
- You need a PostgreSQL instance with an empty database and credentials.
- In order to run it as a Docker container, you need to have Docker installed.
- In order to run it natively on JVM, you need to have JVM 11 installed + all necessary libraries
  for [Cryptobox4j](https://github.com/wireapp/cryptobox4j).

### Configuration

Almost all necessary variables and configurations are located in the [roman.yaml](roman.yaml). Following environment variables should be
set.

```bash          
APP_KEY                 # 32 alphanumeric key used to generate tokens. 
PROXY_DOMAIN            # Domain where your proxy will be exposed.
ROMAN_PUB_KEY_BASE64    # Public key of the HTTPS certificate encoded in base64, read further how to obtain it.
WIRE_API_HOST           # Wire Backed API URL. Set `https://prod-nginz-https.wire.com` for production, `https://staging-nginz-https.zinfra.io` to staging. 
DB_URL                  # Postgres URL. format: jdbc:postgresql://<HOST>:<PORT>/<DB_NAME>  
DB_USER                 # Postgres user.
DB_PASSWORD             # Postgres user's password.  
```

Optional environment variables:

```bash
APPENDER_TYPE=json-console   # to enable logging to JSON 
```

#### Getting the ROMAN_PUB_KEY_BASE64

```bash
# set env variable host to the HOST name of your Roman instance (without protocol)
export host="roman.integrations.zinfra.io"
# obtain the public key - this command will "freeze" and produce no input, simply pres any valid character (for example "s") and hit enter
# it should continue in the execution
openssl s_client -showcerts -servername "${host}" -connect "${host}":443 2>/dev/null | openssl x509 -inform pem -pubkey -noout > pubkey.pem
# now the certificate is stored in "pubkey.pem", to use it as an environment variable, you need to convert it to base64
cat pubkey.pem | base64
# take the output of the previous command and set it as ROMAN_PUB_KEY_BASE64 env
```

#### Generating APP_KEY

```bash
openssl rand -hex 32
```

### Docker

We provide [Dockerfile](Dockerfile) and the
prepared [runtime image](https://github.com/wireapp/cryptobox4j/blob/master/dockerfiles/Dockerfile.runtime) -
[wirebot/runtime](https://hub.docker.com/r/wirebot/runtime). We don't provide the whole Roman docker image, but feel free to build one from
the code, all necessary files are present in this repository.

#### Build docker image from source code

```bash
# pull latest Wire Bots runtime
docker pull wirebot/runtime
# build image
docker build -t roman:latest .
```

#### Example of Docker run command on local machine (without HTTPS)

In order to run the Roman locally, to test the proxy itself (not sending data to Wire backend) one do not need to specify the HTTPS
certificate and run following command:

```bash
# assuming there's a PostgreSQL instance running on IP address 192.168.1.2:5432
# with database "roman" and user "postgres" with password "secret"

docker run \     
-e APP_KEY='this_is_some_long_key_normaly_randomly_generated_to_sign_JWTs' \  
-e PROXY_DOMAIN='http://localhost:8080' \  
-e WIRE_API_HOST='https://staging-nginz-https.zinfra.io' \  
-e DB_URL='jdbc:postgresql://192.168.1.2:5432/roman' \ 
-e DB_USER='postgres' \ 
-e DB_PASSWORD='secret' \ 
-p 8080:8080 \ 
--name roman --rm roman:latest
```

#### Example with docker-compose (without HTTPS)

We include [docker-compose.yml](docker-compose.yml) file to run the testing instance of Roman locally using Docker Compose. It includes all
necessary variables and PostgreSQL instance, to get the testing instance up and running. Simply execute:

```bash
docker-compose -f docker-compose.yml up
```

#### Production deployment

In order to run the Roman in the production, one needs to have an HTTPS and to set the `ROMAN_PUB_KEY_BASE64` as well as `PROXY_DOMAIN`
env variables. See [Configuration section](#configuration) how to obtain them.

### Native JVM

As previously mentioned, Wire recommends running the Roman as a docker container. However, you can run it natively on the JVM as well.
Please note that Roman requires JVM >= 11. To run it natively, one needs to install [Cryptobox4j](https://github.com/wireapp/cryptobox4j)
and other cryptographic libraries. You can use
[Docker Build Image](https://github.com/wireapp/cryptobox4j/blob/master/dockerfiles/Dockerfile.cryptobox)
as an inspiration what needs to be installed and what environment variables need to be set to get the Cryptobox working.

Also, don't forget to read the [Configuration section](#configuration) and set all necessary environment variables for the Roman itselgf.

First, it is necessary to build the application:

```bash
# Maven and JVM 11 is required
mvn package -DskipTests
```

Then to run it like that:

```bash
# JVM 11 required
java -jar target/roman.jar server roman.yaml
```

## Simple Guide to Roman Deployment

The previous lines should give you all necessary material you need how to deploy the Roman in multiple environment and how to set everything
up. Even though Wire runs Roman in cloud and uses Kubernetes setup, we decided to provide as simple guide as possible to deploy your own
Roman using just a `docker-compose`. The following lines provides specific and opinionated simple guide, that requires just few basic
things:

- a machine with Docker, Docker Compose and OpenSSL installed
- the machine has a public IP address and DNS record pointing to that IP address
- *(optional)* install `jq` in order to browse and search in logs

In this example we take the DNS as `roman.example.com`, when deploying, change this value to your own domain. In order to obtain the
certificate, we will use [Traefik](https://traefik.io/) edge router and [Let's Encrypt](https://letsencrypt.org/).

### Step by step

1. Clone this repository

```bash
git clone git@github.com:wireapp/roman.git
```

2. Set the correct DNS in the [docker-compose.prod.yml](docker-compose.prod.yml) - replace `roman.example.com` with your own and replace
   the `developers@example.com` email address with our own email.

3. Create `.env.prod` file that will contain all necessary environmental variables.

```bash
touch .env.prod
echo "POSTGRES_DB=roman" >> .env.prod # set the database name
echo "POSTGRES_USER=roman" >> .env.prod # set the database username
echo "POSTGRES_PASSWORD=$(openssl rand -hex 16)" >> .env.prod # set the user's password
echo "APP_KEY=$(openssl rand -hex 32)" >> .env.prod # generate key for signing the JWTs
echo "PROXY_DOMAIN=https://roman.example.com" >> .env.prod # set the proxy domain, replace with your own
echo "WIRE_API_HOST=https://staging-nginz-https.zinfra.io" >> .env.prod # set the Wire backend URL, replace with your own or https://prod-nginz-https.wire.com
echo "APPENDER_TYPE=json-console" >> .env.prod # enable logging to JSON
# at this point we need to set the certificate public key, but we don't have it yet, so we create random base64 encoded string
# as temporary certificate, which we will replace once we have the real certificate
echo "ROMAN_PUB_KEY_BASE64=$(openssl rand -hex 16 | base64)" >> .env.prod
```

4. Start everything up and obtain the certificate.

```bash
docker-compose -f docker-compose.prod.yml --env-file .env.prod up --build -d
```

5. Check the logs
    * proxy - `docker-compose -f docker-compose.prod.yml logs proxy` - should show some noise about certificate and routes registration
    * Roman - `docker-compose -f docker-compose.prod.yml logs roman` - should show normal starting procedure and no errors
    * with Roman, you can pipe logs data to `jq` (if installed), that way you will see nice and formatted JSONs instead of just lines.

6. Give it some time to obtain necessary certificates - around 10 minutes should be fine. Then try to access the `https://roman.example.com`
   to see whether the HTTPS works as expected. If yes, proceed, if no troubleshoot with Traefik proxy.
7. Now you need to download real public key and encode it in base64 -
   see [Getting the ROMAN_PUB_KEY_BASE64](#getting-the-roman_pub_key_base64)
8. Once you have the `ROMAN_PUB_KEY_BASE64`, replace it in `.env.prod`.
9. Restart whole stack.

```bash
# stop all services
docker-compose -f docker-compose.prod.yml stop
# delete old containers
docker-compose -f docker-compose.prod.yml rm -f
# start the stack again now with correct public key in Roman
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d
```

10. All set! You can go to `https://roman.example.com/swagger` and start using Roman.

## Comprehensive tutorial how to onboard new bot

Step-by-step guide, how to create a simple bot for Roman - [onboarding.md](docs/onboarding.md).
