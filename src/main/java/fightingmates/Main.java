package fightingmates;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class Main {

    private static final String DEFAULT_CARDS_PATH = "resources/cards/cards.json";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        String rutaCartas = obtenerRutaCartas(args);
        ArrayList<Carta> cartas = cargarCartasDesdeJson(rutaCartas);

        if (cartas.isEmpty()) {
            System.out.println("No se han podido cargar cartas. El juego no puede empezar.");
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
        boolean partidaTerminada = false;

        while (!partidaTerminada) {
            Jugador ganador = juego.comprobarGanador();

            if (ganador != null) {
                System.out.println();
                System.out.println("Ha ganado " + ganador.getNombre() + "!");
                return;
            }

            Jugador actual = juego.getJugadorActual();
            Jugador rival = juego.getJugadorRival(actual);

            System.out.println();
            System.out.println("==================================");
            System.out.println("Turno de: " + actual.getNombre());
            System.out.println(actual.getNombre() + " vida: " + actual.getVida());
            System.out.println(rival.getNombre() + " vida: " + rival.getVida());
            System.out.println("==================================");

            mostrarTablero(juego);
            mostrarMenuTurno();

            int opcion = leerEntero("Elige acción: ");

            switch (opcion) {
                case 1:
                    mostrarMano(actual);
                    break;

                case 2:
                    jugarUnidad(actual);
                    break;

                case 3:
                    usarObjeto(actual, rival);
                    limpiarMuertas(juego);
                    break;

                case 4:
                    atacar(juego, actual, rival);
                    limpiarMuertas(juego);
                    break;

                case 5:
                    usarHabilidadDeCura(actual, rival);
                    break;

                case 6:
                    actual.robarCarta();
                    System.out.println(actual.getNombre() + " roba una carta.");
                    break;

                case 7:
                    juego.finalizarTurno();
                    break;

                case 0:
                    System.out.println(actual.getNombre() + " se rinde.");
                    System.out.println("Gana " + rival.getNombre() + "!");
                    partidaTerminada = true;
                    break;

                default:
                    System.out.println("Opción no válida.");
                    break;
            }
        }
    }

    private static void mostrarMenuTurno() {
        System.out.println();
        System.out.println("1. Ver mano");
        System.out.println("2. Jugar unidad");
        System.out.println("3. Usar objeto");
        System.out.println("4. Atacar");
        System.out.println("5. Usar habilidad de cura");
        System.out.println("6. Robar carta");
        System.out.println("7. Pasar turno");
        System.out.println("0. Rendirse");
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

    private static void jugarUnidad(Jugador jugador) {
        mostrarMano(jugador);

        int indiceCarta = leerEntero("Índice de la carta unidad: ");
        int posicion = leerEntero("Posición del tablero entre 0 y 4: ");

        boolean colocada = jugador.jugarUnidad(indiceCarta, posicion);

        if (colocada) {
            System.out.println("Unidad colocada correctamente.");
        } else {
            System.out.println("No se pudo colocar la unidad.");
        }
    }

    private static void usarObjeto(Jugador actual, Jugador rival) {
        if (actual.haUsadoObjetoEsteTurno()) {
            System.out.println("Ya has usado un objeto este turno.");
            return;
        }

        mostrarMano(actual);

        int indiceCarta = leerEntero("Índice del objeto: ");
        Carta carta = actual.obtenerCartaMano(indiceCarta);

        if (!(carta instanceof Objeto)) {
            System.out.println("Esa carta no es un objeto.");
            return;
        }

        Objeto objeto = (Objeto) carta;
        Unidad objetivo = null;

        if ("DANIO_JUGADOR".equalsIgnoreCase(objeto.getTipoEfecto())) {
            objetivo = null;
        } else if ("DANIO".equalsIgnoreCase(objeto.getTipoEfecto())) {
            objetivo = elegirUnidadViva(rival, "Elige una unidad enemiga:");
        } else {
            objetivo = elegirUnidadViva(actual, "Elige una unidad aliada:");
        }

        boolean usado = actual.usarObjeto(indiceCarta, objetivo);

        if (usado) {
            System.out.println("Objeto usado correctamente.");
        } else {
            System.out.println("No se pudo usar el objeto.");
        }
    }

    private static void atacar(Juego juego, Jugador actual, Jugador rival) {
        Unidad atacante = elegirUnidadViva(actual, "Elige tu unidad atacante:");

        if (atacante == null) {
            System.out.println("No tienes unidades para atacar.");
            return;
        }

        if (!atacante.esActiva()) {
            System.out.println("Esa unidad ya ha actuado este turno.");
            return;
        }

        if (juego.getTablero().hayUnidadesVivas(rival)) {
            Unidad objetivo = elegirUnidadViva(rival, "Elige unidad enemiga objetivo:");

            if (objetivo == null) {
                System.out.println("No hay objetivo válido.");
                return;
            }

            juego.ejecutarAtaque(atacante, objetivo);
            System.out.println(atacante.getNombre() + " atacó a " + objetivo.getNombre() + ".");
        } else {
            juego.atacarJugador(atacante, rival);
            System.out.println(atacante.getNombre() + " atacó directamente a " + rival.getNombre() + ".");
        }
    }

    private static void usarHabilidadDeCura(Jugador actual, Jugador rival) {
        Unidad origen = elegirUnidadViva(actual, "Elige la unidad que tiene habilidad de cura:");

        if (origen == null) {
            System.out.println("No hay unidad válida.");
            return;
        }

        if (!origen.esActiva()) {
            System.out.println("Esa unidad ya ha actuado este turno.");
            return;
        }

        if (origen.getHabilidad() == null || !origen.getHabilidad().esSoloAliados()) {
            System.out.println("Esa unidad no tiene una habilidad de cura para aliados.");
            return;
        }

        Unidad objetivo = elegirUnidadViva(actual, "Elige la unidad aliada a curar:");

        if (objetivo == null) {
            System.out.println("No hay objetivo válido.");
            return;
        }

        boolean habilidadUsada = origen.usarHabilidadManual(objetivo, actual, rival);

        if (habilidadUsada) {
            System.out.println(origen.getNombre() + " ha usado su habilidad sobre " + objetivo.getNombre() + ".");
        } else {
            System.out.println("No se pudo usar la habilidad.");
        }
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

        for (int i = 0; i < args.length - 1; i++) {
            if ("--cards".equals(args[i])) {
                ruta = args[i + 1];
            }
        }

        return ruta;
    }

    private static ArrayList<Carta> cargarCartasDesdeJson(String ruta) {
        ArrayList<Carta> cartas = new ArrayList<>();

        File archivo = new File(ruta);

        if (!archivo.exists()) {
            System.out.println("Archivo de cartas no encontrado: " + ruta);
            return cartas;
        }

        try (FileReader reader = new FileReader(archivo)) {
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

                int copias = datos.getCopias() != null ? Math.max(1, datos.getCopias()) : 1;

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
        String nombre = texto(datos.getNombre());
        String rareza = texto(datos.getRareza());
        String tipo = texto(datos.getTipo());
        String objetivo = texto(datos.getObjetivo());
        String momento = texto(datos.getMomento());
        String descripcion = texto(datos.getDescripcion());

        if (nombre.isEmpty() || rareza.isEmpty() || tipo.isEmpty() || objetivo.isEmpty()
                || momento.isEmpty() || descripcion.isEmpty()) {
            System.out.println("Carta ignorada por tener campos obligatorios vacíos.");
            return null;
        }

        ArrayList<Efecto> efectos = convertirEfectos(datos.getEfectos());

        String claseCarta = texto(datos.getClaseCarta());

        if (claseCarta.isEmpty()) {
            claseCarta = texto(datos.getClase());
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
        carta.setMomento(momento);
        carta.setEfectos(efectos);

        return carta;
    }

    private static Unidad crearUnidad(
            int id,
            String nombre,
            String descripcion,
            CartaJson datos,
            ArrayList<Efecto> efectos
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
            ArrayList<Efecto> efectos
    ) {
        Efecto efectoPrincipal;

        if (efectos.isEmpty()) {
            efectoPrincipal = new Efecto();
        } else {
            efectoPrincipal = efectos.get(0);
        }

        String tipoEfecto = convertirTipoObjeto(efectoPrincipal.getTipo());
        int valor = efectoPrincipal.getValor();

        return new Objeto(id, nombre, descripcion, tipoEfecto, valor);
    }

    private static Habilidad crearHabilidad(String nombreCarta, ArrayList<Efecto> efectos) {
        if (efectos.isEmpty()) {
            return null;
        }

        Efecto efecto = efectos.get(0);
        String tipo = normalizar(efecto.getTipo());

        String descripcion = efecto.getDescripcion();

        if (descripcion.isEmpty()) {
            descripcion = "Efecto de " + nombreCarta;
        }

        String nombreHabilidad = extraerNombreHabilidad(descripcion, nombreCarta);

        switch (tipo) {
            case "damage":
            case "damage_percent_max_hp":
                return new HabilidadDanio(nombreHabilidad, descripcion, efecto.getValor());

            case "heal":
            case "heal_percent_max_hp":
                return new HabilidadCura(
                        nombreHabilidad,
                        descripcion,
                        efecto.getValor(),
                        esObjetivoAliado(efecto.getObjetivo())
                );

            case "status":
            case "apply_status":
                String estado = efecto.getValorTexto();

                if (estado.isEmpty()) {
                    estado = nombreHabilidad;
                }

                return new HabilidadEstado(nombreHabilidad, descripcion, estado, Math.max(1, efecto.getValor()));

            default:
                return null;
        }
    }

    private static ArrayList<Efecto> convertirEfectos(ArrayList<EfectoJson> efectosJson) {
        ArrayList<Efecto> efectos = new ArrayList<>();

        if (efectosJson == null) {
            return efectos;
        }

        for (int i = 0; i < efectosJson.size(); i++) {
            EfectoJson datos = efectosJson.get(i);

            String tipo = texto(datos.getTipo());
            String objetivo = texto(datos.getObjetivo());
            String descripcion = texto(datos.getDescripcion());
            int valor = datos.getValor() != null ? datos.getValor() : 0;

            String valorTexto = texto(datos.getValorTexto());

            if (valorTexto.isEmpty()) {
                valorTexto = texto(datos.getEstado());
            }

            efectos.add(new Efecto(tipo, objetivo, valor, valorTexto, descripcion));
        }

        return efectos;
    }

    private static int obtenerAtaque(CartaJson datos) {
        if (datos.getEstadisticas() != null && datos.getEstadisticas().getAtaque() != null) {
            return datos.getEstadisticas().getAtaque();
        }

        if (datos.getAtaque() != null) {
            return datos.getAtaque();
        }

        return 1;
    }

    private static int obtenerVida(CartaJson datos) {
        if (datos.getEstadisticas() != null && datos.getEstadisticas().getSalud() != null) {
            return datos.getEstadisticas().getSalud();
        }

        if (datos.getSalud() != null) {
            return datos.getSalud();
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
            mazo.anadirCarta(cartasBase.get(i));
        }

        return mazo;
    }

    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    private static int leerEntero(String mensaje) {
        System.out.print(mensaje);

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
