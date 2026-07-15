FROM eclipse-temurin:25-jdk

WORKDIR /backend

COPY gradle gradle
#COPY gradle.properties .
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradlew .

RUN ./gradlew --no-daemon dependencies

#COPY lombok.config .
COPY src src

RUN ./gradlew --no-daemon build

EXPOSE 8080

CMD ["java", "-jar", "build/libs/app-0.0.1-SNAPSHOT.jar"]
