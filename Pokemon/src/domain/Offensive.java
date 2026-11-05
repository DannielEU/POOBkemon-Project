package domain;

/**
 * Estrategia ofensiva: siempre ataca con el primer movimiento que todavía tenga PP.
 */
public class Offensive extends Machine {

    /**
     * @param id      identificador del entrenador
     * @param bagPack mochila del entrenador
     * @throws POOBkemonException si la mochila es nula
     */
    public Offensive(int id, BagPack bagPack) throws POOBkemonException {
        super(id, bagPack);
    }

    /**
     * Elige el primer ataque con PP del Pokémon activo.
     *
     * @throws POOBkemonException si no hay Pokémon activo o ningún ataque tiene PP disponibles
     */
    @Override
    public String[] machineMovement(POOBkemon game) throws POOBkemonException {
        Pokemon active = getActivePokemon(getMyTeam(game));
        for (Attack attack : active.getAttacks()) {
            if (attack.getPPActual() > 0) {
                return attackDecision(active, attack);
            }
        }
        throw new POOBkemonException("No hay ataques con PP disponibles");
    }
}
