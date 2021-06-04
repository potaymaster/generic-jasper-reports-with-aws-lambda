#!/bin/bash

# Load configuration
source lambda_config.sh

echo "Create CloudFormation stack with Docker AWS CLI : " $docker_aws_cli_version

docker run --rm -it -v $(pwd)/aws_config:/root/.aws -v $(pwd)/aws_IaC:/aws $docker_aws_cli_version cloudformation create-stack --stack-name jasperreports --template-url https://$files_s3_bucket.s3-$region.amazonaws.com/IaC/jasperreports_stack.yml --parameters ParameterKey=FilesS3Location,ParameterValue=$files_s3_bucket ParameterKey=TemplatesS3Location,ParameterValue=$templates_s3_bucket ParameterKey=LambdaPackagedFile,ParameterValue=$lambda_packaged_file --capabilities CAPABILITY_AUTO_EXPAND CAPABILITY_NAMED_IAM

echo "CloudFormation stack launch - See status into AWS Web Console"