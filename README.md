# FightingMates

Esqueleto inicial de la v1 del proyecto en Java.

## Estructura

- `src/main/java/fightingmates/Main.java`: arranque básico.
- `src/main/java/fightingmates/Juego.java`: ciclo principal de partida.
- `src/main/java/fightingmates/Jugador.java`: gestión de mano, descarte y vida.
- `src/main/java/fightingmates/Mazo.java`: mazo de 17 cartas con arrays.
- `src/main/java/fightingmates/Tablero.java`: campos de unidades por jugador.
- `src/main/java/fightingmates/Carta.java`: clase base abstracta.
- `src/main/java/fightingmates/Unidad.java`: carta de unidad.
- `src/main/java/fightingmates/Objeto.java`: carta de objeto.
- `src/main/java/fightingmates/Habilidad.java`: habilidad abstracta.
- `src/main/java/fightingmates/HabilidadDanio.java`
- `src/main/java/fightingmates/HabilidadCura.java`
- `src/main/java/fightingmates/HabilidadEstado.java`

## Compilación rápida

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out fightingmates.Main
```

## Carga de cartas desde JSON

Por defecto el juego carga el mazo desde `resources/cards/cards.json` al arrancar.

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out fightingmates.Main --list-cards
java -cp out fightingmates.Main
```

También puedes indicar otro archivo de cartas:

```bash
java -cp out fightingmates.Main --cards resources/cards/cards.json
```

El formato completo está documentado en `docs/data-design.md`.

## Prototipo JavaFX

La primera versión visual de la interfaz se encuentra en `fightingmates.viewfx.FightingMatesFxApp` y usa estilos CSS desde `src/main/resources/styles/fightingmates.css`.

```bash
mvn javafx:run
```

El prototipo crea una partida demo con dos mazos básicos para validar rápidamente tablero, mano, registro y acciones principales sin depender todavía de recursos gráficos externos.
