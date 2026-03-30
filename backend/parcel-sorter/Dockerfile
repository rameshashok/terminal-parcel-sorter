# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2: Run
FROM registry.access.redhat.com/ubi8/openjdk-17:1.23
ENV LANGUAGE='en_US:en'
COPY --chown=185 --from=builder /app/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 --from=builder /app/target/quarkus-app/*.jar /deployments/
COPY --chown=185 --from=builder /app/target/quarkus-app/app/ /deployments/app/
COPY --chown=185 --from=builder /app/target/quarkus-app/quarkus/ /deployments/quarkus/
EXPOSE 8081
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"
ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
