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

EXPOSE  8080 8081

ENTRYPOINT ["java", "-jar", "roman.jar", "server", "/etc/roman/roman.yaml"]