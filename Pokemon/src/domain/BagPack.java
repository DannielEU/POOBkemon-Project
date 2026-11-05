package domain;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Mochila de un entrenador: colección de ítems consultables por nombre.
 */
public class BagPack implements Serializable {

	private final ArrayList<Item> items;

	/**
	 * @param items ítems iniciales (se copia la lista)
	 */
	public BagPack(ArrayList<Item> items){
		this.items = new ArrayList<>(items);
	}

	/**
	 * Busca un ítem por su nombre exacto.
	 *
	 * @param itemName nombre del ítem
	 * @return el ítem, o {@code null} si la mochila no lo tiene
	 */
	public Item getItem(String itemName) {
		for (Item item : items) {
			if (item.getName().equals(itemName)) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Información de los ítems: cada fila es {nombre, cantidad}.
	 */
	public String[][] getItems(){
		String[][] info = new String[items.size()][];
		for (int i = 0; i < items.size(); i++) {
			info[i] = items.get(i).getItemInfo();
		}
		return info;
	}
}
