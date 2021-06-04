#!/bin/bash

# Load configuration
source lambda_config.sh

echo "Creating S3 buckets with Docker AWS CLI : " $docker_aws_cli_version

docker run --rm -it -v $(pwd)/aws_config:/root/.aws $docker_aws_cli_version s3 mb s3://$files_s3_bucket
docker run --rm -it -v $(pwd)/aws_config:/root/.aws $docker_aws_cli_version s3 mb s3://$templates_s3_bucket

echo "S3 buckets created"