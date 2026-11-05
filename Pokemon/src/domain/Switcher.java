package domain;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Switcher class represents a machine-driven trainer that implements decision-making logic
 * for switching Pokémon or attacking based on various factors such as health, type effectiveness,
 * and randomness.
 *
 * This class extends the Machine class and provides a concrete implementation
 * of*/
public class Switcher extends Machine {
    // Probabilidades base y factores de ajuste
    private static final double BASE_SWITCH_PROBABILITY = 0.3;
    private static final double HEALTH_SWITCH_THRESHOLD = 0.25;
    private static final double TYPE_DISADVANTAGE_THRESHOLD = 0.5;

    // Pesos para el cálculo de puntuaciones
    private static final double TYPE_SCORE_WEIGHT = 0.5;
    private static final double HEALTH_SCORE_WEIGHT = 0.3;
    private static final double RANDOM_SCORE_WEIGHT = 0.2;

    private java.util.Random random;

    /**
     * Constructs a Switcher object that represents a specialized Machine with enhanced capabilities.
     * The Switcher includes logic to decide optimal actions during gameplay, such as whether to
     * switch Pokemon or execute specific moves based on the current game state and probabilities.
     *
     * @param id the unique identifier for this Switcher instance
     * @param bagPack the BagPack containing items available to this Switcher instance
     * @throws POOBkemonException if the BagPack is null or other initialization errors occur
     */
    public Switcher(int id, BagPack bagPack) throws POOBkemonException {
        super(id, bagPack);
        this.random = new Random();
    }

    /**
     * Determines the next movement for a machine-controlled player in a POOBkemon game.
     * Based on the current game state, the method decides whether to attack the opponent's active Pokemon
     * or switch to another Pokemon in the team. The decision is influenced by factors like type effectiveness,
     * health thresholds, and other strategic considerations.
     *
     * @param game the current state of the POOBkemon game, including teams, active Pokemon, and other game details
     * @return a String array representing the action to be performed, where the first element indicates the type of action
     *         (e.g., "attack" or "switch"), and subsequent elements provide additional details specific to the chosen action
     * @throws POOBkemonException if an error occurs during the decision-making process, such as invalid game data or
     *                             unexpected states within the game
     */
    @Override
    public String[] machineMovement(POOBkemon game) throws POOBkemonException {
        Team myTeam = getMyTeam(game);
        Pokemon myActive = getActivePokemon(myTeam);
        Pokemon opponent = getOpponentActivePokemon(game);

        double typeEffectiveness = calculateTypeEffectiveness(myActive, opponent);

        if (shouldSwitchPokemon(myTeam, myActive, opponent, typeEffectiveness)) {
            return createSwitchDecision(myTeam, myActive, opponent);
        }
        return createAttackDecision(myActive, opponent);
    }




    /**
     * Determines whether the active Pokemon on the player's team should be switched out,
     * based on its current health, the effectiveness of its type against the opponent,
     * and dynamically calculated probabilities.
     *
     * @param myTeam the player's team, containing the available Pokemon for potential switching
     * @param myActive the currently active Pokemon on the player's team
     * @param opponent the opponent's active Pokemon
     * @param typeEffectiveness the effectiveness of the type match-up between the active Pokemon and the opponent
     *                          (a value greater than 1 indicates an advantage, less than 1 indicates a disadvantage,
     *                          and 0 indicates no effect)
     * @return true if the active Pokemon should be switched out, otherwise false
     */
    private boolean shouldSwitchPokemon(Team myTeam, Pokemon myActive, Pokemon opponent, double typeEffectiveness) {

        if ((double) myActive.currentHealth / myActive.maxHealth < HEALTH_SWITCH_THRESHOLD) {
            return true;
        }

        if (typeEffectiveness < TYPE_DISADVANTAGE_THRESHOLD) {
            return true;
        }

        if (typeEffectiveness == 0) {
            return true;
        }
        double switchProbability = calculateDynamicSwitchProbability(typeEffectiveness);
        return random.nextDouble() < switchProbability;
    }

    /**
     * Calculates the dynamic probability of switching a Pokemon based on type effectiveness.
     * The base switch probability is adjusted depending on whether the given type effectiveness
     * value indicates an advantage or disadvantage in battle dynamics.
     *
     * @param typeEffectiveness the effectiveness of the type match-up, where a value less than 1.0 indicates a disadvantage,
     *                          1.0 indicates neutrality, and a value greater than 1.0 indicates an advantage
     * @return the calculated switch probability as*/
    private double calculateDynamicSwitchProbability(double typeEffectiveness) {
        double probability = BASE_SWITCH_PROBABILITY;

        if (typeEffectiveness < 1.0) {
            probability += (1.0 - typeEffectiveness) * 0.3;
        }

        if (typeEffectiveness > 1.0) {
            probability -= (typeEffectiveness - 1.0) * 0.2;
        }

        return Math.max(0.1, Math.min(0.9, probability));
    }

