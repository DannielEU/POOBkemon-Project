package domain;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a team composed of Pokémon and a Trainer. This class manages Pokémon
 * activities such as changing the active Pokémon, checking team status, and applying effects.
 * It also facilitates Trainer interactions with the Pokémon, such as using items.
 */
public class Team implements Serializable {

	private ArrayList<Pokemon> pokemons;
	private Trainer trainer;

	/**
	 * Constructs a Team object with a list of Pokémon and a trainer.
	 * Sets the first Pokémon in the list as active and updates the trainer's current Pokémon ID accordingly.
	 *
	 * @param pokemons the list of Pokémon that belong to the team
	 * @param trainer the trainer who owns the team
	 */
	public Team(ArrayList<Pokemon> pokemons, Trainer trainer) {
		this.pokemons = pokemons;
		this.trainer = trainer;
		this.pokemons.get(0).setActive(true);
		this.trainer.setCurrentPokemonId(this.pokemons.get(0).getId());
	}
	public ArrayList<Pokemon> getPokemons() {
		return pokemons;
	}
	/**
	 * Cambia el pokémon activo por el que coincida con el ID especificado
	 * @param id ID del pokémon a activar
	 * @return El pokémon que fue activado
	 * @throws POOBkemonException Si no se encuentra el pokémon o está debilitado
	 */
	public Pokemon changePokemon(int id) throws POOBkemonException {
		Pokemon pokemonToActivate = null;
		Pokemon currentActive = null;

		// Primer recorrido para identificar ambos pokémones
		for (Pokemon pokemon : pokemons) {
			if (pokemon.getActive()) {
				currentActive = pokemon;
			}
			if (pokemon.getId() == id) {
				pokemonToActivate = pokemon;
			}
		}
		if (pokemonToActivate == null) {
			throw new POOBkemonException( POOBkemonException.POKEMON_ID_NOT_FOUND + id );
		}
		if (pokemonToActivate.currentHealth <= 0) {
			throw new POOBkemonException(POOBkemonException.POKEMON_WEAK_CHANGE);
		}
		// Cambiar estados
		if (currentActive != null) {
			currentActive.setActive(false);
		}
		pokemonToActivate.setActive(true);
		this.trainer.setCurrentPokemonId(pokemonToActivate.getId());
		return pokemonToActivate;
	}

	/**
	 * Checks if all Pokémon in the team have fainted.
	 *
	 * @return true if all Pokémon in the team's list are in a weakened state, false otherwise
	 */
	public boolean allFainted(){
		for (Pokemon pokemon : this.pokemons) {
			if(!pokemon.getWeak()) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Obtiene los IDs de los Pokémon inactivos en el equipo, excluyendo al Pokémon actual.
	 * @param currentPokemon ID del Pokémon actualmente activo (que se excluirá de los resultados)
	 * @return Arreglo de enteros con los IDs de los Pokémon inactivos
	 */
	public int[] getPokemonsInactive(int currentPokemon) {
		// Lista para almacenar los IDs de los Pokémon inactivos
		ArrayList<Integer> inactiveList = new ArrayList<>();

		// Filtrar Pokémon inactivos que no sean el actual
		for (Pokemon pokemon : pokemons) {
			if(!pokemon.getActive()) {
				inactiveList.add(Integer.parseInt(pokemon.idPokedex));
			}
		}
		// Convertir ArrayList a array primitivo
		int[] inactivePokemons = new int[inactiveList.size()];
		for (int i = 0; i < inactiveList.size(); i++) {
			inactivePokemons[i] = inactiveList.get(i);
		}

		return inactivePokemons;
	}

	/**
	 * Retrieves a Pokémon from the team's list by its unique ID.
	 *
	 * @param id the unique identifier of the Pokémon to be retrieved
	 * @return the Pokémon object that matches the specified ID
	 * @throws POOBkemonException if no Pokémon with the given ID is found in the team's list
	 */
	public Pokemon getPokemonById(int id) throws POOBkemonException {
		Pokemon pokemon = null;
		for (Pokemon p : pokemons) {
			if (p.getId() == id) {
				pokemon = p;
			}
		}
		if(pokemon == null)throw new POOBkemonException(POOBkemonException.POKEMON_ID_NOT_FOUND + id);
		return pokemon;
	}

	/**
	 * Retrieves the trainer associated with the team.
	 *
	 * @return the trainer object that owns the team
	 */
	public Trainer getTrainer() {
		return trainer;
	}

	/**
	 * Uses an item on a specific Pokémon in the team based on the Pokémon's ID.
	 * Searches the team for the Pokémon with the given ID, and if found, applies the specified item.
	 * Throws an exception if the Pokémon with the provided ID does not exist in the team.
	 *
	 * @param idPokemon the unique identifier of the Pokémon on which the item should be used
	 * @param datoItem the name or data of the item to be used on the Pokémon
	 * @throws POOBkemonException if the Pokémon with the specified ID is not found in the team
	 */
	public void useItem(int idPokemon,String datoItem) throws POOBkemonException{
		Pokemon pokemonTarget = null;
		for(Pokemon p : pokemons){
			if(p.getId() == idPokemon){
				pokemonTarget = p;
				break;
			}
		}
		//lanza error si no lo encuentra
		if(pokemonTarget == null){
			throw new POOBkemonException(POOBkemonException.POKEMON_ID_NOT_FOUND + idPokemon);
		}
		this.getTrainer().useItem(pokemonTarget, datoItem);
	}
	/**
	 * Decision auntomatica si se le acaba el tiempo al entrenador
	 * @param pokemonID Id del pokemon al que se le ejecuta la accion
	 */
	public void timeOver(int pokemonID) {
		for (Pokemon p : pokemons) {
			if (p.getId() == pokemonID) {
				p.timeOver();
			}
		}
	}

	/**
	 * Applies the current state effects to all Pokémon in the team.
	 *
	 * This method iterates through the list of Pokémon associated with the team
	 * and invokes the `applyState()` method on each Pokémon. The `applyState()`
	 * method is expected to handle any state-related updates or effects for
	 * the individual Pokémon.
	 */
	public void applyEffect(){
		for (Pokemon p : pokemons) {
			p.applyState();
		}
	}
}
