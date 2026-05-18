GIT_VERSION:=$(shell git log --format="%h" -n 1)
MVN_VERSION:=$(shell ./mvnw -q -Dexec.executable=echo -Dexec.args='$${project.version}' --non-recursive exec:exec)
VERSION=$(MVN_VERSION)-$(GIT_VERSION)

all: build image deploy
local: build_local run_local

build:
	./mvnw clean install -P docker -DskipTests -f pom.xml

build_local:
	./mvnw clean install -P local -DskipTests -f pom.xml

run_local:
	./mvnw tomcat7:run -f pom.xml

image:
	docker build -t outertuning:$(VERSION) .
	docker tag outertuning:$(VERSION) outertuning:latest

deploy:
	chmod 777 ./docker-compose/tpch_load_data_test/load.sh
	docker compose -f ./docker-compose/docker-compose.yml up -d

stop:
	docker compose -f ./docker-compose/docker-compose.yml down

build_postgres:
	./mvnw clean install -P docker-postgres -DskipTests -f pom.xml

image_postgres:
	docker build -t outertuning:postgres .

deploy_postgres:
	chmod 777 ./docker-compose/postgres/load.sh
	docker compose -f ./docker-compose/docker-compose.yml up -d postgres

stop_postgres:
	docker compose -f ./docker-compose/docker-compose.yml stop postgres

build_postgres_tpcc:
	./mvnw clean install -P docker-postgres-tpcc -DskipTests -f pom.xml

image_postgres_tpcc:
	docker build -t outertuning:postgres-tpcc .

deploy_postgres_tpcc:
	docker compose -f ./docker-compose/docker-compose.yml up -d --build hammerdb outertuning-postgres-tpcc

stop_postgres_tpcc:
	docker compose -f ./docker-compose/docker-compose.yml stop hammerdb outertuning-postgres-tpcc