package Implementacion;

import Interfaz.EstrategiaPregunta;
import java.util.List;

/**
 * Estrategia Greedy (voraz) - la usa la Maquina 2.
 *
 * En cada turno toma la decision localmente optima: la pregunta que parte a los candidatos
 * lo mas cerca posible de la mitad (minimiza |SI - NO|). Asi, responda lo que responda el
 * rival, en el peor caso quedan la menor cantidad posible de candidatos y la busqueda se
 * acerca a una busqueda binaria (~log2(23) = 4.5 preguntas).
 *
 * Costo por turno: O(P * n), con P = 9 preguntas y n = candidatos restantes.
 * No garantiza el optimo global (ver documentacion), pero es simple y muy efectiva.
 */
public class EstrategiaGreedy implements EstrategiaPregunta {

    @Override
    public String elegirPregunta(List<Personaje> candidatos, List<String> preguntasHechas) {
        String mejorPregunta = null;
        int menorDiferencia = Integer.MAX_VALUE;
        int total = candidatos.size();

        for (String pregunta : Preguntas.TODAS) {
            if (preguntasHechas.contains(pregunta)) {
                continue;
            }
            int si = Preguntas.contarCoincidencias(candidatos, pregunta);
            if (si == 0 || si == total) {
                continue; // no separa a nadie: no aporta informacion
            }
            int diferencia = Math.abs(si - (total - si));
            if (diferencia < menorDiferencia) {
                menorDiferencia = diferencia;
                mejorPregunta = pregunta;
            }
        }
        return mejorPregunta;
    }

    @Override
    public String explicar(List<Personaje> candidatos, List<String> preguntasHechas, String elegida) {
        StringBuilder sb = new StringBuilder("Greedy evalua cada pregunta (SI/NO sobre ")
                .append(candidatos.size()).append(" candidatos): ");
        boolean primera = true;
        for (String pregunta : Preguntas.TODAS) {
            if (preguntasHechas.contains(pregunta) || !Preguntas.esInformativa(candidatos, pregunta)) {
                continue;
            }
            int si = Preguntas.contarCoincidencias(candidatos, pregunta);
            if (!primera) {
                sb.append(", ");
            }
            sb.append(pregunta).append(" ").append(si).append("/").append(candidatos.size() - si);
            primera = false;
        }
        sb.append(" -> elige ").append(elegida).append(" por ser la division mas pareja.");
        return sb.toString();
    }

    @Override
    public String getNombre() {
        return "Greedy (division mas pareja)";
    }
}
