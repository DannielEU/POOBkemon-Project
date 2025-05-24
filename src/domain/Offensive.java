package domain;

public class Offensive extends Machine {

    public Offensive(int id, BagPack bagPack) throws POOBkemonException {
        super(id,bagPack);
    }
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
