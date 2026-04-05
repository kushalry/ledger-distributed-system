# ==========================================
# STAGE 1: Build the application
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Run the application
# ==========================================
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy ONLY the built .jar file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the API port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]