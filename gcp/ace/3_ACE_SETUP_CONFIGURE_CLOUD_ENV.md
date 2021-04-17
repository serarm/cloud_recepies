# Set Up and Configure a Cloud Environment in Google Cloud

![Environment](img/3_architecture.png "Environment")

## Task 1: Create development VPC manually
+ Navigation menu > VPC network > VPC networks
+ Subnet custom

|VPC Name|Subnet Name|Region|IP address range|
| :---:  |:---:      |:---: |:-----------:   |
|griffin-dev-vpc|griffin-dev-wp|us-east1|192.168.16.0/20|
|griffin-dev-vpc|griffin-dev-mgmt|us-east1|192.168.32.0/20|

## Task 2: Create production VPC manually

+ Navigation menu > VPC network > VPC networks
+ Subnet custom

|VPC Name|Subnet Name|Region|IP address range|
| :---:  |:---:      |:---: |:-----------:   |
|griffin-prod-vpc|griffin-prod-wp|us-east1|192.168.48.0/20|
|griffin-prod-vpc|griffin-prod-mgmt|us-east1|192.168.64.0/20|

```bash
gcloud compute networks create griffin-prod-vpc --project=qwiklabs-gcp-00-a24ba41ff025 --description=Production\ VPC --subnet-mode=custom --mtu=1460 --bgp-routing-mode=regional

gcloud compute networks subnets create griffin-prod-wp --project=qwiklabs-gcp-00-a24ba41ff025 --range=192.168.48.0/20 --network=griffin-prod-vpc --region=us-east1

gcloud compute networks subnets create griffin-prod-mgmt --project=qwiklabs-gcp-00-a24ba41ff025 --range=192.168.64.0/20 --network=griffin-prod-vpc --region=us-east1
```
## Task 3: Create bastion host
+ Create instance `griffin-bastion-host`
+ Region  us-east1
+ Zone us-east1-b
+ Type n1-standard-1
+ Add to subnet `griffin-prod-mgmt` and `griffin-dev-mgmt`
+ Create firewall rule to ensure ssh to instance
```bash
gcloud compute --project=qwiklabs-gcp-00-a24ba41ff025 firewall-rules create griffin-dev-vpc-firewall --direction=INGRESS --priority=1000 --network=griffin-dev-vpc --action=ALLOW --rules=tcp:22 --source-ranges=0.0.0.0/0
gcloud compute --project=qwiklabs-gcp-00-a24ba41ff025 firewall-rules create griffin-prod-vpc-firewall --direction=INGRESS --priority=1000 --network=griffin-prod-vpc --action=ALLOW --rules=tcp:22 --source-ranges=0.0.0.0/0
```

## Task 4: Create and configure Cloud SQL Instance
+ Create a MySQL Cloud SQL Instance called griffin-dev-db in us-east1
+ Configure for wordpress site

```sql
gcloud sql connect griffin-dev-db --user=root
CREATE DATABASE wordpress;
GRANT ALL PRIVILEGES ON wordpress.* TO "wp_user"@"%" IDENTIFIED BY "stormwind_rules";
FLUSH PRIVILEGES;
```
## Task 5: Create Kubernetes cluster
+ Create a 2 node cluster (n1-standard-4) called griffin-dev, in the griffin-dev-wp subnet
+ zone us-east1-b

```bash
gcloud container clusters create griffin-dev \
  --num-nodes=2 --zone=us-east1-b \
  --machine-type=n1-standard-4  \
  --network=griffin-dev-vpc --subnetwork=griffin-dev-wp
  gcloud container clusters get-credentials griffin-dev
```

## Task 6: Prepare the Kubernetes cluster
+ Updated yaml file can be found at [`wp_k8s`](wp_k8s)
```bash
mkdir wordpress
cd wordpress
#Copy the code
gsutil cp gs://cloud-training/gsp321/wp-k8s .
```
+ The WordPress server needs to access the MySQL database using the username and password you created in task 4. 
+ Add the following secrets and volume to the cluster using wp-env.yaml. 
+ Make sure you configure the username to wp_user and password to stormwind_rules before creating the configuration.
+ Use the command below to create the key, and then add the key to the Kubernetes environment.
```bash
kubectl create -f wp-env.yaml
gcloud iam service-accounts keys create key.json \
    --iam-account=cloud-sql-proxy@$GOOGLE_CLOUD_PROJECT.iam.gserviceaccount.com
kubectl create secret generic cloudsql-instance-credentials \
    --from-file key.json
```
## Task 7: Create a WordPress deployment

+ Create the deployment using wp-deployment.yaml. 
+ Replace YOUR_SQL_INSTANCE with griffin-dev-db's Instance connection name from Cloud SQL instance
`qwiklabs-gcp-00-a24ba41ff025:us-east1:griffin-dev-db`
+ Check the site installation page is up
```bash
kubectl create -f wp-deployment.yaml
kubectl list deployment -w
kubectl create -f wp-service.yaml
kubectl list service -w
```
## Task 8: Enable monitoring
+ Create an uptime check for your WordPress development site
>Navigation menu >Monitoring>Uptime Check

## Task 9: Provide access for an additional engineer

> Navigation menu > IAM & Admin > IAM
