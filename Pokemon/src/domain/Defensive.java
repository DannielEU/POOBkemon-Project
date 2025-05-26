package domain;

import persistence.StatsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Defensive class represents a Machine implementation focusing on a defensive strategy
 * in the POOBkemon game. It prioritizes preserving its team and minimizing damage taken by
 * strategically switching Pokémon, using protective moves, or leveraging game items during battle.
 *
 * This class extends the abstract Machine class and overrides the machineMovement method
 * to provide decision-making logic tailored to defensive gameplay.
 */
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

    /**
     * Constructs a new Defensive object with the specified identifier and BagPack.
     * This constructor initializes the defensive strategy using the given data and enforces
     * validation to ensure correct initialization of its parent class, Machine.
     *
     * @param id the unique identifier of the Defensive object
     * @param bagPack the BagPack object associated with the Defensive, containing items
     * @throws POOBkemonException if the BagPack is null or there are issues in the parent initialization
     */
    public Defensive(int id, BagPack bagPack) throws POOBkemonException {
        super(id, bagPack);
    }

    /**
     * Executes the decision-making process for a machine-controlled team in the POOBkemon game.
     * This method determines the appropriate action to take based on defensive strategies,
     * including using items, switching Pokémon, or selecting moves such as "Protect" or other defensive attacks.
     *
     * @param game the current instance of the POOBkemon game, which includes all relevant game state
     *             and information needed for decision-making.
     * @return an array of strings representing the chosen action. The format and meaning of the array
     *         elements depend on the action type (e.g., item usage, Pokémon switch, or defensive attack).
     * @throws POOBkemonException if an error occurs during the decision-making process, such as
     *                            invalid game state or problems accessing required information.
     */
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

    /**
     * Determines whether the active Pokémon should be switched out during a battle.
     * The decision is based on type effectiveness and the health ratio of the active Pokémon.
     *
     * @param myActive the currently active Pokémon on the player's team
     * @param opponent the opponent's active Pokémon
     * @return true if the active Pokémon should be switched out based on the defensive strategy, false otherwise
     */
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

    /**
     * Evaluates the defensive strategy and decides whether to use an item on the active Pokémon.
     * Based on the Pokémon's current health and status, it may choose to use healing or revival items.
     *
     * @param team the team to which the active Pokémon belongs, providing context for item usage
     * @param active the currently active Pokémon that may benefit from item usage
     * @return a string array containing the action to use an item on the active Pokémon, or null if no action is needed
     * @throws POOBkemonException if an error occurs during item evaluation, such as accessing invalid data
     */
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

    /**
     * Determines whether the active Pokémon should use the move "Protect" based on its current health,
     * a randomized factor, and whether it has access to the move.
     *
     * @param active the currently active Pokémon to evaluate for using the "Protect" move
     * @return true if the active Pokémon should use "Protect" based on the defensive strategy, false otherwise
     */
    private boolean shouldUseProtect(Pokemon active) {
        double healthRatio = (double) active.currentHealth / active.maxHealth;
        return healthRatio < HEALTH_DEFENSE_THRESHOLD &&
                random.nextDouble() < PROTECT_PROBABILITY &&
                hasProtectMove(active);
    }

    /**
     * Checks whether the given Pokémon has the move "Protect" in its list of attacks.
     *
     * @param pokemon the Pokémon to check for the "Protect" move
     * @return true if the Pokémon has the move "Protect", false otherwise
     */
    private boolean hasProtectMove(Pokemon pokemon) {
        for (Attack attack : pokemon.getAttacks()) {
            if (attack.getName().equalsIgnoreCase("Protect")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds and returns the internal identifier of the "Protect" move for*/
    private String findProtectMove(Pokemon pokemon) {
        for (Attack attack : pokemon.getAttacks()) {
            if (attack.getName().equalsIgnoreCase("Protect") && attack.getPPActual() > 0) {
                return String.valueOf(attack.getIdInside());
            }
        }
        return "1"; // Fallback to first attack if Protect not found (shouldn't happen if shouldUseProtect passed)
    }

    /**
     * Selects a defensive attack for the given attacker Pokémon against the specified opponent.
     * This method evaluates the available attacks and chooses the most suitable one based on defensive strategy.
     *
     * @param attacker the Pokémon that is performing the attack
     * @param opponent the opposing Pokémon that the attack is targeting
     * @return a string array representing the selected attack action. The array contains:
     *         - "Attack" (a constant identifying the action type),
     *         - the internal ID of the selected attack,
     *         - the ID of the attacker Pokémon,
     *         - the ID of the current machine.
     * @throws POOBkemonException if no attacks are available for the*/
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

    /**
     * Selects the best defensive attack from a list of available attacks for the given attacker Pokémon
     * against the specified opponent Pokémon. The selection is based on a scoring strategy
     * that evaluates the defensive effectiveness of each attack.
     *
     * @param attacks the list of available attacks that the attacker Pokémon can use
     * @param attacker the Pokémon that is selecting and will perform the attack
     * @*/
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

    /**
     * Evaluates the defensive effectiveness of a given*/
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

    /**
     * Retrieves a list of available attacks for the specified Pokémon.
     * An attack is considered available if its current PP (Power Points) is greater than 0.
     *
     * @param pokemon the Pokémon whose available attacks are to be retrieved
     * @return a list of attacks that have remaining PP and can be used in battle
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
     * Calculates the type effectiveness of an attacker Pokémon's type against the defender Pokémon's type.
     * The effectiveness is based on a predefined type chart, which determines
     * multipliers for various type matchups.
     *
     * @param attacker the Pokémon performing the attack, whose type is used for calculation
     * @param defender the Pokémon being attacked, whose type is used for calculation
     * @return the type effectiveness multiplier as a double. A value greater than 1.0 indicates
     *         a super-effective attack, less than 1.0 indicates a not very effective attack,
     *         and 1.0 represents neutral effectiveness. If an error occurs, a neutral effectiveness
     *         multiplier of 1.0 is returned by default.
     */
    private double calculateTypeEffectiveness(Pokemon attacker, Pokemon defender) {
        try {
            return typeChart.getMultiplier(attacker.getType(), defender.type);
        } catch (Exception e) {
            return 1.0; // Neutral effectiveness if error occurs
        }
    }

    /**
     * Retrieves the team associated with the trainer in the current game.
     * It iterates through the teams in the game to find the one whose trainer
     * matches the trainer's unique identifier.
     *
     * @param game the current instance of the POOBkemon game, containing information about all teams and trainers.
     * @return the team associated with the trainer.
     * @throws POOBkemonException if no team is found for the trainer.
     */
    private Team getMyTeam(POOBkemon game) throws POOBkemonException {
        for (Team team : game.teams()) {
            if (team.getTrainer().getId() == this.getId()) {
                return team;
            }
        }
        throw new POOBkemonException("Equipo no encontrado para el entrenador: " + this.getId());
    }

    /**
     * Retrieves the active Pokémon from the specified team.
     * The active Pokémon is determined based on the current Pokémon ID*/
    private Pokemon getActivePokemon(Team team) throws POOBkemonException {
        return team.getPokemonById(team.getTrainer().getCurrentPokemonId());
    }

    /**
     * Retrieves the active Pokémon of the opposing team in the game. This method
     * iterates through all teams in the game to find the opposing team's active Pokémon.
     * The active Pokémon is determined by checking which Pokémon is marked as active
     * within the opposing team. If no active opponent Pokémon is found, an exception is thrown.
     *
     * @param game the current instance of the POOBkemon game, containing details of
     *             all teams and their states.
     * @return the active Pokémon from the opposing team,*/
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

    /**
     * Creates a decision to switch the currently active Pokémon with a new one based on defensive strategy.
     * This method evaluates the eligible Pokémon from the team and selects the best candidate for switching,
     * prioritizing defensive effectiveness against the opponent.
     *
     * @param myTeam the team of the player or machine, providing the list of available Pokémon.
     * @param current the currently active Pokémon that may be switched out.
     * @param opponent the opponent's active Pokémon, used to evaluate type and defensive matchups.
     * @return an array of strings representing the switch action:
     *         - "ChangePokemon" (action type),
     *         - the ID of the switching machine,
     *        */
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

    /**
     *
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

    /**
     * Selects the best defensive Pokémon switch from a list of candidates against the specified opponent.
     * The selection is based on a scoring function that evaluates each candidate's defensive effectiveness.
     *
     * @param candidates the list of*/
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

    /**
     * Evaluates a candidate Pokémon to determine its suitability for a defensive switch during a battle.
     * The evaluation considers factors such as type effectiveness, defensive stats, current health ratio,
     * and a small random factor.
     *
     * @param candidate the Pokémon being considered for the defensive switch
     * @param opponent the opposing Pokémon in the battle
     * @return a score (double value) representing the suitability of the candidate Pokémon for
     *         the defensive switch, where a higher score indicates a better choice
     */
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