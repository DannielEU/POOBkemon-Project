package presentacion;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * Diálogos estándar de la aplicación, con un aspecto uniforme.
 */
final class Dialogs {

    private Dialogs() {
    }

    static void error(String title, String detail) {
        error(null, title, detail);
    }

    static void error(Component parent, String title, String detail) {
        JOptionPane.showMessageDialog(parent, title + ":\n" + detail, "Error",
                JOptionPane.ERROR_MESSAGE, Assets.icon(Theme.APP_ICON));
    }

    static void info(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title,
                JOptionPane.INFORMATION_MESSAGE, Assets.icon(Theme.APP_ICON));
    }

    static boolean confirm(Component parent, String title, String message, ImageIcon icon) {
        return JOptionPane.showConfirmDialog(parent, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, icon) == JOptionPane.YES_OPTION;
    }
}
