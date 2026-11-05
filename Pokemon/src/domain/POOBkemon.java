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

	private static final int ATTACKS_PER_POKEMON = 4;
	private static final int DEFAULT_POKEMON_LEVEL = 1;
	/** Cada cuántas decisiones se aplican los efectos de estado (cada turno completo). */
	private static final int TURNS_PER_STATE_TICK = 2;
	private static final int NO_WINNER = -1;

	/** Construye un entrenador a partir de su id y su mochila (Factory Method). */
	@FunctionalInterface
	private interface TrainerBuilder {
		Trainer build(int id, BagPack bagPack) throws POOBkemonException;
	}

	/** Tipos de entrenador reconocidos; cualquier otro nombre se trata como {@link Switcher}. */
	private static final Map<String, TrainerBuilder> TRAINER_BUILDERS = Map.of(
			"Player", Trainer::new,
			"Offensive", Offensive::new,
			"Defensive", Defensive::new,
			"Expert", Expert::new);

	protected ArrayList<String> moves;
	protected ArrayList<Integer> order;
	private boolean finishBattle = false;
	protected ArrayList<Team> teams;
	protected int nid = 0;
	protected int trainerId = 0;
	protected boolean random;
	protected boolean ok;
	protected static POOBkemon game;
	private int winner = NO_WINNER;
	private int counter = 1;

	protected POOBkemon() {}

	/**
	 * Obtiene la instancia única del juego, creándola si aún no existe.
	 * @return Instancia única del juego
	 */
	public static POOBkemon getInstance(){
		if (game == null) {
			game = new POOBkemon();
		}
		return game;
	}

	/**
	 * Descarta la instancia actual; la siguiente llamada a {@link #getInstance()} crea una nueva.
	 */
	public static void resetInstance(){
		game = null;
	}

	/**
	 * Nivel con el que se crean los Pokémon. Los modos de juego lo sobrescriben (Template Method).
	 * @return nivel inicial de los Pokémon
	 */
	protected int pokemonLevel() {
		return DEFAULT_POKEMON_LEVEL;
	}

	/**
	 * Inicializa el juego con los datos proporcionados.
	 * @param trainers Lista de nombres de entrenadores
	 * @param pokemons Mapa de Pokémon por entrenador (clave: nombre entrenador)
	 * @param items Mapa de objetos por entrenador (clave: nombre entrenador)
	 * @param attacks Mapa de ataques por entrenador (clave: nombre entrenador)
	 * @param random Indica si se usan elementos aleatorios
	 * @throws POOBkemonException Si hay errores en los datos de entrada
	 */
	public void initGame(ArrayList<String> trainers,
						 HashMap<String, ArrayList<Integer>> pokemons,
						 HashMap<String, String[][]> items,
						 HashMap<String, ArrayList<Integer>> attacks,
						 boolean random) throws POOBkemonException{
		if (trainers == null || trainers.isEmpty()) {
			throw new POOBkemonException(POOBkemonException.MISSING_TRAINER_DATA);
		}
		if (pokemons == null || pokemons.isEmpty()) {
			throw new POOBkemonException(POOBkemonException.MISSING_POKEMON_DATA);
		}
		if (items == null || items.isEmpty()) {
			throw new POOBkemonException(POOBkemonException.MISSING_ITEMS_DATA);
		}

		// Una nueva partida no hereda nada de la anterior (el singleton se reutiliza).
		this.teams = new ArrayList<>();
		this.finishBattle = false;
		this.random = random;
		this.winner = NO_WINNER;
		this.counter = 1;
		this.nid = 0;
		this.trainerId = 0;

		try {
			for (String trainer : trainers) {
				if (!pokemons.containsKey(trainer)){
					throw new POOBkemonException(POOBkemonException.INCOMPLETE_DATA + " Entrenador: " + trainer);
				}

				ArrayList<Integer> pokemonIds = pokemons.get(trainer);
				String[][] trainerItems = items.get(trainer);
				ArrayList<Integer> trainerAttacks = attacks.get(trainer);

				if (pokemonIds == null || trainerItems == null || trainerAttacks == null) {
					throw new POOBkemonException(POOBkemonException.INCOMPLETE_DATA);
				}

				BagPack bagPack = new BagPack(this.createItems(trainerItems));
				Trainer train = createTrainerByType(trainer, bagPack);
				this.teams.add(new Team(this.createPokemons(pokemonIds, trainerAttacks), train));
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
	 * Avanza el identificador que se asignará al siguiente Pokémon creado.
	 */
	protected void nextIdPokemon(){
		this.nid++;
	}

	/**
	 * Crea un entrenador según su tipo.
	 * @param trainerName Nombre del entrenador (el tipo es el nombre sin dígitos finales)
	 * @param bagPack Mochila del entrenador
	 * @return Instancia del entrenador creado
	 * @throws POOBkemonException Si no se puede crear el entrenador
	 */
	private Trainer createTrainerByType(String trainerName, BagPack bagPack) throws POOBkemonException {
		String trainerType = trainerName.replaceAll("\\d+$", "");
		TrainerBuilder builder = TRAINER_BUILDERS.getOrDefault(trainerType, Switcher::new);
		return builder.build(trainerId++, bagPack);
	}

	/**
	 * Crea los ítems de un entrenador a partir de filas {tipo, cantidad[, cura]}.
	 * Los tipos desconocidos se ignoran.
	 */
	private ArrayList<Item> createItems(String[][] itemData) {
		ArrayList<Item> created = new ArrayList<>();
		for (String[] data : itemData) {
			if (data[0].equals("Potion")) {
				created.add(new Potion(Integer.parseInt(data[1]), Integer.parseInt(data[2])));
			} else if (data[0].equals("Revive")) {
				created.add(new Revive(Integer.parseInt(data[1])));
			}
		}
		return created;
	}

	/**
	 * Crea los Pokémon de un entrenador asignando {@value #ATTACKS_PER_POKEMON} ataques consecutivos a cada uno.
	 * @param pokemonIds IDs de Pokédex de los Pokémon
	 * @param attackIds IDs de ataques, en bloques de {@value #ATTACKS_PER_POKEMON} por Pokémon
	 */
	public ArrayList<Pokemon> createPokemons(ArrayList<Integer> pokemonIds, ArrayList<Integer> attackIds) throws POOBkemonException {
		int totalAttacksNeeded = pokemonIds.size() * ATTACKS_PER_POKEMON;
		if (attackIds.size() < totalAttacksNeeded) {
			throw new IllegalArgumentException(
					"No hay suficientes ataques. Se necesitan " + totalAttacksNeeded +
							", pero solo hay " + attackIds.size());
		}
		ArrayList<Pokemon> pokemons = new ArrayList<>();
		int attackIndex = 0;
		for (Integer pokemonId : pokemonIds) {
			List<Integer> block = attackIds.subList(attackIndex, attackIndex + ATTACKS_PER_POKEMON);
			pokemons.add(this.createPokemon(pokemonId, new ArrayList<>(block)));
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
		String[] infoPokemon = new PokemonRepository().getPokemonId(id);
		Pokemon pokemon = new Pokemon(nid, infoPokemon, attackIds, this.random, this.pokemonLevel());
		this.nextIdPokemon();
		return pokemon;
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
		if (Math.random() < 0.5) {
			turnOrder.add(t1);
			turnOrder.add(t2);
		} else {
			turnOrder.add(t2);
			turnOrder.add(t1);
		}
		return turnOrder;
	}

	// ------------------------------------------------------------------ búsquedas

	/**
	 * Equipo del entrenador indicado.
	 * @throws POOBkemonException si no existe un entrenador con ese ID
	 */
	private Team findTeam(int trainerId) throws POOBkemonException {
		for (Team team : teams) {
			if (team.getTrainer().getId() == trainerId) {
				return team;
			}
		}
		throw new POOBkemonException("Entrenador con ID " + trainerId + " no encontrado");
	}

	/** Equipo rival del entrenador indicado, o {@code null} si no hay. */
	private Team findOpponentTeam(int trainerId) {
		for (Team team : teams) {
			if (team.getTrainer().getId() != trainerId) {
				return team;
			}
		}
		return null;
	}

	/** Pokémon con ese ID en cualquier equipo, o {@code null} si no existe. */
	private Pokemon searchPokemon(int id){
		for (Team team : teams) {
			for (Pokemon pokemon : team.getPokemons()) {
				if (pokemon.getId() == id) {
					return pokemon;
				}
			}
		}
		return null;
	}

	/**
	 * Obtiene la decisión de un entrenador controlado por la máquina.
	 * @param idTrainer id del entrenador
	 * @return decisión, o {@code null} si el entrenador no es una máquina
	 */
	public String[] machineDecision(int idTrainer) throws POOBkemonException {
		Trainer trainer = findTeam(idTrainer).getTrainer();
		return trainer instanceof Machine ? ((Machine) trainer).machineMovement(this) : null;
	}

	/**
	 * Verifica si el entrenador es una máquina.
	 */
	public boolean isMachine(int trainerId){
		for (Team team : teams) {
			if (team.getTrainer().getId() == trainerId) {
				return team.getTrainer() instanceof Machine;
			}
		}
		return false;
	}

	// ------------------------------------------------------------------ turnos

	/**
	 * Procesa la decisión de un entrenador en su turno.
	 * @param decisionTrainer decisión en el formato descrito en {@link Decision}
	 * @throws POOBkemonException Si la decisión es inválida o no puede ejecutarse
	 */
	public void takeDecision(String[] decisionTrainer) throws POOBkemonException {
		if (this.finishBattle) return;
		if (this.counter % TURNS_PER_STATE_TICK == 0) {
			this.applyStates();
		}
		this.counter++;
		this.checkBattleStatus();
		if (this.teams.size() < 2) {
			throw new POOBkemonException("Se necesitan al menos 2 entrenadores para una batalla");
		}
		Decision.validate(decisionTrainer);
		if (this.winner != NO_WINNER) return;

		try {
			switch (decisionTrainer[0]) {
				case Decision.ATTACK:
					this.attack(Integer.parseInt(decisionTrainer[1]),
							Integer.parseInt(decisionTrainer[3]),
							Integer.parseInt(decisionTrainer[2]));
					break;
				case Decision.USE_ITEM:
					this.useItem(Integer.parseInt(decisionTrainer[1]),
							Integer.parseInt(decisionTrainer[2]),
							decisionTrainer[3]);
					break;
				case Decision.CHANGE_POKEMON:
					this.switchPokemon(Integer.parseInt(decisionTrainer[1]),
							Integer.parseInt(decisionTrainer[2]));
					break;
				case Decision.RUN:
					int fleeingTrainer = Integer.parseInt(decisionTrainer[1]);
					this.run(fleeingTrainer);
					moves.add("Player " + fleeingTrainer + " huyó de la batalla");
					break;
				case Decision.TIME_OVER:
					this.timeOver(Integer.parseInt(decisionTrainer[1]), Integer.parseInt(decisionTrainer[2]));
					moves.add("Player se le acabo el tiempo");
					break;
				default:
					throw new POOBkemonException("Acción no reconocida: " + decisionTrainer[0]);
			}
		} catch (NumberFormatException e) {
			throw new POOBkemonException("Formato inválido en parámetros: " + e.getMessage());
		}
		checkBattleStatus();
	}

	/** Cambio de Pokémon decidido por el jugador, con registro en el historial. */
	private void switchPokemon(int trainerId, int pokemonId) throws POOBkemonException {
		this.changePokemon(trainerId, pokemonId);
		moves.add("Player " + trainerId + " cambió a Pokémon " + this.searchPokemon(pokemonId).getName());
	}

	/**
	 * Verifica el estado de la batalla y determina si ha terminado.
	 */
	public void checkBattleStatus() {
		if (this.finishBattle) return;
		for (Team team : teams) {
			if (team.allFainted()) {
				this.finishBattle = true;
				moves.add("¡Batalla terminada! " + team.getTrainer().getId() + " ha sido derrotado");
				this.searchWinner(team);
				break;
			}
		}
	}

	/** Declara ganador al entrenador del equipo contrario al derrotado. */
	private void searchWinner(Team defeated){
		if (this.winner != NO_WINNER) return;
		Team opponent = findOpponentTeam(defeated.getTrainer().getId());
		if (opponent != null) {
			this.setWinner(opponent);
		}
	}

	/**
	 * Cambia el Pokémon activo de un entrenador.
	 * @param trainerId ID del entrenador
	 * @param pokemonId ID del nuevo Pokémon a activar
	 * @throws POOBkemonException Si el entrenador no existe o el Pokémon no es válido
	 */
	public void changePokemon(int trainerId, int pokemonId) throws POOBkemonException {
		if (this.finishBattle) return;
		findTeam(trainerId).changePokemon(pokemonId);
	}

	/** Acción automática cuando se acaba el tiempo del entrenador. */
	private void timeOver(int trainerId, int pokemonId) throws POOBkemonException {
		if (this.finishBattle) return;
		findTeam(trainerId).timeOver(pokemonId);
	}

	/** El entrenador abandona: gana su rival y la batalla termina. */
	private void run(int trainer) throws POOBkemonException {
		if (this.finishBattle) return;
		this.moves.add("GameOver para el jugador " + findTeam(trainer).getTrainer().getId());
		Team opponent = findOpponentTeam(trainer);
		if (opponent != null) {
			this.setWinner(opponent);
		}
		this.finishBattle = true;
	}

	private void setWinner(Team team) {
		this.winner = team.getTrainer().getId();
	}

	/**
	 * @return ID del entrenador ganador
	 * @throws POOBkemonException si todavía no hay ganador
	 */
	public int getWinner() throws POOBkemonException{
		if (this.winner == NO_WINNER) {
			throw new POOBkemonException("No hay ganador aún");
		}
		return this.winner;
	}

	/** Usa un ítem de la mochila del entrenador sobre uno de sus Pokémon. */
	private void useItem(int trainer, int idPokemon, String itemName) throws POOBkemonException {
		if (this.finishBattle) return;
		checkBattleStatus();
		if (this.finishBattle) return;
		Team team = this.teams.stream()
				.filter(t -> t.getTrainer().getId() == trainer)
				.findFirst()
				.orElseThrow(() -> new POOBkemonException("Error: No se encontró el Equipo, para el uso de Item"));
		team.useItem(idPokemon, itemName);
		this.moves.add("El entrenador " + trainer + " ha usado " + itemName
				+ " en " + team.getPokemonById(idPokemon).getName());
	}

	/**
	 * Realiza un ataque contra el Pokémon activo del equipo rival.
	 * @param idAttack ID del ataque a utilizar
	 * @param idTrainer ID del entrenador que ataca
	 * @param idThrower ID del Pokémon que realiza el ataque
	 */
	public void attack(int idAttack, int idTrainer, int idThrower) throws POOBkemonException{
		if (this.finishBattle) return;

		Pokemon attacker = null;
		Attack attackUsed = null;
		for (Team team : teams) {
			if (team.getTrainer().getId() == idTrainer) {
				for (Pokemon pokemon : team.getPokemons()) {
					if (pokemon.getId() == idThrower) {
						attacker = pokemon;
						attackUsed = pokemon.getAttack(idAttack);
						break;
					}
				}
				break;
			}
		}

		Pokemon target = null;
		Team opponentTeam = findOpponentTeam(idTrainer);
		if (opponentTeam != null) {
			for (Pokemon pokemon : opponentTeam.getPokemons()) {
				if (pokemon.getActive()) {
					target = pokemon;
					break;
				}
			}
		}

		if (attacker == null) {
			throw new POOBkemonException("Error: No se encontró el Pokemon atacante");
		}
		if (attackUsed == null) {
			throw new POOBkemonException("Error: No se encontró el ataque");
		}
		if (target == null) {
			throw new POOBkemonException("Error: No se encontró un Pokémon objetivo activo");
		}

		attackUsed.usePP();
		String effect = target.getDamage(attackUsed, attacker);
		// Si el atacante ya estaba debilitado el efecto viene vacío y no se registra nada.
		if (!effect.isEmpty()) {
			this.moves.add(attacker.getName() + " atacó a " + target.getName() + effect);
		}
		this.autoChangePokemon();
	}

	/**
	 * Reemplaza automáticamente al Pokémon activo debilitado por uno que siga en pie.
	 */
	private void autoChangePokemon() throws POOBkemonException{
		if (this.finishBattle) return;
		for (Team team : teams) {
			for (Pokemon pokemon : team.getPokemons()) {
				if (pokemon.getWeak() && pokemon.getActive()) {
					this.checkBattleStatus();
					if (this.finishBattle) return;
					int replacement = this.getAlivePokemon(team.getTrainer().getId());
					this.changePokemon(team.getTrainer().getId(), replacement);
					this.moves.add(pokemon.getName() + " ha sido debilitado, cambiando a "
							+ searchPokemon(replacement).getName());
					return;
				}
			}
		}
	}

	/**
	 * ID de un Pokémon del entrenador que aún no esté debilitado.
	 * @return el ID, o -1 si la batalla terminó o no queda ninguno
	 */
	private int getAlivePokemon(int trainerId){
		if (this.finishBattle) return -1;
		int id = -1;
		for (Team team : teams) {
			if (team.getTrainer().getId() == trainerId) {
				for (Pokemon p : team.getPokemons()) {
					if (!p.getWeak()) {
						id = p.getId();
					}
				}
			}
		}
		return id;
	}

	/** Aplica a todos los Pokémon los efectos de sus estados alterados. */
	private void applyStates(){
		for (Team team : teams) {
			team.applyEffect();
		}
	}

	// ------------------------------------------------------------------ consultas

	public ArrayList<Team> getTeams(){
		return teams;
	}
	public ArrayList<Team> teams(){
		return this.teams;
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
	public ArrayList<Integer> getOrder(){
		return order;
	}

	/**
	 * Obtiene los Pokémon actualmente activos de cada entrenador.
	 * @return Mapa con ID de entrenador como clave y array de información del Pokémon activo como valor
	 */
	public HashMap<Integer, String[]> getCurrentPokemons() {
		if (this.teams == null) throw new NullPointerException("No hay equipos");
		HashMap<Integer, String[]> pokemons = new HashMap<>();
		for (Team t : this.teams) {
			Trainer trainer = t.getTrainer();
			try {
				pokemons.put(trainer.getId(), t.getPokemonById(trainer.getCurrentPokemonId()).getInfo());
			} catch (POOBkemonException e) {
				pokemons.put(trainer.getId(), new String[]{"No active Pokémon"});
			}
		}
		return pokemons;
	}

	/**
	 * IDs de los Pokémon de un entrenador que no están en combate.
	 * @return los IDs; {@code null} si la batalla terminó
	 */
	public int[] getPokemonsInactive(int idTrainer) {
		if (this.finishBattle) return null;
		return pokemonIds(idTrainer, false);
	}

	/**
	 * IDs de todos los Pokémon de un entrenador.
	 * @return los IDs; {@code null} si la batalla terminó
	 */
	public int[] getPokemonsPerTrainer(int idTrainer) {
		if (this.finishBattle) return null;
		return pokemonIds(idTrainer, true);
	}

	private int[] pokemonIds(int idTrainer, boolean includeActive) {
		for (Team team : teams) {
			if (team.getTrainer().getId() == idTrainer) {
				return team.getPokemons().stream()
						.filter(p -> includeActive || !p.getActive())
						.mapToInt(Pokemon::getId)
						.toArray();
			}
		}
		return new int[0];
	}

	/**
	 * Información de un Pokémon específico de un entrenador.
	 * @return Array de Strings con la información del Pokémon; {@code null} si la batalla terminó
	 * @throws POOBkemonException Si no se encuentra el entrenador o el Pokémon
	 */
	public String[] getPokemonInfo(int idTrainer, int idPokemon) throws POOBkemonException {
		if (this.finishBattle) return null;
		return findTeam(idTrainer).getPokemonById(idPokemon).getInfo();
	}

	/**
	 * Ataques del Pokémon activo de cada entrenador.
	 * @return Mapa entrenador → información de ataques; {@code null} si la batalla terminó
	 */
	public HashMap<Integer, String[][]> getActiveAttacks(){
		if (this.finishBattle) return null;
		HashMap<Integer, String[][]> attacks = new HashMap<>();
		for (Team t : this.teams) {
			Trainer trainer = t.getTrainer();
			try {
				attacks.put(trainer.getId(), t.getPokemonById(trainer.getCurrentPokemonId()).getAttacksInfo());
			} catch (POOBkemonException e) {
				attacks.put(trainer.getId(), new String[][]{{"Sin ataques activos"}});
			}
		}
		return attacks;
	}

	// ------------------------------------------------------------------ catálogos

	/** Cantidad de Pokémon del catálogo (sus IDs van de 1 a este valor). */
	public static int pokemonCatalogSize() {
		return new PokemonRepository().getPokemons().size();
	}

	/** Cantidad de movimientos del catálogo (sus IDs van de 1 a este valor). */
	public static int moveCatalogSize() {
		return new MovesRepository().getMoves().size();
	}

	/** Información de todos los Pokémon disponibles. */
	public ArrayList<String[]> getPokInfo(){
		return new PokemonRepository().getPokemons();
	}

	/** Información de todos los ítems disponibles; {@code null} si la batalla terminó. */
	public ArrayList<ArrayList<String>> getItemInfo(){
		if (this.finishBattle) return null;
		return new ItemRepository().getItems();
	}

	/** Descripción corta de un ataque. */
	public String getMoveInfo(int id){
		return new MovesRepository().getAttackId(id);
	}

	/**
	 * Ítems de la mochila de un entrenador.
	 * @throws POOBkemonException si el entrenador no existe
	 */
	public String[][] getInfoItems(int trainerId) throws POOBkemonException {
		return findTeam(trainerId).getTrainer().getBagPack().getItems();
	}

	/** Todos los ataques compatibles con un Pokémon. */
	public ArrayList<String[]> getCompatibleAttacks(int pokemonId) {
		return new MovesRepository().getCompatibleAttacks(pokemonId);
	}

	/** Descripción corta de un ataque. */
	public String getAttackId(int id) {
		return new MovesRepository().getAttackId(id);
	}

	/** Fila del catálogo correspondiente a un Pokémon de la Pokédex. */
	public String[] getPokemonInfoById(int id){
		return new PokemonRepository().getPokemonId(id);
	}

	// ------------------------------------------------------------------ persistencia

	/**
	 * Guarda la batalla en un archivo.
	 * @throws POOBkemonException si no se puede escribir el archivo
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
	 * Carga una batalla desde un archivo.
	 * @throws POOBkemonException si el archivo no existe o no contiene una batalla válida
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

	public void addMove(String move){
		this.moves.add(move);
	}

	public void setOk(boolean ok){
		this.ok = ok;
	}
}
