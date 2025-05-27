package domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TeamTest {

    @Test
    void shouldChangePokemon() throws POOBkemonException {
        ArrayList<Item> items = new ArrayList<>();
        BagPack bagpack = new BagPack(items);

        Trainer trainer = new Defensive(1, bagpack);
        ArrayList<Pokemon> pokemons = getPokemons();
        Team team = new Team(pokemons, trainer);
        team.changePokemon(2);

        assertEquals(2, team.getPokemonById(team.getTrainer().getCurrentPokemonId()).getId());
    }

    private static ArrayList<Pokemon> getPokemons() throws POOBkemonException {
        ArrayList<Pokemon> pokemons = new ArrayList<>();

        String[] pokemonInfo1 = {"1", "Pikachu", "ELECTRIC", "Static", "70", "35", "55", "30", "50", "40", "90"};
        Pokemon pokemon1 = new Pokemon(1, pokemonInfo1, new ArrayList<>(), false, 1);

        String[] pokemonInfo2 = {"2", "Raichu", "ELECTRIC", "Static", "70", "35", "55", "30", "50", "40", "90"};
        Pokemon pokemon2 = new Pokemon(2, pokemonInfo2, new ArrayList<>(), false, 2);
        pokemons.add(pokemon1);
        pokemons.add(pokemon2);
        return pokemons;
    }

    @Test
    void shouldGetPokemonsInactive() throws POOBkemonException {
        ArrayList<Item> items = new ArrayList<>();
        BagPack bagpack = new BagPack(items);
        Trainer trainer = new Defensive(1, bagpack);

        ArrayList<Pokemon> pokemons = new ArrayList<>();
        String[] pokemonInfo1 = {"1", "Pikachu", "ELECTRIC", "Static", "70", "35", "55", "30", "50", "40", "90"};
        Pokemon pokemon1 = new Pokemon(1, pokemonInfo1, new ArrayList<>(), false, 1);

        String[] pokemonInfo2 = {"2", "Raichu", "ELECTRIC", "Static", "70", "35", "55", "30", "50", "40", "90"};
        Pokemon pokemon2 = new Pokemon(2, pokemonInfo2, new ArrayList<>(), false, 2);

        String[] pokemonInfo3 = {"3", "Jolteon", "ELECTRIC", "Static", "70", "35", "55", "30", "50", "40", "90"};
        Pokemon pokemon3 = new Pokemon(3, pokemonInfo3, new ArrayList<>(), false, 3);

        pokemons.add(pokemon1);
        pokemons.add(pokemon2);
        pokemons.add(pokemon3);

        Team team = new Team(pokemons, trainer);

        int[] inactivePokemons = team.getPokemonsInactive(1);

        assertEquals(2, inactivePokemons.length, "Debe haber 2 pokémon inactivos");
        assertEquals(2, inactivePokemons[0], "El primer pokémon inactivo debe tener ID 2");
        assertEquals(3, inactivePokemons[1], "El segundo pokémon inactivo debe tener ID 3");
    }


}
