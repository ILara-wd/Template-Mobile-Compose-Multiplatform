<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/sportkid-governance/fitness-functions.md. NO EDITAR A MANO. -->
---
mode: agent
description: Fitness Functions — chequeo rápido de todo el proyecto (línea base, no PR)
---

## Fitness Functions — chequeo rápido de todo el proyecto (línea base, no PR)

Se activa cuando necesitas el **estado actual de todo SportKid** (`:app`, `core:*` y `feature:*`): para refrescar la tabla `TD-SK-0XX` de `CLAUDE.md` §Deuda técnica, para decidir qué deuda atacar o para redactar una historia de deuda (`tech-story-architect:liquidar-deuda`). No se activa para auditar un PR: para eso está `/validate-pullrequest`, que aplica estas reglas acotadas a las líneas agregadas.

**Una sola fuente para las cifras:** los valores vigentes viven en `CLAUDE.md` §Deuda técnica (línea base y fecha en el título de la sección). Estos comandos **recalculan**; no anotes números fijos aquí. Corre, compara y, si difiere, actualiza vía `sportkid-governance:propagacion`.

SportKid no tiene un documento de fitness functions ni ADRs: los IDs `FF-01..FF-16` son locales de este chequeo y cada línea indica la regla de `CLAUDE.md` §Convenciones o la fila `TD-SK-0XX` que mide. Córrelo desde la raíz del repo (funciona desde zsh: el heredoc se ejecuta con bash).

