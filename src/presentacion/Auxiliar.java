package presentacion;

import domain.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public interface Auxiliar {

    static final String APP_ICON = "resources/icon/gear.png";
    public static JButton crearBotonTransparente(String texto, Rectangle bounds, boolean alineado) {
        JButton boton = new JButton(texto);

        // Poner las coordenadas y tamaño
        boton.setBounds(bounds);

        // Hacer el botón transparente
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setOpaque(false);

        // Color inicial del texto
        boton.setForeground(Color.BLACK);

        // Cambiar color al pasar el ratón
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setForeground(Color.BLACK);
            }
        });

        boton.setFont(cargarFuentePixel(18));
        if (!alineado) {
            boton.setHorizontalAlignment(SwingConstants.LEFT);
        }
        return boton;
    }
    public static Font cargarFuentePixel(float tamaño) {
        try {
            Font fuenteBase = Font.createFont(Font.TRUETYPE_FONT,
                    new File("resources/fonts/themevck-text.ttf"));
            Font fuenteNegrita = fuenteBase.deriveFont(Font.BOLD, tamaño);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(fuenteNegrita);
            return fuenteNegrita;

        } catch (FontFormatException | IOException e) {
            Log.record(e);
            return new Font("Monospaced", Font.BOLD, (int)tamaño);
        }
    }
    public static JButton crearBotonEstilizado(String texto, Rectangle bounds, Color color) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fondo opaco
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Borde negro
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2f)); // Grosor del borde (2px)
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 15, 15);

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        boton.setBounds(bounds);
        boton.setFont(cargarFuentePixel(18));
        boton.setForeground(Color.BLACK);
        boton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Margen interno
        boton.setFocusable(false);
        boton.setContentAreaFilled(false); // Importante mantenerlo para que funcione nuestro paintComponent
        boton.setOpaque(false); // Permitimos que se vea nuestro fondo personalizado



        return boton;
    }
    public static ImageIcon scaleIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    static void mostrarError(String titulo, String error) {
        String mensaje = titulo + ":\n"+ error;
        JOptionPane.showMessageDialog(null, mensaje,
                "Error", JOptionPane.ERROR_MESSAGE,new ImageIcon(APP_ICON));
    }

    default int findAbsoluteLowestVisibleY(BufferedImage img) {
        int lowestY = 0;
        // Usamos un enfoque por columnas para mejor precisión
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = img.getHeight()-1; y >= lowestY; y--) {
                if ((img.getRGB(x, y) & 0xFF000000) != 0) {
                    if (y > lowestY) {
                        lowestY = y;
                    }
                    break; // Pasamos a la siguiente columna
                }
            }
        }
        return lowestY;
    }

    default BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage bufferedImage = new BufferedImage(
                img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D bGr = bufferedImage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bufferedImage;
    }
}
