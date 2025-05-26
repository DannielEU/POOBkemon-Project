package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

public class StateTest {
    POOBkemon game;
    private String name;
    private State testState;

    @BeforeEach
    public void setUp() {
        game = POOBkemon.getInstance();
        name = "BURN";
        String[] sampleStateData = {"BURN", "5", "0", "1", "Burn effect information"};
        testState = new State(sampleStateData);
    }

    @Test
    public void testGetName() {
        assertEquals("BURN", testState.getName(),
                "The method should return the name of the state as a string.");
    }

    @Test
    public void shouldThrowExceptionStateConstructor() {
        String[] invalidData = {"INVALID", "5", "0", "1", "Invalid state information", "1"};
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new State(invalidData);
        });
    }

    @Test
    public void testGetTypeReturnsCorrectType() {
        assertEquals(State.StateType.BURN, testState.getType());
    }

    @Test
    public void shouldReturnCorrectDuration() {
        assertEquals(5, testState.getDuration(), "The duration should be 5.");
    }

    @Test
    public void shouldReturnIsNotPrincipal() {
        assertFalse(testState.isPrincipal(), "The state should not be principal.");
    }

    @Test
    public void shouldReturnIsPrincipal() {
        String[] stateDataExpample = {"POISON", "1", "0", "0", "poison effect information"};
        testState = new State(stateDataExpample);
        assertTrue(testState.isPrincipal(), "The state should not be principal.");
    }

    @Test
    public void shouldApplyState() throws POOBkemonException {
        String[] stateDataExpample = {"BURN","5", "0", "1", "Burn effect information"};
        testState = new State(stateDataExpample);
        Pokemon pokemon = game.createPokemon(1, new ArrayList<>(List.of(1, 2, 3, 4)));
        pokemon.addSecundariState(testState);
        testState.applyEffect(pokemon);
        assertTrue(pokemon.hasState(String.valueOf(testState)), "The state should be applied to the Pokemon.");
    }

    @Test
    public void shouldNotApplyState() throws POOBkemonException {
        String[] stateDataExpample = {"BURN","5", "0", "1", "Burn effect information"};
        testState = new State(stateDataExpample);
        Pokemon pokemon = game.createPokemon(1, new ArrayList<>(List.of(1, 2, 3, 4)));
        pokemon.addSecundariState(testState);
        testState.applyEffect(pokemon);
        assertFalse(pokemon.hasState("POISON"), "The state should not be applied to the Pokemon.");
    }

    @Test
    public void shouldNotAffectImmuneType() throws POOBkemonException {
        String[] stateData = {"PARALYSIS", "3", "0", "0", "Paralysis effect"};
        testState = new State(stateData);
        Pokemon pokemon = game.createPokemon(25, new ArrayList<>(List.of(1, 2, 3, 4))); // Asumiendo que 25 es un Pokémon eléctrico

        assertTrue(testState.isImmune(pokemon), "The electric Pokémon should be immune to paralysis.");
    }

    @Test
    public void shouldApplyRechargeEffect() throws POOBkemonException {
            Pokemon pokemon = new Pokemon();
            int originalSpeed = pokemon.speed;
            StringBuilder message = new StringBuilder();
            pokemon.modifyStat("speed", 0.3);

            assertEquals((int)(originalSpeed * 0.3), pokemon.speed);
            assertTrue(message.toString().isEmpty());
    }

    @Test
    public void shouldApplyDisableEffect() throws POOBkemonException {
            Pokemon pokemon = new Pokemon();
            StringBuilder message = new StringBuilder();
            pokemon.disableLastMove();

            assertEquals(0, pokemon.getAttacks().get(pokemon.getAttacks().size()-1).getPPActual());
            assertTrue(message.toString().isEmpty());
    }

    @Test
    public void shouldApplyRageEffect() throws POOBkemonException {
            Pokemon pokemon = new Pokemon();
            int originalAttack = pokemon.attack;
            StringBuilder message = new StringBuilder();
            pokemon.modifyStat("attack", 1.3);

            assertEquals((int)(originalAttack * 1.3), pokemon.attack);
            assertTrue(message.toString().isEmpty());

    }

    @Test
    public void shouldApplyTrappedEffect() throws POOBkemonException {
            Pokemon pokemon = new Pokemon();
            StringBuilder message = new StringBuilder();
            pokemon.setTrapped(true);

            assertFalse(pokemon.isFree());
            assertTrue(message.toString().isEmpty());
    }

    @Test
    public void shouldCheckStateImmunity() throws POOBkemonException {
            // Arrange
            String[] pokemonInfo = {"1", "Pikachu", "ELECTRIC", "Static", "70", "35", "55", "30", "50", "40", "90"};
            Pokemon pokemon = new Pokemon(1, pokemonInfo, new ArrayList<>(), false, 1);
            State state = new State(new String[]{"PARALYSIS", "3", "0", "0", "Paralysis effect"});

            assertTrue(state.isImmune(pokemon));
    }

    @Test
    public void shouldNotBeImmuneToParalysis() throws POOBkemonException {
            String[] pokemonInfo = {"1", "Charmander", "FIRE", "Blaze", "70", "35", "55", "30", "50", "40", "90"};
            Pokemon pokemon = new Pokemon(1, pokemonInfo, new ArrayList<>(), false, 1);
            State state = new State(new String[]{"PARALYSIS", "3", "0", "0", "Paralysis effect"});

            assertFalse(state.isImmune(pokemon));
    }

