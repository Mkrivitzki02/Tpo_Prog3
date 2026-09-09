package Implementacion;

import Interfaz.JuegoAdivinanzas;
import java.util.ArrayList;
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
        private static final List<String> OPCIONES_PREGUNTAS = List.of(
            "GENERO=FEMENINO", "GENERO=MASCULINO", "CALVICIE=SI",
            "CALVICIE=NO", "LENTES=SI", "LENTES=NO", "PELO=NEGRO",
            "PELO=COLORADO", "PELO=AMARILLO");

    public JuegoAdivinanzasImpl() {}

    public void ejecutar() {
        Scanner scanner = new Scanner(System.in);
        String nombreUsuario = this.pedirNombre(scanner);
        this.marcador.registrarJugador(nombreUsuario);
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
            int opcion = this.leerEntero(scanner);
            switch (opcion) {
                case 1:
                    this.jugarHumanoVsMaquina(scanner, nombreUsuario, 1);
                    break;
                case 2:
                    this.jugarHumanoVsMaquina(scanner, nombreUsuario, 2);
                    break;
                case 3:
                    this.jugarMaquinaVsMaquina();
                    break;
                case 4:
                    this.mostrarMarcador();
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

    private int leerEntero(Scanner scanner) {
        // Repite la lectura hasta recibir un numero valido.
        while (true) {
            String entrada = scanner.nextLine().trim();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.print("Ingrese un numero valido: ");
            }
        }
    }

    // Divide y conquista: ordena y luego asigna IDs en el orden resultante.
    private void prepararLista() {
        List<Personaje> personajes = Personaje.crearPersonajes();
        this.personajesOrdenados = mergeSort(personajes);

        for (int i = 0; i < this.personajesOrdenados.size(); ++i) {
            this.personajesOrdenados.get(i).setId(i + 1);
        }
    }

    private List<Personaje> mergeSort(List<Personaje> lista) {
        if (lista.size() <= 1) {
            return lista;
        }
        int medio = lista.size() / 2;
        List<Personaje> izquierda = new ArrayList<>(lista.subList(0, medio));
        List<Personaje> derecha = new ArrayList<>(lista.subList(medio, lista.size()));

        izquierda = mergeSort(izquierda);
        derecha = mergeSort(derecha);

        return combinar(izquierda, derecha);
    }

    // Merge: une dos mitades ya ordenadas conservando su orden.
    private List<Personaje> combinar(List<Personaje> izquierda, List<Personaje> derecha) {
        List<Personaje> resultado = new ArrayList<>();
        int indiceIzquierda = 0;
        int indiceDerecha = 0;
        while (indiceIzquierda < izquierda.size() && indiceDerecha < derecha.size()) {
            if (izquierda.get(indiceIzquierda).getGenero()
                    .compareTo(derecha.get(indiceDerecha).getGenero()) <= 0) {
                resultado.add(izquierda.get(indiceIzquierda++));
            } else {
                resultado.add(derecha.get(indiceDerecha++));
            }
        }
        while (indiceIzquierda < izquierda.size()) {
            resultado.add(izquierda.get(indiceIzquierda++));
        }
        while (indiceDerecha < derecha.size()) {
            resultado.add(derecha.get(indiceDerecha++));
        }
        return resultado;
    }

    private void jugarHumanoVsMaquina(Scanner scanner, String nombreUsuario, int tipoMaquina) {
        this.prepararLista();
        this.historialPreguntasMaquina1.clear();
        System.out.println("La maquina prepara una lista ordenada por genero con 23 personajes usando Divide y Conquista (MergeSort).");
        this.mostrarListaPersonajes();
        this.secretoHumano = this.elegirPersonajeHumano(scanner);
        this.secretoMaquina1 = this.elegirOtroPersonajeDistinto(this.secretoHumano);
        this.secretoMaquina2 = this.elegirOtroPersonajeDistinto(this.secretoHumano, this.secretoMaquina1);
        Personaje secretoMaquinaActual = tipoMaquina == 1 ? this.secretoMaquina1 : this.secretoMaquina2;

        List<Personaje> candidatosHumano = new ArrayList<>(this.personajesOrdenados);
        List<Personaje> candidatosMaquina = new ArrayList<>(this.personajesOrdenados);
        List<String> historialPreguntasHumanas = new ArrayList<>();
        List<String> historialPreguntasMaquina = new ArrayList<>();

        System.out.println("Tu personaje queda fijo y no puede cambiarse.");
        System.out.println("Se eligieron personajes secretos para la maquina.");

        while (true) {
            System.out.println();
            System.out.println("=== TURNO DEL JUGADOR ===");
            System.out.println("1) Hacer filtro");
            System.out.println("2) Hacer suposicion");
            System.out.println("3) Salir al menu");
            System.out.print("Seleccione: ");
            int accion = this.leerEntero(scanner);
            if (accion == 1) {
                this.aplicarFiltroHumano(scanner, candidatosMaquina, historialPreguntasHumanas, secretoMaquinaActual);
                System.out.println("Candidatos actuales: " + this.listarNombres(candidatosMaquina));
            } else if (accion == 2) {
                System.out.print("Ingrese el nombre del personaje que cree que eligio la maquina: ");
                String nombreAdivinado = scanner.nextLine();
                if (nombreAdivinado.equalsIgnoreCase(secretoMaquinaActual.getNombre())) {
                    System.out.println("GANASTE. Adivinaste el personaje secreto de la maquina.");
                    this.marcador.registrarVictoria(nombreUsuario);
                    this.mostrarMarcador();
                    return;
                }
                System.out.println("No es ese personaje. Sigue intentando.");
            } else if (accion == 3) {
                return;
            } else {
                System.out.println("Accion invalida.");
                continue;
            }

            if (this.turnoMaquina(tipoMaquina, candidatosHumano, historialPreguntasMaquina)) {
                break;
            }
        }

        System.out.println("La maquina gano. El personaje oculto del humano era: " + this.secretoHumano.getNombre());
    }

    private boolean turnoMaquina(int tipoMaquina, List<Personaje> candidatosHumano,
            List<String> historialPreguntasMaquina) {
        String preguntaElegida = this.elegirPreguntaGreedy(candidatosHumano, historialPreguntasMaquina);
        historialPreguntasMaquina.add(preguntaElegida);
        if (tipoMaquina == 1) {
            this.historialPreguntasMaquina1.add(preguntaElegida);
        }

        System.out.println();
        System.out.println("La maquina " + tipoMaquina + " realiza la pregunta (Greedy): " + preguntaElegida);
        boolean respuesta = this.coincideConPregunta(this.secretoHumano, preguntaElegida);
        System.out.println("El humano responde: " + (respuesta ? "SI" : "NO"));
        this.aplicarFiltroCandidatos(candidatosHumano, preguntaElegida, respuesta);
        System.out.println("Quedan " + candidatosHumano.size() + " candidatos posibles.");
        System.out.println("Candidatos actuales de la maquina para tu personaje: " + this.listarNombres(candidatosHumano));

        if (candidatosHumano.size() == 1 && candidatosHumano.get(0).getId() == this.secretoHumano.getId()) {
            return true;
        }

        double probabilidadAdivinar = tipoMaquina == 2 ? 0.85 : 0.55;
        if (candidatosHumano.size() <= 2 && this.random.nextDouble() < probabilidadAdivinar) {
            System.out.println("La maquina " + tipoMaquina + " intenta adivinar: " + this.secretoHumano.getNombre());
            return true;
        }
        return false;
    }

    // Greedy: elige la pregunta que divide mejor a los candidatos restantes.
    private String elegirPreguntaGreedy(List<Personaje> candidatos, List<String> historialPreguntas) {
        String mejorPregunta = null;
        int menorDiferencia = Integer.MAX_VALUE;
        int mitad = candidatos.size() / 2;

        for (String opcion : OPCIONES_PREGUNTAS) {
            if (!historialPreguntas.contains(opcion)) {
                int queCumplen = contarCoincidencias(candidatos, opcion);
                int diferencia = Math.abs(queCumplen - mitad);

                if (diferencia < menorDiferencia) {
                    menorDiferencia = diferencia;
                    mejorPregunta = opcion;
                }
            }
        }

        if (mejorPregunta == null) {
            for (String opcion : OPCIONES_PREGUNTAS) {
                if (!historialPreguntas.contains(opcion)) return opcion;
            }
            return OPCIONES_PREGUNTAS.get(0);
        }

        return mejorPregunta;
    }

    private int contarCoincidencias(List<Personaje> candidatos, String pregunta) {
        int contador = 0;

        for (Personaje personaje : candidatos) {
            if (coincideConPregunta(personaje, pregunta)) {
                contador++;
            }
        }
        return contador;
    }

    // Centraliza la interpretación de las cuatro características del juego.
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

    private void aplicarFiltroHumano(Scanner scanner, List<Personaje> candidatosMaquina,
            List<String> historialPreguntas, Personaje secretoMaquina) {
        System.out.println("Filtros aplicables:");
        System.out.println("1) Genero");
        System.out.println("2) Calvicie");
        System.out.println("3) Lentes");
        System.out.println("4) Color de pelo");
        System.out.print("Elija filtro: ");
        int filtro = this.leerEntero(scanner);
        String valor = this.leerValorFiltro(scanner, filtro);
        if (valor == null) {
            System.out.println("Filtro invalido.");
            return;
        }

        String pregunta = this.construirPregunta(filtro, valor);
        historialPreguntas.add(pregunta);
        boolean respuesta = this.coincideConPregunta(secretoMaquina, pregunta);
        System.out.println("La maquina responde: " + (respuesta ? "SI" : "NO"));
        this.aplicarFiltroCandidatos(candidatosMaquina, pregunta, respuesta);
        System.out.println("La pregunta realizada fue: " + pregunta);
    }

    private String leerValorFiltro(Scanner scanner, int filtro) {
        String mensaje;
        switch (filtro) {
            case 1:
                mensaje = "Ingrese genero (FEMENINO/MASCULINO): ";
                break;
            case 2:
                mensaje = "Tiene calvicie? (SI/NO): ";
                break;
            case 3:
                mensaje = "Usa lentes? (SI/NO): ";
                break;
            case 4:
                mensaje = "Color de pelo? (NEGRO/COLORADO/AMARILLO): ";
                break;
            default:
                return null;
        }
        System.out.print(mensaje);
        return scanner.nextLine().toUpperCase();
    }

    private String construirPregunta(int filtro, String valor) {
        return switch (filtro) {
            case 1 -> "GENERO=" + valor;
            case 2 -> "CALVICIE=" + valor;
            case 3 -> "LENTES=" + valor;
            case 4 -> "PELO=" + valor;
            default -> "GENERO=FEMENINO";
        };
    }

    private void aplicarFiltroCandidatos(List<Personaje> candidatos, String pregunta) {
        aplicarFiltroCandidatos(candidatos, pregunta, true);
    }

    private void aplicarFiltroCandidatos(List<Personaje> candidatos, String pregunta, boolean respuestaEsperada) {
        // Conserva coincidencias o descartes segun la respuesta recibida.
        String[] partes = pregunta.split("=");
        if (partes.length != 2) {
            return;
        }

        List<Personaje> filtrados = new ArrayList<>();
        for (Personaje personaje : candidatos) {
            if (this.coincideConPregunta(personaje, pregunta) == respuestaEsperada) {
                filtrados.add(personaje);
            }
        }
        candidatos.clear();
        candidatos.addAll(filtrados);
        System.out.println("Se conservaron los candidatos compatibles con la respuesta.");
    }

    private Personaje elegirPersonajeHumano(Scanner scanner) {
        System.out.print("Ingrese el ID del personaje elegido: ");
        int id = this.leerEntero(scanner);

        for (Personaje personaje : this.personajesOrdenados) {
            if (personaje.getId() == id) {
                return personaje;
            }
        }

        System.out.println("ID invalido. Se elige un personaje aleatorio.");
        return this.personajesOrdenados.get(this.random.nextInt(this.personajesOrdenados.size()));
    }

    private Personaje elegirOtroPersonajeDistinto(Personaje... excluidos) {
        List<Personaje> disponibles = new ArrayList<>(this.personajesOrdenados);
        for (Personaje excluido : excluidos) {
            if (excluido != null) {
                disponibles.removeIf(p -> p.getId() == excluido.getId());
            }
        }
        return disponibles.get(this.random.nextInt(disponibles.size()));
    }

    private void mostrarListaPersonajes() {
        System.out.println("Listado de 23 personajes:");
        for (Personaje personaje : this.personajesOrdenados) {
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
        this.prepararLista();
        this.historialPreguntasMaquina1.clear();
        this.secretoMaquina1 = this.elegirOtroPersonajeDistinto();
        this.secretoMaquina2 = this.elegirOtroPersonajeDistinto(this.secretoMaquina1);
        List<Personaje> candidatosM1 = new ArrayList<>(this.personajesOrdenados);
        List<Personaje> candidatosM2 = new ArrayList<>(this.personajesOrdenados);
        System.out.println();
        System.out.println("=== MODO MAQUINA VS MAQUINA ===");
        System.out.println("Se muestran todos los procesos utilizando Greedy para optimizar las preguntas.");
        System.out.println("Maquina 1 oculto: " + this.secretoMaquina1.getNombre());
        System.out.println("Maquina 2 oculto: " + this.secretoMaquina2.getNombre());

        int turno = 1;
        // Cada maquina evita repetir sus propias preguntas.
        List<String> historialPreguntasMaquina2 = new ArrayList<>();

        do {
            System.out.println();
            System.out.println("--- Turno " + turno + " ---");
            String pregunta1 = this.elegirPreguntaGreedy(candidatosM2, this.historialPreguntasMaquina1);
            this.historialPreguntasMaquina1.add(pregunta1);
            System.out.println("Maquina 1 pregunta (Greedy): " + pregunta1);
            boolean respuesta1 = this.coincideConPregunta(this.secretoMaquina2, pregunta1);
            System.out.println("Maquina 2 responde: " + (respuesta1 ? "SI" : "NO"));
            this.aplicarFiltroCandidatos(candidatosM2, pregunta1, respuesta1);
            System.out.println("Candidatos restantes para M2: " + this.listarNombres(candidatosM2));

            if (candidatosM2.size() == 1 && candidatosM2.get(0).getId() == this.secretoMaquina2.getId()) {
                System.out.println("Maquina 1 gano la simulacion porque redujo correctamente el secreto de Maquina 2.");
                return;
            }

            String pregunta2 = this.elegirPreguntaGreedy(candidatosM1, historialPreguntasMaquina2);
            historialPreguntasMaquina2.add(pregunta2);
            System.out.println("Maquina 2 pregunta (Greedy): " + pregunta2);
            boolean respuesta2 = this.coincideConPregunta(this.secretoMaquina1, pregunta2);
            System.out.println("Maquina 1 responde: " + (respuesta2 ? "SI" : "NO"));
            this.aplicarFiltroCandidatos(candidatosM1, pregunta2, respuesta2);
            System.out.println("Candidatos restantes para M1: " + this.listarNombres(candidatosM1));

            if (candidatosM1.size() == 1 && candidatosM1.get(0).getId() == this.secretoMaquina1.getId()) {
                System.out.println("Maquina 2 gano la simulacion porque redujo correctamente el secreto de Maquina 1.");
                return;
            }

            ++turno;
        } while (turno <= 25);

        System.out.println("Se alcanzaron muchos turnos y finaliza la simulacion.");
    }

    public void mostrarMarcador() {
        this.marcador.mostrarMarcador();
    }
}