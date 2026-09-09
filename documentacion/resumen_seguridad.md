# Resumen de Hardening y Securización - Venta de Entradas API

He completado el endurecimiento y securización de la aplicación según el Plan de Implementación. 

A continuación el detalle de los cambios realizados y los riesgos restantes:

## Cambios Realizados

### 1. Gestión de Secretos y Configuración
- **Se eliminaron los fallbacks inseguros**: En `application.yml` ya no existen contraseñas ni secretos quemados por defecto (como `jwt.secret` o `mercadopago.access-token`). Si no se proveen en el `.env`, la aplicación fallará apropiadamente, previniendo su despliegue con valores inseguros.
- **Admin Setup Seguro**: Se eliminó la migración Flyway que insertaba un usuario Admin con contraseña plana. En su lugar, se creó `AdminSetupRunner` que permite inyectar el usuario semilla al arrancar el contenedor a través de las variables `ADMIN_EMAIL` y `ADMIN_PASSWORD`.
- Se parametrizaron `APP_FRONTEND_URL` y `APP_BACKEND_URL` para evitar el uso de URLs hardcodeadas (ej. `localhost` o `ngrok`).

### 2. Seguridad Web (JWT, Cookies, CORS, CSRF)
- **CORS Centralizado**: Se eliminaron los `@CrossOrigin("*")` y se configuró un `CorsConfigurationSource` global en `SecurityConfig` restringiendo orígenes y cabeceras adecuadamente.
- **Cookies HttpOnly**: Se endureció `CookieUtil.java`. Ahora, los tokens se devuelven en cookies con `SameSite=Strict` (o Lax según el token) y la bandera `Secure` se activa automáticamente en el perfil `prod` (requerirá HTTPS).
- **Protección de Swagger y Endpoints**: Se restringió el acceso a `/api/ventas/test-mp-direct` y toda la UI de Swagger/OpenAPI. Ahora requieren privilegios `ROLE_ADMIN`.
- **Protección CSRF**: Se habilitó la protección CSRF nativa con un `CookieCsrfTokenRepository` para compatibilidad con Single Page Applications.

### 3. Lógica de Negocio y Webhooks
- **Bloqueo Pesimista (Stock Atómico)**: Se implementó un `@Lock(LockModeType.PESSIMISTIC_WRITE)` en `EventoRepository` que es invocado por el `VentaService`. Esto previene condiciones de carrera donde compras simultáneas masivas resultan en una sobreventa del stock.
- **Estado Inicial de Venta**: Las entradas generadas antes del pago ahora se marcan como `RESERVADA`, y sólo pasan a `VALIDA` cuando Mercado Pago devuelve un Webhook de aprobación.
- **Validación DTO**: Se añadió `@Valid` y restricciones `@Max` a los items de compra en `CompraRequestDTO`.
- **Logs Seguros en Webhooks**: Se limpiaron los `System.out.println` del `MercadoPagoWebhookController`. Se reemplazaron por `Slf4j` configurado para ocultar detalles sensibles en producción (`log.trace`). Las redirecciones utilizan la variable `frontendUrl`.

### 4. Manejo de Errores y Pruebas
- **Excepciones Seguras**: En `GlobalExceptionHandler`, los errores no interceptados (`Exception.class`) ya no filtran la traza completa ni los mensajes SQL de Hibernate al cliente, previniendo la fuga de información (Information Disclosure). Se devuelve un mensaje genérico al usuario mientras que el detalle queda únicamente en los logs internos.
- **Pruebas en Memoria**: Se instaló `H2` para que la fase de testing use una base de datos en memoria (ver `application-test.yml`). Se ejecutó exitosamente el comando `mvnw clean test`, por lo que el pipeline de compilación está en luz verde.

---

## Riesgos Restantes & Pasos para Producción

> [!WARNING]
> Ten en cuenta lo siguiente antes de desplegar:

1. **Requisito HTTPS**: Para que las cookies protegidas funcionen en `prod`, necesitas desplegar tras un Load Balancer (Nginx/AWS/GCP) que provea HTTPS, de otra manera la bandera `Secure` hará que el navegador rechace el JWT.
2. **Setup de Variables (.env)**: Asegúrate de tener listos los valores `ADMIN_EMAIL` y `ADMIN_PASSWORD` la primera vez que levantes el servidor de producción para obtener un usuario semilla.
3. **Manejo de Reintentos de Webhook (Idempotencia)**: Aunque el webhook ahora es mucho más seguro y transaccional, aún requerimos lógica estricta de idempotencia para garantizar que múltiples reintentos del mismo Webhook de MP no generen problemas (esto puede mitigarse revisando si el estado de la compra ya está en `COMPLETADA` al inicio del método).

> [!TIP]
> Puedes utilizar los siguientes comandos para crear un `.env` sólido:
> - Genera una clave JWT fuerte con: `openssl rand -base64 64`
> - Configura `SPRING_PROFILES_ACTIVE=prod` al correr la aplicación.
