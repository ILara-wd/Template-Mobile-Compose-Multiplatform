# API para Pagos y Detalle de pago

| Dato | Valor |
|------|-------|
| Pantallas | `payments/TkdPaymentsScreen.kt` (pestaña **Pagos**, Figma Prototipo-Demo-UiKit nodo 44:2) y `payments/TkdPaymentDetailScreen.kt` (nodo 46:325) |
| Estado actual | Datos de `FakeTkdPaymentsRepository` (noviembre de 2024); repositorio síncrono; los dos VM con default a la *Fake* (sin Hilt); `LocalDate.now()` en `TkdPaymentsUiState` (TD-KEN-005); montos en dólares enteros en la lista y centavos en el detalle |
| Depende de | Plan de cobro del dojo (`PUT dojo/settings`, [dojo-config-api.md](dojo-config-api.md) §6): cuota, moneda, cuota de inscripción (`enrollmentFeeCents`, nuevo), inscripción exenta y día de corte |
| Estado backend | **Implementado** (`netlify/lib/payments.ts`, migración `20261109195237_payments`, `tests/payments.test.ts`, carpeta **Pagos** de Postman) |
| Fecha | 2026-11-09 |

---

## 1. Resumen y decisiones

El servidor genera los **cargos** y la app **registra** los cobros. Un cargo es lo que un alumno debe por un concepto:
la mensualidad de un mes, la inscripción, un examen o un evento. Un **pago** salda un cargo completo con un método (no
hay pagos parciales). La lista "Transacciones" de la pantalla es la lista de cargos con su estado.

| # | Endpoint | Para qué |
|---|----------|----------|
| 1 | `GET /payments/summary?month=YYYY-MM` | Flujo de caja del mes y el examen o evento con cuota más próximo |
| 2 | `GET /payments/charges?status=&category=&studentId=&cursor=&limit=` | Lista de cargos con filtros y paginación |
| 3 | `GET /payments/charges/{chargeId}` | Detalle para cobrar: alumno, tutor, desglose, historial y recibo |
| 4 | `POST /payments/charges/{chargeId}/payments` | Registrar el cobro (con `Idempotency-Key`) |
| 5 | `DELETE /payments/charges/{chargeId}/payments/{paymentId}` | Anular un cobro registrado por error |
| 6 | `GET /payments/events` · `POST /payments/events` | Listar y crear exámenes y eventos con cuota |
| 7 | `GET /payments/events/{eventId}` | Detalle del evento con sus alumnos |
| 8 | `PUT` · `DELETE /payments/events/{eventId}/students/{studentId}` | Agregar o quitar a un alumno del evento |

Todas requieren `Authorization: Bearer` y responden `cache-control: no-store`. Montos en **centavos** con la `currency`
del plan.

**Fuera de alcance (por ahora):** recordatorios (P1), reporte CSV/PDF (P2), cobro con pasarela o tarjeta guardada.

### 1.1 Generación de cargos

| Cargo | Cuándo se crea | Vence | Monto |
|-------|----------------|-------|-------|
| **Mensualidad** (`MONTHLY`) | Al leer Pagos (`summary` o `charges`), para cada alumno `ACTIVE`. Es idempotente: una por alumno y mes | El `billingDay` del mes | `monthlyFeeCents`. Si es 0, no se generan |
| **Primer mes** | Igual, el mes del primer día de clase (`firstClassOn`) | El `billingDay`, o el primer día de clase si es después | Proporcional: `cuota × días restantes / días del mes`, redondeado al peso. Si empieza en los **últimos 7 días** del mes, ese mes no se cobra y paga completo el siguiente |
| **Inscripción** (`ENROLLMENT`) | Al dar de alta (`POST /students`), salvo `enrollmentWaived = true` o `enrollmentFeeCents = 0` | El día del alta | `enrollmentFeeCents` |
| **Examen / evento** (`EXAM`, `EVENT`) | Al crearlo desde Pagos (§3.6) o al agregar a un alumno | La fecha del evento | La suma de su desglose |

- **No hay cron.** La primera lectura de Pagos genera solo el **mes en curso**. Después, cada lectura genera los meses
  que falten desde el último generado (`dojo_settings.billing_generated_through`), aunque nadie haya abierto Pagos en
  semanas.
