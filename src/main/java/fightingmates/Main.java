package fightingmates;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class Main {

    private static final String DEFAULT_CARDS_PATH = "resources/cards/cards.json";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        String rutaCartas = obtenerRutaCartas(args);
        boolean listarCartasYSalir = existeArgumento(args, "--list-cards");
        ArrayList<Carta> cartas = cargarCartasDesdeJson(rutaCartas);

        if (cartas.isEmpty()) {
            System.out.println("No se han podido cargar cartas. El juego no puede empezar.");
            return;
        }

        if (listarCartasYSalir) {
            listarCartas(cartas);
            return;
        }

        boolean salir = false;

        while (!salir) {
            mostrarMenuPrincipal();

            int opcion = leerEntero("Elige una opción: ");

            switch (opcion) {
                case 1:
                    listarCartas(cartas);
                    break;

                case 2:
                    iniciarNuevaPartida(cartas);
                    break;

                case 0:
                    salir = true;
                    System.out.println("Saliendo de FightingMates...");
                    break;

                default:
                    System.out.println("Opción no válida.");
                    break;
            }
        }
    }

    // =========================================================
    // MENÚ PRINCIPAL
    // =========================================================

    private static void mostrarMenuPrincipal() {
        System.out.println();
        System.out.println("====== FIGHTINGMATES ======");
        System.out.println("1. Listar cartas cargadas");
        System.out.println("2. Iniciar partida");
        System.out.println("0. Salir");
        System.out.println("===========================");
    }

    private static void listarCartas(ArrayList<Carta> cartas) {
        System.out.println();
        System.out.println("Cartas cargadas: " + cartas.size());

        for (int i = 0; i < cartas.size(); i++) {
            Carta carta = cartas.get(i);
            System.out.println(i + ". " + carta);
        }
    }

    // =========================================================
    // PARTIDA
    // =========================================================

    private static void iniciarNuevaPartida(ArrayList<Carta> cartasBase) {
        System.out.println();
        System.out.println("=== Nueva partida ===");

        String nombre1 = leerTexto("Nombre Jugador 1: ");
        if (nombre1.isEmpty()) {
            nombre1 = "Jugador 1";
        }

        String nombre2 = leerTexto("Nombre Jugador 2: ");
        if (nombre2.isEmpty()) {
            nombre2 = "Jugador 2";
        }

        Mazo mazo1 = crearMazo(cartasBase);
        Mazo mazo2 = crearMazo(cartasBase);

        Jugador jugador1 = new Jugador(nombre1, Jugador.VIDA_INICIAL, mazo1);
        Jugador jugador2 = new Jugador(nombre2, Jugador.VIDA_INICIAL, mazo2);

        Juego juego = new Juego(jugador1, jugador2);
        juego.iniciarPartida();

        ejecutarPartida(juego);
    }

    private static void ejecutarPartida(Juego juego) {
        while (true) {
            if (!ejecutarFaseDespliegue(juego, juego.getJugador1(), juego.getJugador2())) {
                return;
            }

            if (!ejecutarFaseDespliegue(juego, juego.getJugador2(), juego.getJugador1())) {
                return;
            }

            limpiarMuertas(juego);
            Jugador ganador = juego.comprobarGanador();
            if (ganador != null) {
                anunciarGanador(ganador);
                return;
            }

            if (!ejecutarFaseAccion(juego, juego.getJugador1(), juego.getJugador2())) {
                return;
            }

            if (!ejecutarFaseAccion(juego, juego.getJugador2(), juego.getJugador1())) {
                return;
            }

            limpiarMuertas(juego);
            ganador = juego.comprobarGanador();
            if (ganador != null) {
                anunciarGanador(ganador);
                return;
            }
        }
    }

    private static boolean ejecutarFaseDespliegue(Juego juego, Jugador actual, Jugador rival) {
        int unidadesJugadas = 0;
        boolean objetoUsado = false;

        robarAlInicioDelTurno(actual);

        boolean terminar = false;
        while (!terminar) {
            mostrarEstado(juego, actual, rival, "FASE DE DESPLIEGUE");
            mostrarMenuDespliegue(actual, unidadesJugadas, objetoUsado);

            int opcion = leerEntero("Elige acción: ");
            switch (opcion) {
                case 1:
                    mostrarMano(actual);
                    break;
                case 2:
                    if (!puedeJugarUnidad(actual, unidadesJugadas, objetoUsado)) {
                        System.out.println("No puedes jugar más unidades en esta fase.");
                    } else if (jugarUnidad(actual)) {
                        unidadesJugadas++;
                    }
                    break;
                case 3:
                    if (!puedeUsarObjeto(actual, unidadesJugadas, objetoUsado)) {
                        System.out.println("No puedes usar más objetos en esta fase.");
                    } else if (usarObjeto(actual, rival)) {
                        objetoUsado = true;
                        limpiarMuertas(juego);
                        Jugador ganador = juego.comprobarGanador();
                        if (ganador != null) {
                            anunciarGanador(ganador);
                            return false;
                        }
                    }
                    break;
                case 4:
                    terminar = true;
                    break;
                case 0:
                    System.out.println(actual.getNombre() + " se rinde.");
                    System.out.println("Gana " + rival.getNombre() + "!");
                    return false;
                default:
                    System.out.println("Opción no válida.");
                    break;
            }
        }

        actual.setPrimerTurno(false);
        return true;
    }

    private static boolean ejecutarFaseAccion(Juego juego, Jugador actual, Jugador rival) {
        boolean accionCompletada = false;

        while (!accionCompletada) {
            mostrarEstado(juego, actual, rival, "FASE DE ACCIÓN");
            mostrarMenuAccion();

            int opcion = leerEntero("Elige acción: ");
            switch (opcion) {
                case 1:
                    if (atacar(juego, actual, rival)) {
                        accionCompletada = true;
                    }
                    break;
                case 2:
                    if (usarHabilidadDeCura(actual, rival)) {
                        accionCompletada = true;
                    }
                    break;
                case 3:
                    System.out.println(actual.getNombre() + " pasa su acción.");
                    accionCompletada = true;
                    break;
                case 0:
                    System.out.println(actual.getNombre() + " se rinde.");
                    System.out.println("Gana " + rival.getNombre() + "!");
                    return false;
                default:
                    System.out.println("Opción no válida.");
                    break;
            }
        }

        return true;
    }

    private static void mostrarEstado(Juego juego, Jugador actual, Jugador rival, String fase) {
        System.out.println();
        System.out.println("==================================");
        System.out.println(fase + " - " + actual.getNombre());
        System.out.println(actual.getNombre() + " vida: " + actual.getVida());
        System.out.println(rival.getNombre() + " vida: " + rival.getVida());
        System.out.println("==================================");
        mostrarTablero(juego);
    }

    private static void mostrarMenuDespliegue(Jugador jugador, int unidadesJugadas, boolean objetoUsado) {
        System.out.println();
        System.out.println("1. Ver mano");
        System.out.println("2. Jugar unidad");
        System.out.println("3. Usar objeto");
        System.out.println("4. Terminar despliegue");
        System.out.println("0. Rendirse");
        System.out.println("Acciones usadas: unidades " + unidadesJugadas + "/"
                + maximoUnidadesPorTurno(jugador)
                + ", objeto " + textoSiNo(objetoUsado));
    }

    private static void mostrarMenuAccion() {
        System.out.println();
        System.out.println("1. Atacar");
        System.out.println("2. Usar habilidad de cura");
        System.out.println("3. Pasar turno");
        System.out.println("0. Rendirse");
    }

    private static void anunciarGanador(Jugador ganador) {
        System.out.println();
        System.out.println("Ha ganado " + ganador.getNombre() + "!");
    }

    private static void robarAlInicioDelTurno(Jugador jugador) {
        System.out.println();
        System.out.println("Inicio del turno de " + jugador.getNombre() + ".");

        if (jugador.getNumCartasMano() >= Jugador.MANO_MAXIMA) {
            System.out.println("La mano está llena (" + Jugador.MANO_MAXIMA + " cartas). No roba carta.");
            return;
        }

        Carta cartaRobada = jugador.robarCarta();

        if (cartaRobada == null) {
            System.out.println("No roba carta porque el mazo está vacío.");
        } else {
            System.out.println(jugador.getNombre() + " roba: " + cartaRobada.getNombre());
        }
    }

    private static int maximoUnidadesPorTurno(Jugador jugador) {
        if (jugador.esPrimerTurno()) {
            return 2;
        }

        return 1;
    }

    private static boolean puedeJugarUnidad(Jugador jugador, int unidadesJugadas, boolean objetoUsado) {
        if (jugador.esPrimerTurno()) {
            return unidadesJugadas < 2;
        }

        return unidadesJugadas == 0 && !objetoUsado;
    }

    private static boolean puedeUsarObjeto(Jugador jugador, int unidadesJugadas, boolean objetoUsado) {
        if (jugador.esPrimerTurno()) {
            return !objetoUsado;
        }

        return unidadesJugadas == 0 && !objetoUsado;
    }

    private static String textoSiNo(boolean valor) {
        if (valor) {
            return "sí";
        }

        return "no";
    }

    // =========================================================
    // ACCIONES DE TURNO
    // =========================================================

    private static void mostrarMano(Jugador jugador) {
        System.out.println();
        System.out.println("Mano de " + jugador.getNombre() + ":");

        if (jugador.getNumCartasMano() == 0) {
            System.out.println("No tiene cartas en la mano.");
            return;
        }

        for (int i = 0; i < jugador.getNumCartasMano(); i++) {
            System.out.println(i + ". " + jugador.obtenerCartaMano(i));
        }
    }

    private static boolean jugarUnidad(Jugador jugador) {
        mostrarMano(jugador);

        int indiceCarta = leerEntero("Índice de la carta unidad: ");
        Carta carta = jugador.obtenerCartaMano(indiceCarta);

        if (carta == null) {
            System.out.println("Índice de carta no válido.");
            return false;
        }

        if (!(carta instanceof Unidad)) {
            System.out.println("Esa carta no es una unidad.");
            return false;
        }

        int posicion = leerEntero("Posición del tablero entre 0 y " + (Tablero.TAMANIO_CAMPO - 1) + ": ");

        if (!jugador.getTablero().esPosicionValida(posicion)) {
            System.out.println("Posición no válida.");
            return false;
        }

        if (jugador.getTablero().obtenerUnidad(jugador, posicion) != null) {
            System.out.println("Esa posición ya está ocupada.");
            return false;
        }

        boolean colocada = jugador.jugarUnidad(indiceCarta, posicion);

        if (colocada) {
            System.out.println("Unidad colocada correctamente.");
        } else {
            System.out.println("No se pudo colocar la unidad.");
        }

        return colocada;
    }

    private static boolean usarObjeto(Jugador actual, Jugador rival) {
        mostrarMano(actual);

        int indiceCarta = leerEntero("Índice del objeto: ");
        Carta carta = actual.obtenerCartaMano(indiceCarta);

        if (carta == null) {
            System.out.println("Índice de carta no válido.");
            return false;
        }

        if (!(carta instanceof Objeto)) {
            System.out.println("Esa carta no es un objeto.");
            return false;
        }

        Objeto objeto = (Objeto) carta;
        Unidad objetivo = null;

        String tipo = objeto.getTipoEfecto() != null ? objeto.getTipoEfecto().toUpperCase(Locale.ROOT) : "";

        switch (tipo) {
            case "DANIO_JUGADOR":
                objetivo = null;
                break;
            case "DANIO":
                objetivo = elegirUnidadViva(rival, "Elige una unidad enemiga:");
                break;
            case "CURA":
            case "BONUS_ATAQUE":
                objetivo = elegirUnidadViva(actual, "Elige una unidad aliada:");
                break;
            default:
                System.out.println("Tipo de objeto no soportado: " + objeto.getTipoEfecto());
                return false;
        }

        if (objetivo == null && !"DANIO_JUGADOR".equals(tipo)) {
            System.out.println("Objetivo no válido para ese objeto.");
            return false;
        }

        boolean usado = actual.usarObjeto(indiceCarta, objetivo);

        if (usado) {
            System.out.println("Objeto usado correctamente.");
        } else {
            System.out.println("No se pudo usar el objeto.");
        }

        return usado;
    }

    private static boolean atacar(Juego juego, Jugador actual, Jugador rival) {
        Unidad atacante = elegirUnidadViva(actual, "Elige tu unidad atacante:");

        if (atacante == null) {
            System.out.println("No tienes unidades para atacar.");
            return false;
        }

        if (!atacante.esActiva()) {
            System.out.println("Esa unidad ya ha actuado este turno.");
            return false;
        }

        if (juego.getTablero().hayUnidadesVivas(rival)) {
            Unidad objetivo = elegirUnidadViva(rival, "Elige unidad enemiga objetivo:");

            if (objetivo == null) {
                System.out.println("No hay objetivo válido.");
                return false;
            }

            juego.ejecutarAtaque(atacante, objetivo);
            System.out.println(atacante.getNombre() + " atacó a " + objetivo.getNombre() + ".");
        } else {
            juego.atacarJugador(atacante, rival);
            System.out.println(atacante.getNombre() + " atacó directamente a " + rival.getNombre() + ".");
        }

        return true;
    }

    private static boolean usarHabilidadDeCura(Jugador actual, Jugador rival) {
        Unidad origen = elegirUnidadViva(actual, "Elige la unidad que tiene habilidad de cura:");

        if (origen == null) {
            System.out.println("No hay unidad válida.");
            return false;
        }

        if (!origen.esActiva()) {
            System.out.println("Esa unidad ya ha actuado este turno.");
            return false;
        }

        if (origen.getHabilidad() == null || !origen.getHabilidad().esSoloAliados()) {
            System.out.println("Esa unidad no tiene una habilidad de cura para aliados.");
            return false;
        }

        Unidad objetivo = elegirUnidadViva(actual, "Elige la unidad aliada a curar:");

        if (objetivo == null) {
            System.out.println("No hay objetivo válido.");
            return false;
        }

        boolean habilidadUsada = origen.usarHabilidadManual(objetivo, actual, rival);

        if (habilidadUsada) {
            System.out.println(origen.getNombre() + " ha usado su habilidad sobre " + objetivo.getNombre() + ".");
        } else {
            System.out.println("No se pudo usar la habilidad.");
        }

        return habilidadUsada;
    }

    // =========================================================
    // TABLERO
    // =========================================================

    private static void mostrarTablero(Juego juego) {
        System.out.println();
        System.out.println(juego.getTablero());
    }

    private static Unidad elegirUnidadViva(Jugador jugador, String mensaje) {
        Unidad[] unidades = jugador.getTablero().obtenerUnidadesVivas(jugador);

        if (unidades.length == 0) {
            return null;
        }

        System.out.println();
        System.out.println(mensaje);

        for (int i = 0; i < unidades.length; i++) {
            System.out.println(i + ". " + unidades[i]);
        }

        int indice = leerEntero("Elige unidad: ");

        if (indice < 0 || indice >= unidades.length) {
            return null;
        }

        return unidades[indice];
    }

    private static void limpiarMuertas(Juego juego) {
        juego.getTablero().limpiarUnidadesMuertas(juego.getJugador1());
        juego.getTablero().limpiarUnidadesMuertas(juego.getJugador2());
    }

    // =========================================================
    // CARGA DE CARTAS DESDE JSON EN EL MAIN
    // =========================================================

    private static String obtenerRutaCartas(String[] args) {
        String ruta = DEFAULT_CARDS_PATH;

        for (int i = 0; i < args.length; i++) {
            if ("--cards".equals(args[i])) {
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    ruta = args[i + 1];
                } else {
                    System.out.println("No se indicó ruta después de --cards. Se usará el archivo por defecto.");
                }
            }
        }

        return ruta;
    }

    private static boolean existeArgumento(String[] args, String argumentoBuscado) {
        for (int i = 0; i < args.length; i++) {
            if (argumentoBuscado.equals(args[i])) {
                return true;
            }
        }

        return false;
    }

    private static ArrayList<Carta> cargarCartasDesdeJson(String ruta) {
        ArrayList<Carta> cartas = new ArrayList<>();

        File archivo = new File(ruta);

        if (!archivo.exists()) {
            System.out.println("Archivo de cartas no encontrado: " + ruta);
            return cartas;
        }

        try (FileReader reader = new FileReader(archivo, StandardCharsets.UTF_8)) {
            CartaJson[] datosCartas = new Gson().fromJson(reader, CartaJson[].class);

            if (datosCartas == null) {
                System.out.println("El JSON debe contener una lista de cartas.");
                return cartas;
            }

            int id = 1;

            for (int i = 0; i < datosCartas.length; i++) {
                CartaJson datos = datosCartas[i];

                if (datos == null) {
                    System.out.println("Carta #" + (i + 1) + " ignorada porque está vacía.");
                    continue;
                }

                int copias = datos.getCopies() != null ? Math.max(1, datos.getCopies()) : 1;

                for (int c = 0; c < copias; c++) {
                    Carta carta = crearCartaDesdeJson(datos, id);

                    if (carta != null) {
                        cartas.add(carta);
                        id++;
                    }
                }
            }

            System.out.println("Cartas cargadas correctamente: " + cartas.size());

        } catch (JsonSyntaxException e) {
            System.out.println("El JSON está mal escrito: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error leyendo cartas: " + e.getMessage());
        }

        return cartas;
    }

    private static Carta crearCartaDesdeJson(CartaJson datos, int id) {
        String nombre = texto(datos.getName());
        String rareza = texto(datos.getRarity());
        String tipo = texto(datos.getType());
        String objetivo = texto(datos.getTarget());
        String timing = texto(datos.getTiming());
        String descripcion = texto(datos.getDescription());

        if (nombre.isEmpty() || rareza.isEmpty() || tipo.isEmpty() || objetivo.isEmpty()
                || timing.isEmpty() || descripcion.isEmpty()) {
            System.out.println("Carta ignorada por tener campos obligatorios vacíos.");
            return null;
        }

        ArrayList<Effect> efectos = convertirEfectos(datos.getEffects());

        String claseCarta = texto(datos.getCardClass());

        if (claseCarta.isEmpty()) {
            claseCarta = texto(datos.getClazz());
        }

        if (claseCarta.isEmpty()) {
            claseCarta = "unit";
        }

        Carta carta;

        if (esObjeto(claseCarta)) {
            carta = crearObjeto(id, nombre, descripcion, efectos);
        } else {
            carta = crearUnidad(id, nombre, descripcion, datos, efectos);
        }

        carta.setRareza(rareza);
        carta.setTipo(tipo);
        carta.setObjetivo(objetivo);
        carta.setMomento(timing);
        carta.setEfectos(efectos);

        return carta;
    }

    private static Unidad crearUnidad(
            int id,
            String nombre,
            String descripcion,
            CartaJson datos,
            ArrayList<Effect> efectos
    ) {
        int ataque = obtenerAtaque(datos);
        int vida = obtenerVida(datos);
        Habilidad habilidad = crearHabilidad(nombre, efectos);

        return new Unidad(id, nombre, descripcion, ataque, vida, habilidad, "", 0, true);
    }

    private static Objeto crearObjeto(
            int id,
            String nombre,
            String descripcion,
            ArrayList<Effect> efectos
    ) {
        Effect efectoPrincipal;

        if (efectos.isEmpty()) {
            efectoPrincipal = new Effect();
        } else {
            efectoPrincipal = efectos.get(0);
        }

        String tipoEfecto = convertirTipoObjeto(efectoPrincipal.getType());
        int valor = efectoPrincipal.getValue();

        return new Objeto(id, nombre, descripcion, tipoEfecto, valor);
    }

    private static Habilidad crearHabilidad(String nombreCarta, ArrayList<Effect> efectos) {
        if (efectos.isEmpty()) {
            return null;
        }

        Effect efecto = efectos.get(0);
        String tipo = normalizar(efecto.getType());

        String descripcion = efecto.getDescription();

        if (descripcion.isEmpty()) {
            descripcion = "Efecto de " + nombreCarta;
        }

        String nombreHabilidad = extraerNombreHabilidad(descripcion, nombreCarta);

        switch (tipo) {
            case "damage":
            case "damage_percent_max_hp":
                return new HabilidadDanio(nombreHabilidad, descripcion, efecto.getValue());

            case "heal":
            case "heal_percent_max_hp":
                return new HabilidadCura(
                        nombreHabilidad,
                        descripcion,
                        efecto.getValue(),
                        esObjetivoAliado(efecto.getTarget())
                );

            case "status":
            case "apply_status":
                String estado = efecto.getTextValue();

                if (estado.isEmpty()) {
                    estado = nombreHabilidad;
                }

                return new HabilidadEstado(nombreHabilidad, descripcion, estado, Math.max(1, efecto.getValue()));

            default:
                return null;
        }
    }

    private static ArrayList<Effect> convertirEfectos(ArrayList<EffectJson> efectosJson) {
        ArrayList<Effect> efectos = new ArrayList<>();

        if (efectosJson == null) {
            return efectos;
        }

        for (int i = 0; i < efectosJson.size(); i++) {
            EffectJson datos = efectosJson.get(i);

            if (datos == null) {
                continue;
            }

            String tipo = texto(datos.getType());
            String objetivo = texto(datos.getTarget());
            String descripcion = texto(datos.getDescription());
            int valor = datos.getValue() != null ? datos.getValue() : 0;

            String valorTexto = texto(datos.getTextValue());

            if (valorTexto.isEmpty()) {
                valorTexto = texto(datos.getStatus());
            }

            efectos.add(new Effect(tipo, objetivo, valor, valorTexto, descripcion));
        }

        return efectos;
    }

    private static int obtenerAtaque(CartaJson datos) {
        if (datos.getStats() != null && datos.getStats().getAttack() != null) {
            return datos.getStats().getAttack();
        }

        if (datos.getAttack() != null) {
            return datos.getAttack();
        }

        return 1;
    }

    private static int obtenerVida(CartaJson datos) {
        if (datos.getStats() != null && datos.getStats().getHealth() != null) {
            return datos.getStats().getHealth();
        }

        if (datos.getHealth() != null) {
            return datos.getHealth();
        }

        return 5;
    }

    private static String convertirTipoObjeto(String tipoEfecto) {
        switch (normalizar(tipoEfecto)) {
            case "damage":
                return "DANIO";

            case "heal":
                return "CURA";

            case "player_damage":
                return "DANIO_JUGADOR";

            case "bonus_attack":
            case "buff_damage":
                return "BONUS_ATAQUE";

            default:
                return normalizar(tipoEfecto).toUpperCase(Locale.ROOT);
        }
    }

    // =========================================================
    // MAZOS
    // =========================================================

    private static Mazo crearMazo(ArrayList<Carta> cartasBase) {
        Mazo mazo = new Mazo();

        for (int i = 0; i < cartasBase.size() && !mazo.estaLleno(); i++) {
            mazo.anadirCarta(copiarCarta(cartasBase.get(i)));
        }

        return mazo;
    }

    private static Carta copiarCarta(Carta carta) {
        if (carta instanceof Unidad) {
            return new Unidad((Unidad) carta);
        }

        if (carta instanceof Objeto) {
            return new Objeto((Objeto) carta);
        }

        return null;
    }

    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        if (!scanner.hasNextLine()) {
            return "";
        }

        return scanner.nextLine().trim();
    }

    private static int leerEntero(String mensaje) {
        System.out.print(mensaje);

        if (!scanner.hasNextLine()) {
            return 0;
        }

        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static boolean esObjeto(String claseCarta) {
        String clase = normalizar(claseCarta);

        return clase.equals("object") || clase.equals("objeto") || clase.equals("item");
    }

    private static boolean esObjetivoAliado(String objetivo) {
        String target = normalizar(objetivo);

        return target.equals("ally")
                || target.equals("all_allies")
                || target.equals("self")
                || target.equals("aliado")
                || target.equals("jugador");
    }

    private static String extraerNombreHabilidad(String descripcion, String nombreCarta) {
        int posicionDosPuntos = descripcion.indexOf(':');

        if (posicionDosPuntos > 0) {
            return descripcion.substring(0, posicionDosPuntos).trim();
        }

        return nombreCarta;
    }

    private static String texto(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.trim();
    }

    private static String normalizar(String valor) {
        return texto(valor).toLowerCase(Locale.ROOT);
    }
}
