package Implementacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Reglas y estado del juego. Lo usan tanto la consola (JuegoAdivinanzasImpl) como la GUI,
 * de modo que la logica y los algoritmos estan en un unico lugar.
 *
 * Sesion: el humano ingresa su nombre y elige su personaje una sola vez; queda fijo para
 * todas las partidas de la sesion (no se puede cambiar). Las maquinas eligen un personaje
 * nuevo, distinto al del humano y entre si, en cada partida.
 */
public class MotorJuego {

    public enum Modo { HUMANO_VS_MAQUINA_1, HUMANO_VS_MAQUINA_2, MAQUINA_VS_MAQUINA }

    private final Random random = new Random();
    private final Tablero tablero = new Tablero();
    private final Marcador marcador = new Marcador();

    // Las dos maquinas se diferencian por estrategia, umbral para arriesgar y uso de info del rival.
    private final Maquina maquina1 = new Maquina("Maquina 1", new EstrategiaAleatoria(random), 1, false, random);
    private final Maquina maquina2 = new Maquina("Maquina 2", new EstrategiaGreedy(), 2, true, random);

    // Sesion del humano
    private String nombreJugador;
    private Jugador humano;
    /** Todo lo que la Maquina 1 pregunto al humano en la sesion: la Maquina 2 parte con esto. */
    private final List<RegistroPregunta> preguntasDeMaquina1AlHumano = new ArrayList<>();

    // Partida en curso
    private Modo modo;
    private Maquina rival;
    private List<Personaje> candidatosHumano = new ArrayList<>();
    private final List<RegistroPregunta> preguntasHumano = new ArrayList<>();
    private boolean terminada = true;
    private String ganador;
    private int turno;

    // ===================== Sesion =====================

    public void iniciarSesion(String nombre, int idPersonaje) {
        Personaje elegido = tablero.buscarPorId(idPersonaje);
        if (elegido == null) {
            throw new IllegalArgumentException("No existe un personaje con ID " + idPersonaje + ".");
        }
        String nombreLimpio = (nombre == null || nombre.trim().isEmpty()) ? "Jugador" : nombre.trim();
        this.nombreJugador = nombreLimpio;
        this.humano = new Jugador(nombreLimpio, elegido);
        this.preguntasDeMaquina1AlHumano.clear();
        this.terminada = true;
        marcador.registrarJugador(nombreLimpio);
    }

    public boolean haySesion() {
        return humano != null;
    }

    // ===================== Humano vs Maquina =====================

    public void nuevaPartidaHumano(int numeroMaquina) {
        if (!haySesion()) {
            throw new IllegalStateException("Primero hay que iniciar sesion y elegir personaje.");
        }
        Personaje propio = humano.revelarPersonaje();
        maquina1.nuevaPartida(tablero, propio);
        maquina2.nuevaPartida(tablero, propio, maquina1.revelarPersonaje());

        modo = numeroMaquina == 2 ? Modo.HUMANO_VS_MAQUINA_2 : Modo.HUMANO_VS_MAQUINA_1;
        rival = numeroMaquina == 2 ? maquina2 : maquina1;
        if (rival == maquina2) {
            maquina2.incorporarConocimiento(preguntasDeMaquina1AlHumano);
        }
        candidatosHumano = new ArrayList<>(tablero.getPersonajes());
        preguntasHumano.clear();
        terminada = false;
        ganador = null;
        turno = 1;
    }

    /** El humano aplica un filtro: pregunta al rival y descarta los candidatos incompatibles. */
    public boolean preguntarHumano(String pregunta) {
        verificarTurnoHumano();
        String normalizada = pregunta == null ? "" : pregunta.trim().toUpperCase();
        if (!Preguntas.esValida(normalizada)) {
            throw new IllegalArgumentException("Pregunta invalida: " + pregunta);
        }
        for (RegistroPregunta registro : preguntasHumano) {
            if (registro.getPregunta().equals(normalizada)) {
                throw new IllegalArgumentException("Esa pregunta ya la hiciste (respuesta: "
                        + (registro.getRespuesta() ? "SI" : "NO") + ").");
            }
        }
        boolean respuesta = rival.comoOponente().responder(normalizada);
        preguntasHumano.add(new RegistroPregunta(normalizada, respuesta));
        candidatosHumano = Preguntas.filtrar(candidatosHumano, normalizada, respuesta);
        return respuesta;
    }

