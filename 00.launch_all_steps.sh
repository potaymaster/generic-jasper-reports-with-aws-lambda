#!/bin/bash

./01.lambda_build.sh
./02.aws_create_buckets.sh
./03.aws_copy_resources.sh
./04.aws_create_stack.sh