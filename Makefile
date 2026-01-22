APP_NAME = portfolio-rubenjp
build:
	mvn clean package -DskipTests
docker-build:
	docker-compose build --no-cache
run: docker-build
	docker-compose up -d
logs:
	docker-compose logs -f app
stop:
	docker-compose down
restart: stop run
clean:
	mvn clean
	docker system prune -f
test:
	mvn test
ps:
	docker-compose ps
infra:
	docker-compose up -d db redis