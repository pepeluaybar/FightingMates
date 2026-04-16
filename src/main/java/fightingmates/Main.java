package fightingmates;

import java.util.Scanner;

/**
 * Punto de entrada de FightingMates v1.
 * Gestiona la interfaz de consola y el bucle principal de la partida.
 *
 * Cartas disponibles en v1:
 *   - Defecto      : Unidad ofensiva básica (ATK 3, VID 6). Habilidad: +1 daño extra al atacar.
 *   - Curador      : Unidad de soporte (ATK 1, VID 5). Habilidad: cura 3 HP a una unidad aliada.
 *   - Amuleto Fuerza: Objeto. Aplica x1.5 de daño a la unidad aliada sobre la que se usa.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    // ─── Punto de entrada ─────────────────────────────────────────────────────

    public static void main(String[] args) {
        imprimirBanner();

        String nombre1 = pedirNombre("Jugador 1");
        String nombre2 = pedirNombre("Jugador 2");

        Jugador j1 = new Jugador(nombre1, Jugador.VIDA_INICIAL, crearMazo());
        Jugador j2 = new Jugador(nombre2, Jugador.VIDA_INICIAL, crearMazo());

        Juego juego = new Juego(j1, j2);
        juego.iniciarPartida();

        println("\n¡Partida iniciada! Cada jugador empieza con "
                + Juego.MANO_INICIAL + " cartas en mano.");
        pausa();

        // ─── Bucle principal ──────────────────────────────────────────────────
        Jugador ganador = null;
        while (ganador == null) {
            Jugador actual = juego.getJugadorActual();
            Jugador rival  = juego.getJugadorRival(actual);

            imprimirCabeceraTurno(juego, actual, rival);

            // 1. Robar carta
            Carta robada = actual.robarCarta();
            if (robada != null)
                println("  Robas: " + cartaResumen(robada));
            else
                println("  (Mazo vacío, no robas carta)");

            // 2. Mostrar estado del tablero
            imprimirTablero(juego, actual, rival);

            // 3. Fase de juego (colocar unidades / usar objetos)
            faseJugar(juego, actual);

            // Mostrar tablero actualizado tras colocar cartas
            imprimirTablero(juego, actual, rival);

            // 4. Fase de ataque
            faseAtaque(juego, actual, rival);

            // 5. Limpiar unidades muertas
            juego.getTablero().limpiarUnidadesMuertas(actual);
            juego.getTablero().limpiarUnidadesMuertas(rival);

            // 6. Fin de turno
            juego.finalizarTurno();
            ganador = juego.comprobarGanador();

            if (ganador == null) pausa();
        }

        // ─── Fin de partida ───────────────────────────────────────────────────
        println("\n" + "=".repeat(52));
        println("  ¡¡¡ " + ganador.getNombre().toUpperCase() + " GANA LA PARTIDA !!!");
        println("=".repeat(52));
    }

    // ─── Fase de juego ────────────────────────────────────────────────────────

    private static void faseJugar(Juego juego, Jugador actual) {
        int maxUnidades  = actual.isPrimerTurno() ? 2 : 1;
        int unidadesColocadas = 0;
        boolean objetoUsado   = false;

        println("\n┌─ FASE DE JUEGO ──────────────────────────────┐");

        while (true) {
            imprimirMano(actual);
            println("  [0] Terminar fase de juego");

            int restanUnidades = maxUnidades - unidadesColocadas;
            String limites = "  Puedes colocar: " + restanUnidades + " unidad(es)"
                    + (objetoUsado ? "" : " | 1 objeto");
            println(limites);
            print("  Elige carta (número) → ");

            int opcion = leerEntero(0, actual.getNumCartasMano());
            if (opcion == 0) break;

            int indice = opcion - 1;
            Carta carta = actual.obtenerCartaMano(indice);
            if (carta == null) continue;

            if (carta instanceof Unidad) {
                if (unidadesColocadas >= maxUnidades) {
                    println("  ✗ Ya colocaste el máximo de unidades este turno.");
                    continue;
                }
                int pos = elegirPosicionLibre(juego.getTablero(), actual);
                if (pos == -1) continue; // campo lleno o cancelado
                if (actual.jugarUnidad(indice, pos)) {
                    println("  ✓ [" + carta.getNombre() + "] colocada en posición " + pos + ".");
                    unidadesColocadas++;
                }

            } else if (carta instanceof Objeto) {
                if (objetoUsado) {
                    println("  ✗ Ya usaste un objeto este turno.");
                    continue;
                }
                Objeto obj = (Objeto) carta;
                Unidad objetivo = elegirUnidadAliada(juego.getTablero(), actual, "sobre la que usar el objeto");
                if (objetivo == null) continue;
                if (actual.usarObjeto(indice, objetivo)) {
                    println("  ✓ [" + carta.getNombre() + "] aplicado sobre ["
                            + objetivo.getNombre() + "]. ¡Su daño es ahora x1.5!");
                    objetoUsado = true;
                }
            }
        }
        println("└──────────────────────────────────────────────┘");
    }

    // ─── Fase de ataque ───────────────────────────────────────────────────────

    private static void faseAtaque(Juego juego, Jugador actual, Jugador rival) {
        Unidad[] atacantes = juego.getTablero().obtenerUnidadesVivas(actual);

        println("\n┌─ FASE DE ATAQUE ─────────────────────────────┐");

        if (atacantes.length == 0) {
            println("  (No tienes unidades en campo para atacar)");
            println("└──────────────────────────────────────────────┘");
            return;
        }

        for (Unidad atacante : atacantes) {
            Unidad[] enemigas = juego.getTablero().obtenerUnidadesVivas(rival);
            Unidad[] aliadas  = juego.getTablero().obtenerUnidadesVivas(actual);
            boolean tieneHeal = atacante.getHabilidad() != null
                    && atacante.getHabilidad().esSoloAliados();

            println("\n  ► " + atacante + "");
            println("  Elige acción:");
            println("    [1] Atacar " + (enemigas.length > 0 ? "unidad enemiga" : "→ ataque directo al jugador"));
            if (tieneHeal) {
                println("    [2] Curar unidad aliada  (" + atacante.getHabilidad().getNombre() + ")");
            }
            println("    [0] Pasar");
            print("  Acción → ");

            int maxOp = tieneHeal ? 2 : 1;
            int accion = leerEntero(0, maxOp);

            if (accion == 0) {
                println("  (Pasa)");
                continue;
            }

            if (accion == 1) {
                realizarAtaque(juego, atacante, rival, enemigas);
            } else if (accion == 2 && tieneHeal) {
                realizarCuracion(atacante, aliadas, actual, rival);
            }
        }

        println("└──────────────────────────────────────────────┘");
    }

    /** Lógica de ataque normal: elige objetivo o ataca al jugador si no hay unidades. */
    private static void realizarAtaque(Juego juego, Unidad atacante, Jugador rival, Unidad[] enemigas) {
        if (enemigas.length == 0) {
            int danio = atacante.getAtaqueEfectivo();
            juego.atacarJugador(atacante, rival);
            println("  → Ataque directo a " + rival.getNombre()
                    + " (" + danio + " daño). Vida rival: " + rival.getVida());
        } else {
            println("  Unidades enemigas:");
            for (int i = 0; i < enemigas.length; i++) {
                println("    [" + (i + 1) + "] " + enemigas[i]);
            }
            print("  Elige objetivo → ");
            int idx = leerEntero(1, enemigas.length) - 1;
            Unidad objetivo = enemigas[idx];
            int danio = atacante.getAtaqueEfectivo();
            juego.ejecutarAtaque(atacante, objetivo);
            println("  → " + atacante.getNombre() + " ataca a " + objetivo.getNombre()
                    + " (" + danio + " daño). Vida restante: " + objetivo.getVida());
            if (!objetivo.estaViva())
                println("  ✦ ¡" + objetivo.getNombre() + " ha sido derrotada!");
        }
    }

    /** Lógica de curación: solo sobre unidades aliadas vivas (nunca enemigas ni jugador). */
    private static void realizarCuracion(Unidad curador, Unidad[] aliadas, Jugador propietario, Jugador rival) {
        if (aliadas.length == 0) {
            println("  (No hay unidades aliadas a las que curar)");
            return;
        }
        println("  Unidades aliadas que puedes curar:");
        for (int i = 0; i < aliadas.length; i++) {
            println("    [" + (i + 1) + "] " + aliadas[i]);
        }
        print("  Elige aliado → ");
        int idx = leerEntero(1, aliadas.length) - 1;
        Unidad objetivo = aliadas[idx];
        int vidaAntes = objetivo.getVida();
        curador.aplicarHabilidad(objetivo, propietario, rival);
        int curado = objetivo.getVida() - vidaAntes;
        println("  → " + curador.getNombre() + " cura a " + objetivo.getNombre()
                + " (+" + curado + " vida). Vida: " + objetivo.getVida() + "/" + objetivo.getVidaMaxima());
    }

    // ─── Presentación ─────────────────────────────────────────────────────────

    private static void imprimirBanner() {
        println("╔══════════════════════════════════════════════════╗");
        println("║          F I G H T I N G   M A T E S  v1        ║");
        println("╚══════════════════════════════════════════════════╝");
    }

    private static void imprimirCabeceraTurno(Juego juego, Jugador actual, Jugador rival) {
        println("\n" + "═".repeat(52));
        println("  TURNO " + (juego.getTurnosJugados() + 1)
                + (actual.isPrimerTurno() ? " ★ PRIMER TURNO (puedes colocar 2 unidades)" : "")
                + "");
        println("  ► " + actual.getNombre() + "  ♥ " + actual.getVida() + " HP"
                + "   vs   " + rival.getNombre() + "  ♥ " + rival.getVida() + " HP");
        println("═".repeat(52));
    }

    private static void imprimirTablero(Juego juego, Jugador actual, Jugador rival) {
        println("\n  ── Campo de " + rival.getNombre() + " ─────────────────────");
        imprimirCampo(juego.getTablero(), rival);
        println("  ── Campo de " + actual.getNombre() + " ─────────────────────");
        imprimirCampo(juego.getTablero(), actual);
    }

    private static void imprimirCampo(Tablero tablero, Jugador jugador) {
        Unidad[] campo = tablero.getCampo(jugador);
        if (campo == null) { println("  (sin campo)"); return; }
        StringBuilder sb = new StringBuilder("  ");
        for (int i = 0; i < campo.length; i++) {
            sb.append("[").append(i).append(":");
            if (campo[i] != null) {
                sb.append(campo[i].getNombre())
                  .append(" V:").append(campo[i].getVida())
                  .append("/").append(campo[i].getVidaMaxima());
                if (campo[i].getMultiplicadorAtaque() > 1.0f) sb.append("★");
            } else {
                sb.append("---");
            }
            sb.append("] ");
        }
        println(sb.toString().trim());
    }

    private static void imprimirMano(Jugador jugador) {
        println("  Tu mano (" + jugador.getNumCartasMano() + " cartas):");
        for (int i = 0; i < jugador.getNumCartasMano(); i++) {
            Carta c = jugador.obtenerCartaMano(i);
            println("    [" + (i + 1) + "] " + cartaResumen(c));
        }
    }

    private static String cartaResumen(Carta c) {
        if (c instanceof Unidad) {
            Unidad u = (Unidad) c;
            String hab = u.getHabilidad() != null ? " | Hab: " + u.getHabilidad().getNombre() : "";
            return u.getNombre() + " [Unidad ATK:" + u.getAtaque()
                    + " VID:" + u.getVidaMaxima() + hab + "]";
        } else if (c instanceof Objeto) {
            Objeto o = (Objeto) c;
            return o.getNombre() + " [Objeto: " + o.getDescripcion() + "]";
        }
        return c.getNombre();
    }

    // ─── Helpers de interacción ───────────────────────────────────────────────

    private static String pedirNombre(String etiqueta) {
        String nombre = "";
        while (nombre.isEmpty()) {
            print("Introduce el nombre del " + etiqueta + ": ");
            nombre = scanner.nextLine().trim();
            if (nombre.isEmpty()) println("  El nombre no puede estar vacío.");
        }
        return nombre;
    }

    private static int elegirPosicionLibre(Tablero tablero, Jugador jugador) {
        Unidad[] campo = tablero.getCampo(jugador);
        println("  Posiciones disponibles:");
        boolean hayLibre = false;
        for (int i = 0; i < campo.length; i++) {
            if (campo[i] == null) { println("    [" + i + "] libre"); hayLibre = true; }
            else println("    [" + i + "] " + campo[i].getNombre() + " (ocupada)");
        }
        if (!hayLibre) { println("  ✗ Campo lleno."); return -1; }
        print("  Elige posición → ");
        return leerEntero(0, campo.length - 1);
    }

    private static Unidad elegirUnidadAliada(Tablero tablero, Jugador jugador, String contexto) {
        Unidad[] vivas = tablero.obtenerUnidadesVivas(jugador);
        if (vivas.length == 0) { println("  ✗ No tienes unidades en campo."); return null; }
        println("  Elige unidad aliada " + contexto + ":");
        for (int i = 0; i < vivas.length; i++) println("    [" + (i + 1) + "] " + vivas[i]);
        print("  Objetivo → ");
        return vivas[leerEntero(1, vivas.length) - 1];
    }

    /** Lee un entero entre min y max inclusive, repitiendo hasta obtener entrada válida. */
    private static int leerEntero(int min, int max) {
        while (true) {
            try {
                String linea = scanner.nextLine().trim();
                int val = Integer.parseInt(linea);
                if (val >= min && val <= max) return val;
            } catch (NumberFormatException ignored) {}
            print("  Opción inválida. Elige entre " + min + " y " + max + " → ");
        }
    }

    private static void pausa() {
        println("\n  [Pulsa ENTER para continuar...]");
        scanner.nextLine();
    }

    private static void print(String msg)   { System.out.print(msg); }
    private static void println(String msg) { System.out.println(msg); }

    // ─── Fábrica de cartas ────────────────────────────────────────────────────

    /**
     * Crea el mazo estándar de v1 (17 cartas):
     *   8x Defecto | 5x Curador | 4x Amuleto de Fuerza
     */
    private static Mazo crearMazo() {
        Mazo mazo = new Mazo();
        for (int i = 1; i <= 8;  i++) mazo.anadirCarta(crearDefecto(i));
        for (int i = 9; i <= 13; i++) mazo.anadirCarta(crearCurador(i));
        for (int i = 14; i <= 17; i++) mazo.anadirCarta(crearAmuletoDeFuerza(i));
        return mazo;
    }

    /**
     * Defecto — unidad ofensiva básica.
     * ATK 3 | VID 6 | Habilidad: Arañazo (1 daño extra al atacar)
     */
    private static Unidad crearDefecto(int id) {
        return new Unidad(id, "Defecto", "Unidad de combate básica. Inflige daño extra al atacar.",
                3, 6,
                new HabilidadDanio("Arañazo", "Inflige 1 daño adicional al atacar", 1),
                "", 0, true);
    }

    /**
     * Curador — unidad de soporte.
     * ATK 1 | VID 5 | Habilidad: Vendas (cura 3 HP a una unidad aliada; NO puede curar enemigas ni al jugador)
     */
    private static Unidad crearCurador(int id) {
        return new Unidad(id, "Curador",
                "Unidad de soporte. Puede curar 3 HP a una unidad aliada (solo aliadas).",
                1, 5,
                new HabilidadCura("Vendas", "Restaura 3 HP a una unidad aliada", 3, true),
                "", 0, true);
    }

    /**
     * Amuleto de Fuerza — objeto de potenciación.
     * Al usarlo sobre una unidad aliada, su daño se multiplica por 1.5 permanentemente.
     */
    private static Objeto crearAmuletoDeFuerza(int id) {
        return new Objeto(id, "Amuleto de Fuerza",
                "Multiplica el daño de una unidad aliada por 1.5 de forma permanente.",
                "BONUS_ATAQUE", 0);
    }
}
