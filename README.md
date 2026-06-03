# FightingMates

FightingMates es un proyecto Java 17 con Maven. La interfaz gráfica actual está migrada a JavaFX y la lógica del juego se mantiene en clases de dominio sencillas para que el proyecto sea fácil de entender y defender.

## Requisitos

- Java 17.
- Maven 3.8 o superior.

## Ejecutar la interfaz JavaFX

```bash
mvn clean javafx:run
```

Por defecto se cargan las cartas desde:

```text
resources/cards/cards.json
```

También puedes indicar otro archivo de cartas:

```bash
mvn javafx:run -Djavafx.args="--cards resources/cards/cards.json"
```

## Comprobar compilación

```bash
mvn clean compile
```

## Ejecutar la versión de consola

La consola se mantiene como modo alternativo para probar la lógica sin interfaz gráfica:

```bash
mvn -q exec:java -Dexec.mainClass=fightingmates.Main -Dexec.args="--console"
```

También puedes listar las cartas cargadas:

```bash
mvn -q exec:java -Dexec.mainClass=fightingmates.Main -Dexec.args="--list-cards"
```

## Estructura principal

- `src/main/java/fightingmates/FightingMatesApp.java`: arranque principal JavaFX.
- `src/main/java/fightingmates/Main.java`: entrada alternativa de consola y carga inicial compartida.
- `src/main/java/fightingmates/controller/GameController.java`: fachada de acciones de partida usada por la interfaz.
- `src/main/java/fightingmates/view/MainGameView.java`: pantalla principal JavaFX.
- `src/main/java/fightingmates/view/CardView.java`: representación visual JavaFX de cartas y unidades.
- `src/main/java/fightingmates/view/AssetManager.java`: carga opcional de imágenes desde recursos.
- `src/main/resources/fightingmates/styles/game.css`: estilos de la interfaz JavaFX.
- `src/main/java/fightingmates/Juego.java`: estado global de partida y flujo de turnos.
- `src/main/java/fightingmates/Jugador.java`: vida, mano, mazo y descarte.
- `src/main/java/fightingmates/Mazo.java`: mazo de 17 cartas.
- `src/main/java/fightingmates/Tablero.java`: tablero de 3 huecos por jugador.
- `src/main/java/fightingmates/Carta.java`, `Unidad.java`, `Objeto.java`: modelo de cartas.
- `src/main/java/fightingmates/Habilidad*.java`: habilidades básicas implementadas.

## Reglas visibles en la interfaz

- Tablero de 3 huecos por jugador.
- Mazo de 17 cartas.
- Sin sistema de energía.
- No se permite atacar directamente al jugador durante la primera ronda.
- En el primer turno de cada jugador se permiten como máximo 2 despliegues.
- Después del primer turno se permite como máximo 1 despliegue por turno.
- Al finalizar el turno, el nuevo jugador roba 1 carta si su mano no está llena.
- Las acciones inválidas no se ejecutan y muestran el motivo en el registro.
- La partida puede abandonarse con el botón **Rendirse**.

## Recursos

Las imágenes opcionales de cartas deben colocarse en:

```text
src/main/resources/fightingmates/images/cards/
```

Se busca primero por `id` de carta y después por nombre normalizado. Si no existe imagen, la interfaz muestra un placeholder visual y la partida continúa sin errores.

El formato de datos está documentado en `docs/data-design.md`.
