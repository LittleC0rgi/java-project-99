app:
	./gradlew bootRun --args='--spring.profiles.active=dev'

build:
	./gradlew clean build

clean:
	./gradlew clean

coverage:
	./gradlew jacocoTestReport

docker-build:
	docker build --no-cache -t java-project-99 .

lint:
	./gradlew spotlessApply

test:
	./gradlew test

setup:
	./gradlew wrapper --gradle-version 9.2.1
	./gradlew build
