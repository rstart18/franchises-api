# Proyecto Base Implementando Clean Architecture

## Antes de Iniciar

Empezaremos por explicar los diferentes componentes del proyectos y partiremos de los componentes externos, continuando con los componentes core de negocio (dominio) y por último el inicio y configuración de la aplicación.

Lee el artículo [Clean Architecture — Aislando los detalles](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)

# Arquitectura

![Clean Architecture](https://miro.medium.com/max/1400/1*ZdlHz8B0-qu9Y-QO3AXR_w.png)

## Domain

Es el módulo más interno de la arquitectura, pertenece a la capa del dominio y encapsula la lógica y reglas del negocio mediante modelos y entidades del dominio.

## Usecases

Este módulo gradle perteneciente a la capa del dominio, implementa los casos de uso del sistema, define lógica de aplicación y reacciona a las invocaciones desde el módulo de entry points, orquestando los flujos hacia el módulo de entities.

## Infrastructure

### Helpers

En el apartado de helpers tendremos utilidades generales para los Driven Adapters y Entry Points.

Estas utilidades no están arraigadas a objetos concretos, se realiza el uso de generics para modelar comportamientos
genéricos de los diferentes objetos de persistencia que puedan existir, este tipo de implementaciones se realizan
basadas en el patrón de diseño [Unit of Work y Repository](https://medium.com/@krzychukosobudzki/repository-design-pattern-bc490b256006)

Estas clases no puede existir solas y debe heredarse su compartimiento en los **Driven Adapters**

### Driven Adapters

Los driven adapter representan implementaciones externas a nuestro sistema, como lo son conexiones a servicios rest,
soap, bases de datos, lectura de archivos planos, y en concreto cualquier origen y fuente de datos con la que debamos
interactuar.

### Entry Points

Los entry points representan los puntos de entrada de la aplicación o el inicio de los flujos de negocio.

## Application

Este módulo es el más externo de la arquitectura, es el encargado de ensamblar los distintos módulos, resolver las dependencias y crear los beans de los casos de use (UseCases) de forma automática, inyectando en éstos instancias concretas de las dependencias declaradas. Además inicia la aplicación (es el único módulo del proyecto donde encontraremos la función "public static void main(String[] args)".

**Los beans de los casos de uso se disponibilizan automaticamente gracias a un '@ComponentScan' ubicado en esta capa.**

---

## Endpoints Disponibles

| Método | Ruta | Descripción | Respuesta |
|--------|------|-------------|-----------|
| POST | `/api/v1/franchises` | Crear una nueva franquicia | 201 `{id, name}` |
| PATCH | `/api/v1/franchises/{franchiseId}` | Actualizar nombre de franquicia | 200 `{id, name}` |
| POST | `/api/v1/franchises/{franchiseId}/branches` | Agregar sucursal a una franquicia | 201 `{id, name}` |
| GET | `/api/v1/franchises/{franchiseId}/products/top-stock` | Producto con más stock por sucursal | 200 `[{productId, productName, stock, branchId, branchName}]` |
| PATCH | `/api/v1/branches/{branchId}` | Actualizar nombre de sucursal | 200 `{id, name}` |
| POST | `/api/v1/branches/{branchId}/products` | Agregar producto a una sucursal | 201 `{productId, branchId, productName, stock}` |
| DELETE | `/api/v1/branches/{branchId}/products/{productId}` | Eliminar producto de una sucursal | 204 |
| PATCH | `/api/v1/branches/{branchId}/products/{productId}/stock` | Actualizar stock de un producto | 200 `{productId, branchId, productName, stock}` |
| PATCH | `/api/v1/products/{productId}` | Actualizar nombre de producto | 200 `{id, name}` |

### Documentación Swagger UI

Con la aplicación corriendo, accede a la documentación interactiva en:

```
http://localhost:8080/swagger-ui.html
```

---

## Ejecutar con Docker (Recomendado)

Levanta la API y PostgreSQL con un solo comando:

```bash
docker compose up --build
```

Esto:
- Construye la imagen de la API (multi-stage build)
- Levanta PostgreSQL 15.4 en el puerto `5432`
- Levanta la API en el puerto `8080` con el perfil `local` (sin SSL)
- Ejecuta las migraciones Flyway automáticamente al iniciar

### Verificar que todo funciona

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/swagger-ui.html
```

### Detener los contenedores

```bash
docker compose down
```

### Reconstruir desde cero (limpiar volumen de datos)

```bash
docker compose down -v
docker compose up --build
```

### Solo construir la imagen (sin levantar)

```bash
docker build -f deployment/Dockerfile -t franchises-api:latest .
```

### Ejecutar la imagen contra un PostgreSQL externo (ej. RDS)

```bash
docker run -p 8080:8080 \
  -e DB_HOST=tu-host-rds.amazonaws.com \
  -e DB_PORT=5432 \
  -e DB_NAME=franchises_db \
  -e DB_USER=franchises_admin \
  -e DB_PASSWORD=tu-password \
  franchises-api:latest
```

---

## Ejecutar sin Docker

Requiere PostgreSQL corriendo (puede ser el del docker-compose):

```bash
docker compose up -d postgres
./gradlew bootRun
```

---

## Tests y Cobertura de Código

### Ejecutar tests y generar reporte Jacoco

```bash
./gradlew test jacocoTestReport
```

El reporte HTML de cobertura se genera en cada módulo en:

```
build/reports/jacocoHtml/index.html
```

### Verificar umbral mínimo de cobertura (90%)

```bash
./gradlew check
```

Este comando ejecuta los tests, genera el reporte Jacoco y falla si la cobertura de líneas es menor al 90% en cualquier módulo.

### Pipeline completo (build + tests + cobertura)

```bash
./gradlew build
```

---

## Tecnologías

- **Java 25** + **Spring Boot 4.0.1** + **Spring WebFlux** (reactivo)
- **R2DBC** (PostgreSQL reactivo) + **Flyway** (migraciones)
- **MapStruct 1.5.5** (mapeo de objetos)
- **Jacoco 0.8.14** (cobertura de código ≥ 90%)
- **ArchUnit** (validación de arquitectura en tests)
- **SpringDoc 3.0.1** (documentación OpenAPI/Swagger)
