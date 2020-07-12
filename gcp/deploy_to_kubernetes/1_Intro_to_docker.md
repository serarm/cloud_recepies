# Step 1: Introduction to Docker

## Checking docker installation
 ```bash
 docker run hello-world
 ```

 ##  Building docker image with tag
 ```bash
 docker build -t node-app:0.1 .
 ```

 ## Running the docker image in background `-d` flag
```bash
 docker run -p 4000:80 --name my-app -d node-app:0.1
```
 ## Debuging
 + Checking logs
 ```bash
 docker logs -f [container_id]
 ```
 + Logging in to container
 ```bash
 docker exec -it [container_id] bash
 ```
 + Inspecting the container
 ```bash
 docker inspect [container_id]
 docker inspect --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' [container_id]
 ```

 ## Publishing the container
+ Get the project id
```bash
 gcloud config list project
 ```
+ Push the image to docker registry
```bash
docker tag node-app:0.2 gcr.io/[project-id]/node-app:0.2
```
 ## Cleanup

 + Stop and remove all containers

 ```bash
 docker stop $(docker ps -q)
docker rm $(docker ps -aq)
 ```

 + Removing images
Following command can be used to remove the images.Care need to taken for removing the root image later.
 ```bash
docker rmi $(docker images -aq) # remove remaining images
```
