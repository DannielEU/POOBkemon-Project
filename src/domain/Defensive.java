package domain;

import persistence.StatsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Defensive extends Machine {
    // Defense strategy parameters
    private static final double HEALTH_DEFENSE_THRESHOLD = 0.5;
    private static final double TYPE_DISADVANTAGE_THRESHOLD = 0.5;
    private static final double PROTECT_PROBABILITY = 0.3;
    private static final double STATUS_ATTACK_PREFERENCE = 0.7;

    // Weights for decision scoring
    private static final double DEFENSIVE_SCORE_WEIGHT = 0.5;
    private static final double HEALTH_SCORE_WEIGHT = 0.3;
    private static final double STATUS_SCORE_WEIGHT = 0.4;
    private static final double RANDOM_FACTOR_WEIGHT = 0.1;

    private final StatsRepository typeChart = new StatsRepository();
    private final Random random = new Random();

    public Defensive(int id, BagPack bagPack) throws POOBkemonException {
        super(id, bagPack);
    }

    @Override
    public String[] machineMovement(POOBkemon game) throws POOBkemonException {
        Team myTeam = getMyTeam(game);
        Pokemon myActive = getActivePokemon(myTeam);
        Pokemon opponent = getOpponentActivePokemon(game);

        // Check if we should use a defensive item
        String[] itemDecision = considerUsingItem(myTeam, myActive);
        if (itemDecision != null) {
            return itemDecision;
        }

        // Consider switching if at a disadvantage
        if (shouldSwitchPokemon(myActive, opponent)) {
            String[] switchDecision = createSwitchDecision(myTeam, myActive, opponent);
            if (switchDecision != null) {
                return switchDecision;
            }
        }

        // Consider using Protect if health is low
        if (shouldUseProtect(myActive)) {
            return new String[] {"Attack", findProtectMove(myActive),
                    String.valueOf(myActive.getId()),
                    String.valueOf(this.getId())};
        }

        // Default to selecting the best defensive attack
        return selectDefensiveAttack(myActive, opponent);
    }

    private boolean shouldSwitchPokemon(Pokemon myActive, Pokemon opponent) {
        double typeEffectiveness = calculateTypeEffectiveness(myActive, opponent);
        double healthRatio = (double) myActive.currentHealth / myActive.maxHealth;

        // Switch if type disadvantage and health is below threshold
        if (typeEffectiveness < TYPE_DISADVANTAGE_THRESHOLD &&
                healthRatio < HEALTH_DEFENSE_THRESHOLD) {
            return true;
        }

        // Switch if completely ineffective against opponent
        return typeEffectiveness == 0;
    }

    private String[] considerUsingItem(Team team, Pokemon active) throws POOBkemonException {
        // Check if health is below threshold for healing
        double healthRatio = (double) active.currentHealth / active.maxHealth;
        if (healthRatio < HEALTH_DEFENSE_THRESHOLD) {
            String[][] items = this.getBagPack().getItems();

            // Prefer Potions over Revives
            for (String[] item : items) {
                if (item[0].equalsIgnoreCase("Potion") && Integer.parseInt(item[1]) > 0) {
                    return new String[] {
                            "UseItem",
                            String.valueOf(this.getId()),
                            String.valueOf(active.getId()),
                            "Potion"
                    };
                }
            }

            // Consider Revive if no Potions available and Pokemon is fainted
            if (active.getWeak()) {
                for (String[] item : items) {
                    if (item[0].equalsIgnoreCase("Revive") && Integer.parseInt(item[1]) > 0) {
                        return new String[] {
                                "UseItem",
                                String.valueOf(this.getId()),
                                String.valueOf(active.getId()),
                                "Revive"
                        };
                    }
                }
            }
        }
        return null;
    }

    private boolean shouldUseProtect(Pokemon active) {
        double healthRatio = (double) active.currentHealth / active.maxHealth;
        return healthRatio < HEALTH_DEFENSE_THRESHOLD &&
                random.nextDouble() < PROTECT_PROBABILITY &&
                hasProtectMove(active);
    }

    private boolean hasProtectMove(Pokemon pokemon) {
        for (Attack attack : pokemon.getAttacks()) {
            if (attack.getName().equalsIgnoreCase("Protect")) {
                return true;
            }
        }
        return false;
    }

    private String findProtectMove(Pokemon pokemon) {
        for (Attack attack : pokemon.getAttacks()) {
            if (attack.getName().equalsIgnoreCase("Protect") && attack.getPPActual() > 0) {
                return String.valueOf(attack.getIdInside());
            }
        }
        return "1"; // Fallback to first attack if Protect not found (shouldn't happen if shouldUseProtect passed)
    }

    private String[] selectDefensiveAttack(Pokemon attacker, Pokemon opponent) throws POOBkemonException {
        List<Attack> availableAttacks = getAvailableAttacks(attacker);

        if (availableAttacks.isEmpty()) {
            throw new POOBkemonException("No hay ataques disponibles para " + attacker.getName());
        }

        Attack bestAttack = selectBestDefensiveAttack(availableAttacks, attacker, opponent);
        return new String[] {
                "Attack",
                String.valueOf(bestAttack.getIdInside()),
                String.valueOf(attacker.getId()),
                String.valueOf(this.getId())
        };
    }

    private Attack selectBestDefensiveAttack(List<Attack> attacks, Pokemon attacker, Pokemon opponent) {
        Attack bestAttack = attacks.get(0);
        double bestScore = evaluateDefensiveAttack(bestAttack, attacker, opponent);

        for (Attack attack : attacks) {
            double currentScore = evaluateDefensiveAttack(attack, attacker, opponent);
            if (currentScore > bestScore) {
                bestAttack = attack;
                bestScore = currentScore;
            }
        }

        return bestAttack;
    }

    private double evaluateDefensiveAttack(Attack attack, Pokemon attacker, Pokemon opponent) {
        double score = 0;

        // Prefer status moves that hinder the opponent
        if (attack instanceof StateAttack) {
            score += STATUS_ATTACK_PREFERENCE;

            // Extra points for defensive status effects
            StateAttack stateAttack = (StateAttack) attack;
            if (stateAttack.getState().toUpperCase().contains("DOWN") ||
                    stateAttack.getState().toUpperCase().contains("REDUCE")) {
                score += 0.3;
            }
        }

        // Consider type effectiveness (but less important than for offensive)
        try {
            double typeEffectiveness = typeChart.getMultiplier(attack.getType(), opponent.type);
            score += typeEffectiveness * 0.2;
        } catch (Exception e) {
            // Default to neutral effectiveness if error occurs
            score += 1.0 * 0.2;
        }

        // Slight preference for higher accuracy moves
        score += (attack.getAccuracy() / 100.0) * 0.1;

        // Small random factor to add variability
        score += random.nextDouble() * RANDOM_FACTOR_WEIGHT;

        return score;
    }

    private List<Attack> getAvailableAttacks(Pokemon pokemon) {
        List<Attack> available = new ArrayList<>();
        for (Attack attack : pokemon.getAttacks()) {
            if (attack.getPPActual() > 0) {
                available.add(attack);
            }
        }
        return available;
    }

    private double calculateTypeEffectiveness(Pokemon attacker, Pokemon defender) {
        try {
            return typeChart.getMultiplier(attacker.getType(), defender.type);
        } catch (Exception e) {
            return 1.0; // Neutral effectiveness if error occurs
        }
    }

    // Helper methods from Switcher implementation
    private Team getMyTeam(POOBkemon game) throws POOBkemonException {
        for (Team team : game.teams()) {
            if (team.getTrainer().getId() == this.getId()) {
                return team;
            }
        }
        throw new POOBkemonException("Equipo no encontrado para el entrenador: " + this.getId());
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
        throw new POOBkemonException("No se encontró Pokémon oponente activo");
    }

    private String[] createSwitchDecision(Team myTeam, Pokemon current, Pokemon opponent) throws POOBkemonException {
        List<Pokemon> candidates = getSwitchCandidates(myTeam, current);

        if (candidates.isEmpty()) {
            return null;
        }

        Pokemon bestChoice = selectBestDefensiveSwitch(candidates, opponent);
        return new String[] {
                "ChangePokemon",
                String.valueOf(this.getId()),
                String.valueOf(bestChoice.getId())
        };
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

    private Pokemon selectBestDefensiveSwitch(List<Pokemon> candidates, Pokemon opponent) {
        Pokemon best = candidates.get(0);
        double bestScore = evaluateDefensiveSwitch(best, opponent);

        for (Pokemon candidate : candidates) {
            double currentScore = evaluateDefensiveSwitch(candidate, opponent);
            if (currentScore > bestScore) {
                best = candidate;
                bestScore = currentScore;
            }
        }

        return best;
    }

    private double evaluateDefensiveSwitch(Pokemon candidate, Pokemon opponent) {
        double score = 0;

        // Type advantage is important but not as much as for offensive
        double typeEffectiveness = calculateTypeEffectiveness(candidate, opponent);
        score += (typeEffectiveness >= 1.0 ? 1.0 : typeEffectiveness) * 0.4;

        // Higher weight to defensive stats
        double defensiveStat = (candidate.defense + candidate.specialDefense) / 2.0;
        score += (defensiveStat / 200.0) * 0.5; // Normalized

        // Prefer healthier Pokémon
        double healthRatio = (double) candidate.currentHealth / candidate.maxHealth;
        score += healthRatio * 0.3;

        // Small random factor
        score += random.nextDouble() * 0.1;

        return score;
    }
}