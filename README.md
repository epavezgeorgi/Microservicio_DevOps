# Usuarios DevOps: Pipeline CI/CD (EP2 · DOY0101 Ingeniería DevOps)

Microservicio de gestión de **Usuarios, Productos y Tarjetas** (HackerFinder), desarrollado en **Java 21** con **Spring Boot 4** y **Maven**, con un pipeline CI/CD en **GitHub Actions** que incluye pruebas automatizadas, análisis de seguridad, contenedores, despliegue automático y orquestación.

**Integrantes:** _Nombre 1_ · _Nombre 2_

---

## 1. Endpoints

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/usuarios/register` | Registra un usuario (contraseña guardada con hash BCrypt) |
| POST | `/api/usuarios/login` | Inicio de sesión |
| GET | `/api/usuarios`, `/api/usuarios/{id}`, `/api/usuarios/username/{username}` | Consultas de usuarios |
| POST / GET | `/api/productos`, `/api/productos/{id}`, `/api/productos/categoria/{categoria}` | Productos |
| GET | `/api/status` | Estado de la aplicación y de la base de datos |
| GET | `/actuator/health` (`/liveness`, `/readiness`) | Health checks para Docker y Kubernetes |

### Ejecutar en local

```bash
./mvnw verify                      # compila, ejecuta pruebas y verifica cobertura
cp .env.example .env               # definir contraseñas de la base de datos
docker compose up -d --build       # MySQL + 2 réplicas de la API + Nginx
curl http://localhost:8080/actuator/health
```

Toda la arquitectura corre **en local**: la base de datos MySQL es un contenedor más (con su volumen `db_data`), así que no depende de ningún servidor externo.

**Correr la API desde el IDE (solo MySQL en Docker):**

```bash
docker compose up -d db
DB_PASSWORD=<la clave de tu .env> ./mvnw spring-boot:run
```

En IntelliJ o VS Code basta con agregar la variable de entorno `DB_PASSWORD` en la configuración de ejecución.

---

## 2. Estructura del repositorio

```
├── src/main/java/...            # microservicio (controllers, services, repositories, models)
├── src/test/java/...            # pruebas unitarias (JUnit 5 + Mockito + MockMvc)
├── Dockerfile                   # imagen multi-stage, usuario no-root, healthcheck
├── docker-compose.yml           # orquestación: MySQL + 2 réplicas + balanceador Nginx
├── nginx/nginx.conf             # balanceo de carga
├── k8s/                         # Kubernetes: Namespace, Secret, MySQL, Deployment, Service, HPA
├── .env.example                 # variables de entorno requeridas (sin secretos reales)
└── .github/
    ├── dependabot.yml           # escaneo de dependencias (Maven, Docker, Actions)
    └── workflows/ci-cd.yml      # pipeline CI/CD
```

---

## 3. Pipeline CI/CD

Se ejecuta en cada `push` a `develop` o `main`, en cada Pull Request hacia esas ramas, y manualmente (`workflow_dispatch`).

```
push / PR (develop, main)
      │
      ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────────┐   ┌────────────────────┐
│ 1. Test      │──▶│ 2. Seguridad │──▶│ 3. Build + Trivy │──▶│ 4. Deploy (main)   │
│ JUnit+Mockito│   │ Snyk SCA +   │   │ + push a GHCR    │   │ Docker Compose     │
│ JaCoCo ≥70%  │   │ Snyk Code    │   │                  │   │ + smoke tests      │
└──────────────┘   └──────────────┘   └──────────────────┘   └────────────────────┘
      │ si falla cualquier etapa
      ▼
 5. Alerta: se crea un Issue automático con el enlace al run
