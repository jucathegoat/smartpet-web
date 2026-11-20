# ---- build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- runtime ----
FROM tomcat:11.0-jdk21-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /build/build-war/smartpet.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 10000

CMD ["sh", "-c", "sed -i 's/port=\"8005\"/port=\"-1\"/g' /usr/local/tomcat/conf/server.xml && sed -i \"s/Connector port=\\\"8080\\\"/Connector port=\\\"${PORT:-10000}\\\"/g\" /usr/local/tomcat/conf/server.xml && catalina.sh run"]