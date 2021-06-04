#!/bin/bash

# Load configuration
source lambda_config.sh

echo "Building lambda java package with Docker Maven : " $docker_maven_version

docker run -it --rm --name jasperreports-maven -v "$(pwd)/lambda_code":/usr/src/mymaven -w /usr/src/mymaven $docker_maven_version mvn clean install

echo "Lambda java package builded"