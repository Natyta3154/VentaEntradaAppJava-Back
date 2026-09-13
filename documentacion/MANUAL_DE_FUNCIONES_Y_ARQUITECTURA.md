# 📘 Manual de Arquitectura, Funciones y Flujo del Sistema
## Sistema Integral de Venta de Entradas y Control de Accesos (Spring Boot + MySQL + Mercado Pago)

---

## 📑 Tabla de Contenidos
1. [Visión General y Arquitectura del Sistema](#1-visión-general-y-arquitectura-del-sistema)
2. [Seguridad y Autenticación (Módulo `auth` y `core/security`)](#2-seguridad-y-autenticación-módulo-auth-y-coresecurity)
3. [Gestión de Ventas, Pagos y Control de Accesos (Módulo `ventas`)](#3-gestión-de-ventas-pagos-y-control-de-accesos-módulo-ventas)
4. [Gestión de Eventos, Artistas y Multimedia (Módulo `eventos`)](#4-gestión-de-eventos-artistas-y-multimedia-módulo-eventos)
5. [Gestión de Usuarios y Roles (Módulo `usuarios`)](#5-gestión-de-usuarios-y-roles-módulo-usuarios)
6. [Mensajería y Soporte al Cliente (Módulo `contacto`)](#6-mensajería-y-soporte-al-cliente-módulo-contacto)
7. [Panel de Métricas y Estadísticas (Módulo `admin`)](#7-panel-de-métricas-y-estadísticas-módulo-admin)
8. [Servicios Transversales y Multimedia (Módulo `core`)](#8-servicios-transversales-y-multimedia-módulo-core)
9. [Guía de Modificación Futura (Mantenimiento Paso a Paso)](#9-guía-de-modificación-futura-mantenimiento-paso-a-paso)

---

## 1. Visión General y Arquitectura del Sistema

El backend está desarrollado siguiendo una **Arquitectura en Capas por Módulos de Dominio**, estructurada de la siguiente manera:

```text
src/main/java/com/example/venta_entrada/
├── admin/          -> Métricas globales, KPIs financieros y dashboard administrativo.
├── auth/           -> Autenticación JWT, Refresh Tokens y manejo seguro de cookies.
├── contacto/       -> Formulario público de soporte y respuestas por email.
├── core/           -> Seguridad global (Spring Security), CORS, CSRF, Cloudinary, excepciones y utilitarios.
├── eventos/        -> Gestión de eventos, artistas, categorías y material multimedia.
├── usuarios/       -> Modelos de Usuario, Roles y gestión de perfiles.
└── ventas/         -> Carrito de compras, integración Mercado Pago, generación QR, emails y scanner de acceso.
```

### Flujo Típico de una Petición (Request Lifecycle)
1. **Cliente Web / Móvil** envía una solicitud HTTP con sus cookies (`access_token`, `refresh_token`, `XSRF-TOKEN`).
2. **CORS Filter & SecurityFilterChain**: Verifica origen permitido, protección CSRF y ejecuta `JwtAuthenticationFilter`.
3. **Controlador (`@RestController`)**: Recibe el DTO validado (`@Valid`) y extrae el usuario autenticado.
4. **Servicio (`@Service`)**: Ejecuta la lógica de negocio, validaciones y transacciones de base de datos (`@Transactional`).
5. **Repositorio (`@Repository` / Spring Data JPA)**: Interactúa con la base de datos MySQL mediante Hibernate y consultas optimizadas.
6. **Respuesta**: Se mapea a un DTO de respuesta y retorna el código de estado HTTP correspondiente (200, 201, 400, 403, 500).

---

## 2. Seguridad y Autenticación (Módulo `auth` y `core/security`)

### Clases Principales
- `AuthController.java`: Controlador con los endpoints de login, refresh y logout.
- `RefreshTokenServiceImpl.java`: Gestión de persistencia y expiración de tokens de refresco en base de datos.
- `JwtService.java`: Generación, firma criptográfica con algoritmo HMAC-SHA y validación de tokens JWT.
- `JwtAuthenticationFilter.java`: Filtro interceptor que extrae la cookie `access_token` en cada petición y autentica al usuario en el contexto de Spring Security.
- `SecurityConfig.java`: Configuración de filtros, reglas de autorización por endpoint y políticas CORS/CSRF.

---

### Explicación de Funciones

#### 🔹 `AuthController.login(LoginRequest request, HttpServletResponse response)`
- **¿Qué hace?**: Autentica al usuario contra la base de datos con su email y contraseña.
- **¿Cómo funciona?**:
  1. Utiliza `AuthenticationManager` para verificar las credenciales con BCrypt.
  2. Si es válido, genera un `access_token` (JWT de corta duración, ej: 15 min) y un `refresh_token` (UUID guardado en BD de 7 días).
  3. Crea dos cookies HTTP-Only seguras y las agrega al `HttpServletResponse`.
  4. Retorna información del usuario (ID, nombre, rol, email).

#### 🔹 `AuthController.refreshToken(HttpServletRequest request, HttpServletResponse response)`
- **¿Qué hace?**: Renueva el `access_token` cuando expira sin obligar al usuario a volver a escribir sus credenciales.
- **¿Cómo funciona?**:
  1. Lee la cookie `refresh_token`.
  2. Verifica que el token exista en la base de datos y que no haya expirado (`verifyExpiration`).
  3. Genera un nuevo `access_token` JWT firmado y actualiza la cookie correspondiente.

#### 🔹 `AuthController.logout(HttpServletRequest request, HttpServletResponse response)`
- **¿Qué hace?**: Cierra la sesión activa.
- **¿Cómo funciona?**:
  1. Elimina el registro del `RefreshToken` en la base de datos.
  2. Envía cookies con `Max-Age=0` para que el navegador del cliente las borre de inmediato.

#### 🔹 `JwtService.generateToken(UserDetails userDetails)`
- **¿Qué hace?**: Crea un token JWT codificado y firmado criptográficamente.
- **¿Cómo funciona?**: Inserta el `subject` (email), los roles asignados (`ROLE_ADMIN`, etc.), la fecha de emisión (`iat`) y la fecha de vencimiento (`exp`), firmándolo con la clave secreta `jwt.secret`.

#### 🔹 `JwtAuthenticationFilter.doFilterInternal(...)`
- **¿Qué hace?**: Intercepta cada solicitud HTTP entrante antes de llegar a los controladores.
- **¿Cómo funciona?**: Lee la cookie `access_token`, valida su firma y caducidad mediante `JwtService`, carga el usuario en `SecurityContextHolder` para que Spring sepa quién está ejecutando la acción.

---

## 3. Gestión de Ventas, Pagos y Control de Accesos (Módulo `ventas`)

### Clases Principales
- `VentaService.java`: Orquestador principal de ventas y confirmación de webhooks.
- `MercadoPagoService.java`: Cliente de comunicación con la API de Mercado Pago.
- `AccesoService.java`: Motor de validación y escaneo de códigos QR en puerta.
- `AdminVentasServiceImpl.java`: Gestión administrativa de compras, listados y reembolsos.
- `EmailService.java`: Envío asíncrono de tickets por correo con QRs adjuntos.
- `QrCodeGeneratorUtil.java`: Generador de imágenes QR en memoria con la librería ZXing.

---

### Explicación de Funciones

#### 🔹 `VentaService.procesarCompra(CompraRequestDTO request, Usuario usuario)`
- **¿Qué hace?**: Reserva las entradas solicitadas y genera el enlace de pago de Mercado Pago.
- **¿Cómo funciona?**:
  1. Recorre los ítems del carrito.
  2. Aplica **Bloqueo Pesimista** (`findByIdWithLock`) sobre el evento para evitar que dos usuarios compren la última entrada al mismo milisegundo (prevención de sobreventa).
  3. Comprueba que `(capacidadTotal - vendidas) >= cantidadSolicitada`.
  4. Crea las `Entrada` en estado `RESERVADA` vinculadas a la `Compra` en estado `PENDIENTE`.
  5. Invoca a `MercadoPagoService.createPreference()` y obtiene el enlace de pago (`sandbox_init_point` o `init_point`).
  6. Devuelve el ID de compra y la URL de checkout.

#### 🔹 `VentaService.procesarNotificacionPago(Long paymentId)`
- **¿Qué hace?**: Recibe la notificación de Mercado Pago y emite las entradas definitivas.
- **¿Cómo funciona?**:
  1. Consulta a Mercado Pago el estado real del pago mediante su API.
  2. Si el estado es `"approved"`:
     - Cambia el estado de la compra a `COMPLETADA`.
     - Registra la entidad `Pago` con el monto y método utilizado.
     - Cambia las entradas a estado `VALIDA` (listas para ser usadas en puerta).
     - Ejecuta de forma asíncrona (`@Async`) `emailService.enviarEntradasPorEmail()` para generar los QR y enviarlos al correo del comprador.
  3. Si el estado es `"rejected"` o `"cancelled"`:
     - Cambia la compra y las entradas a estado `CANCELADA` (liberando automáticamente la capacidad del evento).

#### 🔹 `AccesoService.validarAccesoPuerta(ValidacionQrRequestDTO request, Usuario portero)`
- **¿Qué hace?**: Valida el código QR escaneado por el personal de control en el acceso al evento.
- **¿Cómo funciona?**:
  1. Busca la entrada por su UUID único de código QR.
  2. **Validación 1 (Existencia)**: Si no existe, retorna status `"rojo"` con mensaje "Entrada inexistente".
  3. **Validación 2 (Vigencia del Evento)**: Si el evento ya finalizó (`fechaFin < now`), retorna status `"rojo"`.
  4. **Validación 3 (Doble Uso / Fraude)**: Si el estado es `USADA`, retorna status `"rojo"` ("Entrada ya utilizada").
  5. **Validación 4 (Cancelación)**: Si el estado es `CANCELADA` (por reembolso), retorna status `"rojo"`.
  6. **Aprobación**: Si el estado es `VALIDA`:
     - Asigna `fechaIngreso = now()`.
     - Asigna `validadoPor = portero`.
     - Cambia el estado a `USADA`.
     - Retorna status `"verde"` con los datos del titular y tipo de entrada.

#### 🔹 `AdminVentasServiceImpl.procesarDevolucion(Long compraId)`
- **¿Qué hace?**: Realiza la devolución del dinero al comprador y anula los tickets.
- **¿Cómo funciona?**:
  1. Verifica que la compra esté en estado `COMPLETADA`.
  2. Obtiene el ID de pago de Mercado Pago y llama a la API de devoluciones (`PaymentRefundClient.refund()`).
  3. Al confirmarse el reembolso en MP, cambia la compra a `REEMBOLSADA`, el pago a `REEMBOLSADO` y todas las entradas a `CANCELADA`.

#### 🔹 `QrCodeGeneratorUtil.generateQrCodeBytes(String text)`
- **¿Qué hace?**: Convierte un texto (código UUID de la entrada) en un array de bytes (`byte[]`) correspondiente a una imagen PNG con el código QR.
- **¿Cómo funciona?**: Utiliza la librería Google ZXing con matriz de bits (`BitMatrix`) de 300x300 píxeles y la escribe en un flujo de salida `ByteArrayOutputStream`.

---

## 4. Gestión de Eventos, Artistas y Multimedia (Módulo `eventos`)

### Clases Principales
- `EventoServiceImpl.java`: Lógica CRUD de eventos, búsquedas, filtrado y asignación de artistas.
- `ArtistaServiceImpl.java`: Gestión del catálogo de artistas.
- `EventoImagenServiceImpl.java` & `EventoVideoServiceImpl.java`: Gestión de galerías multimedia.

---

### Explicación de Funciones

#### 🔹 `EventoServiceImpl.crearEvento(CrearEventoRequest request)`
- **¿Qué hace?**: Registra un nuevo evento en la plataforma.
- **¿Cómo funciona?**: Construye la entidad `Evento` con nombre, descripción, fecha, capacidad total, edad mínima y URL de la imagen de portada subida previamente a Cloudinary.

#### 🔹 `EventoServiceImpl.eliminarEvento(Long id)`
- **¿Qué hace?**: Aplica **Soft Delete (Borrado Lógico)** del evento.
- **¿Cómo funciona?**: En lugar de borrar la fila de la base de datos (lo que rompería la integridad referencial de compras pasadas), establece `evento.setActivo(false)`.

#### 🔹 `EventoServiceImpl.asignarArtistas(Long eventoId, AsignarArtistasRequest request)`
- **¿Qué hace?**: Vincula uno o varios artistas a un evento específico.
- **¿Cómo funciona?**: Valida la existencia de ambos y persiste registros en la tabla intermedia `evento_artistas` evitando duplicados (`existsByEventoIdAndArtistaId`).

#### 🔹 `EventoServiceImpl.crearTipoEntrada(Long eventoId, TipoEntradaRequestDTO request)`
- **¿Qué hace?**: Crea una categoría de entrada para el evento (ej: Campo General $5000, VIP $12000).
- **¿Cómo funciona?**: Asocia el tipo de entrada al evento con su precio, stock total y fechas de vigencia de venta.

---

## 5. Gestión de Usuarios y Roles (Módulo `usuarios`)

### Clases Principales
- `UsuarioServiceImpl.java`: Registro de clientes, cambios de roles, listados y edición.
- `UsuarioController.java`: Endpoints REST con seguridad basada en roles (`@PreAuthorize`).

---

### Explicación de Funciones

#### 🔹 `UsuarioServiceImpl.registrarUsuario(RegistroUsuarioRequest request)`
- **¿Qué hace?**: Da de alta una nueva cuenta de cliente en el sistema.
- **¿Cómo funciona?**:
  1. Valida que el email no esté en uso (`existsByEmail`). Si existe, lanza `EmailYaRegistradoException`.
  2. Busca el rol `"ROLE_CLIENTE"`.
  3. Encripta la contraseña enviada en texto plano utilizando `passwordEncoder.encode()` (BCrypt).
  4. Guarda el nuevo usuario con fecha de registro actual.

#### 🔹 `UsuarioServiceImpl.cambiarRol(Long id, CambiarRolRequest request)`
- **¿Qué hace?**: Permite a un administrador ascender o cambiar el rol de un usuario (ej: otorgar permisos de `ROLE_ADMIN` o `ROLE_PORTERO`).
- **¿Cómo funciona?**: Busca el usuario y el rol por su nombre, actualiza la clave foránea `rol_id` y actualiza la fecha de modificación.

---

## 6. Mensajería y Soporte al Cliente (Módulo `contacto`)

### Clases Principales
- `ContactoService.java`: Recepción y respuesta a mensajes de contacto.
- `ContactoController.java`: Endpoints para envío público y administración privada.

---

### Explicación de Funciones

#### 🔹 `ContactoService.guardarMensaje(ContactoRequestDTO dto)`
- **¿Qué hace?**: Recibe y almacena un mensaje enviado desde el formulario público de la web.
- **¿Cómo funciona?**: Guarda nombre, email, asunto y cuerpo del mensaje en la tabla `mensajes_contacto` con `leido = false` y `respondido = false`.

#### 🔹 `ContactoService.responderMensaje(Long id, String respuesta)`
- **¿Qué hace?**: Envía una respuesta oficial por correo electrónico al cliente y actualiza el estado.
- **¿Cómo funciona?**:
  1. Recupera el mensaje original.
  2. Usa `JavaMailSender` (`SimpleMailMessage`) para enviar el correo al email del remitente con copia del mensaje.
  3. Guarda la respuesta en la base de datos y marca `respondido = true` y `leido = true`.

---

## 7. Panel de Métricas y Estadísticas (Módulo `admin`)

### Clases Principales
- `AdminDashboardService.java`: Motor de cálculo estadístico y agregación de datos para el dashboard.
- `AdminDashboardController.java`: Endpoint protegido para suministrar datos en tiempo real al frontend del panel.

---

### Explicación de Funciones

#### 🔹 `AdminDashboardService.getMetrics()`
- **¿Qué hace?**: Retorna un resumen con todos los KPIs globales del sistema.
- **¿Cómo calcula cada dato?**:
  1. **Total Recaudado**: Suma los montos de todas las compras con estado `COMPLETADA`.
  2. **Ventas del Día**: Suma de compras completadas cuya fecha coincida con `LocalDate.now()`.
  3. **Entradas Vendidas**: Cuenta entradas en estado `VALIDA` o `USADA`.
  4. **Entradas Validadas**: Cuenta entradas en estado `USADA` (asistentes que ya ingresaron).
  5. **Usuarios Activos**: `usuarioRepository.count()`.
  6. **Eventos Activos**: Cuenta eventos con `activo = true`.
  7. **Compras Reembolsadas**: Cuenta compras con estado `REEMBOLSADA`.

#### 🔹 `AdminDashboardService.getOcupacionEventos()`
- **¿Qué hace?**: Calcula el porcentaje de ocupación y aforo disponible para cada evento activo.
- **¿Cómo funciona?**:
  - Para cada evento: `porcentaje = (entradasVendidas / capacidadTotal) * 100`.
  - Retorna lista de DTOs con la capacidad total, vendidas y porcentaje redondeado.

#### 🔹 `AdminDashboardService.getAlertasStock()`
- **¿Qué hace?**: Detecta tipos de entrada que están a punto de agotarse para alertar a los administradores.
- **¿Cómo funciona?**: Filtra tipos de entrada cuyo stock disponible sea menor al 20% o menor a 15 unidades.

---

## 8. Servicios Transversales y Multimedia (Módulo `core`)

### Clases Principales
- `CloudinaryService.java`: Servicio de subida y procesamiento de imágenes en la nube.
- `FileUploadController.java`: Endpoint `POST /api/upload` para subir imágenes (solo admins).
- `GlobalExceptionHandler.java`: Capturador centralizado de errores (`@RestControllerAdvice`) para retornar respuestas JSON limpias y estructuradas con códigos HTTP correctos.

---

## 9. Guía de Modificación Futura (Mantenimiento Paso a Paso)

### 📌 ¿Cómo cambiar el tiempo de duración de la sesión (JWT)?
1. Abre el archivo `src/main/resources/application.yml`.
2. Modifica los valores bajo la clave `jwt`:
   ```yaml
   jwt:
     expiration: 900000        # 15 minutos en milisegundos para el Access Token
     refresh-expiration: 604800000 # 7 días en milisegundos para el Refresh Token
   ```

### 📌 ¿Cómo agregar un nuevo campo a un Evento (ej: `direccionExacta`)?
1. Crea un nuevo script de migración SQL en `src/main/resources/db/migration/V16__add_direccion_to_eventos.sql`:
   ```sql
   ALTER TABLE eventos ADD COLUMN direccion_exacta VARCHAR(255) NULL;
   ```
2. Agrega el atributo en el modelo Java `src/main/java/com/example/venta_entrada/eventos/models/Evento.java`:
   ```java
   @Column(name = "direccion_exacta")
   private String direccionExacta;
   ```
3. Agrega el campo en los DTOs `CrearEventoRequest.java` y `EventoResponse.java`.
4. Mapea el nuevo campo en `EventoServiceImpl.java` y en `EventoMapper.java`.

### 📌 ¿Cómo cambiar el correo remitente o la plantilla de emails?
- Configura las credenciales en tu `.env` (`MAIL_USERNAME` y `MAIL_PASSWORD`).
- Edita el método `enviarEntradasPorEmail` en `src/main/java/com/example/venta_entrada/ventas/services/EmailService.java` para personalizar los estilos HTML y textos.

### 📌 ¿Cómo pasar de credenciales de prueba a producción en Mercado Pago?
- En tu archivo `.env` o en las variables de entorno de tu servidor (Render/Railway), actualiza:
  ```env
  MERCADOPAGO_ACCESS_TOKEN=APP_USR-tu-token-de-produccion-aqui
  ```
- En `MercadoPagoService.java`, cambia el retorno `sandbox_init_point` por `init_point` cuando desees cobrar con dinero real.

---
*Documentación generada automáticamente y mantenida para el equipo de desarrollo.*
