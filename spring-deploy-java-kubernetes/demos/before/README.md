# Deploying Spring Framework 6 Applications with Kubernetes

## 1. Package Application to Docker

Use Maven to package the application to a FAT JAR
```
mvn clean package
```

Test the application
```
java -jar target/spring-deploy-1.0.0.jar
```

Copy the following to a new Dockerfile
```
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
```

Build Docker Image
```
docker build -t spring-deploy:1.0 -f docker/Dockerfile .
```

## 2. Deploy Spring Application and MySQL

Create `deployment.yml` and paste the following content
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-deploy
spec:
  replicas: 2
  selector:
    matchLabels:
      app: spring-deploy
  template:
    metadata:
      labels:
        app: spring-deploy
    spec:
      containers:
        - name: spring-deploy
          image: spring-deploy:1.0
```

Start deployment
```
kubectl apply -f kubernetes/deployment.yaml
```

Check pods
```
kubectl get pods
```

Check ReplicaSet
```
kubectl get replicaset
```
Check Deployment
```
kubectl get deployment
```
Check logs
```
kubectl logs <<name-of-the-pod>>
```
Delete deployment
```
kubectl delete deployment spring-deploy
```

Add bitnami repo
```
helm repo add bitnami https://charts.bitnami.com/bitnami
```

Deploy MySQL
```
helm install mysql bitnami/mysql --set auth.rootPassword=password,auth.database=pluralsight 
```
Deploy Spring app
```
kubectl apply -f kubernetes/deployment.yaml
```

## 3. Services
Create a new file called `service.yaml` and paste the following content:
```
apiVersion: v1
kind: Service
metadata:
  name: spring-deploy
spec:
  ports:
  - name: http
    port: 8080
    targetPort: 8080
  selector:    
    app: spring-deploy
```

Apply the Service
```
kubectl apply -f kubernetes/service.yaml
```

Check the Service
```
kubectl get service
```

Create a new Pod on the default namespace
```
kubectl run -it --rm --image=curlimages/curl curly -- sh
```

Reach out the application by executing a curl request
```
curl http://spring-deploy:8080/books
```

Exit the pod
```
exit
```
Create a new file called `service-nodeport.yaml` and paste the following content:
```
apiVersion: v1
kind: Service
metadata:
  name: spring-deploy-np
spec:
  type: NodePort
  ports:
  - name: http
    port: 8080
    targetPort: 8080
  selector:    
    app: spring-deploy
```

Apply the NodePort Service
```
kubectl apply -f kubernetes/service-nodeport.yaml
```
Check the Service
```
kubectl get service
```

Create a new book
```
curl -X POST http://localhost:xxxxx/books \
   -H "Content-Type: application/json" \
   -d '{"title": "Deploying Spring Framework 6 Applications Playbook", "authorName": "Bogdan Sucaciu"}'
```

Get all books
```
curl http://localhost:xxxxx/books
```
## 4. Config Maps
Clean up resources
```
kubectl delete deployment spring-deploy
kubectl delete service spring-deploy
kubectl delete service spring-deploy-np
helm uninstall mysql
```
Create `db` and `books` namespaces
```
kubectl create namespace db
kubectl create namespace books
```

Deploy MySQL Server to `db` namespace
```
helm install mysql bitnami/mysql --set auth.rootPassword=password,auth.database=pluralsight,namespaceOverride=db 
```

Check the status
```
kubectl get pods -n db
```
Deploy Spring application to `books` namespace
```
kubectl apply -f kubernetes/deployment.yaml -n books
```
Check the status
```
kubectl get pods -n books
```

Create a new file called `configmap.yaml` and paste the following:
```
apiVersion: v1
kind: ConfigMap
metadata:
  name: spring-deploy
data:
  spring.datasource.url: jdbc:mysql://mysql.db:3306/pluralsight
  flyway.url: jdbc:mysql://mysql.db:3306/pluralsight
```

Apply the Config Map
```
kubectl apply -f kubernetes/configmap.yaml -n books
```
Update the `deployment.yaml` to include the config:
```
      envFrom:
      - configMapRef:
          name: spring-deploy
```
Update the deployment
```
kubectl apply -f kubernetes/deployment.yaml -n books
```
 
## 5. Liveness and Readiness
Configure Liveness and Readiness Endpoints
```
management:
  endpoint:
    health:
      enabled: true
      probes:
        enabled: true
      group:
        liveness:
          include: livenessState,diskSpace,ping,kubernetes
        readiness:
          include: readinessState,diskSpace,ping,kubernetes,db
```
Repackage the application
```
mvn clean package

docker build -t spring-deploy:1.0 -f docker/Dockerfile .
```

Update Deployment
```
readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 15
            periodSeconds: 20
```

Reapply the deployment
```
kubectl delete deployment spring-deploy -n books

kubectl apply -f kubernetes/deployment.yaml -n books
```

Check status
```
kubectl get pods -n books
```
Try out the get all books endpoint
```
curl http://localhost:xxxxx/books
```
Bring down the database
```
helm uninstall mysql
```
## 6. Init Containers
Deploy the MySQL Database
```
helm install mysql bitnami/mysql --set auth.rootPassword=password,auth.database=pluralsight,namespaceOverride=db
```
Add the Init Container to `deployment.yaml`
```
initContainers:
  - name: flyway-migration
    image: flyway/flyway:9.16
    command: ['flyway', 'migrate']
    volumeMounts:
    - name: flyway-config
      mountPath: "/flyway/conf"
    - name: flyway-migration
      mountPath: "/flyway/sql"  
...
  volumes:
  - name: flyway-config
    hostPath:
      path: /path/to/project/deploying-spring-framework-6/src/main/resources/db
  - name: flyway-migration
     hostPath:
      path: /path/to/project/deploying-spring-framework-6/src/main/resources/db/migration  
```
Apply the deployment
```
kubectl apply -f kubernetes/deployment.yaml -n books
```
Config the migration using logs
```
kubectl logs <podName> -n books -c flyway-migration
```