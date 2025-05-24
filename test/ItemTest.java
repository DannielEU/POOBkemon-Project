import domain.Item;
import domain.Potion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {

    @Test
    public void testNumberMethodReturnsCorrectValue() {
        // Arrange
        int expectedNumber = 789;
        Potion item = new Potion(expectedNumber,2);

        int actualNumber = item.number();

        assertEquals(expectedNumber, actualNumber);
    }

}