    /** El humano arriesga un nombre. Si acierta gana y se suma la victoria al marcador. */
    public boolean arriesgarHumano(String nombrePersonaje) {
        verificarTurnoHumano();
        Personaje sospechoso = tablero.buscarPorNombre(nombrePersonaje);
        if (sospechoso == null) {
            throw new IllegalArgumentException("No existe el personaje \"" + nombrePersonaje + "\".");
        }
        if (rival.comoOponente().verificarSuposicion(sospechoso)) {
            terminada = true;
            ganador = nombreJugador;
            marcador.registrarVictoria(nombreJugador);
            return true;
        }
        candidatosHumano.removeIf(p -> p.getId() == sospechoso.getId());
        return false;
    }

    /** Turno de la maquina rival contra el humano. */
    public Jugada turnoMaquina() {
        verificarTurnoHumano();
        Jugada jugada = rival.jugarTurno(humano);
        if (rival == maquina1 && !jugada.esSuposicion()) {
            // Queda registrado para que la Maquina 2 lo aproveche en sus partidas.
            preguntasDeMaquina1AlHumano.add(new RegistroPregunta(jugada.getPregunta(), jugada.getRespuesta()));
        }
        if (jugada.esGanadora()) {
            terminada = true;
            ganador = rival.getNombre();
        } else {
            turno++;
        }
        return jugada;
    }

    private void verificarTurnoHumano() {
        if (terminada || rival == null || modo == Modo.MAQUINA_VS_MAQUINA) {
            throw new IllegalStateException("No hay una partida Humano vs Maquina en curso.");
        }
    }

    // ===================== Maquina vs Maquina =====================

    public void nuevaPartidaMvM() {
        maquina1.nuevaPartida(tablero);
        maquina2.nuevaPartida(tablero, maquina1.revelarPersonaje());
        modo = Modo.MAQUINA_VS_MAQUINA;
        rival = null;
        terminada = false;
        ganador = null;
        turno = 1;
    }

    /**
     * Un turno completo: juega la Maquina 1 y, si no gano, la Maquina 2 (que antes escucha
     * las preguntas hechas por la Maquina 1). No hay limite de turnos: la partida siempre
     * termina porque cada jugada descarta al menos un candidato.
     */
    public List<Jugada> turnoMvM() {
        if (terminada || modo != Modo.MAQUINA_VS_MAQUINA) {
            throw new IllegalStateException("No hay una partida Maquina vs Maquina en curso.");
        }
        List<Jugada> jugadas = new ArrayList<>();
        Jugada jugada1 = maquina1.jugarTurno(maquina2.comoOponente());
        jugadas.add(jugada1);
        if (jugada1.esGanadora()) {
            terminada = true;
            ganador = maquina1.getNombre();
            return jugadas;
        }

        maquina2.observarPreguntasDelRival(maquina1.getHistorial(), tablero.getPersonajes());
        Jugada jugada2 = maquina2.jugarTurno(maquina1.comoOponente());
        jugadas.add(jugada2);
        if (jugada2.esGanadora()) {
            terminada = true;
            ganador = maquina2.getNombre();
            return jugadas;
        }
        turno++;
        return jugadas;
    }

    // ===================== Consultas =====================

    public List<Personaje> getPersonajes() {
        return tablero.getPersonajes();
    }

    public Personaje buscarPorId(int id) {
        return tablero.buscarPorId(id);
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    /** El humano puede ver su propio personaje. */
    public Personaje getPersonajeHumano() {
        return humano == null ? null : humano.revelarPersonaje();
    }

    public List<Personaje> getCandidatosHumano() {
        return Collections.unmodifiableList(candidatosHumano);
    }

    public List<RegistroPregunta> getPreguntasHumano() {
        return Collections.unmodifiableList(preguntasHumano);
    }

    public Maquina getRival() {
        return rival;
    }

    public Maquina getMaquina1() {
        return maquina1;
    }

    public Maquina getMaquina2() {
        return maquina2;
    }

    public List<RegistroPregunta> getPreguntasDeMaquina1AlHumano() {
        return Collections.unmodifiableList(preguntasDeMaquina1AlHumano);
    }

    public Modo getModo() {
        return modo;
    }

    public boolean isTerminada() {
        return terminada;
    }

    public String getGanador() {
        return ganador;
    }

    public int getTurno() {
        return turno;
    }

    public Marcador getMarcador() {
        return marcador;
    }
}
