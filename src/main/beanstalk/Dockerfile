FROM williamyeh/docker-java8

ADD aroma-application-service-1.2-SNAPSHOT.jar aroma-application-service.jar
ADD lib lib

RUN apt-get update -y
RUN apt-get install wget curl -y

EXPOSE 7026

CMD java -jar aroma-application-service.jar --port 7026