- **Alumnos pausados o de baja:** no generan mensualidades nuevas, pero lo que ya debían se queda pendiente y los
  cuenta como morosos.
- **Cambio de cuota o moneda:** cada cargo guarda su monto y moneda; el cambio solo afecta a los cargos nuevos.

### 1.2 Vencido y recargo

- Un cargo `PENDING` pasa a **`OVERDUE`** cuando `asOf` supera `dueOn`; no se guarda, se calcula al leer.
- **Recargo del 5 %**, solo en **mensualidades**, desde el día siguiente a `dueOn`, una sola vez (no se acumula por mes).
  Se redondea al centavo y se muestra como la línea `"Recargo 5% por pago tardío"` del desglose.
- El recargo se calcula al leer y **se congela al registrar el pago** (`payment.lateFeeCents`). Si se anula ese pago,
  se vuelve a calcular.

---

## 2. Mapa pantalla → datos

### 2.1 Pagos (nodo 44:2)

| Se pinta | Debe venir del API | Notas |
|----------|--------------------|-------|
| Encabezado del dojo | `GET /me` | |
| "Recaudado $4,850 de $5,470" | `summary.collectedCents`, `expectedCents`, `currency` | |
| "+12.4 % vs. mes anterior" | `summary.previousCollectedCents` | La app calcula el porcentaje (negativos y división entre 0) |
| "Pendiente $620 · 8 alumnos" | `summary.pendingCents`, `summary.studentsWithPending` | Incluye adeudos de meses anteriores |
| "Especiales $830" | `summary.otherCollectedCents` | Exámenes, eventos e inscripciones |
| "Al ritmo" | — | La app lo calcula: `collected / expected` contra el avance del mes en `asOf` |
| Evento próximo | `summary.upcomingEvent` (o `null`) | Sin eventos, el bloque se oculta |
| Filtros (Todos, Mensualidad, Exámenes, Eventos, Pendientes) | `GET /payments/charges?category=&status=` | "Pendientes" = `status=PENDING&status=OVERDUE` |
| Fila de transacción | `charges.items[]` | Foto, rango y estado como en Alumnos |
| Fecha de corte | `asOf` | Elimina `LocalDate.now()` del `UiState` |
| Crear examen / evento | `POST /payments/events` | Pantalla por diseñar (§3.6) |

### 2.2 Detalle de pago (nodo 46:325)

| Se pinta | Debe venir del API | Notas |
|----------|--------------------|-------|
| "Caja Tatami 01 · En línea" | — | **Se quita del diseño**: no hay cajas ni puntos de cobro en el dominio |
| Alumno, foto, edad, rango | `student.displayName`, `photoUrl`, `age`, `rank` | |
| Dojo y grupo | `student.scheduleDescription` | El nombre del dojo sale de `/me` |
| Tutor y teléfono | `student.guardian` (`name`, `relation`, `phone` E.164) | `null` si no tiene tutor; `student.contactPhone` siempre |
| "Mensualidad al día · 1 cargo pendiente" | `student.monthlyUpToDate`, `student.pendingCharges` | |
| Concepto, programa y fecha límite | `charge.concept`, `charge.programName`, `charge.dueOn` | |
| Desglose | `charge.lines[]` | Incluye el recargo si aplica |
| Tarjeta guardada | — | **Se quita**: no hay pasarela |
| Historial | `history[]` | Últimos 5 pagos del alumno |
| Nota | `charge.note` | La nota del pago vigente |
| Método y nota | Cuerpo del `POST` (§3.4) | Métodos: Efectivo, Transferencia, Terminal |
| "Enviar recibo" | `receipt` del `POST` o del detalle | Se comparte **desde el teléfono** (WhatsApp o SMS); el servidor no envía nada |

---

## 3. Endpoints

### 3.1 `GET /payments/summary?month=2026-11`

`month` es opcional (por defecto, el mes de `asOf` en la zona del dojo). Un valor que no sea `YYYY-MM` responde 422.

