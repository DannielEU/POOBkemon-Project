package presentacion;

import java.awt.Color;

/**
 * Constantes visuales y rutas de recursos compartidas por todas las pantallas.
 */
final class Theme {

    // ---- rutas de recursos
    static final String CHARACTER = "resources/personaje/";
    static final String MENU = "resources/menu/";
    static final String POKEMONES = "resources/pokemones/Emerald/";
    static final String BACK_PATH = POKEMONES + "Back/";
    static final String BACK_SHINY_PATH = POKEMONES + "BackShiny/";
    static final String NORMAL_PATH = POKEMONES + "Normal/";
    static final String SHINY_PATH = POKEMONES + "Shiny/";
    static final String FRAME = MENU + "frame/";
    static final String SONGS = "resources/songs/";
    static final String FONT_FILE = "resources/fonts/themevck-text.ttf";
    static final String APP_ICON = "resources/icon/window_icon.png";
    static final String PNG_EXT = ".png";

    // ---- colores
    static final Color TEXT = Color.BLACK;
    static final Color TEXT_HOVER = Color.WHITE;
    static final Color BUTTON_FILL = new Color(240, 240, 240, 200);
    static final Color BUTTON_HOVER_FILL = new Color(255, 255, 255, 235);
    static final Color BUTTON_PRESSED_FILL = new Color(205, 205, 205, 235);
    static final Color BUTTON_DISABLED_FILL = new Color(200, 200, 200, 120);
    static final Color BUTTON_BORDER = Color.BLACK;
    static final Color FOCUS_RING = new Color(255, 196, 0);

    private Theme() {
    }
}
