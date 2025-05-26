package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class SurviveTest{
    private Survive game;
    private ArrayList<String> trainers;
    private HashMap<String, ArrayList<Integer>> pokemons;
    private HashMap<String, String[][]> items;
    private HashMap<String, ArrayList<Integer>> attacks;

    @BeforeEach
    void setUp() {
        game.resetInstance();
        game = Survive.getInstance();

        // Datos básicos de prueba
        trainers = new ArrayList<>();
        trainers.add("Player1");
        trainers.add("Player2");

        pokemons = new HashMap<>();
        pokemons.put("Player1", new ArrayList<>(List.of(1, 2, 56, 4,5,28)));
        pokemons.put("Player2", new ArrayList<>(List.of(3, 4, 45, 3,203,301)));

        items = new HashMap<>();            //"Tipo","cantidad","SaludQueRecupera"
        items.put("Player1", new String[][]{{"Potion", "5","54"},{"Potion", "2","20"}});
        items.put("Player2", new String[][]{{"Potion", "2","5"},{"Potion", "1","20"}});

        attacks = new HashMap<>();
        attacks.put("Player1", new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8,9,10,11,12,13,14,15,16,17,17,19,20,21,22,23,24)));
        attacks.put("Player2", new ArrayList<>(List.of(9, 10, 11, 12, 13, 14, 15, 16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,45)));
        try {
            game.initGame(trainers, pokemons, items, attacks, false);
        }catch (POOBkemonException e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Singleton instance")
    void testSingletonInstance() {
        Survive anotherInstance = Survive.getInstance();
        assertSame(game, anotherInstance);
    }

    @Test
    @DisplayName("Test initial game state")
    void testInitialState() {
        assertFalse(!game.isOk());
        assertFalse(game.finishBattle());
        assertNotNull(game.teams());
    }

    @Test
    @DisplayName("Test successful game initialization")
    void testInitGameSuccess() {
        try {
            game.initGame(trainers, pokemons, items, attacks, false);
        }catch (POOBkemonException e){
            System.out.println(e.getMessage());
        }
        assertTrue(game.isOk());
        assertEquals(2, game.teams().size());
        assertEquals("Start Game", game.getMoves().get(0));
        assertEquals(2, game.getOrder().size());
    }
    @Test
    @DisplayName("Test game verify Machine")
    void shouldCreateTwoMachineOffensive() throws POOBkemonException{
        trainers = new ArrayList<>();
        trainers.add("Offensive1");
        trainers.add("Offensive2");

        pokemons = new HashMap<>();
        pokemons.put("Offensive1", new ArrayList<>(List.of(1, 2, 56, 4,5,28)));
        pokemons.put("Offensive2", new ArrayList<>(List.of(3, 4, 45, 3,203,301)));

        items = new HashMap<>();
        items.put("Offensive1", new String[][]{{"Potion", "5","54"},{"Potion", "2","20"}});
        items.put("Offensive2", new String[][]{{"Potion", "2","5"},{"Potion", "1","20"}});

        attacks = new HashMap<>();
        attacks.put("Offensive1", new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8,9,10,11,12,13,14,15,16,17,17,19,20,21,22,23,24)));
        attacks.put("Offensive2", new ArrayList<>(List.of(9, 10, 11, 12, 13, 14, 15, 16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,45)));

        game.initGame(trainers, pokemons, items, attacks, false);
        Trainer trainerOffensive = null;
        for(Team t : game.getTeams()){
            trainerOffensive = t.getTrainer();
            if (trainerOffensive instanceof Offensive) break;
        }
        assertTrue(game.isMachine(trainerOffensive.getId()));
    }


    @Test
    @DisplayName("Test verify Info")
    void shouldGiveInfoMoves(){
        assertDoesNotThrow(()->game.getMoveInfo(3));
        assertDoesNotThrow(()->game.getMoveInfo(10));
        assertDoesNotThrow(()->game.getMoveInfo(11));
        assertDoesNotThrow(()->game.getMoveInfo(12));
        assertDoesNotThrow(()->game.getMoveInfo(13));
        assertDoesNotThrow(()->game.getMoveInfo(14));
        assertDoesNotThrow(()->game.getMoveInfo(15));
        assertDoesNotThrow(()->game.getMoveInfo(16));
        assertDoesNotThrow(()->game.getMoveInfo(17));
        assertDoesNotThrow(()->game.getMoveInfo(19));
        assertDoesNotThrow(()->game.getMoveInfo(20));
        assertDoesNotThrow(()->game.getMoveInfo(21));
        assertDoesNotThrow(()->game.getMoveInfo(22));
        assertDoesNotThrow(()->game.getMoveInfo(23));
        assertDoesNotThrow(()->game.getMoveInfo(24));
        assertDoesNotThrow(()->game.getMoveInfo(25));
    }

    @Test
    @DisplayName("Test verify Info")
    void shouldGiveInfoItems(){
        assertDoesNotThrow(()->game.getItemInfo());
    }

    @Test
    @DisplayName("Test verify Info")
    void shouldGiveInfoPokemon(){
        assertDoesNotThrow(()->game.getPokInfo());
    }

    @Test
    @DisplayName("Test game verify Machine")
    void shouldMachineOffensiveAttackVerify() throws POOBkemonException{
        trainers = new ArrayList<>();
        trainers.add("Player1");
        trainers.add("Offensive2");

        pokemons = new HashMap<>();
        pokemons.put("Player1", new ArrayList<>(List.of(1, 2, 56, 4,5,28)));
        pokemons.put("Offensive2", new ArrayList<>(List.of(3, 4, 45, 3,203,301)));

        items = new HashMap<>();
        items.put("Player1", new String[][]{{"Potion", "5","54"},{"Potion", "2","20"}});
        items.put("Offensive2", new String[][]{{"Potion", "2","5"},{"Potion", "1","20"}});

        attacks = new HashMap<>();
        attacks.put("Player1", new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8,9,10,11,12,13,14,15,16,17,17,19,20,21,22,23,24)));
        attacks.put("Offensive2", new ArrayList<>(List.of(9, 10, 11, 12, 13, 14, 15, 16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,45)));

        game.initGame(trainers, pokemons, items, attacks, false);
        Trainer trainerOffensive = null;
        for(Team t : game.getTeams()){
            trainerOffensive = t.getTrainer();
            if (trainerOffensive instanceof Offensive) break;
        }
        assertTrue(game.isMachine(trainerOffensive.getId()));
    }

    @Test
    @DisplayName("Test game with Offensive Machine")
    void shouldMachineOffensiveAttack() throws POOBkemonException{
        // Arrange - Configuración inicial del juego
        // Datos básicos de prueba
        trainers = new ArrayList<>();
        trainers.add("Offensive1");
        trainers.add("Player2");

        pokemons = new HashMap<>();
        pokemons.put("Offensive1", new ArrayList<>(List.of(1, 2, 56, 4,5,28)));
        pokemons.put("Player2", new ArrayList<>(List.of(3, 4, 45, 3,203,301)));

        items = new HashMap<>();
        items.put("Offensive1", new String[][]{{"Potion", "5","54"},{"Potion", "2","20"}});
        items.put("Player2", new String[][]{{"Potion", "2","5"},{"Potion", "1","20"}});

        attacks = new HashMap<>();
        attacks.put("Offensive1", new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8,9,10,11,12,13,14,15,16,17,17,19,20,21,22,23,24)));
        attacks.put("Player2", new ArrayList<>(List.of(9, 10, 11, 12, 13, 14, 15, 16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,45)));

        game.initGame(trainers, pokemons, items, attacks, false);
        Trainer trainerOffensive = null;
        for(Team t : game.getTeams()){
            trainerOffensive = t.getTrainer();
            if (trainerOffensive instanceof Offensive) break;
        }
        assertTrue(game.isMachine(trainerOffensive.getId()));
        String[] machineMove = game.machineDecision(trainerOffensive.getId());
        assertNotNull(machineMove);
        assertTrue(machineMove.length > 0);

        assertDoesNotThrow(()->game.takeDecision(machineMove));
    }
    @Test
    @DisplayName("Test Attack Pokemon")
    void shouldAttackAnotherPokemon() throws POOBkemonException {
        // Arrange - Configuración inicial del juego
        game.initGame(trainers, pokemons, items, attacks, false);

        // Obtener el orden de turno y verificar
        ArrayList<Integer> turnOrder = game.getOrder();
        assertEquals(2, turnOrder.size(), "Debe haber exactamente 2 entrenadores");

        // Identificar equipos y entrenadores
        Team attackingTeam = null;
        Team defendingTeam = null;

        for (Team team : game.getTeams()) {
            if (team.getTrainer().getId() == turnOrder.get(0)) {
                attackingTeam = team;
            } else if (team.getTrainer().getId() == turnOrder.get(1)) {
                defendingTeam = team;
            }
        }

        assertNotNull(attackingTeam, "Equipo atacante no encontrado");
        assertNotNull(defendingTeam, "Equipo defensor no encontrado");

        // Obtener Pokémon atacante y defensor
        Pokemon attacker = attackingTeam.getPokemonById(attackingTeam.getTrainer().getCurrentPokemonId());
        Pokemon defender = defendingTeam.getPokemonById(defendingTeam.getTrainer().getCurrentPokemonId());

        assertTrue(attacker.getActive(), "Pokémon atacante debe estar activo");
        assertTrue(defender.getActive(), "Pokémon defensor debe estar activo");

        // Obtener el primer ataque del Pokémon atacante
        Attack attackToUse = attacker.getAttacks().get(3);
        int initialDefenderHP = defender.currentHealth;
        int initialPP = attackToUse.getPPActual();

        String[] attackDecision = {
                "Attack",
                String.valueOf(attackToUse.getIdInside()),
                String.valueOf(attacker.getId()),
                String.valueOf(attackingTeam.getTrainer().getId())
        };

        assertDoesNotThrow(() -> game.takeDecision(attackDecision),
                "El ataque no debería lanzar excepción");

        // Assert - Verificaciones post-ataque
        // 1. Verificar que el PP del ataque disminuyó
        assertEquals(initialPP - 1, attackToUse.getPPActual(),
                "El PP del ataque debería haber disminuido en 1");

        // 2. Verificar que el atacante sigue activo
        assertTrue(attacker.getActive(),
                "El Pokémon atacante debería seguir activo después del ataque");

        // 3. Verificar que el defensor sigue activo (a menos que se debilitara)
        if (defender.currentHealth > 0) {
            assertTrue(defender.getActive(),
                    "El Pokémon defensor debería seguir activo si no fue debilitado");
        }
        if(defender.currentHealth == 0){
            assertTrue(defender.getWeak());
        }
    }
    @Test
    @DisplayName("CreatePotis")
    void shouldCreatePotis() {
        try {
            game.initGame(trainers, pokemons, items, attacks, false);
        } catch (POOBkemonException e) {
            fail("No debería lanzar POOBkemonException: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("UsePotis")
    void shouldUsePotis() {
        try {
            int pokemonId = -1;
            game.initGame(trainers, pokemons, items, attacks, false);
            for(Team team : game.getTeams()){
                if(team.getTrainer().getId() == game.getOrder().get(0) ){
                    pokemonId = team.getTrainer().getCurrentPokemonId();
                    break;
                }
            }
            //"UseItem","IdTrainer","IdPokemon","Item (Nombre)" ------> se generan dependiendo si es Revive, o cantidad de Hps que restauran
            String[] decision = {"UseItem",String.valueOf(game.getOrder().get(0)), String.valueOf(pokemonId),"potion"};
            game.takeDecision(decision);
        } catch (POOBkemonException e) {
            fail(e.getMessage());
        }
    }


    @Test
    @DisplayName("Test game initialization with missing trainer data")
    void testInitGameMissingTrainerData() {
        assertThrows(POOBkemonException.class, () -> {
            game.initGame(null, pokemons, items, attacks, false);
        });
    }


    @Test
    @DisplayName("Test createPokemons with insufficient attacks")
    void testCreatePokemonsWithInsufficientAttacks() {
        assertThrows(IllegalArgumentException.class, () -> {
            game.createPokemons(
                    new ArrayList<>(List.of(1, 2)),
                    new ArrayList<>(List.of(1, 2, 3)) // Solo 3 ataques para 2 pokémones (necesita 8)
            );
        });
    }

    @Test
    @DisplayName("Test coin method for turn order")
    void testCoinMethod() throws POOBkemonException {
        game.initGame(trainers, pokemons, items, attacks, false);

        ArrayList<Integer> order = game.getOrder();
        assertEquals(2, order.size());
        assertNotEquals(order.get(0), order.get(1));
    }


    @Test
    @DisplayName("Test changePokemon method")
    void testChangePokemon() {
        try {
            game.initGame(trainers, pokemons, items, attacks, false);

            int trainerId = game.getOrder().get(0);
            Trainer entrenador = game.getTeams().get(0).getTrainer();
            int actualPokemon = entrenador.getCurrentPokemonId();

            String[] decision1 = {"ChangePokemon", "1"};
            String[] decision2 = {"Attack", "1", "0"};

            game.takeDecision(decision1);
            int cambiodePokemon = entrenador.getCurrentPokemonId();
            System.out.println(cambiodePokemon);
            assertTrue(cambiodePokemon == actualPokemon);
        }catch(POOBkemonException e){
            System.out.println(e.getMessage());
        }

    }

    @Test
    @DisplayName("Test getCurrentPokemons method")
    void testGetCurrentPokemons() throws POOBkemonException {
        game.initGame(trainers, pokemons, items, attacks, false);

        HashMap<Integer, String[]> currentPokemons = game.getCurrentPokemons();
        assertEquals(2, currentPokemons.size());

        for (String[] pokemonInfo : currentPokemons.values()) {
            assertNotNull(pokemonInfo);
            assertTrue(pokemonInfo.length > 0);
        }
    }

    @Test
    @DisplayName("Test getPokemonsInactive method")
    void testGetPokemonsInactive() throws POOBkemonException {
        game.initGame(trainers, pokemons, items, attacks, false);

        int trainerId = game.getOrder().get(0);
        int[] inactivePokemons = game.getPokemonsInactive(trainerId);

        assertTrue(inactivePokemons.length > 0);
    }

    @Test
    @DisplayName("Test run action ends battle")
    void testRunAction() throws POOBkemonException {
        game.initGame(trainers, pokemons, items, attacks, false);

        String[] decision1 = {"Run","1"};
        game.takeDecision(decision1);
        assertTrue(game.finishBattle());
    }

    @Test
    @DisplayName("Test getPokemonInfo method")
    void testGetPokemonInfo() throws POOBkemonException {
        game.initGame(trainers, pokemons, items, attacks, false);
        int pokemon = -1;
        int trainerId = game.getOrder().get(0);
        for(Team t : game.getTeams()){
            if(t.getTrainer().getId() == trainerId){
                pokemon = t.getPokemons().get(0).getId();
            }
        }
        String[] pokemonInfo = game.getPokemonInfo(trainerId, pokemon);

        for(String info : pokemonInfo){
            System.out.println(info);
        }

        assertNotNull(pokemonInfo);
        assertTrue(pokemonInfo.length > 0);
    }

    @Test
    @DisplayName("Test repository information methods")
    void testRepositoryMethods() {
        assertNotNull(game.getPokInfo());
        assertNotNull(game.getItemInfo());
        assertNotNull(game.getMoveInfo(1));
    }

    @Test
    @DisplayName("Test deleteGame method")
    void deleteGame() throws POOBkemonException {
        POOBkemon game = POOBkemon.getInstance();
        game.initGame(trainers, pokemons, items, attacks, false);
        game.resetInstance();
    }

    @Test
    @DisplayName("Test createPokemons method")
    void createPokemons() throws POOBkemonException {
        ArrayList<Integer> pokemonIds = new ArrayList<>(List.of(1, 2));
        ArrayList<Integer> attackIds = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8));
        ArrayList<Pokemon> pokemons = game.createPokemons(pokemonIds, attackIds);
        assertEquals(2, pokemons.size());
        assertEquals(4, pokemons.get(0).getAttacks().size());
        assertEquals(4, pokemons.get(1).getAttacks().size());
    }

    @Test
    @DisplayName("Test coin method for turn order")
    void coin() throws POOBkemonException {
        game.initGame(trainers, pokemons, items, attacks, false);
        ArrayList<Integer> order = game.coin();
        assertEquals(2, order.size());
        assertNotEquals(order.get(0), order.get(1));
    }

    @Test
    @DisplayName("Test machineDecision method")
    void machineDecision() throws POOBkemonException {
        game = Survive.getInstance();

        // Datos básicos de prueba
        trainers = new ArrayList<>();
        trainers.add("Player1");
        trainers.add("Offensive2");

        pokemons = new HashMap<>();
        pokemons.put("Player1", new ArrayList<>(List.of(1, 2, 56, 4,5,28)));
        pokemons.put("Offensive2", new ArrayList<>(List.of(3, 4, 45, 3,203,301)));

        items = new HashMap<>();            //"Tipo","cantidad","SaludQueRecupera"
        items.put("Player1", new String[][]{{"Potion", "5","54"},{"Potion", "2","20"}});
        items.put("Offensive2", new String[][]{{"Potion", "2","5"},{"Potion", "1","20"}});

        attacks = new HashMap<>();
        attacks.put("Player1", new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8,9,10,11,12,13,14,15,16,17,17,19,20,21,22,23,24)));
        attacks.put("Offensive2", new ArrayList<>(List.of(9, 10, 11, 12, 13, 14, 15, 16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,45)));
        game.initGame(trainers, pokemons, items, attacks, false);

        Trainer machineTrainer = null;
        for (Team t : game.getTeams()) {
            if (t.getTrainer() instanceof Offensive) {
                machineTrainer = t.getTrainer();
                break;
            }
        }

        assertNotNull(machineTrainer);
        String[] decision = game.machineDecision(machineTrainer.getId());
        assertNotNull(decision);
        assertTrue(decision.length > 0);
    }

    @Test
    @DisplayName("Test isMachine method with non-machine trainer")
    void isMachine_WithHumanTrainer() throws POOBkemonException {
        game.initGame(trainers, pokemons, items, attacks, false);
        int humanTrainerId = game.getOrder().get(0);
        assertFalse(game.isMachine(humanTrainerId));
    }


    @Test
    @DisplayName("Test getTeams method")
    void getTeams() throws POOBkemonException {
        game.initGame(trainers, pokemons, items, attacks, false);
        ArrayList<Team> teams = game.getTeams();
        assertEquals(2, teams.size());
        assertNotNull(teams.get(0).getTrainer());
        assertNotNull(teams.get(1).getTrainer());
    }

    @Test
    @DisplayName("Test getLastMoves method")
    void getLastMoves() throws POOBkemonException {
        game.initGame(trainers, pokemons, items, attacks, false);
        assertEquals("Start Game", game.getLastMoves());
    }

    @Test
    @DisplayName("Test getPokemonsPerTrainer method")
    void getPokemonsPerTrainer() throws POOBkemonException {
        game.initGame(trainers, pokemons, items, attacks, false);
        int trainerId = game.getOrder().get(0);
        int[] pokemonIds = game.getPokemonsPerTrainer(trainerId);
        assertEquals(6, pokemonIds.length);
    }

    @Test
    @DisplayName("Test getActiveAttacks method")
    void getActiveAttacks() throws POOBkemonException {
        game.initGame(trainers, pokemons, items, attacks, false);
        HashMap<Integer, String[][]> activeAttacks = game.getActiveAttacks();
        assertEquals(2, activeAttacks.size());
        for (String[][] attacks : activeAttacks.values()) {
            assertEquals(4, attacks.length); // 4 attacks per pokemon
        }
    }

    @Test
    @DisplayName("Test save and open methods")
    void save() throws POOBkemonException {
        game.initGame(trainers, pokemons, items, attacks, false);
        File tempFile = new File("temp_save.poob");

        // Test save
        game.save(tempFile);
        assertTrue(tempFile.exists());

        // Test open
        POOBkemon loadedGame = POOBkemon.open(tempFile);
        assertNotNull(loadedGame);
        assertEquals(2, loadedGame.getTeams().size());

        // Clean up
        tempFile.delete();
    }

    @Test
    @DisplayName("Test addMove method")
    void addMove() {
        game.addMove("Test move");
        assertEquals("Test move", game.getLastMoves());
    }

    @Test
    @DisplayName("Test setOk method")
    void setOk() {
        game.setOk(false);
        assertFalse(game.isOk());
        game.setOk(true);
        assertTrue(game.isOk());
    }

    @Test
    @DisplayName("Test getCompatibleAttacks method")
    void getCompatibleAttacks() {
        ArrayList<String[]> compatibleAttacks = game.getCompatibleAttacks(1); // Bulbasaur
        assertNotNull(compatibleAttacks);
        assertTrue(compatibleAttacks.size() > 0);
    }

    @Test
    @DisplayName("Test getAttackId method")
    void getAttackId() {
        String attackInfo = game.getAttackId(1); // Pound
        assertNotNull(attackInfo);
        assertTrue(attackInfo.contains("Pound"));
    }
}