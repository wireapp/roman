#!/usr/bin/env bash
docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD
mvn package -DskipTests=true -Dmaven.javadoc.skip=true
docker build -t $DOCKER_USERNAME/roman:latest .
docker push $DOCKER_USERNAME/roman
kubectl delete pod -l name=roman
kubectl get pods -l name=roman
