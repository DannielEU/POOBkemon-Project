package domain;

import persistence.ItemRepository;
import persistence.MovesRepository;
import persistence.PokemonRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase principal que representa el juego POOBkemon.
 * La clase administra toda la lógica y los aspectos fundamentales del juego,
 * incluyendo la creación de entrenadores, Pokémon, manejo de batallas, y control general.
 * Utiliza un patrón Singleton para garantizar que solo exista una única instancia del juego.
 */
public class POOBkemon implements Serializable {

	protected ArrayList<String> moves;
	protected ArrayList<Integer> order;
	private boolean finishBattle = false;
	protected ArrayList<Team> teams;
	protected int nid = 0;
	protected int trainerId = 0;
	protected boolean random;
	protected boolean ok;
	protected static POOBkemon game;
	private int pokemonLvl = 1;
	private int winner = -1;
	private int counter = 1;


	/**
	 * Constructor del Juego
	 */
	protected POOBkemon() {}

	/**
	 * Obtiene la instancia singleton del juego POOBkemon.
	 * @return Instancia única del juego
	 * @throws IllegalStateException Si el juego no ha sido inicializado
	 */
	public static POOBkemon getInstance(){
		if (game == null) {
			game = new POOBkemon();
		}
		return game;
	}

	/**
	 * Resets the current instance of the game by setting
	 * the game property to null. This method is used to
	 * clear or reset the game state.
	 */
	public static void resetInstance(){
		game = null;
	}
	/**
	 * Inicializa el juego con los datos proporcionados.
	 * @param trainers Lista de nombres de entrenadores
	 * @param pokemons Mapa de Pokémon por entrenador (clave: nombre entrenador)
	 * @param items Mapa de objetos por entrenador (clave: nombre entrenador)
	 * @param attacks Mapa de ataques por entrenador (clave: nombre entrenador)
	 * @param random Indica si se usan elementos aleatorios
	 * @throws POOBkemonException Si hay errores en los datos de entrada
	 * @throws NullPointerException Si algún parámetro requerido es nulo
	 */
	public void initGame(ArrayList<String> trainers,
						 HashMap<String, ArrayList<Integer>> pokemons,
						 HashMap<String, String[][]> items,
						 HashMap<String, ArrayList<Integer>> attacks,
						 boolean random) throws POOBkemonException{
		// Validar datos básicos
		if (trainers == null || trainers.isEmpty()) {
			throw new POOBkemonException(POOBkemonException.MISSING_TRAINER_DATA);
		}
		if (pokemons == null || pokemons.isEmpty()) {
			throw new POOBkemonException(POOBkemonException.MISSING_POKEMON_DATA);
		}
		if (items == null || items.isEmpty()) {
			throw new POOBkemonException(POOBkemonException.MISSING_ITEMS_DATA);
		}

		this.teams = new ArrayList<>();
		this.finishBattle = false;
		this.random = random;

		try {
			for (String trainer : trainers) {
				// Validar que el entrenador tenga datos asociados (sino despues dará error)
				if (!pokemons.containsKey(trainer)){
					throw new POOBkemonException(POOBkemonException.INCOMPLETE_DATA + " Entrenador: " + trainer);
				}

				ArrayList<Integer> pokemonIds = pokemons.get(trainer);
				String[][] trainerItems = items.get(trainer);
				ArrayList<Integer> trainerAttacks = attacks.get(trainer);

				// Validar formatos
				if (pokemonIds == null || trainerItems == null || trainerAttacks == null) {
					throw new POOBkemonException(POOBkemonException.INCOMPLETE_DATA);
				}

				//Error de Items aqui
				BagPack bagPack = this.createBagPack(this.createItems(trainerItems));

				Trainer train;
				train = createTrainerByType(trainer, bagPack);
				Team team = this.createTeam(this.createPokemons(pokemonIds, trainerAttacks),train );



				this.teams.add(team);
			}

			this.order = this.coin();
			this.moves = new ArrayList<>();
			this.moves.add("Start Game");
			this.ok = true;

		} catch (NullPointerException | NumberFormatException e) {
			throw new POOBkemonException(POOBkemonException.INVALID_FORMAT + ": " + e.getMessage());
		}
	}

	/**
	 * Increments the value of the `nid` property by 1.
	 * This method is used to update the internal state of the object
	 * by advancing the numeric identifier for Pokémon.
	 */
	protected void nextIdPokemon(){
		this.nid = this.nid + 1;
	}

