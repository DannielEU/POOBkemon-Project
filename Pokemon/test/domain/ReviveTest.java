package domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;

public class ReviveTest {
    @Test
    public void shouldCreateReviveWithCorrectNumber() {
        Revive revive = new Revive(5);
        String[] info = revive.getItemInfo();

        assertEquals("revive", info[0], "El nombre del ítem debería ser 'revive'");
        assertEquals("5", info[1], "La cantidad del ítem debería ser 5");
    }

    @Test
    public void shouldApplyReviveEffect() throws POOBkemonException {
        String[] pokemonInfo = {"1", "Pikachu", "ELECTRIC", "Static", "70", "35", "55", "30", "50", "40", "90"};
        Pokemon pokemon = new Pokemon(1, pokemonInfo, new ArrayList<>(), false, 1);
        pokemon.currentHealth = 0;
        Revive revive = new Revive(1);

        revive.itemEffect(pokemon);

        assertTrue(pokemon.currentHealth > 0, "El Pokémon debería tener vida después de usar Revive");
    }

    @Test
    public void shouldGetCorrectItemInfo() {
        Revive revive = new Revive(2);
        String[] info = revive.getItemInfo();

        assertEquals(2, info.length, "El array de información debería tener 2 elementos");
        assertEquals("revive", info[0], "El primer elemento debería ser el nombre 'revive'");
        assertEquals("2", info[1], "El segundo elemento debería ser la cantidad '2'");
    }

}
