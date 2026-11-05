# Vuelve+ MVP — Estado, deuda técnica y siguientes pasos

> Documento para retomar el proyecto. Fecha de corte: **5 nov 2026** · Rama: `vuelve_plus` · Último commit: `cbcb3eb`.
> Complementa al `README.md` (UI Kit / Atomic Design), que sigue siendo la referencia de diseño.

---

## 1. Resumen ejecutivo

- **Qué existe:** 9 pantallas del flujo MVP construidas en **Jetpack Compose + Material 3** a partir del archivo de Figma. Las 7 marcadas como ✅ en §2.1 replican el diseño y se validaron visualmente en emulador (Pixel 9a). Hay navegación básica entre pantallas e interacciones locales: filtros, búsqueda, validación de formulario, posponer y toggles.
- **Qué NO existe:** backend, persistencia, autenticación real, lógica de retención (cálculo de estados) ni integración con WhatsApp. **Todos los datos son de ejemplo** y viven junto a la UI (`Sample*UiState`).
- **Madurez:** prototipo navegable de alta fidelidad. Sirve para demos y validación con usuarios, pero no para producción.
- **Tamaño:** ~7,500 líneas de Kotlin en 14 archivos, 120 íconos vectoriales, 11 imágenes de ejemplo y ~270 strings/plurals.

### Stack actual

| Elemento | Valor |
|---|---|
| Módulos | Uno solo, `:app` (Android). **No es Kotlin Multiplatform**, aunque el repo se llame así |
| Paquete / nombre | `com.invex.one.template` / "Template" (pendiente de renombrar) |
| SDK | minSdk 24 · targetSdk/compileSdk 36 · JVM 11 |
| Versiones | AGP 8.13.2 · Kotlin 2.0.21 · Compose BOM 2024.09.00 |
| Dependencias | Solo AndroidX core, Activity Compose, Compose UI/Material 3 (sin navegación, DI, red, persistencia ni carga de imágenes) |
| Release | `isMinifyEnabled = false` |
| Pruebas | Solo los ejemplos de la plantilla (`ExampleUnitTest`, `ExampleInstrumentedTest`) |

---

## 2. Estado por pantalla

Archivo de Figma: `KqTFktvrRvCzO2idPga5xf` (Prototipo-Demo-UiKit).

| # | Pantalla | Nodo Figma | Archivo | Estado UI | Interacción real | Pendiente funcional |
|---|---|---|---|---|---|---|
| 1 | Iniciar sesión | `87:2049` | `ui/login/LoginScreen.kt` | ✅ | Tabs Tel/Correo, ver contraseña | Auth real, OTP por WhatsApp, Google, recuperar contraseña |
| 2 | Registro de negocio | `89:2171` | `ui/register/RegisterScreen.kt` | ✅ | Chips multiselección, checkbox de términos, links | Crear cuenta, validaciones, abrir términos/privacidad |
| 3 | Inicio / Oportunidades | `90:2326` | `ui/home/HomeScreen.kt` | ✅ | Navega a Seguimientos, Clientes y Detalle (solo María) | Datos reales, "Contactar" (WhatsApp), fecha y saludo dinámicos |
| 4 | Clientes | `91:2586` | `ui/clients/ClientsScreen.kt` | ✅ | Búsqueda sin acentos, filtros, "Nuevo" | Paginación, conteos reales, detalle de todos los clientes |
| 5 | Seguimientos | `92:2885` | `ui/followups/FollowUpsScreen.kt` | ✅ | Filtros, "Posponer 7d" (solo en memoria), "Ver perfil" | WhatsApp, persistir posposición, "Contactados este mes" |
| 6 | Detalle del cliente | `93:3217` | `ui/detail/ClientDetailScreen.kt` | ✅ | Solo datos de María | Editar, ⋮, Llamar, WhatsApp, Nueva visita |
| 7 | Mi Negocio (perfil) | `95:3425` | `ui/business/BusinessScreen.kt` | ✅ | Toggle de recordatorios, selector de periodo (solo visual) | Todo lo editable: reglas, plantilla, reporte, soporte |
| 8 | Nuevo cliente recurrente | `96:3729` | `ui/newclient/NewClientScreen.kt` | ✅ | Validación de requeridos, selectores, guardar con Toast | Persistir, "Registrar primera visita", frecuencia personalizada |
| 9 | Resultados | — (sin frame) | `MainActivity.kt` (`ComingSoonScreen`) | ⛔ "Próximamente" | — | Diseñar e implementar (ver README §4 y panel O7) |

