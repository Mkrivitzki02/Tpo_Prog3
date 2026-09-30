package Implementacion;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Interfaz grafica (Swing). No contiene logica de juego: todas las reglas y algoritmos
 * estan en MotorJuego, igual que para la version de consola.
 */
public class Gui extends JFrame {
    private final MotorJuego motor = new MotorJuego();

    // GUI
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenedor = new JPanel(cardLayout);
    private JTextField txtNombreJugador;
    private JLabel lblSesion, lblModoTitulo, lblMiPersonajeInfo, lblContadorCandidatos, lblJugadaHumano, lblJugadaMaquina;
    private JTextArea txtLog;
    private TitledBorder bordeGrilla;
    private final JPanel panelGrilla = new JPanel(new GridLayout(3, 8, 6, 6));
    private final Map<Integer, JPanel> tarjetas = new HashMap<>();
    private final Map<String, JButton> mapaBotones = new HashMap<>();
    private JButton btnAdivinar, btnSaltear, btnTurnoMvM, btnAutoMvM;

    public Gui() {
        super("¿Quién es Quién? - Juego de Adivinanzas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1020, 820);
        setLocationRelativeTo(null);

        panelContenedor.add(crearMenu(), "MENU");
        panelContenedor.add(crearTableroJuego(), "JUEGO");
        setContentPane(panelContenedor);
        construirGrilla();
    }

    // ================= 1. MENÚ PRINCIPAL =================
    private JPanel crearMenu() {
        JPanel menu = new JPanel(new GridBagLayout());
        menu.setBackground(new Color(240, 245, 250));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 12, 8, 12);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;

        JLabel tit = new JLabel("🎯 ¿QUIÉN ES QUIÉN?", SwingConstants.CENTER);
        tit.setFont(new Font("SansSerif", Font.BOLD, 34));
        tit.setForeground(new Color(24, 60, 120));

        JPanel pNom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pNom.setOpaque(false);
        pNom.add(new JLabel("Ingresa tu Nombre: "));
        txtNombreJugador = new JTextField("Jugador", 14);
        pNom.add(txtNombreJugador);

        lblSesion = new JLabel("Todavía no elegiste personaje.", SwingConstants.CENTER);
        lblSesion.setForeground(new Color(0, 110, 70));

        JButton b1 = botonMenu("👤 Jugar: Humano vs Máquina 1 (menos asertiva)", e -> iniciarPartida(1));
        JButton b2 = botonMenu("🤖 Jugar: Humano vs Máquina 2 (más asertiva)", e -> iniciarPartida(2));
        JButton b3 = botonMenu("⚔️ Simulación: Máquina vs Máquina", e -> iniciarPartidaMvM());
        JButton b4 = botonMenu("🏆 Ver Marcador de Victorias", e -> mostrarMarcador());
        JButton b5 = botonMenu("🔄 Cambiar Jugador / Personaje", e -> elegirPersonaje());
        JButton b6 = botonMenu("❌ Salir del Juego", e -> System.exit(0));

        JComponent[] comps = {tit, pNom, lblSesion, b1, b2, b3, b4, b5, b6};
        for (int i = 0; i < comps.length; i++) {
            g.gridy = i;
            menu.add(comps[i], g);
        }
        return menu;
    }

    private JButton botonMenu(String txt, java.awt.event.ActionListener al) {
        JButton b = new JButton(txt);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setPreferredSize(new Dimension(420, 42));
        b.setFocusPainted(false);
        b.addActionListener(al);
        return b;
    }