	/**
	 * Crea un entrenador según su tipo.
	 * @param trainerName Nombre del entrenador (incluye tipo)
	 * @param bagPack Mochila del entrenador
	 * @return Instancia del entrenador creado
	 * @throws POOBkemonException Si no se puede crear el entrenador
	 */
	private Trainer createTrainerByType(String trainerName, BagPack bagPack) throws POOBkemonException {
		String trainerType = trainerName.replaceAll("\\d+$", "");

		Map<String, CheckedFunction<BagPack, Trainer>> trainerFactories = new HashMap<>();
		trainerFactories.put("Player", bp -> new Trainer(trainerId++, bp));
		trainerFactories.put("Offensive", bp -> new Offensive(trainerId++, bp));
		trainerFactories.put("Defensive", bp -> new Defensive(trainerId++, bp));
		trainerFactories.put("Expert", bp -> new Expert(trainerId++, bp));

		CheckedFunction<BagPack, Trainer> factory = trainerFactories.getOrDefault(
				trainerType,
				bp -> new Switcher(trainerId++, bp)
		);

		return factory.apply(bagPack);
	}
	/**
	 * Interfaz funcional para métodos que pueden lanzar excepciones.
	 * @param <T> Tipo de entrada
	 * @param <R> Tipo de retorno
	 */
	@FunctionalInterface
	interface CheckedFunction<T, R> {
		R apply(T t) throws POOBkemonException;
	}


	/**
	 * Crea una maleta para el entrenador
	 * @return bagPack Mochila del entrenador
	 * @return ArrayList<Item>
	 */
	private BagPack createBagPack(ArrayList<Item> items) {
		BagPack bagPack = new BagPack(items);
		return bagPack;
	}
	/*
	* metodo para crear los items
	* @return ArrayList<Item>
	* @param items
	 */
	private ArrayList<Item> createItems(String[][] items) {
		ArrayList<Item> item = new ArrayList<Item>();
		Potion ite = null;
		Revive ite0 = null;
		for(String[] i: items){
			if(i[0].equals("Potion")){
				ite = new Potion(Integer.parseInt(i[1]),Integer.parseInt(i[2]));

				item.add(ite);
			}else if (i[0].equals("Revive")) {
				ite0 = new Revive(Integer.parseInt(i[1]));
				item.add(ite0);
			}
		}
		return item;
	}

	/**
	 * crear los Pokemons
	 * @param pokemonIds Lista de IDs de Pokémon
	 *
	 */
	public ArrayList<Pokemon> createPokemons(ArrayList<Integer> pokemonIds, ArrayList<Integer> attackIds) throws POOBkemonException {
		ArrayList<Pokemon> pokemons = new ArrayList<>();
		final int ATTACKS_PER_POKEMON = 4;
		int totalAttacksNeeded = pokemonIds.size() * ATTACKS_PER_POKEMON;

		// Verificar si hay suficientes ataques
		if (attackIds.size() < totalAttacksNeeded) {
			throw new IllegalArgumentException(
					"No hay suficientes ataques. Se necesitan " + totalAttacksNeeded +
							", pero solo hay " + attackIds.size()
			);
		} // hacer la exception en la clase correspondiente.
		int attackIndex = 0;
		for (Integer pokemonId : pokemonIds) {
			// Obtener 4 ataques consecutivos para este Pokémon
			List<Integer> attacksForPokemon = attackIds.subList(
					attackIndex,
					attackIndex + ATTACKS_PER_POKEMON
			);

			// Crear el Pokémon con sus ataques (asumo que createPokemon acepta una lista)
			Pokemon pokemon = this.createPokemon(pokemonId, new ArrayList<>(attacksForPokemon));
			pokemons.add(pokemon);
			// Avanzar al siguiente bloque de 4 ataques
			attackIndex += ATTACKS_PER_POKEMON;
		}
		return pokemons;
	}

	/**
	 * Crea un Pokémon con sus características.
	 * @param id ID del Pokémon en el repositorio
	 * @param attackIds Lista de IDs de ataques
	 * @return Pokémon creado
	 */
	public Pokemon createPokemon(int id, ArrayList<Integer> attackIds) throws POOBkemonException{
		PokemonRepository info = new PokemonRepository();
		String[] infoPokemon = info.getPokemonId(id);
		Pokemon pokemon = new Pokemon(nid,infoPokemon,attackIds, this.random, this.pokemonLvl);
		this.nextIdPokemon();
		return pokemon;
	}

