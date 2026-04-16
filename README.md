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
