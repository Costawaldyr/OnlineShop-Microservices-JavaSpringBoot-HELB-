#!/bin/bash

mvn clean
mvn package
java -jar target/order-service-0.0.1-SNAPSHOT.jar &
