package domain;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a backpack that holds a collection of items.
 * This class allows access and manipulation of items stored within the backpack.
 */
public class BagPack implements Serializable {
	private ArrayList<Item> items;

	/**
	 * Constructs a BagPack object with a provided list of items.
	 *
	 * @param items the list of items to be stored in the backpack
	 */
	public BagPack(ArrayList<Item> items){
		this.items = new ArrayList<Item>(items);
	}

	/**
	 * Retrieves the items stored*/
	private Item[] showItems(){
		return null;
	}

	/**
	 * Retrieves an item from the list of stored items based on its name.
	 *
	 * @param itemName*/
	public Item getItem(String itemName) {
		Item item = null;
		for(Item i: items){
			System.out.println(i.getName());
			if(i.getName().equals(itemName)) item = i;
		}
		return item;
	}

	/**
	 * Retrieves the item information stored in the backpack as a two-dimensional array.
	 * Each sub-array contains details about an item, with specific properties
	 * defined and returned by the implementation of the {@link Item#getItemInfo()} method.
	 *
	 * @return a two-dimensional array of Strings where each sub-array represents
	 *         the properties of an item in the backpack.
	 */
	public String[][] getItems(){
		String[][] items = new String[this.items.size()][2];
		for (int i = 0; i<this.items.size(); i++ ){
			items[i] = this.items.get(i).getItemInfo();
		}
		return  items;
	}
}
