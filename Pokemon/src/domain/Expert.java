package domain;

import persistence.StatsRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Expert class represents an advanced AI entity, extending the behavior of a Machine.
 * It is designed to make strategic decisions during a POOBkemon game, such as selecting moves,
 * switching Pokemon, or utilizing items, based on various aspects of the game state.
 */
public class Expert extends Machine {
	// Umbrales estratégicos
	private static final double CRITICAL_HEALTH = 0.25;
	private static final double LOW_HEALTH = 0.5;
	private static final double TYPE_DISADVANTAGE = 0.5;

	// Probabilidades base
	private static final double BASE_SWITCH_PROB = 0.35;
	private static final double ITEM_USE_PROB = 0.8;

	// Pesos para decisiones
	private static final double OFFENSIVE_WEIGHT = 0.45;
	private static final double DEFENSIVE_WEIGHT = 0.35;
	private static final double STATUS_WEIGHT = 0.2;

	private final StatsRepository typeChart = new StatsRepository();
	private final Random random = new Random();

	public Expert(int id, BagPack bagPack) throws POOBkemonException {
		super(id, bagPack);
	}

	/**
	 * Decides the next action for the automated machine in a POOBkemon game.
	 * This includes evaluating item use, making strategic switches, or selecting the optimal move.
	 *
	 * @param game the current instance of the POOBkemon game containing game state information,
	 *             including teams, active Pokémon, and available moves or items.
	 * @return an array of strings representing the decision for the machine's next action.
	 *         The array format depends on the type of action, such as using an item, switching Pokémon,
	 *         or performing an attack.
	 * @throws POOBkemonException if any game-specific rule*/
	@Override
	public String[] machineMovement(POOBkemon game) throws POOBkemonException {
		Team myTeam = getMyTeam(game);
		Pokemon myActive = getActivePokemon(myTeam);
		Pokemon opponent = getOpponentActivePokemon(game);

		// 1. Prioridad: Uso de items si es crítico
		String[] itemDecision = evaluateItemUsage(myTeam, myActive);
		if (itemDecision != null) {
			return itemDecision;
		}

		// 2. Evaluar cambio estratégico
		if (shouldSwitch(myActive, opponent)) {
			String[] switchDecision = createOptimalSwitch(myTeam, myActive, opponent);
			if (switchDecision != null) {
				return switchDecision;
			}
		}

		// 3. Seleccionar mejor movimiento ofensivo/defensivo
		return selectOptimalMove(myActive, opponent);
	}

	/**
	 * Evaluates the usage of items for the active Pokémon in the given team. The method determines
	 * if an item should be used based on the Pokémon's current health, the type of available items,
	 * and certain predefined conditions and probabilities.
	 *
	 * @param team the team containing the active Pokémon and the bag of items
	 * @param active the currently active Pokémon whose health or status may determine item usage
	 * @return an array of strings representing the action to use a specific item. The structure of
	 *         the array includes the type of action, machine ID, Pokémon ID, and item name.
	 *         Returns null if no item usage is deemed necessary.
	 * @throws POOBkemonException if there is an issue with accessing the team's items or Pokémon data
	 */
	private String[] evaluateItemUsage(Team team, Pokemon active) throws POOBkemonException {
		String[][] items = this.getBagPack().getItems();
		double healthRatio = (double) active.currentHealth / active.maxHealth;

		// 1. Revivir si está debilitado (máxima prioridad)
		if (active.getWeak()) {
			for (String[] item : items) {
				if (item[0].equals("Revive") && Integer.parseInt(item[1]) > 0) {
					return new String[] {
							"UseItem",
							String.valueOf(this.getId()),
							String.valueOf(active.getId()),
							"Revive"
					};
				}
			}
		}

		// 2. Usar la poción más adecuada según salud faltante
		int missingHealth = active.maxHealth - active.currentHealth;

		// MegaPotion para salud crítica (cura 100+)
		if (healthRatio < CRITICAL_HEALTH && missingHealth > 100) {
			for (String[] item : items) {
				if (item[0].equals("Mega") && Integer.parseInt(item[1]) > 0) {
					return createItemDecision("Mega", active);
				}
			}
		}

		// HyperPotion para salud media-baja (cura 50-100)
		if (healthRatio < LOW_HEALTH && missingHealth > 50) {
			for (String[] item : items) {
				if (item[0].equals("hyperPotion") && Integer.parseInt(item[1]) > 0) {
					return createItemDecision("hyper", active);
				}
			}
		}

		// SuperPotion para salud moderada (cura 25-50)
		if (healthRatio < LOW_HEALTH && missingHealth > 25) {
			for (String[] item : items) {
				if (item[0].equals("superPotion") && Integer.parseInt(item[1]) > 0) {
					return createItemDecision("super", active);
				}
			}
		}

		// Potion básica para pequeñas curaciones (1-25)
		if (healthRatio < LOW_HEALTH && random.nextDouble() < ITEM_USE_PROB) {
			for (String[] item : items) {
				if (item[0].equals("potion") && Integer.parseInt(item[1]) > 0) {
					return createItemDecision("potion", active);
				}
			}
		}

		return null;
	}

