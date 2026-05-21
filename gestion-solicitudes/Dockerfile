# =========================
# Etapa 1: Compilación
# =========================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia primero el pom.xml para aprovechar la caché de dependencias
COPY pom.xml .

RUN mvn -B dependency:go-offline

# Copia el código fuente
COPY src ./src

# Compila el proyecto y genera el .jar
RUN mvn -B clean package -DskipTests


# =========================
# Etapa 2: Ejecución
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Usuario no root para mayor seguridad
RUN useradd -r -u 1001 springuser

# Copia únicamente el .jar generado
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

USER springuser

ENTRYPOINT ["java", "-jar", "app.jar"]