### 2.1 Pantallas o flujos referenciados pero sin diseño

- **Registrar visita / ticket de atención** (desde Detalle, Inicio > Acciones rápidas y Nuevo cliente).
- **Editar cliente**, menú ⋮ del cliente, **ajustes y cuenta**, **catálogo de servicios**, **personalizar plantilla**, **modificar regla de frecuencia**.
- **Estados vacíos y de error**, **carga** (skeleton), **sin conexión**.
- **Empty state de búsqueda.** El banner "Simular búsqueda vacía" del Figma se omitió a propósito (README §6). La pantalla de Clientes muestra un mensaje simple.

---

## 3. Arquitectura actual

```
app/src/main/java/com/invex/one/template/
├── MainActivity.kt                # Navegación manual (enum + flags en rememberSaveable)
└── ui/
    ├── theme/                      # Color.kt (42 tokens), Type.kt, Theme.kt (solo claro, sin dynamic color)
    ├── components/
    │   ├── VuelveComponents.kt    # FlowScaffold, FlowTopBar (O2 auth), VuelveButton, VuelveTextField, subtleShadow
    │   └── VuelveMainShell.kt     # MainTab, MainScaffold, AppTopBar (O1), BottomBar (O3), DetailTopBar (O2)
    ├── login/  register/  home/  clients/  followups/  detail/  business/  newclient/
    │                               # Cada archivo: modelos + Sample*UiState + Screen (stateful) + Content (stateless) + Preview
```

- **Patrón por pantalla:** `XScreen` mantiene el estado con `rememberSaveable` y llama a `XContent`, que no tiene estado y recibe todo por parámetros y callbacks. Es una buena base para migrar a ViewModel.
- **Navegación:** `VuelveApp()` en `MainActivity` conmuta con `AppDestination` (Login/Register/Main), `MainTab` y banderas (`detailClientId`, `showBusiness`, `showNewClient`). Usa `return` temprano dentro del `when`.
- **Assets:** los SVG de Figma se convirtieron a VectorDrawable con un script local. Usan un prefijo por pantalla (`ic_login_*`, `ic_home_*`…) y `ic_vuelve_*` para los compartidos. Las fotos están en `drawable-nodpi`.

---

## 4. Deuda técnica (priorizada)

### 🔴 Alta (bloquea escalar o pasar a producción)

1. **Sin capa de datos ni de estado.**
   - Los modelos y los datos de ejemplo viven en los archivos de UI. No hay ViewModel, repositorios, DI ni `UiState` con carga y error.
   - *Acción:* crear `ViewModel + StateFlow` por pantalla y repositorios con interfaz. Al inicio, una implementación *fake* con los mismos datos de ejemplo.
2. **Navegación manual.**
   - No hay back stack real, deep links ni paso de argumentos tipado.
   - El detalle solo abre para `maria-luna` (`sampleClientDetail`).
   - *Acción:* Navigation Compose con rutas tipadas (`@Serializable`), un grafo de auth y otro principal con bottom nav.
3. **El motor de retención no existe.** Los estados (Al día / Toca contactar / Seguimiento hoy / Atrasado) están escritos a mano en los datos de ejemplo, y son el núcleo del producto.
4. **Vocabulario de estados inconsistente (README §6).** Hay 4 variantes visuales de badge para el mismo concepto:

   | Pantalla | Variantes |
   |---|---|
   | Inicio | "Toca servicio" / "Sin regresar" / "Seguimiento", con punto |
   | Clientes | "Toca contactar" / "Atrasado N días" / "Seguimiento hoy" / "Al día", sin punto |
   | Seguimientos | "14d retraso" / "Hoy sugerido" |
   | Detalle | "Corresponde contactar hoy" |

   *Acción:* un único `enum RetentionStatus` en dominio y un `StatusBadge` en el design system, con variantes de tamaño.
5. **Sin pruebas ni CI.** No hay pruebas unitarias, de UI ni de screenshot, ni pipeline.

### 🟠 Media (calidad y mantenibilidad)

