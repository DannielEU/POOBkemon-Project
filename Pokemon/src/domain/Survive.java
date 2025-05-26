package domain;

import persistence.PokemonRepository;

import java.util.ArrayList;
import java.util.HashMap;

public class Survive extends POOBkemon {
    private static Survive instance;
    private int pokemonLvl = 100;

    private Survive() {
        super();
    }

    public static Survive getInstance() {
        if (instance == null) {
            instance = new Survive();
        }
        return instance;
    }
    @Override
    public Pokemon createPokemon(int id, ArrayList<Integer> attackIds)throws POOBkemonException{
        PokemonRepository info = new PokemonRepository();
        String[] infoPokemon = info.getPokemonId(id);
        Pokemon pokemon = new Pokemon(nid,infoPokemon,attackIds, this.random, this.pokemonLvl);
        this.nextIdPokemon();
        return pokemon;
    }

    @Override
    public void initGame(ArrayList<String> trainers,
                         HashMap<String, ArrayList<Integer>> pokemons,
                         HashMap<String, String[][]> items,
                         HashMap<String, ArrayList<Integer>> attacks,
                         boolean random) throws POOBkemonException {
        this.pokemonLvl = 100;
        super.initGame(trainers, pokemons, items, attacks, random);
    }
    @Override
    public String[][] getInfoItems(int trainerId) throws POOBkemonException {
        return new String[][]{};
    }
}