	/**
	 * Crea un equipo de Pokémon.
	 * @param pokemones Lista de Pokémon del equipo
	 * @return Equipo creado
	 */
	private Team createTeam(ArrayList<Pokemon> pokemones, Trainer trainer){
		Team team = new Team(pokemones, trainer);
		return team;
	}

	/**
	 * Determina aleatoriamente el orden de turnos inicial.
	 * @return Lista con IDs de entrenadores en orden de turno
	 */
	public ArrayList<Integer> coin(){
		ArrayList<Integer> turnOrder = new ArrayList<>();
		if (this.teams.size() < 2) return turnOrder;

		int t1 = teams.get(0).getTrainer().getId();
		int t2 = teams.get(1).getTrainer().getId();
		if (Math.random()<0.5) {
			turnOrder.add(t1);
			turnOrder.add(t2);
		} else {
			turnOrder.add(t2);
			turnOrder.add(t1);
		}
		return turnOrder;
	}
	/**
	*metodo para obtener las decisiones de la maquina
	* @param idTrainer id del entrenador
	* @return String[]
	 **/
	public String[] machineDecision(int idTrainer) throws POOBkemonException {
		for (Team t : teams) {
			if (idTrainer == t.getTrainer().getId()) {
				Trainer trainer = t.getTrainer();

				if (trainer instanceof Machine) {
					String[] desci = ((Machine) trainer).machineMovement(this);
					return desci;
				}
			}
		}
		return null;
	}


	/**
	 * Método para procesar las decisiones de los entrenadores en un turno
	 * @param decisionTrainer Lista de decisiones del entrenador (String)
	 * @throws POOBkemonException Si hay errores en las decisiones
	 */
	public void takeDecision(String[] decisionTrainer) throws POOBkemonException {
		if(this.finishBattle)return;
		if(this.counter % 2 == 0) {
			this.applyStates();
		}
		this.counter++;
		this.checkBattleStatus();
		// Validar que haya dos entrenadores
		if (this.teams.size() < 2) {
			throw new POOBkemonException("Se necesitan al menos 2 entrenadores para una batalla");
		}
		// Procesar decisiones en el orden establecido (order)

		String[] decision = decisionTrainer;

		if (decision == null || decision.length == 0) {
			throw new POOBkemonException("Se necesita introducir un movimiento valido");
		}

		if(this.winner == -1) {

			String action = decision[0];
			try {
				switch (action) {
					case "Attack":
						if (decision.length < 3) throw new POOBkemonException("Faltan parámetros para Attack");
						int attackId = Integer.parseInt(decision[1]);
						int pokemonId1 = Integer.parseInt(decision[2]);
						int trainerId = Integer.parseInt(decision[3]);
						this.attack(attackId, trainerId, pokemonId1);
						checkBattleStatus();
						break;

					case "UseItem":
						if (decision.length < 2) throw new POOBkemonException("Faltan parámetros para UseItem");
						int idTrainer = Integer.parseInt(decision[1]);
						int pokemonId = Integer.parseInt(decision[2]);
						String datoItem = decision[3];
						this.useItem(idTrainer, pokemonId, datoItem);
						break;

					case "ChangePokemon":
						if (decision.length < 3) throw new POOBkemonException("Faltan parámetros para ChangePokemon");
						int newPokemonId = Integer.parseInt(decision[2]);
						this.changePokemon(Integer.parseInt(decision[1]), newPokemonId);
						String pokemonName = "";

						pokemonName = this.searchPokemon(newPokemonId).getName();
						moves.add("Player " + decision[1] + " cambió a Pokémon " + pokemonName);

						break;

					case "Run":
						this.run(Integer.valueOf(decision[1]));
						moves.add("Player " + decision[1] + " huyó de la batalla");
						this.finishBattle = true;
						break;

					case "timeOver":
						if (decision.length < 2) throw new POOBkemonException("Faltan parámetros para Attack");
						int trainerid = Integer.parseInt(decision[1]);
						int pokemonid = Integer.parseInt(decision[2]);
						this.timeOver(trainerid, pokemonid);
						moves.add("Player se le acabo el tiempo");
						break;
					default:
						throw new POOBkemonException("Acción no reconocida: " + action);
				}
				checkBattleStatus();
			} catch (NumberFormatException e) {
				throw new POOBkemonException("Formato inválido en parámetros: " + e.getMessage());
			}
			// Verificar estado de la batalla después de ambos turnos
			checkBattleStatus();
		}
	}

