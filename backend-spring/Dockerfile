# ---- build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=production
COPY --from=build /build/target/backend-0.0.1-SNAPSHOT.jar app.jar

# O Render injeta a porta em $PORT
ENV PORT=8080
ENV JAVA_OPTS="-Xms64m -Xmx320m -Dfile.encoding=UTF-8"
EXPOSE 8080

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
