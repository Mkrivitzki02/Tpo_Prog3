package Implementacion;

import Interfaz.Oponente;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Casos de prueba del juego (sin dependencias externas).
 *
 * Ejecutar desde la raiz del proyecto:
 *   javac -d bin $(find src test -name "*.java")
 *   java -cp bin Implementacion.PruebasJuego
 *
 * Las pruebas del marcador trabajan sobre un marcador_record.txt temporal y restauran
 * el original al terminar.
 */
public class PruebasJuego {
    private static int aprobadas = 0;
    private static final List<String> fallidas = new ArrayList<>();

    // ============================ mini framework ============================

    interface Prueba {
        void correr() throws Exception;
    }

    private static void prueba(String nombre, Prueba p) {
        try {
            p.correr();
            aprobadas++;
            System.out.println("  [OK]    " + nombre);
        } catch (Throwable e) {
            fallidas.add(nombre + " -> " + e);
            System.out.println("  [FALLA] " + nombre + " -> " + e);
        }
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    private static void igual(Object esperado, Object obtenido, String mensaje) {
        if (esperado == null ? obtenido != null : !esperado.equals(obtenido)) {
            throw new AssertionError(mensaje + " (esperado: " + esperado + ", obtenido: " + obtenido + ")");
        }
    }

    private static <T extends Throwable> void lanza(Class<T> tipo, Prueba p, String mensaje) {
        try {
            p.correr();
        } catch (Throwable e) {
            if (tipo.isInstance(e)) {
                return;
            }
            throw new AssertionError(mensaje + " (lanzo " + e + ")");
        }
        throw new AssertionError(mensaje + " (no lanzo excepcion)");
    }

    private static List<String> nombres(List<Personaje> lista) {
        List<String> r = new ArrayList<>();
        for (Personaje p : lista) {
            r.add(p.getNombre());
        }
        return r;
    }

    private static boolean contiene(List<Personaje> lista, Personaje buscado) {
        for (Personaje p : lista) {
            if (p.getId() == buscado.getId()) {
                return true;
            }
        }
        return false;
    }

    /** Oponente de prueba que cuenta cuantas veces se lo consulta. */
    static class OponenteEspia implements Oponente {
        final Jugador real;
        int preguntas = 0;
        int suposiciones = 0;

        OponenteEspia(Personaje secreto) {
            real = new Jugador("espia", secreto);
        }

        @Override
        public boolean responder(String pregunta) {
            preguntas++;
            return real.responder(pregunta);
        }

        @Override
        public boolean verificarSuposicion(Personaje sospechoso) {
            suposiciones++;
            return real.verificarSuposicion(sospechoso);
        }
    }

    // ============================ main ============================

    public static void main(String[] args) throws Exception {
        File archivo = new File("marcador_record.txt");
        byte[] respaldo = archivo.exists() ? java.nio.file.Files.readAllBytes(archivo.toPath()) : null;
        try {
            pruebasOrdenamiento();
            pruebasTablero();
            pruebasPreguntas();
            pruebasJugador();
            pruebasEstrategias();
            pruebasMaquina();
            pruebasMotorSesion();
            pruebasMotorHumano();
            pruebasVentajaMaquina2();
            pruebasMotorMvM();
            pruebasSimulacionMasiva();
            pruebasMarcador();
            pruebasConsola();
        } finally {
            if (respaldo != null) {
                java.nio.file.Files.write(archivo.toPath(), respaldo);
            } else {
                archivo.delete();
            }
        }

        System.out.println();
        System.out.println("==============================================");
        System.out.println("Pruebas aprobadas: " + aprobadas + " | fallidas: " + fallidas.size());
        for (String f : fallidas) {
            System.out.println("  - " + f);
        }
        System.exit(fallidas.isEmpty() ? 0 : 1);
    }

    // ============================ Ordenamiento ============================

    private static void pruebasOrdenamiento() {
        System.out.println("\n== Ordenamiento (MergeSort) ==");
        Comparator<Integer> natural = Comparator.naturalOrder();

        prueba("Lista vacia devuelve lista vacia", () ->
                igual(List.of(), Ordenamiento.mergeSort(new ArrayList<Integer>(), natural), "vacia"));

        prueba("Lista de un elemento", () ->
                igual(List.of(7), Ordenamiento.mergeSort(List.of(7), natural), "un elemento"));

        prueba("Ordena lista desordenada con repetidos y negativos", () ->
                igual(List.of(-3, 0, 1, 1, 2, 5, 9), Ordenamiento.mergeSort(List.of(5, 1, -3, 9, 1, 0, 2), natural), "orden"));

        prueba("Lista ya ordenada e invertida", () -> {
            igual(List.of(1, 2, 3, 4), Ordenamiento.mergeSort(List.of(1, 2, 3, 4), natural), "ya ordenada");
            igual(List.of(1, 2, 3, 4), Ordenamiento.mergeSort(List.of(4, 3, 2, 1), natural), "invertida");
        });

        prueba("No modifica la lista original", () -> {
            List<Integer> original = new ArrayList<>(List.of(3, 1, 2));
            Ordenamiento.mergeSort(original, natural);
            igual(List.of(3, 1, 2), original, "original intacta");
        });

        prueba("Es estable (a igual clave conserva el orden de entrada)", () -> {
            List<String> entrada = List.of("b1", "a1", "b2", "a2", "b3", "a3");
            List<String> r = Ordenamiento.mergeSort(entrada, Comparator.comparing(s -> s.charAt(0)));
            igual(List.of("a1", "a2", "a3", "b1", "b2", "b3"), r, "estabilidad");
        });

        prueba("Coincide con Collections.sort en 500 listas aleatorias", () -> {
            Random r = new Random(42);
            for (int i = 0; i < 500; i++) {
                List<Integer> l = new ArrayList<>();
                int n = r.nextInt(60);
                for (int j = 0; j < n; j++) l.add(r.nextInt(20) - 10);
                List<Integer> esperado = new ArrayList<>(l);
                Collections.sort(esperado);
                igual(esperado, Ordenamiento.mergeSort(l, natural), "lista " + l);
            }
        });
    }

    // ============================ Tablero ============================

    private static void pruebasTablero() {
        System.out.println("\n== Tablero (lista, IDs, busqueda binaria) ==");
        Tablero t = new Tablero();
        List<Personaje> ps = t.getPersonajes();

        prueba("Hay exactamente 23 personajes", () -> igual(23, ps.size(), "cantidad"));

        prueba("Las 23 combinaciones de caracteristicas son unicas", () -> {
            Set<String> combos = new HashSet<>();
            for (Personaje p : ps) combos.add(p.getGenero() + p.isCalvicie() + p.isLentes() + p.getColorPelo());
            igual(23, combos.size(), "combinaciones distintas");
        });

        prueba("Los nombres son unicos", () -> igual(23, new HashSet<>(nombres(ps)).size(), "nombres distintos"));

        prueba("IDs autoincrementales 1..23 en el orden de la lista", () -> {
            for (int i = 0; i < ps.size(); i++) igual(i + 1, ps.get(i).getId(), "id en posicion " + i);
        });

        prueba("Ordenados por genero: primero los 11 FEMENINO y luego los 12 MASCULINO", () -> {
            for (int i = 0; i < 11; i++) igual("FEMENINO", ps.get(i).getGenero(), "pos " + i);
            for (int i = 11; i < 23; i++) igual("MASCULINO", ps.get(i).getGenero(), "pos " + i);
        });

        prueba("Dentro de cada genero se respeta el orden de carga (MergeSort estable)", () -> {
            List<String> cargados = nombres(Personaje.crearPersonajes());
            igual(cargados, nombres(ps), "orden");
        });

        prueba("Si la carga viene mezclada, igual queda agrupada por genero", () -> {
            List<Personaje> mezclados = new ArrayList<>(Personaje.crearPersonajes());
            Collections.shuffle(mezclados, new Random(3));
            List<Personaje> ord = Ordenamiento.mergeSort(mezclados, Comparator.comparing(Personaje::getGenero));
            boolean vioMasculino = false;
            for (Personaje p : ord) {
                if (p.getGenero().equals("MASCULINO")) vioMasculino = true;
                else verificar(!vioMasculino, "aparece FEMENINO despues de MASCULINO");
            }
        });

        prueba("La lista del tablero no se puede modificar desde afuera", () ->
                lanza(UnsupportedOperationException.class, () -> ps.remove(0), "remove deberia fallar"));

        prueba("buscarPorId encuentra los 23 IDs", () -> {
            for (int id = 1; id <= 23; id++) igual(id, t.buscarPorId(id).getId(), "id " + id);
        });

        prueba("buscarPorId devuelve null para IDs inexistentes (0, -1, 24, 1000)", () -> {
            for (int id : new int[] {0, -1, 24, 1000, Integer.MAX_VALUE, Integer.MIN_VALUE})
                verificar(t.buscarPorId(id) == null, "id " + id);
        });

        prueba("buscarPorNombre ignora mayusculas y espacios", () -> {
            igual("Eva", t.buscarPorNombre("  eVa ").getNombre(), "eva");
            igual("Walter", t.buscarPorNombre("WALTER").getNombre(), "walter");
        });

        prueba("buscarPorNombre devuelve null para inexistente, vacio o null", () -> {
            verificar(t.buscarPorNombre("Zoe") == null, "Zoe");
            verificar(t.buscarPorNombre("") == null, "vacio");
            verificar(t.buscarPorNombre(null) == null, "null");
        });

        prueba("elegirAlAzar nunca devuelve un excluido (5000 intentos)", () -> {
            Random r = new Random(1);
            for (int i = 0; i < 5000; i++) {
                Personaje a = ps.get(r.nextInt(23));
                Personaje b = ps.get(r.nextInt(23));
                Personaje c = t.elegirAlAzar(r, a, b);
                verificar(c.getId() != a.getId() && c.getId() != b.getId(), "eligio un excluido");
            }
        });

        prueba("elegirAlAzar con 22 excluidos devuelve el unico restante", () -> {
            Personaje[] excluidos = ps.subList(0, 22).toArray(new Personaje[0]);
            igual(23, t.elegirAlAzar(new Random(), excluidos).getId(), "restante");
        });

        prueba("elegirAlAzar tolera excluidos null", () ->
                verificar(t.elegirAlAzar(new Random(), (Personaje) null) != null, "null"));

        prueba("elegirAlAzar puede devolver cualquiera de los 23", () -> {
            Set<Integer> vistos = new HashSet<>();
            Random r = new Random(9);
            for (int i = 0; i < 3000; i++) vistos.add(t.elegirAlAzar(r).getId());
            igual(23, vistos.size(), "cobertura");
        });
    }

    // ============================ Preguntas ============================

    private static void pruebasPreguntas() {
        System.out.println("\n== Preguntas (filtros) ==");
        Tablero t = new Tablero();
        List<Personaje> ps = t.getPersonajes();
        Personaje eva = t.buscarPorNombre("Eva");       // F, no calva, lentes, colorado
        Personaje walter = t.buscarPorNombre("Walter"); // M, calvo, lentes, amarillo

        prueba("Hay 9 preguntas: genero x2, calvicie x2, lentes x2, pelo x3", () -> igual(9, Preguntas.TODAS.size(), "cantidad"));

        prueba("coincide evalua bien cada caracteristica (Eva)", () -> {
            verificar(Preguntas.coincide(eva, "GENERO=FEMENINO"), "F");
            verificar(!Preguntas.coincide(eva, "GENERO=MASCULINO"), "M");
            verificar(Preguntas.coincide(eva, "CALVICIE=NO"), "calv no");
            verificar(!Preguntas.coincide(eva, "CALVICIE=SI"), "calv si");
            verificar(Preguntas.coincide(eva, "LENTES=SI"), "lentes si");
            verificar(!Preguntas.coincide(eva, "LENTES=NO"), "lentes no");
            verificar(Preguntas.coincide(eva, "PELO=COLORADO"), "colorado");
            verificar(!Preguntas.coincide(eva, "PELO=NEGRO"), "negro");
            verificar(!Preguntas.coincide(eva, "PELO=AMARILLO"), "amarillo");
        });

        prueba("coincide evalua bien cada caracteristica (Walter)", () -> {
            verificar(Preguntas.coincide(walter, "GENERO=MASCULINO"), "M");
            verificar(Preguntas.coincide(walter, "CALVICIE=SI"), "calvo");
            verificar(Preguntas.coincide(walter, "LENTES=SI"), "lentes");
            verificar(Preguntas.coincide(walter, "PELO=AMARILLO"), "amarillo");
        });

        prueba("Para cada personaje, preguntas complementarias dan respuestas opuestas", () -> {
            for (Personaje p : ps) {
                verificar(Preguntas.coincide(p, "GENERO=FEMENINO") != Preguntas.coincide(p, "GENERO=MASCULINO"), p.getNombre());
                verificar(Preguntas.coincide(p, "CALVICIE=SI") != Preguntas.coincide(p, "CALVICIE=NO"), p.getNombre());
                verificar(Preguntas.coincide(p, "LENTES=SI") != Preguntas.coincide(p, "LENTES=NO"), p.getNombre());
                int colores = 0;
                for (String c : new String[] {"NEGRO", "COLORADO", "AMARILLO"}) if (Preguntas.coincide(p, "PELO=" + c)) colores++;
                igual(1, colores, "un solo color para " + p.getNombre());
            }
        });

        prueba("coincide con preguntas mal formadas devuelve false (no explota)", () -> {
            verificar(!Preguntas.coincide(eva, "PELO"), "sin =");
            verificar(!Preguntas.coincide(eva, "ALTURA=ALTA"), "caracteristica inexistente");
            verificar(!Preguntas.coincide(eva, "PELO=AZUL"), "valor inexistente");
            verificar(!Preguntas.coincide(eva, "A=B=C"), "dos =");
        });

        prueba("esValida acepta las 9 y rechaza invalidas", () -> {
            for (String q : Preguntas.TODAS) verificar(Preguntas.esValida(q), q);
            for (String q : new String[] {"PELO=AZUL", "pelo=negro", "", "GENERO", "ALTURA=ALTA", "PELO=NEGRO "})
                verificar(!Preguntas.esValida(q), "'" + q + "'");
            verificar(!Preguntas.esValida(null), "null");
        });

        prueba("Cantidades por filtro sobre los 23: F=11 M=12 calvos=11 lentes=12 negro=8 colorado=8 amarillo=7", () -> {
            igual(11, Preguntas.contarCoincidencias(ps, "GENERO=FEMENINO"), "F");
            igual(12, Preguntas.contarCoincidencias(ps, "GENERO=MASCULINO"), "M");
            igual(11, Preguntas.contarCoincidencias(ps, "CALVICIE=SI"), "calvos");
            igual(12, Preguntas.contarCoincidencias(ps, "LENTES=SI"), "lentes");
            igual(8, Preguntas.contarCoincidencias(ps, "PELO=NEGRO"), "negro");
            igual(8, Preguntas.contarCoincidencias(ps, "PELO=COLORADO"), "colorado");
            igual(7, Preguntas.contarCoincidencias(ps, "PELO=AMARILLO"), "amarillo");
        });

        prueba("filtrar SI y filtrar NO forman una particion de los candidatos", () -> {
            for (String q : Preguntas.TODAS) {
                List<Personaje> si = Preguntas.filtrar(ps, q, true);
                List<Personaje> no = Preguntas.filtrar(ps, q, false);
                igual(23, si.size() + no.size(), q);
                for (Personaje p : si) verificar(!no.contains(p), q + " repetido");
            }
        });

        prueba("filtrar no modifica la lista recibida", () -> {
            List<Personaje> copia = new ArrayList<>(ps);
            Preguntas.filtrar(copia, "GENERO=FEMENINO", true);
            igual(23, copia.size(), "tamanio");
        });

        prueba("Filtrar con las 4 caracteristicas del personaje deja solo a ese personaje", () -> {
            for (Personaje p : ps) {
                List<Personaje> c = new ArrayList<>(ps);
                c = Preguntas.filtrar(c, "GENERO=" + p.getGenero(), true);
                c = Preguntas.filtrar(c, "CALVICIE=SI", p.isCalvicie());
                c = Preguntas.filtrar(c, "LENTES=SI", p.isLentes());
                c = Preguntas.filtrar(c, "PELO=" + p.getColorPelo(), true);
                igual(List.of(p.getNombre()), nombres(c), "identificacion de " + p.getNombre());
            }
        });

        prueba("esInformativa: falsa si todos responden igual o no hay candidatos", () -> {
            verificar(Preguntas.esInformativa(ps, "GENERO=FEMENINO"), "23 candidatos");
            List<Personaje> mujeres = Preguntas.filtrar(ps, "GENERO=FEMENINO", true);
            verificar(!Preguntas.esInformativa(mujeres, "GENERO=FEMENINO"), "todas SI");
            verificar(!Preguntas.esInformativa(mujeres, "GENERO=MASCULINO"), "todas NO");
            verificar(!Preguntas.esInformativa(List.of(eva), "PELO=NEGRO"), "un candidato");
            verificar(!Preguntas.esInformativa(new ArrayList<>(), "PELO=NEGRO"), "sin candidatos");
        });
    }

    // ============================ Jugador ============================

    private static void pruebasJugador() {
        System.out.println("\n== Jugador (personaje secreto encapsulado) ==");
        Tablero t = new Tablero();
        Personaje eva = t.buscarPorNombre("Eva");
        Jugador j = new Jugador("Matias", eva);

        prueba("responder coincide con las caracteristicas del secreto", () -> {
            for (String q : Preguntas.TODAS) igual(Preguntas.coincide(eva, q), j.responder(q), q);
        });

        prueba("verificarSuposicion: true solo con el secreto", () -> {
            for (Personaje p : t.getPersonajes()) igual(p.getId() == eva.getId(), j.verificarSuposicion(p), p.getNombre());
            verificar(!j.verificarSuposicion(null), "null");
        });

        prueba("No se puede crear un jugador sin personaje", () ->
                lanza(IllegalArgumentException.class, () -> new Jugador("x", null), "secreto null"));

        prueba("El secreto no es accesible publicamente (solo metodos de Oponente + getNombre)", () -> {
            for (java.lang.reflect.Method m : Jugador.class.getDeclaredMethods()) {
                if (java.lang.reflect.Modifier.isPublic(m.getModifiers())) {
                    verificar(m.getReturnType() != Personaje.class, "metodo publico que devuelve Personaje: " + m.getName());
                }
            }
            for (java.lang.reflect.Field f : Jugador.class.getDeclaredFields()) {
                verificar(java.lang.reflect.Modifier.isPrivate(f.getModifiers()), "campo no privado: " + f.getName());
                verificar(java.lang.reflect.Modifier.isFinal(f.getModifiers()), "campo no final: " + f.getName());
            }
        });

        prueba("Maquina no tiene ningun campo ni parametro de tipo Jugador del rival (solo Oponente)", () -> {
            for (java.lang.reflect.Method m : Maquina.class.getDeclaredMethods()) {
                for (Class<?> tipo : m.getParameterTypes()) {
                    verificar(tipo != Jugador.class, "metodo " + m.getName() + " recibe un Jugador");
                }
            }
        });
    }

    // ============================ Estrategias ============================

    private static void pruebasEstrategias() {
        System.out.println("\n== Estrategias (Greedy y Aleatoria) ==");
        Tablero t = new Tablero();
        List<Personaje> ps = t.getPersonajes();
        EstrategiaGreedy greedy = new EstrategiaGreedy();
        EstrategiaAleatoria azar = new EstrategiaAleatoria(new Random(5));

        prueba("Greedy con 23 candidatos elige una division 11/12", () -> {
            String q = greedy.elegirPregunta(ps, List.of());
            int si = Preguntas.contarCoincidencias(ps, q);
            igual(1, Math.abs(si - (23 - si)), "diferencia de " + q);
        });

        prueba("Greedy siempre elige la pregunta de minima diferencia (fuerza bruta en 2000 subconjuntos)", () -> {
            Random r = new Random(11);
            for (int i = 0; i < 2000; i++) {
                List<Personaje> sub = new ArrayList<>();
                for (Personaje p : ps) if (r.nextBoolean()) sub.add(p);
                if (sub.size() < 2) continue;
                String q = greedy.elegirPregunta(sub, List.of());
                int mejor = Integer.MAX_VALUE;
                for (String o : Preguntas.TODAS) {
                    int si = Preguntas.contarCoincidencias(sub, o);
                    if (si > 0 && si < sub.size()) mejor = Math.min(mejor, Math.abs(2 * si - sub.size()));
                }
                verificar(q != null, "devolvio null con " + sub.size() + " candidatos distintos");
                int si = Preguntas.contarCoincidencias(sub, q);
                igual(mejor, Math.abs(2 * si - sub.size()), "no es la minima para " + nombres(sub));
            }
        });

        prueba("Greedy no repite preguntas ya hechas", () -> {
            String q1 = greedy.elegirPregunta(ps, List.of());
            String q2 = greedy.elegirPregunta(ps, List.of(q1));
            verificar(!q1.equals(q2), "repitio " + q1);
        });

        prueba("Greedy y Aleatoria devuelven null con 1 candidato o sin preguntas utiles", () -> {
            verificar(greedy.elegirPregunta(List.of(ps.get(0)), List.of()) == null, "greedy 1");
            verificar(azar.elegirPregunta(List.of(ps.get(0)), List.of()) == null, "azar 1");
            verificar(greedy.elegirPregunta(ps, Preguntas.TODAS) == null, "greedy todas hechas");
            verificar(azar.elegirPregunta(ps, Preguntas.TODAS) == null, "azar todas hechas");
        });

        prueba("Aleatoria solo elige preguntas utiles y no hechas (3000 intentos)", () -> {
            Random r = new Random(12);
            for (int i = 0; i < 3000; i++) {
                List<Personaje> sub = new ArrayList<>();
                for (Personaje p : ps) if (r.nextBoolean()) sub.add(p);
                List<String> hechas = new ArrayList<>();
                for (String q : Preguntas.TODAS) if (r.nextInt(4) == 0) hechas.add(q);
                String q = azar.elegirPregunta(sub, hechas);
                if (q != null) {
                    verificar(!hechas.contains(q), "repitio " + q);
                    verificar(Preguntas.esInformativa(sub, q), "no informativa " + q);
                }
            }
        });

        prueba("Aleatoria usa todas las preguntas utiles (no siempre la misma)", () -> {
            Set<String> vistas = new HashSet<>();
            for (int i = 0; i < 500; i++) vistas.add(azar.elegirPregunta(ps, List.of()));
            igual(9, vistas.size(), "variedad");
        });

        prueba("explicar describe la eleccion sin fallar", () -> {
            String q = greedy.elegirPregunta(ps, List.of());
            verificar(greedy.explicar(ps, List.of(), q).contains(q), "greedy");
            verificar(azar.explicar(ps, List.of(), q).contains(q), "azar");
        });
    }

    // ============================ Maquina ============================

    private static Maquina m1(Random r) {
        return new Maquina("M1", new EstrategiaAleatoria(r), 1, false, r);
    }

    private static Maquina m2(Random r) {
        return new Maquina("M2", new EstrategiaGreedy(), 2, true, r);
    }

    private static void pruebasMaquina() {
        System.out.println("\n== Maquina ==");
        Tablero t = new Tablero();
        List<Personaje> ps = t.getPersonajes();

        prueba("nuevaPartida: la maquina elige un personaje distinto del excluido y arranca con 23 candidatos", () -> {
            Random r = new Random(2);
            Maquina m = m1(r);
            for (int i = 0; i < 1000; i++) {
                Personaje ex = ps.get(r.nextInt(23));
                m.nuevaPartida(t, ex);
                verificar(m.revelarPersonaje().getId() != ex.getId(), "eligio el excluido");
                igual(23, m.getCandidatos().size(), "candidatos iniciales");
                verificar(m.getHistorial().isEmpty(), "historial limpio");
            }
        });

        prueba("Ambas maquinas encuentran a los 23 personajes, jugando solas, en 6 turnos o menos", () -> {
            for (int semilla = 0; semilla < 40; semilla++) {
                for (Personaje secreto : ps) {
                    for (Maquina m : new Maquina[] {m1(new Random(semilla)), m2(new Random(semilla))}) {
                        m.nuevaPartida(t, secreto);
                        OponenteEspia rival = new OponenteEspia(secreto);
                        int turnos = 0;
                        Jugada j;
                        do {
                            j = m.jugarTurno(rival);
                            turnos++;
                            verificar(turnos <= 6, m.getNombre() + " supero 6 turnos buscando a " + secreto.getNombre());
                            verificar(contiene(m.getCandidatos(), secreto) || j.esGanadora(),
                                    m.getNombre() + " descarto al secreto " + secreto.getNombre());
                        } while (!j.esGanadora());
                        igual(secreto.getId(), j.getSospechoso().getId(), "adivino a otro");
                    }
                }
            }
        });

        prueba("Maquina 1 solo arriesga cuando le queda 1 candidato (nunca falla una suposicion)", () -> {
            Random r = new Random(4);
            for (int i = 0; i < 2000; i++) {
                Maquina m = m1(r);
                Personaje secreto = ps.get(r.nextInt(23));
                m.nuevaPartida(t, secreto);
                OponenteEspia rival = new OponenteEspia(secreto);
                Jugada j;
                do {
                    int antes = m.getCandidatos().size();
                    j = m.jugarTurno(rival);
                    if (j.esSuposicion()) igual(1, antes, "arriesgo con " + antes);
                } while (!j.esGanadora());
                igual(1, rival.suposiciones, "suposiciones");
            }
        });

        prueba("Maquina 2 arriesga con 2 candidatos; si falla descarta y acierta al turno siguiente", () -> {
            Random r = new Random(6);
            int fallos = 0;
            for (int i = 0; i < 2000; i++) {
                Maquina m = m2(r);
                Personaje secreto = ps.get(r.nextInt(23));
                m.nuevaPartida(t, secreto);
                OponenteEspia rival = new OponenteEspia(secreto);
                Jugada j;
                do {
                    int antes = m.getCandidatos().size();
                    j = m.jugarTurno(rival);
                    if (j.esSuposicion()) verificar(antes <= 2, "arriesgo con " + antes);
                    if (j.esSuposicion() && !j.esGanadora()) {
                        fallos++;
                        igual(antes - 1, m.getCandidatos().size(), "no descarto al fallar");
                    }
                } while (!j.esGanadora());
                verificar(rival.suposiciones <= 2, "mas de 2 suposiciones");
            }
            verificar(fallos > 0, "nunca fallo: no esta arriesgando con 2");
        });

        prueba("Las preguntas de la maquina nunca se repiten dentro de una partida", () -> {
            Random r = new Random(8);
            for (int i = 0; i < 2000; i++) {
                Maquina m = i % 2 == 0 ? m1(r) : m2(r);
                Personaje secreto = ps.get(r.nextInt(23));
                m.nuevaPartida(t, secreto);
                OponenteEspia rival = new OponenteEspia(secreto);
                while (!m.jugarTurno(rival).esGanadora()) { }
                Set<String> vistas = new HashSet<>();
                for (RegistroPregunta rp : m.getHistorial()) verificar(vistas.add(rp.getPregunta()), "repitio " + rp.getPregunta());
            }
        });

        prueba("incorporarConocimiento filtra los candidatos y evita repetir esas preguntas", () -> {
            Maquina m = m2(new Random(1));
            Personaje eva = t.buscarPorNombre("Eva");
            m.nuevaPartida(t, eva);
            m.incorporarConocimiento(List.of(new RegistroPregunta("GENERO=FEMENINO", true), new RegistroPregunta("LENTES=SI", true)));
            igual(Arrays.asList("Ana", "Cecilia", "Eva", "Iris", "Julia", "Karina"), nombres(m.getCandidatos()), "candidatos");
            Jugada j = m.jugarTurno(new OponenteEspia(eva));
            verificar(!j.getPregunta().equals("GENERO=FEMENINO") && !j.getPregunta().equals("LENTES=SI"), "repitio heredada");
        });

        prueba("Maquina 2 arriesga antes si el rival ya la tiene identificada", () -> {
            Maquina m = m2(new Random(1));
            m.nuevaPartida(t);
            Personaje propio = m.revelarPersonaje();
            // Preguntas que identifican al personaje propio de la maquina (como si las hubiera hecho el rival)
            List<RegistroPregunta> delRival = List.of(
                    new RegistroPregunta("GENERO=" + propio.getGenero(), true),
                    new RegistroPregunta("CALVICIE=SI", propio.isCalvicie()),
                    new RegistroPregunta("LENTES=SI", propio.isLentes()),
                    new RegistroPregunta("PELO=" + propio.getColorPelo(), true));
            m.observarPreguntasDelRival(delRival, ps);
            Jugada j = m.jugarTurno(new OponenteEspia(ps.get(0).getId() == propio.getId() ? ps.get(1) : ps.get(0)));
            verificar(j.esSuposicion(), "deberia arriesgar con 23 candidatos porque el rival esta por ganar");
        });

        prueba("Maquina 1 ignora las preguntas del rival (no escucha)", () -> {
            Maquina m = m1(new Random(1));
            m.nuevaPartida(t);
            Personaje propio = m.revelarPersonaje();
            m.observarPreguntasDelRival(List.of(
                    new RegistroPregunta("GENERO=" + propio.getGenero(), true),
                    new RegistroPregunta("CALVICIE=SI", propio.isCalvicie()),
                    new RegistroPregunta("LENTES=SI", propio.isLentes()),
                    new RegistroPregunta("PELO=" + propio.getColorPelo(), true)), ps);
            Personaje otro = ps.get(0).getId() == propio.getId() ? ps.get(1) : ps.get(0);
            verificar(!m.jugarTurno(new OponenteEspia(otro)).esSuposicion(), "M1 no deberia arriesgar");
        });

        prueba("La maquina se comporta como Oponente honesto sobre su propio personaje", () -> {
            Maquina m = m1(new Random(3));
            m.nuevaPartida(t);
            Personaje propio = m.revelarPersonaje();
            for (String q : Preguntas.TODAS) igual(Preguntas.coincide(propio, q), m.comoOponente().responder(q), q);
            verificar(m.comoOponente().verificarSuposicion(propio), "suposicion correcta");
        });
    }

    // ============================ MotorJuego: sesion ============================

    private static void pruebasMotorSesion() {
        System.out.println("\n== MotorJuego: sesion ==");

        prueba("No se puede jugar Humano vs Maquina sin iniciar sesion", () -> {
            MotorJuego m = new MotorJuego();
            verificar(!m.haySesion(), "haySesion");
            lanza(IllegalStateException.class, () -> m.nuevaPartidaHumano(1), "sin sesion");
            lanza(IllegalStateException.class, () -> m.preguntarHumano("GENERO=FEMENINO"), "preguntar sin partida");
            lanza(IllegalStateException.class, () -> m.turnoMaquina(), "turno sin partida");
            lanza(IllegalStateException.class, () -> m.arriesgarHumano("Eva"), "arriesgar sin partida");
        });

        prueba("iniciarSesion rechaza IDs inexistentes", () -> {
            MotorJuego m = new MotorJuego();
            for (int id : new int[] {0, 24, -5}) lanza(IllegalArgumentException.class, () -> m.iniciarSesion("x", id), "id " + id);
            verificar(!m.haySesion(), "no deberia quedar sesion");
        });

        prueba("Nombre vacio o null se registra como 'Jugador'; se recortan espacios", () -> {
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("   ", 1);
            igual("Jugador", m.getNombreJugador(), "vacio");
            m.iniciarSesion(null, 1);
            igual("Jugador", m.getNombreJugador(), "null");
            m.iniciarSesion("  Ana  ", 1);
            igual("Ana", m.getNombreJugador(), "trim");
        });

        prueba("El personaje del humano se mantiene en todas las partidas de la sesion", () -> {
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("x", 7);
            for (int i = 0; i < 50; i++) {
                m.nuevaPartidaHumano(1 + (i % 2));
                igual(7, m.getPersonajeHumano().getId(), "cambio el personaje");
            }
        });

        prueba("Cada maquina elige un personaje distinto al del humano y entre si (3000 partidas)", () -> {
            MotorJuego m = new MotorJuego();
            Random r = new Random(10);
            for (int i = 0; i < 3000; i++) {
                m.iniciarSesion("x", 1 + r.nextInt(23));
                m.nuevaPartidaHumano(1 + r.nextInt(2));
                int h = m.getPersonajeHumano().getId();
                int a = m.getMaquina1().revelarPersonaje().getId();
                int b = m.getMaquina2().revelarPersonaje().getId();
                verificar(a != h && b != h && a != b, "repetidos h=" + h + " m1=" + a + " m2=" + b);
            }
        });
    }

    // ============================ MotorJuego: humano ============================

    private static void pruebasMotorHumano() {
        System.out.println("\n== MotorJuego: turnos del humano ==");

        prueba("preguntarHumano normaliza minusculas y espacios", () -> {
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("x", 1);
            m.nuevaPartidaHumano(1);
            boolean esperado = Preguntas.coincide(m.getRival().revelarPersonaje(), "GENERO=FEMENINO");
            igual(esperado, m.preguntarHumano("  genero=femenino "), "respuesta");
            igual("GENERO=FEMENINO", m.getPreguntasHumano().get(0).getPregunta(), "registrada normalizada");
        });

        prueba("preguntarHumano rechaza preguntas invalidas sin consumir el turno", () -> {
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("x", 1);
            m.nuevaPartidaHumano(1);
            for (String q : new String[] {"PELO=AZUL", "", "GENERO", "ALTURA=ALTA"})
                lanza(IllegalArgumentException.class, () -> m.preguntarHumano(q), "'" + q + "'");
            lanza(IllegalArgumentException.class, () -> m.preguntarHumano(null), "null");
            igual(23, m.getCandidatosHumano().size(), "no deberia filtrar");
            igual(0, m.getPreguntasHumano().size(), "no deberia registrar");
        });

        prueba("preguntarHumano rechaza preguntas repetidas", () -> {
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("x", 1);
            m.nuevaPartidaHumano(1);
            m.preguntarHumano("PELO=NEGRO");
            lanza(IllegalArgumentException.class, () -> m.preguntarHumano("pelo=negro"), "repetida");
        });

        prueba("Los filtros del humano nunca descartan el personaje de la maquina", () -> {
            Random r = new Random(13);
            MotorJuego m = new MotorJuego();
            for (int i = 0; i < 1000; i++) {
                m.iniciarSesion("x", 1 + r.nextInt(23));
                m.nuevaPartidaHumano(1 + r.nextInt(2));
                List<String> qs = new ArrayList<>(Preguntas.TODAS);
                Collections.shuffle(qs, r);
                for (String q : qs) {
                    m.preguntarHumano(q);
                    verificar(contiene(m.getCandidatosHumano(), m.getRival().revelarPersonaje()), "descarto al secreto con " + q);
                }
                igual(1, m.getCandidatosHumano().size(), "tras las 9 preguntas queda 1");
            }
        });

        prueba("arriesgarHumano incorrecto descarta al sospechoso y la partida sigue", () -> {
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("x", 1);
            m.nuevaPartidaHumano(1);
            Personaje secreto = m.getRival().revelarPersonaje();
            Personaje otro = m.getPersonajes().stream().filter(p -> p.getId() != secreto.getId() && p.getId() != 1).findFirst().get();
            verificar(!m.arriesgarHumano(otro.getNombre()), "deberia fallar");
            verificar(!contiene(m.getCandidatosHumano(), otro), "no lo descarto");
            verificar(!m.isTerminada(), "no deberia terminar");
        });

        prueba("arriesgarHumano con nombre inexistente lanza error sin terminar la partida", () -> {
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("x", 1);
            m.nuevaPartidaHumano(1);
            lanza(IllegalArgumentException.class, () -> m.arriesgarHumano("Zoe"), "inexistente");
            lanza(IllegalArgumentException.class, () -> m.arriesgarHumano(""), "vacio");
            verificar(!m.isTerminada(), "termino");
        });

        prueba("arriesgarHumano correcto: gana el humano y la partida queda cerrada", () -> {
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("PruebaGanador", 1);
            m.nuevaPartidaHumano(2);
            verificar(m.arriesgarHumano(m.getRival().revelarPersonaje().getNombre().toLowerCase()), "deberia ganar");
            verificar(m.isTerminada(), "terminada");
            igual("PruebaGanador", m.getGanador(), "ganador");
            lanza(IllegalStateException.class, () -> m.preguntarHumano("PELO=NEGRO"), "preguntar tras fin");
            lanza(IllegalStateException.class, () -> m.turnoMaquina(), "turno maquina tras fin");
        });

        prueba("Cuando gana la maquina la partida queda cerrada y el ganador es la maquina", () -> {
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("x", 3);
            m.nuevaPartidaHumano(1);
            Jugada j;
            do { j = m.turnoMaquina(); } while (!j.esGanadora());
            verificar(m.isTerminada(), "terminada");
            igual(m.getRival().getNombre(), m.getGanador(), "ganador");
            igual(3, j.getSospechoso().getId(), "adivino al humano");
            lanza(IllegalStateException.class, () -> m.arriesgarHumano("Ana"), "arriesgar tras fin");
        });

        prueba("La maquina nunca descarta el personaje del humano (invariante, 3000 partidas)", () -> {
            Random r = new Random(14);
            MotorJuego m = new MotorJuego();
            for (int i = 0; i < 3000; i++) {
                m.iniciarSesion("x", 1 + r.nextInt(23));
                m.nuevaPartidaHumano(1 + r.nextInt(2));
                while (!m.isTerminada()) {
                    Jugada j = m.turnoMaquina();
                    if (!j.esGanadora())
                        verificar(contiene(m.getRival().getCandidatos(), m.getPersonajeHumano()), "descarto al humano");
                }
            }
        });

        prueba("Las listas que expone el motor no se pueden modificar", () -> {
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("x", 1);
            m.nuevaPartidaHumano(1);
            lanza(UnsupportedOperationException.class, () -> m.getCandidatosHumano().clear(), "candidatos humano");
            lanza(UnsupportedOperationException.class, () -> m.getRival().getCandidatos().clear(), "candidatos maquina");
            lanza(UnsupportedOperationException.class, () -> m.getPreguntasHumano().clear(), "preguntas humano");
        });
    }

    // ============================ Ventaja Maquina 2 ============================

    private static void pruebasVentajaMaquina2() {
        System.out.println("\n== Ventaja de la Maquina 2 ==");

        prueba("Sin partidas previas contra M1, la Maquina 2 arranca con 23 candidatos", () -> {
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("x", 5);
            m.nuevaPartidaHumano(2);
            igual(23, m.getRival().getCandidatos().size(), "candidatos");
            verificar(m.getRival().getConocimientoHeredado().isEmpty(), "no deberia heredar");
        });

        prueba("La Maquina 2 hereda las preguntas que M1 le hizo al humano y arranca con menos candidatos", () -> {
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("x", 5);
            m.nuevaPartidaHumano(1);
            m.turnoMaquina();
            m.turnoMaquina();
            List<RegistroPregunta> deM1 = new ArrayList<>(m.getPreguntasDeMaquina1AlHumano());
            igual(2, deM1.size(), "preguntas registradas de M1");
            m.nuevaPartidaHumano(2);
            igual(deM1.size(), m.getRival().getConocimientoHeredado().size(), "heredadas");
            verificar(m.getRival().getCandidatos().size() < 23, "no redujo candidatos");
            verificar(contiene(m.getRival().getCandidatos(), m.getPersonajeHumano()), "descarto al humano");
        });

        prueba("Las preguntas de la Maquina 2 no se registran como conocimiento de M1", () -> {
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("x", 5);
            m.nuevaPartidaHumano(2);
            m.turnoMaquina();
            verificar(m.getPreguntasDeMaquina1AlHumano().isEmpty(), "registro preguntas de M2");
        });

        prueba("El conocimiento heredado no tiene preguntas repetidas tras varias partidas contra M1", () -> {
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("x", 5);
            for (int i = 0; i < 10; i++) {
                m.nuevaPartidaHumano(1);
                while (!m.isTerminada()) m.turnoMaquina();
            }
            List<RegistroPregunta> deM1 = m.getPreguntasDeMaquina1AlHumano();
            Set<String> unicas = new HashSet<>();
            for (RegistroPregunta rp : deM1) unicas.add(rp.getPregunta());
            igual(unicas.size(), deM1.size(), "hay preguntas repetidas en el conocimiento de M1 " + deM1);
        });

        prueba("Cambiar de sesion borra el conocimiento de M1 sobre el humano anterior", () -> {
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("x", 5);
            m.nuevaPartidaHumano(1);
            m.turnoMaquina();
            m.iniciarSesion("y", 20);
            verificar(m.getPreguntasDeMaquina1AlHumano().isEmpty(), "no se limpio");
            m.nuevaPartidaHumano(2);
            igual(23, m.getRival().getCandidatos().size(), "M2 no deberia heredar de otra sesion");
        });

        prueba("Con la ventaja, M2 identifica al humano en menos turnos (promedio de 2000 sesiones)", () -> {
            Random r = new Random(15);
            MotorJuego m = new MotorJuego();
            long conVentaja = 0, sinVentaja = 0;
            for (int i = 0; i < 2000; i++) {
                m.iniciarSesion("x", 1 + r.nextInt(23));
                m.nuevaPartidaHumano(2);
                while (!m.isTerminada()) { m.turnoMaquina(); sinVentaja++; }
                m.nuevaPartidaHumano(1);
                m.turnoMaquina();
                m.turnoMaquina();
                m.nuevaPartidaHumano(2);
                while (!m.isTerminada()) { m.turnoMaquina(); conVentaja++; }
            }
            verificar(conVentaja < sinVentaja, "con ventaja " + conVentaja + " vs sin ventaja " + sinVentaja);
        });
    }

    // ============================ MotorJuego: MvM ============================

    private static void pruebasMotorMvM() {
        System.out.println("\n== MotorJuego: Maquina vs Maquina ==");

        prueba("turnoMvM sin partida MvM lanza error", () -> {
            MotorJuego m = new MotorJuego();
            lanza(IllegalStateException.class, () -> m.turnoMvM(), "sin partida");
            m.iniciarSesion("x", 1);
            m.nuevaPartidaHumano(1);
            lanza(IllegalStateException.class, () -> m.turnoMvM(), "en partida humana");
        });

        prueba("En MvM no se pueden usar las acciones del humano", () -> {
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("x", 1);
            m.nuevaPartidaMvM();
            lanza(IllegalStateException.class, () -> m.preguntarHumano("PELO=NEGRO"), "preguntar");
            lanza(IllegalStateException.class, () -> m.arriesgarHumano("Ana"), "arriesgar");
            lanza(IllegalStateException.class, () -> m.turnoMaquina(), "turno maquina");
        });

        prueba("MvM funciona sin sesion de humano y las maquinas tienen personajes distintos", () -> {
            MotorJuego m = new MotorJuego();
            for (int i = 0; i < 2000; i++) {
                m.nuevaPartidaMvM();
                verificar(m.getMaquina1().revelarPersonaje().getId() != m.getMaquina2().revelarPersonaje().getId(), "iguales");
                while (!m.isTerminada()) m.turnoMvM();
            }
        });

        prueba("Cada turno: juega M1 y, si no gano, juega M2; el ganador adivino al rival", () -> {
            MotorJuego m = new MotorJuego();
            for (int i = 0; i < 2000; i++) {
                m.nuevaPartidaMvM();
                List<Jugada> ultimas;
                do {
                    ultimas = m.turnoMvM();
                    igual("Maquina 1", ultimas.get(0).getAutor(), "primero M1");
                    if (ultimas.size() == 2) igual("Maquina 2", ultimas.get(1).getAutor(), "segundo M2");
                    if (ultimas.get(0).esGanadora()) igual(1, ultimas.size(), "M2 jugo despues de que gano M1");
                } while (!m.isTerminada());
                Jugada ganadora = ultimas.get(ultimas.size() - 1);
                verificar(ganadora.esGanadora(), "la ultima jugada no gano");
                igual(ganadora.getAutor(), m.getGanador(), "ganador");
                Maquina perdedora = m.getGanador().equals("Maquina 1") ? m.getMaquina2() : m.getMaquina1();
                igual(perdedora.revelarPersonaje().getId(), ganadora.getSospechoso().getId(), "adivino otro");
                lanza(IllegalStateException.class, () -> m.turnoMvM(), "turno tras fin");
            }
        });

        prueba("MvM no registra preguntas como conocimiento sobre el humano", () -> {
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("x", 1);
            m.nuevaPartidaMvM();
            while (!m.isTerminada()) m.turnoMvM();
            verificar(m.getPreguntasDeMaquina1AlHumano().isEmpty(), "registro preguntas de MvM");
        });
    }

    // ============================ Simulacion masiva ============================

    private static void pruebasSimulacionMasiva() {
        System.out.println("\n== Simulacion masiva ==");

        prueba("20.000 partidas MvM: todas terminan en 6 turnos o menos y M2 gana mas", () -> {
            MotorJuego m = new MotorJuego();
            int ganaM2 = 0;
            for (int i = 0; i < 20000; i++) {
                m.nuevaPartidaMvM();
                while (!m.isTerminada()) {
                    m.turnoMvM();
                    verificar(m.getTurno() <= 6, "mas de 6 turnos");
                }
                if (m.getGanador().equals("Maquina 2")) ganaM2++;
            }
            System.out.printf("          (Maquina 2 gano %.1f%% de las partidas)%n", ganaM2 / 200.0);
            verificar(ganaM2 > 10000, "M2 gano solo " + ganaM2);
        });

        prueba("Humano aleatorio vs maquinas: 5000 partidas terminan con un ganador valido", () -> {
            Random r = new Random(16);
            MotorJuego m = new MotorJuego();
            for (int i = 0; i < 5000; i++) {
                m.iniciarSesion("x", 1 + r.nextInt(23));
                m.nuevaPartidaHumano(1 + r.nextInt(2));
                int acciones = 0;
                while (!m.isTerminada()) {
                    acciones++;
                    verificar(acciones < 60, "partida demasiado larga");
                    List<Personaje> cand = m.getCandidatosHumano();
                    if (r.nextInt(3) == 0 || m.getPreguntasHumano().size() == 9) {
                        if (m.arriesgarHumano(cand.get(r.nextInt(cand.size())).getNombre())) break;
                    } else {
                        List<String> libres = new ArrayList<>(Preguntas.TODAS);
                        for (RegistroPregunta rp : m.getPreguntasHumano()) libres.remove(rp.getPregunta());
                        m.preguntarHumano(libres.get(r.nextInt(libres.size())));
                    }
                    verificar(!cand.isEmpty(), "el humano se quedo sin candidatos");
                    m.turnoMaquina();
                }
                verificar(m.getGanador() != null, "sin ganador");
            }
        });
    }

    // ============================ Marcador ============================

    private static void escribir(String contenido) throws IOException {
        try (FileWriter w = new FileWriter("marcador_record.txt")) {
            w.write(contenido);
        }
    }

    private static void pruebasMarcador() {
        System.out.println("\n== Marcador ==");
        Marcador mc = new Marcador();

        prueba("Sin archivo: marcador vacio y mensaje de que no hay registros", () -> {
            new File("marcador_record.txt").delete();
            verificar(mc.cargarMarcador().isEmpty(), "no vacio");
            verificar(mc.textoRanking().contains("no hay registros"), "mensaje");
        });

        prueba("registrarJugador agrega con 0 y no pisa victorias existentes", () -> {
            escribir("Ana=3\n");
            mc.registrarJugador("Ana");
            mc.registrarJugador("Beto");
            Map<String, Integer> d = mc.cargarMarcador();
            igual(3, d.get("Ana"), "Ana");
            igual(0, d.get("Beto"), "Beto");
        });

        prueba("registrarVictoria suma 1 y persiste en el archivo", () -> {
            escribir("Ana=3\n");
            mc.registrarVictoria("Ana");
            mc.registrarVictoria("Nuevo");
            Map<String, Integer> d = new Marcador().cargarMarcador();
            igual(4, d.get("Ana"), "Ana");
            igual(1, d.get("Nuevo"), "Nuevo");
        });

        prueba("Ignora lineas invalidas o vacias sin perder las validas", () -> {
            escribir("Ana=2\n\nbasura\nBeto=abc\nCarla=5\n");
            Map<String, Integer> d = mc.cargarMarcador();
            igual(2, d.size(), "cantidad");
            igual(5, d.get("Carla"), "Carla");
        });

        prueba("Ranking ordenado de mayor a menor", () -> {
            escribir("A=1\nB=7\nC=3\nD=0\n");
            List<Map.Entry<String, Integer>> rk = mc.obtenerRanking();
            List<Integer> valores = new ArrayList<>();
            for (Map.Entry<String, Integer> e : rk) valores.add(e.getValue());
            igual(List.of(7, 3, 1, 0), valores, "orden");
        });

        prueba("Una victoria del humano en el motor queda en el marcador", () -> {
            escribir("");
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("Ganador", 1);
            m.nuevaPartidaHumano(1);
            m.arriesgarHumano(m.getRival().revelarPersonaje().getNombre());
            igual(1, new Marcador().cargarMarcador().get("Ganador"), "victoria registrada");
        });

        prueba("Una victoria de la maquina NO suma al humano", () -> {
            escribir("");
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("Perdedor", 1);
            m.nuevaPartidaHumano(2);
            while (!m.isTerminada()) m.turnoMaquina();
            igual(0, new Marcador().cargarMarcador().get("Perdedor"), "no deberia sumar");
        });

        prueba("Nombres con '=' se guardan y se vuelven a leer", () -> {
            escribir("");
            mc.registrarVictoria("Juan=Pablo");
            igual(1, new Marcador().cargarMarcador().get("Juan=Pablo"), "nombre con =");
        });

        prueba("Nombres con espacios al final no duplican al jugador", () -> {
            escribir("");
            MotorJuego m = new MotorJuego();
            m.iniciarSesion("Luz ", 1);
            m.nuevaPartidaHumano(1);
            m.arriesgarHumano(m.getRival().revelarPersonaje().getNombre());
            igual(1, mc.cargarMarcador().size(), "entradas");
        });
    }

    // ============================ Consola ============================

    private static String correrConsola(String entrada) {
        InputStream inOriginal = System.in;
        PrintStream outOriginal = System.out;
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
            new JuegoAdivinanzasImpl().ejecutar();
        } finally {
            System.setIn(inOriginal);
            System.setOut(outOriginal);
        }
        return salida.toString(StandardCharsets.UTF_8);
    }

