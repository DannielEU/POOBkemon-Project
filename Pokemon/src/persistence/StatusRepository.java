package persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Catálogo de estados alterados.
 * Columnas: Tipo_0, Duracion_1, EsPermanente_2, EsVolatil_3, Descripcion_4, Parametros_5.
 * El identificador de un estado es su posición (desde 1) en el archivo.
 */
public class StatusRepository implements Serializable {

    private static final String STATUS_CSV = "resources/csv/Estados.csv";

    private static volatile List<String[]> statuses;

    public StatusRepository() {
        load();
    }

    private static List<String[]> load() {
        List<String[]> local = statuses;
        if (local == null) {
            synchronized (StatusRepository.class) {
                local = statuses;
                if (local == null) {
                    local = CsvReader.readRows(STATUS_CSV);
                    statuses = local;
                }
            }
        }
        return local;
    }

    /**
     * @return copia de todos los estados registrados
     */
    public ArrayList<String[]> getAllStatuses() {
        ArrayList<String[]> copy = new ArrayList<>();
        for (String[] row : load()) {
            copy.add(row.clone());
        }
        return copy;
    }

    /**
     * @param id posición del estado en el CSV (desde 1)
     * @return fila del estado o {@code null} si no existe
     */
    public String[] getStatusById(int id) {
        List<String[]> all = load();
        return (id >= 1 && id <= all.size()) ? all.get(id - 1).clone() : null;
    }

    /**
     * Devuelve el estado en formato corto para selección en menús.
     */
    public String getStatusToChoose(int id) {
        String[] status = getStatusById(id);
        if (status == null) {
            return null;
        }
        return id + " | " + status[0] + " (" + status[1] + " turnos)" + " → " + status[4];
    }

    /**
     * Devuelve el nombre y la duración del estado para mostrar en batalla.
     */
    public String getStatusInfo(int id) {
        String[] status = getStatusById(id);
        if (status == null) {
            return null;
        }
        return status[0] + " | Duración: " + status[1] + " | Permanente: " + status[2];
    }

    /**
     * Devuelve la descripción textual del estado.
     */
    public String getStatusDescription(int id) {
        String[] status = getStatusById(id);
        return status != null ? status[4] : null;
    }

    /**
     * Busca un estado por su nombre (sin distinguir mayúsculas).
     *
     * @return fila del estado, o {@code null} si no existe
     */
    public String[] getStatusByName(String name) {
        for (String[] status : load()) {
            if (status[0].equalsIgnoreCase(name)) {
                return status.clone();
            }
        }
        return null;
    }
}
