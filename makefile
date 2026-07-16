setup:
	./gradlew wrapper --gradle-version 9.2.1
	./gradlew build

app:
	./gradlew bootRun --args='--spring.profiles.active=dev'

lint:
	./gradlew spotlessApply

test:
	./gradlew test

clean:
	./gradlew clean

build:
	./gradlew clean build

docker-build:
	docker build --no-cache -t java-project-99 .
