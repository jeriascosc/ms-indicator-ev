# ms-indicator-ev

API REST para gestionar y analizar el comportamiento de un proyecto mediante la metodología Earned Value Management

---

## Stack

| Componente | Versión |
|---|---|
| Java | 17 (Temurin 17.0.19+10) |
| Spring Boot | 4.1.0 |
| Base de datos | H2 en memoria + Spring Data JPA |
| Documentación | springdoc-openapi 3.0.3 (Swagger UI) |
| Cobertura | JaCoCo 0.8.15, umbral mínimo del 82 % |

## Requisitos previos

El proyecto usa un toolchain **portable**: no hace falta instalar Java ni Maven en el sistema.

- El JDK 17 vive en `c:\Trycore\tools\jdk-17.0.19+10`
- El Spring Boot CLI 4.1.0 vive en `c:\Trycore\tools\spring-4.1.0`
- Maven lo aporta el wrapper (`mvnw.cmd`), que se autodescarga en la primera ejecución

```powershell
# carga JAVA_HOME y el PATH en la sesión actual
. c:\Trycore\tools\env.ps1
java -version   # openjdk version "17.0.19"
```

## Build y ejecución

```powershell
# compilar, ejecutar los tests y validar el umbral de cobertura
.\mvnw.cmd clean verify

# levantar el servicio
.\mvnw.cmd spring-boot:run
#   o bien
java -jar target\ms-indicator-ev-0.0.1-SNAPSHOT.jar
```

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |
| Consola H2 | http://localhost:8080/h2-console (JDBC `jdbc:h2:mem:evdb`, usuario `sa`, sin contraseña) |
| Actuator | http://localhost:8080/actuator/health |

Al arrancar, `data.sql` precarga seis actividades que cubren los cuatro cuadrantes CPI/SPI, el caso de igualdad a 1 y el caso de indicadores no calculables.

## Modelo — entidad `activity`

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Long | Identificador autogenerado |
| `name` | String | Nombre de la actividad |
| `totalPlannedBudget` | BigDecimal | BAC — presupuesto total planificado |
| `porcentPlanned` | BigDecimal | Porcentaje planificado, **fracción 0.0 – 1.0** |
| `porcentComplete` | BigDecimal | Porcentaje completado, **fracción 0.0 – 1.0** |
| `actualCost` | BigDecimal | AC — costo real incurrido |
| `created` | String | Usuario que dio de alta el registro |
| `createDate` | LocalDateTime | Fecha de alta (automática, `@PrePersist`) |
| `createUpdate` | String | Usuario que realizó la última modificación |
| `createDateUpdate` | LocalDateTime | Fecha de modificación (automática, `@PreUpdate`) |

Los campos de usuario se toman de la cabecera opcional **`X-User`**; si no se envía, se registra `system`.

## Endpoints

La API se sitúa en el **nivel 2 del Modelo de Madurez de Richardson**: recursos con URIs propias, verbos HTTP con semántica correcta y códigos de estado significativos.

Las respuestas son **JSON plano**: los recursos individuales son objetos y las colecciones son arrays, sin envoltorios `_embedded` ni enlaces `_links`.

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| `GET` | `/api/v1/activities` | Listar actividades | 200 |
| `GET` | `/api/v1/activities/{id}` | Obtener una actividad | 200, 404 |
| `POST` | `/api/v1/activities` | Crear actividad | 201 + `Location`, 400 |
| `PUT` | `/api/v1/activities/{id}` | Actualizar actividad | 200, 400, 404 |
| `DELETE` | `/api/v1/activities/{id}` | Eliminar actividad | 204, 404 |
| `GET` | `/api/v1/activities/indicators` | Indicadores EVM de **todos** los registros | 200 |
| `GET` | `/api/v1/activities/{id}/indicators` | Indicadores EVM de una actividad | 200, 404 |
| `GET` | `/api/v1/activities/interpretations` | Interpretación de **todos** los registros | 200 |
| `GET` | `/api/v1/activities/{id}/interpretation` | Interpretación de una actividad | 200, 404 |

Los errores se devuelven como **`ProblemDetail` (RFC 7807)** con `application/problem+json`:

