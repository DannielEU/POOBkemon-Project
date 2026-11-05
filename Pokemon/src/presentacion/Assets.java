package presentacion;

import domain.Log;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Carga y cachea imágenes, íconos y la fuente del juego.
 * Antes cada pantalla releía el disco y reescalaba en cada construcción de botón.
 */
final class Assets {

    private static final Map<String, BufferedImage> IMAGES = new ConcurrentHashMap<>();
    private static final Map<String, ImageIcon> SCALED_ICONS = new ConcurrentHashMap<>();
    private static final Map<Float, Font> FONTS = new ConcurrentHashMap<>();
    private static volatile Font baseFont;
    private static boolean baseFontLoaded;

    private Assets() {
    }

    /**
     * Imagen del disco, leída una sola vez.
     *
     * @return la imagen, o {@code null} si no existe o no se puede leer
     */
    static BufferedImage image(String path) {
        BufferedImage cached = IMAGES.get(path);
        if (cached != null) {
            return cached;
        }
        try {
            BufferedImage read = ImageIO.read(new File(path));
            if (read != null) {
                IMAGES.put(path, read);
            }
            return read;
        } catch (IOException e) {
            Log.record(e);
            return null;
        }
    }

    /**
     * Ícono sin escalar. No se cachea porque los GIF animados no deben compartir estado.
     */
    static ImageIcon icon(String path) {
        return new ImageIcon(path);
    }

    /**
     * Ícono escalado con suavizado, cacheado por ruta y tamaño.
     */
    static ImageIcon scaledIcon(String path, int width, int height) {
        String key = path + '@' + width + 'x' + height;
        ImageIcon cached = SCALED_ICONS.get(key);
        if (cached != null) {
            return cached;
        }
        BufferedImage source = image(path);
        if (source == null) {
            return new ImageIcon();
        }
        ImageIcon scaled = new ImageIcon(source.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        SCALED_ICONS.put(key, scaled);
        return scaled;
    }

    /**
     * Escala un ícono ya cargado (se mantiene por compatibilidad con código que ya tiene el ImageIcon).
     */
    static ImageIcon scale(ImageIcon icon, int width, int height) {
        return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    /**
     * Fuente pixelada del juego en negrita, en el tamaño pedido. El archivo se lee y registra una sola vez.
     */
    static Font font(float size) {
        return FONTS.computeIfAbsent(size, s -> {
            Font base = loadBaseFont();
            return base != null ? base.deriveFont(Font.BOLD, s) : new Font(Font.MONOSPACED, Font.BOLD, Math.round(s));
        });
    }

    private static synchronized Font loadBaseFont() {
        if (!baseFontLoaded) {
            baseFontLoaded = true;
            try {
                baseFont = Font.createFont(Font.TRUETYPE_FONT, new File(Theme.FONT_FILE));
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(baseFont);
            } catch (FontFormatException | IOException e) {
                Log.record(e);
            }
        }
        return baseFont;
    }
}
