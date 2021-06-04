#!/bin/bash
# Configuration properties
docker_maven_version='maven:3.3-jdk-8'
docker_aws_cli_version='amazon/aws-cli:2.2.8'

lambda_packaged_file='jasperreports-1.0.0.jar'

region='eu-west-1'
files_s3_bucket='XXXX-lambda-files'
templates_s3_bucket='XXXX-jasperreports-templates'