package persistence;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Catálogo de movimientos.
 * Columnas: id_0, name_1, description_2, type_3, damage_class_4, power_5, accuracy_6, pp_7,
 * direccion_8, consequence_9, probabilityStatus_10.
 */
public class MovesRepository {

    private static final String ATTACKS_ARCHIVE = "resources/csv/movimientos.csv";
    private static final int COL_NAME = 1;
    private static final int COL_TYPE = 3;
    private static final int COL_DAMAGE_CLASS = 4;
    private static final int COL_POWER = 5;
    private static final int COL_PP = 7;

    private static volatile Map<Integer, String[]> moves;

    public MovesRepository() {
        load();
    }

    private static Map<Integer, String[]> load() {
        Map<Integer, String[]> local = moves;
        if (local == null) {
            synchronized (MovesRepository.class) {
                local = moves;
                if (local == null) {
                    local = new TreeMap<>();
                    for (String[] row : CsvReader.readRows(ATTACKS_ARCHIVE)) {
                        local.put(Integer.parseInt(row[0]), row);
                    }
                    moves = local;
                }
            }
        }
        return local;
    }

    public ArrayList<String[]> getMoves() {
        ArrayList<String[]> copy = new ArrayList<>();
        for (String[] row : load().values()) {
            copy.add(row.clone());
        }
        return copy;
    }

    /**
     * @return copia de la fila del movimiento, o {@code null} si no existe
     */
    public String[] getAttacksId(int id) {
        String[] row = load().get(id);
        return row == null ? null : row.clone();
    }

    /**
     * @return "001 Nombre - Tipo - Clase" o {@code null} si el movimiento no existe
     */
    public String getAttackId(int id) {
        String[] attack = load().get(id);
        if (attack == null) {
            return null;
        }
        return String.format("%03d", id) + " " + attack[COL_NAME] + " - " + attack[COL_TYPE]
                + " - " + attack[COL_DAMAGE_CLASS];
    }

    public String getAttackToChoose(int id) {
        String[] attack = load().get(id);
        if (attack == null) {
            return null;
        }
        return attack[0] + " | " + attack[COL_NAME] + "\n"
                + "T." + attack[COL_TYPE] + " | C." + attack[COL_DAMAGE_CLASS] + "\n"
                + "PD " + attack[COL_POWER] + " | PP " + attack[COL_PP];
    }

    public String getAttackType(int id) {
        String[] attack = load().get(id);
        return attack == null ? null : attack[COL_TYPE];
    }

    /**
     * @return {tipo, poder}; el poder es "0" cuando el movimiento no hace daño directo
     */
    public String[] getAttackDamageAndType(int id) {
        String[] attack = load().get(id);
        if (attack == null) {
            return null;
        }
        String power = attack[COL_POWER];
        return new String[]{attack[COL_TYPE], (power == null || power.isEmpty()) ? "0" : power};
    }

    /**
     * Movimientos que un Pokémon puede aprender: los de alguno de sus tipos y los de tipo Normal.
     */
    public ArrayList<String[]> getCompatibleAttacks(int pokemonId) {
        ArrayList<String[]> compatible = new ArrayList<>();
        String[] pokemon = new PokemonRepository().getPokemonId(pokemonId);
        if (pokemon == null) {
            return compatible;
        }
        String primaryType = pokemon[2];
        String secondaryType = pokemon[3].isEmpty() ? primaryType : pokemon[3];
        for (String[] attack : load().values()) {
            String attackType = attack[COL_TYPE];
            if (primaryType.equalsIgnoreCase(attackType)
                    || secondaryType.equalsIgnoreCase(attackType)
                    || "Normal".equalsIgnoreCase(attackType)) {
                compatible.add(attack.clone());
            }
        }
        return compatible;
    }
}