	/**
	 * Verifica si el entrenador es una máquina.
	 * @param TrainerId
	 * @return
	 */
	public boolean isMachine(int TrainerId){

		for (Team t : teams) {
			if(t.getTrainer().getId() == TrainerId){
				if(t.getTrainer() instanceof Machine){
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Verifica el estado de la batalla y determina si ha terminado.
	 */
    public void checkBattleStatus() {
		if(this.finishBattle)return;
		for (Team team : teams) {
			if (team.allFainted()) {
				this.finishBattle = true;
				moves.add("¡Batalla terminada! " + team.getTrainer().getId() + " ha sido derrotado");
				this.searchWinner(team);
				break;
			}
		}
	}

	/**
	 * Busca el ganador de la batalla.
	 * @param team
	 */
	private void searchWinner(Team team){
		if(this.winner != -1)return;
		for (Team t : teams) {
			if(t.getTrainer().getId() != team.getTrainer().getId()){
				this.setWinner(t);
			}
		}
	}

	/**
	 * Cambia el Pokémon activo de un entrenador.
	 * @param trainerId ID del entrenador
	 * @param pokemonId ID del nuevo Pokémon a activar
	 * @throws POOBkemonException Si el entrenador no existe o el Pokémon no es válido
	 */
	public void changePokemon(int trainerId, int pokemonId) throws POOBkemonException {
		if(this.finishBattle)return;
		// Buscar el entrenador
		Trainer selectedTrainer = null;
		Team team = null;
		for (Team t : this.teams) {

			if (t.getTrainer().getId() == trainerId) {
				selectedTrainer = t.getTrainer();
				team = t;
				break;
			}
		}
		// Verificar si se encontró el entrenador
		if (selectedTrainer == null) {
			throw new POOBkemonException("Entrenador con ID " + trainerId + " no encontrado");
		}
		// Intentar cambiar el Pokémon
		team.changePokemon(pokemonId);

	}

	/**
	 * Decision auntomatica si se le acaba el tiempo al entrenador
	 * @param trainerId Id del entrador al que se le ejecuta la accion
	 * @param pokemonId Id el pokemon al que se le ejecuta la accion
	 */
	private void timeOver(int trainerId, int pokemonId) throws POOBkemonException {
		if(this.finishBattle)return;
		Team team = null;
		for (Team t : teams)
			if (t.getTrainer().getId() == trainerId) {
				team = t;
			}
		if (team == null) {
			throw new POOBkemonException("Entrenador con ID " + trainerId + " no fue encontrado");
		}
		team.timeOver(pokemonId);
	}
	/**
	 * metodo parasalir de la pelea
	 */
	private void run(int trainer) {
		if(this.finishBattle)return;
		Team team_1 = null;
		for (Team team : teams) {
			if (team.getTrainer().getId() == trainer) {
				this.moves.add("GameOver para el jugador " + team.getTrainer().getId());
			}else{
				team_1 = team;
			}
		}
		this.setWinner(team_1);
		this.finishBattle = true;
	}
	/**
	 * metodo para obtener el ganador de la pelea
	 */
	private void setWinner(Team team_1) {
		this.winner = team_1.getTrainer().getId();
	}
	public int getWinner() throws POOBkemonException{
		if(this.winner == -1){
			throw new POOBkemonException("No hay ganador aún");
		}else{
			return this.winner;
		}
	}
	/**
	 * metodo para usar un item
	 */
	private void useItem(int trainer, int idPokemon, String datoItem) throws POOBkemonException {
		if(this.finishBattle)return;
		checkBattleStatus();
		Team team_0 = null;
		if (!finishBattle) {
			for (Team team : teams) {
				if (team.getTrainer().getId() == trainer) {
					team_0 = team;
					break;
				}
			}
		}
		if(team_0 == null)throw new POOBkemonException("Error: No se encontró el Equipo, para el uso de Item");
		team_0.useItem(idPokemon,datoItem);
		String message = "El entrenador " + trainer + " ha usado " + datoItem + " en " + team_0.getPokemonById(idPokemon).getName();
		this.moves.add(message);
	}
	/**
	 * Realiza un ataque entre Pokémon.
	 * @param idAttack ID del ataque a utilizar
	 * @param idThrower ID del Pokémon que realiza el ataque
	 */
	public void attack(int idAttack, int idTrainer, int idThrower) throws POOBkemonException{
		if(this.finishBattle)return;
		Attack damage = null;
		Pokemon attacker = null;
		Pokemon target = null;
		// Primero: Encontrar el Pokémon atacante (idThrower) en el equipo del entrenador (idTrainer)
		for (Team team : teams) {
			if (team.getTrainer().getId() == idTrainer) { // Buscamos en el equipo del entrenador
				for (Pokemon pokemon : team.getPokemons()) {
					if (pokemon.getId() == idThrower) {
						attacker = pokemon;
						damage = pokemon.getAttack(idAttack);
						break;
					}
				}
				break; // Salimos después de encontrar el equipo correcto
			}
		}

		// Segundo: Encontrar el Pokémon objetivo (el activo del otro equipo)
		for (Team team : teams) {
			if (team.getTrainer().getId() != idTrainer) { // Buscamos en el equipo contrario
				for (Pokemon pokemon : team.getPokemons()) {
					if (pokemon.getActive()) {
						target = pokemon;
						break;
					}
				}
				break; // Solo necesitamos un objetivo activo
			}
		}



		// Verificar que tenemos ambos Pokémon
		if (attacker == null) {
			throw new POOBkemonException("Error: No se encontró el Pokemon atacante");
		}
		if( damage == null){
			throw new POOBkemonException("Error: No se encontró el ataque");
		}
		if (target == null) {
			throw new POOBkemonException("Error: No se encontró un Pokémon objetivo activo");
		}
		// Aplicar el daño
		damage.usePP();
		String effect = target.getDamage(damage, attacker);

		//agrego el mensaje de la ultima acción
		if(effect.equals("")){
			//si el pokemon atacante ya esta debilitado no agregar nada.
		}else {
			String message = attacker.getName() + " atacó a " + target.getName() + effect;
			this.moves.add(message);
		}
		if(this.finishBattle)return;
		// Cambio automático si es necesario
		this.autoChangePokemon();
	}

	/**
	 * Metodo que cambia el pokemon activo si es necesario.
	 * @throws POOBkemonException
	 */
	private void autoChangePokemon() throws POOBkemonException{
		if(this.finishBattle)return;
			for (Team team : teams) {
				for (Pokemon pokemon : team.getPokemons()) {
					if (pokemon.getWeak() && pokemon.getActive()) {
						this.checkBattleStatus();
						if(this.finishBattle)return;
						int savePokemon = this.getAlivePokemon(team.getTrainer().getId());
						this.changePokemon(team.getTrainer().getId(), savePokemon);
						String message = pokemon.getName() + " ha sido debilitado, cambiando a " + searchPokemon(savePokemon).getName();
						this.moves.add(message);
						return;
					}
				}
			}
	}

	/**
	 * Busca un Pokemon en el equipo de un entrenador.
	 * @param id
	 * @return
	 */
	private Pokemon searchPokemon(int id){
		try {
			for (Team team : teams) {
				for (Pokemon pokemon : team.getPokemons()) {
					if (pokemon.getId() == id) {
						return pokemon;
					}
				}
			}
			throw new POOBkemonException("Error: No se encontró el Pokemon con ID " + id);
		}catch (POOBkemonException e){
			return null;
		}
	}

	/**
	 * Obtiene el ID del Pokemon activo que no sea muy dificil.
	 * @param trainerId
	 * @return
	 */
	private int getAlivePokemon(int trainerId){
		if(this.finishBattle)return -1;
		int id = -1;
		for (Team team : teams) {
			if(team.getTrainer().getId() == trainerId){
				for(Pokemon p: team.getPokemons()){
					if(!p.getWeak()){
						id = p.getId();
					}
				}
			}
		}
		return id;
	}

	public ArrayList<Team> getTeams(){
		return teams;
	}
	public boolean isOk (){
		return this.ok;
	}
	public ArrayList<String> getMoves(){
		return this.moves;
	}
	public String getLastMoves(){
		return this.moves.getLast();
	}
	public boolean finishBattle(){
		return this.finishBattle;
	}
	public ArrayList<Team> teams(){
		return this.teams;
	}
	public ArrayList<Integer> getOrder(){
		return order;
	}

	/**
	 * Obtiene los Pokémon actualmente activos de cada entrenador.
	 * @return Mapa con ID de entrenador como clave y array de información del Pokémon activo como valor
	 */
	public HashMap<Integer, String[]> getCurrentPokemons() {
		if(this.teams == null) throw new NullPointerException("No hay equipos");
		HashMap<Integer, String[]> pokemons = new HashMap<>();
		for (Team t : this.teams) {
			try {
				int trainerid = t.getTrainer().getId();
				Trainer currentTrainer = t.getTrainer();
				Pokemon currentPokemon = t.getPokemonById(currentTrainer.getCurrentPokemonId());// Obtiene el objeto Pokemon
				String[] pokemonInfo = currentPokemon.getInfo();
				pokemons.put(trainerid, pokemonInfo);
			} catch (POOBkemonException e) {
				pokemons.put(t.getTrainer().getId(), new String[]{"No active Pokémon"});
			}
		}
		return pokemons;
	}

	/**
	 * Obtiene los IDs de los Pokémon inactivos de un entrenador.
	 * @param idTrainer
	 * @return
	 */
	public int[] getPokemonsInactive(int idTrainer) {
		if(this.finishBattle)return null;
		for (Team t : teams) {
			if (t.getTrainer().getId() == idTrainer) {
				ArrayList<Pokemon> pokemons = t.getPokemons();
				int activePokemonId = t.getTrainer().getCurrentPokemonId();
				ArrayList<Integer> inactiveIds = new ArrayList<>();
				for (Pokemon p : pokemons) {
					if (!p.getActive()) {
						inactiveIds.add(p.getId());
					}
				}
				// Convertir ArrayList a arreglo
				int[] result = new int[inactiveIds.size()];
				for (int i = 0; i < inactiveIds.size(); i++) {
					result[i] = inactiveIds.get(i);
				}
				return result;
			}
		}
		return new int[0]; // Si no se encuentra el entrenador
	}

	/**
	 * Obtiene los IDs de los Pokémon activos de un entrenador.
	 * @param idTrainer
	 * @return
	 */
	public int[] getPokemonsPerTrainer(int idTrainer) {
		if(this.finishBattle)return null;
		for (Team t : teams) {
			if (t.getTrainer().getId() == idTrainer) {
				ArrayList<Pokemon> pokemons = t.getPokemons();
				int activePokemonId = t.getTrainer().getCurrentPokemonId();
				ArrayList<Integer> inactiveIds = new ArrayList<>();
				for (Pokemon p : pokemons) {
					inactiveIds.add(p.getId());
				}
				// Convertir ArrayList a arreglo
				int[] result = new int[inactiveIds.size()];
				for (int i = 0; i < inactiveIds.size(); i++) {
					result[i] = inactiveIds.get(i);
				}
				return result;
			}
		}
		return new int[0]; // Si no se encuentra el entrenador
	}

	/**
	 * Obtiene la información de un Pokémon específico de un entrenador.
	 *
	 * @param idTrainer ID del entrenador dueño del Pokémon
	 * @param idPokemon ID del Pokémon a buscar
	 * @return Array de Strings con la información del Pokémon
	 * @throws POOBkemonException Si no se encuentra el entrenador o el Pokémon,
	 *                           o si ocurre un error al acceder a la información
	 */
	public String[] getPokemonInfo(int idTrainer, int idPokemon) throws POOBkemonException {
		if(this.finishBattle)return null;
		// Buscar el entrenador
		String[] infoPokemon = null;
		for (Team t : teams) {
			if (t.getTrainer().getId() == idTrainer) {
				infoPokemon = t.getPokemonById(idPokemon).getInfo();
			}
		}
		if(infoPokemon == null) throw new POOBkemonException("Entrenador con ID " + idTrainer + " no encontrado");
		return infoPokemon;
	}

	/**
	 * Obtiene la información de un Pokémon específico de un entrenador.
	 * @return HasMap
	 */
	public HashMap<Integer, String[][]> getActiveAttacks(){
		if(this.finishBattle)return null;
		HashMap<Integer, String[][]> pokemons = new HashMap<>();
		for (Team t : this.teams) {
			try {
				int trainerid = t.getTrainer().getId();
				Trainer currentTrainer = t.getTrainer();
				Pokemon currentPokemon = t.getPokemonById(currentTrainer.getCurrentPokemonId());// Obtiene el objeto Pokemon
				String[][] pokemonInfo = currentPokemon.getAttacksInfo();
				pokemons.put(trainerid, pokemonInfo);
			} catch (POOBkemonException e) {
				pokemons.put(t.getTrainer().getId(), new String[][]{{"Sin ataques activos"}});
			}
		}
		return pokemons;
	}
	//==================Informacion==========================
	/**
	 * Obtiene información de todos los Pokémon disponibles.
	 * @return Lista de arrays con información de Pokémon
	 */
	public ArrayList<String[]> getPokInfo(){
		ArrayList<String[]> info = new PokemonRepository().getPokemons();
		return info;
	}
	/**
	 * Obtiene información de todos los objetos disponibles.
	 * @return Lista de listas con información de objetos
	 */
	public ArrayList<ArrayList<String>> getItemInfo(){
		if(this.finishBattle)return null;
		ArrayList<ArrayList<String>> info = new ItemRepository().getItems();
		return info;
	}
	/**
	 * Obtiene información de un ataque específico.
	 * @param id ID del ataque
	 * @return String con información del ataque
	 */
	public String getMoveInfo(int id){
		String info = new MovesRepository().getAttackId(id);
		return info;
	}

	/**
	 * Obtiene información de todos los ataques disponibles.
	 * @param trainerId
	 * @return
	 * @throws POOBkemonException
	 */
	public String[][] getInfoItems(int trainerId) throws POOBkemonException {
		Trainer trainer = null;
		for (Team t: this.teams){
			if(t.getTrainer().getId() == trainerId){
				trainer = t.getTrainer();
			}
		}
		if(trainer == null) throw new POOBkemonException("Entrenador no encontrado");
		return trainer.getBagPack().getItems();
	}

	/**
	 * Guarda la batalla en un archivo.
	 * @param archivo
	 * @throws POOBkemonException
	 */
	public void save(File archivo) throws POOBkemonException {
		try (ObjectOutputStream writer = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(archivo)))) {
			writer.writeObject(this);
		} catch (FileNotFoundException e) {
			throw new POOBkemonException("Archivo no encontrado al intentar guardar: " + archivo.getAbsolutePath());
		} catch (IOException e) {
			throw new POOBkemonException("Error de E/S al guardar la batalla: " + e.getMessage());
		}
	}

	/**
	 * Carga una batalla de un archivo.
	 * @param archivo
	 * @return
	 * @throws POOBkemonException
	 */
	public static POOBkemon open(File archivo) throws POOBkemonException {
		try (ObjectInputStream reader = new ObjectInputStream(
				new BufferedInputStream(new FileInputStream(archivo)))) {

			return (POOBkemon) reader.readObject();
		} catch (FileNotFoundException e) {
			throw new POOBkemonException("Archivo no encontrado al intentar abrir: " + archivo.getAbsolutePath());
		} catch (ClassNotFoundException e) {
			throw new POOBkemonException("Clase no encontrada al leer el archivo: " + e.getMessage());
		} catch (IOException e) {
			throw new POOBkemonException("Error de E/S al abrir la batalla: " + e.getMessage());
		}
	}

	/**
	 * Agrega un movimiento a la lista de movimientos.
	 * @param move
	 */
	public void addMove(String move){
		this.moves.add(move);
	}

	/**
	 * Cambia el estado de la batalla.
	 * @param ok
	 */
	public void setOk(boolean ok){
		this.ok = ok;
	}

	/**
	 * Obtiene todos los ataques compatibles con un Pokémon.
	 * @param pokemonId
	 * @return
	 */
	public ArrayList<String[]> getCompatibleAttacks(int pokemonId) {
		ArrayList<String[]> compatibleAttacks = new MovesRepository().getCompatibleAttacks(pokemonId);
		return compatibleAttacks;
	}

	/**
	 * Obtiene el nombre de un ataque.
	 * @param id
	 * @return
	 */
	public String getAttackId(int id) {
		String move = new MovesRepository().getAttackId(id);
		return move;
	}

	/**
	 * Aplica los efectos de los Pokémon en la batalla.
	 */
	private void applyStates(){
		for (Team team : teams) {
			team.applyEffect();
		}
	}

	public String[] getPokemonInfoById(int id){
		PokemonRepository pokemonInfo = new PokemonRepository();
		return pokemonInfo.getPokemonId(id);
	}
}
