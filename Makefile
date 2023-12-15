docker-run-tests: db
	trap ./test.sh EXIT
	docker-compose stop

db:
	docker-compose up -d db

docker-build:
	docker build -t eu.gcr.io/wire-bot/roman .
