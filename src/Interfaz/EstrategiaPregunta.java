package Interfaz;

import Implementacion.Personaje;
import java.util.List;

/**
 * Patron Strategy: define como una maquina elige la proxima pregunta.
 * Cada maquina recibe una estrategia distinta, lo que le da su "forma de jugar".
 */
public interface EstrategiaPregunta {

    /**
     * Elige la proxima pregunta a partir de los candidatos que siguen en juego.
     *
     * @return la pregunta elegida, o null si ninguna pregunta aporta informacion
     *         (todos los candidatos responderian igual a cualquiera de ellas).
     */
    String elegirPregunta(List<Personaje> candidatos, List<String> preguntasHechas);

    /** Explica en texto el razonamiento detras de la eleccion (se muestra en el modo MvM). */
    String explicar(List<Personaje> candidatos, List<String> preguntasHechas, String elegida);

    String getNombre();
}
