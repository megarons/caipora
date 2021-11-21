FROM openjdk:11-alpine
COPY target/lib/* /opt/lib/
COPY target/*-runner.jar /opt/app.jar
ENTRYPOINT ["java", "-Duser.country=BR", "-Duser.language=pt", "-jar", "/opt/app.jar"]
