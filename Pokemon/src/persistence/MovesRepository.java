package persistence;

import domain.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MovesRepository {
    private static final String ATACKS_ARCHIVE = "resources/csv/movimientos.csv";
    private static TreeMap<Integer, String[]> movimientos = new TreeMap<>();

    public MovesRepository() {
        List<String> pokemonsIput = null;
        try {
            pokemonsIput = Files.readAllLines(Paths.get(ATACKS_ARCHIVE));
        } catch (IOException e) {
            Log.record(e);
        }

        for (int i = 1; i < pokemonsIput.size(); i++) {
            String[] valores = splitCSVLine(pokemonsIput.get(i));
            this.movimientos.put(Integer.parseInt(valores[0]), valores);
        }
    }

    public ArrayList<String[]> getMoves() {
        ArrayList<String[]> moves = new ArrayList<>();
        for (String[] s : this.movimientos.values()) {
            moves.add(s);
        }
        return moves;
    }

    public String[] getAttacksId(int id) {
        if (movimientos.containsKey(id)) {
            return movimientos.get(id);
        } else {
            return null;
        }
    }

    public String getAttackId(int id) {
        if (movimientos.containsKey(id)) {
            String[] attack = getAttacksId(id);
            if (attack[0].length() == 1) {
                attack[0] = "00" + attack[0];
            } else if (attack[0].length() == 2) {
                attack[0] = "0" + attack[0];
            }
            return attack[0] + " " + attack[1] + " - " + attack[3] + " - " + attack[4];
        } else {
            return null;
        }
    }

    public String getAttackToChoose(int id) {
        if (movimientos.containsKey(id)) {
            String[] attack = getAttacksId(id);
            return attack[0] + " | " + attack[1] + "\n" +"T."+ attack[3] + " | C." + attack[4]+ "\n" +"PD "+attack[5]+" | PP "+attack[7];
        } else {
            return null;
        }
    }
    public String getAttackType(int id) {
        if (movimientos.containsKey(id)) {
            String[] attack = getAttacksId(id);
            return attack[3];
        } else {
            return null;
        }
    }

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
    public String[] getAttackDamageAndType(int id) {
         if (!movimientos.containsKey(id)) return null;
         String[] ataque = movimientos.get(id);
         String tipo = ataque[3];
         String poder = ataque[5];
         if (poder == null || poder.isEmpty()) {
             poder = "0";
         }
         String[] info = {tipo,poder};
         return info;
     }

     //metodo para maquina (Experta)
     public ArrayList<String[]> getCompatibleAttacks(int pokemonId) {
        ArrayList<String[]> compatibleAttacks = new ArrayList<>();
        PokemonRepository pokemonRepo = new PokemonRepository();

        String[] pokemonInfo = pokemonRepo.getPokemonId(pokemonId);
        if (pokemonInfo == null) return compatibleAttacks;
        String pokemonType = pokemonInfo[2];
        String pokemonType2 = pokemonInfo[3] == "" ? pokemonType : pokemonInfo[3];
        for (Map.Entry<Integer, String[]> entry : movimientos.entrySet()) {
            String[] attack = entry.getValue();
            String attackType = attack[3];
            try {
                if (pokemonType.equalsIgnoreCase(attackType) || pokemonType2.equalsIgnoreCase(attackType) || attackType.equalsIgnoreCase("Normal")) {
                    compatibleAttacks.add(attack);
                }
            } catch (Exception e) {
                compatibleAttacks.add(attack);
                Log.record(e);
            }
        }
        return compatibleAttacks;
    }
}
