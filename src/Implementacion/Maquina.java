package Implementacion;

import Interfaz.EstrategiaPregunta;
import Interfaz.Oponente;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Jugador maquina. Lo que distingue a una maquina de otra se configura en el constructor:
 *
 * - estrategia:       como elige la proxima pregunta (patron Strategy).
 * - umbralArriesgar:  con cuantos candidatos (o menos) se anima a arriesgar un nombre.
 * - escuchaAlRival:   si usa las preguntas que le hizo el rival para saber cuanto sabe de ella.
 *
 * La maquina solo conoce al rival como Oponente: le puede preguntar y arriesgar, pero no
 * tiene acceso a su personaje. Lleva su propia lista de candidatos y la va filtrando
 * con las respuestas que recibe.
 */
public class Maquina {
    private final String nombre;
    private final EstrategiaPregunta estrategia;
    private final int umbralArriesgar;
    private final boolean escuchaAlRival;
    private final Random random;

    private Jugador identidad;
    private List<Personaje> candidatos = new ArrayList<>();
    private final List<RegistroPregunta> historial = new ArrayList<>();
    private final List<RegistroPregunta> conocimientoHeredado = new ArrayList<>();
    private int candidatosDelRival = Integer.MAX_VALUE;

    public Maquina(String nombre, EstrategiaPregunta estrategia, int umbralArriesgar,
            boolean escuchaAlRival, Random random) {
        this.nombre = nombre;
        this.estrategia = estrategia;
        this.umbralArriesgar = umbralArriesgar;
        this.escuchaAlRival = escuchaAlRival;
        this.random = random;
    }

    /** Elige su propio personaje (distinto de los excluidos) y reinicia sus candidatos. */
    public void nuevaPartida(Tablero tablero, Personaje... excluidos) {
        identidad = new Jugador(nombre, tablero.elegirAlAzar(random, excluidos));
        candidatos = new ArrayList<>(tablero.getPersonajes());
        historial.clear();
        conocimientoHeredado.clear();
        candidatosDelRival = Integer.MAX_VALUE;
    }

    /**
     * Ventaja de la Maquina 2: arranca aplicando preguntas y respuestas que obtuvo otra maquina
     * sobre el mismo rival, como si fueran filtros ya hechos.
     */
    public void incorporarConocimiento(List<RegistroPregunta> conocimiento) {
        for (RegistroPregunta registro : conocimiento) {
            candidatos = Preguntas.filtrar(candidatos, registro.getPregunta(), registro.getRespuesta());
            conocimientoHeredado.add(registro);
        }
    }

    /**
     * Escucha las preguntas que el rival le hizo a esta maquina. Como sabe que respondio,
     * puede calcular cuantos candidatos le quedan al rival y detectar si esta por ganar.
     */
    public void observarPreguntasDelRival(List<RegistroPregunta> preguntasDelRival, List<Personaje> todos) {
        if (!escuchaAlRival) {
            return;
        }
        List<Personaje> vistaDelRival = new ArrayList<>(todos);
        for (RegistroPregunta registro : preguntasDelRival) {
            vistaDelRival = Preguntas.filtrar(vistaDelRival, registro.getPregunta(), registro.getRespuesta());
        }
        candidatosDelRival = vistaDelRival.size();
    }

    /** Un turno: pregunta (filtro) o suposicion, segun cuantos candidatos le quedan. */
    public Jugada jugarTurno(Oponente rival) {
        String motivo = null;
        if (candidatos.size() <= umbralArriesgar) {
            motivo = candidatos.size() == 1
                    ? "Queda un solo candidato: arriesga con certeza."
                    : "Le quedan " + candidatos.size() + " candidatos (umbral " + umbralArriesgar + "): arriesga.";
        } else if (escuchaAlRival && candidatosDelRival <= 1) {
            motivo = "Por las preguntas del rival sabe que la tiene identificada y ganaria en su proximo turno: "
                    + "arriesga ahora entre " + candidatos.size() + " candidatos.";
        }

        String pregunta = null;
        if (motivo == null) {
            pregunta = estrategia.elegirPregunta(candidatos, preguntasHechas());
            if (pregunta == null) {
                motivo = "Ninguna pregunta separa a los candidatos restantes: arriesga.";
            }
        }

        if (motivo != null) {
            Personaje sospechoso = candidatos.get(random.nextInt(candidatos.size()));
            boolean acierto = rival.verificarSuposicion(sospechoso);
            if (!acierto) {
                candidatos.remove(sospechoso);
            }
            return Jugada.suposicion(nombre, sospechoso, acierto, candidatos.size(), motivo);
        }

        String razonamiento = estrategia.explicar(candidatos, preguntasHechas(), pregunta);
        boolean respuesta = rival.responder(pregunta);
        candidatos = Preguntas.filtrar(candidatos, pregunta, respuesta);
        historial.add(new RegistroPregunta(pregunta, respuesta));
        return Jugada.pregunta(nombre, pregunta, respuesta, candidatos.size(), razonamiento);
    }

    private List<String> preguntasHechas() {
        List<String> hechas = new ArrayList<>();
        for (RegistroPregunta registro : conocimientoHeredado) {
            hechas.add(registro.getPregunta());
        }
        for (RegistroPregunta registro : historial) {
            hechas.add(registro.getPregunta());
        }
        return hechas;
    }

    /** La maquina como rival: solo expone responder y verificar suposiciones. */
    public Oponente comoOponente() {
        return identidad;
    }

    /** Para mostrar el personaje al terminar la partida o al espectador del modo MvM. */
    public Personaje revelarPersonaje() {
        return identidad.revelarPersonaje();
    }

    public List<Personaje> getCandidatos() {
        return Collections.unmodifiableList(candidatos);
    }

    public List<RegistroPregunta> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    public List<RegistroPregunta> getConocimientoHeredado() {
        return Collections.unmodifiableList(conocimientoHeredado);
    }

    public String getNombre() {
        return nombre;
    }

    public String describirEstilo() {
        return nombre + ": estrategia " + estrategia.getNombre() + ", arriesga con " + umbralArriesgar
                + (umbralArriesgar == 1 ? " candidato" : " candidatos o menos")
                + (escuchaAlRival ? ", usa las preguntas del rival" : "");
    }
}
