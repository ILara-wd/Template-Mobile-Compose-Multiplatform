<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/validate-pullrequest/ff-cov.md. NO EDITAR A MANO. -->
---
mode: agent
description: 5.12 Revisiones de Tests (FF-COV-XX)
---

## 5.12 Revisiones de Tests (FF-COV-XX)

Estrategia de pruebas de Kensho (`03-standards.md §9`, ADR-013, TD-KEN-001): **unit tests JVM** para ViewModels y lógica pura usando las *Fakes*, y **Compose UI Test** sobre `Content` para flujos interactivos. No hay Jacoco configurado todavía: la regla es por existencia y contenido de tests, no por porcentaje.

| ID | Regla | Severidad |
|---|---|---|
| FF-COV-01 | ViewModel nuevo, o con eventos nuevos, sin `Tkd<X>ViewModelTest` en `app/src/test` que cubra estado inicial y cada evento (TEST-01) | 🟠 nuevo · 🟡 existente |
| FF-COV-02 | Lógica pura nueva (`core/`, `*Format.kt`, propiedades derivadas en `model/`, filtros en `data/`) sin unit test (TEST-02) | 🟠 nuevo · 🟡 existente |
| FF-COV-03 | Pantalla nueva con interacción (selección, formulario, confirmación) sin Compose UI Test sobre su `Content` (TEST-03) | 🟡 |
| FF-COV-04 | Test que no sigue `sujeto_condicion_resultado`, sin aserciones, o test de ejemplo (`ExampleUnitTest`) agregado (TEST-04/05) | 🟡 |
| FF-COV-05 | `NetworkTkd<X>Repository` nuevo/modificado sin test con `MockWebServer`, o mapper `data/remote/mapper/` sin test con fixtures (TEST-06, ADR-017) | 🟠 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
[ -z "$PR_MAIN" ] && { echo "ℹ️ FF-COV: sin código productivo en el cambio"; exit 0; }
sev() { is_new_file "$1" && echo 🟠 || echo 🟡; }

# FF-COV-01 — ViewModels
for f in $PR_VIEWMODELS; do
  vm=$(basename "$f" .kt)
  t=$(grep -rlE "\b$vm\(" app/src/test 2>/dev/null | head -1)
  if [ -z "$t" ]; then echo "$(sev "$f") FF-COV-01 $vm — sin unit test (esperado ${UNIT_TEST_DIR}/$(feature_of "$f")/${vm}Test.kt)"; continue; fi
  events=$(grep -oE '^[[:space:]]*fun on[A-Z][A-Za-z0-9]*' "$f" | sed -E 's/.*fun //' | sort -u)
  missing=$(for e in $events; do grep -q "\.$e(" "$t" || echo "$e"; done | tr '\n' ' ')
  echo "ℹ️ FF-COV-01 $vm → $t ($(grep -c '@Test' "$t") @Test)"
  [ -n "$missing" ] && echo "🟡 FF-COV-01 $vm — eventos sin test: $missing"
done

# FF-COV-02 — lógica pura
for f in $(printf '%s\n' $PR_MAIN | grep -E "/core/|Format\.kt$|/model/|/data/"); do
  git diff "$BASE_REF"...HEAD -- "$f" | grep -qE '^\+[[:space:]]*(internal |private )?(fun |val [a-zA-Z]+[^=]*get\(\)|val [a-zA-Z]+: [A-Za-z<>]+ get)' || continue
  names=$(git diff "$BASE_REF"...HEAD -- "$f" | grep -oE '^\+[[:space:]]*(internal |private )?fun ([A-Za-z]+\.)?[a-zA-Z0-9]+' | sed -E 's/.*fun ([A-Za-z]+\.)?//' | sort -u)
  for n in $names; do
    grep -rqE "\b$n\b" app/src/test 2>/dev/null || echo "$(sev "$f") FF-COV-02 $f — \`$n\` sin unit test"
  done
done

# FF-COV-03 — pantallas nuevas con interacción
for f in $(printf '%s\n' $PR_MAIN_NEW | grep 'Screen\.kt$'); do
  content=$(grep -oE '^fun Tkd[A-Za-z]*Content' "$f" | sed 's/fun //' | head -1)
  [ -z "$content" ] && continue
  grep -qE 'on[A-Z][A-Za-z]*: *\(' "$f" || continue
  grep -rqE "\b$content\(" app/src/androidTest 2>/dev/null || echo "🟡 FF-COV-03 $content — sin Compose UI Test en ${UI_TEST_DIR}/$(feature_of "$f")/"
done

# FF-COV-05 — red: repositorios con MockWebServer y mappers con fixtures (ADR-017)
for f in $(printf '%s\n' $PR_DATA | grep -E 'NetworkTkd[A-Za-z]*Repository\.kt$'); do
  repo=$(basename "$f" .kt)
  grep -rlE "\b$repo\(" app/src/test 2>/dev/null | xargs -r grep -l 'MockWebServer' | grep -q . \
    || echo "🟠 FF-COV-05 $repo — sin test con MockWebServer (2xx, 401, 404, 409, 5xx, timeout, JSON malformado)"
done
for f in $(printf '%s\n' $PR_DATA | grep '/data/remote/mapper/'); do
  for n in $(git diff "$BASE_REF"...HEAD -- "$f" | grep -oE '^\+[[:space:]]*(internal )?fun [A-Za-z]+Dto\.[a-zA-Z]+' | sed -E 's/.*fun //' | sort -u); do
    grep -rqF "${n#*.}" app/src/test 2>/dev/null || echo "🟠 FF-COV-05 $f — $n sin test con fixtures JSON"
  done
done

# FF-COV-04 — calidad de los tests agregados
for t in $PR_UNIT_TESTS $PR_UI_TESTS; do
  case "$(basename "$t")" in Example*) echo "🟡 FF-COV-04 $t — test de ejemplo: reemplazar por tests reales"; continue ;; esac
  added_lines "$t" | grep -E ':[[:space:]]*fun [a-zA-Z]' | grep -vE 'fun [a-z][A-Za-z0-9]*_[a-z][A-Za-z0-9]*_[a-z][A-Za-z0-9]*\(|fun (setUp|tearDown|before|after)' \
    | sed 's/^/🟡 FF-COV-04 nombre fuera de sujeto_condicion_resultado /'
  grep -qE 'assert|assertThat|assertEquals|assertTrue|assertIs|onNode.*assert' "$t" || echo "🟡 FF-COV-04 $t — sin aserciones"
done
EOF
```

**Cómo interpretar:**
- **FF-COV-01:** el VM se instancia con la *Fake* (o un repositorio de prueba local al test) y se lee `viewModel.uiState.value`. Si el VM usa corrutinas, `kotlinx-coroutines-test` con `runTest` y `Dispatchers.setMain`.
- Los tests instrumentados **no corren en este skill** (requieren emulador): si hace falta, `./gradlew :app:connectedDebugAndroidTest` y anexar el resultado. Nunca afirmes que pasan sin haberlos corrido. Los unit tests sí: `./gradlew :app:testDebugUnitTest`.
- Modelos sin lógica (`data class` solo con campos) no exigen test; las propiedades calculadas (`totalCents`, `visibleTransactions`) sí.
