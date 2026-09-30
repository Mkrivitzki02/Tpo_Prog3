import Implementacion.Gui;
import Implementacion.JuegoAdivinanzasImpl;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("--console")) {
            JuegoAdivinanzasImpl juego = new JuegoAdivinanzasImpl();
            juego.ejecutar();
        } else {
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}
                new Gui().setVisible(true);
            });
        }
    }
}
