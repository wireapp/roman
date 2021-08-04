FROM node:alpine as frontend-build
COPY frontend/ ./frontend
WORKDIR ./frontend
RUN npm i
RUN npm run build

FROM maven:3-openjdk-11 AS build
WORKDIR /app

COPY backend/pom.xml ./

RUN mvn verify --fail-never -U

COPY backend/ ./

RUN mvn -Dmaven.test.skip=true package

FROM wirebot/runtime AS runtime
LABEL description="Wire Roman"
LABEL project="wire-bots:roman"

# Copy backend
COPY --from=build /app/target/roman.jar /opt/roman/backend/
COPY backend/roman.yaml /etc/roman/
# Copy frontend
ENV FRONTEND_PATH=/opt/roman/frontend
COPY --from=frontend-build ./frontend/build $FRONTEND_PATH

# create version file
ARG release_version=development
ENV RELEASE_FILE_PATH=/opt/roman/release.txt
RUN echo $release_version > /opt/roman/release.txt

# ENV APPENDER_TYPE=json-console

WORKDIR /opt/roman/backend/

EXPOSE  8080 8081 8082

ENTRYPOINT ["java", "-javaagent:/opt/wire/lib/prometheus-agent.jar=8082:/opt/wire/lib/metrics.yaml", "-jar", "roman.jar", "server", "/etc/roman/roman.yaml"]