    private static void pruebasConsola() {
        System.out.println("\n== Consola (entradas simuladas) ==");

        prueba("Flujo basico: nombre, ID invalido, ID valido, marcador y salir", () -> {
            String out = correrConsola("Matias\n99\nabc\n5\n4\n6\n");
            verificar(out.contains("No existe un personaje con ID 99"), "no rechazo ID 99");
            verificar(out.contains("Ingrese un numero valido"), "no rechazo 'abc'");
            verificar(out.contains("Tu personaje es Eva"), "no eligio a Eva");
            verificar(out.contains("MARCADOR RECORD"), "no mostro marcador");
        });

        prueba("Opcion de menu invalida no rompe el juego", () -> {
            String out = correrConsola("Matias\n5\n9\n6\n");
            verificar(out.contains("Opcion invalida"), "no aviso");
        });

        prueba("Humano vs Maquina: filtro invalido, valor invalido, pregunta repetida y abandono", () -> {
            String out = correrConsola("Matias\n5\n1\n1\n9\n1\n1\n5\n1\n1\n1\n1\n1\n1\n3\n6\n");
            verificar(out.contains("Filtro invalido"), "no rechazo filtro 9");
            verificar(out.contains("Valor invalido"), "no rechazo valor 5");
            verificar(out.contains("Esa pregunta ya la hiciste"), "no rechazo repetida");
            verificar(out.contains("Partida abandonada"), "no abandono");
        });

        prueba("Humano vs Maquina: arriesgar un nombre inexistente avisa y sigue", () -> {
            String out = correrConsola("Matias\n5\n1\n2\nZoe\n3\n6\n");
            verificar(out.contains("No existe el personaje \"Zoe\""), "no aviso");
        });

        prueba("MvM por consola se simula completo y muestra el razonamiento", () -> {
            String out = correrConsola("Matias\n5\n3\n2\n6\n");
            verificar(out.contains("GANADOR: Maquina"), "sin ganador");
            verificar(out.contains("Greedy evalua"), "sin razonamiento Greedy");
            verificar(out.contains("Elige al azar"), "sin razonamiento M1");
        });

        prueba("Cambiar de jugador reinicia la sesion con otro personaje", () -> {
            String out = correrConsola("Matias\n5\n5\nOtro\n20\n6\n");
            verificar(out.contains("Tu personaje es Tomas"), "no cambio de personaje");
        });

        prueba("Si la entrada se corta (EOF) el programa termina sin excepcion", () -> {
            String out = correrConsola("Matias\n5\n1\n");
            verificar(out.contains("Se cerro la entrada"), "no termino ordenadamente");
            correrConsola("");
        });
    }
}
