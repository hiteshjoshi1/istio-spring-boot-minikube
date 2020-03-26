# Spring Boot Microservice example with Istio

This uses Spring webflux but in concepts are same for any REST based on Spring boot.

Based on -
https://developer.okta.com/blog/2019/04/01/spring-boot-microservices-with-kubernetes
https://github.com/oktadeveloper/okta-spring-boot-microservice-kubernetes

While the blog shows how to deploy in GCE, this implementation is generic and can be used to deploy
in any cluster.

## My change -

I had to make a few fixes to the charts and I use istio auto injection. I am not sure if google auto enables istio injection. The blog does not show/talk about envoy proxy injection which is the key feature of istio-service mesh.
We will use minikube.

TODO- Deploy in AWS.

#### Enable Istio into your cluster

```
istioctl manifest apply --set profile=demo
```

#### Enable namespace injection

```
kubectl label namespace default istio-injection=enabled
```

#### Install Mongo- by Add mongo manifest

```
kubectl apply -f deployment-mongo.yml
```

#### Edit the application config to point to docker mongo, let your Spring app know where is the datasource

```
server.port=8000
spring.data.mongodb.host=mongodb
spring.data.mongodb.port=27017
```

#### Build

```
gradle clean build
```

#### Once build is complete, Build docker image

```
docker build -t demo-app:1.0 .
```

#### Step 1, 2 ,3 not required if you are just using my image from dockerhub.

1. Push docker image to repo

```
docker login
```

2. Tag image first(mandatory) -

```
docker tag demo-app:1.0 hiteshjoshi1/demo-app:1.0
```

3. Then push -

```
docker push hiteshjoshi1/demo-app:1.0
```

Note - if you are making changes and getting older images, check kuberntes image pull policy in deployment.yml

#### Deploy using istio - Will deploy a envoy sidecar and the pod

```
kubectl apply -f <(istioctl kube-inject -f deployment.yml)
```

#### Get cluster IP from minikube

```
export INGRESS_HOST= $(minikube ip)
```

#### Any other platform, use

```
export INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}');
```

#### Get Ingress IP

```
export INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].nodePort}')
```

#### Then you can access your microservices

```
curl http://$INGRESS_HOST:$INGRESS_PORT/kayaks
```

# Secured Micro Service with Okta

For non secured MS(starting point) see the branch

#### Build the App again with Spring security authentication with Okta

```
docker build -t demo-app-auth:1.0 .
```

#### Tag it

```
docker tag demo-app-auth:1.0 hiteshjoshi1/demo-app-auth:1.0
```

#### Push it again

```
docker push hiteshjoshi1/demo-app-auth:1.0
```

#### Change the image name in deployment.yml. Stop any running deployment

```
kubectl delete deployment <dep_name>
```

#### Then run again-

```
kubectl apply -f <(istioctl kube-inject -f deployment.yml)
```

#### Now get the port again

```
export INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].nodePort}')
```

#### Try health check it works as it not blocked by Spring security

curl http://$INGRESS_HOST:$INGRESS_PORT/

#### Try microservice, should be blocked.

```
curl http://$INGRESS_HOST:$INGRESS_PORT/kayaks
```

#### Get Auth token from OAuth using (Follow main blog if needed\*\*)

http://oidcdebugger.com/

#### Then make request again with Auth header

```
curl -H "Authorization: Bearer ${TOKEN}" http://$INGRESS_HOST:$INGRESS_PORT/kayaks
```
