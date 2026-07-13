setup:
	./gradlew wrapper --gradle-version 9.2.1
	./gradlew build

app:
	./gradlew bootRun --args='--spring.profiles.active=dev'
