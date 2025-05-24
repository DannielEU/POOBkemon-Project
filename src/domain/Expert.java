package domain;

import persistence.StatsRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

	private String[] createItemDecision(String itemName, Pokemon target) {
		return new String[] {
				"UseItem",
				String.valueOf(this.getId()),
				String.valueOf(target.getId()),
				itemName
		};
	}

	private boolean shouldSwitch(Pokemon myActive, Pokemon opponent) {
		double effectiveness = calculateEffectiveness(myActive, opponent);
		double healthRatio = (double) myActive.currentHealth / myActive.maxHealth;

		if (effectiveness == 0) return true; // Cambio obligatorio

		if (effectiveness < TYPE_DISADVANTAGE && healthRatio < LOW_HEALTH) {
			return true;
		}

		return random.nextDouble() < adjustedSwitchProbability(effectiveness, healthRatio);
	}

	// Resto de los métodos permanecen igual...
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

	// Métodos auxiliares
	private List<Attack> getAvailableAttacks(Pokemon pokemon) {
		List<Attack> available = new ArrayList<>();
		for (Attack attack : pokemon.getAttacks()) {
			if (attack.getPPActual() > 0) {
				available.add(attack);
			}
		}
		return available;
	}

	private double calculateEffectiveness(Pokemon attacker, Pokemon defender) {
		try {
			return typeChart.getMultiplier(attacker.getType(), defender.type);
		} catch (Exception e) {
			return 1.0;
		}
	}

	private Team getMyTeam(POOBkemon game) throws POOBkemonException {
		for (Team team : game.teams()) {
			if (team.getTrainer().getId() == this.getId()) {
				return team;
			}
		}
		throw new POOBkemonException("Team not found for trainer: " + this.getId());
	}

	private Pokemon getActivePokemon(Team team) throws POOBkemonException {
		return team.getPokemonById(team.getTrainer().getCurrentPokemonId());
	}

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