//    @Test
//    public void shouldCalculateBaseDamageCorrectly() throws POOBkemonException {
//        String[] pokemonInfo = {"1", "Charizard", "Fire", "100", "100", "100", "100", "100", "100", "100", "100"};
//
//        ArrayList<Integer> attacks = new ArrayList<>();
//        attacks.add(1);
//        Pokemon attacker = new Pokemon(1, pokemonInfo, attacks, false, 50);
//        Pokemon defender = new Pokemon(2, pokemonInfo, attacks, false, 50);
//        Attack testAttack = attacker.getAttack(1);
//        String damageResult = defender.getDamage(testAttack, attacker);
//
//
//        assertTrue(damageResult.contains("causó"));
//        assertTrue(damageResult.contains("puntos de daño"));
//
//        int damageDealt = Integer.parseInt(damageResult.replaceAll(".*causó (\\d+) puntos.*", "$1"));
//
//        assertTrue(damageDealt > 0);
//        assertTrue(damageDealt < defender.maxHealth);
//    }

    @Test
    public void shouldPoisonTypePokemonBeImmunetoBadPoison() throws POOBkemonException {
        // Arrange
        Pokemon pokemon = new Pokemon();
        pokemon.type = "POISON"; // Configura el tipo del Pokémon como Veneno
        State state = new State(new String[]{"BAD_POISON", "2", "0.1", "false", "0"}); // Estado de envenenamiento grave
        boolean isImmune = state.isImmune(pokemon);

        assertTrue(isImmune);
    }


    @Test
    public void shouldApplyMagicCoatEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        State state = new State(new String[]{"MAGIC_COAT", "3", "0", "0", "Magic Coat effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.isProtected(), "El Pokémon debería estar protegido");
    }

    @Test
    public void shouldApplySnatchEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalSpeed = pokemon.speed;
        int originalAttack = pokemon.attack;
        State state = new State(new String[]{"SNATCH", "3", "0", "0", "Snatch effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.speed > originalSpeed && pokemon.attack > originalAttack,
                "La velocidad y el ataque deberían aumentar");
    }

    @Test
    public void shouldApplyGrudgeEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalAttack = pokemon.attack;
        State state = new State(new String[]{"GRUDGE", "3", "0", "0", "Grudge effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.attack < originalAttack, "El ataque debería reducirse");
    }

    @Test
    public void shouldApplyImprisonEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        State state = new State(new String[]{"IMPRISON", "3", "0", "0", "Imprison effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.isProtected(), "El Pokémon debería estar protegido");
    }

    @Test
    public void shouldTestStateImmunity() throws POOBkemonException {
        Pokemon electricPokemon = new Pokemon(1, new String[]{"1", "Pikachu", "ELECTRIC", "", "", "100", "100", "100", "100", "100", "100"},
                new ArrayList<>(), false, 1);
        State paralysisState = new State(new String[]{"PARALYSIS", "3", "0", "0", "Paralysis effect"});

        assertTrue(paralysisState.isActive());
        assertTrue(paralysisState.isImmune(electricPokemon), "Un Pokémon eléctrico debería ser inmune a la parálisis");

    }
    

}
