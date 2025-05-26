package domain;

import java.io.Serializable;
import java.util.List;

public class Trainer implements Serializable {

	private int currentPokemonId = -1;  // Ahora es solo el ID (int)
	private BagPack bagPack;
	private int id;

	public Trainer(int id, BagPack bagPack) throws POOBkemonException {
		if (bagPack == null) {
			throw new POOBkemonException(POOBkemonException.NULL_BAGPACK);
		}
		this.id = id;
		this.bagPack = bagPack;
	}

	public int getId() {
		return id;
	}
	public BagPack getBagPack() {
		return bagPack;
	}
	public int getCurrentPokemonId() {
		return currentPokemonId;
	}
	public void setCurrentPokemonId(int currentPokemonId) {
		this.currentPokemonId = currentPokemonId;
	}
	public void useItem(Pokemon pokemon, String item) throws POOBkemonException{
		Item itemUse = this.bagPack.getItem(item);
		if(itemUse == null) throw new POOBkemonException("No se encontró el Item. ");
		itemUse.effect(pokemon);
	}
}