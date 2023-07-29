# Deploying Spring Framework 6 Applications with Docker

## 1. Package Application to Docker

Use Maven to package the application to a FAT JAR
```
mvn clean package
```

Inspect the JAR
```
jar -tf target/deploy-spring-1.0.0.jar
```

Test the application
```
java -jar target/deploy-spring-1.0.0.jar
```

Copy the following to a new Dockerfile
```
FROM eclipse-temurin:17-jre-alpine

COPY target/spring-deploy-1.0.0.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build Docker Image
```
docker build -t spring-deploy:1.0 .
```

Run the application using Docker
```
docker run spring-deploy:1.0
```

## 3. Deploy MySQL Server
Deploy MySQL Server using Docker
```
docker run --name mysql \
-e MYSQL_ROOT_PASSWORD=password \
-e MYSQL_DATABASE=pluralsight \
-p 3306:3306 \
mysql:8.0.32
```

In another tab, run the application using Docker
```
docker run spring-deploy:1.0
```

Create Docker Network
```
docker network create spring-network
```

Run MySQL Server with Docker Network
```
docker run --network spring-network \
--name mysql \
-e MYSQL_ROOT_PASSWORD=password \
-e MYSQL_DATABASE=pluralsight \
-p 3306:3306 \
mysql:8.0.32
```

In another tab, run the application with Docker Network 
```
docker run --network spring-network spring-deploy:1.0
```

Change the following properties and rebuild the Docker Image
```
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/pluralsight
flyway:
  url: jdbc:mysql://mysql:3306/pluralsight
```

## 3. Dockerfile Best Practices
Try reaching the application with curl
```
curl http://localhost:8080/books
```

[Optional]Expose the port via `Dockerfile`
```
EXPOSE 8080
```

Expose the port via `docker run`
```
docker run -d --rm \
--name spring-deploy \
--network spring-network \
-p 8080:8080 \
spring-deploy:1.0
```

Add a WORKDIR
```
RUN mkdir /application
WORKDIR /application
COPY target/spring-deploy-1.0.0.jar ./app.jar
ENTRYPOINT ["java", "-jar", "./app.jar"]
```

Stop the current container, rebuild the Docker image ,and run the Application
```
docker stop spring-deploy 

docker build -t spring-deploy:1.0 .

docker run -d --rm \
--name spring-deploy \
--network spring-network \
-p 8080:8080 \
spring-deploy:1.0
```

Investigate the changes by diving inside the container
```
docker exec -it spring-deploy /bin/bash

pwd
```

Add a non-root user
```
RUN addgroup --system spring
RUN adduser -S -s /bin/false -G spring spring
RUN chown -R spring:spring /application

USER spring
```

Investigate the changes by diving inside the container
```
docker exec -it spring-deploy /bin/bash

whoami
```

Add Healthcheck
```
HEALTHCHECK --interval=30s --timeout=3s --retries=1 CMD wget -qO- http://localhost:8080/actuator/health/ | grep UP || exit 1
```

Check the Health status
```
docker ps
```

## 4. Configuring via Environment Variables

Persist a new book
```
curl -X POST http://localhost:8080/books/persist \
   -H "Content-Type: application/json" \
   -d '{"title": "Deploying Spring Framework 6 Applications Playbook", "authorName": "Bogdan Sucaciu"}'
```

Investigate the persisted file by diving inside the container
```
docker exec -it spring-deploy /bin/bash

cat /tmp/books.txt
```

Stop the container
```
docker stop spring-deploy
```

Run the application using environment variables
```
docker run -d --rm \
--name spring-deploy \
--network spring-network \
-e BOOK_PERSISTENCE_FILE_PATH= '/tmp/persisted-books.txt' \
-p 8080:8080 \
spring-deploy:1.0
```

Investigate the persisted file by diving inside the container
```
docker exec -it spring-deploy /bin/bash

cat /tmp/persisted-books.txt
```

What happened with the previous file?
```
cat /tmp/books.txt
```

## 5. Persisting data

Create a Docker volume
```
docker volume create books-volume
```

Mount the volume while starting the application
```
docker run -d --rm \
--name spring-deploy \
--network spring-network \
-e BOOK_PERSISTENCE_FILE_PATH= '/tmp/persisted-books.txt' \
--mount source=books-volume,target=/tmp \
-p 8080:8080 \
spring-deploy:1.0
```

Stop the application and clean up the volume
```
docker stop spring-deploy

docker volume delete books-volume
```

Start the application while generating a volume on the fly
```
docker run -d --rm \
--name spring-deploy \
--network spring-network \
-e BOOK_PERSISTENCE_FILE_PATH= '/tmp/persisted-books.txt' \
-v /tmp/spring-deploy:/tmp \
-p 8080:8080 \
spring-deploy:1.0
```

## 6. Wrap everything with Docker Compose

Create a new `docker-compose.yml` file and paste the following content
```
version: "3.8"

services:
  spring-deploy:
    image: spring-deploy:1.0
    ports:
      - 8080:8080
    environment:
     - BOOK_PERSISTENCE_FILE_PATH=/tmp/persisted-books.txt
    volumes:
      - /tmp/spring-deploy:/tmp
  mysql:
    image: mysql:8.0.32
    environment:
      - MYSQL_ROOT_PASSWORD=password
      - MYSQL_DATABASE=pluralsight
    ports:
      - 3306:3306
```

```
docker compose up
```

```
docker compose up -d
```

Inspect the application logs
```
docker logs deploying-spring-framework-6-spring-deploy-1
```
Stop the containers
```
docker compose down
```

Add dependencies
```
    depends_on:
      - mysql
```

Provide container names
```
  spring-deploy:
    container_name: spring-deploy
...
  mysql:
    container_name: mysql
```

Create an environment file called `.env` and paste the following content
```
BOOK_PERSISTENCE_FILE_PATH=/tmp/persisted-books.txt

MYSQL_ROOT_PASSWORD=password
MYSQL_DATABASE=pluralsight
```

Replace the hard-coded environment variables inside the `docker-compose.yml` file
```
   environment:
      - BOOK_PERSISTENCE_FILE_PATH=${BOOK_PERSISTENCE_FILE_PATH}
...
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
      - MYSQL_DATABASE=${MYSQL_DATABASE}
```

Run `docker compose` with an env file
```
docker compose --env-file=.env up
```

## Annex: Helper `curl` commands

Create a new book
```
curl -X POST http://localhost:8080/books \
   -H "Content-Type: application/json" \
   -d '{"title": "Deploying Spring Framework 6 Applications Playbook", "authorName": "Bogdan Sucaciu"}'
```

Get all books
```
curl http://localhost:8080/books
```

Get book by id
```
curl http://localhost:8080/books/1
```

Delete book
```
curl -X DELETE http://localhost:8080/books/1
```

Persist a new book
```
curl -X POST http://localhost:8080/books/persist \
   -H "Content-Type: application/json" \
   -d '{"title": "Deploying Spring Framework 6 Applications Playbook", "authorName": "Bogdan Sucaciu"}'
```