```json
{
  "asOf": "2026-11-19T18:05:00-06:00",
  "month": "2026-11",
  "currency": "MXN",
  "collectedCents": 485000,
  "expectedCents": 547000,
  "previousCollectedCents": 431500,
  "pendingCents": 62000,
  "studentsWithPending": 8,
  "otherCollectedCents": 83000,
  "upcomingEvent": {
    "id": "ev-7d1c…",
    "category": "EXAM",
    "title": "Examen Nacional de Grado",
    "date": "2026-11-28",
    "venue": "Tatami Central",
    "currency": "MXN",
    "lines": [{ "label": "Derecho a examen", "amountCents": 4500 }],
    "amountCents": 4500,
    "examId": "ex-41f0…",
    "candidates": 18,
    "candidatesPaid": 12,
    "collectedCents": 54000,
    "goalCents": 81000
  }
}
```

| Campo | Regla |
|-------|-------|
| `expectedCents` | Suma de los cargos con `dueOn` en el mes (cualquier categoría, pagados o no, sin cancelados), con recargo si aplica |
| `collectedCents` | Pagos vigentes con fecha en el mes, de cualquier cargo |
| `previousCollectedCents` | Lo mismo para el mes anterior |
| `pendingCents`, `studentsWithPending` | Cargos sin pagar con `dueOn` hasta el fin del mes, **incluidos los de meses anteriores** (morosos) |
| `otherCollectedCents` | La parte de `collectedCents` que no es mensualidad |
| `upcomingEvent` | El examen o evento más próximo desde hoy; `null` si no hay. `candidates` no cuenta a los quitados |

### 3.2 `GET /payments/charges`

Más recientes primero (`dueOn` y luego `id`, descendente). Parámetros opcionales:

| Parámetro | Valores |
|-----------|---------|
| `category` | `MONTHLY`, `ENROLLMENT`, `EXAM`, `EVENT`; se puede repetir |
| `status` | `PENDING`, `OVERDUE`, `PAID`, `CANCELED`; se puede repetir. Sin `status` se listan todos menos `CANCELED` |
| `studentId` | Solo los cargos de ese alumno |
| `cursor` | `nextCursor` de la página anterior |
| `limit` | 1–100 (default 20) |

```json
{
  "asOf": "2026-11-19T18:05:00-06:00",
  "items": [
    {
      "id": "ch-7f3a…",
      "concept": "Mensualidad noviembre 2026",
      "category": "MONTHLY",
      "amountCents": 68250,
      "currency": "MXN",
      "status": "OVERDUE",
      "dueOn": "2026-11-05",
      "paidOn": null,
      "method": null,
      "student": {
        "id": "st-kenji-sato",
        "displayName": "Kenji Sato",
        "photoUrl": null,
        "rank": { "discipline": "KARATE", "belt": "GREEN", "grade": { "system": "KYU", "value": 7 }, "stripes": 2 }
      }
    }
  ],
  "nextCursor": "eyJkIjoiMjAyNi0xMS0wNSIsImkiOiJjaC03ZjNhIn0"
}
```

| Campo | Regla |
|-------|-------|
| `amountCents` | Si está pagado, lo que se cobró; si no, el monto con el recargo de una mensualidad vencida |
| `method` | `CASH`, `TRANSFER`, `CARD` (terminal); solo con `PAID` |
| `nextCursor` | `null` en la última página. Es opaco: no lo armes en la app |

### 3.3 `GET /payments/charges/{chargeId}`

```json
{
  "asOf": "2026-11-19T18:05:00-06:00",
  "charge": {
    "id": "ch-7f3a…",
    "concept": "Mensualidad noviembre 2026",
    "programName": "Shotokan Karate",
    "category": "MONTHLY",
    "period": "2026-11",
    "eventId": null,
    "currency": "MXN",
    "status": "OVERDUE",
    "dueOn": "2026-11-05",
    "lines": [
      { "label": "Mensualidad noviembre 2026", "amountCents": 65000 },
      { "label": "Recargo 5% por pago tardío", "amountCents": 3250 }
    ],
    "amountCents": 68250,
    "note": null,
    "payment": null
  },
  "student": {
    "id": "st-kenji-sato",
    "displayName": "Kenji Sato",
    "age": 14,
    "photoUrl": null,
    "status": "ACTIVE",
    "rank": { "discipline": "KARATE", "belt": "GREEN", "grade": { "system": "KYU", "value": 7 }, "stripes": 2 },
    "scheduleDescription": "Tatami B • Sensei Tanaka • Cadetes",
    "guardian": { "name": "Carlos Sato", "relation": "FATHER", "phone": "+525543219876" },
    "contactPhone": "+525543219876",
    "monthlyUpToDate": false,
    "pendingCharges": 1
  },
  "history": [
    { "id": "pay-91c2…", "paidOn": "2026-10-01", "concept": "Mensualidad octubre 2026", "method": "CASH", "amountCents": 65000 }
  ],
  "receipt": null
}
```

