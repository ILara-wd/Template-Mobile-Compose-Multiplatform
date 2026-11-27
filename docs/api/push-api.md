# API de notificaciones push

| Dato | Valor |
|------|-------|
| Para qué | Avisarle al sensei de su membresía de Kensho, de novedades y de versiones nuevas aunque no tenga la app abierta |
| Decisión | ADR-027 (`docs/architecture/01-governance.md`) |
| Transporte | Firebase Cloud Messaging (FCM), API HTTP v1, mismo proyecto de Firebase que Crashlytics/Analytics |
| Consumidor actual | App Android (rol `SENSEI`); fase 2: tutores (`TUTOR`) |
| Fecha | 2026-11-24 |

## 1. Alcance

| Fase | Destinatario | Tipos |
|------|--------------|-------|
| **1 (ahora)** | Sensei | `MEMBERSHIP_DUE`, `MEMBERSHIP_OVERDUE`, `MEMBERSHIP_PAID`, `FEATURE_ANNOUNCEMENT`, `APP_UPDATE` |
| 2 | Tutores (cuando tengan app) | Se agregan tipos nuevos (pagos del alumno, avisos del dojo, exámenes) sin cambiar este contrato: el rol sale del token y el tipo desconocido abre la app |

Fuera de alcance en la fase 1: bandeja de notificaciones dentro de la app, preferencias por tipo en el servidor,
iOS (el contrato ya admite `IOS`).

## 2. Registro del dispositivo

Requieren `Authorization: Bearer <accessToken>`; el usuario y su rol salen del token, nunca del cuerpo.

| Método | Ruta | Éxito | Errores |
|--------|------|-------|---------|
| PUT | `/api/v1/me/devices/{installationId}` | 204 | 401, 422 |
| DELETE | `/api/v1/me/devices/{installationId}` | 204 (también si no existía) | 401 |

### `PUT` — alta o actualización (idempotente)

```json
{
  "fcmToken": "eXaMpLe:APA91b...",
  "platform": "ANDROID",
  "appVersion": "0.3.0",
  "locale": "es-MX",
  "notificationsEnabled": true
}
```

- **`installationId`**: UUID v4 que la app genera una vez y guarda en el dispositivo; sobrevive al cierre de
  sesión. Es la llave del registro: el token de FCM cambia (reinstalación, restauración, rotación) y el id no.
- **Upsert por `installationId`**: si ya existía, se reemplazan token, versión, idioma, permiso **y el usuario**.
  Así, si otra cuenta inicia sesión en el mismo teléfono, el dispositivo deja de pertenecer a la anterior.
- Si el mismo `fcmToken` está en otro `installationId`, se borra ese otro registro (un token, un dispositivo).
- `notificationsEnabled`: el permiso `POST_NOTIFICATIONS` del sistema (Android 13+) o las notificaciones de la app
  apagadas. El servidor puede seguir enviando (FCM lo acepta y Android no lo muestra); sirve para métricas y para
  no enviar en vano.
- 422 con `fields` si falta `fcmToken`, `platform` no es `ANDROID|IOS` o `appVersion` no es `major.minor.patch`
  (se admite sufijo `-x`).

**Cuándo lo llama la app**: al iniciar sesión, en `onNewToken`, al cambiar `versionName` o el permiso, y una vez
al día al abrir (para `lastSeenAt`). Si falla, reintenta en la siguiente ocasión; nunca bloquea la UI.

### `DELETE` — al cerrar sesión

La app lo llama **antes** de `POST /auth/logout` (todavía tiene Bearer) y borra la sesión local con cualquier
resultado. `POST /auth/logout-all` borra en el servidor todos los dispositivos del usuario.

## 3. Lo que envía el backend (mensaje FCM)

`notification` + `data`, no solo `data`: Android la muestra aunque la app esté cerrada o el fabricante restrinja
la batería. La app solo la pinta ella misma en primer plano y, al tocarla, lee `data` para decidir a dónde ir.

```json
{
  "message": {
    "token": "<fcmToken>",
    "notification": {
      "title": "Tu membresía vence en 3 días",
      "body": "Renueva para que tu dojo siga activo."
    },
    "android": {
      "priority": "HIGH",
      "notification": {
        "channel_id": "account",
        "tag": "membership",
        "click_action": "dev.treecode.kensho.OPEN_PUSH"
      }
    },
    "data": {
      "v": "1",
      "id": "ntf_01JD8X2Q9M",
      "type": "MEMBERSHIP_DUE",
      "url": "https://treecode.dev/kensho/membresia"
    }
  }
}
```

**`click_action` es obligatorio.** Sin él, Android abre la app con el mismo intent del ícono y, si ya estaba
abierta, solo la trae al frente sin entregarle `data`: no se cuenta el toque ni se abre la `url`.

### `data` (todos los valores son string, regla de FCM)

