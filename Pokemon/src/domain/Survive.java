package domain;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Modo supervivencia: los Pokémon se crean a nivel 100 y no se permite el uso de ítems.
 */
public class Survive extends POOBkemon {

    private static final int SURVIVAL_LEVEL = 100;

    private static Survive instance;

    private Survive() {
        super();
    }

    public static Survive getInstance() {
        if (instance == null) {
            instance = new Survive();
        }
        return instance;
    }

    /**
     * Descarta la instancia de supervivencia; la siguiente llamada a {@link #getInstance()} crea otra.
     */
    public static void resetInstance() {
        instance = null;
        POOBkemon.resetInstance();
    }

    @Override
    protected int pokemonLevel() {
        return SURVIVAL_LEVEL;
    }

    @Override
    public String[][] getInfoItems(int trainerId) throws POOBkemonException {
        return new String[][]{};
    }
}
