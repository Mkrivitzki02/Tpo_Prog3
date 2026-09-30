package Implementacion;

/** Una pregunta realizada junto con la respuesta obtenida. */
public final class RegistroPregunta {
    private final String pregunta;
    private final boolean respuesta;

    public RegistroPregunta(String pregunta, boolean respuesta) {
        this.pregunta = pregunta;
        this.respuesta = respuesta;
    }

    public String getPregunta() {
        return pregunta;
    }

    public boolean getRespuesta() {
        return respuesta;
    }

    @Override
    public String toString() {
        return pregunta + " -> " + (respuesta ? "SI" : "NO");
    }
}
