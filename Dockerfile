# ---------- Etapa 1: compilación con Maven ----------
FROM maven:3.9-eclipse-temurin-26 AS build
WORKDIR /build
# Primero solo el pom para aprovechar la caché de dependencias
COPY pom.xml lombok.config ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
# Empaquetamiento de la aplicación (sin ejecutar tests)
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: imagen final liviana (solo JRE) ----------
FROM eclipse-temurin:21-jre-alpine
LABEL org.opencontainers.image.title="usuarios-api" \
      org.opencontainers.image.description="Microservicio de Usuarios HackerFinder - EP2 DOY0101"

# Usuario sin privilegios
RUN addgroup -S app && adduser -S -G app -u 10001 app
WORKDIR /app
COPY --from=build /build/target/usuarios-*.jar app.jar

USER app
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
