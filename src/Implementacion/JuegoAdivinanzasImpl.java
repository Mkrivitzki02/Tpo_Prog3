package Implementacion;

import Interfaz.JuegoAdivinanzas;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Version por consola. Toda la logica del juego esta en MotorJuego; esta clase solo
 * lee lo que ingresa el usuario y muestra los resultados.
 */
public class JuegoAdivinanzasImpl implements JuegoAdivinanzas {
    private final MotorJuego motor = new MotorJuego();
    private Scanner scanner;

    @Override
    public void ejecutar() {
        scanner = new Scanner(System.in);
        try {
            iniciarSesion();
            menuPrincipal();
        } catch (FinDeEntrada e) {
            System.out.println();
            System.out.println("Se cerro la entrada. Fin del juego.");
        }
    }

    private void menuPrincipal() {
        boolean salir = false;

        while (!salir) {
            System.out.println();
            System.out.println("==== JUEGO DE ADIVINANZAS ====");
            System.out.println("Jugador: " + motor.getNombreJugador() + " | Tu personaje: "
                    + motor.getPersonajeHumano().getNombre());
            System.out.println("1) Humano vs Maquina 1");
            System.out.println("2) Humano vs Maquina 2");
            System.out.println("3) Maquina 1 vs Maquina 2");
            System.out.println("4) Ver marcador");
            System.out.println("5) Cambiar de jugador / personaje");
            System.out.println("6) Salir");
            System.out.print("Ingrese opcion: ");
            switch (leerEntero()) {
                case 1 -> jugarHumanoVsMaquina(1);
                case 2 -> jugarHumanoVsMaquina(2);
                case 3 -> jugarMaquinaVsMaquina();
                case 4 -> mostrarMarcador();
                case 5 -> iniciarSesion();
                case 6 -> salir = true;
                default -> System.out.println("Opcion invalida.");
            }
        }
    }

    @Override
    public void mostrarMarcador() {
        motor.getMarcador().mostrarMarcador();
    }

    // ===================== Sesion =====================