| Campo | Regla |
|-------|-------|
| `amountCents` | Igual a la suma de `lines` |
| `period` | `YYYY-MM` en mensualidades; `null` en las demás |
| `payment` | `{ id, paidOn, method, amountCents, lateFeeCents }` del pago vigente, o `null` |
| `guardian` | `null` si el alumno no tiene tutor registrado; `phone` es el teléfono de contacto de la ficha |
| `monthlyUpToDate` | `false` si tiene alguna mensualidad vencida sin pagar |
| `pendingCharges` | Cargos sin pagar (pendientes o vencidos) del alumno |
| `history` | Últimos 5 pagos vigentes del alumno, más reciente primero |
| `receipt` | Solo si está pagado (§3.4) |

404 `CHARGE_NOT_FOUND` si no existe o es de otro dojo.

### 3.4 `POST /payments/charges/{chargeId}/payments`

Requiere `Idempotency-Key` (UUID que la app genera una vez por intento de cobro, como el folio del alta, hasta 100
caracteres). Un reintento tras un timeout con la misma llave y el mismo cuerpo devuelve el mismo pago.

```json
{ "method": "CASH", "note": "Entregado en efectivo al sensei (clase 5:30)" }
```

Respuesta `201`:

```json
{
  "payment": {
    "id": "pay-a1b2…",
    "chargeId": "ch-7f3a…",
    "paidOn": "2026-11-19",
    "method": "CASH",
    "amountCents": 68250,
    "lateFeeCents": 3250,
    "note": "Entregado en efectivo al sensei (clase 5:30)",
    "voided": false
  },
  "receipt": {
    "phone": "+525543219876",
    "text": "Kensho Dojo · Recibo de pago\nAlumno: Kenji Sato\nConcepto: Mensualidad noviembre 2026\nMonto: $682.50 MXN\nMétodo: Efectivo\nFecha: 19/11/2026\nFolio: pay-a1b2…\n¡Gracias!",
    "whatsappUrl": "https://wa.me/525543219876?text=Kensho%20Dojo%20…",
    "smsUrl": "sms:+525543219876?body=Kensho%20Dojo%20…"
  }
}
```

| Campo | Regla |
|-------|-------|
| `method` | `CASH`, `TRANSFER` o `CARD` (terminal). `SAVED_CARD` responde 422 `NOT_AVAILABLE` |
| `note` | Opcional, hasta 200 |
| `sendReceipt` | Ya no se usa: se acepta y se ignora |
| `paidOn` | Hoy en la zona del dojo |
| `receipt` | Texto listo y enlaces para abrir WhatsApp o SMS desde el teléfono, hacia el teléfono de contacto de la ficha (el del tutor si es menor). `null` si el alumno no tiene teléfono (solo alumnos creados antes de `POST /students`) |

### 3.5 `DELETE /payments/charges/{chargeId}/payments/{paymentId}`

Anula un cobro registrado por error. El pago no se borra (queda con `voided_at`) y el cargo vuelve a `PENDING` u
`OVERDUE`. Responde `200` con el detalle del cargo (§3.3). Anular un pago ya anulado también responde `200`. 404
`PAYMENT_NOT_FOUND` si el pago no existe, no es de ese cargo o es de otro dojo.

### 3.6 Exámenes y eventos

`POST /payments/events`:

```json
{
  "category": "EXAM",
  "title": "Examen de grado a cinta azul",
  "date": "2026-11-28",
  "venue": "Tatami Central",
  "lines": [
    { "label": "Tasa de evaluación y sinodales", "amountCents": 3500 },
    { "label": "Cinta nueva bordada y certificado", "amountCents": 1000 }
  ],
  "studentIds": ["st-kenji-sato", "st-maya-rodriguez"]
}
```

