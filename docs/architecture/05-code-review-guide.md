# 🔍 Guía del Code Reviewer — Kensho

> **Versión:** 1.1.0 | **Última actualización:** 2026-10-30 (revisión de DI y red: ARC-10…14, TEST-06, LOG-03, DEP-06, FF-14…16)
> Guía de campo para revisar Pull Requests de Kensho (app Android de gestión de dojo, módulo único `:app`, namespace `dev.treecode.kensho`).
> **Repositorio:** Bitbucket `warriors_dev_platforms/kensho` · rama de integración `develop` · `main` solo para releases.

> **¿Por qué existe este documento?**
> `03-standards.md` dice *qué* reglas hay; este documento dice *cómo* comprobarlas en un PR concreto, en qué orden y con qué palabras pedir el cambio. El proyecto está empezando: cada PR que entra sin revisar bien fija un precedente que el siguiente copiará. Revisar con método ahora es más barato que limpiar después.

---

## Cómo usar este documento

1. Corre el **setup rápido** (§2) en la rama del PR y compara con la línea base.
2. Recorre las **checklists por tema** (§3) solo para los temas que toca el PR.
3. Si el PR trae una pantalla nueva desde Figma, aplica **§4** completo.
4. Usa las **plantillas** (§6) para comentar citando la regla.
5. Cierra con el **checklist final de aprobación** (§8).

Validación asistida: `/validate-pullrequest <PR_ID|rama>` corre las mismas comprobaciones por familia (`ff-arc`, `ff-cmp`, `ff-thm`, `ff-a11y`, `ff-i18n`, `ff-res`, `ff-st`, `ff-pf`, `ff-cov`, `ff-dep`, `ff-dup`). La herramienta no sustituye la lectura del diff: la acelera.

---

## Índice

