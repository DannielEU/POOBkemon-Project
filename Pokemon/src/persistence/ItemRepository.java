package persistence;

import java.util.ArrayList;
import java.util.List;

/**
 * Catálogo de ítems disponibles: cada entrada contiene {nombre, descripción}.
 */
public class ItemRepository {

    private static final String ITEMS_ARCHIVE = "resources/csv/Items.csv";

    private static volatile List<List<String>> catalog;

    public ItemRepository() {
        load();
    }

    private static List<List<String>> load() {
        List<List<String>> local = catalog;
        if (local == null) {
            synchronized (ItemRepository.class) {
                local = catalog;
                if (local == null) {
                    local = new ArrayList<>();
                    // Este archivo no tiene cabecera: la primera línea ya es un ítem.
                    for (String[] row : CsvReader.readRowsWithoutHeader(ITEMS_ARCHIVE)) {
                        if (row.length >= 2) {
                            local.add(List.of(row[0], row[1]));
                        }
                    }
                    catalog = local;
                }
            }
        }
        return local;
    }

    /**
     * @return copia del catálogo; cada elemento es {nombre, descripción}
     */
    public ArrayList<ArrayList<String>> getItems() {
        ArrayList<ArrayList<String>> copy = new ArrayList<>();
        for (List<String> item : load()) {
            copy.add(new ArrayList<>(item));
        }
        return copy;
    }
}
