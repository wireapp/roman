#!/usr/bin/env bash
#mvn package -DskipTests=true -Dmaven.javadoc.skip=true
docker build -t $DOCKER_USERNAME/roman:0.1.0 .
docker push $DOCKER_USERNAME/roman
#kubectl delete pod -l name=roman
#kubectl get pods -l name=roman
