# ---- build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- runtime ----
FROM tomcat:11.0-jdk21-temurin
# Remove os apps de exemplo do Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /build/target/smartpet.war /usr/local/tomcat/webapps/smartpet.war

# O Render injeta a porta em $PORT; o Tomcat precisa escutar nela.
# Também fixa UTF-8 e limita a heap para caber nos 512 MB do plano gratuito.
ENV CATALINA_OPTS="-Dfile.encoding=UTF-8 -Xms64m -Xmx320m"
COPY docker-entrypoint-port.sh /usr/local/bin/docker-entrypoint-port.sh
RUN chmod +x /usr/local/bin/docker-entrypoint-port.sh
EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/docker-entrypoint-port.sh"]
