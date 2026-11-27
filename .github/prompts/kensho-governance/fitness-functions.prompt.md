<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/kensho-governance/fitness-functions.md. NO EDITAR A MANO. -->
---
mode: agent
description: Fitness Functions — chequeo rápido de todo el proyecto (línea base, no PR)
---

## Fitness Functions — chequeo rápido de todo el proyecto (línea base, no PR)

Se activa cuando necesitas el **estado actual de todo `:app`** (p. ej. antes de refrescar `docs/architecture/06-compliance-report.md` o para decidir qué deuda atacar). No se activa para auditar un PR: para eso está `/validate-pullrequest`, que aplica estas reglas acotadas a las líneas agregadas.

**Una sola fuente para las cifras:** los valores vigentes de cada `TD-KEN-XXX` viven en `CLAUDE.md` §Deuda técnica y en `06-compliance-report.md`. Estos comandos **recalculan**; no anotes números fijos aquí. Corre, compara y, si difiere, actualiza vía `kensho-governance:propagacion`.

Los IDs siguen `01-governance.md §7` (`FF-01`..`FF-16`); entre paréntesis, la revisión equivalente de `/validate-pullrequest`.

```bash
bash <<'EOF'
M=app/src/main/java/dev/treecode/kensho
n() { wc -l | tr -d ' '; }

echo "FF-01 logging directo (FF-CORE-01, ADR-012):                 $(grep -rnE 'android\.util\.Log|println\(|printStackTrace\(' $M | n)"
echo "FF-02 !! / GlobalScope / runBlocking (FF-CORE-02):            $(grep -rnE '!!|GlobalScope|runBlocking' $M | n)"
echo "FF-03 colores literales fuera de ui/theme (FF-THM-01):        $(grep -rnE '\bColor\(0x|\bColor\.(White|Black|Red|Gray|Green|Blue|Yellow|Cyan|Magenta)\b' $M | grep -v /ui/theme/ | n)"
echo "FF-04 dp/sp literales fuera de ui/theme (FF-THM-02):          $(grep -rnE '[0-9]\.(dp|sp)\b' $M | grep -v /ui/theme/ | n)"
echo "FF-05 Text(\"literal\") (FF-I18N-01):                           $(grep -rnE 'Text\(\s*(text\s*=\s*)?"' $M | n)"
echo "FF-06 contentDescription = null sin comentario (FF-A11Y-02):  $(grep -rn 'contentDescription = null' $M | grep -v '//' | n)"
echo "FF-07 reloj del sistema fuera de data/ (FF-CORE-03):          $(grep -rnE 'LocalDate\.now\(|LocalDateTime\.now\(|System\.currentTimeMillis\(' $M | grep -v /data/ | n)"
echo "FF-08 imports entre features (FF-CORE-04):                    $(for f in login signup students profile checkin payments ui.present; do
  grep -rn "import dev.treecode.kensho\.\(login\|signup\|students\|profile\|checkin\|payments\|ui.present\)\." $M/${f//.//} \
    | grep -v "import dev.treecode.kensho.$f\." ; done | n)"
echo "FF-09 archivos > 300 líneas (FF-ST-03):                       $(find $M -name '*.kt' -exec wc -l {} + | awk '$2 != "total" && $1 > 300' | n)"
echo "FF-10 collectAsState() (FF-PF-04):                            $(grep -rn 'collectAsState()' $M | n)"
echo "FF-11 componentes públicos sin preview (FF-CMP-09):           $(for f in $(grep -rlE '^fun [A-Z]' $M/components); do grep -qE '@Preview|@TkdFontScalePreviews' $f || echo $f; done | n)"
echo "FF-12 archivos de test (FF-COV):                              $(find app/src/test app/src/androidTest -name '*Test.kt' | n) ($(grep -rn '@Test' app/src/test app/src/androidTest | n) @Test)"
echo "FF-13 escalado manual de fuente (FF-A11Y):                    $(grep -rnE 'fontScale\s*\*|\*\s*[A-Za-z.]*fontScale' $M | grep -v /ui/theme/TkdFontScale.kt | n)"
echo "FF-14 red/DTO fuera de data/ y di/ (FF-CORE-11):              $(grep -rnE 'import (retrofit2|okhttp3)\.|import dev\.treecode\.kensho\.data\.remote\.' $M | grep -vE '/data/|/di/' | n)"
echo "FF-15 defaults a la Fake (FF-ARC-09):                         $(grep -rnE 'Repository = Fake' $M | n)"
echo "FF-16 dispatchers literales fuera de di/ (FF-ARC-11):         $(grep -rnE 'Dispatchers\.(IO|Default)' $M | grep -v /di/ | n)"
echo "—— Complementarias"
echo "@Composable > 60 líneas (CMP-08):                             $(for f in $(find $M -name '*.kt'); do awk '/^[[:space:]]*@Composable/{p=1;next} p&&/fun /{s=NR;d=0;i=1;p=0} i{o=gsub(/\{/,"{");c=gsub(/\}/,"}");d+=o-c;if(o>0)op=1;if(op&&d<=0){if(NR-s+1>60)print;i=0;op=0}} !/^[[:space:]]*@/&&!i{p=0}' $f; done | n)"
echo "repositorios de red / interfaces:                             $(find $M/data -name 'NetworkTkd*Repository.kt' 2>/dev/null | n) / $(grep -rlE '^interface Tkd[A-Za-z]*Repository' $M/data | n)"
echo "ViewModels / con test:                                        $(find $M -name '*ViewModel.kt' | n) / $(for v in $(find $M -name '*ViewModel.kt' -exec basename {} .kt \;); do grep -rlq "\b$v(" app/src/test 2>/dev/null && echo $v; done | n)"
echo "@Preview:                                                     $(grep -rnE '@Preview|@TkdFontScalePreviews' $M | n)"
echo "modelos @Immutable / data class en model/:                    $(grep -rn '@Immutable' $M/model | n) / $(grep -rnE '^(@Immutable )?data class' $M/model | n)"
echo "stringResource:                                               $(grep -rn 'stringResource' $M | n)"
echo "vectores ic_tkd sin cabecera de Figma:                        $(for d in app/src/main/res/drawable/ic_tkd_*.xml; do head -3 "$d" | grep -q 'Generado desde Figma' || echo "$d"; done | n)"
echo "fotos > 200 KB:                                               $(find app/src/main/res/drawable* -type f \( -name '*.jpg' -o -name '*.png' -o -name '*.webp' \) -size +200k | n)"
EOF
```

Interpreta cada resultado contra la fila correspondiente de `CLAUDE.md` §Deuda técnica y de `06-compliance-report.md`, no contra un número embebido en esta skill. Los conteos de `grep` son heurísticos (p. ej. FF-06 no distingue un comentario de justificación de uno cualquiera); para cifras de reporte, confirma los casos límite leyendo el código.
