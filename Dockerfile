FROM openjdk:8u171-slim
EXPOSE 8090
VOLUME /tmp

COPY target/nplsweb-0.0.1-SNAPSHOT.jar app.jar

COPY inchiPet.jar /

RUN java -jar inchiPet.jar


ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]


