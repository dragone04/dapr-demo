# DAPR

## utility commands

    minikube start --cpus=4 --memory=4096 --driver=docker
    dapr init -k --dev (install dapr with zipkins and redis)
    kubectl rollout restart deployment -n dapr-system
    kubectl rollout restart deployment -n development

## dapr configuration

### access dapr local configuration

    cd $HOME/.dapr

### configure basic components

Create in your $HOME/.dapr files below.
n.d. If you don't enable hot reload, you need to restart your services to get new configurations.

#### component-config.yaml

```yaml
apiVersion: dapr.io/v1alpha1
kind: Configuration
metadata:
    name: tracing
    namespace: development
spec:
    mtls:
        enabled: false
tracing:
    samplingRate: "0.1"
    zipkin:
        endpointAddress: "http://localhost:9411/api/v2/spans"
secrets:
    scopes:
    - storeName: localsecretstore
        defaultAccess: allow
```

#### component-secret.yaml

```yaml
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
```

#### component-pubsub.yaml

You can select one of Redis or Rabbit as a message broker and exchange it by rewriting the component-pubsub.yaml.

1. redis

    ```yaml
    apiVersion: dapr.io/v1alpha1
    kind: Component
    metadata:
        name: messagepubsub
    spec:
        type: pubsub.redis
        version: v1
        metadata:
            - name: redisHost
                value: localhost:6379
            - name: redisPassword
                value: ""
    ```

2. rabbit-mq

    ```yaml
    apiVersion: dapr.io/v1alpha1
    kind: Component
    metadata:
        name: messagepubsub
    spec:
        type: pubsub.rabbitmq
        version: v1
        metadata:
            - name: hostname
                value: localhost
            - name: username
                value: guest
            - name: password
                value: guest
            - name: reconnectWait
                value: 1 # reconnect will be performed in 1 sec after failure
            - name: deletedWhenUnused
                value: false
            - name: requeueInFailure
                value: true
            - name: enableDeadLetter
                value: true
    ```

### run a dummy dapr service on local machine to enable sidecar connetion

    dapr run --app-id dapr-app --dapr-http-port 3500

You can test pubsub component by sending a message

```json
curl --location 'http://localhost:3500/v1.0/publish/messagepubsub/messages' \
--header 'Content-Type: application/cloudevents+json' \
--header 'traceparent: 867557e4-0fcf-4cf8-9d3b-29c558bf8404' \
--header 'tracestate: bd5a12e5-6106-4f8e-b03c-3fa1c35d975c' \
--data '{
    "specversion": "1.0",
    "type": "com.dapr.cloudevent.sent",
    "source": "testcloudeventspubsub",
    "subject": "Cloud Events Test",
    "id": "someCloudEventId",
    "time": "2025-01-16T15:35:36.536Z",
    "datacontenttype": "application/cloudevents+json",
    "traceid": "5bfd6d65-6758-4b82-9584-81ba01f9114e",
    "traceparent": "81bc1585-5288-41af-b9bf-ce3eb64c2d2e",
    "topic": "messages",
    "pubsubname": "messagepubsub",
    "data": {
        "key": "key1",
        "value": "30549300"
    }
}'
```

## Kubernetes

### pre requisites, create a dedicated namespace and give access roles to read secrets

    kubectl create namespace development
    kubectl create role access-secrets --verb=get,list,watch,update,create --resource=secrets -n development
    kubectl create rolebinding --role=access-secrets default-to-secrets --serviceaccount=development:default -n development

    cd configuration.kubernetes.performance
    kubectl apply -f namespace-development.yaml
    kubectl apply -f component-config.yaml -n development
    kubectl apply -f component-resiliency.yaml -n development

### dapr

    dapr init -k --dev (install dapr in dev mode, with zipkins and redis)
    dapr components -k -A (list all the components installed)
    dapr dashboard -k (open dapr dashboard related to kubernetes instance)

### build spring boot admin

    eval $(minikube -p minikube docker-env)
    docker build -t spring-boot-admin:0.1.0 -f ./Dockerfile .
    kubectl apply -f deployment-spring-boot-admin.yaml

### build service a

    eval $(minikube -p minikube docker-env)
    docker build -t spring-service-producer:0.1.0 -f ./Dockerfile .
    kubectl apply -f deployment-spring-service-producer.yaml

### build service b

    eval $(minikube -p minikube docker-env)
    docker build -t spring-service-consumer:0.1.0 -f ./Dockerfile .
    kubectl apply -f deployment-spring-service-consumer.yaml

### build service c

    eval $(minikube -p minikube docker-env)
    docker build -t spring-service-mongo:0.1.0 -f ./Dockerfile .
    kubectl apply -f deployment-spring-service-mongo.yaml
    
### test service spring-performance-a

    minikube service spring-service-producer --namespace=development --url

### run local service

    dapr run --app-id myapp -- java -jar myapp.jar
