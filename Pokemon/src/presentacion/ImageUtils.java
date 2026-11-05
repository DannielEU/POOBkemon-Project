package presentacion;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * Operaciones sobre imágenes usadas al acomodar sprites en el campo de batalla.
 */
final class ImageUtils {

    private ImageUtils() {
    }

    /**
     * Fila más baja con algún píxel visible (alfa distinto de cero) en toda la imagen.
     */
    static int findLowestVisibleY(BufferedImage img) {
        int lowestY = 0;
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = img.getHeight() - 1; y >= lowestY; y--) {
                if ((img.getRGB(x, y) & 0xFF000000) != 0) {
                    lowestY = Math.max(lowestY, y);
                    break;
                }
            }
        }
        return lowestY;
    }

    static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage copy = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return copy;
    }
}
