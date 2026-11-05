package domain;

import persistence.StatsRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Entrenador controlado por la máquina. Cada subclase implementa una estrategia distinta
 * ({@link #machineMovement(POOBkemon)}); aquí viven las consultas del estado de la batalla
 * y los constructores de decisiones que comparten todas las estrategias.
 */
public abstract class Machine extends Trainer {

    /** Tabla de tipos compartida; al ser estática no forma parte del estado serializado. */
    protected static final StatsRepository TYPE_CHART = new StatsRepository();

    public Machine(int id, BagPack bagPack) throws POOBkemonException {
        super(id, bagPack);
    }

    /**
     * Decide la acción a realizar en el turno actual.
     *
     * @param game partida en curso
     * @return decisión en el formato que entiende {@link POOBkemon#takeDecision(String[])}
     */
    public abstract String[] machineMovement(POOBkemon game) throws POOBkemonException;

    // ------------------------------------------------------------------ consultas

    /** Equipo al que pertenece esta máquina. */
    protected Team getMyTeam(POOBkemon game) throws POOBkemonException {
        for (Team team : game.teams()) {
            if (team.getTrainer().getId() == getId()) {
                return team;
            }
        }
        throw new POOBkemonException("Equipo no encontrado para el entrenador: " + getId());
    }

    /** Pokémon activo de un equipo. */
    protected Pokemon getActivePokemon(Team team) throws POOBkemonException {
        return team.getPokemonById(team.getTrainer().getCurrentPokemonId());
    }

    /** Pokémon activo del equipo rival. */
    protected Pokemon getOpponentActivePokemon(POOBkemon game) throws POOBkemonException {
        for (Team team : game.teams()) {
            if (team.getTrainer().getId() != getId()) {
                for (Pokemon pokemon : team.getPokemons()) {
                    if (pokemon.getActive()) {
                        return pokemon;
                    }
                }
            }
        }
        throw new POOBkemonException("No se encontró Pokémon oponente activo");
    }

    /** Ataques del Pokémon que aún tienen PP. */
    protected static List<Attack> getAvailableAttacks(Pokemon pokemon) {
        List<Attack> available = new ArrayList<>();
        for (Attack attack : pokemon.getAttacks()) {
            if (attack.getPPActual() > 0) {
                available.add(attack);
            }
        }
        return available;
    }

    /** Pokémon del equipo, distintos del actual, que pueden entrar al combate. */
    protected static List<Pokemon> getSwitchCandidates(Team team, Pokemon current) {
        List<Pokemon> candidates = new ArrayList<>();
        for (Pokemon pokemon : team.getPokemons()) {
            if (!pokemon.getWeak() && pokemon.getId() != current.getId()) {
                candidates.add(pokemon);
            }
        }
        return candidates;
    }

    /** Multiplicador de efectividad de tipos entre dos Pokémon (1.0 si no se puede calcular). */
    protected static double calculateTypeEffectiveness(Pokemon attacker, Pokemon defender) {
        return TYPE_CHART.getMultiplier(attacker.getType(), defender.getType());
    }

    /** Proporción de vida restante del Pokémon, entre 0 y 1. */
    protected static double healthRatio(Pokemon pokemon) {
        return pokemon.maxHealth == 0 ? 0 : (double) pokemon.currentHealth / pokemon.maxHealth;
    }

    // ------------------------------------------------------------------ mochila

    /**
     * Primer ítem de la lista de preferencia que todavía tenga unidades en la mochila.
     *
     * @param preferredNames nombres de ítem, en orden de preferencia (sin distinguir mayúsculas)
     * @return el nombre real del ítem tal como lo conoce la mochila, o {@code null} si no hay ninguno
     */
    protected String firstAvailableItem(String... preferredNames) {
        String[][] owned = getBagPack().getItems();
        for (String wanted : preferredNames) {
            for (String[] item : owned) {
                if (item[0].equalsIgnoreCase(wanted) && Integer.parseInt(item[1]) > 0) {
                    return item[0];
                }
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ decisiones

    protected String[] attackDecision(Pokemon attacker, Attack attack) {
        return Decision.attack(attack.getIdInside(), attacker.getId(), getId());
    }
}
