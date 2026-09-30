package Implementacion;

import Interfaz.Oponente;

/**
 * Participante con un personaje secreto (el humano o la identidad de una maquina).
 *
 * El secreto es privado y final: se fija al crear el jugador y no se puede cambiar
 * durante la partida. Hacia afuera solo se exponen las operaciones de Oponente
 * (responder preguntas y verificar suposiciones), nunca el personaje en si.
 * Las respuestas se calculan a partir del secreto, asi nadie puede mentir.
 */
public class Jugador implements Oponente {
    private final String nombre;
    private final Personaje secreto;

    public Jugador(String nombre, Personaje secreto) {
        if (secreto == null) {
            throw new IllegalArgumentException("El jugador debe elegir un personaje.");
        }
        this.nombre = nombre;
        this.secreto = secreto;
    }

    @Override
    public boolean responder(String pregunta) {
        return Preguntas.coincide(secreto, pregunta);
    }

    @Override
    public boolean verificarSuposicion(Personaje sospechoso) {
        return sospechoso != null && sospechoso.getId() == secreto.getId();
    }

    public String getNombre() {
        return nombre;
    }

    /**
     * Solo para la vista del propio duenio (mostrarle al humano su personaje) o para
     * revelarlo al final de la partida. Las maquinas nunca reciben un Jugador, sino un Oponente.
     */
    Personaje revelarPersonaje() {
        return secreto;
    }
}
