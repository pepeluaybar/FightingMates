package fightingmates;

import java.util.ArrayList;
import java.util.Scanner;

/** Arranque simple de FightingMates. */
public class Main {

    public static void main(String[] args) {
        String rutaCartas = JsonCardLoader.RUTA_CARTEAS_POR_DEFECTO;
        for (int i = 0; i < args.length - 1; i++) {
            if ("--cards".equals(args[i])) {
                rutaCartas = args[i + 1];
            }
        }

        ArrayList<Carta> cartas;
        try {
            cartas = new JsonCardLoader().cargar(rutaCartas);
        } catch (CardLoadException e) {
            System.out.println("Error cargando cartas: " + e.getMessage());
            return;
        }

        System.out.println("Cartas cargadas: " + cartas.size());

        Scanner scanner = new Scanner(System.in);
        System.out.print("Nombre Jugador 1: ");
        String nombre1 = scanner.nextLine().trim();
        if (nombre1.isEmpty()) nombre1 = "Jugador 1";

        System.out.print("Nombre Jugador 2: ");
        String nombre2 = scanner.nextLine().trim();
        if (nombre2.isEmpty()) nombre2 = "Jugador 2";

        Jugador j1 = new Jugador(nombre1, Jugador.VIDA_INICIAL, crearMazo(cartas));
        Jugador j2 = new Jugador(nombre2, Jugador.VIDA_INICIAL, crearMazo(cartas));

        Juego juego = new Juego(j1, j2);
        juego.iniciarPartida();

        System.out.println("Partida iniciada. Turno actual: " + juego.getJugadorActual().getNombre());
    }

    private static Mazo crearMazo(ArrayList<Carta> cartasBase) {
        Mazo mazo = new Mazo();
        for (int i = 0; i < cartasBase.size() && !mazo.estaLleno(); i++) {
            mazo.anadirCarta(cartasBase.get(i));
        }
        return mazo;
    }
}