6. **Componentes duplicados entre pantallas.** Se repiten:
   - Chips de filtro (3 implementaciones) y botones tonales.
   - Encabezados de sección con punto de color y avatares (iniciales, foto, mini-badge).
   - Wrappers locales de `VuelveTextField` (`RegisterTextField`, `FilledField`, `FilledInput`).
   - Tarjetas KPI/métricas y checkboxes (Registro, Nuevo cliente).

   *Acción:* extraer al design system (README §1.5–§3: átomos M*, organismos O*).
7. **Tokens fuera del `MaterialTheme`.**
   - Las pantallas importan los 42 `Vuelve*` directamente en vez de usar `MaterialTheme.colorScheme` o un `VuelveColors` por `CompositionLocal`.
   - No hay modo oscuro.
   - Hay **tres verdes de WhatsApp** (`#25D366`, `#006329`, `#006D2F`) y **dos azules primarios** (`#004AC6`, `#2563EB`) sin una regla clara de cuándo usar cada uno.
8. **Tipografía.**
   - Inter no está incluida; se usa Roboto (`FontFamily.Default`).
   - Hay muchos `.copy(...)` de estilos sueltos. Conviene ampliar la escala (`labelSmallMedium`, `metric`, etc.).
9. **Nombres de recursos.**
   - Hay drawables con prefijo de pantalla usados en otras pantallas: `ic_login_phone` en Mi Negocio, `ic_home_contacts` en Nuevo cliente, `ic_register_scissors`, `ic_business_paw_service`…
   - *Acción:* renombrar por significado (`ic_phone`, `ic_scissors`) y no por pantalla de origen.
10. **Script de conversión SVG → VectorDrawable fuera del repo.**
    - Está en el scratchpad de la sesión y no soporta `transform`, `opacity` de grupo, `clip-path` ni `stroke`. La opacidad de la mascota se ajustó a mano con `fillAlpha`.
    - *Acción:* versionarlo en `tools/` o usar `vd-tool` / Android Studio.
11. **Imágenes de ejemplo en el APK.**
    - 10 JPG de ejemplo y un logo PNG de 64 px que se ve borroso en xxhdpi.
    - *Acción:* exportar el logo como SVG y cargar las fotos remotas con **Coil**; dejar las de ejemplo solo en `debug`/previews.
12. **Fechas y montos como texto.**
    - Ejemplos: "Hace 4 sem (26 Sep)", "$28,450".
    - *Acción:* usar `java.time` (con *core library desugaring* por minSdk 24) y `NumberFormat` con `es-MX`/MXN.
13. **Identidad y build.**
    - Falta renombrar paquete/`applicationId`/`app_name` (hoy "Template") y crear un ícono de app real.
    - Falta activar R8 y crear `proguard-rules`.
    - Conviene actualizar el BOM de Compose (2024.09) y Kotlin.

### 🟡 Baja (pulido)

14. **Barras translúcidas sin desenfoque.** Usan 90% de opacidad; se puede evaluar `RenderEffect` en API 31+.
15. **`return` temprano dentro de un composable** en `VuelveApp`. Funciona, pero desaparecerá con Navigation.
16. **Títulos en inglés** en las barras de auth ("Sign In", "Register Business"). Según README §6 deberían ir en español.
17. **Accesibilidad.**
    - Varios toques son menores a 48 dp: chips de 32 dp, links de texto, mini-botones.
    - Algunas imágenes decorativas tienen `contentDescription` y algunos botones-ícono no lo tienen.
    - El gris `#C3C6D7` como texto ("(Línea de recordatorios)") probablemente no cumple el contraste AA.
    - Falta probar con fuente grande, TalkBack, landscape y tablet.
18. **Bandera como emoji 🇲🇽.** Se ve bien en Android, pero el README pide un asset SVG.

---

## 5. Brechas conocidas (producto y diseño)

