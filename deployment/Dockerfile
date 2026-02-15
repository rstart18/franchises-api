# ====== Stage 1: Build ======
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /workspace

# Copiar archivos de configuración de Gradle (cache de dependencias)
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle main.gradle gradle.properties ./

# Copiar build.gradle de cada módulo (cache de dependencias)
COPY applications/app-service/build.gradle applications/app-service/
COPY domain/model/build.gradle domain/model/
COPY domain/usecase/build.gradle domain/usecase/
COPY infrastructure/entry-points/reactive-web/build.gradle infrastructure/entry-points/reactive-web/
COPY infrastructure/driven-adapters/r2dbc-postgresql-repository/build.gradle infrastructure/driven-adapters/r2dbc-postgresql-repository/

# Descargar dependencias (capa cacheada)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Copiar código fuente
COPY applications/ applications/
COPY domain/ domain/
COPY infrastructure/ infrastructure/

# Compilar (se omiten tests y validateStructure — se ejecutan en CI)
RUN ./gradlew bootJar -x test -x validateStructure --no-daemon

# ====== Stage 2: Runtime ======
FROM eclipse-temurin:25-jre-alpine

# Crear usuario non-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder /workspace/applications/app-service/build/libs/FranchisesApi.jar app.jar

RUN chown appuser:appgroup app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"

USER appuser

HEALTHCHECK --interval=30s --timeout=10s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
