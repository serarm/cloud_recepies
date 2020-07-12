# Step 2:Kubernetes Engine: Qwik Start

## Getting the project id

```bash
gcloud config list project
```

## Setting the compute zone
Below commands set compute zone to `us-central1-a`

```bash
gcloud config set compute/zone us-central1-a
```
## Creating the Kubernetes Engine CLuster
```bash
gcloud container clusters create [CLUSTER-NAME]
```
## Getting authentication credentials for the cluster
```bash
gcloud container clusters get-credentials [CLUSTER-NAME]
```
## Creating deployment
Kubernetes Engine uses Kubernetes objects to create and manage your cluster's resources.
Kubernetes provides the [Deployment object](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/) for deploying stateless applications like web servers.
```bash
kubectl create deployment hello-server --image=gcr.io/google-samples/hello-app:1.0
```

## Creating service
[Service objects](https://kubernetes.io/docs/concepts/services-networking/service/) define rules and load balancing for accessing your application from the Internet.
Kubernetes service let expose application to external traffic
+ `--port` specifies the port that the container exposes
+ `type="LoadBalancer"` creates a Compute Engine load balancer for your container

 ```bash
 kubectl expose deployment hello-server --type=LoadBalancer --port 8080
 ```

## Cleanup
Deleting the provisioned cluster
```bash
gcloud container clusters delete [CLUSTER-NAME]
```
