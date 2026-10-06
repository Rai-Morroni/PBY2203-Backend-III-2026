# Usa una imagen base ligera de Java
FROM eclipse-temurin:21-jre-alpine

# Define el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo empaquetado desde la carpeta target al contenedor
COPY target/*.jar app.jar

# Expone el puerto por defecto
EXPOSE 8080

# Comando de inicio del microservicio
ENTRYPOINT ["java", "-jar", "app.jar"]