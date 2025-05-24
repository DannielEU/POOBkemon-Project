package domain;

/**
 * The Offensive class extends the Machine class and represents an offensive strategy
 * for a trainer within the game. It implements specific behaviors for selecting
 * and executing attacks based on the current state of the game.
 */
public class Offensive extends Machine {
    /**
     * Constructs an Offensive object, representing an offensive strategy.
     * It initializes the machine with the specified ID and BagPack.
     *
     * @param id the unique identifier for the trainer using the offensive strategy
     * @param bagPack the BagPack object containing the items required by the trainer
     * @throws*/
    public Offensive(int id, BagPack bagPack) throws POOBkemonException {
        super(id,bagPack);
    }

    /**
     * Determines the movement decision for the machine-controlled trainer
     * within the game based on its current active Pokémon and available attacks.
     *
     * @param game the current state of the POOBkemon game, including all teams and their Pokémon
     * @return a String array representing the decision, where the elements describe the action type,
     *         attack ID, Pokémon ID, and trainer ID
     * @throws POOBkemonException if there is no active Pokémon found for this trainer
     */
    @Override
    public String[] machineMovement(POOBkemon game) throws POOBkemonException {
        // 1. Obtener el Pokémon activo de este entrenador
        Pokemon myActivePokemon = null;
        for (Team team : game.teams()) {
            if (team.getTrainer().getId() == this.getId()) {
                myActivePokemon = team.getPokemonById(team.getTrainer().getCurrentPokemonId());
                break;
            }
        }
        if (myActivePokemon == null) {
            throw new POOBkemonException("No se encontró Pokémon activo");
        }
        Attack selectedAttack = null;
        for(Attack a: myActivePokemon.getAttacks()){
            if(a.getPPActual()>0){
                selectedAttack = a;
                break;
            } else if (selectedAttack == null && a.getPPActual() > 0) {
                selectedAttack = a;
            }
        }

        int attackId = selectedAttack.getIdInside();

        // 4. Crear la decisión de ataque
        String[] decision = {"Attack", String.valueOf(attackId), String.valueOf(myActivePokemon.getId()), String.valueOf(this.getId())};
        return decision;
    }
}