```bash
bash <<'EOF'
SRC="app/src/main core/designsystem/src/main core/model/src/main core/ui/src/main $(ls -d feature/*/src/main 2>/dev/null)"
FEAT=$(ls -d feature/*/src/main)
UI=core/ui/src/main
DS=core/designsystem/src/main
THEME=$DS/kotlin/com/invex/sportkid/core/designsystem/theme
kt() { grep -rn --include='*.kt' "$@"; }
n() { wc -l | tr -d ' '; }
DP='\b[0-9]+(\.[0-9]+)?\.dp\b'
CLOCK='LocalDate\.now\(|LocalDateTime\.now\(|LocalTime\.now\(|System\.currentTimeMillis\('

echo "FF-01 logging directo (Log/println/printStackTrace):         $(kt -E 'android\.util\.Log|\bLog\.[dewiv]\(|println\(|printStackTrace\(' $SRC | n)"
echo "FF-02 !! / GlobalScope / runBlocking:                         $(kt -E '!!|GlobalScope|runBlocking' $SRC | n)"
echo "FF-03 Color(0x…) fuera de designsystem/theme (TD-SK-004):      $(kt -E '\bColor\(0x' $SRC | grep -v "^$THEME/" | n)"
echo "FF-04 N.dp en feature/ (TD-SK-004):                            $(kt -E "$DP" $FEAT | n) líneas ($(kt -oE "$DP" $FEAT | n) literales)"
echo "FF-05 N.dp en core:ui (TD-SK-004):                             $(kt -E "$DP" $UI | n) líneas ($(kt -oE "$DP" $UI | n) literales)"
echo "FF-06 N.sp fuera de designsystem/theme:                        $(kt -E '\b[0-9]+(\.[0-9]+)?\.sp\b' $SRC | grep -v "^$THEME/" | n)"
echo "FF-07 archivos .kt > 400 líneas (TD-SK-005):                   $(find $SRC -name '*.kt' -exec wc -l {} + | awk '$2 != "total" && $1 > 400' | n) (máx. $(find $SRC -name '*.kt' -exec wc -l {} + | awk '$2 != "total"' | sort -rn | head -1 | awk '{sub(".*/","",$2); print $2", "$1}'))"
echo "FF-08 contentDescription = null (TD-SK-006):                   $(kt -E 'contentDescription\s*=\s*null' $SRC | n)"
echo "FF-09 TODO en el código (TD-SK-002):                           $(kt -E '\bTODO\b' $SRC | n)"
echo "FF-10 feature → otro feature (imports / Gradle):               $(for f in $(ls -d feature/*/src/main | cut -d/ -f2); do kt -E '^import com\.invex\.sportkid\.feature\.' feature/$f/src/main | grep -v "import com.invex.sportkid.feature.$f\."; done | n) / $(grep -lE '":feature:' feature/*/build.gradle.kts 2>/dev/null | n)"
echo "FF-11 core:designsystem → core:model/ui (imports / Gradle):    $(kt -E '^import com\.invex\.sportkid\.core\.(model|ui)\.' $DS | n) / $(grep -cE 'projects\.core\.(model|ui)|":core:(model|ui)"' core/designsystem/build.gradle.kts)"
echo "FF-12 reloj fuera del default 'today' de un Screen:            $(kt -E "$CLOCK" $SRC | grep -vE 'Screen\.kt:[0-9]+:\s+today: LocalDate = LocalDate\.now\(\),$' | n)"
echo "FF-13 Text(\"literal\") sin stringResource:                      $(kt -E '\bText\(\s*(text\s*=\s*)?"[^"$]*[A-Za-zÁÉÍÓÚáéíóúñ]' $SRC | n)"
echo "FF-14 archivos con fun Sk* sin @SkPreviews:                    $(for f in $(grep -rlE '^fun Sk[A-Z]' $DS --include='*.kt'); do grep -q '@SkPreviews' $f || echo $f; done | n) de $(grep -rlE '^fun Sk[A-Z]' $DS --include='*.kt' | n)"
echo "FF-15 dependencias fuera de libs.versions.toml:                $(grep -nE '(implementation|api|ksp|kapt)\(\s*"' app/build.gradle.kts core/*/build.gradle.kts feature/*/build.gradle.kts 2>/dev/null | n)"
echo "FF-16 tests (TD-SK-001):                                       $(find app core feature -path '*/build' -prune -o \( -path '*/src/test/*' -o -path '*/src/androidTest/*' \) -name '*Test.kt' -print | n) archivos, $(find app core feature -path '*/build' -prune -o \( -path '*/src/test/*' -o -path '*/src/androidTest/*' \) -name '*.kt' -print | xargs grep -h '@Test' 2>/dev/null | n) @Test"
echo "—— Complementarias"
echo "MainActivity.kt líneas / casos de Destination (TD-SK-003):     $(wc -l < app/src/main/java/com/invex/one/template/MainActivity.kt | tr -d ' ') / $(sed -n 's/.*enum class Destination *{\(.*\)}.*/\1/p' app/src/main/java/com/invex/one/template/MainActivity.kt | tr ',' '\n' | grep -c '[A-Za-z]')"
echo "Restos de plantilla (TD-SK-007):                               $(grep -cE 'com\.invex\.one\.template' app/build.gradle.kts) refs en app/build.gradle.kts; carpetas feature/ sin módulo: $(for d in feature/*/; do m=$(basename $d); grep -q "\":feature:$m\"" settings.gradle.kts || printf '%s ' $m; done)"
echo "Detekt en Gradle / workflows de CI (TD-SK-008):                $(grep -lE 'detekt' build.gradle.kts app/build.gradle.kts core/*/build.gradle.kts feature/*/build.gradle.kts gradle/libs.versions.toml 2>/dev/null | n) / $(ls .github/workflows 2>/dev/null | n)"
echo "@Composable > 60 líneas:                                       $(for f in $(find $SRC -name '*.kt'); do awk '/^[[:space:]]*@Composable/{p=1;next} p&&/fun /{s=NR;d=0;i=1;p=0} i{o=gsub(/\{/,"{");c=gsub(/\}/,"}");d+=o-c;if(o>0)op=1;if(op&&d<=0){if(NR-s+1>60)print FILENAME":"s;i=0;op=0}} !/^[[:space:]]*@/&&!i{p=0}' $f; done | n)"
echo "@SkPreviews / @Preview sueltos:                                $(kt '@SkPreviews' $SRC | grep -v 'annotation class' | n) / $(kt -E '@Preview\b' $SRC | n)"
echo "stringResource / pluralStringResource:                         $(kt 'stringResource(' $SRC | grep -v pluralStringResource | n) / $(kt 'pluralStringResource(' $SRC | n)"
echo "fotos > 200 KB en res/drawable*:                               $(find $SRC -path '*/res/drawable*' -type f \( -name '*.jpg' -o -name '*.png' -o -name '*.webp' \) -size +200k | n)"
EOF
```

