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

## Carga de cartas en Java

El juego de consola carga sus cartas iniciales desde `resources/cards/cards.json` mediante `JsonCardLoader`, invocado desde `Main` al arrancar.

### Formato runtime de `resources/cards/cards.json`

El archivo debe contener una lista JSON. Cada carta acepta estos campos:

| Campo | Obligatorio | Descripción |
| --- | --- | --- |
| `name` | Sí | Nombre visible de la carta. No puede estar vacío. |
| `rarity` | Sí | Rareza visible o de diseño, por ejemplo `perro` u `oca`. No puede estar vacía. |
| `type` | Sí | Tipo visible de carta, por ejemplo `Personaje` u `Objeto`. No puede estar vacío. |
| `cardClass` | No | Clase ejecutable para el motor: `unit`/`personaje` o `object`/`objeto`. Si falta, se asume `unit`. |
| `target` | Sí | Objetivo principal del efecto, por ejemplo `enemy`, `ally` o `self`. |
| `timing` | Sí | Momento de uso, por ejemplo `action` o `passive`. |
| `description` | Sí | Texto visible para el jugador. Debe existir y no estar vacío. |
| `copies` | No | Número de copias que se expanden al cargar. Si falta, vale `1`. |
| `stats.attack` | No | Ataque de una unidad. Si falta, vale `1`. |
| `stats.health` | No | Vida máxima de una unidad. Si falta, vale `5`. |
| `effects` | Sí | Lista de efectos. Puede estar vacía, pero nunca ser `null`. |

Efectos ejecutables por el motor v1:

- `damage`: se transforma en `HabilidadDanio` en unidades o `DANIO` en objetos.
- `heal`: se transforma en `HabilidadCura` en unidades o `CURA` en objetos.
- `status` / `apply_status`: se transforma en `HabilidadEstado` en unidades.
- `bonus_attack`: se transforma en `BONUS_ATAQUE` para objetos.
- `player_damage`: se transforma en `DANIO_JUGADOR` para objetos.

El loader también reconoce los efectos del diseño de datos (`buff_damage`, `reduce_damage`, `summon`, etc.) para poder conservarlos como metadatos, aunque la v1 del motor no ejecute todos todavía.

### Ejemplo mínimo

```json
[
  {
    "name": "Pepelu Delegado",
    "rarity": "oca",
    "type": "Personaje",
    "cardClass": "unit",
    "target": "enemy",
    "timing": "action",
    "description": "Carta especial de Pepelu.",
    "stats": {
      "attack": 2,
      "health": 6
    },
    "effects": [
      {
        "type": "damage",
        "target": "enemy",
        "value": 2,
        "description": "Hace 2 puntos de daño."
      }
    ]
  }
]
```

### Cómo añadir nuevas cartas

1. Edita `resources/cards/cards.json`.
2. Añade un nuevo objeto a la lista con los campos obligatorios.
3. Usa `cardClass: "unit"` para cartas que entran al tablero como `Unidad` o `cardClass: "object"` para objetos que se consumen desde la mano.
4. Añade `stats.attack` y `stats.health` si es una unidad.
5. Añade uno o varios efectos en `effects`; si todavía no tienen ejecución real, quedarán guardados como metadatos de la carta.
6. Ejecuta `java -cp out fightingmates.Main --list-cards` tras compilar para comprobar que el archivo carga correctamente.

### Errores controlados

`Main` captura los errores de carga y muestra mensajes claros para:

- archivo no encontrado;
- JSON mal formado;
- campos obligatorios vacíos;
- `effects` ausente o `null`;
- efecto desconocido.

Si la carga falla, el juego usa un mazo interno de emergencia para no terminar con una excepción sin explicación.
=======
