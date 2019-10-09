#!/usr/bin/env bash
docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD
mvn package -DskipTests=true -Dmaven.javadoc.skip=true
docker build -t $DOCKER_USERNAME/roman:1.1.0 .
docker push $DOCKER_USERNAME/roman
