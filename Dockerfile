FROM dejankovacevic/bots.runtime:2.10.3

COPY target/roman.jar   /opt/roman/roman.jar
COPY roman.yaml         /etc/roman/roman.yaml

WORKDIR /opt/roman

EXPOSE  8080 8081 8082
