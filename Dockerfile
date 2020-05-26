FROM maven:3.6.3-jdk-8-slim AS build
LABEL description="Wire Roman"
LABEL project="wire-bots:roman"

WORKDIR /src

# download wait-for script
RUN curl https://raw.githubusercontent.com/LukasForst/wait-for/master/wait-for > wait-for
RUN chmod +x wait-for

# ------------------ App specific ------------------
WORKDIR /app

# install maven dependencies
COPY pom.xml ./
RUN mvn verify --fail-never -U

# build application
COPY . ./
RUN mvn -Dmaven.test.skip=true package

# runtime
FROM dejankovacevic/bots.runtime:2.10.3

COPY --from=build /app/target/roman.jar /opt/roman/

# create configuration
COPY roman.yaml /etc/roman/

# ------------------ Wire common ------------------
# set APP_DIR - where is the entrypoint
ENV APP_DIR=/opt/roman
# copy wait for script to root for running in kubernetes
COPY --from=build /src/wait-for /wait-for
# create version file
ARG release_version=development
ENV RELEASE_FILE_PATH=$APP_DIR/release.txt
RUN echo $release_version > $RELEASE_FILE_PATH
# enable json logging
ENV JSON_LOGGING=true
# move to runtime directory
WORKDIR $APP_DIR
# /------------------ Wire common -----------------

EXPOSE  8080 8081 8082

# create entrypoint
RUN echo '\
java    -javaagent:/opt/wire/lib/jmx_prometheus_javaagent.jar=8082:/opt/wire/lib/metrics.yaml \
        -jar roman.jar \
        server /etc/roman/roman.yaml'\
>> entrypoint.sh
RUN chmod +x entrypoint.sh

ENTRYPOINT $APP_DIR/entrypoint.sh
