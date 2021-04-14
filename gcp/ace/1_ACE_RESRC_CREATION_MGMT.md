# Create and manage Cloud Resources
## Task 1: Create a project jumphost instance

+ Name the instance nucleus-jumphost.
+ Use an f1-micro machine type.
+ Use the default image type (Debian Linux).
>Navigation menu > Compute engine > VM Instance

## Task 2: Create a Kubernetes service cluster

```bash
gcloud config set compute/zone us-east1-b
gcloud container clusters create nucleus-cluster
gcloud container clusters get-credentials nucleus-cluster
kubectl create deployment nucleus-server --image=gcr.io/google-samples/hello-app:2.0
kubectl expose deployment nucleus-server --type=LoadBalancer --port 8080
kubectl get service -w
```
## Task 3: Set up an HTTP load balancer

>Startup script

```bash
cat << EOF > startup.sh
#! /bin/bash
apt-get update
apt-get install -y nginx
service nginx start
sed -i -- 's/nginx/Google Cloud Platform - '"\$HOSTNAME"'/' /var/www/html/index.nginx-debian.html
EOF
```

1. Create an instance template, which uses the startup script

```bash
gcloud compute instance-templates create nginx-template \
   --region=us-east1 \
   --network=default \
   --subnet=default \
   --tags=allow-health-check \
   --image-family=debian-9 \
   --image-project=debian-cloud \
   --metadata-from-file startup-script=startup.sh
```

2. Create target pools

```bash
gcloud compute target-pools create nginx-pool
```

3. Create a managed instance group based on the template:

```bash
gcloud compute instance-groups managed create nginx-group \
   --base-instance-name nginx \
   --template=nginx-template \
   --size=2 \
   --zone=default \
   --target-pool nginx-pool
# List the compute instances
gcloud compute instances list
```

4.Create the fw-allow-health-check firewall rule.  

```bash
gcloud compute firewall-rules create www-firewall --allow tcp:80
```

5. L3 firewall rule

```bash
gcloud compute forwarding-rules create nginx-lb \
--region us-east1 \
--ports=80 \
--target-pool nginx-pool
```

6. Set up a global static external IP address that your customers use to reach your load balancer.

```bash
gcloud compute addresses create lb-ipv4-1 \
    --ip-version=IPV4 \
    --global
gcloud compute addresses describe lb-ipv4-1 \
    --format="get(address)" \
    --global
```

7. Create a healthcheck for the load balancer:

```bash
gcloud compute health-checks create http http-basic-check 
gcloud compute instance-groups managed \
set-named-ports nginx-group \
--named-ports http:80
```

8. Create a backend service

```bash
gcloud compute backend-services create nginx-service \
        --protocol=HTTP \
        --port-name=http \
        --health-checks=http-basic-check \
        --global
```

9. Add your instance group as the backend to the backend service:

```bash
gcloud compute backend-services add-backend nginx-backend \
        --instance-group=nginx-group \
        --instance-group-zone=us-east1-b \
        --global
```

10. Create a URL map to route the incoming requests to the default backend service:

```bash
gcloud compute url-maps create web-map \
        --default-service nginx-backend
```

11. Create a target HTTP proxy to route requests to your URL map:

```bash
gcloud compute target-http-proxies create http-lb-proxy \
        --url-map web-map
```

12. Create a global forwarding rule to route incoming requests to the proxy:

```bash
gcloud compute forwarding-rules create http-content-rule \
        --address=lb-ipv4-1\
        --global \
        --target-http-proxy=http-lb-proxy \
        --ports=80
```

13. List forwarding rules

```bash
gcloud compute forwarding-rules list
```