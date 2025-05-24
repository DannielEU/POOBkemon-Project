package domain;

import java.io.Serializable;
import java.util.ArrayList;

public class BagPack implements Serializable {
	private ArrayList<Item> items;

	public BagPack(ArrayList<Item> items){
		this.items = new ArrayList<Item>(items);
	}

	private Item[] showItems(){
		return null;
	}

	public Item getItem(String itemName) {
		Item item = null;
		for(Item i: items){
			System.out.println(i.getName());
			if(i.getName().equals(itemName)) item = i;
		}
		return item;
	}
	public String[][] getItems(){
		String[][] items = new String[this.items.size()][2];
		for (int i = 0; i<this.items.size(); i++ ){
			items[i] = this.items.get(i).getItemInfo();
		}
		return  items;
	}
}
