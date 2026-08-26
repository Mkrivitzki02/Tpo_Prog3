package Implementacion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Marcador {
    private static final String ARCHIVO = "marcador_record.txt";

    public Map<String, Integer> cargarMarcador() {
        Map<String, Integer> puntuaciones = new HashMap<>();

        try (BufferedReader lector = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                String[] datos = linea.split("=");
                if (datos.length == 2) {
                    puntuaciones.put(datos[0], Integer.parseInt(datos[1]));
                }
            }
        } catch (IOException e) {
            // Si no existe archivo, se crea al guardar por primera vez.
        }

        return puntuaciones;
    }

    public void registrarJugador(String nombreUsuario) {
        Map<String, Integer> marcador = cargarMarcador();
        if (!marcador.containsKey(nombreUsuario)) {
            marcador.put(nombreUsuario, 0);
            guardarMarcador(marcador);
        }
    }

    public void registrarVictoria(String nombreUsuario) {
        Map<String, Integer> marcador = cargarMarcador();
        int victorias = marcador.getOrDefault(nombreUsuario, 0);
        marcador.put(nombreUsuario, victorias + 1);
        guardarMarcador(marcador);
    }

    public void guardarMarcador(Map<String, Integer> marcador) {
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(ARCHIVO))) {
            for (Map.Entry<String, Integer> entrada : marcador.entrySet()) {
                escritor.write(entrada.getKey() + "=" + entrada.getValue());
                escritor.newLine();
            }
        } catch (IOException e) {
            System.out.println("No se pudo guardar el marcador. Error: " + e.getMessage());
        }
    }

    public void mostrarMarcador() {
        Map<String, Integer> marcador = cargarMarcador();
        if (marcador.isEmpty()) {
            System.out.println("Todavia no hay registros en el marcador.");
            return;
        }

        System.out.println("=== MARCADOR RECORD ===");
        for (Map.Entry<String, Integer> entrada : marcador.entrySet()) {
            System.out.println(entrada.getKey() + " -> " + entrada.getValue() + " partidas ganadas");
        }
        System.out.println("=======================");
    }
}