| Área | Brecha |
|---|---|
| Diseño | Falta el frame de **Resultados**. Faltan **Registrar visita**, **Editar cliente**, **Ajustes**, **Catálogo**, **Editor de plantilla**, empty/error/loading |
| Diseño | Varios frames copian la barra superior de otro ("Detalle Cliente" en Mi Negocio y Nuevo cliente). Se corrigió en código, falta corregir en Figma |
| Diseño | Datos de ejemplo incoherentes: fechas de 2023 en el historial vs. oct 2026; conteos de chips ≠ conteos de secciones en Seguimientos |
| Producto | ¿Qué agrupa cada filtro? "Sin actividad" se interpretó como Atrasado y "Por contactar" como Toca contactar + Seguimiento hoy. **Por confirmar** |
| Producto | ¿El "servicio habitual" del alta es único o múltiple? Se implementó como único. ¿Los "servicios ofrecidos" del registro son múltiples? Se implementó así |
| Producto | ¿Qué pasa al "Posponer 7d"? ¿Mueve la fecha sugerida o solo la oculta? ¿Cuenta como contacto? |
| Producto | Reglas de cálculo: ¿umbral de "Toca contactar" vs. "Atrasado"? ¿Frecuencia por cliente, por servicio o aprendida ("Vuelve calculará automáticamente")? |
| Producto | Métricas: definición de *Contactados*, *Recuperadas*, *Conversión*, *Ticket promedio*, *Potencial MXN* |
| Legal | Consentimiento de términos/privacidad (arranca desmarcado), aviso de privacidad LFPDPPP, opt-in de mensajes de WhatsApp |
| Integraciones | WhatsApp: ¿deep link `wa.me` (MVP) o **WhatsApp Business Cloud API** (envío automático y plantillas aprobadas por Meta)? |

---

## 6. APIs a construir (propuesta)

### 6.1 Modelo de dominio

```
Business 1─* Staff(User)
Business 1─* Service ─1 RetentionRule (intervalDays, auto)
Business 1─* Client(Tutor) 1─* Pet
Pet 1─* Visit (serviceId, date, amount, notes)
Pet 1─* FollowUp (dueDate, status, postponedUntil, reason)
FollowUp 1─* ContactAttempt (channel, at, outcome)
Business 1─* MessageTemplate (placeholders {nombre_tutor}, {nombre_mascota}, …)
Business 1─1 Settings (sendWindow, autoReminders, timezone)
```

**Estado de retención (calculado en servidor):**
`nextSuggested = lastVisit.date + interval(pet/service)` y `delta = today - nextSuggested`. Con eso se clasifica en `UP_TO_DATE`, `DUE_SOON`, `DUE_TODAY` u `OVERDUE(days)`. Los umbrales se configuran por negocio.

### 6.2 Endpoints REST (v1)

| Dominio | Endpoint | Usado por |
|---|---|---|
| Auth | `POST /auth/register` · `POST /auth/login` (tel/correo + contraseña) · `POST /auth/otp/request` + `/auth/otp/verify` (código por WhatsApp) · `POST /auth/google` · `POST /auth/refresh` · `POST /auth/password/reset` | Login, Registro |
| Dashboard | `GET /dashboard/today` → `{toContactToday, potentialRevenue, priorityClients[], monthResults}` | Inicio |
| Clientes | `GET /clients?query=&status=&page=` · `GET /clients/summary` (conteos por filtro/KPIs) · `POST /clients` · `GET/PATCH/DELETE /clients/{id}` | Clientes, Nuevo cliente, Detalle |
| Mascotas | `POST /clients/{id}/pets` · `PATCH /pets/{id}` | Nuevo cliente, Editar |
| Visitas | `GET /pets/{id}/visits?limit=` · `POST /pets/{id}/visits` | Detalle, Registrar visita |
| Seguimientos | `GET /follow-ups?bucket=overdue\|today\|upcoming` · `POST /follow-ups/{id}/postpone {days}` · `POST /follow-ups/{id}/contacts {channel, outcome}` · `GET /follow-ups/goal` (objetivo del turno) | Seguimientos, Inicio |
| Mensajería | `GET/PUT /templates/{id}` · `POST /messages/preview {templateId, petId}` → texto renderizado · `POST /messages/send` (Cloud API) | Detalle, Seguimientos, Mi Negocio |
| Negocio | `GET/PATCH /business` · `POST /business/photo` (multipart) · `GET/PATCH /business/settings` · `GET/POST/PATCH /services` · `PATCH /retention-rules/{id}` | Mi Negocio, Registro |
| Métricas | `GET /metrics?period=current\|previous` → facturación, desglose, reactivados vs. orgánicos | Mi Negocio, Resultados |
| Reportes | `GET /reports/export?format=pdf\|xlsx&period=` | Mi Negocio |
| Dispositivo | `POST /devices {fcmToken}` | Push de recordatorios |