| Campo | Regla |
|-------|-------|
| `category` | `EXAM` o `EVENT` |
| `title` | Hasta 120; es el concepto de cada cargo |
| `date` | `YYYY-MM-DD`, hoy o después; es el vencimiento de los cargos |
| `venue` | Opcional, hasta 120 |
| `lines` | 1–10 líneas `{ label (hasta 80), amountCents (≥ 1) }`; el monto por alumno es su suma |
| `studentIds` | Opcional (hasta 500). Solo alumnos `ACTIVE` del dojo; los demás se marcan `studentIds[i]` `NOT_AVAILABLE` |

- Crea un cargo por alumno. Los eventos no llevan recargo aunque venzan.
- Un **examen** también crea la fila de `exams` y la fija como **próximo examen** (`nextExam`) de sus alumnos, que es
  lo que ya muestran la lista de Alumnos y el perfil. Reemplaza al próximo examen que tuvieran.
- Responde `201` con el detalle (§3.7) y `Location: /api/v1/payments/events/{eventId}`.
- Por ahora no se puede editar ni borrar un evento.

`GET /payments/events` → `{ asOf, upcoming: [Event], past: [Event] }`: los de hoy en adelante (el más próximo primero)
y los 20 pasados más recientes. `Event` tiene la misma forma que `upcomingEvent` (§3.1).

### 3.7 `GET /payments/events/{eventId}` y alumnos del evento

```json
{
  "asOf": "2026-11-19T18:05:00-06:00",
  "event": { "id": "ev-7d1c…", "category": "EXAM", "title": "…", "candidates": 2, "candidatesPaid": 1, "…": "…" },
  "students": [
    {
      "chargeId": "ch-…",
      "status": "PAID",
      "amountCents": 4500,
      "paidOn": "2026-11-19",
      "method": "CASH",
      "student": { "id": "st-kenji-sato", "displayName": "Kenji Sato", "photoUrl": null, "rank": { "…": "…" } }
    }
  ]
}
```

`students` está en orden alfabético y no incluye a los quitados.

- `PUT /payments/events/{eventId}/students/{studentId}` agrega al alumno: crea su cargo, o reactiva el que se había
  cancelado. Es idempotente. Responde `200` con el detalle.
- `DELETE /payments/events/{eventId}/students/{studentId}` lo quita: su cargo pasa a `CANCELED` (queda en el historial
  y se ve con `status=CANCELED`) y, si es examen, deja de ser su próximo examen. Un cargo pagado no se puede quitar:
  primero se anula el pago. Repetirlo responde `200`.

### 3.8 Errores

| HTTP | `code` | Cuándo | Qué hace la app |
|------|--------|--------|-----------------|
| `400` | `BAD_REQUEST` | Falta `Idempotency-Key` o el cuerpo no es JSON | — |
| `404` | `CHARGE_NOT_FOUND` | El cargo no existe o es de otro dojo | Regresa a la lista y recarga |
| `404` | `PAYMENT_NOT_FOUND` | El pago a anular no existe | Recarga el detalle |
| `404` | `EVENT_NOT_FOUND`, `STUDENT_NOT_FOUND`, `STUDENT_NOT_IN_EVENT` | Evento o alumno inexistente | Recarga |
| `409` | `CHARGE_ALREADY_PAID` | Otro dispositivo ya lo cobró, o se quiere quitar de un evento a quien ya pagó | Mensaje y recarga el detalle |
| `409` | `CHARGE_CANCELED` | Se quiere cobrar un cargo cancelado | Recarga |
| `409` | `IDEMPOTENCY_KEY_REUSED` | Misma llave con otro cuerpo | Genera una llave nueva y pide confirmar de nuevo |
| `409` | `STUDENT_NOT_ACTIVE` | Agregar a un evento a un alumno pausado o de baja | Mensaje |
| `422` | `VALIDATION_FAILED` | `fields: [{ field, code }]` como en `POST /students` | Marca el campo |

---

## 4. Modelo de datos

| Tabla | Qué guarda |
|-------|------------|
| `charges` | Un cargo: alumno, categoría, concepto, `period` (mensualidades), `event_id`, `due_on`, `currency`, `lines` (jsonb), `amount_cents` (sin recargo), `status` (`PENDING`, `PAID`, `CANCELED`). Índices únicos: una mensualidad por alumno y mes, una inscripción por alumno y un cargo por alumno y evento |
| `payments` | Un cobro: `paid_on`, `method`, `amount_cents` (con recargo), `late_fee_cents`, `note`, `idempotency_key` + `request_hash`, `voided_at`. Solo un pago vigente por cargo |
| `billing_events` | Examen o evento con cuota: título, fecha, sede, desglose, moneda y `exam_id` |
| `dojo_settings` | Nuevas columnas: `enrollment_fee_cents` y `billing_generated_through` |