1. [Filosofía del reviewer](#1-filosofía-del-reviewer)
2. [Setup rápido](#2-setup-rápido)
3. [Checklists por tema](#3-checklists-por-tema)
4. [Revisión de una pantalla nueva desde Figma](#4-revisión-de-una-pantalla-nueva-desde-figma)
5. [Red flags](#5-red-flags)
6. [Plantillas de comentario](#6-plantillas-de-comentario)
7. [Documentos a citar](#7-documentos-a-citar)
8. [Checklist final de aprobación](#8-checklist-final-de-aprobación)

---

## 1. Filosofía del reviewer

### Cuatro preguntas antes de leer el diff

1. **¿Qué pantalla o nodo de Figma toca?** Abre la captura de Figma: es el criterio visual de aceptación.
2. **¿Introduce una capa o dependencia nueva?** Hilt, Room, Retrofit, un módulo… requieren ADR (`01-governance.md §9`). Si no hay ADR, se detiene ahí.
3. **¿Empeora alguna fitness function?** Una cifra que sube bloquea, aunque el código "se vea bien".
4. **¿Hay deuda `TD-KEN-0XX` en los archivos tocados?** Regla del boy scout: no puede empeorar.

### Tono y foco

- Comenta el código, no a la persona. Cita la regla (`🔒 THM-01`) en vez de opinar.
- Distingue **bloqueante** (🔒) de **sugerencia** (⚠️ o `nit:`). Un PR no se detiene por un `nit`.
- Si una regla no tiene sentido en un caso real, la conversación es sobre la regla: propón el cambio con `/kensho-governance:propagacion`, no la ignores en silencio.
- Reconoce lo bien hecho: separar `Screen`/`Content`, un token bien nombrado o una preview por estado también se comentan.

### Lo que NO se revisa a mano

Formato e imports los resolverá Detekt/ktlint cuando exista (`08-lint-rules.md`, TD-KEN-008). Mientras tanto, no gastes comentarios en espacios: si molesta, que lo arregle el IDE (`Reformat Code`).

---

## 2. Setup rápido

### 2.1 Compilar y probar

```bash
git fetch origin && git checkout <rama-del-pr>
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug                 # informativo hasta que exista baseline (TD-KEN-008)
```

### 2.2 Qué cambió

```bash
BASE=$(git merge-base HEAD origin/develop)
git diff --stat $BASE...HEAD
git diff --name-only $BASE...HEAD -- 'app/src/main/*.kt'
git diff --name-only $BASE...HEAD -- 'app/src/main/res/*'
```

### 2.3 Fitness functions (comparar con la línea base)

Mismos comandos que [`01-governance.md §7`](01-governance.md#7-fitness-functions), verificados en macOS/zsh. Línea base al 2026-10-27:

```bash
M=app/src/main/java/dev/treecode/kensho

# FF-01 logging directo — base 0 (LOG-01)
grep -rnE "android\.util\.Log|println\(|printStackTrace\(" $M | wc -l
# FF-02 !!, GlobalScope, runBlocking — base 0
grep -rnE '!!|GlobalScope|runBlocking' $M | wc -l
# FF-03 colores literales fuera de ui/theme — base 4 (THM-01)
grep -rnE "\bColor\(0x|\bColor\.(White|Black|Red|Gray|Green|Blue|Yellow|Cyan|Magenta)\b" $M | grep -v /ui/theme/ | wc -l
# FF-04 dp/sp literales fuera de ui/theme — base 2, 1 comentado (THM-02)
grep -rnE "[0-9]\.(dp|sp)\b" $M | grep -v /ui/theme/ | wc -l
# FF-05 texto literal en Text() — base 1 (I18N-01)
grep -rnE 'Text\(\s*(text\s*=\s*)?"' $M | wc -l
# FF-06 contentDescription = null sin comentario — base 65 (A11Y-01)
grep -rn "contentDescription = null" $M | grep -v "//" | wc -l
# FF-07 reloj del sistema fuera de data/ — base 3 (ARC-07)
grep -rnE "LocalDate\.now\(|LocalDateTime\.now\(|System\.currentTimeMillis\(" $M | grep -v /data/ | wc -l
# FF-08 imports entre features — base 0 (ARC-05)
for f in login signup students profile checkin payments ui.present; do
  grep -rn "import dev.treecode.kensho\.\(login\|signup\|students\|profile\|checkin\|payments\|ui.present\)\." $M/${f//.//} \
    | grep -v "import dev.treecode.kensho.$f\." ; done | wc -l
# FF-09 archivos > 300 líneas — base 4 (STR-02)
find $M -name '*.kt' -exec wc -l {} + | awk '$2 != "total" && $1 > 300' | wc -l
# FF-10 collectAsState() — base 0 (PERF-02)
grep -rn "collectAsState()" $M | wc -l
# FF-11 componentes públicos sin preview — base 2 (CMP-09)
for f in $(grep -rlE "^fun [A-Z]" $M/components); do grep -qE "@Preview|@TkdFontScalePreviews" $f || echo $f; done | wc -l
# FF-12 archivos de test — base 3: 2 de ejemplo + TkdFontScaleTest (TEST-01)
find app/src/test app/src/androidTest -name '*Test.kt' | wc -l
# FF-13 escalado manual de fuente — base 0 (A11Y-06)
grep -rnE "fontScale\s*\*|\*\s*[A-Za-z.]*fontScale" $M | grep -v /ui/theme/TkdFontScale.kt | wc -l
# FF-14 red/DTO fuera de data/ y di/ — base 0 (ARC-11)
grep -rnE "import (retrofit2|okhttp3)\.|import dev\.treecode\.kensho\.data\.remote\." $M | grep -vE "/data/|/di/" | wc -l
# FF-15 defaults a la Fake — base 14 al 2026-10-30, solo baja (ARC-06)
grep -rnE "Repository = Fake" $M | wc -l
# FF-16 dispatchers literales fuera de di/ — base 0 (ARC-13)
grep -rnE "Dispatchers\.(IO|Default)" $M | grep -v /di/ | wc -l
# FF-17 perfil del instructor fuera de TkdSenseiViewModel o GET me — base 0 al 2026-11-16 (ARC-15)
grep -rnE 'signedInAccount|currentAccount\(|@GET\("me"\)|\.observeProfile\(\)|[a-zA-Z]+\.profile\((refresh[^)]*)?\)' $M \
  | grep -vE "/data/|/ui/TkdSenseiViewModel.kt|/feature/profile/sensei/" | wc -l
```

### 2.4 Solo lo que agrega el PR

Las cifras globales pueden bajar en un archivo y subir en otro. Para ver solo las líneas añadidas:

```bash
BASE=$(git merge-base HEAD origin/develop)
ADDED() { git diff -U0 $BASE...HEAD -- 'app/src/main/*.kt' | grep -E '^\+' | grep -vE '^\+\+\+'; }

ADDED | grep -nE "\bColor\(0x|[0-9]\.(dp|sp)\b"                 # THM-01/02
ADDED | grep -nE 'Text\(\s*(text\s*=\s*)?"'                     # I18N-01
ADDED | grep -n "contentDescription = null" | grep -v "//"      # A11Y-01
ADDED | grep -nE "LocalDate\.now\(|System\.currentTimeMillis\(" # ARC-07
ADDED | grep -nE "collectAsState\(\)|NavController|!!"          # PERF-02, ARC-04
ADDED | grep -nE "^\+\s*//\s*(val|var|fun|[A-Z][a-zA-Z]+\()"    # STR-06 código comentado
ADDED | grep -nE "Repository = Fake|Dispatchers\.(IO|Default)"  # ARC-06, ARC-13
ADDED | grep -nE "\.message\b|Level\.BODY|http(s)?://"            # ARC-12, LOG-03, DEP-06
git diff -U0 $BASE...HEAD -- 'app/src/main/*.kt' | grep -E '^\+\+\+ ' | grep -vE "/data/|/di/" \
  | while read -r _ f; do git diff -U0 $BASE...HEAD -- "${f#b/}" | grep -nE "^\+import (retrofit2|okhttp3|dev\.treecode\.kensho\.data\.remote)\."; done   # ARC-11
```

### 2.5 Mapa mental del proyecto

```
MainActivity → KenshoApp → ui/TkdApp.kt (NavHost: login, signup, tabs TkdTab, student/{id}, payment/{id})
                               │
   <feature>/Tkd<X>Screen ── hiltViewModel() (viewModel() hasta migrar) + collectAsStateWithLifecycle
            │
   <feature>/Tkd<X>Content (UiState + Tkd<X>Actions + lambdas)  ── previews con Fake
            │
   <feature>/Tkd<X>Sections (internal/private)
            │
   components/atoms · components/molecules (Tkd*)  ── ui/theme: TkdColors, TkdDimens, TkdTypography, TkdBeltPalette
   model/ (UiState @Immutable)   data/ (Tkd<X>Repository + FakeTkd<X>Repository + NetworkTkd<X>Repository)   core/ (Validate)
                                 data/remote (api · dto · mapper)   data/local (TkdSessionStore)   di/ (módulos Hilt)
```

Features: `login`, `signup`, `ui/present` (resumen), `students`, `profile`, `checkin`, `payments`. Referencia canónica: `payments/`.

---

## 3. Checklists por tema

Cada casilla cita la regla de [`03-standards.md`](03-standards.md).

### 3.1 Arquitectura (ARC)

- [ ] La pantalla tiene `Tkd<X>Screen` stateful y `Tkd<X>Content` stateless; solo `Screen` ve el ViewModel (🔒 ARC-01).
- [ ] El VM expone `StateFlow` desde un `MutableStateFlow` privado y actualiza con `update { it.copy(...) }` (🔒 ARC-02).
- [ ] El VM no recibe `Context`, `Resources`, `NavController` ni tipos de Compose UI (🔒 ARC-03).
- [ ] La navegación sale por `Tkd<X>Actions` hacia `TkdApp`; rutas con argumento como constantes con `navArgument` (🔒 ARC-04).
- [ ] Ningún import entre paquetes de feature (🔒 ARC-05, FF-08).
- [ ] Los datos llegan por `interface Tkd<X>Repository`; un VM nuevo o migrado es `@HiltViewModel @Inject constructor` sin default `= Fake…()` y lee los argumentos de ruta de `SavedStateHandle` (🔒 ARC-06, FF-15 no sube).
- [ ] Sin `LocalDate.now()` en `model/` ni en UI; "hoy" sale del repositorio (`closingDate()`, `asOf`) (🔒 ARC-07).
- [ ] Filtros, totales y conteos son propiedades del `UiState` o del VM, no cálculos en composables (⚠️ ARC-08).
- [ ] Montos en centavos `Int` y formateados con los helpers de `TkdPaymentsFormat.kt` (🔒 ARC-09).
- [ ] Repositorio de red con funciones `suspend`/`Flow` que devuelven `TkdResult`; relanza `CancellationException`; ningún `null` como fallo (🔒 ARC-10).
- [ ] DTO, `Tkd<X>Api`, Retrofit y OkHttp solo en `data/` y `di/` (🔒 ARC-11, FF-14).
- [ ] Pantalla con red: `Loading`/`Error` en el `UiState`, error como `@StringRes tkd_error_*`, botón de reintento y preview de cada estado (🔒 ARC-12).
- [ ] Sin `Dispatchers.IO`/`Default` literal; se inyecta `@IoDispatcher` (🔒 ARC-13, FF-16).
- [ ] El token solo se toca en `TkdSessionStore` y los interceptores; la expiración de sesión navega desde `TkdApp` (⚠️ ARC-14).
- [ ] Una pantalla con datos del instructor (encabezado del dojo, agenda) tiene un VM que hereda de `TkdSenseiViewModel` y usa `onSenseiProfile { }`; nada de `GET me`, de llamar a `profile()` desde el VM ni de una caché propia del perfil (🔒 ARC-15, FF-17 = 0).

```kotlin
// ✅ patrón de TkdPaymentDetailViewModel
private val _uiState = MutableStateFlow(/* … */)
val uiState: StateFlow<TkdPaymentDetailUiState> = _uiState.asStateFlow()
fun onConfirm() = _uiState.update { it.copy(confirmed = true) }
```

### 3.2 Composables (CMP)

- [ ] `modifier: Modifier = Modifier` es el primer opcional y se aplica una sola vez a la raíz (🔒 CMP-01).
- [ ] Átomos y moléculas no guardan estado de negocio; `remember` solo para estado visual efímero (🔒 CMP-02).
- [ ] `components/atoms/` no importa `model/` ni `data/`; moléculas usan `data/` solo en previews (🔒 CMP-03).
- [ ] Secciones de pantalla `internal`/`private` en el paquete del feature (🔒 CMP-04).
- [ ] Más de 2 callbacks hacia fuera agrupados en `Tkd<X>Actions` con defaults vacíos (⚠️ CMP-05).
- [ ] Variantes con objeto de estilo (`TkdInputFieldStyle`/`TkdInputFieldDefaults.Note`) o slot, no con booleanos (⚠️ CMP-06).
- [ ] Antes de crear un componente se buscó en `components/` (🔒 CMP-07):
  ```bash
  grep -rn "^fun Tkd" app/src/main/java/dev/treecode/kensho/components
  ```
- [ ] Composables nuevos ≤ 60 líneas (⚠️ CMP-08).
- [ ] Todo composable público con `@Preview` privada; `Content` con una preview por estado y `widthDp = 390` (🔒 CMP-09).

### 3.3 Tokens de diseño (THM)

- [ ] Ningún `Color(0x…)`/`Color.White` fuera de `ui/theme/`, tampoco en previews; `Color.Unspecified` y `Color.Transparent` permitidos (🔒 THM-01).
- [ ] Ningún `N.dp`/`N.sp` fuera de `ui/theme/` (🔒 THM-02).
- [ ] Estilos de texto desde `TkdTypography` (🔒 THM-03).
- [ ] Tokens nuevos nombrados por rol y sin duplicar valor+rol existente (🔒 THM-04):
  ```bash
  grep -n "0xFFFFFBEB" app/src/main/java/dev/treecode/kensho/ui/theme/TkdColors.kt   # ¿ya existe ese color?
  ```
- [ ] Colores de cinturón vía `BeltColor.palette()` (🔒 THM-05).
- [ ] Ningún token quedó sin uso tras el PR (⚠️ THM-06).

### 3.4 Accesibilidad (A11Y)

- [ ] Cada `Icon`/`Image` informativo usa `stringResource(R.string.tkd_cd_…)`; cada `null` lleva comentario en la misma línea (🔒 A11Y-01).
- [ ] Selección y toggles en la fila completa con `selectable`/`toggleable` + `Role`; grupos con `selectableGroup()` (🔒 A11Y-02). Referencia: `TkdSwitch` (no interactivo) + `TkdReceiptDeliveryCard` (fila `toggleable` con `Role.Switch`).
- [ ] Área táctil ≥ 48dp (🔒 A11Y-03).
- [ ] Tarjetas que se leen como unidad con `semantics(mergeDescendants = true)` (⚠️ A11Y-04).
- [ ] Estados de cobro (pendiente/vencido/pagado) con texto o icono además del color (⚠️ A11Y-05).
- [ ] Texto escalable (🔒 A11Y-06): contenedores con texto en `heightIn(min)`, nombre + badge en `FlowRow`, topes solo con `TkdFontScale` y comentario; preview con `@TkdFontScalePreviews`; pantalla nueva revisada a 150 % y 200 % ([`FONT_SCALE_ANALISIS.md §7.4`](FONT_SCALE_ANALISIS.md)).
  ```bash
  # alturas fijas nuevas en el diff: revisar si el contenedor tiene texto
  git diff develop...HEAD -- '*.kt' | grep -E '^\+.*\.height\(TkdDimens\.'
  ```

Prueba manual recomendada: TalkBack en el emulador sobre la pantalla nueva; cada foco debe decir algo con sentido.

### 3.5 Textos y formato (I18N)

- [ ] Ningún texto visible literal; `stringResource`/`pluralStringResource` (🔒 I18N-01).
- [ ] Claves `tkd_<feature>_<descripcion>`; accesibilidad `tkd_cd_*` o `tkd_<feature>_cd_*`; bloque con comentario `<!-- Pantalla (Figma: Prototipo-Demo-UiKit, nodo X:Y) -->` (🔒 I18N-02).
- [ ] Sin concatenar textos traducibles; argumentos `%1$s` y plurales (🔒 I18N-03).
- [ ] Formato con `Locale.forLanguageTag("es-MX")` (`spanish`) de los helpers; nada de `"%02d".format(...)` (⚠️ I18N-04).
- [ ] Textos nuevos en español con acentos correctos aunque Figma esté en inglés; si el PR toca una pantalla con cadenas en inglés, las traduce (🔒 I18N-05, TD-KEN-011).

### 3.6 Recursos de Figma (RES)

- [ ] Nombres `ic_tkd_*`, `img_tkd_<pantalla>_*`, `ic_tkd_nav_*` (🔒 RES-01).
- [ ] Vectores nuevos con cabecera `<!-- Generado desde Figma (...). No editar a mano. -->` y tamaños del SVG original (🔒 RES-02). Línea base: 0 vectores `ic_tkd_*` sin cabecera.
  ```bash
  grep -L "Generado desde Figma" app/src/main/res/drawable/ic_tkd_*.xml
  ```
- [ ] Reutilizó un drawable solo si el path data es idéntico (🔒 RES-03).
- [ ] Ninguna URL de Figma en el código (🔒 RES-04):
  ```bash
  grep -rn "figma.com" app/src/main
  ```
- [ ] Fotos ≤ 200 KB, preferible WebP (⚠️ RES-05):
  ```bash
  find app/src/main/res/drawable -name 'img_tkd_*' -size +200k
  ```

### 3.7 Estructura y nombres (STR)

- [ ] Archivos en el paquete correcto según `03-standards.md §7` (🔒 STR-01).
- [ ] Archivo nuevo ≤ 300 líneas; uno existente no crece > 10 % si ya excede (🔒 STR-02).
- [ ] Nombres según la tabla de STR-03 (`Tkd<X>Screen`, `Tkd<X>UiState`, `FakeTkd<X>Repository`, `on<Algo>`).
- [ ] Un componente público por archivo salvo familias (⚠️ STR-04).
- [ ] KDoc en español de una línea en lo público (⚠️ STR-05).
- [ ] Sin código comentado ni muerto (🔒 STR-06).

### 3.8 Rendimiento (PERF)

- [ ] Modelos de entrada `@Immutable` con `val`, sin `MutableList` (🔒 PERF-01).
- [ ] `collectAsStateWithLifecycle()` (🔒 PERF-02).
- [ ] `items(list, key = { it.id })` en listas perezosas (🔒 PERF-03).
- [ ] `DateTimeFormatter`, `Regex` y listas derivadas fuera de la composición (top-level o `remember`) (⚠️ PERF-04).
- [ ] Referencias de método del VM (`viewModel::onConfirm`) en `Screen` (⚠️ PERF-05).

### 3.9 Pruebas (TEST)

- [ ] ViewModel nuevo o modificado con `Tkd<X>ViewModelTest` en `app/src/test/.../<feature>/` cubriendo estado inicial y cada evento (🔒 TEST-01).
- [ ] Lógica pura con unit test (🔒 TEST-02).
- [ ] Flujos interactivos con Compose UI Test sobre `Content` (⚠️ TEST-03).
- [ ] Nombres `sujeto_condicion_resultado` (🔒 TEST-04).
- [ ] Sin `Thread.sleep`, fecha real, servidor real ni tests de ejemplo junto a reales (🔒 TEST-05).
- [ ] Mapper nuevo con test de fixtures JSON; `NetworkTkd<X>Repository` nuevo con `MockWebServer` (2xx, 401, 404, 409, 5xx, timeout, JSON malformado) (🔒 TEST-06).

```kotlin
// ✅ forma esperada de un test de VM
class TkdPaymentDetailViewModelTest {
    @Test
    fun onMethodSelected_whenConfirmed_keepsPreviousMethod() {
        val vm = TkdPaymentDetailViewModel("kenji")
        vm.onConfirm()
        vm.onMethodSelected(PaymentMethod.Transfer)
        assertEquals(PaymentMethod.Cash, vm.uiState.value.method)
    }
}
```

### 3.10 Logging (LOG)

- [ ] Sin `android.util.Log`, `println`, `printStackTrace` (🔒 LOG-01).
- [ ] Ningún dato personal (alumno, tutor, teléfono, monto) en logs (🔒 LOG-02).
- [ ] `HttpLoggingInterceptor` solo en debug, `BASIC` y con `redactHeader("Authorization")` (🔒 LOG-03).

### 3.11 Dependencias y build (DEP)

- [ ] Versiones solo en `gradle/libs.versions.toml`, sin rangos dinámicos (🔒 DEP-01).
- [ ] API usada directamente = artefacto declarado (⚠️ DEP-02).
- [ ] Dependencia nueva justificada en el PR; frameworks con ADR — Hilt (ADR-016) y el stack de red/sesión (ADR-017) ya aprobados (🔒 DEP-03).
- [ ] `BASE_URL` por `buildConfigField`, sin URLs literales ni cleartext fuera del flavor `dev` (🔒 DEP-06).
- [ ] No se agrega `mavenLocal()` (⚠️ DEP-04).
- [ ] Sin secretos, keystores, `google-services.json` ni `local.properties` (🔒 DEP-05):
  ```bash
  git diff --name-only $BASE...HEAD | grep -E "local\.properties|\.jks$|\.keystore$|google-services\.json"
  ```

### 3.12 Git (GIT)

- [ ] Commits `<tipo>(<ámbito>): <descripción>` con ticket opcional `[KEN-n]` (🔒 GIT-01):
  ```bash
  git log --format=%s $BASE..HEAD
  ```
- [ ] Rama `feature/…`, `fix/…`, `chore/…` o `docs/…` contra `develop` (🔒 GIT-02).
- [ ] PR acotado a una pantalla, componente o deuda; capturas si hay UI (⚠️ GIT-03).
- [ ] Sin `build/`, `.idea/`, `.DS_Store`, `.gradle/`, `.kotlin/` (🔒 GIT-04):
  ```bash
  git diff --name-only $BASE...HEAD | grep -E "(^|/)(build|\.idea|\.gradle|\.kotlin)/|\.DS_Store$"
  ```

---

## 4. Revisión de una pantalla nueva desde Figma

### 4.1 Anatomía obligatoria

Para una pantalla `<X>` en el paquete `<feature>/`:

| Pieza | Archivo | Comprobar |
|-------|---------|-----------|
| Estado | `model/Tkd<X>UiState.kt` | `@Immutable`, `val`, derivados como propiedades, dinero en centavos |
| Datos | `data/Tkd<X>Repository.kt` (o método nuevo en uno existente) | interfaz + `FakeTkd<X>Repository` con datos del prototipo y fecha de referencia |
| ViewModel | `<feature>/Tkd<X>ViewModel.kt` | repositorio por constructor con default, `StateFlow`, un método por evento |
| Pantalla | `<feature>/Tkd<X>Screen.kt` | `Tkd<X>Actions`, `Screen`, `Content`, previews por estado |
| Secciones | `<feature>/Tkd<X>Sections.kt` | `internal`, ≤ 300 líneas por archivo (dividir si crece) |
| Navegación | `ui/TkdApp.kt` | ruta nueva, `navArgument`, `onBack = { navController.popBackStack() }` |
| Textos | `res/values/strings.xml` | bloque nuevo con comentario de nodo de Figma |
| Recursos | `res/drawable/ic_tkd_*`, `img_tkd_*` | cabecera de Figma, reutilización solo exacta |
| Tokens | `ui/theme/Tkd*.kt` | solo si no existía el rol |
| Tests | `app/src/test/.../<feature>/Tkd<X>ViewModelTest.kt` | estado inicial + eventos |

`payments/TkdPaymentDetail*` es el ejemplo de referencia completo, con su unit test (`TkdPaymentDetailViewModelTest`) y su Compose test (`TkdPaymentDetailContentTest`).

### 4.2 Verificación visual contra Figma

1. El PR enlaza el nodo de Figma (`Prototipo-Demo-UiKit?node-id=X-Y`) y adjunta **dos capturas lado a lado**: Figma y emulador (Pixel 8 Pro, API 33 o similar, ancho 390dp lógico).
2. El reviewer compara: jerarquía de secciones, espaciados (múltiplos de `TkdDimens.Space*`), tipografía, colores y todos los iconos/fotos en su posición.
3. Si el reviewer quiere reproducirlo:
   ```bash
   ./gradlew :app:installDebug
   adb shell am start -n dev.treecode.kensho/.MainActivity
   adb exec-out screencap -p > /tmp/kensho.png
   ```
4. Interacciones: cada elemento que en Figma parece interactivo (chips, tiles, switches, botones) responde o está explícitamente fuera de alcance en la descripción del PR.
5. Datos: la pantalla funciona con los datos de la *Fake*; si un id no tiene datos, muestra un estado vacío (`tkd_payment_detail_not_found`), no un crash.

### 4.3 Checklist específico de pantalla nueva

- [ ] Todas las piezas de §4.1 presentes.
- [ ] Capturas Figma vs. emulador adjuntas y coincidentes.
- [ ] Ningún recurso de Figma omitido, sustituido o redibujado.
- [ ] Se reutilizaron átomos/moléculas existentes; los nuevos son genéricos o se quedaron como `internal`.
- [ ] Previews por estado (normal, vacío, confirmado/error si aplica).
- [ ] Fitness functions sin empeorar.

---

## 5. Red flags

Señales que justifican detenerse y pedir explicación antes de seguir leyendo:

| Señal en el diff | Por qué preocupa | Regla |
|------------------|------------------|-------|
| `viewModel` como parámetro de una sección o molécula | rompe previews y tests de `Content` | ARC-01 |
| `navController` fuera de `TkdApp` | navegación dispersa | ARC-04 |
| `import dev.treecode.kensho.students.` dentro de `payments/` | acoplamiento entre features | ARC-05 |
| `LocalDate.now()` como default de un `UiState` | cuentas regresivas distintas según el día | ARC-07 |
| `Double`/`Float` en un monto | errores de redondeo en cobros | ARC-09 |
| `Color(0xFF…)` o `16.dp` en un feature | el diseño deja de tener una sola fuente | THM-01/02 |
| `contentDescription = null` sin comentario | indistinguible de un olvido | A11Y-01 |
| `Text("…")` o `"%02d".format(…)` | texto o formato fuera de recursos/locale | I18N-01/04 |
| `.clickable` sobre un `Switch`/`RadioButton` pequeño | área táctil y semántica incorrectas | A11Y-02/03 |
| drawable nuevo sin cabecera de Figma o editado a mano | se pierde la trazabilidad con el diseño | RES-02 |
| archivo `*Sections.kt` > 300 líneas | ya hay 4 casos (TD-KEN-006) | STR-02 |
| bloques `//` con código | basura que nadie borra | STR-06 |
| dependencia nueva sin explicación (Room, Ktor, otra librería de red o DI) | decisión arquitectónica sin ADR | DEP-03 |
| `= FakeTkd…Repository()` en un VM nuevo | la app nunca usaría la implementación real | ARC-06 |
| `import …data.remote.` o `retrofit2.` en un feature o en `model/` | el JSON del servidor se filtra a la UI | ARC-11 |
| `runCatching { … }` o `catch (e: Exception)` sin relanzar `CancellationException` | corrutinas que no se cancelan | ARC-10 |
| `exception.message` (o un cuerpo de error que no sea el `Error` del API) en un `UiState` | mensajes técnicos o en inglés al usuario | ARC-12 |
| `Level.BODY` o token en un log | fuga de datos de alumnos y credenciales | LOG-03 |
| VM nuevo sin test | reabre TD-KEN-001 (saldada: 22/22 VMs con test) | TEST-01 |

---

## 6. Plantillas de comentario

Copia, ajusta y pega. Formato: **severidad · regla · qué · cómo**.

**Token literal**
> 🔒 **THM-01** (`03-standards.md §3`): `Color(0xFF059669)` fuera de `ui/theme/`. Ese valor ya existe como `TkdColors.Success`; úsalo. Si el rol es otro, agrega un token con nombre de rol en `TkdColors`.

**Medida literal**
> 🔒 **THM-02**: `12.dp` en `Tkd<X>Sections.kt`. Usa `TkdDimens.Space12` (o el token del rol, p. ej. `TkdDimens.IconCircleSmall`).

**ViewModel fuera de Screen**
> 🔒 **ARC-01** (`03-standards.md §1`): `TkdTransactionFilters` recibe el ViewModel. Pasa `selected` y `onFilterSelected: (TransactionFilter) -> Unit`; solo `TkdPaymentsScreen` debe conocer al VM.

**Navegación en feature**
> 🔒 **ARC-04**: esta pantalla recibe `NavController`. Agrega el callback a `Tkd<X>Actions` y resuelve la ruta en `ui/TkdApp.kt`.

**Reloj del sistema**
> 🔒 **ARC-07** (ADR-006): `today: LocalDate = LocalDate.now()` hace que la pantalla cambie según el día. Obtén la fecha del repositorio (como `closingDate()` en `TkdPaymentsRepository`).

**contentDescription nulo**
> 🔒 **A11Y-01**: `contentDescription = null` sin justificar. Si es decorativo, agrega en la misma línea `// decorativo: <motivo>`; si informa algo, usa un `tkd_cd_*`.

**Texto literal / formato**
> 🔒 **I18N-01**: `Text("Cobrar ahora")` → `stringResource(R.string.tkd_payments_…)`.
> ⚠️ **I18N-04**: `"%02d".format(day)` usa el locale por defecto; usa un helper de `TkdPaymentsFormat.kt` con `Locale.forLanguageTag("es-MX")` (`spanish`).

**Idioma**
> 🔒 **I18N-05** (TD-KEN-011): las cadenas nuevas van en español en `res/values/`. Ya que tocas esta pantalla, traduce también sus cadenas en inglés.

**Recurso de Figma**
> 🔒 **RES-03**: `ic_tkd_credit_card` se reutiliza para el tile de método, pero el SVG exportado tiene otro tamaño. Exporta el recurso del nodo y crea `ic_tkd_method_saved_card`.

**Archivo grande**
> 🔒 **STR-02**: `Tkd<X>Sections.kt` llega a 420 líneas. Divide por sección (`Tkd<X>HeaderSection.kt`, `Tkd<X>HistorySection.kt`).

**Test faltante**
> 🔒 **TEST-01** (ADR-013): `Tkd<X>ViewModel` no tiene test. Agrega `Tkd<X>ViewModelTest` con estado inicial y un caso por evento usando la *Fake*.

**Default a la Fake**
> 🔒 **ARC-06** (ADR-016): `Tkd<X>ViewModel` recibe `repository: Tkd<X>Repository = FakeTkd<X>Repository()`. Conviértelo en `@HiltViewModel class … @Inject constructor(private val repository: Tkd<X>Repository)` y, si depende de la ruta, lee el argumento de `SavedStateHandle`. El test sigue construyéndolo con la *Fake*.

**Error de red sin tipar**
> 🔒 **ARC-10** (ADR-017): `paymentDetail()` deja escapar `IOException`/`HttpException` (o devuelve `null`). Devuelve `TkdResult<PaymentDetail>`, traduce con `toDataError()` y relanza `CancellationException`.

**Encabezado con su propia cuenta**
> 🔒 **ARC-15** (ADR-021, FF-17): `Tkd<X>ViewModel` pide la cuenta (`GET me`) o llama a `profileRepository.profile()` para el encabezado. Hereda de `TkdSenseiViewModel(profileRepository)` y en `init` usa `onSenseiProfile { profile -> _uiState.update { it.copy(header = profile.header) } }`; reintenta con `loadSenseiProfile()`.

**DTO fuera de data**
> 🔒 **ARC-11** (FF-14): `StudentDto` llega a `TkdStudentsUiState`. Agrega `StudentDto.toModel()` en `data/remote/mapper/` y expón `Student` de `model/`.

**Mensaje del servidor**
> 🔒 **ARC-12** (ADR-009): el `UiState` guarda `e.message`. Mapea `TkdDataError` a `@StringRes` (`tkd_error_no_connection`, `tkd_error_server`…) y resuélvelo con `stringResource` en `Content`.

**Logging HTTP**
> 🔒 **LOG-03**: el interceptor usa `Level.BODY` fuera de debug. Usa `BASIC`, solo con `BuildConfig.DEBUG` y `redactHeader("Authorization")`.

**Dependencia nueva**
> 🔒 **DEP-03**: el PR agrega `<lib>`. Explica en la descripción para qué, qué alternativas se evaluaron y su peso; si es un framework (DI, red, persistencia) necesita ADR en `01-governance.md`.

**Sugerencia no bloqueante**
> `nit:` (⚠️ **CMP-08**) `TkdChargeCard` pasa de 90 líneas; considera extraer el desglose a una función `private`. No bloquea.

---

## 7. Documentos a citar

| Necesito… | Documento |
|-----------|-----------|
| La decisión y su porqué | [`01-governance.md`](01-governance.md) (ADR-001…017) |
| El marco conceptual | [`02-best-practices.md`](02-best-practices.md) |
| La regla con ID | [`03-standards.md`](03-standards.md) |
| Ejemplo correcto vs. incorrecto | [`04-detailed-rules.md`](04-detailed-rules.md) |
| Línea base y estado de la deuda | [`06-compliance-report.md`](06-compliance-report.md) |
| Historia para la deuda | [`07-user-stories.md`](07-user-stories.md) |
| Automatización futura | [`08-lint-rules.md`](08-lint-rules.md) |

---

## 8. Checklist final de aprobación

Un PR se aprueba cuando **todas** las casillas aplicables están marcadas:

**Build y pruebas**
- [ ] `./gradlew :app:assembleDebug` pasa.
- [ ] `./gradlew :app:testDebugUnitTest` pasa.
- [ ] Si hay UI interactiva nueva, Compose UI Test o justificación de por qué no.
- [ ] Si hay red nueva, tests de mapper y `MockWebServer` (TEST-06).

**Métricas**
- [ ] Ninguna fitness function (§2.3) empeora respecto a la línea base de `06-compliance-report.md`.
- [ ] Las líneas añadidas (§2.4) no introducen literales, `null` sin comentario, reloj del sistema ni código comentado.

**Reglas**
- [ ] Todas las reglas 🔒 de `03-standards.md` que aplican se cumplen, o la excepción está justificada en la descripción del PR y aceptada por el reviewer.
- [ ] Las advertencias ⚠️ están resueltas o explicadas.

**Pantallas**
- [ ] Capturas Figma vs. emulador coincidentes (§4.2).
- [ ] Anatomía completa (§4.1).

**Gobernanza**
- [ ] Si el PR cambia una decisión, resuelve o agrava una `TD-KEN-0XX`, o crea una regla, se propagó al corpus con `/kensho-governance:propagacion` (06 → 01 → 03 → 04 → 05 → 02 → `CLAUDE.md`).
- [ ] Dependencias o capas nuevas tienen ADR.

**Proceso**
- [ ] Plantilla `.bitbucket/PULL_REQUEST_TEMPLATE.md` completa (incluida la sección de asistencia de IA si se usó).
- [ ] Commits y rama siguen GIT-01/GIT-02; destino `develop`.
- [ ] Sin archivos de build, IDE ni secretos.

> **Criterio de rechazo inmediato:** dependencia arquitectónica sin ADR, secreto en el repo, import entre features, o una fitness function que empeora sin acuerdo explícito.
