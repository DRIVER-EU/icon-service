FROM openjdk:8u212-jdk
ENV VERSION 1.0.2
ADD target/icon-service-${VERSION}.jar /opt/application/icon-service-${VERSION}.jar
ADD run.sh /opt/application/run.sh
ADD dockerconfig /opt/application/config
CMD ["/bin/sh","/opt/application/run.sh"]
