package domain;

/**
 * The Potion class represents an item that restores health points to a Pokémon.
 * It extends the abstract Item class and provides functionality specific to
 * healing items in the game.
 */
public class Potion extends Item {
	private int healthPoints;

	/**
	 * Constructs a new Potion object with the specified number of uses and health points.
	 * The potion is assigned a name based on the amount of health it restores.
	 *
	 * @param number The number of potions of this type available.
	 * @param health The amount of health points this potion restores.
	 */
	public Potion(int number, int health) {
		super(number);
		this.healthPoints = health;
		this.createName();
	}

	/**
	 * Applies the effect of the item to the specified Pokemon. This method
	 * provides the necessary information about the item and its effect, then
	 * delegates the effect application to the Pokemon object.
	 *
	 * @param pokemon The Pokemon object to which the effect of the item is applied.
	 */
	@Override
	public void itemEffect(Pokemon pokemon) {
		String[] info = new String[4];
		info[0] = "Potion"; // nombre del item
		info[1] = "Heals"; // indicacion de efecto
		info[2] = Integer.toString(this.healthPoints); //puntos que da
		info[3] = "1"; //turnos del efecto
		pokemon.effect(info);
	}

	/**
	 * Assigns a name to the Potion object based on its health points value.
	 * The name is determined as follows:
	 * - If the health points are between 1 and 25 (inclusive), the name is set to "potion".
	 * - If the health points are greater than 25 and up to 50 (inclusive), the name is set to "superPotion".
	 * - If the health points are greater than 50 and up to 100 (inclusive), the name is set to "hyperPotion".
	 * - If the health points are greater than 100, the name is set to "Mega".
	 *
	 * This method is intended to be used internally within the class to automatically
	 * assign a meaningful name to the Potion based on its healing capability.
	 */
	private void createName() {
		if(this.healthPoints >=1 &&this.healthPoints <= 25) this.name = "potion";
		if(this.healthPoints > 25 && this.healthPoints <= 50) this.name = "superPotion";
		if(this.healthPoints > 50 && this.healthPoints <= 100) this.name = "hyperPotion";
		if(this.healthPoints > 100) this.name = "Mega";
	}

	/**
	 * Retrieves the name of the potion based on its health restoration capability.
	 *
	 * @return The name of the potion as a string, representing its type
	 * (e.g., "potion", "superPotion", "hyperPotion", "Mega").
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Retrieves detailed information about the item, including its name and the number
	 * of items available.
	 *
	 * @return A string array where the first element is the name of the item
	 * and the second element is the quantity of the item.
	 */
	public String[] getItemInfo(){
		String[] info = new String[2];
		info[0] = ""+this.name; // nombre del item
		info[1] = ""+this.number;
		return  info;
	}
}
