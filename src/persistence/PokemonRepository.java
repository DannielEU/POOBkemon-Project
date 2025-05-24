package persistence;

import domain.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class

PokemonRepository {

    private static final String POKEMONS_ARCHIVE = "resources/csv/Pokemones.csv";

    private static TreeMap<Integer,String[]> pokemones = new TreeMap<>();
    
    public PokemonRepository(){
    	List<String> pokemonsIput = null;
        try {
            pokemonsIput = Files.readAllLines(Paths.get(POKEMONS_ARCHIVE));

        } catch (IOException e) {
            Log.record(e);
        }

        for (int i = 1; i < pokemonsIput.size(); i++) {
            //ID_0,"Name"_1,"Type1"_3,"Type2"_4,"HP"_6,"Attack"_7,"Defense"_8,"Sp. Atk"_9,"Sp. Def"_10,"Speed"_11,
            String[] valores = pokemonsIput.get(i).split(",");
            this.pokemones.put(Integer.parseInt(valores[0]),valores);
        }
    	
    }

    public ArrayList<String[]> getPokemons() {
        ArrayList<String[]> pokemones = new ArrayList<>();
        for (String[] s:this.pokemones.values()) {
            //ID_0,"Name"_1,"Type1"_3,"Type2"_4,"HP"_6,"Attack"_7,"Defense"_8,"Sp. Atk"_9,"Sp. Def"_10,"Speed"_11,
            pokemones.add(s);
        }
        return pokemones;
    }

    public String[] getPokemonId(int id) {
        if (pokemones.containsKey(id)) {
            return pokemones.get(id);
        } else {
            return null;
        }
    }
}
