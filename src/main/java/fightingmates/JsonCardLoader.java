package fightingmates;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Locale;

/** Carga cartas del juego desde JSON con Gson y clases simples. */
public class JsonCardLoader {
    public static final String RUTA_CARTEAS_POR_DEFECTO = "resources/cards/cards.json";

    public ArrayList<Carta> cargar(String rutaTexto) throws CardLoadException {
        String rutaNormalizada = esVacio(rutaTexto) ? RUTA_CARTEAS_POR_DEFECTO : rutaTexto;
        File archivo = new File(rutaNormalizada);
        if (!archivo.exists()) {
            throw new CardLoadException("Archivo no encontrado: " + rutaNormalizada);
        }

        CartaJson[] datosCartas;
        try (FileReader lector = new FileReader(archivo)) {
            datosCartas = new Gson().fromJson(lector, CartaJson[].class);
        } catch (FileNotFoundException e) {
            throw new CardLoadException("No se pudo leer el archivo JSON de cartas: " + rutaNormalizada, e);
        } catch (JsonSyntaxException e) {
            throw new CardLoadException("JSON mal formado: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CardLoadException("Error leyendo el JSON de cartas: " + rutaNormalizada, e);
        }

        if (datosCartas == null) {
            throw new CardLoadException("JSON mal formado: el archivo de cartas debe contener una lista de cartas");
        }

        ArrayList<Carta> cartas = new ArrayList<>();
        int siguienteId = 1;
        for (int i = 0; i < datosCartas.length; i++) {
            CartaJson datoCarta = datosCartas[i];
            int copias = datoCarta != null && datoCarta.getCopias() != null ? Math.max(1, datoCarta.getCopias()) : 1;
            for (int copia = 0; copia < copias; copia++) {
                cartas.add(aCarta(datoCarta, siguienteId++, i + 1));
            }
        }
        return cartas;
    }

    private Carta aCarta(CartaJson datos, int id, int numeroCarta) throws CardLoadException {
        if (datos == null) {
            throw new CardLoadException("JSON mal formado: la carta #" + numeroCarta + " no es un objeto JSON");
        }

        String nombre = textoRequerido(datos.getNombre(), "nombre", numeroCarta);
        String rareza = textoRequerido(datos.getRareza(), "rareza", numeroCarta);
        String tipo = textoRequerido(datos.getTipo(), "tipo", numeroCarta);
        String objetivo = textoRequerido(datos.getObjetivo(), "objetivo", numeroCarta);
        String momento = textoRequerido(datos.getMomento(), "momento", numeroCarta);
        String descripcion = textoRequerido(datos.getDescripcion(), "descripcion", numeroCarta);
        ArrayList<Efecto> efectos = analizarEfectos(datos.getEfectos(), numeroCarta);

        String claseOriginal = !esVacio(datos.getClaseCarta()) ? datos.getClaseCarta() : datos.getClase();
        String claseCarta = esVacio(claseOriginal) ? "unit" : claseOriginal;

        Carta carta;
        if (esClaseObjeto(claseCarta)) {
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

    private Unidad crearUnidad(int id, String nombre, String descripcion, CartaJson datos, ArrayList<Efecto> efectos) {
        int ataque = obtenerAtaque(datos);
        int salud = obtenerSalud(datos);
        Habilidad habilidad = crearHabilidad(nombre, efectos);
        return new Unidad(id, nombre, descripcion, ataque, salud, habilidad, "", 0, true);
    }

    private int obtenerAtaque(CartaJson datos) {
        if (datos.getEstadisticas() != null && datos.getEstadisticas().getAtaque() != null) return datos.getEstadisticas().getAtaque();
        if (datos.getAtaque() != null) return datos.getAtaque();
        return 1;
    }

    private int obtenerSalud(CartaJson datos) {
        if (datos.getEstadisticas() != null && datos.getEstadisticas().getSalud() != null) return datos.getEstadisticas().getSalud();
        if (datos.getSalud() != null) return datos.getSalud();
        return 5;
    }

    private Objeto crearObjeto(int id, String nombre, String descripcion, ArrayList<Efecto> efectos) {
        Efecto efectoPrincipal = efectos.isEmpty() ? new Efecto() : efectos.get(0);
        String efectoObjeto = aTipoEfectoObjeto(efectoPrincipal.getTipo());
        return new Objeto(id, nombre, descripcion, efectoObjeto, efectoPrincipal.getValor());
    }

    private Habilidad crearHabilidad(String nombreCarta, ArrayList<Efecto> efectos) {
        if (efectos.isEmpty()) return null;
        Efecto efecto = efectos.get(0);
        String tipo = normalizar(efecto.getTipo());
        String descripcionEfecto = !esVacio(efecto.getDescripcion()) ? efecto.getDescripcion() : "Efecto de " + nombreCarta;
        String nombreHabilidad = extraerNombreHabilidad(descripcionEfecto, nombreCarta);

        switch (tipo) {
            case "damage":
            case "damage_percent_max_hp":
                return new HabilidadDanio(nombreHabilidad, descripcionEfecto, efecto.getValor());
            case "heal":
            case "heal_percent_max_hp":
                return new HabilidadCura(nombreHabilidad, descripcionEfecto, efecto.getValor(), esObjetivoAliado(efecto.getObjetivo()));
            case "status":
            case "apply_status":
                String estado = !esVacio(efecto.getValorTexto()) ? efecto.getValorTexto() : nombreHabilidad;
                return new HabilidadEstado(nombreHabilidad, descripcionEfecto, estado, Math.max(1, efecto.getValor()));
            default:
                return null;
        }
    }

    private ArrayList<Efecto> analizarEfectos(ArrayList<EfectoJson> efectosCrudos, int numeroCarta) throws CardLoadException {
        if (efectosCrudos == null) {
            throw new CardLoadException("Campos obligatorios vacíos: la carta #" + numeroCarta + " debe tener 'effects' como lista, aunque esté vacía");
        }

        ArrayList<Efecto> efectos = new ArrayList<>();
        for (int i = 0; i < efectosCrudos.size(); i++) {
            EfectoJson datoEfecto = efectosCrudos.get(i);
            if (datoEfecto == null) {
                throw new CardLoadException("JSON mal formado: el efecto #" + (i + 1) + " de la carta #" + numeroCarta + " no es un objeto");
            }
            String tipo = textoRequerido(datoEfecto.getType(), "tipo", numeroCarta);
            if (!esEfectoSoportado(tipo)) {
                throw new CardLoadException("Efecto desconocido: '" + tipo + "' en la carta #" + numeroCarta);
            }

            String objetivo = textoSeguro(datoEfecto.getTarget());
            String descripcion = textoSeguro(datoEfecto.getDescription());
            int valor = datoEfecto.getValue() != null ? datoEfecto.getValue() : 0;
            String valorTexto = !esVacio(datoEfecto.getTextValue()) ? datoEfecto.getTextValue() : textoSeguro(datoEfecto.getStatus());
            efectos.add(new Efecto(tipo, objetivo, valor, valorTexto, descripcion));
        }
        return efectos;
    }

    private boolean esEfectoSoportado(String tipo) {
        switch (normalizar(tipo)) {
            case "damage":
            case "heal":
            case "status":
            case "apply_status":
            case "bonus_attack":
            case "player_damage":
            case "buff_damage":
            case "buff_defense":
            case "buff_technique":
            case "clear_status":
            case "reduce_damage":
            case "reduce_next_damage":
            case "skip_next_turn":
            case "evade_next_effect":
            case "random_effect":
            case "damage_percent_max_hp":
            case "heal_percent_max_hp":
            case "summon":
            case "conditional_immunity":
            case "destroy_all_cards":
            case "destroy_random_card":
            case "return_to_hand":
                return true;
            default:
                return false;
        }
    }

    private String aTipoEfectoObjeto(String tipoEfecto) {
        switch (normalizar(tipoEfecto)) {
            case "damage": return "DANIO";
            case "heal": return "CURA";
            case "player_damage": return "DANIO_JUGADOR";
            case "bonus_attack":
            case "buff_damage": return "BONUS_ATAQUE";
            default: return normalizar(tipoEfecto).toUpperCase(Locale.ROOT);
        }
    }

    private String extraerNombreHabilidad(String descripcionEfecto, String nombreCarta) {
        int separador = descripcionEfecto.indexOf(':');
        if (separador > 0) return descripcionEfecto.substring(0, separador).trim();
        return nombreCarta;
    }

    private boolean esClaseObjeto(String claseCarta) {
        String normalizado = normalizar(claseCarta);
        return "object".equals(normalizado) || "objeto".equals(normalizado) || "item".equals(normalizado);
    }

    private boolean esObjetivoAliado(String objetivo) {
        String normalizado = normalizar(objetivo);
        return "ally".equals(normalizado) || "all_allies".equals(normalizado) || "self".equals(normalizado)
                || "aliado".equals(normalizado) || "jugador".equals(normalizado);
    }

    private String textoRequerido(String valor, String campo, int numeroCarta) throws CardLoadException {
        String seguro = textoSeguro(valor);
        if (esVacio(seguro)) {
            throw new CardLoadException("Campos obligatorios vacíos: '" + campo + "' en la carta #" + numeroCarta);
        }
        return seguro;
    }

    private String textoSeguro(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }
}