	/**
	 * Creates a decision to use an item on a target Pokémon. The returned decision
	 * array contains information regarding the type of action, the ID of the
	 * machine making the decision, the target Pokémon's ID, and the name of the
	 * item to be used.
	 *
	 * @param itemName the name of the item to be used
	 * @param target the target Pokémon on which the item will be used
	 * @return an array of strings representing the decision to use an item. The
	 *         structure of the array includes:
	 *         - "UseItem" (string literal representing the action)
	 *         - machine ID (string representation of the machine's ID)
	 *         - target Pokémon ID (string representation of the Pokémon's ID)
	 *         - item name
	 */
	private String[] createItemDecision(String itemName, Pokemon target) {
		return new String[] {
				"UseItem",
				String.valueOf(this.getId()),
				String.valueOf(target.getId()),
				itemName
		};
	}

	/**
	 * Determines whether the active Pokémon should be switched out during a battle.
	 * The decision is based on the effectiveness of its moves against the opponent,
	 * its current health ratio, and a probability adjustment influenced by these factors.
	 *
	 * @param myActive the current active Pokémon belonging to the machine.
	 * @param opponent the opponent's active Pokémon.
	 * @return {@code true} if the active Pokémon should be switched out; {@code false} otherwise.
	 */
	private boolean shouldSwitch(Pokemon myActive, Pokemon opponent) {
		double effectiveness = calculateEffectiveness(myActive, opponent);
		double healthRatio = (double) myActive.currentHealth / myActive.maxHealth;

		if (effectiveness == 0) return true; // Cambio obligatorio

		if (effectiveness < TYPE_DISADVANTAGE && healthRatio < LOW_HEALTH) {
			return true;
		}

		return random.nextDouble() < adjustedSwitchProbability(effectiveness, healthRatio);
	}

	/**
	 * Adjusts the probability for switching out the active Pokémon during a battle.
	 * The adjustment is based on the effectiveness of the moves against the opponent
	 * and the active Pokémon's current health ratio.
	 *
	 * @param effectiveness the effectiveness of the active Pokémon's moves against the opponent,
	 *                      where a value less than 1.0 signifies a disadvantage.
	 * @param healthRatio   the current health of the active Pokémon normalized as a value between 0.0 and 1.0,
	 *                      with lower values indicating poorer health.
	 * @return the adjusted switch probability as a double, with a maximum threshold of 0.8.
	 */
	private double adjustedSwitchProbability(double effectiveness, double healthRatio) {
		double prob = BASE_SWITCH_PROB;

		if (effectiveness < 1.0) {
			prob += (1.0 - effectiveness) * 0.3;
		}

		if (healthRatio < LOW_HEALTH) {
			prob += (1.0 - healthRatio) * 0.2;
		}

		return Math.min(0.8, prob);
	}

	/**
	 * Creates an optimal switch decision for the active Pokémon in a battle. This method
	 * determines which Pokémon from the team should be switched in based on strategic
	 * evaluations of the current situation and the opponent's active Pokémon.
	 *
	 * @param team the team of the machine, containing all available Pokémon that can be switched in
	 * @param current the currently active Pokémon belonging to the machine
	 * @param opponent the opponent's currently active Pokémon
	 * @return an array of strings representing the decision to switch Pokémon. The array structure includes:
	 *         - "ChangePokemon" (string literal representing the action),
	 *         - ID of the machine making the decision (as a string),
	 *         - ID of the chosen Pokémon to switch in (as a string).
	 *         Returns null if no optimal switch candidate is available.
	 * @throws POOBkemonException if there is an issue determining switch candidates or accessing team data
	 */
	private String[] createOptimalSwitch(Team team, Pokemon current, Pokemon opponent) throws POOBkemonException {
		List<Pokemon> candidates = getSwitchCandidates(team, current);
		if (candidates.isEmpty()) return null;

		Pokemon bestChoice = selectBestSwitch(candidates, opponent);
		return new String[] {
				"ChangePokemon",
				String.valueOf(this.getId()),
				String.valueOf(bestChoice.getId())
		};
	}