### Cómo leer cada resultado

Compara contra la fila de `CLAUDE.md` §Deuda técnica, no contra un número embebido en esta skill. Lo que **no** tiene fila TD se espera en 0 (son reglas de §Convenciones):

| Línea | Regla en `CLAUDE.md` | Esperado |
|---|---|---|
| FF-01, FF-02 | §Convenciones "Prohibido en `src/main`" · §Deuda técnica "Fortalezas" | 0 |
| FF-03 | §Convenciones "Tokens" (nunca `Color(0x…)` fuera de `core/designsystem/theme/`) · TD-SK-004 | baja hasta 0 |
| FF-04, FF-05, FF-06 | §Convenciones "Tokens" (espaciado de `SportKidTheme.spacing`) · README §6.2 · TD-SK-004. CLAUDE.md cuenta **líneas** con literal (`grep -c`), no literales sueltos | solo baja |
| FF-07 | §Convenciones "Tamaño" (≤ 400 líneas) · TD-SK-005 | solo baja |
| FF-08 | §Convenciones "Accesibilidad" (`null` solo decorativo) · TD-SK-006 | revisar caso a caso |
| FF-09 | TD-SK-002 (guardar en formularios es `TODO`) | solo baja |
| FF-10, FF-11 | §Arquitectura "Reglas de dependencia" | 0 |
| FF-12 | §Convenciones "Fechas" ("hoy" solo como default de la pantalla, nunca en modelos o lógica) | baja hasta 0 |
| FF-13 | §Convenciones "Textos" (`stringResource` siempre) | 0 fuera de previews |
| FF-14 | §Convenciones "Componentes" (≥ 1 preview `@SkPreviews`) | 0 |
| FF-15 | §Convenciones "Dependencias" (solo vía `gradle/libs.versions.toml`) | 0 |
| FF-16 | TD-SK-001 | solo sube |
| Complementarias | TD-SK-003, TD-SK-007, TD-SK-008 y señales de salud (composables > 60 líneas se dividen en secciones `private`) | informativo |

Los conteos de `grep` son **heurísticos**; para una cifra que vaya a `CLAUDE.md`, confirma los casos límite leyendo el código:

- **FF-03** incluye colores en previews de `core:designsystem/component/`: la regla aplica igual.
- **FF-12** solo exime la línea exacta `today: LocalDate = LocalDate.now(),` dentro de un `*Screen.kt`. Cuenta como hallazgo el reloj usado como default en `<X>Samples`, en `Draft.empty(...)` o en un holder `<X>State`, y `System.currentTimeMillis()` en composables o modelos.
- **FF-13** ignora textos que empiezan con plantilla (`"${…}"`, `"$count"`); los que quedan suelen estar en previews (`TokensPreview`, previews de `SkCard`), que no son UI de producción pero conviene revisar.
- **FF-14** mira archivos de `core:designsystem` con `fun Sk…` público sin `@SkPreviews`; un `@Preview` suelto no cuenta.
- **Composables > 60 líneas** es un conteo aproximado por llaves (no entiende lambdas en strings).

Si una cifra difiere de `CLAUDE.md`, no la corrijas aquí: actualiza la fila (y la fecha de línea base) vía `sportkid-governance:propagacion`.
