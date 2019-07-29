#!/usr/bin/env bash
docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD
mvn package -DskipTests=true -Dmaven.javadoc.skip=true
docker build -t $DOCKER_USERNAME/ealarming:latest .
docker push $DOCKER_USERNAME/ealarming
kubectl delete pod -l name=ealarming
kubectl get pods -l name=ealarming
