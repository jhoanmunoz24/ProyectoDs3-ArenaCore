# ArenaCore (GolFlow) — Backend de 4 Microservicios

Sistema de gestión de canchas sintéticas y complejos deportivos construido con **Java 21**, **Spring Boot 3.5.14**, **PostgreSQL 16**, **Flyway** y **Docker Compose**.

## Arquitectura de Microservicios

| Servicio | Puerto | Base de Datos | Puerto Host DB | Responsabilidad |
| :--- | :--- | :--- | :--- | :--- |
| `identity-service` | `8081` | `db_identity` | `55431` | Usuarios, roles RBAC, auditoría, JWT RS256, `/.well-known/jwks.json` y tokens `SERVICE_INTERNAL`. |
| `courts-service` | `8080` | `db_courts` | `55432` | Sedes, canchas (`FUTBOL_5/7/8/11`), disponibilidad, reservas anti-overbooking, check-in y cierre. |
| `equipment-service` | `8082` | `db_equipment` | `55435` | Catálogo de implementos deportivos, stock, kardex, préstamos por `sesionJuegoId` y devoluciones. |
| `billing-service` | `8083` | `db_billing` | `55434` | Tarifas diurnas/nocturnas, liquidación consolidada con snapshot, cajas, pagos ACID y comprobantes. |

## Arranque con Docker Compose

```bash
cp .env.example .env
docker compose up --build -d
```

> Si algún puerto del host (por ejemplo `55433`) está ocupado por otro proyecto en Docker, ajusta `DB_EQUIPMENT_PORT` (u otro puerto) en tu archivo `.env` local antes de levantar Compose.

Verificar estado de salud de los 4 servicios y las 4 bases:

```bash
docker compose ps
```

## Credenciales Iniciales (Bootstrap)

- **Administrador**: `admin@arenacore.local` / `Admin1234!`
- **Operador**: `operador@arenacore.local` / `Operador1234!`

## Patrones Implementados

- **Database-per-Service** con migraciones Flyway versionadas (`Identity V1-V3`, `Courts V1-V4`, `Equipment V1-V3`, `Billing V1-V4`).
- **Separación de credenciales (RNF-02)**: rol migrador para DDL en Flyway y rol de aplicación para JPA en tiempo de ejecución.
- **Seguridad RS256 + JWKS** y aislamiento de rutas `/internal/v1/**` con rol técnico `ROLE_SERVICE_INTERNAL`.
- **Idempotency-Key** en creación de reservas, check-in, préstamos, liquidaciones, apertura de caja y pagos.
- **Orquestación HTTP fuera de `@Transactional`** (HT-05 / ADR-006) para evitar bloqueos de conexiones JDBC.
