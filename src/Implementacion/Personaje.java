package Implementacion;

import java.util.ArrayList;
import java.util.List;

public class Personaje {
    private int id;
    private String nombre;
    private String genero;
    private boolean calvicie;
    private boolean lentes;
    private String colorPelo;

    public Personaje(int id, String nombre, String genero, boolean calvicie, boolean lentes, String colorPelo) {
        this.id = id;
        this.nombre = nombre;
        this.genero = genero;
        this.calvicie = calvicie;
        this.lentes = lentes;
        this.colorPelo = colorPelo;
    }

    public static List<Personaje> crearPersonajes() {
        List<Personaje> personajes = new ArrayList<>();

        personajes.add(new Personaje(1, "Ana", "FEMENINO", false, true, "NEGRO"));
        personajes.add(new Personaje(2, "Belen", "FEMENINO", false, false, "COLORADO"));
        personajes.add(new Personaje(3, "Cecilia", "FEMENINO", true, true, "AMARILLO"));
        personajes.add(new Personaje(4, "Diana", "FEMENINO", false, false, "NEGRO"));
        personajes.add(new Personaje(5, "Eva", "FEMENINO", false, true, "COLORADO"));
        personajes.add(new Personaje(6, "Florencia", "FEMENINO", false, false, "AMARILLO"));
        personajes.add(new Personaje(7, "Gala", "FEMENINO", true, false, "NEGRO"));
        personajes.add(new Personaje(8, "Hilda", "FEMENINO", false, true, "COLORADO"));
        personajes.add(new Personaje(9, "Iris", "FEMENINO", false, false, "AMARILLO"));
        personajes.add(new Personaje(10, "Julia", "FEMENINO", true, true, "NEGRO"));
        personajes.add(new Personaje(11, "Karina", "FEMENINO", false, false, "COLORADO"));
        personajes.add(new Personaje(12, "Lucas", "MASCULINO", false, true, "NEGRO"));
        personajes.add(new Personaje(13, "Mateo", "MASCULINO", true, false, "COLORADO"));
        personajes.add(new Personaje(14, "Nicolas", "MASCULINO", false, true, "AMARILLO"));
        personajes.add(new Personaje(15, "Oscar", "MASCULINO", true, false, "NEGRO"));
        personajes.add(new Personaje(16, "Pablo", "MASCULINO", false, false, "COLORADO"));
        personajes.add(new Personaje(17, "Quintin", "MASCULINO", false, true, "NEGRO"));
        personajes.add(new Personaje(18, "Ramon", "MASCULINO", true, false, "AMARILLO"));
        personajes.add(new Personaje(19, "Sergio", "MASCULINO", false, false, "COLORADO"));
        personajes.add(new Personaje(20, "Tomas", "MASCULINO", true, true, "NEGRO"));
        personajes.add(new Personaje(21, "Ulises", "MASCULINO", false, false, "AMARILLO"));
        personajes.add(new Personaje(22, "Victor", "MASCULINO", false, true, "COLORADO"));
        personajes.add(new Personaje(23, "Walter", "MASCULINO", true, false, "NEGRO"));

        return personajes;
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

    public void setId(int id) {
        this.id = id;
    }

    public String toString() {
        return "[id=" + id + ", nombre=" + nombre + ", genero=" + genero + ", calvicie=" + calvicie + ", lentes=" + lentes + ", pelo=" + colorPelo + "]";
    }
}
