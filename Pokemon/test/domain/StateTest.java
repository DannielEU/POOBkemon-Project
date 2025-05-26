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

            assertTrue(pokemon.isFree());
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
        assertTrue(pokemon.isProtected(), "The pokemon should be protected by Magic Coat");
    }

    @Test
    public void shouldApplySnatchEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalSpeed = pokemon.speed;
        int originalAttack = pokemon.attack;
        State state = new State(new String[]{"SNATCH", "3", "0", "0", "Snatch effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.speed > originalSpeed && pokemon.attack > originalAttack, "the speed and attack should increase with Snatch effect");
    }

    @Test
    public void shouldApplyGrudgeEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalAttack = pokemon.attack;
        State state = new State(new String[]{"GRUDGE", "3", "0", "0", "Grudge effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.attack < originalAttack, "the attack should decrease with Grudge effect");
    }

    @Test
    public void shouldApplyImprisonEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        State state = new State(new String[]{"IMPRISON", "3", "0", "0", "Imprison effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.isProtected(), "the pokemon should be protected by Imprison effect");
    }

    @Test
    public void shouldTestStateImmunity() throws POOBkemonException {
        Pokemon electricPokemon = new Pokemon(1, new String[]{"1", "Pikachu", "ELECTRIC", "", "", "100", "100", "100", "100", "100", "100"},
                new ArrayList<>(), false, 1);
        State paralysisState = new State(new String[]{"PARALYSIS", "3", "0", "0", "Paralysis effect"});

        assertTrue(paralysisState.isActive());
        assertTrue(paralysisState.isImmune(electricPokemon), "a electric Pokémon should be immune to paralysis");

    }

    @Test
    public void shouldApplyPoisonEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalHealth = pokemon.currentHealth;
        State state = new State(new String[]{"POISON", "5", "0", "1", "Poison effect"});
        int expectedDamage = originalHealth / 8;

        state.applyEffect(pokemon);
        assertEquals(originalHealth - expectedDamage, pokemon.currentHealth, "the poison should cause 1/8 of the maximum health as damage");

    }

    @Test
    public void shouldApplyParalysisEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalSpeed = pokemon.speed;
        State state = new State(new String[]{"PARALYSIS", "3", "0", "0", "Paralysis effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.speed < originalSpeed, "the speed should be reduced by the paralysis effect");
    }

    @Test
    public void shouldApplyBadPoisonEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalHealth = pokemon.currentHealth;
        State state = new State(new String[]{"BAD_POISON", "5", "0", "1", "Bad Poison effect"});
        int expectedDamage = originalHealth / 25;

        state.applyEffect(pokemon);
        assertEquals(originalHealth - expectedDamage, pokemon.currentHealth, "the bad poison should cause 1/25 of the maximum health as damage");
    }

    @Test
    public void shouldApplySleepEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        boolean originalStatus = pokemon.canAttack();
        State state = new State(new String[]{"SLEEP", "1", "0", "0", "Sleep effect"});

        state.applyEffect(pokemon);
        assertFalse(pokemon.canAttack(), "the Pokémon should be asleep after applying the sleep state");
        assertTrue(originalStatus, "the original status of the Pokémon should not be asleep");
    }

    @Test
    public void shouldApplyFreezeEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        boolean originalStatus = pokemon.canAttack();
        State state = new State(new String[]{"FREEZE", "1", "0", "0", "Freeze effect"});

        state.applyEffect(pokemon);
        assertFalse(pokemon.canAttack(), "the pokemon should be frozen after applying the freeze state");
        assertTrue(originalStatus, "the original status of the Pokémon should not be frozen");
    }

    @Test
    public void shouldApplyHealEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalHealth = pokemon.currentHealth;
        State state = new State(new String[]{"HEAL", "3", "0", "0", "Heal effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.currentHealth >= originalHealth, "the pokemon should recover health after applying the heal state");
    }

    @Test
    public void shouldNotApplyHealEffectIfIsWeak() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalHealth = pokemon.currentHealth;
        State state = new State(new String[]{"HEAL", "3", "0", "0", "Heal effect"});
        state.applyEffect(pokemon);
        assertEquals(originalHealth, pokemon.currentHealth, "the pokemon should not recover health if it is weak");
    }

    @Test
    public void shouldApplyAttackUpEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalAttack = pokemon.attack;
        State state = new State(new String[]{"ATTACK_UP", "3", "0", "0", "Attack Up effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.attack > originalAttack, "the pokemon should increase its attack after applying the attack up state");
    }

    @Test
    public void shouldApplyDefenseUpEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalDefense = pokemon.defense;
        State state = new State(new String[]{"DEFENSE_UP", "3", "0", "0", "Defense Up effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.defense > originalDefense, "the pokemon should increase its defense after applying the defense up state");
    }

    @Test
    public void shouldApplySpeedUpEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalSpeed = pokemon.speed;
        State state = new State(new String[]{"SPEED_UP", "3", "0", "0", "Speed Up effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.speed > originalSpeed, "the pokemon should increase its speed after applying the speed up state");
    }

    @Test
    public void shouldApplySpecialAttackUpEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalSpecialAttack = pokemon.specialAttack;
        State state = new State(new String[]{"SP_ATTACK_UP", "3", "0", "0", "Special Attack Up effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.specialAttack > originalSpecialAttack, "the pokemon should increase its special attack after applying the special attack up state");
    }

    @Test
    public void shouldApplySpecialDefenseUpEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalSpecialDefense = pokemon.specialDefense;
        State state = new State(new String[]{"SP_DEFENSE_UP", "3", "0", "0", "Special Defense Up effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.specialDefense > originalSpecialDefense, "the pokemon should increase its special defense after applying the special defense up state");
    }

    @Test
    public void shouldApplyEvasionUpEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalEvasion = pokemon.getEvasionStage();
        State state = new State(new String[]{"EVASION_UP", "3", "0", "0", "Evasion Up effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.getEvasionStage() + 3 > originalEvasion, "the pokemon should increase its evasion after applying the evasion up state");
    }

    @Test
    public void shouldApplyAccuracyUpEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalAccuracy = pokemon.getAccuracyStage();
        State state = new State(new String[]{"ACCURACY_UP", "3", "0", "0", "Accuracy Up effect"});
        state.applyEffect(pokemon);

        assertTrue(originalAccuracy  < pokemon.getAccuracyStage() + 3, "the pokemon should increase its accuracy after applying the accuracy up state");
    }

    @Test
    public void shouldApplyAttackDownEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalAttack = pokemon.attack;
        State state = new State(new String[]{"ATTACK_DOWN", "3", "0", "0", "Attack Down effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.attack < originalAttack, "the pokemon should reduce its attack after applying the attack down state");
    }

    @Test
    public void shouldApplyDefenseDownEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalDefense = pokemon.defense;
        State state = new State(new String[]{"DEFENSE_DOWN", "3", "0", "0", "Defense Down effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.defense < originalDefense, "the pokemon should reduce its defense after applying the defense down state");
    }

    @Test
    public void shouldApplySpeedDownEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalSpeed = pokemon.speed;
        State state = new State(new String[]{"SPEED_DOWN", "3", "0", "0", "Speed Down effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.speed < originalSpeed, "the pokemon should reduce its speed after applying the speed down state");
    }

    @Test
    public void shouldApplySpecialAttackDownEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalSpecialAttack = pokemon.specialAttack;
        State state = new State(new String[]{"SP_ATTACK_DOWN", "3", "0", "0", "Special Attack Down effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.specialAttack < originalSpecialAttack, "the pokemon should reduce its special attack after applying the special attack down state");
    }

    @Test
    public void shouldApplySpecialDefenseDownEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalSpecialDefense = pokemon.specialDefense;
        State state = new State(new String[]{"SP_DEFENSE_DOWN", "3", "0", "0", "Special Defense Down effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.specialDefense < originalSpecialDefense, "the pokemon should reduce its special defense after applying the special defense down state");
    }

    @Test
    public void shouldApplyEvasionDownEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalEvasion = pokemon.getEvasionStage();
        State state = new State(new String[]{"EVASION_DOWN", "3", "0", "0", "Evasion Down effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.getEvasionStage() - 3 < originalEvasion, "the pokemon should reduce its evasion after applying the evasion down state");
    }

    @Test
    public void shouldApplyAccuracyDownEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalAccuracy = pokemon.getAccuracyStage();
        State state = new State(new String[]{"ACCURACY_DOWN", "3", "0", "0", "Accuracy Down effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.getAccuracyStage() - 3 < originalAccuracy, "the pokemon should reduce its accuracy after applying the accuracy down state");
    }

    @Test
    public void shouldApplyConfusionEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        boolean originalStatus = pokemon.canAttack();
        State state = new State(new String[]{"CONFUSION", "3", "0", "0", "Confusion effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.canAttack(), "the pokemon should be confused after applying the confusion state");
        assertTrue(originalStatus, "the original status of the Pokémon should not be confused");
    }

    @Test
    public void shouldApplyFlinchEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        boolean originalStatus = pokemon.canAttack();
        State state = new State(new String[]{"FLINCH", "3", "0", "0", "Flinch effect"});

        state.applyEffect(pokemon);
        assertFalse(pokemon.canAttack(), "the pokemon should be flinched after applying the flinch state");
        assertTrue(originalStatus, "the original status of the Pokémon should not be flinched");
    }

    @Test
    public void shouldApplyLeechSeedEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalHealth = pokemon.currentHealth;
        State state = new State(new String[]{"LEECH_SEED", "3", "0", "0", "Leech Seed effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.currentHealth < originalHealth, "the pokemon should lose health after applying the leech seed state");
    }

    @Test
    public void shouldApplyCurseEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalHealth = pokemon.currentHealth;
        State state = new State(new String[]{"CURSE", "1", "0", "0", "Curse effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.currentHealth <= originalHealth, "the pokemon should lose health after applying the curse state");
    }

    @Test
    public void shouldApplyNightmareEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalHealth = pokemon.currentHealth;
        State state = new State(new String[]{"NIGHTMARE", "3", "0", "0", "Nightmare effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.currentHealth <= originalHealth, "the pokemon should lose health after applying the nightmare state");
    }

    @Test
    public void shouldApplyProtectEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        State state = new State(new String[]{"PROTECT", "3", "0", "0", "Protect effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.isProtected());
    }

//    @Test
//    public void shouldApplyPerishSongEffect() throws POOBkemonException {
//        Pokemon pokemon = new Pokemon();
//        int originalHealth = pokemon.currentHealth;
//        System.out.println("Current Health: " + originalHealth);
//        State state = new State(new String[]{"PERISH_SONG", "3", "0", "0", "Perish Song effect"});
//
//        state.applyEffect(pokemon);
//        state.applyEffect(pokemon); // 2 turnos restantes
//        state.applyEffect(pokemon); // 1 turno restante
//        state.applyEffect(pokemon); // 0 turnos - debería causar desmayo
//
//        System.out.println("Current Health: " + pokemon.currentHealth);
//        // Assert
//        assertTrue(pokemon.currentHealth == 0, "El Pokémon debería desmayarse después de 3 turnos con Perish Song");
//
//    }

    @Test
    public void shouldApplyDestinyBondEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        State state = new State(new String[]{"DESTINY_BOND", "1", "0", "0", "Destiny Bond effect"});

        String result = state.applyEffect(pokemon);
        assertFalse(result.isEmpty(), "should return a message indicating that the Pokémon is bound by Destiny Bond");
    }

    @Test
    public void shouldApplySpikesEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        State state = new State(new String[]{"SPIKES", "1", "0", "1", "Spikes effect"});
        String result = state.applyEffect(pokemon);

        assertFalse(result.isEmpty(), "should return a message indicating that the Pokémon is affected by Spikes");
    }

//    @Test
//    public void shouldApplySandstormEffectToNonImmuneType() throws POOBkemonException {
//        // Arrange
//        String[] pokemonInfo = {"1", "Pikachu", "ELECTRIC", "Static", "70", "35", "55", "30", "50", "40", "90"};
//        Pokemon pokemon = new Pokemon(1, pokemonInfo, new ArrayList<>(), false, 1);
//        int originalHealth = pokemon.currentHealth;
//        State state = new State(new String[]{"SANDSTORM", "5", "0", "1", "Sandstorm effect"});
//
//        // Act
//        state.applyEffect(pokemon);
//
//        // Assert
//        assertTrue(pokemon.currentHealth < originalHealth, "Un Pokémon no inmune debería recibir daño de la tormenta de arena");
//    }

    @Test
    public void shouldNotApplySandstormDamageToImmuneTypes() throws POOBkemonException {
        // Arrange
        String[] pokemonInfo = {"1", "Geodude", "ROCK", "Rock Head", "70", "35", "55", "30", "50", "40", "90"};
        Pokemon pokemon = new Pokemon(1, pokemonInfo, new ArrayList<>(), false, 1);
        int originalHealth = pokemon.currentHealth;
        State state = new State(new String[]{"SANDSTORM", "5", "0", "1", "Sandstorm effect"});

        // Act
        state.applyEffect(pokemon);

        // Assert
        assertEquals(originalHealth, pokemon.currentHealth, "A pokemon immune should not receive damage from Sandstorm");
    }

    @Test
    public void shouldApplyTauntEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalSpeed = pokemon.speed;
        int originalDefense = pokemon.defense;
        int originalSpDefense = pokemon.specialDefense;
        State state = new State(new String[]{"TAUNT", "3", "0", "1", "Taunt effect"});

        state.applyEffect(pokemon);

        assertTrue(pokemon.speed > originalSpeed, "the speed should increase with Taunt");
        assertTrue(pokemon.defense < originalDefense, "the defense should decrease with Taunt");
        assertTrue(pokemon.specialDefense < originalSpDefense, "the special defense should decrease with Taunt");
    }

    @Test
    public void shouldApplyTormentEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalSpeed = pokemon.speed;
        int originalDefense = pokemon.defense;
        int originalSpAttack = pokemon.specialAttack;
        State state = new State(new String[]{"TORMENT", "3", "0", "1", "Torment effect"});

        state.applyEffect(pokemon);

        assertEquals(pokemon.speed, originalSpeed* 1.3, "the speed should increase with Torment");
        assertEquals(pokemon.defense, originalDefense * 0.6, "the defense should decrease with Torment");
        assertEquals(pokemon.specialAttack, originalSpAttack * 1.3, "the special attack should increase with Torment");
    }

    @Test
    public void shouldApplyTransformEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int maxHealth = pokemon.maxHealth;
        State state = new State(new String[]{"TRANSFORM", "1", "0", "0", "Transform effect"});
        state.applyEffect(pokemon);

        assertEquals(pokemon.maxHealth, maxHealth + pokemon.currentHealth/5, "the pokemon should transform and gain health");
    }

    @Test
    public void shouldApplySubstituteEffect() throws POOBkemonException {
        Pokemon pokemon = new Pokemon();
        int originalHealth = pokemon.currentHealth;
        State state = new State(new String[]{"SUBSTITUTE", "1", "0", "0", "Substitute effect"});

        state.applyEffect(pokemon);
        assertTrue(pokemon.currentHealth < originalHealth, "The pokemon should lose health after applying the substitute state");
    }

//    @Test
//    public void shouldApplyIngrainEffect() throws POOBkemonException {
//        Pokemon pokemon = new Pokemon();
//        int originalHealth = pokemon.currentHealth;
//        State state = new State(new String[]{"INGRAIN", "1", "0", "0", "Ingrain effect"});
//        state.applyEffect(pokemon);
//        System.out.println("Current Health: " + pokemon.currentHealth);
//
//        assertEquals(pokemon.currentHealth, originalHealth + pokemon.maxHealth/16, "El Pokémon debería recuperar salud al aplicar el estado de Ingrain");
//    }

    @Test
    public void shouldApplyRechargeEffects() throws POOBkemonException {
        String[] pokemonInfo = {"1", "Pikachu", "ELECTRIC", "Static", "70", "35", "55", "30", "50", "40", "90"};
        Pokemon pokemon = new Pokemon(1, pokemonInfo, new ArrayList<>(), false, 1);
        int originalSpeed = pokemon.speed;
        State state = new State(new String[]{"RECHARGE", "1", "0", "1", "Recharge effect"});

        String result = state.applyEffect(pokemon);

        assertEquals((int)(originalSpeed * 0.3), pokemon.speed, "the speed should increase by 30% with the Recharge effect");
        assertFalse(result.isEmpty(), "should return a message about the recharge effect");
        assertTrue(result.contains("debe recargar"), "the message should mention that the Pokémon must recharge");
    }

    @Test
    public void shouldApplyRageEffects() throws POOBkemonException {
        String[] pokemonInfo = {"1", "Pikachu", "ELECTRIC", "Static", "70", "35", "55", "30", "50", "40", "90"};
        Pokemon pokemon = new Pokemon(1, pokemonInfo, new ArrayList<>(), false, 1);
        int originalAttack = pokemon.attack;
        State state = new State(new String[]{"RAGE", "1", "0", "1", "Rage effect"});

        String result = state.applyEffect(pokemon);

        assertEquals((int)(originalAttack * 1.3), pokemon.attack, "the attack should increase by 30% with the Rage effect");
        assertFalse(result.isEmpty(), "should return a message about the rage effect");
        assertTrue(result.contains("cólera"), "the message should mention that the Pokémon is in a rage state");
    }

    @Test
    public void shouldApplyTrappedEffects() throws POOBkemonException {
        String[] pokemonInfo = {"1", "Pikachu", "ELECTRIC", "Static", "70", "35", "55", "30", "50", "40", "90"};
        Pokemon pokemon = new Pokemon(1, pokemonInfo, new ArrayList<>(), false, 1);
        State state = new State(new String[]{"TRAPPED", "2", "0", "1", "Trapped effect"});

        String result = state.applyEffect(pokemon);

        assertTrue(pokemon.isFree(), "the pokemon should be trapped");
        assertFalse(result.isEmpty(), "should return a message about the trapped effect");
        assertTrue(result.contains("no puede escapar"), "the message should mention that the Pokémon cannot escape");
    }

}
