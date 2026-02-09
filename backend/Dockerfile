# it is hard to build the image inside container. this is relying on the host machine to build the image. we can change this. RG


FROM amazoncorretto:21

WORKDIR /app

COPY target/ottro-service-0.0.1-SNAPSHOT.jar /app/ottro-service-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/ottro-service-0.0.1-SNAPSHOT.jar"]
