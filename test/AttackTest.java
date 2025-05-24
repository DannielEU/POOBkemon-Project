

import domain.Attack;
import persistence.MovesRepository;
import domain.POOBkemonException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

public class AttackTest {

    private Attack testAttack;
    private String[] sampleAttackData;

    @BeforeEach
    public void setUp() {
        MovesRepository sampleAttackData = new MovesRepository();
        String[] sampleAttackData1 = sampleAttackData.getAttacksId(52);
        try {
            testAttack = new Attack(1, sampleAttackData1);
        }catch (POOBkemonException e){
            System.out.println(e);
        }
    }

    @Test
    public void testConstructorInitialization() {
        assertEquals(52, testAttack.getIdCSV());
        assertEquals("Ember", testAttack.getInfo()[0]);
        assertEquals("Fire", testAttack.getInfo()[1]);
        assertEquals("40", testAttack.getInfo()[2]);
        assertEquals("100", testAttack.getInfo()[3]);
        assertEquals("25", testAttack.getInfo()[4]);
        assertEquals("The target is attacked with small flames. This may also leave the target with a burn.", testAttack.getInfo()[7]);
    }

    @Test
    public void testGetInfo() {
        String[] info = testAttack.getInfo();
        assertEquals(9, info.length);
        assertEquals("Ember", info[0]);
        assertEquals("Fire", info[1]);
        assertEquals("40", info[2]);
        assertEquals("100", info[3]);
        assertEquals("25", info[4]);
        assertEquals("25", info[5]);
        assertEquals("The target is attacked with small flames. This may also leave the target with a burn.", info[7]);
    }

    @Test
    public void testToString() {
        String expected = "Ember (Type: Fire, Power: 40, Accuracy: 100%, PP: 25/25)";
        assertEquals(expected, testAttack.toString());
    }

    @Test
    public void testConstructorWithInvalidData() {
        String[] invalidData = {"invalid", "name", "desc", "type", "class", "not-number", "100", "25"};

        assertThrows(NumberFormatException.class, () -> {
            new Attack(1,invalidData);
        });
    }

    @Test
    public void testConstructorWithShortArray() {
        String[] shortData = {"52", "Water Gun", "Shoots water"};
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            new Attack(1,shortData);
        });
    }

}