**Transversal:**
- **Auth:** JWT de acceso y refresco.
- **Multi-negocio:** `businessId` por token.
- **Paginación:** por cursor.
- **Errores:** formato estándar (`code`, `message`, `field`).
- **Fechas:** ISO-8601 con zona horaria del negocio.
- **Montos:** en centavos + `currency`.

### 6.3 Integración WhatsApp por fases

1. **MVP (sin backend de mensajería):** abrir `https://wa.me/52XXXXXXXXXX?text=<mensaje renderizado>` con `Intent.ACTION_VIEW`, y registrar el intento con `POST /follow-ups/{id}/contacts`.
2. **v2:** WhatsApp Business Cloud API con plantillas aprobadas, envío dentro de la ventana horaria configurada, webhooks de entrega y lectura, y respuesta para medir la conversión.

---

## 7. Buenas prácticas sugeridas

- **Arquitectura:** UDF/MVVM. Cada pantalla tiene `ViewModel` → `StateFlow<UiState>` (`Loading | Content | Error`) y eventos de una sola vez (`Channel`/`SharedFlow`). `XContent` ya es *stateless* y se reutiliza tal cual.
- **Capas y módulos** (cuando crezca):

  ```
  :core:designsystem  ← theme + components (M*/O* del README)
  :core:model  :core:data (Retrofit/Ktor + Room)  :core:domain (motor de retención, casos de uso)
  :feature:auth  :feature:home  :feature:clients  :feature:followups  :feature:business  :feature:results
  ```

- **Librerías:**

  | Necesidad | Librería |
  |---|---|
  | Navegación | Navigation Compose (rutas tipadas) |
  | Inyección de dependencias | Hilt o Koin |
  | Red | Retrofit/Ktor + kotlinx.serialization |
  | Caché offline | Room |
  | Sesión y preferencias | DataStore |
  | Imágenes | Coil |
  | Fechas | `java.time` (+ desugaring) |

- **Design system:**
  - Mapear tokens a `colorScheme` y ampliar con `VuelveColors` (status, whatsapp, mint) vía `CompositionLocal`.
  - Un solo `StatusBadge` y `VuelveChip`.
  - Incluir Inter (`res/font` o Google Fonts descargables).
  - Code Connect de Figma → componentes Compose para que el siguiente *design-to-code* reutilice componentes.
- **Calidad:**
  - Pruebas unitarias del motor de retención (es la lógica crítica).
  - Pruebas de screenshot (Roborazzi/Paparazzi) con los `@Preview` existentes.
  - Pruebas de UI de flujos clave (login → inicio → detalle).
  - ktlint/detekt y Android Lint como error en CI (GitHub Actions).
- **Accesibilidad:** `minimumInteractiveComponentSize`, `contentDescription` coherentes, contraste AA y pruebas con fuente al 200% y TalkBack.
- **Rendimiento:** listas con `LazyColumn` (hoy son `Column` + `verticalScroll` y no escalan a 48+ clientes), baseline profiles y R8.
- **Seguridad y privacidad:**
  - Tokens en almacenamiento cifrado.
  - No registrar teléfonos en logs; los números son datos personales (LFPDPPP).
  - Consentimiento explícito para mensajes.
- **Observabilidad:** Crashlytics/Sentry y analítica de embudo (contactados → recuperados), alineada con las métricas del producto.
- **Decisión KMP:** el repo se llama *Multiplatform*. Si iOS está en el plan, conviene mover dominio y datos a un módulo `shared` (KMP) **antes** de escribir la capa de datos. La UI puede migrar después a Compose Multiplatform casi sin cambios (no usa APIs exclusivas de Android, salvo recursos y `Toast`).

---

## 8. Siguientes pasos (roadmap sugerido)

### Fase 0 — Limpieza (1–2 días)
- [ ] Renombrar paquete, `applicationId`, `app_name` e ícono de la app.
- [ ] Unificar el vocabulario de estados y crear `StatusBadge` único.
- [ ] Títulos de auth en español; corregir títulos copiados en Figma.
- [ ] Incluir Inter y exportar el logo en SVG.
- [ ] Versionar `tools/svg2vd.py` y renombrar drawables compartidos por significado.
- [ ] Actualizar Compose BOM y Kotlin; activar R8 en release.

