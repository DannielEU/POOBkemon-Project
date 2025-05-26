package domain;

/**
 * The Revive class represents a specific type of item that can be used to apply
 * an effect on a Pokemon. This item is designed to "revive" a Pokemon by invoking
 * specific effects defined in the Pokemon class.
 *
 * This class extends the abstract class Item and provides concrete implementations
 * for the itemEffect and getItemInfo methods.
 */
public class Revive extends Item{
    /**
     * Constructs a Revive object*/
    public Revive(int number){
        super(number);
        this.name = "revive";
    }

    /**
     * Applies the effect of the Revive item on the specified Pokemon.
     *
     * The effect is represented by an array of strings, where:
     * - The first element contains the name*/
    @Override
    public void itemEffect(Pokemon pokemon) {
        String[] info = new String[4];
        info[0] = "Potion"; // nombre del item
        info[1] = "Revive"; // indicacion de efecto
        //aplico el efecto de la poti
        pokemon.effect(info);
    }

    /**
     * Retrieves information about the item, including its name and quantity.
     *
     * @return A String array where:
     *         - The first element is the name of the item.
     *         - The second element is the number of the item available.
     */
    public String[] getItemInfo(){
        String[] info = new String[2];
        info[0] = ""+this.name; // nombre del item
        info[1] = ""+this.number;
        return  info;
    }
}
