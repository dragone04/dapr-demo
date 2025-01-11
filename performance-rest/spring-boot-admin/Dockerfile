FROM amazoncorretto:17

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file into the container at /app
COPY target/*.jar app.jar

EXPOSE 9082

# Specify the command to run your application
CMD ["java", "-jar", "app.jar"]