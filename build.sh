#!/usr/bin/env bash
#mvn package -DskipTests=true -Dmaven.javadoc.skip=true
docker build -t $DOCKER_USERNAME/roman:1.2.0 .
docker push $DOCKER_USERNAME/roman
kubectl delete pod -l name=roman -n prod
kubectl get pods -l name=roman -n prod
