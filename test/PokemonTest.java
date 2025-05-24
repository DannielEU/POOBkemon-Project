
import domain.POOBkemonException;
import domain.Pokemon;
import persistence.PokemonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Arrays;

public class PokemonTest {

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
}

