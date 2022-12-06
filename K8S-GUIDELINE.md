### 1. Setup Kubernetes in Windows & Run Spring boot application on k8s cluster
    https://medium.com/@javatechie/kubernetes-tutorial-setup-kubernetes-in-windows-run-spring-boot-application-on-k8s-cluster-c6cab8f7de5a

### 2. Run & Deploy Spring Boot Application in K8s Cluster
    https://medium.com/@javatechie/kubernetes-tutorial-run-deploy-spring-boot-application-in-k8s-cluster-using-yaml-configuration-3b079154d232
##### 2.1 Start minikube
- $ minikube start --driver=docker
- $ minikube docker-env
- $ @FOR /f "tokens=*" %i IN ('minikube -p minikube docker-env --shell cmd') DO @%i
- $ minikube status
- $ kubectl cluster-info

##### 2.2 Build docker image
- $ docker build -t api-service:1.0 .

##### 2.3 Deployment
- $ kubectl apply file db-configMap.yaml
- $ kubectl apply file db-secrets.yaml
- $ kubectl apply file db-deployment.yaml
- $ kubectl apply file app-deployment.yaml

##### 2.4 Check
- $ kubectl get services
- $ kubectl get deployments
- $ kubectl get pods
- $ kubectl get nodes
- $ kubectl get configMap
- $ kubectl get secrets
- $ kubectl logs -f [pod-name]
- $ kubectl exec -it [pod-name] -- psql -h localhost -U postgres --password -p 5432 postgres
    - \l
    - \c postgres
    - \dt
    - select * from tbl_users;

##### 2.5 Test API:
- $ kubectl get services
- $ minikube service app-k8s-service --url
  - `http://127.0.0.1:63436
    ! Because you are using a Docker driver on windows, the terminal needs to be open to run it.`
  - request url: curl --location --request POST 'http://127.0.0.1:63436/api/v1/auth/login' \
    --header 'Content-Type: application/x-www-form-urlencoded' \
    --data-urlencode 'username=sysadmin' \
    --data-urlencode 'password=password'

##### 2.6 Stop and Delete components
- $ kubectl delete service [pod-name]
- $ kubectl delete deployment [pod-name]
- $ kubectl delete configMap [pod-name]
- $ kubectl delete secrets [pod-name] 

- $ minikube stop
- $ minikube delete

