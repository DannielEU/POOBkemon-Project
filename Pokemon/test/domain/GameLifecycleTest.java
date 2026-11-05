package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameLifecycleTest {

    private POOBkemon game;
    private ArrayList<String> trainers;
    private HashMap<String, ArrayList<Integer>> pokemons;
    private HashMap<String, String[][]> items;
    private HashMap<String, ArrayList<Integer>> attacks;

    @BeforeEach
    void setUp() {
        POOBkemon.resetInstance();
        game = POOBkemon.getInstance();
        trainers = new ArrayList<>(List.of("Player1", "Defensive1"));
        pokemons = new HashMap<>();
        pokemons.put("Player1", new ArrayList<>(List.of(1, 2)));
        pokemons.put("Defensive1", new ArrayList<>(List.of(4, 5)));
        items = new HashMap<>();
        items.put("Player1", new String[][]{{"Potion", "2", "20"}});
        items.put("Defensive1", new String[][]{{"Potion", "2", "50"}, {"Revive", "1"}});
        attacks = new HashMap<>();
        attacks.put("Player1", new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8)));
        attacks.put("Defensive1", new ArrayList<>(List.of(9, 10, 11, 12, 13, 14, 15, 16)));
    }

    @Test
    void reinitializingStartsAFreshGame() throws POOBkemonException {
        game.initGame(trainers, pokemons, items, attacks, false);
        game.takeDecision(Decision.run(0));
        assertTrue(game.finishBattle());
        assertEquals(1, game.getWinner());

        game.initGame(trainers, pokemons, items, attacks, false);

        assertFalse(game.finishBattle());
        assertThrows(POOBkemonException.class, game::getWinner);
        assertEquals(0, game.getTeams().get(0).getTrainer().getId());
        assertEquals(1, game.getTeams().get(1).getTrainer().getId());
        assertEquals(0, game.getTeams().get(0).getPokemons().get(0).getId());
    }

    @Test
    void incompleteDecisionsAreReportedInsteadOfCrashing() throws POOBkemonException {
        game.initGame(trainers, pokemons, items, attacks, false);
        assertThrows(POOBkemonException.class, () -> game.takeDecision(new String[]{"Attack", "1", "0"}));
        assertThrows(POOBkemonException.class, () -> game.takeDecision(new String[]{"UseItem", "0", "0"}));
        assertThrows(POOBkemonException.class, () -> game.takeDecision(new String[]{"Attack", "x", "0", "0"}));
    }

    @Test
    void unknownTrainerIsRejected() throws POOBkemonException {
        game.initGame(trainers, pokemons, items, attacks, false);
        assertThrows(POOBkemonException.class, () -> game.changePokemon(99, 0));
        assertThrows(POOBkemonException.class, () -> game.getInfoItems(99));
    }

    @Test
    void savedGameWithMachineTrainerCanBeRestored(@TempDir File dir) throws POOBkemonException {
        game.initGame(trainers, pokemons, items, attacks, false);
        File file = new File(dir, "battle.poob");

        game.save(file);
        POOBkemon restored = POOBkemon.open(file);

        assertEquals(2, restored.getTeams().size());
        assertTrue(restored.isMachine(1));
        assertFalse(restored.isMachine(0));
    }

    @Test
    void defensiveMachineHealsWithTheRealItemName() throws POOBkemonException {
        game.initGame(trainers, pokemons, items, attacks, false);
        Team machineTeam = game.getTeams().get(1);
        Pokemon active = machineTeam.getPokemonById(machineTeam.getTrainer().getCurrentPokemonId());
        active.currentHealth = (int) (active.maxHealth * 0.2);

        String[] decision = game.machineDecision(1);

        assertEquals(Decision.USE_ITEM, decision[0]);
        assertEquals("superPotion", decision[3]);
        assertDoesNotThrow(() -> game.takeDecision(decision));
    }

    @Test
    void survivalModeCreatesLevel100PokemonAndNoItems() throws POOBkemonException {
        Survive.resetInstance();
        Survive survive = Survive.getInstance();
        survive.initGame(trainers, pokemons, items, attacks, false);

        assertEquals(100, survive.getTeams().get(0).getPokemons().get(0).level);
        assertEquals(0, survive.getInfoItems(0).length);
    }

    @Test
    void offensiveMachineAttacksWithAvailableMove() throws POOBkemonException {
        trainers.set(1, "Offensive1");
        pokemons.put("Offensive1", pokemons.get("Defensive1"));
        items.put("Offensive1", items.get("Defensive1"));
        attacks.put("Offensive1", attacks.get("Defensive1"));
        game.initGame(trainers, pokemons, items, attacks, false);

        String[] decision = game.machineDecision(1);

        assertEquals(Decision.ATTACK, decision[0]);
    }
}