```

| Etapa | Qué hace | Indicador |
|---|---|---|
| **Test** | `./mvnw verify`: compila y ejecuta las pruebas unitarias (JUnit 5, Mockito, MockMvc) de controladores y servicios. **JaCoCo detiene el build si la cobertura de líneas baja del 70 %.** Publica los reportes de Surefire y JaCoCo como artefactos. | IE2 |
| **Seguridad** | Snyk analiza las dependencias Maven (SCA) y el código fuente (SAST). **Bloquea** el pipeline ante vulnerabilidades altas o críticas. Dependabot revisa semanalmente Maven, Docker y GitHub Actions y abre PRs hacia `develop`. | IE3 |
| **Build** | Construye la imagen Docker etiquetada con el SHA del commit, la escanea con **Trivy** (bloquea con CRITICAL/HIGH) y la publica en GitHub Container Registry con las etiquetas `<sha>` y `<rama>`. | IE1, IE3 |
| **Deploy** | Solo desde `main`: levanta MySQL, 2 réplicas de la API y Nginx con Docker Compose usando la imagen exacta del commit, con contraseñas aleatorias generadas en el momento. Ejecuta smoke tests (health, status, registro y login). | IE4, IE5 |
| **Alerta** | Si una etapa falla, abre un Issue con la rama, el commit, el autor y el enlace al run. | IE3 |

El pipeline respeta GitFlow: en `develop` se valida todo (pruebas, seguridad, imagen) y **solo `main` despliega**.

### Configuración requerida en GitHub

1. Crear una cuenta en [snyk.io](https://snyk.io), copiar el **API token** y agregarlo en *Settings → Secrets and variables → Actions* como `SNYK_TOKEN`.
2. Activar *Dependabot alerts* y *Dependabot security updates* en *Settings → Code security*.
3. (Opcional) Crear el environment `produccion-simulada` en *Settings → Environments* para exigir aprobación manual antes del despliegue.
4. Proteger `main` y `develop` en *Settings → Branches*, exigiendo los checks **Pruebas unitarias**, **Análisis de seguridad (Snyk)** y **Build y escaneo de imagen** antes de hacer merge.

---

## 4. Contenedores, seguridad y escalabilidad

**Dockerfile**
- Build **multi-stage**: se compila con `maven:3.9-eclipse-temurin-21` y la imagen final usa solo el JRE (`eclipse-temurin:21-jre-alpine`), mucho más liviana.
- Las dependencias se descargan en una capa aparte, lo que aprovecha la caché de Docker.
- Corre con un **usuario sin privilegios** (`uid 10001`), con `HEALTHCHECK` y memoria de la JVM ajustada al contenedor (`MaxRAMPercentage`).

**Docker Compose** (orquestación usada en el deploy)
- MySQL 8.4 con volumen persistente y healthcheck. La API solo arranca cuando la base de datos está sana.
- `replicas: 2` de la API detrás de **Nginx como balanceador de carga**.
- Límites de CPU y memoria, reinicio automático, sistema de archivos **read-only** y `no-new-privileges`.
- Las credenciales se leen desde variables de entorno (`.env`), nunca desde el código.

**Kubernetes** (`k8s/`, para un clúster real como minikube o kind)
- `Deployment` con **RollingUpdate** (`maxUnavailable: 0`, sin caída), probes de *startup*, *readiness* y *liveness*, y `requests/limits`.
- `securityContext`: no-root, sin escalada de privilegios, `readOnlyRootFilesystem`, sin capabilities.
- Credenciales en un `Secret`, MySQL con `PersistentVolumeClaim`.
- `HorizontalPodAutoscaler`: escala de 2 a 5 pods al 70 % de CPU.

```bash
minikube start && minikube addons enable metrics-server
eval $(minikube docker-env) && docker build -t usuarios-api:latest .
kubectl apply -f k8s/namespace.yaml && kubectl apply -f k8s/
minikube service usuarios-api -n hackerfinder
```

**Mejoras de seguridad respecto a la evaluación anterior**
- Se eliminaron del repositorio las credenciales de la base de datos (antes estaban escritas en `application-*.properties`); ahora se inyectan por variables de entorno o Secrets.
- Las contraseñas de usuarios se guardan con **hash BCrypt** en lugar de texto plano, y no se devuelven en las respuestas JSON.
- Actuator solo expone `health` e `info`, sin detalles internos.

---

## 5. Trazabilidad y calidad

**Trazabilidad (del commit a producción)**
- Cada imagen se etiqueta con el **SHA del commit** y la rama, y lleva labels OCI (`revision`, `source`), así que cualquier contenedor en ejecución se puede rastrear hasta su código.
- El job de deploy publica un **resumen** (commit, rama, autor, imagen, enlace al run) y guarda los **logs del despliegue** como artefacto.
- Los reportes de pruebas (Surefire) y cobertura (JaCoCo) quedan adjuntos a cada ejecución.
- GitFlow, Conventional Commits y Pull Requests obligatorios permiten seguir cada cambio desde su `feature/` hasta `main`.
- El environment `produccion-simulada` registra el historial de despliegues en GitHub.

**Calidad**
- Ninguna imagen se construye si fallan las pruebas o si la cobertura baja de 70 %.
- Ninguna imagen se despliega si Snyk o Trivy detectan vulnerabilidades altas o críticas.
- Dependabot mantiene las dependencias actualizadas mediante PRs que pasan por el mismo pipeline.
- Las fallas generan un Issue automático para que el equipo reaccione.

---

## Estrategia de ramificación: GitFlow

Este proyecto utiliza **GitFlow** como estrategia de ramificación, por las siguientes razones:

- Permite separar claramente el código **estable en producción** (`main`) del código **en integración** (`develop`).
- Facilita el trabajo colaborativo: cada nueva funcionalidad se desarrolla de forma aislada en una rama `feature/`, sin afectar el trabajo de otros integrantes ni el código estable.
- Ofrece un mecanismo claro para **arreglos urgentes** (`hotfix/`) que deben llegar a producción sin esperar el ciclo normal de desarrollo.
- Es un modelo ampliamente documentado y estándar en la industria, lo que facilita la incorporación de nuevos desarrolladores al equipo.

Se descartó trunk-based development porque, al ser un equipo simulando distintos flujos (features y hotfixes en paralelo), GitFlow ofrece más trazabilidad sobre el origen y destino de cada cambio.

### Estructura de ramas

| Rama | Propósito |
|---|---|
| `main` | Código estable, listo para producción. Solo recibe merges desde `develop` (releases) o `hotfix/*` (arreglos urgentes). |
| `develop` | Rama de integración. Todos los `feature/*` se mergean aquí antes de pasar a `main`. |
| `feature/<nombre>` | Nueva funcionalidad. Nace de `develop` y se mergea de vuelta a `develop` vía Pull Request. |
| `hotfix/<nombre>` | Arreglo urgente sobre producción. Nace de `main` y se mergea a `main` **y** a `develop`. |



## Convención de commits

Se utiliza el estándar [Conventional Commits](https://www.conventionalcommits.org/):

- `feature:` nueva funcionalidad (ej. `feature: agrega modelo de Tarjeta`)
- `fix:` corrección de errores (ej. `fix: corrige contraseña datasource`)
- `docs:` cambios en documentación

## Naming de ramas

- Features: `feature/<descripcion-corta-en-minusculas>` — ej. `feature/repositorio-tarjeta`
- Hotfixes: `hotfix/<descripcion-corta-en-minusculas>` — ej. `hotfix/hotfix-1.1`

## Flujo de merge

1. **Feature → develop**: se abre un Pull Request desde `feature/<nombre>` hacia `develop`. Tras revisión, se mergea y se elimina la rama feature.
2. **Hotfix → main y develop**: se abre un Pull Request desde `hotfix/<nombre>` hacia `main`. Una vez mergeado, el cambio se replica también a `develop` (merge o cherry-pick) para que no se pierda en la siguiente release.
3. **Develop → main**: al cerrar un ciclo de desarrollo (release), se abre un Pull Request de `develop` hacia `main`.

## Estrategia de revisión de Pull Requests

- Todo cambio a `develop` o `main` se realiza mediante Pull Request, nunca con push directo.
- Antes de aprobar un PR se revisa:
  - Que el build compile correctamente (validado automáticamente por GitHub Actions).
  - Que el mensaje de commit siga la convención definida.
  - Que la rama de destino sea la correcta (`feature` → `develop`, `hotfix` → `main`).
