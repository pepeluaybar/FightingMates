package fightingmates;

public class Main {
    public static void main(String[] args) {
        Jugador jugador1 = new Jugador("Alice", Jugador.VIDA_INICIAL, crearMazoBase());
        Jugador jugador2 = new Jugador("Bob", Jugador.VIDA_INICIAL, crearMazoBase());

        Juego juego = new Juego(jugador1, jugador2);
        juego.iniciarPartida();

        System.out.println("Partida iniciada: " + juego);
        System.out.println("Estado J1: " + jugador1);
        System.out.println("Estado J2: " + jugador2);
    }

    private static Mazo crearMazoBase() {
        Mazo mazo = new Mazo();

        for (int i = 1; i <= 12; i++) {
            Habilidad habilidad = (i % 3 == 0)
                    ? new HabilidadDanio("Impacto", "Daño adicional", 1)
                    : null;
            Unidad unidad = new Unidad(i, "Unidad " + i, "Unidad base de prueba",
                    2 + (i % 2), 5, habilidad, "", 0, true);
            mazo.anadirCarta(unidad);
        }

        for (int i = 13; i <= 17; i++) {
            Objeto objeto = new Objeto(i, "Objeto " + i, "Objeto de soporte", "CURA", 2);
            mazo.anadirCarta(objeto);
        }

        return mazo;
    }
}
