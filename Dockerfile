FROM dejankovacevic/bots.runtime:2.10.3

COPY target/ealarming.jar   /opt/ealarming/ealarming.jar
COPY ealarming.yaml         /etc/ealarming/ealarming.yaml

WORKDIR /opt/ealarming

EXPOSE  8080 8081 8082