    // ================= 2. PANTALLA DE JUEGO =================
    private JPanel crearTableroJuego() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(8, 10, 8, 10));
        panel.setBackground(new Color(245, 247, 250));

        // Cabecera superior
        JPanel sup = new JPanel(new BorderLayout(10, 5));
        sup.setBackground(new Color(230, 238, 248));
        sup.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(180, 205, 235), 1, true), new EmptyBorder(8, 12, 8, 12)));

        JButton btnVolver = new JButton("🔙 Menú");
        btnVolver.addActionListener(e -> volverAlMenu());

        JPanel pCentro = new JPanel(new GridLayout(2, 1, 2, 2));
        pCentro.setOpaque(false);
        lblModoTitulo = new JLabel("MODO: Humano vs Máquina", SwingConstants.CENTER);
        lblModoTitulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblModoTitulo.setForeground(new Color(15, 45, 95));
        lblMiPersonajeInfo = new JLabel("Tu Personaje: -", SwingConstants.CENTER);
        lblMiPersonajeInfo.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblMiPersonajeInfo.setForeground(new Color(0, 110, 70));
        pCentro.add(lblModoTitulo);
        pCentro.add(lblMiPersonajeInfo);

        JPanel pDer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pDer.setOpaque(false);
        lblContadorCandidatos = new JLabel("Candidatos: 23");
        lblContadorCandidatos.setFont(new Font("SansSerif", Font.BOLD, 12));
        JButton btnMarc = new JButton("🏆 Marcador");
        btnMarc.addActionListener(e -> mostrarMarcador());
        pDer.add(lblContadorCandidatos);
        pDer.add(btnMarc);

        sup.add(btnVolver, BorderLayout.WEST);
        sup.add(pCentro, BorderLayout.CENTER);
        sup.add(pDer, BorderLayout.EAST);

        // Grilla de tarjetas de candidatos (Medio)
        panelGrilla.setBackground(Color.WHITE);
        panelGrilla.setBorder(new EmptyBorder(6, 6, 6, 6));
        JScrollPane scroll = new JScrollPane(panelGrilla);
        bordeGrilla = new TitledBorder(new LineBorder(Color.LIGHT_GRAY), "Tablero de Candidatos (Tus Sospechas)");
        scroll.setBorder(bordeGrilla);
        scroll.setPreferredSize(new Dimension(950, 240));

        // Panel de interacción y registro de procesos (Debajo de candidatos)
        JPanel pResp = new JPanel(new BorderLayout(4, 4));
        pResp.setBorder(new TitledBorder(new LineBorder(new Color(200, 210, 225)), "Últimas Respuestas y Registro de Procesos"));
        pResp.setBackground(Color.WHITE);
        JPanel pLabels = new JPanel(new GridLayout(2, 1, 4, 4));
        pLabels.setOpaque(false);
        lblJugadaHumano = new JLabel("👉 Tu jugada: Aún no has realizado preguntas.");
        lblJugadaMaquina = new JLabel("🤖 Jugada de la Máquina: Esperando inicio de turno...");
        lblJugadaMaquina.setForeground(new Color(110, 30, 30));
        pLabels.add(lblJugadaHumano);
        pLabels.add(lblJugadaMaquina);
        txtLog = new JTextArea(7, 80);
        txtLog.setEditable(false);
        txtLog.setLineWrap(true);
        txtLog.setWrapStyleWord(true);
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        pResp.add(pLabels, BorderLayout.NORTH);
        pResp.add(new JScrollPane(txtLog), BorderLayout.CENTER);

        // Botonera de acciones (Abajo de todo)
        JPanel pInf = new JPanel(new BorderLayout(5, 5));
        pInf.setBorder(new TitledBorder(new LineBorder(Color.LIGHT_GRAY), "Botonera de Acciones (Tus Preguntas)"));
        pInf.setBackground(new Color(245, 247, 250));

        JPanel pBotones = new JPanel(new GridLayout(2, 5, 5, 5));
        pBotones.setOpaque(false);
        String[][] ops = {
                {"♀ Femenino", "GENERO=FEMENINO"}, {"♂ Masculino", "GENERO=MASCULINO"},
                {"👨‍🦲 Calvo: SÍ", "CALVICIE=SI"}, {"💇 Calvo: NO", "CALVICIE=NO"},
                {"👓 Lentes: SÍ", "LENTES=SI"}, {"👀 Lentes: NO", "LENTES=NO"},
                {"⬛ Pelo Negro", "PELO=NEGRO"}, {"🟥 Pelo Colorado", "PELO=COLORADO"},
                {"🟨 Pelo Amarillo", "PELO=AMARILLO"}
        };
        for (String[] o : ops) {
            JButton b = new JButton(o[0]);
            b.setFocusPainted(false);
            b.addActionListener(e -> realizarPreguntaHumano(o[1]));
            mapaBotones.put(o[1], b);
            pBotones.add(b);
        }

        btnAdivinar = new JButton("🎯 Arriesgar / Adivinar");
        btnAdivinar.setBackground(new Color(210, 245, 215));
        btnAdivinar.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnAdivinar.addActionListener(e -> realizarSuposicion());
        pBotones.add(btnAdivinar);

        JPanel pExtra = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 2));
        pExtra.setOpaque(false);
        btnSaltear = new JButton("⏭️ Saltear / Pasar Turno");
        btnSaltear.addActionListener(e -> {
            if (motor.isTerminada()) return;
            lblJugadaHumano.setText("👉 Decidiste saltear tu turno.");
            log("Turno " + motor.getTurno() + " - " + motor.getNombreJugador() + " pasa el turno.");
            ejecutarTurnoMaquina();
        });
        btnTurnoMvM = new JButton("▶ Avanzar 1 Turno (MvM)");
        btnAutoMvM = new JButton("⏩ Simular Todo (MvM)");
        btnTurnoMvM.addActionListener(e -> ejecutarTurnoMvM());
        btnAutoMvM.addActionListener(e -> simularTodoMvM());
        pExtra.add(btnSaltear);
        pExtra.add(new JSeparator(JSeparator.VERTICAL));
        pExtra.add(btnTurnoMvM);
        pExtra.add(btnAutoMvM);

        pInf.add(pBotones, BorderLayout.CENTER);
        pInf.add(pExtra, BorderLayout.SOUTH);

        JPanel pCentroCuerpo = new JPanel(new BorderLayout(5, 5));
        pCentroCuerpo.setOpaque(false);
        pCentroCuerpo.add(scroll, BorderLayout.CENTER);
        pCentroCuerpo.add(pResp, BorderLayout.SOUTH);

        panel.add(sup, BorderLayout.NORTH);
        panel.add(pCentroCuerpo, BorderLayout.CENTER);
        panel.add(pInf, BorderLayout.SOUTH);
        return panel;
    }

    // ================= 3. TARJETAS Y GRID =================
    private void construirGrilla() {
        panelGrilla.removeAll();
        tarjetas.clear();
        for (Personaje p : motor.getPersonajes()) {
            JPanel t = new JPanel(new GridLayout(3, 1));
            t.setBorder(new LineBorder(new Color(180, 200, 220), 1, true));
            t.setBackground(Color.WHITE);
            t.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel lNom = new JLabel("#" + p.getId() + " " + p.getNombre(), SwingConstants.CENTER);
            lNom.setFont(new Font("SansSerif", Font.BOLD, 12));
            lNom.setForeground(new Color(20, 50, 90));
            String gen = p.getGenero().equalsIgnoreCase("FEMENINO") ? "♀ Fem" : "♂ Masc";
            JLabel lD1 = new JLabel(gen + " | " + (p.isCalvicie() ? "Calvo" : "Pelo"), SwingConstants.CENTER);
            lD1.setFont(new Font("SansSerif", Font.PLAIN, 10));
            JLabel lD2 = new JLabel((p.isLentes() ? "👓 " : "👀 ") + p.getColorPelo(), SwingConstants.CENTER);
            lD2.setFont(new Font("SansSerif", Font.PLAIN, 10));

            t.add(lNom); t.add(lD1); t.add(lD2);
            t.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (esPartidaHumana() && !motor.isTerminada() && motor.getCandidatosHumano().contains(p)) {
                        if (JOptionPane.showConfirmDialog(Gui.this, "¿Arriesgar que es " + p.getNombre() + "?", "Adivinar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                            procesarAdivinanza(p.getNombre());
                    }
                }
            });
            tarjetas.put(p.getId(), t);
            panelGrilla.add(t);
        }
        panelGrilla.revalidate();
    }

    /** Pinta como activas las tarjetas de la lista de candidatos que se esta mostrando. */
    private void refrescarTarjetas() {
        List<Personaje> visibles = esPartidaHumana() ? motor.getCandidatosHumano() : motor.getMaquina1().getCandidatos();
        Set<Integer> activos = new HashSet<>();
        for (Personaje p : visibles) activos.add(p.getId());

        for (Map.Entry<Integer, JPanel> entrada : tarjetas.entrySet()) {
            JPanel t = entrada.getValue();
            boolean activo = activos.contains(entrada.getKey());
            t.setBackground(activo ? new Color(245, 252, 255) : new Color(235, 235, 235));
            t.setBorder(new LineBorder(activo ? new Color(100, 160, 220) : new Color(210, 210, 210), activo ? 2 : 1, true));
            for (Component c : t.getComponents()) c.setEnabled(activo);
        }
        if (esPartidaHumana()) {
            lblContadorCandidatos.setText("Candidatos: " + visibles.size());
        } else {
            lblContadorCandidatos.setText("M1: " + motor.getMaquina1().getCandidatos().size()
                    + " | M2: " + motor.getMaquina2().getCandidatos().size());
        }
        panelGrilla.repaint();
    }

    // ================= 4. FLUJO DE JUEGO =================

    /** El personaje se elige una sola vez por sesion: no se puede cambiar entre partidas. */
    private boolean elegirPersonaje() {
        String jugador = txtNombreJugador.getText().trim().isEmpty() ? "Jugador" : txtNombreJugador.getText().trim();
        String[] ops = motor.getPersonajes().stream().map(p -> String.format("#%02d %s (%s, Calvo:%s, Lentes:%s, %s)",
                p.getId(), p.getNombre(), p.getGenero(), p.isCalvicie() ? "SI" : "NO", p.isLentes() ? "SI" : "NO", p.getColorPelo())).toArray(String[]::new);

        String sel = (String) JOptionPane.showInputDialog(this,
                "Elige tu personaje secreto.\nQueda fijo para todas las partidas de la sesión.",
                "Personaje de " + jugador, JOptionPane.QUESTION_MESSAGE, null, ops, ops[0]);
        if (sel == null) return false;

        motor.iniciarSesion(jugador, Integer.parseInt(sel.substring(1, 3))); // busqueda binaria por ID
        lblSesion.setText("Sesión de " + motor.getNombreJugador() + " | Tu personaje: " + motor.getPersonajeHumano().getNombre());
        return true;
    }

    private boolean asegurarSesion() {
        String nombre = txtNombreJugador.getText().trim().isEmpty() ? "Jugador" : txtNombreJugador.getText().trim();
        if (motor.haySesion() && nombre.equals(motor.getNombreJugador())) return true;
        return elegirPersonaje();
    }

    private void iniciarPartida(int tipo) {
        if (!asegurarSesion()) return;
        motor.nuevaPartidaHumano(tipo);
        Maquina rival = motor.getRival();
        Personaje propio = motor.getPersonajeHumano();

        txtLog.setText("");
        log("=== " + motor.getNombreJugador() + " vs " + rival.getNombre() + " ===");
        log(rival.describirEstilo() + ".");
        if (tipo == 2) {
            List<RegistroPregunta> heredado = rival.getConocimientoHeredado();
            log(heredado.isEmpty()
                    ? "La Máquina 1 todavía no te hizo preguntas en esta sesión: la Máquina 2 arranca sin ventaja."
                    : "Ventaja: la Máquina 2 conoce las preguntas que te hizo la Máquina 1 " + heredado
                        + " y arranca con " + rival.getCandidatos().size() + " candidatos.");
        }

        lblModoTitulo.setText("MODO: " + motor.getNombreJugador() + " vs " + rival.getNombre());
        lblMiPersonajeInfo.setText("Tu Personaje: " + propio.descripcion());
        lblJugadaHumano.setText("👉 Tu jugada: ¡Comienza! Elige una pregunta abajo o toca una tarjeta para adivinar.");
        lblJugadaMaquina.setText("🤖 Jugada de la Máquina: Lista.");
        bordeGrilla.setTitle("Tablero de Candidatos (Tus Sospechas)");

        refrescarTarjetas();
        mapaBotones.values().forEach(b -> { b.setEnabled(true); b.setBackground(null); });
        btnAdivinar.setEnabled(true);
        btnSaltear.setEnabled(true);
        btnTurnoMvM.setEnabled(false);
        btnAutoMvM.setEnabled(false);
        cardLayout.show(panelContenedor, "JUEGO");
    }

    private void iniciarPartidaMvM() {
        motor.nuevaPartidaMvM();
        Maquina m1 = motor.getMaquina1();
        Maquina m2 = motor.getMaquina2();

        txtLog.setText("");
        log("=== Simulación Máquina 1 vs Máquina 2 ===");
        log(m1.describirEstilo() + ".");
        log(m2.describirEstilo() + ".");

        lblModoTitulo.setText("MODO: Simulación Máquina 1 vs Máquina 2 - Turno 1");
        lblMiPersonajeInfo.setText("(Espectador) M1 eligió: " + m1.revelarPersonaje().getNombre()
                + " | M2 eligió: " + m2.revelarPersonaje().getNombre());
        lblJugadaHumano.setText("🤖 M1 busca el personaje de M2 (preguntas al azar).");
        lblJugadaMaquina.setText("🤖 M2 busca el personaje de M1 (Greedy + ventaja).");
        bordeGrilla.setTitle("Candidatos que le quedan a la Máquina 1 (busca a " + m2.revelarPersonaje().getNombre() + ")");

        refrescarTarjetas();
        mapaBotones.values().forEach(b -> { b.setEnabled(false); b.setBackground(null); });
        btnAdivinar.setEnabled(false);
        btnSaltear.setEnabled(false);
        btnTurnoMvM.setEnabled(true);
        btnAutoMvM.setEnabled(true);
        cardLayout.show(panelContenedor, "JUEGO");
    }

    private void realizarPreguntaHumano(String preg) {
        if (!esPartidaHumana() || motor.isTerminada()) return;
        boolean rta;
        int antes = motor.getCandidatosHumano().size();
        try {
            rta = motor.preguntarHumano(preg);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
            return;
        }
        JButton b = mapaBotones.get(preg);
        if (b != null) { b.setEnabled(false); b.setBackground(new Color(230, 230, 230)); }

        int descartados = antes - motor.getCandidatosHumano().size();
        lblJugadaHumano.setText(String.format("👉 Preguntaste [%s] ➔ Máquina responde: %s (Descartaste %d personajes)", preg, rta ? "¡SÍ!" : "NO", descartados));
        log("Turno " + motor.getTurno() + " - " + motor.getNombreJugador() + " pregunta " + preg + " -> " + (rta ? "SI" : "NO")
                + " (te quedan " + motor.getCandidatosHumano().size() + ")");

        refrescarTarjetas();
        ejecutarTurnoMaquina();
    }

    private void realizarSuposicion() {
        if (!esPartidaHumana() || motor.isTerminada()) return;
        String[] n = motor.getCandidatosHumano().stream().map(Personaje::getNombre).toArray(String[]::new);
        String sel = (String) JOptionPane.showInputDialog(this, "¿Quién crees que es?", "Adivinar", JOptionPane.QUESTION_MESSAGE, null, n, n[0]);
        if (sel != null) procesarAdivinanza(sel);
    }

    private void procesarAdivinanza(String nom) {
        Maquina rival = motor.getRival();
        if (motor.arriesgarHumano(nom)) {
            lblJugadaHumano.setText("🏆 ¡ACERTASTE! Era " + nom + ".");
            log(motor.getNombreJugador() + " arriesga " + nom + " -> CORRECTO. ¡Gana " + motor.getNombreJugador() + "!");
            JOptionPane.showMessageDialog(this, "¡Victoria! Adivinaste a " + nom + ".\nSe sumó una victoria al marcador.", "¡Ganaste!", JOptionPane.INFORMATION_MESSAGE);
            finalizar();
        } else {
            lblJugadaHumano.setText("❌ Arriesgaste " + nom + ", pero NO es.");
            log("Turno " + motor.getTurno() + " - " + motor.getNombreJugador() + " arriesga " + nom + " -> incorrecto.");
            JOptionPane.showMessageDialog(this, "No era " + nom + ". " + rival.getNombre() + " juega su turno.", "Fallaste", JOptionPane.ERROR_MESSAGE);
            refrescarTarjetas();
            ejecutarTurnoMaquina();
        }
    }

    private void ejecutarTurnoMaquina() {
        if (motor.isTerminada()) return;
        Maquina rival = motor.getRival();
        Jugada jugada = motor.turnoMaquina();
        lblJugadaMaquina.setText("🤖 " + jugada.resumen());
        log("   " + jugada.resumen());

        if (jugada.esGanadora()) {
            JOptionPane.showMessageDialog(this, rival.getNombre() + " descubrió tu personaje (" + motor.getPersonajeHumano().getNombre() + ").\n"
                    + "Su personaje era " + rival.revelarPersonaje().getNombre() + ".\n¡Gana la máquina!", "Fin", JOptionPane.INFORMATION_MESSAGE);
            finalizar();
        }
    }

    private void ejecutarTurnoMvM() {
        if (motor.isTerminada() || motor.getModo() != MotorJuego.Modo.MAQUINA_VS_MAQUINA) return;
        int turno = motor.getTurno();
        List<Jugada> jugadas = motor.turnoMvM();
        log("--- Turno " + turno + " ---");
        for (Jugada j : jugadas) {
            log("  Razonamiento: " + j.getRazonamiento());
            log("  " + j.resumen());
        }
        lblJugadaHumano.setText("🤖 " + jugadas.get(0).resumen());
        if (jugadas.size() > 1) lblJugadaMaquina.setText("🤖 " + jugadas.get(1).resumen());
        refrescarTarjetas();

        if (motor.isTerminada()) {
            log("GANADOR: " + motor.getGanador() + " en el turno " + turno + ".");
            JOptionPane.showMessageDialog(this, "¡" + motor.getGanador() + " adivinó el personaje de su rival y gana!", "Fin de la simulación", JOptionPane.INFORMATION_MESSAGE);
            finalizar();
        } else {
            lblModoTitulo.setText("MODO: Simulación Máquina 1 vs Máquina 2 - Turno " + motor.getTurno());
        }
    }

    private void simularTodoMvM() {
        // Sin limite de turnos: la simulacion siempre termina porque cada jugada descarta candidatos.
        while (!motor.isTerminada() && motor.getModo() == MotorJuego.Modo.MAQUINA_VS_MAQUINA) ejecutarTurnoMvM();
    }

    private boolean esPartidaHumana() {
        return motor.getModo() == MotorJuego.Modo.HUMANO_VS_MAQUINA_1 || motor.getModo() == MotorJuego.Modo.HUMANO_VS_MAQUINA_2;
    }

    private void volverAlMenu() {
        if (!motor.isTerminada() && esPartidaHumana()) {
            log("Partida abandonada. El personaje de " + motor.getRival().getNombre() + " era " + motor.getRival().revelarPersonaje().getNombre() + ".");
        }
        cardLayout.show(panelContenedor, "MENU");
    }

    private void log(String texto) {
        txtLog.append(texto + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    private void finalizar() {
        mapaBotones.values().forEach(b -> b.setEnabled(false));
        btnAdivinar.setEnabled(false);
        btnSaltear.setEnabled(false);
        btnTurnoMvM.setEnabled(false);
        btnAutoMvM.setEnabled(false);
    }

    private void mostrarMarcador() {
        JOptionPane.showMessageDialog(this, "=== MARCADOR RECORD ===\n\n" + motor.getMarcador().textoRanking(), "Marcador", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new Gui().setVisible(true);
        });
    }
}
