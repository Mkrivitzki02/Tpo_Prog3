package Implementacion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Marcador record persistido en un archivo de texto con formato "nombre=victorias".
 * Se lee completo al consultarlo y se reescribe completo al registrar una victoria.
 */
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
                // Se separa por el ULTIMO "=" para admitir nombres que contengan "=".
                int separador = linea.lastIndexOf('=');
                if (separador <= 0) {
                    System.out.println("Se ignora un registro invalido del marcador: " + linea);
                    continue;
                }
                try {
                    int victorias = Integer.parseInt(linea.substring(separador + 1).trim());
                    puntuaciones.put(linea.substring(0, separador).trim(), victorias);
                } catch (NumberFormatException e) {
                    System.out.println("Se ignora un registro invalido del marcador: " + linea);
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

    /** Ranking de mayor a menor cantidad de victorias (reutiliza el MergeSort de Ordenamiento). */
    public List<Map.Entry<String, Integer>> obtenerRanking() {
        List<Map.Entry<String, Integer>> entradas = new ArrayList<>(cargarMarcador().entrySet());
        // Divide y Conquista: ordena recursivamente las puntuaciones y luego las combina.
        return Ordenamiento.mergeSort(entradas, (a, b) -> Integer.compare(b.getValue(), a.getValue()));
    }

    public String textoRanking() {
        List<Map.Entry<String, Integer>> ranking = obtenerRanking();
        if (ranking.isEmpty()) {
            return "Todavia no hay registros en el marcador.";
        }
        StringBuilder sb = new StringBuilder();
        int posicion = 1;
        for (Map.Entry<String, Integer> entrada : ranking) {
            sb.append(posicion++).append(". ").append(entrada.getKey()).append(" -> ")
                    .append(entrada.getValue()).append(" partida(s) ganada(s)\n");
        }
        return sb.toString();
    }

    public void mostrarMarcador() {
        System.out.println("=== MARCADOR RECORD ===");
        System.out.print(textoRanking());
        System.out.println("=======================");
    }
}
