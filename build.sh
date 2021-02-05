#!/usr/bin/env bash
#mvn package -DskipTests=true -Dmaven.javadoc.skip=true
docker build -t eu.gcr.io/wire-bot/roman:staging .
docker push eu.gcr.io/wire-bot/roman
kubectl delete pod -l name=roman -n staging
kubectl get pods -l name=roman -n staging