### Fase 1 — Arquitectura base (3–5 días)
- [ ] Navigation Compose: grafos de auth y principal, rutas tipadas y argumentos (`clientId`).
- [ ] ViewModels + `UiState` por pantalla; mover `Sample*` a `FakeRepository`.
- [ ] DI (Hilt/Koin).
- [ ] Extraer componentes duplicados a `ui/components` (o `:core:designsystem`).
- [ ] Listas con `LazyColumn`.

### Fase 2 — Funcionalidad local sin backend (3–5 días)
- [ ] Motor de retención en `domain`, con pruebas unitarias.
- [ ] Room con datos de ejemplo (seed): alta de cliente persistente y detalle para cualquier cliente.
- [ ] Intents: WhatsApp `wa.me` con mensaje renderizado, `tel:` para Llamar y Share.
- [ ] Fechas y montos con `java.time` / `NumberFormat`.

### Fase 3 — Pantallas faltantes (requiere diseño)
- [ ] **Resultados** (tab 4).
- [ ] **Registrar visita.**
- [ ] Editar cliente, Ajustes, Catálogo y Editor de plantilla.
- [ ] Estados vacío, error, carga y offline.

### Fase 4 — Backend e integraciones
- [ ] API v1 (§6.2) y auth con OTP por WhatsApp y Google.
- [ ] Sincronización Room ↔ API, push (FCM) de recordatorios y exportación de reportes.
- [ ] WhatsApp Business Cloud API (v2).

### Fase 5 — Calidad y release
- [ ] Pruebas de screenshot y de UI, CI, lint.
- [ ] Revisión de accesibilidad, Crashlytics y analítica.
- [ ] Beta interna (Play Console, *internal testing*).

---

## 9. Preguntas abiertas para producto y diseño

1. Vocabulario oficial de estados y umbrales (¿cuántos días para pasar de "toca contactar" a "atrasado"?).
2. Definición exacta de cada métrica (Contactados, Recuperadas, Conversión, Potencial MXN, Ticket promedio).
3. Semántica de "Posponer 7d" y de "Contactados este mes".
4. ¿Servicio habitual único o múltiple por mascota? ¿Un tutor con varias mascotas?
5. ¿Hay roles (dueña vs. staff)? ¿Multi-sucursal?
6. WhatsApp: ¿deep link o Cloud API desde el MVP? ¿Quién paga las conversaciones?
7. ¿iOS está en el alcance? Define si se adopta KMP ya.
8. Diseño pendiente: Resultados, Registrar visita, Editar, Ajustes, estados vacío/error/carga.

---

## 10. Cómo retomar

```bash
# Compilar e instalar en el emulador (evita instalar en dispositivos físicos conectados)
./gradlew :app:assembleDebug
export ANDROID_SERIAL=emulator-5554
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.invex.one.template/.MainActivity
```

**Flujo demo:**
1. Login → "Iniciar sesión" lleva a Inicio sin validar credenciales.
2. Tocar a María López abre su Detalle.
3. Avatar → Mi Negocio.
4. Clientes → "Nuevo" abre el alta.
5. "Regístrate gratis" en Login abre el Registro.

**Flujo design-to-code usado** (para la siguiente pantalla):
1. MCP de Figma `get_design_context` (archivo `KqTFktvrRvCzO2idPga5xf`, nodo de la pantalla).
2. Descargar los assets y compararlos por trazo con los existentes antes de crear nuevos.
3. Convertir SVG → VectorDrawable (revisar `transform`, `opacity` y `clip-path`).
4. Agregar tokens nuevos a `Color.kt` y textos a `strings.xml`.
5. Crear `ui/<feature>/<Feature>Screen.kt` con `UiState` de ejemplo y conectarlo en `MainActivity`.
6. Verificar en el emulador comparando con el screenshot de Figma, y confirmar que no queden drawables sin usar.

**Convenciones vigentes:**
- Textos en `strings.xml`. Usar comillas para conservar espacios al inicio o final (`"Acepto los "`) y escapar `\"`.
- Datos de ejemplo en `Sample*UiState`.
- Cada pantalla tiene un `@Preview` con `widthDp = 390`, el ancho del frame de Figma.