	/**
	 * Selects the best Pokémon to switch to from a given list of candidates, based on
	 * evaluations against the current opponent's Pokémon.
	 *
	 * @param candidates a list of Pokémon available for switching in.
	 * @param opponent the opponent's currently active Pokémon used for evaluation.
	 */
	private Pokemon selectBestSwitch(List<Pokemon> candidates, Pokemon opponent) {
		Pokemon best = candidates.get(0);
		double bestScore = evaluateSwitchCandidate(best, opponent);

		for (Pokemon candidate : candidates) {
			double score = evaluateSwitchCandidate(candidate, opponent);
			if (score > bestScore) {
				best = candidate;
				bestScore = score;
			}
		}

		return best;
	}

	/**
	 * Evaluates the suitability of a Pokémon candidate for switching into battle
	 * against a specified opponent. The score is calculated based on multiple factors,
	 * including type effectiveness, defense,*/
	private double evaluateSwitchCandidate(Pokemon candidate, Pokemon opponent) {
		double score = 0;

		double effectiveness = calculateEffectiveness(candidate, opponent);
		score += effectiveness * 0.4;

		double defenseScore = (candidate.defense + candidate.specialDefense) / 200.0;
		score += defenseScore * 0.3;

		double healthScore = (double) candidate.currentHealth / candidate.maxHealth;
		score += healthScore * 0.2;

		score += random.nextDouble() * 0.1;

		return score;
	}

	/**
	 *
	 */
	private String[] selectOptimalMove(Pokemon attacker, Pokemon opponent) throws POOBkemonException {
		List<Attack> attacks = getAvailableAttacks(attacker);
		if (attacks.isEmpty()) {
			throw new POOBkemonException("No attacks available for " + attacker.getName());
		}

		Attack bestAttack = selectBestAttack(attacks, attacker, opponent);
		return new String[] {
				"Attack",
				String.valueOf(bestAttack.getIdInside()),
				String.valueOf(attacker.getId()),
				String.valueOf(this.getId())
		};
	}

	/**
	 * Selects the best attack from a list of available attacks based on their effectiveness and strategic value
	 * against the opponent's Pokémon.
	 *
	 * @param attacks a list of available attacks to evaluate and choose from.
	 * @param attacker the Pokémon that will use the attack.
	 * @param opponent the opponent's Pokémon against which the attack will be evaluated.
	 * @return the most effective and strategically optimal attack from the list of available attacks.
	 */
	private Attack selectBestAttack(List<Attack> attacks, Pokemon attacker, Pokemon opponent) {
		Attack best = attacks.get(0);
		double bestScore = evaluateAttack(best, attacker, opponent);

		for (Attack attack : attacks) {
			double score = evaluateAttack(attack, attacker, opponent);
			if (score > bestScore) {
				best = attack;
				bestScore = score;
			}
		}

		return best;
	}

	/**
	 * Evaluates the overall effectiveness and strategic value of a given attack
	 * when used by an*/
	private double evaluateAttack(Attack attack, Pokemon attacker, Pokemon opponent) {
		double score = 0;

		try {
			double effectiveness = typeChart.getMultiplier(attack.getType(), opponent.type);
			score += effectiveness * 0.3;

			double powerScore = attack.getPower() / 150.0;
			score += powerScore * 0.25;

			if (attack instanceof StateAttack) {
				StateAttack stateAttack = (StateAttack) attack;
				score += evaluateStatusEffect(stateAttack) * 0.25;
			}

			double accuracyScore = attack.getAccuracy() / 100.0;
			score += accuracyScore * 0.15;

			double ppScore = (double) attack.getPPActual() / attack.getPPMax();
			score += ppScore * 0.05;

		} catch (Exception e) {
			score = random.nextDouble();
		}

		return score;
	}

	/**
	 * Evaluates the overall impact of a status effect inflicted by a StateAttack instance.
	 * The evaluation returns a weight depending on the nature and severity of the status condition.
	 *
	 * @param attack the StateAttack instance containing information about the attack's status condition
	 * @return a double value representing the evaluated weight of the status effect, where higher values
	 *         indicate more severe or impactful conditions
	 */
	private double evaluateStatusEffect(StateAttack attack) {
		String status = attack.getState().toUpperCase();

		if (status.contains("SLEEP") || status.contains("FREEZE")) {
			return 1.0;
		}
		if (status.contains("PARALYZE") || status.contains("CONFUSE")) {
			return 0.8;
		}
		if (status.contains("ATTACK_DOWN") || status.contains("SPEED_DOWN")) {
			return 0.7;
		}
		if (status.contains("DEFENSE_DOWN")) {
			return 0.6;
		}

		return 0.3;
	}

