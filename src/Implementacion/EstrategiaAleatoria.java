package Implementacion;

import Interfaz.EstrategiaPregunta;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Estrategia sin heuristica - la usa la Maquina 1 (la menos asertiva).
 *
 * Elige al azar entre las preguntas que todavia separan a los candidatos. Siempre avanza
 * (descarta al menos un candidato por pregunta), pero no busca la division mas pareja:
 * puede preguntar algo que descarta a uno solo. Sirve como punto de comparacion contra Greedy.
 */
public class EstrategiaAleatoria implements EstrategiaPregunta {
    private final Random random;

    public EstrategiaAleatoria(Random random) {
        this.random = random;
    }

    @Override
    public String elegirPregunta(List<Personaje> candidatos, List<String> preguntasHechas) {
        List<String> utiles = preguntasUtiles(candidatos, preguntasHechas);
        if (utiles.isEmpty()) {
            return null;
        }
        return utiles.get(random.nextInt(utiles.size()));
    }

    @Override
    public String explicar(List<Personaje> candidatos, List<String> preguntasHechas, String elegida) {
        int si = Preguntas.contarCoincidencias(candidatos, elegida);
        return "Elige al azar entre " + preguntasUtiles(candidatos, preguntasHechas).size()
                + " preguntas utiles -> " + elegida + " (divide " + si + "/" + (candidatos.size() - si) + ").";
    }

    private List<String> preguntasUtiles(List<Personaje> candidatos, List<String> preguntasHechas) {
        List<String> utiles = new ArrayList<>();
        for (String pregunta : Preguntas.TODAS) {
            if (!preguntasHechas.contains(pregunta) && Preguntas.esInformativa(candidatos, pregunta)) {
                utiles.add(pregunta);
            }
        }
        return utiles;
    }

    @Override
    public String getNombre() {
        return "Aleatoria (sin heuristica)";
    }
}