    private void iniciarSesion() {
        System.out.print("Ingrese su nombre: ");
        String nombre = leerLinea();
        System.out.println();
        System.out.println("La maquina ordeno los 23 personajes por genero (MergeSort) y les asigno un ID autoincremental:");
        mostrarLista(motor.getPersonajes());
        while (true) {
            System.out.print("Ingrese el ID de su personaje (queda fijo durante toda la sesion): ");
            int id = leerEntero();
            try {
                motor.iniciarSesion(nombre, id); // busca el ID con busqueda binaria
                break;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("Tu personaje es " + motor.getPersonajeHumano().descripcion() + ".");
    }

    // ===================== Humano vs Maquina =====================

    private void jugarHumanoVsMaquina(int numeroMaquina) {
        motor.nuevaPartidaHumano(numeroMaquina);
        Maquina rival = motor.getRival();
        System.out.println();
        System.out.println("=== " + motor.getNombreJugador() + " vs " + rival.getNombre() + " ===");
        System.out.println(rival.describirEstilo() + ".");
        System.out.println(rival.getNombre() + " eligio su personaje secreto.");
        if (numeroMaquina == 2) {
            List<RegistroPregunta> heredado = rival.getConocimientoHeredado();
            if (heredado.isEmpty()) {
                System.out.println("La Maquina 1 todavia no te hizo preguntas en esta sesion: la Maquina 2 arranca sin ventaja.");
            } else {
                System.out.println("Ventaja: la Maquina 2 conoce las preguntas que te hizo la Maquina 1 " + heredado
                        + " y arranca con " + rival.getCandidatos().size() + " candidatos.");
            }
        }

        while (!motor.isTerminada()) {
            System.out.println();
            System.out.println("=== TURNO " + motor.getTurno() + " - JUGADOR === (tu personaje: "
                    + motor.getPersonajeHumano().getNombre() + ")");
            System.out.println("Tus candidatos (" + motor.getCandidatosHumano().size() + "): "
                    + nombres(motor.getCandidatosHumano()));
            System.out.println("1) Hacer una pregunta (filtro)");
            System.out.println("2) Arriesgar un personaje");
            System.out.println("3) Abandonar y volver al menu");
            System.out.print("Seleccione: ");
            int accion = leerEntero();
            if (accion == 3) {
                System.out.println("Partida abandonada. El personaje de " + rival.getNombre() + " era "
                        + rival.revelarPersonaje().getNombre() + ".");
                return;
            }
            if (accion == 1) {
                String pregunta = leerPregunta();
                if (pregunta == null) {
                    continue;
                }
                try {
                    boolean respuesta = motor.preguntarHumano(pregunta);
                    System.out.println(rival.getNombre() + " responde: " + (respuesta ? "SI" : "NO")
                            + ". Te quedan " + motor.getCandidatosHumano().size() + " candidatos.");
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                    continue;
                }
            } else if (accion == 2) {
                System.out.print("Nombre del personaje que crees que eligio la maquina: ");
                String nombre = leerLinea();
                try {
                    if (motor.arriesgarHumano(nombre)) {
                        System.out.println("GANASTE. Adivinaste el personaje de " + rival.getNombre() + ".");
                        mostrarMarcador();
                        return;
                    }
                    System.out.println("No es " + nombre.trim() + ". Lo descartas de tus candidatos.");
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                    continue;
                }
            } else {
                System.out.println("Accion invalida.");
                continue;
            }

            System.out.println();
            System.out.println("--- Turno de " + rival.getNombre() + " ---");
            Jugada jugada = motor.turnoMaquina();
            System.out.println(jugada.resumen());
            if (jugada.esGanadora()) {
                System.out.println(rival.getNombre() + " GANO: adivino tu personaje. El suyo era "
                        + rival.revelarPersonaje().getNombre() + ".");
            }
        }
    }

    private String leerPregunta() {
        System.out.println("Filtros aplicables: 1) Genero  2) Calvicie  3) Lentes  4) Color de pelo");
        System.out.print("Elija filtro: ");
        int filtro = leerEntero();
        String[] valores;
        String caracteristica;
        switch (filtro) {
            case 1 -> { caracteristica = "GENERO"; valores = new String[] {"FEMENINO", "MASCULINO"}; }
            case 2 -> { caracteristica = "CALVICIE"; valores = new String[] {"SI", "NO"}; }
            case 3 -> { caracteristica = "LENTES"; valores = new String[] {"SI", "NO"}; }
            case 4 -> { caracteristica = "PELO"; valores = new String[] {"NEGRO", "COLORADO", "AMARILLO"}; }
            default -> {
                System.out.println("Filtro invalido.");
                return null;
            }
        }
        for (int i = 0; i < valores.length; i++) {
            System.out.print((i + 1) + ") " + valores[i] + "  ");
        }
        System.out.print("\nElija valor: ");
        int opcion = leerEntero();
        if (opcion < 1 || opcion > valores.length) {
            System.out.println("Valor invalido.");
            return null;
        }
        return caracteristica + "=" + valores[opcion - 1];
    }

    // ===================== Maquina vs Maquina =====================

    private void jugarMaquinaVsMaquina() {
        motor.nuevaPartidaMvM();
        Maquina m1 = motor.getMaquina1();
        Maquina m2 = motor.getMaquina2();
        System.out.println();
        System.out.println("=== MODO MAQUINA VS MAQUINA ===");
        System.out.println(m1.describirEstilo() + ".");
        System.out.println(m2.describirEstilo() + ".");
        System.out.println("(Vista de espectador) Maquina 1 eligio a " + m1.revelarPersonaje().getNombre()
                + " y Maquina 2 eligio a " + m2.revelarPersonaje().getNombre() + ".");
        System.out.print("1) Ver turno por turno (Enter para avanzar)  2) Ver toda la simulacion: ");
        boolean pasoAPaso = leerEntero() == 1;

        while (!motor.isTerminada()) {
            System.out.println();
            System.out.println("--- Turno " + motor.getTurno() + " ---");
            for (Jugada jugada : motor.turnoMvM()) {
                System.out.println("  Razonamiento: " + jugada.getRazonamiento());
                System.out.println("  " + jugada.resumen());
            }
            System.out.println("  Candidatos de Maquina 1 (" + m1.getCandidatos().size() + "): " + nombres(m1.getCandidatos()));
            System.out.println("  Candidatos de Maquina 2 (" + m2.getCandidatos().size() + "): " + nombres(m2.getCandidatos()));
            if (pasoAPaso && !motor.isTerminada()) {
                System.out.print("[Enter para el siguiente turno]");
                leerLinea();
            }
        }
        System.out.println();
        System.out.println("GANADOR: " + motor.getGanador() + " en el turno " + motor.getTurno() + ".");
    }

    // ===================== Utilidades =====================

    /** Se lanza cuando la entrada estandar se termina (por ejemplo Ctrl+D o un archivo redirigido). */
    private static class FinDeEntrada extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private String leerLinea() {
        if (!scanner.hasNextLine()) {
            throw new FinDeEntrada();
        }
        return scanner.nextLine();
    }

    private int leerEntero() {
        while (true) {
            String entrada = leerLinea().trim();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.print("Ingrese un numero valido: ");
            }
        }
    }

    private void mostrarLista(List<Personaje> personajes) {
        for (Personaje p : personajes) {
            System.out.printf("%2d) %-10s | genero=%-9s | calvicie=%-2s | lentes=%-2s | pelo=%s%n",
                    p.getId(), p.getNombre(), p.getGenero(), p.isCalvicie() ? "SI" : "NO",
                    p.isLentes() ? "SI" : "NO", p.getColorPelo());
        }
    }

    private String nombres(List<Personaje> lista) {
        List<String> nombres = new ArrayList<>();
        for (Personaje personaje : lista) {
            nombres.add(personaje.getNombre());
        }
        return nombres.toString();
    }
}
