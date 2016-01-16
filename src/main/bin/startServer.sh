#!/usr/bin/env bash

version="1.0-SNAPSHOT"
jar="banana-application-service-$version.jar"

nohup java -jar $jar > server.log &
