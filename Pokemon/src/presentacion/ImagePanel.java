package presentacion;

import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

class ImagePanel extends JPanel{
    private BufferedImage fondo;

    public ImagePanel(LayoutManager layout, String rutaImagen) {
        super(layout);
        cargarImagen(rutaImagen);
        setOpaque(false);
    }

    private void cargarImagen(String ruta) {
        try {
            fondo = ImageIO.read(new File(ruta));
        } catch (IOException e) {
            fondo = null;
        }
    }

    public void setBackgroundImage(String ruta) {
        cargarImagen(ruta);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (fondo != null) {
            g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
        }
    }
}