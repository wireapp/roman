docker-run-tests: db
	./test.sh
	docker-compose stop

db:
	docker-compose up -d db

docker-build:
	docker build -t eu.gcr.io/wire-bot/roman .
