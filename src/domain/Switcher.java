package domain;

import persistence.StatsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Switcher extends Machine {
    // Probabilidades base y factores de ajuste
    private static final double BASE_SWITCH_PROBABILITY = 0.3;
    private static final double HEALTH_SWITCH_THRESHOLD = 0.25;
    private static final double TYPE_DISADVANTAGE_THRESHOLD = 0.5;

    // Pesos para el cálculo de puntuaciones
    private static final double TYPE_SCORE_WEIGHT = 0.5;
    private static final double HEALTH_SCORE_WEIGHT = 0.3;
    private static final double RANDOM_SCORE_WEIGHT = 0.2;

    private final StatsRepository typeChart = new StatsRepository();
    private java.util.Random random;

    public Switcher(int id, BagPack bagPack) throws POOBkemonException {
        super(id, bagPack);
        this.random = new Random();
    }

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

    private String[] createSwitchDecision(Team myTeam, Pokemon current, Pokemon opponent) throws POOBkemonException {
        List<Pokemon> candidates = getSwitchCandidates(myTeam, current);

        if (candidates.isEmpty()) {
            return createAttackDecision(current, opponent);
        }

        Pokemon bestChoice = selectBestSwitchCandidate(candidates, opponent);
        return new String[] {
                "ChangePokemon",
                String.valueOf(this.getId()),
                String.valueOf(bestChoice.getId())
        };
    }

    private String[] createAttackDecision(Pokemon attacker, Pokemon opponent) throws POOBkemonException {
        List<Attack> availableAttacks = getAvailableAttacks(attacker);

        if (availableAttacks.isEmpty()) {
            throw new POOBkemonException("No hay ataques disponibles para " + attacker.getName());
        }

        Attack bestAttack = selectBestAttack(availableAttacks, attacker, opponent);
        return new String[] {
                "Attack",
                String.valueOf(bestAttack.getIdInside()),
                String.valueOf(attacker.getId()),
                String.valueOf(this.getId())
        };
    }

    // Métodos de selección
    private List<Pokemon> getSwitchCandidates(Team team, Pokemon current) {
        List<Pokemon> candidates = new ArrayList<>();
        for (Pokemon pokemon : team.getPokemons()) {
            if (!pokemon.getWeak() && pokemon.getId() != current.getId()) {
                candidates.add(pokemon);
            }
        }
        return candidates;
    }

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

    private double evaluateSwitchCandidate(Pokemon candidate, Pokemon opponent) {
        double score = 0;

        double typeEffectiveness = calculateTypeEffectiveness(candidate, opponent);
        score += typeEffectiveness * TYPE_SCORE_WEIGHT;

        double healthRatio = (double) candidate.currentHealth / candidate.maxHealth;
        score += healthRatio * HEALTH_SCORE_WEIGHT;

        score += random.nextDouble() * RANDOM_SCORE_WEIGHT;

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

    private double evaluateAttack(Attack attack, Pokemon attacker, Pokemon opponent) {
        try {
            double score = 0;

            double typeEffectiveness = typeChart.getMultiplier(attack.getType(), opponent.type);
            score += typeEffectiveness * 0.5;

            score += (attack.getPower() / 150.0) * 0.3;

            score += (attack.getAccuracy() / 100.0) * 0.15;
            score += random.nextDouble() * 0.05;

            return score;
        } catch (Exception e) {
            return random.nextDouble();
        }
    }

    // Cálculo de efectividad de tipo
    private double calculateTypeEffectiveness(Pokemon attacker, Pokemon defender) {
        try {
            return typeChart.getMultiplier(attacker.getType(), defender.getType());
        } catch (Exception e) {
            return 1.0;
        }
    }
}