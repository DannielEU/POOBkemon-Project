package persistence;

import domain.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utilidad común para leer archivos CSV con soporte de campos entre comillas.
 * Centraliza la lectura que antes estaba duplicada en cada repositorio.
 */
final class CsvReader {

    private static final char BOM = '﻿';

    private CsvReader() {
    }

    /**
     * Lee todas las filas de datos de un CSV (omite la cabecera y las líneas vacías).
     *
     * @param location ruta relativa del archivo
     * @return filas ya separadas en columnas; lista vacía si el archivo no se puede leer
     */
    static List<String[]> readRows(String location) {
        return read(location, 1);
    }

    /**
     * Lee todas las filas de un CSV que no tiene línea de cabecera.
     */
    static List<String[]> readRowsWithoutHeader(String location) {
        return read(location, 0);
    }

    private static List<String[]> read(String location, int firstLine) {
        Path path = Paths.get(location);
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.record(e);
            return Collections.emptyList();
        }
        List<String[]> rows = new ArrayList<>();
        for (int i = firstLine; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.isBlank()) {
                rows.add(split(line));
            }
        }
        return rows;
    }

    /**
     * Lee la línea de cabecera de un CSV.
     *
     * @param location ruta relativa del archivo
     * @return columnas de la cabecera; arreglo vacío si el archivo no existe o está vacío
     */
    static String[] readHeader(String location) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(location), StandardCharsets.UTF_8);
            return lines.isEmpty() ? new String[0] : split(lines.get(0));
        } catch (IOException e) {
            Log.record(e);
            return new String[0];
        }
    }

    /**
     * Separa una línea CSV respetando las comillas dobles.
     */
    static String[] split(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(clean(current));
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(clean(current));
        return values.toArray(new String[0]);
    }

    private static String clean(StringBuilder sb) {
        String value = sb.toString().trim();
        if (!value.isEmpty() && value.charAt(0) == BOM) {
            value = value.substring(1);
        }
        return value;
    }
}
