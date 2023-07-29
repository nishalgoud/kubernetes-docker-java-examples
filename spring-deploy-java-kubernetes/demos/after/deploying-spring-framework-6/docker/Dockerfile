FROM eclipse-temurin:17-jre-alpine

RUN mkdir /application
WORKDIR /application

COPY target/spring-deploy-1.0.0.jar ./app.jar

RUN addgroup --system spring
RUN adduser -S -s /bin/false -G spring spring
RUN chown -R spring:spring /application

USER spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "./app.jar"]