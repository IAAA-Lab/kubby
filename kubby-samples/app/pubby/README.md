# Building and running the Docker image

Build and application package:

```
gradle build
```

Build and tag an image:

```
docker build -t pubby .
```

Start an image:

```
docker run -m512M --cpus 2 -p 8080:8080 --rm pubby
```

With this command, we start Docker in foreground mode limited to 2 CPU and 512M exposing the port 8080.

This image is available at Docker Hub tagged as `iaaa/pubby`