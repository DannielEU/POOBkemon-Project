package presentacion;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Fábrica de los botones que usan todas las pantallas. Reemplaza las tres variantes
 * casi idénticas que había en la ventana principal y las de la interfaz Auxiliar.
 */
final class UiFactory {

    private static final int SMALL_ICON = 50;

    private UiFactory() {
    }

    /** Botón redondeado con estados de hover, presionado, foco y deshabilitado. */
    static JButton styledButton(String text, Rectangle bounds, Color fill) {
        return new RoundedButton(text, bounds, fill);
    }

    /** Botón redondeado con el color de relleno estándar. */
    static JButton styledButton(String text, Rectangle bounds) {
        return new RoundedButton(text, bounds, Theme.BUTTON_FILL);
    }

    /** Botón sin fondo cuyo texto cambia de color al pasar el ratón. */
    static JButton transparentButton(String text, Rectangle bounds, boolean centered) {
        JButton button = new JButton(text);
        button.setBounds(bounds);
        makeFlat(button);
        button.setForeground(Theme.TEXT);
        button.setFont(Assets.font(18));
        if (!centered) {
            button.setHorizontalAlignment(SwingConstants.LEFT);
        }
        addHoverTextColor(button);
        return button;
    }

    /** Botón que muestra solo una imagen escalada al tamaño del botón. */
    static JButton iconButton(String imagePath, int x, int y, int width, int height) {
        JButton button = new JButton();
        button.setBounds(x, y, width, height);
        ImageIcon icon = Assets.scaledIcon(imagePath, width, height);
        if (icon.getIconWidth() > 0) {
            button.setIcon(icon);
        } else {
            button.setText("No image");
        }
        makeFlat(button);
        return button;
    }

    /**
     * Botón con texto (admite saltos de línea) e imagen.
     *
     * @param coverButton si es verdadero la imagen ocupa todo el botón y el texto va encima;
     *                    si no, es un ícono pequeño con el texto al costado
     * @param aligned     alinea el texto a la izquierda con margen
     */
    static JButton imageTextButton(String text, String imagePath, int x, int y, int width, int height,
                                   int fontSize, boolean aligned, boolean coverButton) {
        JButton button = new JButton(multiline(text));
        int iconWidth = coverButton ? width : SMALL_ICON;
        int iconHeight = coverButton ? height : SMALL_ICON;
        button.setIcon(Assets.scaledIcon(imagePath, iconWidth, iconHeight));
        button.setHorizontalTextPosition(coverButton ? SwingConstants.CENTER : SwingConstants.RIGHT);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setFont(Assets.font(fontSize));
        button.setForeground(Theme.TEXT);
        makeFlat(button);
        button.setBounds(x, y, width, height);
        if (aligned) {
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setMargin(new Insets(0, 20, 0, 0));
        }
        addHoverTextColor(button);
        return button;
    }

    /**
     * Botón de texto (admite saltos de línea) que dibuja la imagen de fondo a tamaño completo
     * (si {@code coverButton}) o un ícono pequeño junto al texto.
     */
    static JButton filledImageButton(String text, String imagePath, int x, int y, int width, int height,
                                     int fontSize, boolean aligned, boolean coverButton) {
        final Image background = coverButton ? Assets.image(imagePath) : null;
        JButton button = new JButton(multiline(text)) {
            @Override
            protected void paintComponent(Graphics g) {
                if (background != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setComposite(AlphaComposite.SrcOver);
                    g2.drawImage(background, 0, 0, getWidth(), getHeight(), this);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        button.setFont(Assets.font(fontSize));
        button.setForeground(Theme.TEXT);
        button.setBounds(x, y, width, height);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(5, aligned ? 15 : 5, 5, 5));
        if (coverButton) {
            button.setHorizontalTextPosition(SwingConstants.CENTER);
        } else {
            button.setIcon(Assets.scaledIcon(imagePath, SMALL_ICON, SMALL_ICON));
            button.setHorizontalTextPosition(SwingConstants.RIGHT);
            button.setIconTextGap(10);
        }
        button.setVerticalTextPosition(SwingConstants.CENTER);
        addHoverTextColor(button);
        return button;
    }

    private static String multiline(String text) {
        return "<html>" + text.replace("\n", "<br>") + "</html>";
    }

    private static void makeFlat(JButton button) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static void addHoverTextColor(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(Theme.TEXT_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(Theme.TEXT);
            }
        });
    }
}
