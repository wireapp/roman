FROM maven:3-openjdk-11 AS build
LABEL description="Wire Roman"
LABEL project="wire-bots:roman"

WORKDIR /app

COPY pom.xml ./

RUN mvn verify --fail-never -U

COPY . ./

RUN mvn -Dmaven.test.skip=true package

FROM wirebot/runtime

COPY --from=build /app/target/roman.jar /opt/roman/

COPY roman.yaml         /etc/roman/

# create version file
ARG release_version=development
ENV RELEASE_FILE_PATH=/opt/roman/release.txt
RUN echo $release_version > /opt/roman/release.txt

# ENV APPENDER_TYPE=json-console

WORKDIR /opt/roman

EXPOSE  8080 8081 8082

ENTRYPOINT ["java", "-javaagent:/opt/wire/lib/prometheus-agent.jar=8082:/opt/wire/lib/metrics.yaml", "-jar", "roman.jar", "server", "/etc/roman/roman.yaml"]

