CREATE TABLE Providers (
 id UUID NOT NULL,
 email VARCHAR NOT NULL PRIMARY KEY,
 hash VARCHAR NOT NULL,
 password VARCHAR NOT NULL,
 url VARCHAR,
 service_auth VARCHAR,
 service UUID,
 name VARCHAR,
 service_name VARCHAR
);

CREATE TABLE Bots (
 id UUID PRIMARY KEY,
 provider UUID NOT NULL
);