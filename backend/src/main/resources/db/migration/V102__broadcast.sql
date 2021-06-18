CREATE TABLE Broadcast (
 broadcast_id UUID NOT NULL,
 bot_id UUID NOT NULL,
 provider UUID NOT NULL,
 message_id UUID NOT NULL,
 message_status INTEGER NOT NULL,
 created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(broadcast_id, message_id, message_status)
);