	/**
	 * Retrieves a list of all available attacks for the specified Pokémon
	 * that have remaining Power Points (PP).
	 *
	 * @param pokemon the Pokémon whose attacks are being evaluated
	 * @return a list of available attacks where each attack has at least one PP remaining
	 */
	private List<Attack> getAvailableAttacks(Pokemon pokemon) {
		List<Attack> available = new ArrayList<>();
		for (Attack attack : pokemon.getAttacks()) {
			if (attack.getPPActual() > 0) {
				available.add(attack);
			}
		}
		return available;
	}

	/**
	 * Calculates the effectiveness multiplier of an attack based on the types of the attacking
	 * and defending Pokémon. The method refers to a type chart to determine the relationship
	 * between the types and returns a multiplier value.
	 *
	 * @param attacker the Pokémon that is initiating the attack, whose type will be used
	 *                 to determine the effectiveness
	 * @param defender the Pokémon that is being attacked, whose type will be used to
	 *                 determine the resistance or weakness
	 * @return a double representing the effectiveness multiplier. Values greater than 1.0 indicate
	 *         increased effectiveness, values less than 1.0 indicate reduced effectiveness,
	 *         and 1.0 denotes neutral effectiveness. If an error occurs, a default value of 1.0 is returned.
	 */
	private double calculateEffectiveness(Pokemon attacker, Pokemon defender) {
		try {
			return typeChart.getMultiplier(attacker.getType(), defender.type);
		} catch (Exception e) {
			return 1.0;
		}
	}

	/**
	 * Retrieves the team associated with the current trainer in the given game instance.
	 * The team is identified by matching the trainer's ID with the ID of the current trainer.
	 *
	 * @param game the current instance of the POOBkemon game*/
	private Team getMyTeam(POOBkemon game) throws POOBkemonException {
		for (Team team : game.teams()) {
			if (team.getTrainer().getId() == this.getId()) {
				return team;
			}
		}
		throw new POOBkemonException("Team not found for trainer: " + this.getId());
	}

	/**
	 * Retrieves the currently active Pokémon for the given team.
	 * The method determines the active Pokémon by accessing the trainer's current Pokémon ID.
	 *
	 * @param team the team containing the Pokémon and trainer whose active Pokémon should be retrieved
	 * @return the active Pokémon for the specified team
	 * @throws POOBkemonException if the active Pokémon cannot be retrieved due to an error in the team or trainer data
	 */
	private Pokemon getActivePokemon(Team team) throws POOBkemonException {
		return team.getPokemonById(team.getTrainer().getCurrentPokemonId());
	}

	/**
	 * Retrieves the opponent's active Pokémon from the game.
	 *
	 * @param game the current POOBkemon game instance to search for the opponent's active Pokémon.
	 * @return the opponent's active Pokémon.
	 * @throws POOBkemonException if no active opponent Pokémon is*/
	private Pokemon getOpponentActivePokemon(POOBkemon game) throws POOBkemonException {
		for (Team team : game.teams()) {
			if (team.getTrainer().getId() != this.getId()) {
				for (Pokemon pokemon : team.getPokemons()) {
					if (pokemon.getActive()) {
						return pokemon;
					}
				}
			}
		}
		throw new POOBkemonException("No active opponent Pokémon found");
	}

	/**
	 * Identifies and returns a list of candidate Pokémon for switching out the current Pokémon
	 * based on specific criteria such as not being weak and not being the current Pokémon.
	 *
	 * @param team The team of Pokémon to evaluate for switch candidates.
	 * @param current The currently active Pokémon that will not be considered as a candidate.
	 * @return A list of Pokémon from the team that are eligible for switching, excluding weak Pokémon and the current Pokémon.
	 */
	private List<Pokemon> getSwitchCandidates(Team team, Pokemon current) {
		List<Pokemon> candidates = new ArrayList<>();
		for (Pokemon pokemon : team.getPokemons()) {
			if (!pokemon.getWeak() && pokemon.getId() != current.getId()) {
				candidates.add(pokemon);
			}
		}
		return candidates;
	}
}