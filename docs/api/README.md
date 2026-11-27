# Plan de endpoints de Kensho

Índice de los documentos de API y del orden para quitar los datos fijos del prototipo (TD-KEN-012). El contrato
publicado es `kensho-api.yaml`; cada documento de esta carpeta describe lo que falta antes de pasarlo al contrato.
Para implementar un endpoint publicado en la app: `/kensho-endpoint` con el path y el JSON de ejemplo del documento.

| Fecha | 2026-11-07 |
|-------|------------|

---

## 1. Estado por pantalla

| Pantalla | Datos hoy | Documento | Endpoints | Estado |
|----------|-----------|-----------|-----------|--------|
| Login, registro, sesión | API | `kensho-api.yaml` | `auth/*`, `GET /me` | ✅ Publicado |
| Lista de alumnos | API (encabezado del dojo desde `/me`) | [students-api.md](students-api.md) | `GET /students` | ✅ Publicado |
| Perfil del alumno | API (encabezado del dojo desde `/me`; foto pendiente) | [student-profile-api.md](student-profile-api.md) | `GET /students/{id}` | ✅ Publicado |
| Ficha de alta | API | [student-registration-api.md](student-registration-api.md) | `GET /dojo/registration-setup`, `POST /students` | ✅ Publicado |
| Editar ficha y estado del alumno (sin Figma; reutiliza las tarjetas del alta) | API | [student-edit-api.md](student-edit-api.md) | `GET`/`PATCH /students/{id}/record`, `PUT /students/{id}/membership`, `GET /students?status=` | ⚠️ Probado contra el API local (guardar, pausar y reactivar); falta publicarlo en el API de pruebas |
| Mi Dojo y sus editores | API (el plan incluye la cuota de inscripción, `enrollmentFeeCents`) | [dojo-config-api.md](dojo-config-api.md) | `dojo/config`, `programs`, `schedules`, `settings` | ⚠️ En el contrato; falta publicarlo en el API de pruebas (404) |
| Encabezado de todas las pantallas | Nombre del dojo e instructor desde `/me`; logo y foto del prototipo | §2 de este documento | Ampliar `GET /me` | 📝 Propuesto |
| Resumen | Prototipo (salvo encabezado) | [dashboard-api.md](dashboard-api.md) | `GET /dojo/overview` (+ P1) | 📝 Propuesto |
| Pase de lista | API (foto del alumno pendiente) | [checkin-api.md](checkin-api.md) | `GET /attendance/today`, `GET`/`PUT`/`close` de sesiones | ⚠️ Probado contra el API local; falta publicarlo en el API de pruebas |
| Pagos y detalle de pago | API (foto del alumno pendiente; exámenes y eventos sin pantalla) | [payments-api.md](payments-api.md) | `GET /payments/summary`, `charges`, `charges/{id}`, `POST`/`DELETE .../payments` | ⚠️ Probado contra el API local (cobro, recibo y anulación); falta publicarlo en el API de pruebas |
| Perfil del instructor | `GET /me/profile`, `PUT /me/presence` y `PATCH /me` (editor "Datos del instructor") con red | [sensei-profile-api.md](sensei-profile-api.md) | `GET /me/profile`, `PUT /me/presence`, `PATCH /me` | ⚠️ Probado contra el API local (perfil, switch en el tatami y editor); falta publicarlo en el API de pruebas |
| Notificaciones push (sin pantalla; ADR-027) | — | [push-api.md](push-api.md) | `PUT`/`DELETE /me/devices/{installationId}` + `data` de FCM | ⚠️ Implementado en la app (2026-11-24); falta en el API (hoy 405) |

---

## 2. Encabezado del dojo: ampliar `GET /me`

Resumen, Alumnos, Perfil del alumno, Pase de lista y Pagos muestran el mismo encabezado: logo, nombre del dojo,
instructor y su foto. Desde 2026-11-07 el nombre del dojo y el del instructor salen de `GET /me` (la app ya no
usa `FakeTkdDashboardRepository.dojo()` para ellos). Faltan el logo y la foto, que siguen siendo imágenes del
prototipo. La propuesta es agregarlos a `Me` como campos **opcionales**, sin romper a quien ya lo consume:

```json
{
  "id": "b1536619-a5b2-4199-a506-0b72fe2269c4",
  "email": "sensei@kenshodojo.mx",
  "instructorName": "Marcus Ruiz",
  "dojoName": "Kensho Dojo",
  "discipline": "KARATE",
  "dojoLogoUrl": "https://cdn.treecode.dev/dojos/dojo-1/logo.png",
  "photoUrl": null
}
```

| Campo | Regla |
|-------|-------|
| `dojoLogoUrl` | Opcional; `null` o ausente → la app usa la marca de Kensho |
| `photoUrl` | Opcional; `null` o ausente → avatar con iniciales del instructor |

En la app: `TkdInstructorAccount` gana `dojoLogo: TkdImage?` y `photo: TkdImage?`; `DojoHeader` deja de tener
`@DrawableRes` (TD-KEN-012). La carga de imágenes por URL (Coil, ADR-017) es la misma que necesitan las fotos de
alumnos.

---

## 3. Orden sugerido

Cada paso deja una pantalla sin datos fijos y desbloquea al siguiente.

| # | Paso | Por qué en este orden |
|---|------|------------------------|
| 1 | Publicar `dojo-config` en el API de pruebas | Ya está en el contrato y en la app; sin él no se configuran programas ni horarios |
| 2 | `GET /me` con logo y foto (§2) + carga de imágenes por URL | Quita el último dato fijo del encabezado y desbloquea las fotos de alumnos |
| 3 | Pase de lista ([checkin-api.md](checkin-api.md)) | Usa los horarios y alumnos que ya existen; genera la asistencia que piden el perfil del alumno y el Resumen |
| 4 | Pagos ([payments-api.md](payments-api.md)) | Usa el plan de cobro de `dojo/settings`; genera los ingresos del perfil del instructor y del Resumen |
| 5 | Perfil del instructor ([sensei-profile-api.md](sensei-profile-api.md)) | Agrega identidad, agenda (horarios + asistencia) e ingresos (pagos) |
| 6 | Resumen ([dashboard-api.md](dashboard-api.md)) | Es un agregado de todo lo anterior: sesión en vivo, métricas, exámenes |

**Fuera de alcance por ahora:** buscador del Resumen, exámenes de grado (crear, aprobar), kiosco/QR, cobro con
pasarela y reportes descargables. Cada uno necesita diseño de producto antes que endpoint.

---

## 4. Reglas comunes

Las mismas de los documentos publicados (y de la guía `docs/android-integration-guide.md`):

- Token `Bearer` en todo salvo `auth/login|register|refresh`; el dojo sale del token, no de la ruta.
- `asOf` con offset en cada respuesta de lectura: es el "hoy" de la app (nunca `LocalDate.now()`).
- Dinero en **centavos** enteros + `currency` ISO 4217.
- Catálogos como códigos (`KARATE`, `GREEN`, `KYU`, `PRESENT`); la app los traduce y descarta los desconocidos.
- Textos de interfaz en `strings.xml`; el API solo manda texto que captura el dojo (nombres, descripciones, notas).
- Errores `{ code, message }`; 422 con `fields[]` (`REQUIRED`, `INVALID_FORMAT`, `OUT_OF_RANGE`, `NOT_AVAILABLE`, `DUPLICATED`).
- Mutaciones que cobran o crean recursos con `Idempotency-Key`.
