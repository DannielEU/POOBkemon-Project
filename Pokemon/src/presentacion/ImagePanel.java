package presentacion;

import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * Panel transparente que dibuja una imagen de fondo escalada al tamaño del panel.
 */
class ImagePanel extends JPanel {

    private BufferedImage background;

    public ImagePanel(LayoutManager layout, String imagePath) {
        super(layout);
        background = Assets.image(imagePath);
        setOpaque(false);
    }

    public void setBackgroundImage(String imagePath) {
        background = Assets.image(imagePath);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
