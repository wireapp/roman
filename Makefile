docker-run-tests:
	./test.sh

db:
	docker-compose up -d db

docker-build:
	docker build -t eu.gcr.io/wire-bot/roman .
