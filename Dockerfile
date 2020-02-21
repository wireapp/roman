FROM docker.io/maven AS build-env

WORKDIR /app

COPY pom.xml ./

RUN mvn verify --fail-never

COPY . ./

RUN mvn -Dmaven.test.skip=true package

FROM dejankovacevic/bots.runtime:2.10.3

COPY --from=build-env /app/target/roman.jar /opt/roman/
# COPY target/roman.jar   /opt/roman/roman.jar

COPY roman.yaml         /etc/roman/

WORKDIR /opt/roman
ENV LD_LIBRARY_PATH=/opt/wire/lib

EXPOSE  8080 8081 8082

ENTRYPOINT ["java", "-javaagent:$(LD_LIBRARY_PATH)/jmx_prometheus_javaagent.jar=8082:$(LD_LIBRARY_PATH)/metrics.yaml", -jar", "roman.jar", "server", "/etc/roman/roman.yaml"]