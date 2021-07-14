from openjdk:11.0.10-jre-slim

WORKDIR /reachit

ADD build/libs/server.jar server.jar

EXPOSE 8080

CMD java -jar /reachit/server.jar