# FightingMates: diseño de datos inicial

Este documento describe la primera estructura de datos JSON para cartas, rarezas, clases de objeto, objetos y habilidades de FightingMates.

## Principios aplicados

- Las cartas son la fuente principal de diseño: objetos, clases y habilidades se derivan de lo que ya aparece en las cartas.
- No existe sistema de energía. Los datos y esquemas evitan campos como `cost`, `energy`, `energy_gain` y `energy_loss`.
- Las únicas rarezas activas son:
  - `perro`: común.
  - `oca`: épica.
- `pato` queda registrada solo como rareza eliminada dentro de la taxonomía para evitar reintroducirla como valor válido.
- Todo contenido nuevo o todavía no cerrado se marca como `draft` o `provisional`.

## Archivos de datos

| Archivo | Propósito |
| --- | --- |
| `data/cards.json` | Colección inicial de 24 cartas jugables. |
| `data/rarities.json` | Definición de rarezas activas y pesos de aparición provisionales. |
| `data/card-taxonomy.json` | Enumeraciones de tipos, objetivos, timings, efectos y estados. |
| `data/object-classes.json` | Clases reutilizables para objetos, invocaciones, habilidades y estados. |
| `data/object-class-taxonomy.json` | Categorías y estados válidos para clases de objeto. |
| `data/objects.json` | Objetos o entidades derivados de cartas concretas. |
| `data/abilities.json` | Habilidades, estados y patrones de efecto derivados de las cartas. |

## Convenciones de identificadores

- `id` de carta: `card_` + `slug` normalizado.
- `slug`: minúsculas, sin tildes y con guiones bajos.
- `id` de clase: `class_` + `slug`.
- `id` de objeto: `object_` + `slug`.
- `id` de habilidad: `ability_` + `slug`.

## Estados y habilidades pendientes

Los siguientes elementos están modelados como `provisional` porque todavía requieren reglas finales:

- `depresion`: estado provocado por Redu. Debe decidirse si reduce daño, impide atacar, baja defensa, dura X turnos o combina varias opciones.
- `confusion`: estado provocado por Nach Scratch. Debe decidirse si cambia objetivos, añade probabilidad de fallo o provoca daño propio.
- `bet`: efecto aleatorio de Javi BET. Faltan resultados posibles y probabilidades.
- `no_me_la_merezco`: habilidad defensiva de Merino MiPINO. Actualmente se modela como reducción del siguiente daño recibido en un 50%; falta confirmar si también afecta a estados.

## Validación recomendada

Los esquemas JSON principales están en `schemas/`:

- `schemas/cards.schema.json`
- `schemas/object-classes.schema.json`
- `schemas/objects.schema.json`
- `schemas/abilities.schema.json`

Como comprobación mínima, todos los archivos `.json` deben poder parsearse como JSON válido. Para validación completa, se recomienda usar un validador compatible con JSON Schema draft 2020-12.
