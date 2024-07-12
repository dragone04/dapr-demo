# DAPR

    In this demo you can run a pool of microservices, testing some dapr building blocks like:
        - service-to-service communication
        - secret store
        - database bindings
        - pub-sub
        - state store
        - cron
        - configuration and resiliency

## install dapr with dev features

### local machine

    dapr init

### kubernetes

    dapr init -k --dev

## access dapr local configuration

    cd $HOME/.dapr

## run a dummy dapr service on local machine to enable sidecar connetion

    dapr run --app-id dapr-app --dapr-http-port 3500

## configure local secret store

    creare in your $HOME/.dapr a file called secret.yaml. If you don't enable hot reload, you need to restart your services to get new configurations.

    apiVersion: dapr.io/v1alpha1
    kind: Component
    metadata:
    name: localsecretstore
    namespace: default
    spec:
    type: secretstores.local.file
    version: v1
    metadata:
    - name: secretsFile
        value: secrets.json
    - name: nestedSeparator
        value: ":"

## run zipkin locally

    brew install zipkin
    brew services start zipkin
    access it http://localhost:9411/

## configure dapr to connect to zipkin

    creare in your $HOME/.dapr a file called configuration.yaml. If you don't enable hot reload, you need to restart your services to get new configurations.

    apiVersion: dapr.io/v1alpha1
    kind: Configuration
    metadata:
    name: daprConfig
    namespace: default
    spec:
    tracing:
        samplingRate: "1"
        zipkin:
        endpointAddress: "http://localhost:9411/api/v2/spans"

## install redis

    apiVersion: dapr.io/v1alpha1
    kind: Component
    metadata:
    name: redis-pubsub
    spec:
    type: pubsub.redis
    version: v1
    metadata:
    - name: redisHost
        value: localhost:6379
    - name: redisPassword
        value: "KeFg23!"
    - name: consumerID
        value: "channel1"
    - name: enableTLS
        value: "false"

## publish message through dapr

    curl -X POST http://localhost:3500/v1.0/publish/order-pub-sub/orders -H "Content-Type: application/cloudevents+json" -d '{"specversion" : "1.0", "type" : "com.dapr.cloudevent.sent", "source" : "testcloudeventspubsub", "subject" : "Cloud Events Test", "id" : "someCloudEventId", "time" : "2021-08-02T09:00:00Z", "datacontenttype" : "application/cloudevents+json", "data" : {"orderId": "100"}}'

## run services

    dapr run -f [filename]
    dapr run -f /Users/<<user>>/<<folder>>/dapr.yaml

## Kubernetes

### pre requisites, give access roles to read secrets

    kubectl create role access-secrets --verb=get,list,watch,update,create --resource=secrets -n development
    kubectl create rolebinding --role=access-secrets default-to-secrets --serviceaccount=development:default -n development

### dapr

    dapr init -k --dev (install dapr with zipkins and redis)
    dapr components -k -A (list all the components installed)
    dapr dashboard -k (open dapr dashboard related to kubernetes instance)

### utility commands

    minikube delete -all (clear minikune instance)
    minikube start (start minikube instance)

    dapr init -k --dev (install dapr with zipkins and redis)
    minikube service dapr-dev-zipkin --namespace=default --url (enable connection to remote zipkins instance. Check service name)

    kubectl rollout restart deployment -n dapr-system
    kubectl rollout restart deployment -n development 

### deploy postgres and secret

    helm install pg-minikube --set auth.postgresPassword=password bitnami/postgresql
    kubectl create secret generic secret --from-literal=secret=verysecret
    kubectl create namespace development
    kubectl apply -f component-config.yaml -n development
    kubectl apply -f component-postgresql-bindings.yaml -n development

    minikube service pg-minikube-postgresql --namespace=default --url

    CREATE TABLE public.cloud_event (
        id VARCHAR(255) primary key,
        source VARCHAR(255),
        type VARCHAR(255),
        specversion VARCHAR(255),
        datacontenttype VARCHAR(255),
        data VARCHAR(255)
    );

### build a service. taken spring-store as example.

    eval $(minikube -p minikube docker-env)
    docker build -t spring-store:0.1.0 -f ./Dockerfile .
    kubectl create -f deployment-spring-store.yaml -n development
    minikube service spring-store --namespace=development --url