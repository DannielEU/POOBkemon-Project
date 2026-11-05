package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DecisionTest {

    @Test
    void factoriesBuildTheFormatUnderstoodByTheGame() {
        assertArrayEquals(new String[]{"Attack", "3", "7", "1"}, Decision.attack(3, 7, 1));
        assertArrayEquals(new String[]{"UseItem", "1", "7", "potion"}, Decision.useItem(1, 7, "potion"));
        assertArrayEquals(new String[]{"ChangePokemon", "1", "8"}, Decision.changePokemon(1, 8));
        assertArrayEquals(new String[]{"Run", "0"}, Decision.run(0));
        assertArrayEquals(new String[]{"timeOver", "0", "2"}, Decision.timeOver(0, 2));
    }

    @Test
    void validDecisionsPass() {
        assertDoesNotThrow(() -> Decision.validate(Decision.attack(1, 2, 3)));
        assertDoesNotThrow(() -> Decision.validate(Decision.useItem(1, 2, "potion")));
        assertDoesNotThrow(() -> Decision.validate(Decision.run(1)));
    }

    @Test
    void nullEmptyUnknownAndIncompleteDecisionsAreRejected() {
        assertThrows(POOBkemonException.class, () -> Decision.validate(null));
        assertThrows(POOBkemonException.class, () -> Decision.validate(new String[0]));
        assertThrows(POOBkemonException.class, () -> Decision.validate(new String[]{"Dance"}));
        assertThrows(POOBkemonException.class, () -> Decision.validate(new String[]{"Attack", "1", "2"}));
        assertThrows(POOBkemonException.class, () -> Decision.validate(new String[]{"UseItem", "1", "2"}));
        assertThrows(POOBkemonException.class, () -> Decision.validate(new String[]{"ChangePokemon", "1"}));
    }
}
