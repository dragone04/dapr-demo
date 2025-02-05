# run

## run a dummy dapr service on local machine to enable sidecar connetion

    dapr run --app-id dapr-app --dapr-http-port 3500

    dapr run --app-id spring-service-producer --app-port 9080 -- java -jar target/spring-service-producer-0.0.1-SNAPSHOT.jar

    dapr run --app-id spring-service-consumer --app-port 9081 -- java -jar target/spring-service-consumer-0.0.1-SNAPSHOT.jar

    minikube service my-cluster-kafka-brokers --namespace=kafka --url

### publish & subscribe

    kubectl -n kafka run kafka-producer -ti --image=quay.io/strimzi/kafka:0.45.0-kafka-3.9.0 --rm=true --restart=Never -- bin/kafka-console-producer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic my-topic

    kubectl -n kafka run kafka-consumer -ti --image=quay.io/strimzi/kafka:0.45.0-kafka-3.9.0 --rm=true --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic my-topic --from-beginning

## create env

    kubectl create namespace kafka
    kubectl create -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka
    kubectl logs deployment/strimzi-cluster-operator -n kafka -f

    kubectl apply -f kafka-cluster.yaml -n kafka
    kubectl get kafka -n kafka

    minikube tunnel #open clusters

## strimzi command on minikube

    kubectl get k -n kafka -w #kafka
    kubectl get kt -n kafka -w #topic

## delete env

    kubectl -n kafka delete -f 'https://strimzi.io/install/latest?namespace=kafka'
    kubectl delete namespace kafka

    minikube delete
    minikube start --cpus=4 --memory=4096 --driver=docker
