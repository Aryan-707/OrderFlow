# === Build stage ===
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace
RUN apk add --no-cache maven

# Copy parent POM and base-domain first (dependency layer caching)
COPY pom.xml ./pom.xml
COPY base-domain ./base-domain
RUN mvn -f base-domain/pom.xml install -DskipTests -q

# Copy service source and build
COPY payment-service ./payment-service
RUN mvn -f payment-service/pom.xml package -DskipTests -q

# === Runtime stage ===
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=build /workspace/payment-service/target/*.jar app.jar
RUN chown spring:spring app.jar
USER spring
ENTRYPOINT ["java", "-jar", "app.jar"]
