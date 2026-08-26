package Implementacion;

import Interfaz.JuegoAdivinanzas;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class JuegoAdivinanzasImpl implements JuegoAdivinanzas {
    private final Marcador marcador = new Marcador();
    private final Random random = new Random();

    private List<Personaje> personajesOrdenados;
    private Personaje secretoHumano;
    private Personaje secretoMaquina1;
    private Personaje secretoMaquina2;
    private final List<String> historialPreguntasMaquina1 = new ArrayList<>();

    @Override
    public void ejecutar() {
        Scanner scanner = new Scanner(System.in);
        String nombreUsuario = pedirNombre(scanner);
        marcador.registrarJugador(nombreUsuario);

        boolean salir = false;
        while (!salir) {
            System.out.println();
            System.out.println("==== JUEGO DE ADIVINANZAS ====");
            System.out.println("1) Humano vs Maquina 1");
            System.out.println("2) Humano vs Maquina 2");
            System.out.println("3) Maquina 1 vs Maquina 2");
            System.out.println("4) Ver marcador");
            System.out.println("5) Salir");
            System.out.print("Ingrese opcion: ");

            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    jugarHumanoVsMaquina(scanner, nombreUsuario, 1);
                    break;
                case 2:
                    jugarHumanoVsMaquina(scanner, nombreUsuario, 2);
                    break;
                case 3:
                    jugarMaquinaVsMaquina();
                    break;
                case 4:
                    mostrarMarcador();
                    break;
                case 5:
                    salir = true;
                    break;
                default:
                    System.out.println("Opcion invalida.");
            }
        }

        scanner.close();
    }

    private String pedirNombre(Scanner scanner) {
        System.out.print("Ingrese su nombre: ");
        return scanner.nextLine();
    }

    private void prepararLista() {
        List<Personaje> personajes = Personaje.crearPersonajes();
        personajesOrdenados = new ArrayList<>(personajes);
        personajesOrdenados.sort(Comparator.comparing(Personaje::getGenero));

        for (int i = 0; i < personajesOrdenados.size(); i++) {
            personajesOrdenados.get(i).setId(i + 1);
        }
    }

    private void jugarHumanoVsMaquina(Scanner scanner, String nombreUsuario, int tipoMaquina) {
        prepararLista();
        historialPreguntasMaquina1.clear();

        System.out.println("La maquina prepara una lista ordenada por genero con 23 personajes.");
        mostrarListaPersonajes();

        secretoHumano = elegirPersonajeHumano(scanner);
        secretoMaquina1 = elegirOtroPersonajeDistinto(secretoHumano);
        secretoMaquina2 = elegirOtroPersonajeDistinto(secretoHumano, secretoMaquina1);

        Personaje secretoMaquinaActual = (tipoMaquina == 1) ? secretoMaquina1 : secretoMaquina2;
        List<Personaje> candidatosHumano = new ArrayList<>(personajesOrdenados);
        List<Personaje> candidatosMaquina = new ArrayList<>(personajesOrdenados);
        List<String> historialPreguntasHumanas = new ArrayList<>();

        System.out.println("Tu personaje queda fijo y no puede cambiarse.");
        System.out.println("Se eligieron personajes secretos para la maquina 1 y la maquina 2.");
        System.out.println("La maquina " + tipoMaquina + " esta intentando adivinar tu secreto.");

        while (true) {
            System.out.println();
            System.out.println("=== TURNO DEL JUGADOR ===");
            System.out.println("1) Hacer filtro");
            System.out.println("2) Hacer suposicion");
            System.out.println("3) Salir al menu");
            System.out.print("Seleccione: ");

            int accion = scanner.nextInt();
            scanner.nextLine();

            if (accion == 1) {
                aplicarFiltroHumano(scanner, candidatosMaquina, historialPreguntasHumanas);
                System.out.println("Candidatos actuales: " + listarNombres(candidatosMaquina));
            } else if (accion == 2) {
                System.out.print("Ingrese el nombre del personaje que cree que eligio la maquina: ");
                String nombreAdivinado = scanner.nextLine();
                if (nombreAdivinado.equalsIgnoreCase(secretoMaquinaActual.getNombre())) {
                    System.out.println("GANASTE. Adivinaste el personaje secreto de la maquina.");
                    marcador.registrarVictoria(nombreUsuario);
                    mostrarMarcador();
                    return;
                } else {
                    System.out.println("No es ese personaje. Sigue intentando.");
                }
            } else if (accion == 3) {
                return;
            }

            if (turnoMaquina(tipoMaquina, candidatosHumano, historialPreguntasHumanas)) {
                System.out.println("La maquina gano. El personaje oculto del humano era: " + secretoHumano.getNombre());
                return;
            }
        }
    }

    private boolean turnoMaquina(int tipoMaquina, List<Personaje> candidatosHumano, List<String> historialPreguntasHumanas) {
        String preguntaElegida = elegirPreguntaMaquina(tipoMaquina, candidatosHumano, historialPreguntasHumanas);
        historialPreguntasHumanas.add(preguntaElegida);

        if (tipoMaquina == 1) {
            historialPreguntasMaquina1.add(preguntaElegida);
        }

        System.out.println();
        System.out.println("La maquina " + tipoMaquina + " realiza la pregunta: " + preguntaElegida);
        aplicarFiltroCandidatos(candidatosHumano, preguntaElegida);
        System.out.println("Quedan " + candidatosHumano.size() + " candidatos posibles.");
        System.out.println("Candidatos actuales de la maquina para tu personaje: " + listarNombres(candidatosHumano));

        if (candidatosHumano.size() == 1 && candidatosHumano.get(0).getId() == secretoHumano.getId()) {
            return true;
        }

        double probabilidadAdivinar = (tipoMaquina == 2) ? 0.85 : 0.55;
        if (candidatosHumano.size() <= 2 && random.nextDouble() < probabilidadAdivinar) {
            System.out.println("La maquina " + tipoMaquina + " intenta adivinar: " + secretoHumano.getNombre());
            return true;
        }

        return false;
    }

    private String elegirPreguntaMaquina(int tipoMaquina, List<Personaje> candidatos, List<String> historialPreguntas) {
        List<String> opciones = new ArrayList<>();
        opciones.add("GENERO=FEMENINO");
        opciones.add("GENERO=MASCULINO");
        opciones.add("CALVICIE=SI");
        opciones.add("CALVICIE=NO");
        opciones.add("LENTES=SI");
        opciones.add("LENTES=NO");
        opciones.add("PELO=NEGRO");
        opciones.add("PELO=COLORADO");
        opciones.add("PELO=AMARILLO");

        List<String> disponibles = new ArrayList<>();
        for (String opcion : opciones) {
            if (!historialPreguntas.contains(opcion)) {
                disponibles.add(opcion);
            }
        }

        if (disponibles.isEmpty()) {
            return opciones.get(random.nextInt(opciones.size()));
        }

        return elegirPreguntaGreedy(disponibles, candidatos);
    }

    private String elegirPreguntaGreedy(List<String> preguntas, List<Personaje> candidatos) {
        String mejorPregunta = preguntas.get(0);
        int mejorDiferencia = Integer.MAX_VALUE;

        for (String pregunta : preguntas) {
            int coincidencias = contarCoincidencias(candidatos, pregunta);
            int diferencia = Math.abs(candidatos.size() - (2 * coincidencias));
            if (diferencia < mejorDiferencia) {
                mejorDiferencia = diferencia;
                mejorPregunta = pregunta;
            }
        }

        return mejorPregunta;
    }

    private int contarCoincidencias(List<Personaje> candidatos, String pregunta) {
        int coincidencias = 0;
        for (Personaje candidato : candidatos) {
            if (coincideConPregunta(candidato, pregunta)) {
                coincidencias++;
            }
        }
        return coincidencias;
    }

    private boolean coincideConPregunta(Personaje personaje, String pregunta) {
        String[] partes = pregunta.split("=");
        if (partes.length != 2) {
            return false;
        }

        String clave = partes[0];
        String valor = partes[1];
        switch (clave) {
            case "GENERO":
                return personaje.getGenero().equalsIgnoreCase(valor);
            case "CALVICIE":
                return (valor.equalsIgnoreCase("SI") && personaje.isCalvicie())
                        || (valor.equalsIgnoreCase("NO") && !personaje.isCalvicie());
            case "LENTES":
                return (valor.equalsIgnoreCase("SI") && personaje.isLentes())
                        || (valor.equalsIgnoreCase("NO") && !personaje.isLentes());
            case "PELO":
                return personaje.getColorPelo().equalsIgnoreCase(valor);
            default:
                return false;
        }
    }

    private void aplicarFiltroHumano(Scanner scanner, List<Personaje> candidatosMaquina, List<String> historialPreguntas) {
        System.out.println("Filtros aplicables:");
        System.out.println("1) Genero");
        System.out.println("2) Calvicie");
        System.out.println("3) Lentes");
        System.out.println("4) Color de pelo");
        System.out.print("Elija filtro: ");

        int filtro = scanner.nextInt();
        scanner.nextLine();

        String valor = "";
        switch (filtro) {
            case 1:
                System.out.print("Ingrese genero (FEMENINO/MASCULINO): ");
                valor = scanner.nextLine().toUpperCase();
                break;
            case 2:
                System.out.print("Tiene calvicie? (SI/NO): ");
                valor = scanner.nextLine().toUpperCase();
                break;
            case 3:
                System.out.print("Usa lentes? (SI/NO): ");
                valor = scanner.nextLine().toUpperCase();
                break;
            case 4:
                System.out.print("Color de pelo? (NEGRO/COLORADO/AMARILLO): ");
                valor = scanner.nextLine().toUpperCase();
                break;
            default:
                System.out.println("Filtro invalido.");
                return;
        }

        String pregunta = construirPregunta(filtro, valor);
        historialPreguntas.add(pregunta);
        aplicarFiltroCandidatos(candidatosMaquina, pregunta);
        System.out.println("La pista aplicada fue: " + pregunta);
    }

    private String construirPregunta(int filtro, String valor) {
        switch (filtro) {
            case 1:
                return "GENERO=" + valor;
            case 2:
                return "CALVICIE=" + valor;
            case 3:
                return "LENTES=" + valor;
            case 4:
                return "PELO=" + valor;
            default:
                return "GENERO=FEMENINO";
        }
    }

    private void aplicarFiltroCandidatos(List<Personaje> candidatos, String pregunta) {
        String[] partes = pregunta.split("=");
        if (partes.length != 2) {
            return;
        }

        String clave = partes[0];
        String valor = partes[1];

        List<Personaje> filtrados = new ArrayList<>();
        for (Personaje personaje : candidatos) {
            boolean coincide = false;
            switch (clave) {
                case "GENERO":
                    coincide = personaje.getGenero().equalsIgnoreCase(valor);
                    break;
                case "CALVICIE":
                    coincide = (valor.equalsIgnoreCase("SI") && personaje.isCalvicie()) || (valor.equalsIgnoreCase("NO") && !personaje.isCalvicie());
                    break;
                case "LENTES":
                    coincide = (valor.equalsIgnoreCase("SI") && personaje.isLentes()) || (valor.equalsIgnoreCase("NO") && !personaje.isLentes());
                    break;
                case "PELO":
                    coincide = personaje.getColorPelo().equalsIgnoreCase(valor);
                    break;
                default:
                    coincide = true;
            }
            if (coincide) {
                filtrados.add(personaje);
            }
        }

        candidatos.clear();
        candidatos.addAll(filtrados);
        String tipoFiltro = valor.equalsIgnoreCase("NO") ? "negativo" : "afirmativo";
        System.out.println("El filtro fue " + tipoFiltro + ".");
    }

    private Personaje elegirPersonajeHumano(Scanner scanner) {

        System.out.print("Ingrese el ID del personaje elegido: ");

        int id = scanner.nextInt();
        scanner.nextLine();

        Personaje personajeEncontrado = buscarPorId(personajesOrdenados, id, 0, personajesOrdenados.size() - 1);
        if (personajeEncontrado != null) {
            return personajeEncontrado;
        }

        System.out.println("ID invalido. Se elige un personaje aleatorio.");
        return personajesOrdenados.get(random.nextInt(personajesOrdenados.size()));
    }

    private Personaje buscarPorId(List<Personaje> personajes, int id, int inicio, int fin) {
        if (inicio > fin) {
            return null;
        }

        int medio = inicio + (fin - inicio) / 2;
        Personaje personajeMedio = personajes.get(medio);
        if (personajeMedio.getId() == id) {
            return personajeMedio;
        }

        if (id < personajeMedio.getId()) {
            return buscarPorId(personajes, id, inicio, medio - 1);
        }
        return buscarPorId(personajes, id, medio + 1, fin);
    }

    private Personaje elegirOtroPersonajeDistinto(Personaje... excluidos) {
        List<Personaje> disponibles = new ArrayList<>(personajesOrdenados);
        for (Personaje excluido : excluidos) {
            if (excluido != null) {
                disponibles.removeIf(p -> p.getId() == excluido.getId());
            }
        }
        return disponibles.get(random.nextInt(disponibles.size()));
    }

    private void mostrarListaPersonajes() {
        System.out.println("Listado de 23 personajes:");
        for (Personaje personaje : personajesOrdenados) {
            System.out.println(personaje.getId() + ") " + personaje.getNombre() + " | genero=" + personaje.getGenero() + " | calvicie=" + personaje.isCalvicie() + " | lentes=" + personaje.isLentes() + " | pelo=" + personaje.getColorPelo());
        }
    }

    private String listarNombres(List<Personaje> lista) {
        List<String> nombres = new ArrayList<>();
        for (Personaje personaje : lista) {
            nombres.add(personaje.getNombre());
        }
        return nombres.toString();
    }

    private void jugarMaquinaVsMaquina() {
        prepararLista();
        historialPreguntasMaquina1.clear();

        secretoMaquina1 = elegirOtroPersonajeDistinto();
        secretoMaquina2 = elegirOtroPersonajeDistinto(secretoMaquina1);

        List<Personaje> candidatosM1 = new ArrayList<>(personajesOrdenados);
        List<Personaje> candidatosM2 = new ArrayList<>(personajesOrdenados);

        System.out.println();
        System.out.println("=== MODO MAQUINA VS MAQUINA ===");
        System.out.println("Se muestran todos los procesos para observar la busqueda de solucion.");
        System.out.println("Maquina 1 oculto: " + secretoMaquina1.getNombre());
        System.out.println("Maquina 2 oculto: " + secretoMaquina2.getNombre());

        int turno = 1;
        while (true) {
            System.out.println();
            System.out.println("--- Turno " + turno + " ---");

            String pregunta1 = elegirPreguntaMaquina(1, candidatosM2, historialPreguntasMaquina1);
            historialPreguntasMaquina1.add(pregunta1);
            System.out.println("Maquina 1 pregunta: " + pregunta1);
            aplicarFiltroCandidatos(candidatosM2, pregunta1);
            System.out.println("Candidatos restantes para M2: " + listarNombres(candidatosM2));

            if (candidatosM2.size() == 1 && candidatosM2.get(0).getId() == secretoMaquina2.getId()) {
                System.out.println("Maquina 1 gano la simulacion porque redujo correctamente el secreto de Maquina 2.");
                return;
            }

            String pregunta2 = elegirPreguntaMaquina(2, candidatosM1, historialPreguntasMaquina1);
            System.out.println("Maquina 2 conoce las preguntas previas de la maquina 1: " + historialPreguntasMaquina1);
            System.out.println("Maquina 2 pregunta: " + pregunta2);
            aplicarFiltroCandidatos(candidatosM1, pregunta2);
            System.out.println("Candidatos restantes para M1: " + listarNombres(candidatosM1));

            if (candidatosM1.size() == 1 && candidatosM1.get(0).getId() == secretoMaquina1.getId()) {
                System.out.println("Maquina 2 gano la simulacion porque redujo correctamente el secreto de Maquina 1.");
                return;
            }

            turno++;
            if (turno > 25) {
                System.out.println("Se alcanzaron muchos turnos y finaliza la simulacion.");
                return;
            }
        }
    }

    @Override
    public void mostrarMarcador() {
        marcador.mostrarMarcador();
    }
}
