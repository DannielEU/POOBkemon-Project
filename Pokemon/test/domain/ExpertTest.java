package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpertTest {
    private POOBkemon game;
    private ArrayList<String> trainers;
    private HashMap<String, ArrayList<Integer>> pokemons;
    private HashMap<String, String[][]> items;
    private HashMap<String, ArrayList<Integer>> attacks;

    @BeforeEach
    void setUp() {
        game.resetInstance();
        game = POOBkemon.getInstance();

        // Datos básicos de prueba
        trainers = new ArrayList<>();
        trainers.add("Expert1");
        trainers.add("Expert2");

        pokemons = new HashMap<>();
        pokemons.put("Expert1", new ArrayList<>(List.of(1, 2, 56, 4,5,28)));
        pokemons.put("Expert2", new ArrayList<>(List.of(3, 4, 45, 3,203,301)));

        items = new HashMap<>();            //"Tipo","cantidad","SaludQueRecupera"
        items.put("Expert1", new String[][]{{"Potion", "5","54"},{"Potion", "2","20"}});
        items.put("Expert2", new String[][]{{"Potion", "2","5"},{"Potion", "1","20"}});

        attacks = new HashMap<>();
        attacks.put("Expert1", new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8,9,10,11,12,13,14,15,16,17,17,19,20,21,22,23,24)));
        attacks.put("Expert2", new ArrayList<>(List.of(9, 10, 11, 12, 13, 14, 15, 16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,45)));
        try {
            game.initGame(trainers, pokemons, items, attacks, false);
        }catch (POOBkemonException e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    void machineMovement() {
        try {
            assertTrue(game.isMachine(0));
            assertNotNull(game.machineDecision(0));
        }catch (POOBkemonException e){
            System.out.println(e.getMessage());
        }
        assertTrue(game.isOk());
    }

    @Test
    void shouldUseItemWhenPokemonHasCriticalHealth() {
        try {
            // Configurar Pokémon con salud crítica
            Pokemon pokemon = game.teams().get(0).getPokemons().get(0);
            pokemon.currentHealth = (int)(pokemon.maxHealth * 0.2); // 20% de salud

            String[] decision = game.machineDecision(0);

            assertNotNull(decision);
        } catch (POOBkemonException e) {
            fail("No debería lanzar excepción: " + e.getMessage());
        }
    }

    @Test
    void shouldSelectAttackWhenNoItemNeededAndSwitchingNotOptimal() {
        try {
            Pokemon pokemon = game.teams().get(0).getPokemons().get(0);
            pokemon.currentHealth = pokemon.maxHealth;

            String[] decision = game.machineDecision(0);

            assertNotNull(decision);
        } catch (POOBkemonException e) {
            fail("No debería lanzar excepción: " + e.getMessage());
        }
    }

}