| Clave | Obligatoria | Descripción |
|-------|-------------|-------------|
| `v` | Sí | Versión del esquema de `data`. Hoy `"1"`; un cambio incompatible sube el número |
| `id` | Sí | Id del envío (del `notification_log`). La app lo usa para no duplicar y en Analytics, nunca lo muestra |
| `type` | Sí | Uno de §4. **Un tipo desconocido abre la app en su pantalla inicial**: así un tipo nuevo no rompe versiones viejas |
| `url` | No | `https` de `treecode.dev`. La app la abre en el navegador; cualquier otro esquema o dominio se ignora |

### Reglas del texto

- Lo escribe el servidor en español de México; la app no lo traduce ni lo arma.
- **Sin montos, correos, teléfonos ni nombres de alumnos**: el texto se ve en la pantalla de bloqueo.
- `tag` igual para el mismo asunto (`membership`): un aviso nuevo de membresía reemplaza al anterior en vez de
  apilarse.

## 4. Tipos y canales

Los canales los crea la app; el backend manda el `channel_id`. Si manda uno que la app no conoce, Android usa el
canal por defecto del manifiesto (`account`).

| `type` | Canal (`channel_id`) | Prioridad | Al tocar | Cuándo |
|--------|----------------------|-----------|----------|--------|
| `MEMBERSHIP_DUE` | `account` — Cuenta | `HIGH` | `url` (pago) o la app | 3 días antes y el día del vencimiento |
| `MEMBERSHIP_OVERDUE` | `account` | `HIGH` | `url` o la app | 1 y 7 días después; no más |
| `MEMBERSHIP_PAID` | `account` | `NORMAL` | La app | Al confirmar el pago |
| `FEATURE_ANNOUNCEMENT` | `news` — Novedades | `NORMAL` | `url` (nota) o la app | Manual, máximo 1 por semana |
| `APP_UPDATE` | `updates` — Actualizaciones | `NORMAL` | La app | Manual, a dispositivos con `appVersion` < `recommended` |

- **`APP_UPDATE` solo invita a abrir.** Lo que la app hace al abrir lo sigue decidiendo `GET /app/settings` en el
  splash (obligatoria, recomendada o nada). No se duplica esa lógica en el push.
- El sensei puede silenciar `news` y `updates` desde los ajustes del sistema sin perder `account`.

## 5. Backend: lo mínimo de la fase 1

1. **Tabla `devices`**: `installation_id` (PK), `user_id`, `role`, `dojo_id`, `platform`, `fcm_token` (único),
   `app_version`, `locale`, `notifications_enabled`, `created_at`, `last_seen_at`.
2. **Sender** con la API HTTP v1 (`POST https://fcm.googleapis.com/v1/projects/<id>/messages:send`) y una cuenta de
   servicio de Google; la llave nunca en el repo. Envío por token (no topics): permite filtrar por rol, dojo y
   versión con una consulta.
3. **Limpieza**: con `UNREGISTERED` (404) o `INVALID_ARGUMENT` por token, borrar el registro. Borrar también los
   que lleven más de 60 días sin `last_seen_at`.
4. **Tabla `notification_log`**: `id`, `user_id`, `type`, `title`, `body`, `url`, `sent_at`, resultado por
   dispositivo. Evita mandar dos veces el mismo aviso de membresía (llave única `user_id + type + periodo`) y es
   la base de la bandeja de la fase 2.
5. **Cron diario de membresía** a las 10:00 en la zona del dojo (nunca de noche), según §4; se detiene al pagar.
6. **Envío manual** (script o endpoint interno, fuera de este contrato) para `FEATURE_ANNOUNCEMENT` y
   `APP_UPDATE`, con modo de prueba a un solo `installationId`.

## 6. Pruebas sin backend

Mientras el backend no envía, la app se prueba desde la consola de Firebase (*Messaging → Enviar mensaje de
prueba*) con los campos de `data` de §3 en "Opciones adicionales". El token se lee con un breakpoint en
`onNewToken` o en el `PUT` de `MockWebServer`; **nunca se escribe en logs**, tampoco en debug (regla 10 de
`01-governance.md §5`).

## 7. En la app (implementado 2026-11-24)

| Pieza | Dónde |
|-------|-------|
| Registro, huella diaria y `DELETE` al cerrar sesión | `data/NetworkTkdPushRepository.kt`, `data/local/TkdPushStore.kt`, `data/push/TkdPushRegistrar.kt` |
| Servicio de FCM y notificación en primer plano | `data/push/TkdMessagingService.kt`, `data/push/TkdNotifications.kt` |
| Lectura y validación de `data` | `data/push/TkdPushMessage.kt` |
| Toque (en frío y con la app abierta) | `MainActivity.openPush` (`singleTop` + `OPEN_PUSH`) |
| Permiso (Android 13+) | `ui/TkdPushPermissionScreen.kt`, `ui/TkdPushPermissionViewModel.kt` (en el Resumen) |

Probado en el emulador (API 33): canales creados, explicación → permiso concedido → `PUT me/devices/{id}`
(el API de pruebas responde 405 hasta que exista el endpoint; se reintenta en la siguiente apertura), toque con
la app abierta y cerrada, `url` de otro dominio ignorada.

## 8. Pendiente

- Membresía: la `url` de pago y si habrá pantalla de membresía en la app (hoy no existe). Mientras tanto, sin
  `url` la notificación abre la app.
