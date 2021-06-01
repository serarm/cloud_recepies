# [Managing a GKE Multi-tenant Cluster with Namespaces](https://google.qwiklabs.com/focuses/14861)

## Step 1 :Setup

+ Download required files

```bash
gsutil -m cp -r gs://spls/gsp766/gke-qwiklab ~
cd ~/gke-qwiklab
```

## Step 2 : Create Namespaces

+ Set default computing zone and authenticate to `multi-tenant-cluster` 

```bash
gcloud config set compute/zone us-central1-a 
gcloud container clusters get-credentials multi-tenant-cluster
# Listing Namespaces
kubectl get namespace
```
Intepretation of default namespace:
+ `default` - the default namespace used when no other namespace is specified
+ `kube-node-lease` - manages the lease objects associated with the heartbeats of each of the cluster's nodes
+ `kube-public` - to be used for resources that may need to be visible or readable by all users throughout the whole cluster
+ `kube-system` - used for components created by the Kubernetes system

```bash
# Listing all namespaced resources
kubectl api-resources --namespaced=true
# Listing for specific namespace
kubectl get services --namespace=kube-system
# Create new namespace
kubectl create namespace team-a && \
kubectl create namespace team-b
kubectl config set-context --current --namespace=team-a
```



