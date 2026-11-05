package persistence;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Acceso de solo lectura al catálogo de Pokémon.
 * Columnas: ID_0, Name_1, Type1_2, Type2_3, Total_4, HP_5, Attack_6, Defense_7, Sp.Atk_8, Sp.Def_9, Speed_10.
 * El archivo se lee una sola vez y se comparte entre instancias.
 */
public class PokemonRepository {

    private static final String POKEMONS_ARCHIVE = "resources/csv/Pokemones.csv";

    private static volatile Map<Integer, String[]> catalog;

    public PokemonRepository() {
        load();
    }

    private static Map<Integer, String[]> load() {
        Map<Integer, String[]> local = catalog;
        if (local == null) {
            synchronized (PokemonRepository.class) {
                local = catalog;
                if (local == null) {
                    local = new TreeMap<>();
                    for (String[] row : CsvReader.readRows(POKEMONS_ARCHIVE)) {
                        local.put(Integer.parseInt(row[0]), row);
                    }
                    catalog = local;
                }
            }
        }
        return local;
    }

    /**
     * @return copia de todas las filas del catálogo ordenadas por ID
     */
    public ArrayList<String[]> getPokemons() {
        ArrayList<String[]> rows = new ArrayList<>();
        for (String[] row : load().values()) {
            rows.add(row.clone());
        }
        return rows;
    }

    /**
     * @param id identificador en la Pokédex
     * @return copia de la fila del Pokémon, o {@code null} si no existe
     */
    public String[] getPokemonId(int id) {
        String[] row = load().get(id);
        return row == null ? null : row.clone();
    }
}