Las mutaciones de Pagos de un dojo (generación, cobros, anulaciones y eventos) corren en transacción con
`pg_advisory_xact_lock` por dojo.

---

## 5. Impacto en la app

| Pieza | Hoy | Cambio |
|-------|-----|--------|
| `TkdPaymentsRepository` | 5 funciones síncronas | `suspend fun summary(month)`, `charges(filter, cursor)`, `charge(id)`, `pay(id, request, idempotencyKey)`, `void(chargeId, paymentId)`, eventos → `TkdResult` |
| Implementaciones | `FakeTkdPaymentsRepository` (2024, en dólares) | Fake con fechas de 2026 en MXN y centavos + `NetworkTkdPaymentsRepository` |
| Modelo | `CashFlow` en dólares; `growth`, `onTrack` fijos; `beltLabel`/`rankLabel` como texto; `photo: @DrawableRes`; `register`; `savedCard` | Centavos + `Currency`; derivados en el modelo; `Rank`; `photoUrl`; **sin** `register` ni `savedCard` |
| Recibo | — | `Intent` a `receipt.whatsappUrl` (o `ACTION_SEND` con `receipt.text`); `smsUrl` como alternativa |
| `TkdPaymentsUiState` | `today = LocalDate.now()` | `asOf` del API; `Loading/Error` + `onRetry()`; paginación |
| VMs | `= FakeTkdPaymentsRepository()`, `= FakeTkdDashboardRepository()` | `@HiltViewModel`, sin defaults; encabezado desde `GET /me` |
| Dinero | `$%,d` en dólares | `centsAsMoney(currency)` (`core/TkdFormat.kt`) |
| Tests | — | Mapper (fixtures `payments_summary.json`, `payments_charges.json`, `payments_charge_detail.json`), repositorio (200, 404, 409 ya pagado, timeout + reintento con la misma llave), VMs |

---

## 6. Preguntas resueltas

1. **Cobro real:** solo se **registra** el cobro hecho fuera: efectivo, transferencia o terminal. No hay `SAVED_CARD`.
2. **Exámenes y eventos:** se crean desde Pagos (§3.6). Un examen se registra en `exams` y fija el `nextExam` de sus
   alumnos. Se pueden agregar y quitar alumnos después de crearlo.
3. **"Caja Tatami 01":** no hay cajas ni puntos de cobro en el dominio; se quita del diseño.
4. **Recargos:** 5 % desde el día siguiente al vencimiento, una sola vez, solo en mensualidades (§1.2).
5. **Prorrateo:** el primer mes se cobra proporcional a los días; si empieza en la última semana, paga completo el
   mes siguiente (§1.1).
6. **Recibo:** se comparte desde el teléfono por WhatsApp o mensaje de texto (`receipt`). El alta ya exige el teléfono
   de contacto (el del tutor si es menor).
7. **Pagos parciales:** no; un pago salda el cargo completo.
8. **Pago registrado por error:** se anula (§3.5).
9. **Pausados y bajas:** no generan mensualidades; lo pendiente se queda como adeudo.
10. **Arranque:** al desplegar solo se genera el mes en curso; no hay cargos retroactivos.

## 7. Pendiente

- Recordatorios a morosos (P1) y reporte del mes (P2). Mientras no haya endpoint, la app los manda desde el teléfono
  (PAY-003, 2026-11-19): lista los cargos pendientes y vencidos, toma el teléfono del detalle de cada uno y arma el
  texto en `strings.xml`. Un endpoint con el texto y los enlaces (como `receipt`) evitaría pedir un detalle por cargo.
- Editar o borrar un examen o evento.
- Historial de estados del alumno: la generación usa su estado **actual**. Si nadie abrió Pagos mientras estuvo
  pausado y ya volvió a `ACTIVE`, la puesta al día también le genera esos meses.
- Cancelar o condonar un cargo suelto (p. ej. una mensualidad): hoy solo se cancelan los de eventos, quitando al alumno.
