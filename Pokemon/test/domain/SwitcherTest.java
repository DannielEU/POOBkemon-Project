package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SwitcherTest {
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
        trainers.add("Random1");
        trainers.add("Random2");

        pokemons = new HashMap<>();
        pokemons.put("Random1", new ArrayList<>(List.of(1, 2, 56, 4,5,28)));
        pokemons.put("Random2", new ArrayList<>(List.of(3, 4, 45, 3,203,301)));

        items = new HashMap<>();            //"Tipo","cantidad","SaludQueRecupera"
        items.put("Random1", new String[][]{{"Potion", "5","54"},{"Potion", "2","20"}});
        items.put("Random2", new String[][]{{"Potion", "2","5"},{"Potion", "1","20"}});

        attacks = new HashMap<>();
        attacks.put("Random1", new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8,9,10,11,12,13,14,15,16,17,17,19,20,21,22,23,24)));
        attacks.put("Random2", new ArrayList<>(List.of(9, 10, 11, 12, 13, 14, 15, 16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,45)));
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
    void shouldSwitchWhenLowHealth() throws POOBkemonException {
        Pokemon activePokemon = game.teams().get(0).getPokemons().get(0);
        activePokemon.currentHealth = (int)(activePokemon.maxHealth * 0.2);
        String[] decision = game.machineDecision(0);

        assertNotNull(decision);
        assertEquals("ChangePokemon", decision[0], "Debería cambiar de Pokémon cuando la salud es baja");
    }

    @Test
    void shouldAttackWithTypeAdvantage() throws POOBkemonException {
        Pokemon activePokemon = game.teams().get(0).getPokemons().get(0);
        activePokemon.currentHealth = activePokemon.maxHealth; // Vida completa
        String[] decision = game.machineDecision(0);

        assertNotNull(decision);
        assertTrue(decision[0].equals("Attack") || decision[0].equals("ChangePokemon"), "Debería atacar o cambiar basado en la ventaja de tipo");
    }
    
    @Test
    void shouldReturnValidDecisionFormat() throws POOBkemonException {
        String[] decision = game.machineDecision(0);
        assertNotNull(decision);
        assertTrue(decision.length >= 2, "La decisión debe tener al menos 2 elementos");
        assertTrue(decision[0].equals("Attack") || decision[0].equals("ChangePokemon"), "La decisión debe ser Attack o ChangePokemon");
    }

    @Test
    void shouldConsiderMultiplePokemonOptions() throws POOBkemonException {
        Team team = game.teams().get(0);
        assertTrue(team.getPokemons().size() > 1,
                "Debe haber más de un Pokémon para probar las opciones");

        String[] decision = game.machineDecision(0);

        assertNotNull(decision);
        if (decision[0].equals("ChangePokemon")) {
            int newPokemonId = Integer.parseInt(decision[2]);
            assertTrue(newPokemonId > 0,
                    "El ID del nuevo Pokémon debe ser válido");
        }
    }
}