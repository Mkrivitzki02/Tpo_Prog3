package Implementacion;

import java.util.ArrayList;
import java.util.List;

/**
 * Catalogo de preguntas (filtros) y la logica comun para evaluarlas.
 * Una pregunta tiene la forma CARACTERISTICA=VALOR, por ejemplo "PELO=NEGRO".
 */
public final class Preguntas {

    public static final List<String> TODAS = List.of(
            "GENERO=FEMENINO", "GENERO=MASCULINO",
            "CALVICIE=SI", "CALVICIE=NO",
            "LENTES=SI", "LENTES=NO",
            "PELO=NEGRO", "PELO=COLORADO", "PELO=AMARILLO");

    private Preguntas() {}

    public static boolean esValida(String pregunta) {
        return pregunta != null && TODAS.contains(pregunta);
    }

    /** Indica si el personaje cumple la caracteristica preguntada. */
    public static boolean coincide(Personaje personaje, String pregunta) {
        String[] partes = pregunta.split("=");
        if (partes.length != 2) {
            return false;
        }
        String valor = partes[1];
        switch (partes[0]) {
            case "GENERO":
                return personaje.getGenero().equalsIgnoreCase(valor);
            case "CALVICIE":
                return valor.equalsIgnoreCase("SI") == personaje.isCalvicie();
            case "LENTES":
                return valor.equalsIgnoreCase("SI") == personaje.isLentes();
            case "PELO":
                return personaje.getColorPelo().equalsIgnoreCase(valor);
            default:
                return false;
        }
    }

    public static int contarCoincidencias(List<Personaje> candidatos, String pregunta) {
        int contador = 0;
        for (Personaje personaje : candidatos) {
            if (coincide(personaje, pregunta)) {
                contador++;
            }
        }
        return contador;
    }

    /** Devuelve los candidatos compatibles con la respuesta obtenida (el "filtro" aplicado). */
    public static List<Personaje> filtrar(List<Personaje> candidatos, String pregunta, boolean respuesta) {
        List<Personaje> compatibles = new ArrayList<>();
        for (Personaje personaje : candidatos) {
            if (coincide(personaje, pregunta) == respuesta) {
                compatibles.add(personaje);
            }
        }
        return compatibles;
    }

    /** Una pregunta es util si separa a los candidatos (no todos responden igual). */
    public static boolean esInformativa(List<Personaje> candidatos, String pregunta) {
        int si = contarCoincidencias(candidatos, pregunta);
        return si > 0 && si < candidatos.size();
    }
}
