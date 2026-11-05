package presentacion;

import domain.POOBkemon;
import domain.POOBkemonException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameSetupTest {

    private GameSetup setup;

    @BeforeEach
    void setUp() {
        setup = new GameSetup();
        setup.startSetup("Player1", "Player2");
    }

    @Test
    void validNamesAreAccepted() {
        assertNull(GameSetup.validatePlayerNames("Ash", "Gary"));
    }

    @Test
    void invalidNamesReportTheReason() {
        assertNotNull(GameSetup.validatePlayerNames("", "Gary"));
        assertNotNull(GameSetup.validatePlayerNames("Ash", "Ash"));
        assertNotNull(GameSetup.validatePlayerNames("Ash", "NombreDemasiadoLargo"));
        assertNotNull(GameSetup.validatePlayerNames("Al", "Gary"));
    }

    @Test
    void startSetupCreatesFullRandomTeamsAndEmptyBags() {
        for (int player = 0; player < 2; player++) {
            assertEquals(GameSetup.TEAM_SIZE, setup.pokemonsOf(player).size());
            assertEquals(GameSetup.TEAM_SIZE * GameSetup.MOVES_PER_POKEMON, setup.movesOf(player).size());
            for (String[] item : setup.itemsOf(player)) {
                assertEquals("0", item[1]);
            }
        }
        assertTrue(setup.pokemonsOf(0).stream().allMatch(id -> id >= 1 && id <= POOBkemon.pokemonCatalogSize()));
        assertTrue(setup.movesOf(0).stream().allMatch(id -> id >= 1 && id <= POOBkemon.moveCatalogSize()));
    }

    @Test
    void bagsAreIndependentBetweenPlayersAndSetups() {
        setup.addItem(0, 0);
        assertEquals("1", setup.itemsOf(0)[0][1]);
        assertEquals("0", setup.itemsOf(1)[0][1]);

        setup.startSetup("Player1", "Player2");
        assertEquals("0", setup.itemsOf(0)[0][1]);
    }

    @Test
    void randomStatsToggles() {
        assertFalse(setup.isRandomStats());
        assertTrue(setup.toggleRandomStats());
        assertFalse(setup.toggleRandomStats());
    }

    @Test
    void createGameBuildsANormalBattle() throws POOBkemonException {
        setup.startSetup("Player1", "Defensive2");

        POOBkemon game = setup.createGame(GameSetup.Mode.NORMAL);

        assertEquals(2, game.getTeams().size());
        assertTrue(game.isMachine(1));
        assertEquals(1, game.getTeams().get(0).getPokemons().get(0).level);
    }

    @Test
    void createGameBuildsASurvivalBattleAtLevel100() throws POOBkemonException {
        POOBkemon game = setup.createGame(GameSetup.Mode.SURVIVAL);

        assertEquals(100, game.getTeams().get(0).getPokemons().get(0).level);
    }
}
