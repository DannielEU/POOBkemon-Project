package persistence;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Tabla de efectividad entre tipos (multiplicador de daño según tipo atacante y defensor).
 */
public class StatsRepository {

    private static final String ROOT_STATS_LOCATION = "resources/csv/MovesStatspok.csv";

    private static volatile Map<String, Map<String, Double>> typeChart;

    public StatsRepository() {
        load();
    }

    private static Map<String, Map<String, Double>> load() {
        Map<String, Map<String, Double>> local = typeChart;
        if (local == null) {
            synchronized (StatsRepository.class) {
                local = typeChart;
                if (local == null) {
                    local = readChart();
                    typeChart = local;
                }
            }
        }
        return local;
    }

    private static Map<String, Map<String, Double>> readChart() {
        Map<String, Map<String, Double>> chart = new HashMap<>();
        String[] attackingTypes = CsvReader.readHeader(ROOT_STATS_LOCATION);
        for (String[] row : CsvReader.readRows(ROOT_STATS_LOCATION)) {
            Map<String, Double> multipliers = new HashMap<>();
            for (int j = 1; j < row.length && j < attackingTypes.length; j++) {
                multipliers.put(attackingTypes[j], Double.parseDouble(row[j]));
            }
            chart.put(row[0], multipliers);
        }
        return chart;
    }

    /**
     * @param attackingType tipo del ataque
     * @param defendingType tipo del Pokémon que recibe el ataque
     * @return multiplicador de daño; 1.0 si alguno de los tipos es desconocido
     */
    public double getMultiplier(String attackingType, String defendingType) {
        Map<String, Double> row = load().get(capitalizar(defendingType));
        if (row == null) {
            return 1.0;
        }
        return row.getOrDefault(capitalizar(attackingType), 1.0);
    }

    public static String capitalizar(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }
}
