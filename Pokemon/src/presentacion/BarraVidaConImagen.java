package presentacion;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class BarraVidaConImagen extends JComponent {
    private BufferedImage imagenFondo;
    private int valorActual;
    private int valorMaximo;
    private int arcWidth = 15;
    private int arcHeight = 15;
    private Timer animacion;
    private int valorObjetivo;

    // Márgenes
    private int paddingIzq = 40;
    private int paddingTop =2 ;
    private int paddingBottom = 2;

        public BarraVidaConImagen( int max) {
            this.valorMaximo = max;
            this.valorActual = max;

            // Cargar imagen de fondo
            ImageIcon icon = new ImageIcon("resources/menu/barra.png");
            this.imagenFondo = new BufferedImage(
                    icon.getIconWidth(),
                    icon.getIconHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D g2d = imagenFondo.createGraphics();
            icon.paintIcon(null, g2d, 0, 0);
            g2d.dispose();

            setPreferredSize(new Dimension(imagenFondo.getWidth(), imagenFondo.getHeight()));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Dibujar imagen completa de fondo
            g2.drawImage(imagenFondo, 0, 0, width, height, null);

            // Área de la barra de vida con padding
            int barraX = paddingIzq;
            int barraY = paddingTop;
            int barraWidth = width - paddingIzq;
            int barraHeight = height - paddingTop - paddingBottom;

            // Calcular ancho del progreso
            double porcentaje = (double) valorActual / valorMaximo;
            int anchoProgreso = (int) (barraWidth * porcentaje);

            // Color según porcentaje
            Color colorProgreso;
            if (porcentaje > 0.8) {
                colorProgreso = new Color(0, 200, 0, 180); // verde
            } else if (porcentaje > 0.4) {
                colorProgreso = new Color(255, 140, 0, 180); // naranja
            } else {
                colorProgreso = new Color(200, 0, 0, 180); // rojo
            }

            // Dibujar barra de progreso
            g2.setColor(colorProgreso);
            g2.fillRoundRect(barraX, barraY, anchoProgreso, barraHeight, arcWidth, arcHeight);

            // Borde blanco
            g2.setColor(Color.GRAY);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(barraX, barraY, barraWidth - 1, barraHeight - 1, arcWidth, arcHeight);

            g2.dispose();
        }

    public void setValue(int value) {
        valorObjetivo = Math.min(Math.max(value, 0), valorMaximo);
        final int inicio = valorActual;
        final int cambio = valorObjetivo - inicio;

        if (animacion != null && animacion.isRunning()) {
            animacion.stop();
        }

        final int duracion = 300; // duración total en ms
        final int delay = 15;     // intervalo de actualización
        final int pasos = duracion / delay;
        final double[] t = {0};   // progreso normalizado (0 a 1)

        animacion = new Timer(delay, null);
        animacion.addActionListener(e -> {
            t[0] += 1.0 / pasos;
            if (t[0] >= 1.0) {
                valorActual = valorObjetivo;
                animacion.stop();
            } else {
                double easing = t[0] * (2 - t[0]);  // ease-out cuadrático
                valorActual = inicio + (int) (cambio * easing);
            }
            repaint();
        });

        animacion.start();
    }


    public void setMaxValue(int max) {
            this.valorMaximo = max;
            if (valorActual > valorMaximo) {
                valorActual = valorMaximo;
            }
            repaint();
        }
}
