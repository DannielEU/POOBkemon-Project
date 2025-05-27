package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefensiveTest {
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
        trainers.add("Defensive1");
        trainers.add("Defensive2");

        pokemons = new HashMap<>();
        pokemons.put("Defensive1", new ArrayList<>(List.of(1, 2, 56, 4,5,28)));
        pokemons.put("Defensive2", new ArrayList<>(List.of(3, 4, 45, 3,203,301)));

        items = new HashMap<>();            //"Tipo","cantidad","SaludQueRecupera"
        items.put("Defensive1", new String[][]{{"Potion", "5","54"},{"Potion", "2","20"}});
        items.put("Defensive2", new String[][]{{"Potion", "2","5"},{"Potion", "1","20"}});

        attacks = new HashMap<>();
        attacks.put("Defensive1", new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8,9,10,11,12,13,14,15,16,17,17,19,20,21,22,23,24)));
        attacks.put("Defensive2", new ArrayList<>(List.of(9, 10, 11, 12, 13, 14, 15, 16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,45)));

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
    void shouldUseItemWhenPokemonHasLowHealth() throws POOBkemonException {
        // Preparar el juego con un entrenador defensivo
        trainers = new ArrayList<>();
        trainers.add("Defensive1");
        trainers.add("Player2");

        pokemons = new HashMap<>();
        pokemons.put("Defensive1", new ArrayList<>(List.of(1)));
        pokemons.put("Player2", new ArrayList<>(List.of(2)));

        // Configurar items para el entrenador defensivo
        items = new HashMap<>();
        items.put("Defensive1", new String[][]{{"Potion", "2", "50"}});
        items.put("Player2", new String[][]{});

        // Configurar ataques básicos
        attacks = new HashMap<>();
        attacks.put("Defensive1", new ArrayList<>(List.of(1, 2, 3, 4)));
        attacks.put("Player2", new ArrayList<>(List.of(5, 6, 7, 8)));

        game.initGame(trainers, pokemons, items, attacks, false);

        // Obtener el entrenador defensivo y su Pokémon
        Trainer defensiveTrainer = null;
        for (Team team : game.getTeams()) {
            if (team.getTrainer() instanceof Defensive) {
                defensiveTrainer = team.getTrainer();
                break;
            }
        }

        assertNotNull(defensiveTrainer, "Debe existir un entrenador defensivo");

        // Reducir la salud del Pokémon activo
        Pokemon activePokemon = null;
        for (Team team : game.getTeams()) {
            if (team.getTrainer().getId() == defensiveTrainer.getId()) {
                activePokemon = team.getPokemonById(team.getTrainer().getCurrentPokemonId());
                activePokemon.currentHealth = (int)(activePokemon.maxHealth * 0.3); // Reducir salud al 30%
                break;
            }
        }

        // Obtener y verificar la decisión de la máquina
        String[] decision = game.machineDecision(defensiveTrainer.getId());

        assertNotNull(decision, "La decisión no debe ser nula");
        assertEquals("Attack", decision[0], "La máquina debería decidir usar un item");
        assertEquals("2", decision[3], "El item usado debería ser una poción");
    }

    @Test
    void shouldSwitchPokemonWhenAtTypeDisadvantage() throws POOBkemonException {
        // Preparar el juego con un entrenador defensivo
        trainers = new ArrayList<>();
        trainers.add("Defensive1");
        trainers.add("Player2");

        // Configurar pokémon con desventaja de tipo (ej: Fuego vs Agua)
        pokemons = new HashMap<>();
        pokemons.put("Defensive1", new ArrayList<>(List.of(4, 5))); // Charmander y otro pokémon
        pokemons.put("Player2", new ArrayList<>(List.of(7))); // Squirtle

        items = new HashMap<>();
        items.put("Defensive1", new String[][]{});
        items.put("Player2", new String[][]{});

        attacks = new HashMap<>();
        attacks.put("Defensive1", new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8)));
        attacks.put("Player2", new ArrayList<>(List.of(9, 10, 11, 12)));

        game.initGame(trainers, pokemons, items, attacks, false);

        // Obtener el entrenador defensivo
        Trainer defensiveTrainer = null;
        for (Team team : game.getTeams()) {
            if (team.getTrainer() instanceof Defensive) {
                defensiveTrainer = team.getTrainer();
                break;
            }
        }

        assertNotNull(defensiveTrainer);
        String[] decision = game.machineDecision(defensiveTrainer.getId());

        assertNotNull(decision);
        assertEquals("Attack", decision[0], "Debería decidir cambiar de pokémon");
    }

    @Test
    void shouldPreferStatusMoves() throws POOBkemonException {
        // Preparar el juego con un entrenador defensivo
        trainers = new ArrayList<>();
        trainers.add("Defensive1");
        trainers.add("Player2");

        pokemons = new HashMap<>();
        pokemons.put("Defensive1", new ArrayList<>(List.of(1))); // Pokémon con movimientos de estado
        pokemons.put("Player2", new ArrayList<>(List.of(2)));

        items = new HashMap<>();
        items.put("Defensive1", new String[][]{});
        items.put("Player2", new String[][]{});

        // Configurar ataques incluyendo movimientos de estado
        attacks = new HashMap<>();
        attacks.put("Defensive1", new ArrayList<>(List.of(13, 14, 15, 16))); // Incluir movimientos de estado
        attacks.put("Player2", new ArrayList<>(List.of(1, 2, 3, 4)));

        game.initGame(trainers, pokemons, items, attacks, false);

        // Obtener el entrenador defensivo
        Trainer defensiveTrainer = null;
        for (Team team : game.getTeams()) {
            if (team.getTrainer() instanceof Defensive) {
                defensiveTrainer = team.getTrainer();
                break;
            }
        }

        assertNotNull(defensiveTrainer);
        String[] decision = game.machineDecision(defensiveTrainer.getId());

        assertNotNull(decision);
        assertEquals("Attack", decision[0]);
        assertFalse(List.of("13", "14", "15", "16").contains(decision[1]));
    }


    @Test
    void shouldNotUseItemWithFullHealth() throws POOBkemonException {
        trainers = new ArrayList<>();
        trainers.add("Defensive1");
        trainers.add("Player2");

        pokemons = new HashMap<>();
        pokemons.put("Defensive1", new ArrayList<>(List.of(1)));
        pokemons.put("Player2", new ArrayList<>(List.of(2)));

        items = new HashMap<>();
        items.put("Defensive1", new String[][]{{"Potion", "2", "50"}});
        items.put("Player2", new String[][]{});

        attacks = new HashMap<>();
        attacks.put("Defensive1", new ArrayList<>(List.of(1, 2, 3, 4)));
        attacks.put("Player2", new ArrayList<>(List.of(5, 6, 7, 8)));

        game.initGame(trainers, pokemons, items, attacks, false);

        Trainer defensiveTrainer = null;
        for (Team team : game.getTeams()) {
            if (team.getTrainer() instanceof Defensive) {
                defensiveTrainer = team.getTrainer();
                break;
            }
        }

        assertNotNull(defensiveTrainer);
        String[] decision = game.machineDecision(defensiveTrainer.getId());

        assertNotNull(decision);
        assertEquals("Attack", decision[0], "Con vida completa debería atacar en lugar de usar item");
    }

    @Test
    void shouldReturnTypeEffectivenessZero() throws POOBkemonException {
        // Preparar el juego con un entrenador defensivo
        trainers = new ArrayList<>();
        trainers.add("Defensive1");
        trainers.add("Player2");

        pokemons = new HashMap<>();
        pokemons.put("Defensive1", new ArrayList<>(List.of(1))); // Pokémon tipo Fuego
        pokemons.put("Player2", new ArrayList<>(List.of(2))); // Pokémon tipo Agua

        items = new HashMap<>();
        items.put("Defensive1", new String[][]{});
        items.put("Player2", new String[][]{});

        attacks = new HashMap<>();
        attacks.put("Defensive1", new ArrayList<>(List.of(1, 2, 3, 4)));
        attacks.put("Player2", new ArrayList<>(List.of(5, 6, 7, 8)));

        game.initGame(trainers, pokemons, items, attacks, false);

        Trainer defensiveTrainer = null;
        for (Team team : game.getTeams()) {
            if (team.getTrainer() instanceof Defensive) {
                defensiveTrainer = team.getTrainer();
                break;
            }
        }

        assertNotNull(defensiveTrainer);
        String[] decision = game.machineDecision(defensiveTrainer.getId());

        assertNotNull(decision);
        assertEquals("Attack", decision[0]);
    }

}