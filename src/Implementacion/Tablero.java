package Implementacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Lista de los 23 personajes que arma la maquina al iniciar el juego.
 *
 * 1. Toma los personajes (agrupados solo por genero) y los ordena por genero con MergeSort.
 * 2. Los agrega uno por uno a la lista asignando un ID autoincremental (1, 2, 3, ...).
 * Como los IDs quedan ordenados, luego se puede buscar por ID con busqueda binaria.
 */
public class Tablero {
    private final List<Personaje> personajes = new ArrayList<>();
    private int proximoId = 1;

    public Tablero() {
        // Divide y Conquista: ordena los personajes con MergeSort antes de asignarles IDs.
        List<Personaje> ordenados = Ordenamiento.mergeSort(
                Personaje.crearPersonajes(), Comparator.comparing(Personaje::getGenero));
        for (Personaje personaje : ordenados) {
            agregar(personaje);
        }
    }

    private void agregar(Personaje personaje) {
        personaje.asignarId(proximoId++);
        personajes.add(personaje);
    }

    public List<Personaje> getPersonajes() {
        return Collections.unmodifiableList(personajes);
    }

    public int cantidad() {
        return personajes.size();
    }

    /** Busqueda binaria recursiva (Divide y Conquista): O(log n). */
    public Personaje buscarPorId(int id) {
        // La lista esta ordenada por ID, por eso se puede descartar una mitad en cada paso.
        return buscarPorId(id, 0, personajes.size() - 1);
    }

    private Personaje buscarPorId(int id, int inicio, int fin) {
        if (inicio > fin) {
            return null;
        }

        // Divide: revisa el elemento central del intervalo actual.
        int medio = (inicio + fin) / 2;
        Personaje personaje = personajes.get(medio);
        if (personaje.getId() == id) {
            return personaje;
        }
        if (id < personaje.getId()) {
            // Conquista: el ID solo puede estar en la mitad izquierda.
            return buscarPorId(id, inicio, medio - 1);
        }

        // Conquista: el ID solo puede estar en la mitad derecha.
        return buscarPorId(id, medio + 1, fin);
    }

    /** Busqueda lineal: la lista no esta ordenada por nombre y con n = 23 no justifica otra estructura. */
    public Personaje buscarPorNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        for (Personaje personaje : personajes) {
            if (personaje.getNombre().equalsIgnoreCase(nombre.trim())) {
                return personaje;
            }
        }
        return null;
    }

    /** Elige un personaje al azar distinto de los excluidos (asi cada jugador tiene uno diferente). */
    public Personaje elegirAlAzar(Random random, Personaje... excluidos) {
        List<Personaje> disponibles = new ArrayList<>(personajes);
        for (Personaje excluido : excluidos) {
            if (excluido != null) {
                disponibles.removeIf(p -> p.getId() == excluido.getId());
            }
        }
        return disponibles.get(random.nextInt(disponibles.size()));
    }
}
