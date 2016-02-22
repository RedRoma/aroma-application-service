#!/usr/bin/env bash

version="1.1-SNAPSHOT"
jar="aroma-application-service-$version.jar"

nohup java -jar $jar > server.log &
