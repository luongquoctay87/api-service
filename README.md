# SAMPLE JAVA APPLICATION
### 1. Tech stack
- Java 8
- Spring boot 2.6.11
- Spring boot security 2.6.11
- Spring boot jwt
- Spring boot jpa
- Spring boot actuator
- Postgresql
- Swagger 3.0
- Docker
- Grafana & Prometheus

### 2. Getting started

2.1 build docker images
```
$ mvn clean install
$ docker-compose up -d --build
```

2.2 Test
```
$ curl --location --request POST http://localhost:8181/api/v1/auth/login --header Authorization:Basic b2F1dGgyQ2xpZW50Om9hdXRoMlNlY3JldA== --header Content-Type:application/x-www-form-urlencoded --data-urlencode username=sysadmin --data-urlencode password=password
```

2.3 View API information by Swagger

Visit Swagger:  [Swagger UI](http://localhost:8181/api/v1/swagger-ui.html)


2.4 Check Application Health
```
$ curl --location --request GET http://localhost:8099/actuator
```

2.5 Update Code and ReRun
```
$ mvn clean install
$ docker-compose up -d --build api-service
```
 
2.6 View api-service container log
```
$ docker-compose logs -tf api-service
```

2.7 Remote Debug
Connect port 5005

2.8 Log and Monitoring API-Service
    
- Monitoring and alert: [Prometheus Target](http://localhost:9090/targets)
- Prometheus web UI: [Prometheus Graph](http://localhost:9090/graph)
- Grafana web UI: [Grafana](http://localhost:3000)
  - default account: admin/admin
    


### 3 Push Images to Docker Hub (Reverse to deploy on EC2)
**3.1 On Local**
- Create docker tag
```
$ docker tag sample-java-app_api-service luongquoctay87/sample-java-app:v1.0.0
```

- Push to Docker Hub
```
$ docker login
$ docker push luongquoctay87/sample-java-app:v1.0.0
```

**3.2 On EC2**

- Install docker
```
$ sudo su -
$ yum install -y docker
$ service docker start
$ systemctl enable docker
$ docker --version
```

- Install docker compose
```
$ curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
$ chmod +x /usr/local/bin/docker-compose
$ ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
$ docker-compose -v
```

- Create persisting environment variables

$ vi ~/.bash_profile
  ```
  export POSTGRES_URL=database-1.ctixzu74yr0p.ap-southeast-1.rds.amazonaws.com
  export POSTGRES_PORT=5432
  export POSTGRES_DB=db_test
  export POSTGRES_USER=admin
  export POSTGRES_PASSWORD=12345678
  ```
$ source ~/.bash_profile


- Create file `docker-compose.yml`
```
version: '3.8'

  api-service:
      image: luongquoctay87/sample-java-app:v1.0.0
      container_name: api-service
      environment:
        - MYSQL_URL=${MYSQL_URL}
        - MYSQL_PORT=${MYSQL_PORT}
        - MYSQL_DATABASE=${MYSQL_DATABASE}
        - MYSQL_USER=${MYSQL_USER}
        - MYSQL_PASSWORD=${MYSQL_PASSWORD}
      ports:
        - "8080:8080"
      networks:
        - backend

  prometheus:
    image: "prom/prometheus"
    container_name: prometheus
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"
    networks:
      - backend

  grafana:
    image: "grafana/grafana"
    container_name: grafana
    ports:
      - "3000:3000"
    networks:
      - backend

networks:
  backend:
    name: api-network

```

- Create file `prometheus.yml`
```
scrape_configs:
  - job_name: 'app-metrics'
    metrics_path: '/api/v1/actuator/prometheus'
    scrape_interval: 5s
    static_configs:
      - targets:
          - 54.151.224.116:8080
```

### 4. Set up Jenkins on EC2
[Jenkins Set up Guideline](https://www.section.io/engineering-education/building-a-java-application-with-jenkins-in-docker/)

4.1 Create `Dockerfile`
```
FROM jenkins/jenkins:lts
USER root
RUN apt-get update -qq \
	&& apt-get install -qqy apt-transport-https ca-certificates curl gnupg2 software-properties-common
RUN curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add -
RUN add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/debian \
   $(lsb_release -cs) \
   stable"
RUN apt-get update  -qq \
	&& apt-get install docker-ce -y
RUN usermod -aG docker jenkins
```

4.2 Build image
```
$ service docker start
$ service docker status
$ systemctl enable docker
$ docker image build -t jenkins-docker .
```

4.3 Run docker image
```
docker run -d -it -p 8081:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock --restart unless-stopped jenkins-docker
```

4.4 Set up jenkins

Access [Jenkins Server](http://localhost:8081) 
```
  $ docker exec -it <container_name/container_id> /bin/bash
  $ cat /var/jenkins_home/secrets/initialAdminPassword
  -> copy/paste to jenkins

  1. Jenkins global configurations
      System Configurations
          Global Tool Configuration
              - 
              - JDK
                  Lấy path jav_home trong container
                      $ docker exec -it <container_name/container_id> /bin/bash
                      $echo $JAVA_HOME
                  + name: JDK
                  + JAVA_HOME: /opt/java/openjdk
              - Maven config
                  + name: maven-3.8.5
                  + install automatically: install from apache
              
  2. Jenkins item
      + Name: sample-java-app
      + Freestyle project
  
  3. Git
      Github project: https://github.com/luongquoctay87/sample-java-app
      Git: https://github.com/luongquoctay87/sample-java-app.git
      Credential: luongquoctay87/xxxxxxx
      
  4. Build trigger
      Poll SCM
        Schedule: * * * * *
      
  5. Build Environment
      Invoke top-level Maven targets
          Maven version: 3.8.5
          Goals: test
      Invoke top-level Maven targets
          Maven version: 3.8.5
          Goals: install
          
  6. Build now
  
  7. Building and deploying our Docker image to Docker Hub
    7.1 Install plugin
    In Manage Jenkins, select Manage Plugins under System Configurations, search and install the following plugins:
      - docker-build-step
      - CloudBees Docker Build and Publish
    7.2 Add Build Step
      -> Docker Build
        -> Docker Build and Publish
          + Repository name: luongquoctay87/sample-java-app
          + Tag: v1.0.0
     7.3 Login to our Docker Hub account inside our Jenkins container
      
      or
      $ docker exec -it <container_name/container_id> /bin/bash
      $ docker login
     7.4 Rebuild
      Click: Build now
     
``` 