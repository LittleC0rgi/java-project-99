setup:
	./gradlew wrapper --gradle-version 9.2.1
	./gradlew build

app:
	./gradlew bootRun --args='--spring.profiles.active=dev'

lint:
	./gradlew spotlessApply

test:
	./gradlew test

build:
	./gradle --no-daemon

docker-build:
	docker build --no-cache -t java-project-99 .
