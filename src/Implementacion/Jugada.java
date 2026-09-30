package Implementacion;

/** Resultado de un turno de una maquina: una pregunta o una suposicion. */
public final class Jugada {
    private final String autor;
    private final boolean esSuposicion;
    private final String pregunta;
    private final boolean respuesta;
    private final Personaje sospechoso;
    private final boolean acierto;
    private final int candidatosRestantes;
    private final String razonamiento;

    private Jugada(String autor, boolean esSuposicion, String pregunta, boolean respuesta,
            Personaje sospechoso, boolean acierto, int candidatosRestantes, String razonamiento) {
        this.autor = autor;
        this.esSuposicion = esSuposicion;
        this.pregunta = pregunta;
        this.respuesta = respuesta;
        this.sospechoso = sospechoso;
        this.acierto = acierto;
        this.candidatosRestantes = candidatosRestantes;
        this.razonamiento = razonamiento;
    }

    static Jugada pregunta(String autor, String pregunta, boolean respuesta, int restantes, String razonamiento) {
        return new Jugada(autor, false, pregunta, respuesta, null, false, restantes, razonamiento);
    }

    static Jugada suposicion(String autor, Personaje sospechoso, boolean acierto, int restantes, String razonamiento) {
        return new Jugada(autor, true, null, false, sospechoso, acierto, restantes, razonamiento);
    }

    public String getAutor() {
        return autor;
    }

    public boolean esSuposicion() {
        return esSuposicion;
    }

    public String getPregunta() {
        return pregunta;
    }

    public boolean getRespuesta() {
        return respuesta;
    }

    public Personaje getSospechoso() {
        return sospechoso;
    }

    /** true solo si fue una suposicion correcta (la jugada gana la partida). */
    public boolean esGanadora() {
        return esSuposicion && acierto;
    }

    public int getCandidatosRestantes() {
        return candidatosRestantes;
    }

    public String getRazonamiento() {
        return razonamiento;
    }

    /** Resumen en una linea, para consola y GUI. */
    public String resumen() {
        if (esSuposicion) {
            return autor + " arriesga: \"" + sospechoso.getNombre() + "\" -> "
                    + (acierto ? "CORRECTO" : "incorrecto (lo descarta, " + textoCandidatos() + ")");
        }
        return autor + " pregunta " + pregunta + " -> " + (respuesta ? "SI" : "NO")
                + " (" + textoCandidatos() + ")";
    }

    private String textoCandidatos() {
        return candidatosRestantes == 1 ? "queda 1 candidato" : "quedan " + candidatosRestantes + " candidatos";
    }
}
