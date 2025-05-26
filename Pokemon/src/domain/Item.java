package domain;

import java.io.Serializable;

/**
 * Represents an abstract item that can be used on a Pokemon. Each item has a quantity,
 * a name, and a specific effect that gets applied to a Pokemon.
 * This*/
public abstract class Item implements Serializable {
	protected int number;
	protected String name;
	public Item(int number){
		this.number = number;
	}
	public int number(){
		return number;
	}

	/**
	 * Applies the effect of this item to the specified Pokemon if the item has not been fully used.
	 **/
	public void effect(Pokemon pokemon){
		if(!this.isUsed()) {
			this.number = this.usedItem();
			this.itemEffect(pokemon);
		}
	}

	/**
	 * Decreases the quantity of the current item by one and returns the updated quantity*/
	private int usedItem(){
		this.number = this.number - 1;
		return this.number;
	}

	/**
	 * Checks whether the item has been fully used, determined by its quantity.
	 *
	 * @return true if the item's quantity is zero (completely used), false otherwise
	 */
	public boolean isUsed(){
		return this.number == 0;
	}

	/**
	 * Retrieves the name of the item.
	 * If the name is not set (null), it returns "Unknown".
	 *
	 * @return the name of the item as a String or "Unknown" if the name is not set.
	 */
	public String getName() {
		if(this.name == null) return "Unknown";
		return this.name;
	}

	/**
	 * Applies the specific effect of this item to the provided Pokemon.
	 * The exact effect is determined by the*/
	public abstract void itemEffect( Pokemon pokemon);

	/**
	 * Retrieves information about the item. The information is specific to the
	 * implementation of the item and is represented as an array of strings.
	 *
	 * @return an array of strings containing details about the item
	 */
	public abstract String[] getItemInfo();
}
