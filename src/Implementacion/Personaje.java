package Implementacion;

import java.util.ArrayList;
import java.util.List;

public class Personaje {
    // El ID no se declara a mano: lo asigna el Tablero (autoincremental) al agregar el personaje.
    private int id;
    private final String nombre;
    private final String genero;
    private final boolean calvicie;
    private final boolean lentes;
    private final String colorPelo;

    public Personaje(String nombre, String genero, boolean calvicie, boolean lentes, String colorPelo) {
        this.nombre = nombre;
        this.genero = genero;
        this.calvicie = calvicie;
        this.lentes = lentes;
        this.colorPelo = colorPelo;
    }

    /**
     * Carga los 23 personajes, agrupados unicamente por genero.
     * Con 4 caracteristicas (2 generos x 2 calvicie x 2 lentes x 3 colores) hay 24 combinaciones
     * posibles: cada personaje usa una distinta, asi siempre se lo puede identificar con filtros.
     */
    public static List<Personaje> crearPersonajes() {
        List<Personaje> personajes = new ArrayList<>();

        // Personajes Femeninos (11 combinaciones unicas)
        personajes.add(new Personaje("Ana", "FEMENINO", false, true, "NEGRO"));
        personajes.add(new Personaje("Belen", "FEMENINO", false, false, "COLORADO"));
        personajes.add(new Personaje("Cecilia", "FEMENINO", true, true, "AMARILLO"));
        personajes.add(new Personaje("Diana", "FEMENINO", false, false, "NEGRO"));
        personajes.add(new Personaje("Eva", "FEMENINO", false, true, "COLORADO"));
        personajes.add(new Personaje("Florencia", "FEMENINO", false, false, "AMARILLO"));
        personajes.add(new Personaje("Gala", "FEMENINO", true, false, "NEGRO"));
        personajes.add(new Personaje("Hilda", "FEMENINO", true, false, "COLORADO"));
        personajes.add(new Personaje("Iris", "FEMENINO", false, true, "AMARILLO"));
        personajes.add(new Personaje("Julia", "FEMENINO", true, true, "NEGRO"));
        personajes.add(new Personaje("Karina", "FEMENINO", true, true, "COLORADO"));

        // Personajes Masculinos (12 combinaciones unicas)
        personajes.add(new Personaje("Lucas", "MASCULINO", false, true, "NEGRO"));
        personajes.add(new Personaje("Mateo", "MASCULINO", true, false, "COLORADO"));
        personajes.add(new Personaje("Nicolas", "MASCULINO", false, true, "AMARILLO"));
        personajes.add(new Personaje("Oscar", "MASCULINO", true, false, "NEGRO"));
        personajes.add(new Personaje("Pablo", "MASCULINO", false, false, "COLORADO"));
        personajes.add(new Personaje("Quintin", "MASCULINO", false, false, "NEGRO"));
        personajes.add(new Personaje("Ramon", "MASCULINO", true, false, "AMARILLO"));
        personajes.add(new Personaje("Sergio", "MASCULINO", true, true, "COLORADO"));
        personajes.add(new Personaje("Tomas", "MASCULINO", true, true, "NEGRO"));
        personajes.add(new Personaje("Ulises", "MASCULINO", false, false, "AMARILLO"));
        personajes.add(new Personaje("Victor", "MASCULINO", false, true, "COLORADO"));
        personajes.add(new Personaje("Walter", "MASCULINO", true, true, "AMARILLO"));

        return personajes;
    }

    /** Solo el Tablero asigna IDs (visibilidad de paquete). */
    void asignarId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getGenero() {
        return genero;
    }

    public boolean isCalvicie() {
        return calvicie;
    }

    public boolean isLentes() {
        return lentes;
    }

    public String getColorPelo() {
        return colorPelo;
    }

    public String descripcion() {
        return nombre + " (" + genero + " | calvo: " + (calvicie ? "SI" : "NO")
                + " | lentes: " + (lentes ? "SI" : "NO") + " | pelo: " + colorPelo + ")";
    }

    @Override
    public String toString() {
        return "[id=" + id + ", nombre=" + nombre + ", genero=" + genero + ", calvicie=" + calvicie
                + ", lentes=" + lentes + ", pelo=" + colorPelo + "]";
    }
}
