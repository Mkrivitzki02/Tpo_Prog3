package Interfaz;

import Implementacion.Personaje;

/**
 * Lo unico que un jugador puede hacer con su rival: preguntarle algo o arriesgar un nombre.
 *
 * Las maquinas reciben a su rival solo a traves de esta interfaz, por lo que no tienen
 * forma (ni siquiera a nivel de compilacion) de leer el personaje secreto del otro:
 * solo obtienen respuestas SI/NO y la confirmacion de si una suposicion fue correcta.
 */
public interface Oponente {

    /** Responde SI (true) o NO (false) a una pregunta del tipo "PELO=NEGRO". */
    boolean responder(String pregunta);

    /** Indica si el personaje arriesgado es el personaje secreto. */
    boolean verificarSuposicion(Personaje sospechoso);
}
