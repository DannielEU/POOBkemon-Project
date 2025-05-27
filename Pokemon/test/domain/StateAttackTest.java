package domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StateAttackTest {

    @Test
    void shouldCreateStateAttackSuccessfully() throws POOBkemonException {
        String[] infoAttack = {"1", "ELECTRIC", "0", "100", "20", "1", "1", "23", "22"};
        String[] infoState = {"paralyze", "25", "false"};


        StateAttack attack = new StateAttack(1, infoAttack, infoState);

        assertNotNull(attack);
        assertEquals("paralyze", attack.getState());
        assertFalse(attack.affectsSelf());
    }

    @Test
    public void shouldThrowExceptionWithInvalidStateInfo() {
        String[] infoAttack = {"1", "ELECTRIC", "0", "100", "20", "1", "1", "23", "22"};
        String[] infoState = {"paralyze"};

        assertThrows(POOBkemonException.class, () -> {
            new StateAttack(1, infoAttack, infoState);
        });
    }

    @Test
    public void shouldThrowExceptionWithNullStateInfo() {
        String[] infoAttack = {"1", "ELECTRIC", "0", "100", "20", "1", "1", "23", "22"};
        String[] infoState = null;

        assertThrows(POOBkemonException.class, () -> {
            new StateAttack(1, infoAttack, infoState);
        });
    }

    @Test
    void shouldCreateNotSelfTargetingAttack() throws POOBkemonException {
        String[] infoAttack = {"1", "ELECTRIC", "0", "100", "20", "1", "1", "23", "22"};
        String[] infoState = {"paralyze", "50", "true"};

        StateAttack attack = new StateAttack(1, infoAttack, infoState);

        assertFalse(attack.affectsSelf());
    }

    @Test
    void shouldThrowExceptionWithIncompleteStateInfo() {
        String[] infoAttack = {"Thunder Wave", "ELECTRIC", "0", "100", "20", "Special", "paralyze"};
        String[] infoState = {"paralyze", "25"}; // Falta el tercer elemento

        assertThrows(POOBkemonException.class, () -> {
            new StateAttack(1, infoAttack, infoState);
        });
    }

    @Test
    void shouldReturnCorrectInfoArray() throws POOBkemonException {
        String[] infoAttack = {"1", "1", "0", "100", "20", "1", "1", "23", "foe"};
        String[] infoState = {"attack_down", "30", "false"};

        StateAttack attack = new StateAttack(1, infoAttack, infoState);
        String[] info = attack.getInfo();

        assertNotNull(info);
        assertTrue(info.length > infoAttack.length);
        assertEquals("attack_down", info[info.length - 4]);
        assertEquals("30", info[info.length - 3]);
        assertEquals("false", info[info.length - 2]);
        assertEquals("false", info[info.length - 1]);
    }
}