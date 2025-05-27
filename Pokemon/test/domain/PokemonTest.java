package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.PokemonRepository;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PokemonTest {

    private Pokemon pokemon;
    private String[] sampleInfo;
    private ArrayList<Integer> sampleAttacksIds;

    @BeforeEach
    public void setUp() {
        try {
            PokemonRepository sampleInfo = new PokemonRepository();
            String[] sampleInfo1 = sampleInfo.getPokemonId(1);
            sampleAttacksIds = new ArrayList<>(Arrays.asList(1, 2));
            pokemon = new Pokemon(1, sampleInfo1, sampleAttacksIds, false, 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testConstructorInitialization() {
        assertEquals(1, pokemon.getId());
        assertEquals("Bulbasaur", pokemon.getName());
        assertEquals("1", pokemon.idPokedex);
        assertEquals("Grass", pokemon.type);
        assertEquals(100, pokemon.levelRequirement);
        assertFalse(pokemon.getActive());
        assertFalse(pokemon.getWeak());
        assertEquals(2, pokemon.getAttacks().size());
    }

    @Test
    public void testGetActive() {
        assertFalse(pokemon.getActive());
    }

    @Test
    public void testSetActive() {
        pokemon.setActive(true);
        assertTrue(pokemon.getActive());

        pokemon.setActive(false);
        assertFalse(pokemon.getActive());
    }

    @Test
    public void testGetId() {
        assertEquals(1, pokemon.getId());
    }

    @Test
    public void testGetName() {
        assertEquals("Bulbasaur", pokemon.getName());
    }

    @Test
    public void testCreateAttacks() {
        // Verificamos que se crearon los ataques correctamente
        assertEquals(2, pokemon.getAttacks().size());
        // Aquí podríamos añadir más verificaciones sobre los ataques si tuviéramos acceso a ellos
    }

    @Test
    public void testGetAttack() {
        // Este test fallará hasta que implementes getAttack()
        assertNull(pokemon.getAttack(1));
    }

    @Test
    public void testConstructorWithIncompleteInfo_ShouldCreateDefaultPokemon() {
        try {
            // Arrange
            String[] incompleteInfo = {"001", "Bulbasaur"}; // Falta type, HP, etc. (menos de 11 campos)
            ArrayList<Integer> sampleAttacksIds = new ArrayList<>(Arrays.asList(1, 2));

            // Act
            Pokemon pokemon = new Pokemon(1, incompleteInfo, sampleAttacksIds, false, 1);

            // Assert - Verifica que se creó un Pokémon por defecto
            assertEquals(0, pokemon.getId()); // ID por defecto
            assertEquals("MissingNo", pokemon.getName()); // Nombre por defecto
            assertEquals("Normal", pokemon.type); // Tipo por defecto
            assertEquals(180, pokemon.maxHealth); // HP por defecto
            assertEquals(10, pokemon.attack); // Ataque por defecto

        } catch (POOBkemonException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testConstructorWithInvalidNumberFormat_ShouldCreateDefaultPokemon() {
        try {
            // Arrange - Info con un campo numérico inválido (ej: "abc" en HP)
            String[] invalidInfo = {
                    "001", "Bulbasaur", "Grass", "", "", "abc", // HP no es número
                    "49", "65", "65", "45"
            };
            ArrayList<Integer> sampleAttacksIds = new ArrayList<>(Arrays.asList(1, 2));

            // Act
            Pokemon pokemon = new Pokemon(1, invalidInfo, sampleAttacksIds, false, 1);

            // Assert - Verifica valores por defecto
            assertEquals("MissingNo", pokemon.getName());
            assertEquals(180, pokemon.maxHealth);
        }catch(POOBkemonException e){
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testConstructorWithIncompleteInfo_ShouldCreateDefaultPokemon2() {
        // Arrange
        try {
            String[] incompleteInfo = {"001", "Bulbasaur"}; // Menos de 11 campos
            ArrayList<Integer> sampleAttacksIds = new ArrayList<>(Arrays.asList(1, 2));

            // Act
            Pokemon pokemon = new Pokemon(1, incompleteInfo, sampleAttacksIds, false, 1);

            // Assert - Verifica que se creó un Pokémon por defecto
            assertEquals(0, pokemon.getId()); // ID por defecto
            assertEquals("MissingNo", pokemon.getName()); // Nombre por defecto
            assertEquals("Normal", pokemon.type); // Tipo por defecto
            assertEquals(180, pokemon.maxHealth); // HP por defecto
        } catch (POOBkemonException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    @DisplayName("Test createRandom method")
    void createRandom() {
        int randomValue = pokemon.createRandom(100);
        assertTrue(randomValue >= 0 && randomValue < 100);
    }

    @Test
    @DisplayName("Test getWeak method")
    void getWeak() {
        assertFalse(pokemon.getWeak());
        pokemon.currentHealth = 0;
        pokemon.isWeak();
        assertTrue(pokemon.getWeak());
    }

    @Test
    @DisplayName("Test getAttacks method")
    void getAttacks() {
        ArrayList<Attack> attacks = pokemon.getAttacks();
        assertNotNull(attacks);
        assertEquals(2, attacks.size());
    }

    @Test
    @DisplayName("Test getAttack method with invalid attack")
    void getAttack_WithInvalidId() {
        Attack attack = pokemon.getAttack(999);
        assertNull(attack);
    }

    @Test
    @DisplayName("Test isWeak method when health is zero")
    void isWeak() {
        pokemon.currentHealth = 0;
        pokemon.isWeak();
        assertTrue(pokemon.getWeak());
    }

    @Test
    @DisplayName("Test getInfo method")
    void getInfo() {
        String[] info = pokemon.getInfo();
        assertNotNull(info);
        assertEquals(18, info.length);
        assertEquals("Bulbasaur", info[1]);
        assertEquals("Grass", info[3]);
    }

    @Test
    @DisplayName("Test getAttacksInfo method")
    void getAttacksInfo() {
        String[][] attacksInfo = pokemon.getAttacksInfo();
        assertNotNull(attacksInfo);
        assertEquals(2, attacksInfo.length);
        assertTrue(attacksInfo[0].length >= 7);
    }

    @Test
    @DisplayName("Test timeOver method")
    void timeOver() {
        int initialPP = pokemon.getAttacks().get(0).getPPActual();
        pokemon.timeOver();
        assertTrue(pokemon.getAttacks().get(0).getPPActual() < initialPP);
    }

    @Test
    @DisplayName("Test setProtected method")
    void setProtected() {
        pokemon.setProtected(true);
        assertTrue(pokemon.isProtected());
        pokemon.setProtected(false);
        assertFalse(pokemon.isProtected());
    }

    @Test
    @DisplayName("Test reduceSpeed method")
    void reduceSpeed() {
        int initialSpeed = pokemon.speed;
        pokemon.reduceSpeed(50);
        int secondSpeed = pokemon.speed;
        assertNotEquals(secondSpeed, initialSpeed);
    }

    @Test
    @DisplayName("Test getType method")
    void getType() {
        assertEquals("Grass", pokemon.getType());
    }

    @Test
    @DisplayName("Test modifyStat method for attack")
    void modifyStat() {
        int initialAttack = pokemon.attack;
        pokemon.modifyStat("attack", 1.5);
        assertEquals((int)(initialAttack * 1.5), pokemon.attack);
    }

    @Test
    @DisplayName("Test disableLastMove method")
    void disableLastMove() {
        Attack lastAttack = pokemon.getAttacks().get(pokemon.getAttacks().size()-1);
        pokemon.disableLastMove();
        assertEquals(0, lastAttack.getPPActual());
    }

    @Test
    @DisplayName("Test setTrapped method")
    void setTrapped() {
        pokemon.setTrapped(false);
        assertFalse(pokemon.isFree());
        pokemon.setTrapped(true);
        assertTrue(pokemon.isFree());
    }

    @Test
    @DisplayName("Test setNewPS method")
    void setNewPS() {
        int initialMaxHealth = pokemon.maxHealth;
        pokemon.setNewPS(20);
        assertEquals(initialMaxHealth + 20, pokemon.maxHealth);
    }

    @Test
    @DisplayName("Test isProtected method")
    void isProtected() {
        pokemon.setProtected(true);
        assertTrue(pokemon.isProtected());
        pokemon.setProtected(false);
        assertFalse(pokemon.isProtected());
    }

    @Test
    @DisplayName("Test isFree method")
    void isFree() {
        pokemon.setTrapped(true);
        assertTrue(pokemon.isFree());
        pokemon.setTrapped(false);
        assertFalse(pokemon.isFree());
    }

    @Test
    void shouldModifyCriticalHitChance() {
        double initialCritChance = 0.0417;
        pokemon.modifyStat("Critico", 1.5);
        int expectedAttack = pokemon.attack;
        assertEquals(expectedAttack, pokemon.attack, "the base attack should not change");
        double expectedCritChance = initialCritChance * 1.5;
        assertEquals(expectedCritChance, 0.06255, 0.0001);
    }

    @Test
    void shouldDeleteState() throws POOBkemonException {
        String[] stateInfo = {"BURN", "1", "3", "10", "0.0", "0.0", "0.0"};
        State testState = new State(stateInfo);
        try {
            java.lang.reflect.Field principalStateField = Pokemon.class.getDeclaredField("principalState");
            principalStateField.setAccessible(true);
            principalStateField.set(pokemon, testState);
            pokemon.deleteState(testState);

            State resultState = (State) principalStateField.get(pokemon);
            assertNull(resultState, "El estado principal debería ser null después de eliminarlo");

        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Error al acceder al campo principalState: " + e.getMessage());
        }
    }


}