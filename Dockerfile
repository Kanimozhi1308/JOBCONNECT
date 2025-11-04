# ===============================
# 1. Run stage
# ===============================
FROM openjdk:17-jdk-slim

# Set working directory inside container
WORKDIR /app

# Copy jar file from your project to container
COPY target/jobconnect-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]



