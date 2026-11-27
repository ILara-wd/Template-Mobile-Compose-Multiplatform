# 🛡️ :lint-rules — reglas Detekt de Kensho

Reglas propias (`Kensho*`, `ruleSetId = "kensho"`) que convierten las fitness functions de
[`01-governance.md §7`](../docs/architecture/01-governance.md) en hallazgos de Detekt, con el ID de la regla de
[`03-standards.md`](../docs/architecture/03-standards.md) en cada mensaje.

**Plan, catálogo y proceso:** [`docs/architecture/08-lint-rules.md`](../docs/architecture/08-lint-rules.md)

---

## Uso

```bash
./gradlew detekt               # analiza :app con detekt.yml + baseline (reporte en build/reports/detekt/)
./gradlew :lint-rules:test     # tests de las reglas
./gradlew detektBaseline       # regenera config/detekt/detekt-baseline.xml — solo para ENCOGERLO (08 §5)
```

## Reglas

| Regla | Detecta | Estándar | FF |
|-------|---------|----------|----|
| `KenshoNoLog` | `android.util.Log`, `println`/`print`, `printStackTrace()` | LOG-01 | FF-01 |
| `KenshoNoNullAssertion` | `!!` fuera de tests | ADR-012 | FF-02 |
| `KenshoNoGlobalScope` | `GlobalScope` | ADR-012 | FF-02 |
| `KenshoNoRunBlocking` | `runBlocking` | ADR-012 | FF-02 |
| `KenshoNoColorLiteral` | `Color(0x…)`, `Color(r, g, b)`, `Color.White`… fuera de `ui.theme` | THM-01 | FF-03 |
| `KenshoNoDpLiteral` | `12.dp` / `14.sp` fuera de `ui.theme` | THM-02 | FF-04 |
| `KenshoNoLiteralText` | string literal en `Text(…)` o `contentDescription` | I18N-01 | FF-05 |
| `KenshoNullContentDescriptionNeedsComment` | `contentDescription = null` sin `//` en la línea | A11Y-01 | FF-06 |
| `KenshoNoSystemClock` | `LocalDate.now()`, `Instant.now()`, `System.currentTimeMillis()`… fuera de `data` | ARC-07 | FF-07 |
| `KenshoNoCrossFeatureImport` | import entre features (`features` en `detekt.yml`) | ARC-05 | FF-08 |
| `KenshoMaxFileLines` | archivo > `maxLines` (300) | STR-02 | FF-09 |
| `KenshoNoCollectAsState` | `collectAsState()` | PERF-02 | FF-10 |
| `KenshoPublicComposableNeedsPreview` | `@Composable` público sin `@Preview`/`@TkdFontScalePreviews` en el archivo (solo `components/`) | CMP-09 | FF-11 |
| `KenshoNoFontScaleMultiplication` | `… * fontScale` | A11Y-06 | FF-13 |
| `KenshoNoPublicMutableStateFlow` | `MutableStateFlow`/`MutableSharedFlow` público | ARC-02 | — |

Las reglas razonan por **paquete** (`dev.treecode.kensho.ui.theme`, `.data`, features), no por ruta, para poder
probarse con código en memoria; las exclusiones por ruta (`**/test/**`) van en `config/detekt/detekt.yml`.

## Estructura

```
lint-rules/
├── build.gradle.kts                      # Kotlin JVM, compileOnly detekt-api, tests JUnit 5 + AssertJ
└── src/
    ├── main/kotlin/dev/treecode/kensho/lint/
    │   ├── KenshoRuleSetProvider.kt      # registra las reglas
    │   └── rules/
    │       ├── KenshoRule.kt             # base: Issue + report + helpers de paquete
    │       └── Kensho*.kt                # una regla por archivo
    ├── main/resources/META-INF/services/io.gitlab.arturbosch.detekt.api.RuleSetProvider
    └── test/kotlin/dev/treecode/kensho/lint/rules/Kensho*Test.kt
```

## Compatibilidad

Detekt 1.23.8 trae su propio compilador (Kotlin 2.0.21) y el proyecto usa Kotlin 2.2. Por eso el módulo compila con
`apiVersion`/`languageVersion` 2.0 y el root agrega `detektPlugins(project(":lint-rules")) { isTransitive = false }`.
Al pasar a Detekt 2.x (API distinta: `Rule(config, description)`, sin `Issue`) hay que migrar `KenshoRule.kt`.

## Añadir una regla

Proceso de [`08-lint-rules.md §4.3`](../docs/architecture/08-lint-rules.md): la regla existe en `03-standards.md` →
clase `Kensho<Nombre>` que extiende `KenshoRule` → registro en `KenshoRuleSetProvider` → tests positivo, negativo y de
exclusión → activación en `detekt.yml` → fila en esta tabla y en `08-lint-rules.md §2.1`.
