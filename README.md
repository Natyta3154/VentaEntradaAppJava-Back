# 🎟️ API de Venta de Entradas para Eventos

Sistema backend robusto desarrollado en **Java** con **Spring Boot**, diseñado para la gestión integral de eventos, venta de entradas con códigos QR, integración con pasarelas de pago (Mercado Pago), control de aforos y panel administrativo en tiempo real.

---

## 📑 Tabla de Contenidos
1. [Arquitectura General y Tecnologías](#-tecnologías-utilizadas)
2. [Estructura de Módulos (Explicación Detallada)](#-módulos-del-sistema)
   - [1. Módulo `admin`](#1-módulo-admin)
   - [2. Módulo `auth`](#2-módulo-auth)
   - [3. Módulo `core`](#3-módulo-core)
   - [4. Módulo `eventos`](#4-módulo-eventos)
   - [5. Módulo `usuarios`](#5-módulo-usuarios)
   - [6. Módulo `ventas`](#6-módulo-ventas)
3. [¿Cómo funcionan las Migraciones de Base de Datos (Flyway)?](#-migraciones-de-base-de-datos-flyway)
4. [Historial de Migraciones del Proyecto](#-historial-de-migraciones-del-proyecto)
5. [Variables de Entorno y Configuración](#-variables-de-entorno-y-configuración)
6. [Cómo ejecutar el proyecto](#-cómo-ejecutar-el-proyecto)

---

## 🚀 Tecnologías Utilizadas

- **Lenguaje:** Java 17+
- **Framework:** Spring Boot (Spring Web, Spring Security, Spring Data JPA)
- **Base de Datos:** MySQL
- **Migraciones de BD:** Flyway
- **Autenticación:** JWT (JSON Web Tokens) con Refresh Tokens
- **Pasarela de Pagos:** Mercado Pago SDK
- **Almacenamiento de Imágenes:** Cloudinary
- **Emails:** Spring Mail (SMTP Gmail)
- **Códigos QR:** ZXing (Zebra Crossing)
- **Boilerplate reduction:** Lombok

---

## 📦 Módulos del Sistema

La aplicación está organizada mediante una arquitectura modular por capas (`controllers`, `services`, `repositories`, `models`, `dtos`), donde cada paquete atiende una responsabilidad específica del negocio:

```
src/main/java/com/example/venta_entrada/
│
├── admin/          --> Dashboard, métricas globales, ocupación y alertas de stock
├── auth/           --> Autenticación, registro, JWT, refresh tokens y recuperación de contraseñas
├── core/           --> Seguridad global, manejo de excepciones, Cloudinary, emails y configs
├── eventos/        --> Eventos, lugares/recintos, artistas/DJs, categorías y tipos de tickets
├── usuarios/       --> Gestión de usuarios, roles, perfiles y estados
└── ventas/         --> Proceso de compra, checkout Mercado Pago, generación/validación de QR
```

---

### 1. Módulo `admin`
**Propósito:** Proporcionar información gerencial, estadísticas y alertas en tiempo real exclusivamente a los administradores (`ROLE_ADMIN`).

* **¿Qué hace?**
  * **Métricas Globales:** Calcula el total recaudado en dinero, ventas del día, entradas vendidas, entradas ya escaneadas/validadas en puerta, cantidad de usuarios activos y devoluciones realizadas.
  * **Ocupación de Eventos:** Calcula en tiempo real el porcentaje de aforo vendido de cada evento activo para conocer la ocupación de los recintos.
  * **Alertas de Stock Crítico:** Detecta y notifica cuando un tipo de entrada tiene **20 o menos unidades disponibles** en eventos vigentes, permitiendo tomar decisiones de marketing o habilitar más capacidad.

---

### 2. Módulo `auth`
**Propósito:** Manejar el ciclo de vida de la sesión y la seguridad de los usuarios al registrarse o iniciar sesión.

* **¿Qué hace?**
  * **Registro (`/api/auth/register`):** Crea nuevos usuarios encriptando la contraseña con `BCryptPasswordEncoder` y asignándoles por defecto el rol de cliente.
  * **Login (`/api/auth/login`):** Valida credenciales y genera un par de tokens:
    1. `Access Token` (JWT de corta duración, ej. 15 minutos) para autorizar peticiones en la API.
    2. `Refresh Token` (de larga duración, ej. 7 días) almacenado en base de datos para renovar el acceso sin pedir credenciales nuevamente.
  * **Renovación (`/api/auth/refresh-token`):** Emite un nuevo JWT válido cuando el anterior ha expirado.
  * **Recuperación de Contraseña:** Envío de tokens temporales por correo electrónico para restablecer contraseñas olvidadas.

---

### 3. Módulo `core`
**Propósito:** Funcionar como el "núcleo transversal" que da soporte a todos los demás módulos de la aplicación.

* **¿Qué hace?**
  * **Seguridad y Filtros (`security`):** Contiene el `JwtAuthenticationFilter`, encargado de interceptar cada petición HTTP, extraer el token del encabezado `Authorization: Bearer <token>`, validarlo e inyectar el usuario autenticado en el contexto de Spring Security.
  * **Manejo Global de Errores (`exceptions`):** Captura cualquier excepción (`ResourceNotFoundException`, `BadRequestException`, validaciones `@Valid`, errores de seguridad) y responde con un JSON uniforme y legible (`ErrorResponse`), evitando que el cliente reciba errores feos o trazas de servidor (500).
  * **Configuraciones Globales (`config`):** Configuración de CORS (Cross-Origin Resource Sharing) para conectar con el Frontend (React/Vue/Angular), configuración de beans de Cloudinary, etc.
  * **Servicios Utilitarios (`utils` / `services`):** Subida de imágenes a **Cloudinary**, envío asíncrono de correos electrónicos con **Spring Mail**, etc.

---

### 4. Módulo `eventos`
**Propósito:** Gestionar todo el catálogo cultural y de entretenimiento que se ofrece en la plataforma.

* **¿Qué hace?**
  * **Eventos:** Alta, baja lógica (`soft delete`), edición y consulta pública de eventos (conciertos, obras, fiestas, festivales). Permite filtrar por fecha, categoría y estado.
  * **Tipos de Entrada (`TipoEntrada`):** Define los distintos tipos de tickets para un evento (ej. "General", "VIP", "Early Bird"), su precio y su cupo máximo/stock.
  * **Lugares (`Lugar`):** Administra recintos, estadios o teatros con sus respectivas direcciones, ciudades y capacidad máxima de aforo.
  * **Artistas / DJs (`Artista`):** Gestiona los perfiles de los artistas que se presentan en cada evento, incluyendo fotos y biografías.
  * **Categorías:** Clasificación de eventos (Música, Teatro, Deportes, etc.).

---

### 5. Módulo `usuarios`
**Propósito:** Administración de las cuentas de usuario y el control de accesos basado en roles (RBAC).

* **¿Qué hace?**
  * **Usuarios:** Consulta y actualización de datos personales, teléfono, email, avatar y contraseñas.
  * **Roles y Permisos:** Control de roles (`ROLE_ADMIN`, `ROLE_CLIENTE`, `ROLE_ORGANIZADOR`), permitiendo restringir el acceso a ciertos endpoints según los privilegios.
  * **Borrado Lógico (`Soft Delete`):** Los usuarios y registros clave no se eliminan físicamente de la base de datos, sino que se marcan como inactivos para preservar el historial de compras y auditoría.

---

### 6. Módulo `ventas`
**Propósito:** Orquestar el flujo comercial, el cobro y la emisión/validación de entradas digitales.

* **¿Qué hace?**
  * **Creación de Orden de Compra (`Compra`):** Reserva las entradas y calcula el total monetario a pagar.
  * **Integración con Mercado Pago:** Genera la preferencia de pago para que el usuario pague en línea y escucha las notificaciones del servidor mediante **Webhooks** (`/api/ventas/webhook/mercadopago`) para confirmar el pago automáticamente.
  * **Generación de Entradas (`Entrada`):** Al confirmarse el pago, se generan los tickets con un **código único** y un **código QR descargable**.
  * **Envío de Tickets por Email:** Envía al correo del comprador el comprobante y las entradas con sus códigos QR adjuntos.
  * **Control de Acceso en Puerta (Validación):** Endpoint para que el personal del evento escanee el código QR y cambie el estado de la entrada de `VALIDA` a `USADA`, impidiendo que un boleto sea reutilizado.
  * **Gestión de Reembolsos:** Permite cancelar compras y liberar el stock de entradas cuando sea necesario.

---

## 🗄️ ¿Cómo funcionan las Migraciones de Base de Datos (Flyway)?

### ¿Qué es una migración de base de datos?
Imagina que las migraciones son como un **"Git para tu base de datos"**. En lugar de crear tablas o alterar columnas manualmente en MySQL Workbench (lo cual provoca errores y diferencias entre el entorno de desarrollo y producción), los cambios se escriben en **archivos de script SQL versionados**.

### ¿Cómo funciona en este proyecto?

1. **Ubicación:** Todos los scripts de migración están en:
   ```
   src/main/resources/db/migration/
   ```

2. **Convención de Nombres Obligatoria:**
   Flyway exige una nomenclatura muy específica:
   * **`V<Número>__<descripción_del_cambio>.sql`**
   * Ejemplo: `V1__create_initial_schema.sql`, `V2__compras_y_entradas.sql`
   * *Importante:* Lleva **dos guiones bajos (`__`)** entre la versión y el nombre.

3. **La tabla de control (`flyway_schema_history`):**
   * La primera vez que Spring Boot arranca, Flyway crea automáticamente en tu MySQL una tabla llamada `flyway_schema_history`.
   * En esa tabla, Flyway anota cada archivo que ya ejecutó, junto con la fecha y un código de verificación (checksum).

4. **Ejecución Automática al iniciar Spring Boot:**
   * Cuando inicias la aplicación (`mvn spring-boot:run` o desde tu IDE):
     1. Flyway se conecta a MySQL y lee la tabla `flyway_schema_history`.
     2. Revisa la carpeta `db/migration` en busca de archivos nuevos con una versión mayor a la última ejecutada.
     3. Ejecuta los scripts pendientes en orden cronológico estricto (`V1`, luego `V2`, luego `V3`...).
     4. Si todo sale bien, guarda el registro y la aplicación arranca.
     5. Si algún script tiene un error de sintaxis SQL, Flyway detiene el arranque para proteger la integridad de los datos.

5. **Regla de Oro:**
   > **Nunca modifiques un script SQL que ya fue ejecutado en tu base de datos.** Si necesitas cambiar una tabla o agregar una columna, simplemente crea una nueva versión (por ejemplo, `V15__mi_nuevo_cambio.sql`).

---

## 📜 Historial de Migraciones del Proyecto

| Archivo | Descripción del cambio |
|---|---|
| **`V1__create_initial_schema.sql`** | Crea las tablas base: `usuarios`, `roles`, `lugares`, `djs`, `eventos` y tablas intermedias. |
| **`V2__compras_y_entradas.sql`** | Crea las tablas `tipos_entrada`, `compras` y `entradas` para el sistema de ventas. |
| **`V3__pagos_y_reembolsos.sql`** | Crea las tablas para transacciones de `pagos` y solicitudes de `reembolsos`. |
| **`V4__extras_eventos.sql`** | Agrega tablas para categorías, imágenes secundarias de eventos y valoraciones/reseñas. |
| **`V5__insert_roles.sql`** | Inserta los roles iniciales del sistema (`ROLE_ADMIN`, `ROLE_CLIENTE`, `ROLE_ORGANIZADOR`). |
| **`V7__refresh_tokens_and_roles.sql`** | Crea la tabla `refresh_tokens` para mantener la sesión de los usuarios. |
| **`V8__rename_dj_to_artista.sql`** | Renombra la tabla y columnas de `djs` a `artistas` para abarcar bandas, cantantes y orquestas. |
| **`V9__add_soft_deletes.sql`** | Agrega columnas `deleted` o `activo` para implementar borrado lógico seguro. |
| **`V10__update_entradas_auditoria.sql`** | Agrega campos de auditoría (`fecha_uso`, `validado_por`) para el control de acceso con QR. |
| **`V11__insert_mock_data_for_frontend.sql`** | Inserta datos de prueba (eventos, lugares, tipos de entrada) para probar el frontend. |
| **`V12__add_foto_to_artistas.sql`** | Agrega el campo `foto_url` en la tabla `artistas`. |
| **`V13__add_activo_to_tipos_entrada.sql`** | Agrega la columna `activo` en `tipos_entrada` para habilitar/deshabilitar tickets. |
| **`V14__add_imagen_portada_to_eventos.sql`** | Agrega la columna `imagen_portada` en la tabla `eventos`. |

---

## ⚙️ Variables de Entorno y Configuración

Copia el archivo `.env.example` y renómbralo a `.env` en la raíz del proyecto, completando tus credenciales:

```properties
# Base de Datos MySQL
DB_URL=jdbc:mysql://localhost:3306/venta_entradas_db
DB_USERNAME=root
DB_PASSWORD=tu_password

# Seguridad JWT (Cadena secreta en Base64 de al menos 256 bits)
JWT_SECRET=tu_clave_secreta_super_segura_de_mas_de_32_caracteres_123456

# Cloudinary (Subida de fotos de eventos y artistas)
CLOUDINARY_CLOUD_NAME=tu_cloud_name
CLOUDINARY_API_KEY=tu_api_key
CLOUDINARY_API_SECRET=tu_api_secret

# Mercado Pago (Pasarela de pagos)
MERCADOPAGO_ACCESS_TOKEN=TEST-tu_access_token_de_mercadopago

# Correo Electrónico (Gmail SMTP para envío de entradas)
MAIL_USERNAME=tu_correo@gmail.com
MAIL_PASSWORD=tu_password_de_aplicacion_gmail

# URLs de la aplicación
APP_FRONTEND_URL=http://localhost:5173
APP_BACKEND_URL=http://localhost:8080
```

---

## 🏃 Cómo ejecutar el proyecto

1. **Crear la base de datos en MySQL:**
   ```sql
   CREATE DATABASE venta_entradas_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **Compilar y ejecutar la aplicación:**
   ```bash
   # En Windows (CMD o PowerShell)
   .\mvnw.cmd spring-boot:run

   # En Linux / macOS
   ./mvnw spring-boot:run
   ```

3. **Verificación:**
   Flyway ejecutará todas las migraciones automáticamente en tu base de datos y el servidor estará disponible en:
   ```
   http://localhost:8080
   ```
