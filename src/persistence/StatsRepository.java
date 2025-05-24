package persistence;

import domain.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsRepository {
    private static final String ROOT_STATS_LOCATION = "resources/csv/MovesStatspok.csv";
    private final Map<String, Map<String, Double>> typeChart = new HashMap<>();

    public StatsRepository() {
        try {
            loadTypeChart();
        } catch (IOException e) {
            e.printStackTrace();
            Log.record(e);
        }
    }

    private void loadTypeChart() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(ROOT_STATS_LOCATION));
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío: " + ROOT_STATS_LOCATION);
        }

        String[] types = lines.get(0).split(",");

        for (int i = 1; i < lines.size(); i++) {
            String[] values = lines.get(i).split(",");
            String defendingType = values[0];
            Map<String, Double> multipliers = new HashMap<>();

            for (int j = 1; j < values.length; j++) {
                multipliers.put(types[j], Double.parseDouble(values[j]));
            }

            typeChart.put(defendingType, multipliers);
        }
    }

    public double getMultiplier(String attackingType, String defendingType) {
        return typeChart.get(capitalizar(defendingType)).get(capitalizar(attackingType));
    }
    public static String capitalizar(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
