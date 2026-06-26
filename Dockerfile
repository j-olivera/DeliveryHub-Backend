# Build stage
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# Comando para actualizar desde docker los roles
#    docker exec -it deliveryhub-db psql -U postgres -d deliveryhub -c "UPDATE users SET role = 'ADMIN' WHERE email = 'admin@deliveryhub.com';"	
