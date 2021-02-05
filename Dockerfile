FROM maven:3.6.3-jdk-8-slim AS build
LABEL description="Wire Roman"
LABEL project="wire-bots:roman"

WORKDIR /app

COPY pom.xml ./

RUN mvn verify --fail-never -U

COPY . ./

RUN mvn -Dmaven.test.skip=true package

FROM dejankovacevic/bots.runtime:2.10.3

COPY --from=build /app/target/roman.jar /opt/roman/
# COPY target/roman.jar   /opt/roman/roman.jar

COPY roman.yaml         /etc/roman/

# create version file
ARG release_version=development
ENV RELEASE_FILE_PATH=/opt/roman/release.txt
RUN echo $release_version > /opt/roman/release.txt
# TODO - uncomment this when migration to JSON logging is finalized
#ENV APPENDER_TYPE=json-console

WORKDIR /opt/roman

EXPOSE  8080 8081 8082

ENTRYPOINT ["java", "-javaagent:/opt/wire/lib/jmx_prometheus_javaagent.jar=8082:/opt/wire/lib/metrics.yaml", "-jar", "roman.jar", "server", "/etc/roman/roman.yaml"]

