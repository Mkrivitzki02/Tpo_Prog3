package Implementacion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Divide y Conquista: MergeSort generico.
 *
 * - Dividir: parte la lista en dos mitades.
 * - Conquistar: ordena cada mitad recursivamente.
 * - Combinar: intercala las dos mitades ordenadas (merge).
 *
 * Complejidad: O(n log n) en todos los casos. Es ESTABLE (a igual clave respeta el orden
 * original), por eso al ordenar por genero cada grupo conserva el orden en que se cargaron
 * los personajes. Se usa para armar el tablero y para ordenar el ranking del marcador.
 */
public final class Ordenamiento {

    private Ordenamiento() {}

    public static <T> List<T> mergeSort(List<T> lista, Comparator<? super T> criterio) {
        // Caso base: una lista de cero o un elemento ya esta ordenada.
        if (lista.size() <= 1) {
            return new ArrayList<>(lista);
        }

        // Divide: separa la lista en dos mitades para resolverlas por separado.
        int medio = lista.size() / 2;
        List<T> izquierda = mergeSort(lista.subList(0, medio), criterio);
        List<T> derecha = mergeSort(lista.subList(medio, lista.size()), criterio);

        // Combina las dos mitades despues de ordenarlas recursivamente.
        return combinar(izquierda, derecha, criterio);
    }

    private static <T> List<T> combinar(List<T> izquierda, List<T> derecha, Comparator<? super T> criterio) {
        List<T> resultado = new ArrayList<>(izquierda.size() + derecha.size());
        int i = 0;
        int j = 0;
        while (i < izquierda.size() && j < derecha.size()) {
            // Conquista/combina: toma el menor elemento de cada mitad ordenada.
            // "<=" mantiene la estabilidad: ante empate gana el elemento de la izquierda.
            if (criterio.compare(izquierda.get(i), derecha.get(j)) <= 0) {
                resultado.add(izquierda.get(i++));
            } else {
                resultado.add(derecha.get(j++));
            }
        }
        while (i < izquierda.size()) {
            resultado.add(izquierda.get(i++));
        }
        while (j < derecha.size()) {
            resultado.add(derecha.get(j++));
        }
        return resultado;
    }
}
