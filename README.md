# DAPR

## access dapr local configuration

    cd $HOME/.dapr

## run dapr dummy service to enable sidecar connetion

    dapr run --app-id dapr-app --dapr-http-port 3500

## configure local secret store

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

    brew services start zipkin

## configure dapr to connect to zipkin

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

## run zipkins

    http://localhost:9411/

## run service

    dapr run -f /Users/<<user>>/work/bitbucket/test_env/dapr/messaging/dapr.yaml

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

## Kubernetes

### pre requisites, give correct roles

    kubectl create role access-secrets --verb=get,list,watch,update,create --resource=secrets -n development
    kubectl create rolebinding --role=access-secrets default-to-secrets --serviceaccount=development:default -n development

### dapr

    dapr init -k --dev
    dapr components -k -A
    dapr dashboard -k

### utility

    minikube delete -k --all
    minikube start

    minikube service dapr-dev-zipkin --namespace=default --url

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

### spring-store

    eval $(minikube -p minikube docker-env)
    docker build -t spring-store:0.1.0 -f ./Dockerfile .
    kubectl create -f deployment-spring-store.yaml -n development
    minikube service spring-store --namespace=development --url

### spring-producer

    eval $(minikube -p minikube docker-env)
    docker build -t spring-producer:0.1.0 -f ./Dockerfile .
    kubectl create -f deployment-spring-producer.yaml -n development
    minikube service spring-producer --namespace=development --url