    /**
     * Creates a decision for switching the active Pokemon in the player's team based on the current game state.
     * If no suitable candidates for switching are found, an attack decision is generated instead.
     *
     * @param myTeam the player's team, containing all available Pokemon for this decision-making process
     * @param current the currently active Pokemon on the player's team
     * @param opponent the opponent's active Pokemon
     * @return a String array representing the switch action to be performed, where the first element indicates
     *         the action type ("ChangePokemon"), and subsequent elements provide additional details like
     **/
    private String[] createSwitchDecision(Team myTeam, Pokemon current, Pokemon opponent) throws POOBkemonException {
        List<Pokemon> candidates = getSwitchCandidates(myTeam, current);

        if (candidates.isEmpty()) {
            return createAttackDecision(current, opponent);
        }

        Pokemon bestChoice = selectBestSwitchCandidate(candidates, opponent);
        return Decision.changePokemon(getId(), bestChoice.getId());
    }

    /**
     * Creates a decision for attacking the opponent's Pokemon based on the available attacks of the attacker
     * and their effectiveness against the opponent. This method selects the best attack for the given context.
     *
     * @param attacker the Pokemon executing the attack
     * @param opponent the Pokemon being targeted by the attack
     * @return a String array representing the attack action to be performed, where:
     *         - The first element indicates the action type ("Attack").
     *         - The second element is the ID of the selected attack.
     *         - The third element is the ID of the attacking Pokemon.
     *         - The fourth element is the ID of the current Switcher instance.
     * @throws POOBkemonException if no attacks are available for the specified attacker*/
    private String[] createAttackDecision(Pokemon attacker, Pokemon opponent) throws POOBkemonException {
        List<Attack> availableAttacks = getAvailableAttacks(attacker);

        if (availableAttacks.isEmpty()) {
            throw new POOBkemonException("No hay ataques disponibles para " + attacker.getName());
        }

        Attack bestAttack = selectBestAttack(availableAttacks, attacker, opponent);
        return attackDecision(attacker, bestAttack);
    }


    /**
     * Selects the best Pokemon candidate for switching out based on a set of evaluation criteria.
     * The selection is determined by comparing evaluation scores for each candidate against the opponent.
     *
     * @param candidates the list of Pokemon that are eligible for switching. Each Pokemon in the list
     *                   will be evaluated based on its performance against the opponent.
     * @param opponent the active Pokemon from the opposing team, which the selected candidate will face.
     * @return the Pokemon from the candidates list that has the highest evaluation score
     *         and is deemed most effective against the opponent.
     */
    private Pokemon selectBestSwitchCandidate(List<Pokemon> candidates, Pokemon opponent) {
        Pokemon best = candidates.get(0);
        double bestScore = evaluateSwitchCandidate(best, opponent);

        for (Pokemon candidate : candidates) {
            double currentScore = evaluateSwitchCandidate(candidate, opponent);
            if (currentScore > bestScore) {
                best = candidate;
                bestScore = currentScore;
            }
        }

        return best;
    }

    /**
     * Evaluates a candidate Pokemon's suitability for switching into battle against a given opponent.
     * The evaluation is based on factors such as type*/
    private double evaluateSwitchCandidate(Pokemon candidate, Pokemon opponent) {
        double score = 0;

        double typeEffectiveness = calculateTypeEffectiveness(candidate, opponent);
        score += typeEffectiveness * TYPE_SCORE_WEIGHT;

        double healthRatio = (double) candidate.currentHealth / candidate.maxHealth;
        score += healthRatio * HEALTH_SCORE_WEIGHT;

        score += random.nextDouble() * RANDOM_SCORE_WEIGHT;

        return score;
    }


    /**
     * Selects the best attack from a list of available attacks based on their
     * effectiveness and suitability in the current battle context.
     * The selection is influenced by the attributes of the attacker and the opponent.
     *
     * @param attacks the list of available attacks to evaluate and choose from
     * @param attacker the attacking Pokemon, whose stats and abilities influence the effectiveness of the attacks
     * @param opponent the opponent Pokemon, against which the attack's impact will be measured
     * @return the attack that is deemed most effective based on the evaluation criteria
     */
    private Attack selectBestAttack(List<Attack> attacks, Pokemon attacker, Pokemon opponent) {
        Attack best = attacks.get(0);
        double bestScore = evaluateAttack(best, attacker, opponent);

        for (Attack attack : attacks) {
            double currentScore = evaluateAttack(attack, attacker, opponent);
            if (currentScore > bestScore) {
                best = attack;
                bestScore = currentScore;
            }
        }

        return best;
    }

    /**
     * Evaluates the effectiveness of a given attack from an attacker Pokémon against an opponent Pokémon.
     * The evaluation considers factors such as the attack's type effectiveness, power, and accuracy,
     * alongside randomness to introduce variation.
     *
     * @param attack the attack being evaluated, containing attributes like type, power, and accuracy
     * @param attacker the Pokémon that executes the attack
     * @param opponent the Pokémon targeted by the attacker
     * @return a score as a double representing the effectiveness of the attack against the opponent
     */
    private double evaluateAttack(Attack attack, Pokemon attacker, Pokemon opponent) {
        try {
            double score = 0;

            double typeEffectiveness = TYPE_CHART.getMultiplier(attack.getType(), opponent.type);
            score += typeEffectiveness * 0.5;

            score += (attack.getPower() / 150.0) * 0.3;

            score += (attack.getAccuracy() / 100.0) * 0.15;
            score += random.nextDouble() * 0.05;

            return score;
        } catch (Exception e) {
            return random.nextDouble();
        }
    }

}