# CKAD

```
kubectl cluster info
kubectl get nodes
```
> Pods
```
kubctl run nginx --image nginx
kubectl get pods
kubectl describe pods nginx
#To get additional information
kubectl get pods -o wide
```

> vi settings
```bash
: set tabstop=4 shiftwidth=4 expandtab
# http://vimdoc.sourceforge.net/htmldoc/options.html

: set ts=2 et sw=2 paste number cuc
#Ts tabstop  
#Sw shiftwidth Number of spaces to use for each step of (auto)indent.
#Et expandtab In Insert mode: Use the appropriate number of spaces to insert a
#	<Tab>.  Spaces are used in indents with the '>' and '<' commands and
#	when 'autoindent' is on

#cuc: highlight cursor column


```
> Imperative commands

1. Creating resources
```bash
kubectl run nginx --image=nginx   #(deployment)
kubectl run nginx --image=nginx --restart=Never   #(pod)
kubectl run nginx --image=nginx --restart=OnFailure   #(job)  
kubectl run nginx --image=nginx  --restart=OnFailure --schedule="* * * * *" #(cronJob)
```

2. Exposing services

```bash
kubectl run frontend --replicas=2 --labels=run=load-balancer-example --image=busybox  --port=8080
kubectl expose deployment frontend --type=NodePort --name=frontend-service --port=6262 --target-port=8080
kubectl set serviceaccount deployment frontend myuser
kubectl create service clusterip my-cs --tcp=5678:8080 --dry-run -o yaml
```
3. Useful commands

```bash
kubectl set image pod/nginx nginx=nginx:1.7.1
kubectl logs nginx --previous
kubectl exec -it nginx -- /bin/sh
kubectl run nginx --image=nginx --restart=Never --env=var1=val1
# then
kubectl exec -it nginx -- env
kubectl label po nginx2 app=v2 --overwrite
kubectl get po -l app=v2
# or
kubectl get po --selector=app=v2
#Remove label app
kubectl label po nginx1 nginx2 nginx3 app-
# Checking rollout status of deployment
kubectl rollout status deploy nginx

# Getting deployment history
kubectl rollout history deploy nginx

# Rolling back deployment to certain version
kubectl rollout undo deploy nginx --to-revision=2
```

> Security context:
```bash
runAsUser
Capabilities: add
kubectl exec ubuntu-sleeper -- whoami   # to find the user
```
> Secret

```bash
kubectl create secret generic mysecret --from-literal=password=mypass
echo -n YWRtaW4= | base64 -d
```

> Service account:
```yaml
serviceAccountName: 
```

> Resource Limit
```bash
kubectl run nginx -image=nginx --restart=Never --port=80 --namespace=myname --command --serviceaccount=mysa1 --env=HOSTNAME=local --labels=bu=finance,env=dev  --requests='cpu=100m,memory=256Mi' --limits='cpu=200m,memory=512Mi' --dry-run -o yaml - /bin/sh -c 'echo hello world'
kubectl create quota myrq --hard=cpu=1,memory=1G,pods=2 --dry-run=client -o yaml

# Taint and toleration
k taint nodes node01 spray=mortein:NoSchedule
k taint node controlplane node-role.kubernetes.io/master:NoSchedule-

# Node affinity
kubectl get node node01 --show-labels
kubectl label node node01 color=blue
Operators:  In,Exists
```


> Multi container pod


1. Multicontainer *****
2. Readiness Probe *****
3. Logging
kubectl logs
4. Monitoring
git clone https://github.com/kodekloudhub/kubernetes-metrics-server.git
k top node --sort-by='cpu'
k top node --sort-by='memory'


> Pod design

1. Label and selectors
```bash
k get all --selector env=prod --no-headers|wc -l
k get po --selector env=prod,bu=finance,tier=frontend
```
2. Rolling Updates and Rollbacks
```bash
rollingUpdate:
      maxSurge: 25%
      maxUnavailable: 25%
    type: RollingUpdate
```

3. Jobs and CronJobs 
```bash
# Jobs

job.spec.activeDeadlineSeconds=30.  #30 sec end it
job.spec.completions=5.    #5 completion
 job.spec.parallelism=5.   #5 parallel execution

# Cronjobs
cronjob.spec.startingDeadlineSeconds=17
cronjob.spec.jobTemplate.spec.activeDeadlineSeconds=12

#Cron job running daily at 9:30 pm
k create cronjob throw-dice-cron-job --image=kodekloud/throw-dice --schedule='30 21 * * *'
* * * * * command(s)
- - - - -
| | | | |
| | | | ----- Day of week (0 - 7) (Sunday=0 or 7)
| | | ------- Month (1 - 12)
| | --------- Day of month (1 - 31)
| ----------- Hour (0 - 23)
------------- Minute (0 - 59)
```


> Services and networking

1. Kubernetes services
```bash
k create svc nodeport webapp-service --node-port=30080 --tcp=8080:8080 --dry-run=client -o yaml
```
2. Network Policies
No create command.Check documentation


> State persistence

1. State persistence ,pv**. Check PV
```yml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: pv-log
spec:
  persistentVolumeReclaimPolicy: Retain
  accessModes:
    - ReadWriteMany
  capacity:
    storage: 100Mi
  hostPath:
    path: /pv/log
```

3. Storage class
storageClassName: local-storage

> Install helm
1. Getting the OS
cat /etc/*release*
2. helm —help
> Helm Concepts
1. Helm search
2. helm repo add bitnami https://charts.bitnami.com/bitnami.  #Adding repo
3. helm search repo joomla
4. Helm list    #Use to list packages
5. helm pull --untar bitnami/apache   
6. Helm uninstall <release-name>

> Deployment strategies

Have different deployment with same labels and control traffic based on number of pods
> API version/deprecations
 1. k api-resources |egrep "deployments|replicasets|cronjobs|customresourcedefinitions"
2. In Kubernetes versions : X.Y.Z Where X stands for major, Y stands for minor and Z stands for patch version.
3. Kubeapiserver is in /etc/kubernetes/manifests/kube-apiserver.yaml.    
--runtime-config=batch/v1=false
> Docker images
1. docker image ls|wc -l
2.  docker build -t webapp-color .
3.  Run an instance of the image webapp-color and publish port 8080 on the container to 8282 on the host.
docker run -p 8282:8080 webapp-color
4. Find base OS for the image
docker run python:3.6 cat /etc/*release*

> Admission controller

1.  --enable-admission-plugins=NodeRestriction,NamespaceAutoProvision add controller here
2.  --disable-admission-plugins=DefaultStorageClass
3. ps -ef | grep kube-apiserver | grep admission-plugins
4. Mutating is called followed by validating admission controller


