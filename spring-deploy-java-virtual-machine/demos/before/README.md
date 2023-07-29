# Deploying Spring Framework 6 Applications to Virtual Machine

## 1. Package Application to JAR

Use Maven to package the application to a FAT JAR
```
mvn clean package
```

Inspect the JAR
```
jar -tf target/deploying-spring-framework-6-0.0.1-SNAPSHOT.jar
```

Run the application
```
java -jar target/deploying-spring-framework-6-0.0.1-SNAPSHOT.jar
```

Rename the JAR
```
<build>
   <finalName>spring-deploy-${project.version}</finalName>
</build>
```

Change the version
```
<version>1.0.0</version>
```

Package using AOT
```
mvn clean compile spring-boot:process-aot package
```

Run the application using AOT
```
java -DspringAot=true -jar target/spring-deploy-1.0.0.jar
```

## 2. Deploy the application
Try running the application
```
java -jar spring-deploy-1.0.0.jar
```

Install JRE 17
```
sudo apt install openjdk-17-jre
```

Install MySQL Server
```
sudo apt install mysql-server
```

Start MySQL Server
```
sudo systemctl start mysql.service
```

Login to MySQL CLI
```
sudo mysql
```

Change root password
```
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
```

Create default database
```
CREATE DATABASE pluralsight;
```

Exit MySQL CLI
```
exit
```

Start the application
```
java -jar spring-deploy-1.0.0.jar
```

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

## 4. JVM Tuning
Start the application
```
java -jar spring-deploy-1.0.0.jar
```

Start the application with custom JVM paramaters
```
java -Xms128m -Xmx512m -jar spring-deploy-1.0.0.jar
```

Start the application with Heap Dump capture
```
java -XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/tmp/heapdump.hprof \
-Xms128m -Xmx512m \
-jar spring-deploy-1.0.0.jar
```