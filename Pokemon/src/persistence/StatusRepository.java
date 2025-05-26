package persistence;

import domain.Log;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Repositorio para leer y consultar estados alterados desde un archivo CSV.
 */
public class StatusRepository implements Serializable {

    private static final String STATUS_CSV = "resources/csv/Estados.csv";
    private static TreeMap<Integer, String[]> estados = new TreeMap<>();

    public StatusRepository() {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(STATUS_CSV));
        } catch (IOException e) {
            Log.record(e); // Asume que tienes una clase Log para manejar errores
            return;
        }

        for (int i = 1; i < lines.size(); i++) {
            String[] valores = splitCSVLine(lines.get(i));
            estados.put(i, valores); // Usa el número de línea como ID único
        }
    }

    /**
     * Devuelve todos los estados registrados como lista.
     */
    public ArrayList<String[]> getAllStatuses() {
        return new ArrayList<>(estados.values());
    }

    /**
     * Devuelve el estado por ID (basado en el orden de aparición en el CSV).
     */
    public String[] getStatusById(int id) {
        return estados.getOrDefault(id, null);
    }

    /**
     * Devuelve el estado en formato corto para selección en menús.
     */
    public String getStatusToChoose(int id) {
        String[] estado = getStatusById(id);
        if (estado == null) return null;
        return id + " | " + estado[0] + " (" + estado[1] + " turnos)" + " → " + estado[4];
    }

    /**
     * Devuelve el nombre y duración del estado para mostrar en batalla.
     */
    public String getStatusInfo(int id) {
        String[] estado = getStatusById(id);
        if (estado == null) return null;
        return estado[0] + " | Duración: " + estado[1] + " | Permanente: " + estado[2];
    }

    /**
     * Devuelve la descripción textual del estado.
     */
    public String getStatusDescription(int id) {
        String[] estado = getStatusById(id);
        return (estado != null) ? estado[4] : null;
    }

    /**
     * Busca un estado por su nombre (tipo textual).
     * @param nombre Nombre exacto del estado (ej. "Paralisis")
     * @return El arreglo de Strings con los datos del estado, o null si no existe
     */
    public String[] getStatusByName(String nombre) {
        for (String[] estado : estados.values()) {
            if (estado[0].equalsIgnoreCase(nombre)) {
                return estado;
            }
        }
        return null;
    }

    /**
     * Parsea una línea de CSV con compatibilidad con comillas.
     */
    private static String[] splitCSVLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString().trim().replaceAll("^\"|\"$", ""));
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        values.add(current.toString().trim().replaceAll("^\"|\"$", ""));

        return values.toArray(new String[0]);
    }
}
