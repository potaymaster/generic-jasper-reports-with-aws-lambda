#!/bin/bash

# Load configuration
source lambda_config.sh

echo "Copying local resources to S3 buckets with Docker AWS CLI : " $docker_aws_cli_version

docker run --rm -it -v $(pwd)/aws_config:/root/.aws -v $(pwd)/aws_IaC:/aws $docker_aws_cli_version s3 sync . s3://$files_s3_bucket/IaC
docker run --rm -it -v $(pwd)/aws_config:/root/.aws -v $(pwd)/lambda_code/target:/aws $docker_aws_cli_version s3 cp $lambda_packaged_file s3://$files_s3_bucket/lambda/
docker run --rm -it -v $(pwd)/aws_config:/root/.aws -v $(pwd)/lambda_test:/aws $docker_aws_cli_version s3 cp template.jrxml s3://$templates_s3_bucket

echo "Local resources copied"