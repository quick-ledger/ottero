#!/bin/bash

rm -rf target
mvn clean compile spring-boot:run \
  -Dspring-boot.run.profiles=dev \
  -Dspring-boot.run.jvmArguments="-Daws.profile=ql -Daws.region=ap-southeast-2"