```json
{
  "type": "https://api.trycore.com/problems/validation-error",
  "title": "Datos de entrada invalidos",
  "status": 400,
  "detail": "La actividad enviada no supera las validaciones",
  "instance": "/api/v1/activities",
  "errors": { "porcentPlanned": "porcentPlanned debe estar entre 0.0 y 1.0" },
  "timestamp": "2026-07-29T00:32:32.24"
}
```

## Fórmulas EVM

Con `BAC = totalPlannedBudget` y `AC = actualCost`:

| Indicador | Fórmula |
|---|---|
| PV — Valor planificado | `porcentPlanned × BAC` |
| EV — Valor ganado | `porcentComplete × BAC` |
| CV — Variación de costo | `EV − AC` |
| SV — Variación de cronograma | `EV − PV` |
| CPI — Índice de desempeño del costo | `EV / AC` |
| SPI — Índice de desempeño del cronograma | `EV / PV` |
| EAC — Estimación al completar | `BAC / CPI` |
| VAC — Variación al completar | `BAC − EAC` |

Todos los valores se calculan con `BigDecimal` a escala 4 y redondeo `HALF_UP`. **Cuando un divisor es cero el indicador es indefinido y se devuelve como `null`**, no como cero; la interpretación lo reporta entonces como `"No calculable"`.

> EAC y VAC siguen la definición estándar del PMBOK (sobre `BAC`), no sobre `AC`.

## Interpretación

**Estado CPI**

| Condición | Resultado |
|---|---|
| `CPI = 1` | Gasto conforme a lo planeado |
| `CPI > 1` | Eficiencia de Costo |
| `CPI < 1` | Mal desempeño Financiero |

**Estado SPI**

| Condición | Resultado |
|---|---|
| `SPI = 1` | Avanza conforme a lo planeado |
| `SPI > 1` | Avanza más de lo previsto |
| `SPI < 1` | Retrazado |

**Análisis CPI vs SPI**

| Condición | Resultado |
|---|---|
| `CPI > 1` y `SPI > 1` | Proyecto ideal |
| `CPI < 1` y `SPI < 1` | Proyecto crítico |
| `CPI < 1` y `SPI > 1` | Proyecto con mayor gasto y retrasado |
| `CPI > 1` y `SPI < 1` | Rápido avance a mayor costo |
| Cualquier caso con `CPI = 1` o `SPI = 1` | Proyecto conforme a lo planeado |

> La última fila es una regla de cierre añadida: la especificación original solo define los cuatro cuadrantes estrictos (`>1` / `<1`) y no cubre la igualdad exacta a 1.

### Ejemplo

```bash
curl http://localhost:8080/api/v1/activities/1/interpretation
```

```json
{
  "activityId": 1,
  "activityName": "Cimentacion",
  "cpi": 1.2000,
  "spi": 1.2000,
  "cpiStatus": "Eficiencia de Costo",
  "spiStatus": "Avanza más de lo previsto",
  "cpiVsSpiAnalysis": "Proyecto ideal"
}
```

## Tests

```powershell
.\mvnw.cmd clean verify
```

71 tests repartidos en:

- `IndicatorServiceTest` — las ocho fórmulas, divisiones por cero y los tres bloques de interpretación, incluidos los casos de igualdad a 1
- `ActivityServiceTest` — CRUD con Mockito, entidad no encontrada y sellado de auditoría
- `ActivityControllerTest` / `IndicatorControllerTest` — slices `@WebMvcTest`: códigos de estado, forma del JSON (objeto plano o array, sin envoltorios) y errores de validación
- `GlobalExceptionHandlerTest` — los cinco tipos de `ProblemDetail`
- `ActivityTest` — callbacks `@PrePersist` / `@PreUpdate` e identidad de la entidad
- `MsIndicatorEvApplicationTests` — extremo a extremo con servidor real y H2

El informe de cobertura queda en `target/site/jacoco/index.html`. La regla `check` de JaCoCo **hace fallar el build si la cobertura de líneas baja del 82 %**; la cobertura actual es del **100 % de líneas** y el **98 % de ramas**.
