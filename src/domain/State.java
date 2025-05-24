package domain;

import java.io.Serializable;

/**
 * Representa un estado alterado aplicado a un Pokémon (como envenenado, paralizado, dormido, etc.).
 * Maneja efectos persistentes, duración, y aplicación de daño/efectos cada turno.
 */
public class State implements Serializable {

    public enum StateType {
        // Estados de daño/condición negativa
        PARALYSIS,      // Paraliza reduciendo velocidad y puede impedir ataque
        BURN,           // Quema causando daño y reduciendo ataque físico
        POISON,         // Envenena causando daño constante
        BAD_POISON,     // Envenenamiento grave (daño progresivo)
        SLEEP,          // Duerme e impide atacar
        FREEZE,         // Congela e impide atacar

        CONFUSION,      // Confunde con chance de autodaño
        FLINCH,         // Impide atacar en ese turno
        TRAPPED,        // Impide cambiar Pokémon
        NIGHTMARE,      // Daño extra si está dormido
        LEECH_SEED,     // Drena vida cada turno
        CURSE,          // Efecto diferente para Pokémon Fantasma
        PERISH_SONG,    // Desmayo después de 3 turnos
        DESTINY_BOND,   // Si el usuario cae, el objetivo también

        // Modificadores de stats positivos
        ATTACK_UP,
        DEFENSE_UP,
        SPEED_UP,
        SP_ATTACK_UP,
        SP_DEFENSE_UP,
        EVASION_UP,
        ACCURACY_UP,
        CRITICAL_UP,
        ALL_STATS_UP,

        // Modificadores de stats negativos
        ATTACK_DOWN,
        DEFENSE_DOWN,
        SPEED_DOWN,
        SP_ATTACK_DOWN,
        SP_DEFENSE_DOWN,
        EVASION_DOWN,
        ACCURACY_DOWN,

        // Estados de curación/beneficios
        HEAL,
        AQUA_RING,      // Cura PS cada turno
        INGRAIN,        // Cura PS pero no puede cambiar

        // Estados complejos/mecánicas especiales
        TRANSFORM,      // Copia stats y movimientos
        SUBSTITUTE,     // Absorbe daño con PS
        PROTECT,        // Bloquea ataques
        DISABLE,        // Anula último movimiento usado
        ENCORE,         // Obliga a repetir movimiento
        TAUNT,          // Solo permite movimientos ofensivos
        TORMENT,        // Impide usar mismo movimiento dos veces
        EMBARGO,        // Impide usar objetos
        MAGIC_COAT,     // Refleja efectos de estado
        SNATCH,         // Roba efectos de curación/mejora
        GRUDGE,         // Elimina PP del movimiento que debilita
        IMPRISON,       // Bloquea movimientos compartidos
        RECHARGE,       // Requiere turno de descanso
        RAGE,           // Aumenta ataque al recibir daño

        // Efectos de campo
        SPIKES,         // Daño al cambiar Pokémon
        STEALTH_ROCK,   // Daño tipo roca al cambiar
        TOXIC_SPIKES,   // Envenena al cambiar
        STICKY_WEB,     // Reduce velocidad al cambiar
        SANDSTORM,      // Daño por turno a no tipos Roca/Tierra/Acero
        HAIL,           // Daño por turno a no tipo Hielo

        // Estados combinados
        ATTACK_UP_DEFENSE_UP,
        SP_ATTACK_UP_SP_DEFENSE_UP,
        ATTACK_UP_SPEED_UP,
        DEFENSE_UP_SP_DEFENSE_UP,
        CONFUSION_ATTACK_UP,
        ATTACK_DOWN_DEFENSE_DOWN,

        // Efectos únicos
        TYPE_CHANGE,    // Cambia tipo del Pokémon
        ITEM_LOST,      // Pierde objeto
        IDENTIFIED,     // Revela información del objetivo
        TELEKINESIS,    // Flota e inmune a movimientos tierra
        ILLUSION,       // Muestra como otro Pokémon
        POWER_TRICK,    // Intercambia ataque y defensa
        GASTRO_ACID,    // Anula habilidad
        UPROAR,         // Impide dormir
        ROOTED,         // No puede cambiar pero cura PS
        CHARGE,         // Aumenta poder eléctrico siguiente turno
        PERISH_BODY,    // Activa Perish Song al contacto físico
        OCTOLOCK,      // Reduce defensa y evasión cada turno
        FIRE_WEAK,

        REDIRECT,
        COPY_ABILITY,
        FORCE_SWITCH,
        COPY_MOVE,
        RANDOM_MOVE
    }

    private StateType type;
    private int duration;  // En turnos, -1 si es indefinido
    private boolean isPermanent; // Si persiste fuera de combate
    private boolean isVolatile; // Si puede coexistir con otros estados
    private int damage;
    private String description;
    private int intensity = 1; // Valor de envenenamiento grave (1 = 1/8 de vida)
    /**
     * Crea un nuevo estado.
     * @param info Array con la información del estado [tipo, duración, permanente, volátil, descripción]
     * @throws IllegalArgumentException si la información es inválida
     */
    public State(String[] info) {
        if (info == null || info.length < 5) {
            throw new IllegalArgumentException("Información de estado inválida");
        }
        try {
            this.type = StateType.valueOf(info[0].trim().toUpperCase());
            this.duration = Integer.parseInt(info[1]);
            this.isPermanent = info[2].equals("1");
            this.isVolatile = info[3].equals("1");
            this.description = info[4];
            // Inicializar daño base según tipo
            this.damage = calculateBaseDamage();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al crear estado: " + e.getMessage());
        }
    }

    public String getName(){
        return this.type.name();
    }

    /**
     * Calcula el daño base según el tipo de estado
     */
    private int calculateBaseDamage() {
        switch (this.type) {
            case BURN:
                return 1;
            case POISON:
                return 1; // Representa 1/8 de vida (se ajusta en applyEffect)
            case BAD_POISON:
                return 1; // Representa 1/16 incrementando (se ajusta en applyEffect)
            default:
                return 0;
        }
    }
    
    public State.StateType getType() {  // Especifica que StateType es interno a State
        return this.type;
    }

    public int getDuration() {
        return duration;
    }

    /**
     * Verifica si el estado es principal (no volátil)
     */
    public boolean isPrincipal() {
        return !isVolatile;
    }

    /**
     * Aplica el efecto del estado al Pokémon
     * @param pokemon Pokémon afectado
     * @return Mensaje descriptivo del efecto aplicado
     */
    public String applyEffect(Pokemon pokemon) {
        if (pokemon == null || !isActive() || pokemon.getWeak()) {
            return "";
        }

        StringBuilder effectMessage = new StringBuilder();

        switch (this.type) {
            case BURN:
                applyBurnEffect(pokemon, effectMessage);
                break;
            case POISON:
                applyPoisonEffect(pokemon, effectMessage);
                break;
            case PARALYSIS:
                applyParalysisEffect(pokemon, effectMessage);
                break;
            case BAD_POISON:
                applyBadPoisonEffect(pokemon, effectMessage);
                break;
            case SLEEP:
                applySleepEffect(pokemon, effectMessage);
                break;
            case FREEZE:
                applyFreezeEffect(pokemon, effectMessage);
                break;
            case HEAL:
                if(!pokemon.getWeak()){
                    applyHealEffect(pokemon, effectMessage);
                }
                break;
            case ATTACK_UP:
                pokemon.modifyStat("attack",1.2);
                break;
            case DEFENSE_UP:
                pokemon.modifyStat("defense",1.2);
                break;
            case SPEED_UP:
                pokemon.modifyStat("speed",1.2);
                break;
            case SP_ATTACK_UP:
                pokemon.modifyStat("SP_attack",1.2);
                break;
            case SP_DEFENSE_UP:
                pokemon.modifyStat("SP_defense",1.2);
                break;
            case EVASION_UP:
                pokemon.modifyStat("evasion",1.2);
                break;
            case ACCURACY_UP:
                applyStatUpEffect(pokemon, effectMessage);
                break;
            case ATTACK_DOWN:
                pokemon.modifyStat("attack",0.8);
                break;
            case DEFENSE_DOWN:
                pokemon.modifyStat("defense",0.8);
                break;
            case SPEED_DOWN:
                pokemon.modifyStat("speed",0.8);
                break;
            case SP_ATTACK_DOWN:
                pokemon.modifyStat("SP_attack",0.8);
                break;
            case SP_DEFENSE_DOWN:
                pokemon.modifyStat("SP_defense",0.8);
                break;
            case EVASION_DOWN:
                pokemon.modifyStat("evasion",0.8);
                break;
            case ACCURACY_DOWN:
                applyStatDownEffect(pokemon, effectMessage);
                break;
            case CONFUSION:
                applyConfusionEffect(pokemon, effectMessage);
                break;
            case FLINCH:
                //applyStatDownEffect(pokemon, effectMessage);
                applyFlinchEffect(pokemon,effectMessage);
                break;
            case LEECH_SEED:
                applyLeechSeedEffect(pokemon, effectMessage);
                break;
            case CURSE:
                applyCurseEffect(pokemon, effectMessage);
                break;
            case NIGHTMARE:
                applyNightmareEffect(pokemon, effectMessage);
                break;
            case PROTECT:
                applyProtectEffect(pokemon, effectMessage);
                break;
            case PERISH_SONG:
                applyPerishSongEffect(pokemon, effectMessage);
                break;
            case DESTINY_BOND:
                applyDestinyBondEffect(pokemon, effectMessage);
                break;
            case SPIKES:
                applySpikesEffect(pokemon, effectMessage);
                break;
            case SANDSTORM:
                applySandstormEffect(pokemon, effectMessage);
                break;
            case TAUNT:
                applyTauntEffect(pokemon, effectMessage);
                break;
            case TORMENT:
                applyTormentEffect(pokemon, effectMessage);
                break;
            case TRANSFORM:
                applyTransformEffect(pokemon, effectMessage);
                break;
            case SUBSTITUTE:
                applySubstituteEffect(pokemon, effectMessage);
                break;
            case INGRAIN:
                applyIngrainEffect(pokemon, effectMessage);
                break;
            case DISABLE:
                applyDisableEffect(pokemon, effectMessage);
                break;
            case RECHARGE:
                applyRechargeEffect(pokemon, effectMessage);
                break;
            case RAGE:
                applyRageEffect(pokemon, effectMessage);
                break;
            case TRAPPED:
                applyTrappedEffect(pokemon, effectMessage);
                break;
            case TYPE_CHANGE:
                applyTypeChangeEffect(pokemon, effectMessage);
                break;
            case MAGIC_COAT:
                applyMagicCoatEffect(pokemon, effectMessage);
                break;
            case SNATCH:
                applySnatchEffect(pokemon, effectMessage);
                break;
            case GRUDGE:
                applyGrudgeEffect(pokemon, effectMessage);
                break;
            case IMPRISON:
                applyImprisonEffect(pokemon, effectMessage);
                break;
            default:
                break;
        }
        if(isHeal(this,pokemon)){
            effectMessage.append(pokemon.getName()).append(" se ha curado de ").append(type.name().toLowerCase());
        }
        pokemon.isWeak();
        return effectMessage.toString();
    }

    private boolean isHeal(State state, Pokemon pokemon){
        if (state.duration > 0) {
            duration--;
            if (duration == 0) {
                pokemon.deleteState(this);
                return true;
            }
        }
        return false;
    }

    // Métodos auxiliares para cada efecto
    private void applyBurnEffect(Pokemon pokemon, StringBuilder message) {
        damage = pokemon.maxHealth / 8;
        if (damage == 0) {damage = 1;}
        pokemon.takeDamage(damage);
        pokemon.modifyStat("attack",0.5); // Reduce ataque físico en 50%
        message.append(pokemon.getName()).append(" sufre ").append(damage).append(" de daño por quemadura!");
    }

    private void applyPoisonEffect(Pokemon pokemon, StringBuilder message) {
        this.damage = pokemon.maxHealth / 8;
        if(damage <= 0){ damage = 1;}
        pokemon.takeDamage(damage);
        message.append(pokemon.getName()).append(" sufre ").append(damage).append(" de daño por veneno!");
    }

    private void applyBadPoisonEffect(Pokemon pokemon, StringBuilder message) {
        damage = (pokemon.maxHealth / 25);
        if(damage <= 0){ damage = 1;}
        damage *= intensity;
        pokemon.takeDamage(damage);
        pokemon.isWeak();
        intensity++;
        message.append(pokemon.getName()).append(" sufre ").append(damage)
                .append(" de daño \npor envenenamiento grave (").append(intensity).append("x)!");
    }

    private void applyParalysisEffect(Pokemon pokemon, StringBuilder message) {
        pokemon.reduceSpeed(50); // Reduce velocidad en 50%
        if (Math.random() < 0.25) {
            pokemon.setCanAttack(false);
            message.append(pokemon.getName()).append(" está paralizado y no puede moverse!");
        } else {
            message.append(pokemon.getName()).append(" está paralizado!");
        }
    }

    private void applySleepEffect(Pokemon pokemon, StringBuilder message) {
        pokemon.setCanAttack(false);
        message.append(pokemon.getName()).append(" está dormido.");

        // 20% de probabilidad de despertarse cada turno
        if (Math.random() < 0.2) {
            duration = 0;
            pokemon.setCanAttack(true);
            message.append(" ").append(pokemon.getName()).append(" se despertó!");
        }
    }

    private void applyFlinchEffect(Pokemon pokemon, StringBuilder message){
        pokemon.setCanAttack(false);
        message.append(pokemon.getName()).append(" no ataca este turno.");      
    }

    private void applyFreezeEffect(Pokemon pokemon, StringBuilder message) {
        pokemon.setCanAttack(false);
        message.append(pokemon.getName()).append(" está congelado!");

        if (Math.random() < 0.2) {
            duration = 0;
            pokemon.setCanAttack(true);
            message.append(" ").append(pokemon.getName()).append(" se descongeló!");
        }
    }

    private void applyHealEffect(Pokemon pokemon, StringBuilder message) {
        int healAmount = (int) (pokemon.maxHealth * 0.05);
        if(healAmount == 0) { healAmount = 1; }
        pokemon.heals(healAmount);
        message.append(pokemon.getName()).append(" recuperó ").append(healAmount).append(" PS!");
    }

    private void applyStatUpEffect(Pokemon pokemon, StringBuilder message) {
        String stat = type.name().split("_")[0].toLowerCase();
        pokemon.modifyStat(stat, 1.2);
        message.append(pokemon.getName()).append(" aumentó su ").append(stat).append("!");
    }

    private void applyConfusionEffect(Pokemon pokemon, StringBuilder message) {
        if (Math.random() < 0.33) { // 33% de golpearse a sí mismo
            damage = pokemon.getAttacks().get(0).getPower() / 2;
            pokemon.takeDamage(damage);
            message.append(pokemon.getName()).append(" está confundido y se hirió a sí mismo!");
        } else {
            message.append(pokemon.getName()).append(" está confundido!");
        }
    }

    /**
     * Verifica si el estado sigue activo
     */
    public boolean isActive() {
        return duration != 0;
    }

    @Override
    public String toString() {
        return type.name(); //+ (duration > 0 ? " (" + duration + " turnos restantes)" : "");
    }

    // Métodos auxiliares adicionales para completar la implementación

    private void applyStatDownEffect(Pokemon pokemon, StringBuilder message) {
        String stat = type.name().split("_")[0].toLowerCase();
        int stages = -1;
        pokemon.modifyStat(stat, stages);
        message.append(pokemon.getName()).append(" redujo su ").append(stat).append("!");
    }

    private void applyLeechSeedEffect(Pokemon pokemon, StringBuilder message) {
        int damage = pokemon.maxHealth / 8;
        pokemon.takeDamage(damage);
        // El usuario debería recuperar esta vida (necesitas referencia al Pokémon atacante)
        message.append(pokemon.getName()).append(" perdió ").append(damage).append(" PS por drenadoras!");
    }

    private void applyCurseEffect(Pokemon pokemon, StringBuilder message) {
        if (pokemon.getType().equals("GHOST")) {
            // Efecto para Pokémon fantasma
            int damage = pokemon.maxHealth / 2;
            pokemon.takeDamage(damage);
            message.append(pokemon.getName()).append(" usó maldición a costa de su vida!");
        } else {
            // Efecto para otros tipos
            pokemon.modifyStat("attack", 1);
            pokemon.modifyStat("defense", 1);
            pokemon.modifyStat("speed", -1);
            message.append(pokemon.getName()).append(" maldijo al enemigo!");
        }
    }

    private void applyNightmareEffect(Pokemon pokemon, StringBuilder message) {
        if (pokemon.hasState(String.valueOf(StateType.SLEEP))) {
            int damage = pokemon.maxHealth/ 4;
            pokemon.takeDamage(damage);
            message.append(pokemon.getName()).append(" sufre una pesadilla!");
        }
    }

    private void applyProtectEffect(Pokemon pokemon, StringBuilder message) {
        pokemon.setProtected(true);
        message.append(pokemon.getName()).append(" se protegió!");
    }

    private void applyPerishSongEffect(Pokemon pokemon, StringBuilder message) {
        if (duration == 3) { // Solo mostrar el mensaje el primer turno
            message.append("¡Un canto mortal afecta a ").append(pokemon.getName()).append("!");
        }
        if (duration == 0) {
            pokemon.takeDamage(pokemon.currentHealth);
            message.append("¡").append(pokemon.getName()).append(" sucumbió al canto mortal!");
        }
    }

    private void applyDestinyBondEffect(Pokemon pokemon, StringBuilder message) {
        if (duration == 0) {
            message.append("¡").append(pokemon.getName()).append(" vinculó su destino!");
        }
    }

    private void applySpikesEffect(Pokemon pokemon, StringBuilder message) {
        // Este efecto se maneja a nivel de campo de batalla
        message.append("¡Púas esparcidas en el campo rival!");
    }

    private void applySandstormEffect(Pokemon pokemon, StringBuilder message) {
        if (!pokemon.getType().equals("ROCK") &&
                !pokemon.getType().equals("GROUND") &&
                !pokemon.getType().equals("STEEL")) {
            int damage = pokemon.maxHealth / 16;
            pokemon.takeDamage(damage);
            message.append("¡La tormenta de arena daña a ").append(pokemon.getName()).append("!");
        }
    }

    private void applyTauntEffect(Pokemon pokemon, StringBuilder message) {
        pokemon.modifyStat("speed",1.3);
        pokemon.modifyStat("defense",0.4);
        pokemon.modifyStat("SP_defense",0.5);
        pokemon.modifyStat("SP_attack", 1.3);
        message.append("¡").append(pokemon.getName()).append(" fue provocado!");
    }

    private void applyTormentEffect(Pokemon pokemon, StringBuilder message) {
        pokemon.modifyStat("speed",1.3);
        pokemon.modifyStat("defense",0.6);
        pokemon.modifyStat("SP_attack", 1.3);
        message.append("¡").append(pokemon.getName()).append(" fue atormentado!");
    }

    private void applyTransformEffect(Pokemon pokemon, StringBuilder message) {
        int hpCost = pokemon.maxHealth / 5;
        pokemon.setNewPS(hpCost);
        message.append("¡").append(pokemon.getName()).append(" se transformó!");
    }

    private void applySubstituteEffect(Pokemon pokemon, StringBuilder message) {
        int hpCost = pokemon.maxHealth / 4;
        if (pokemon.currentHealth > hpCost) {
            pokemon.takeDamage(hpCost);
            pokemon.setNewPS(hpCost);
            message.append("¡").append(pokemon.getName()).append(" se modificó!");
        } else {
            message.append("¡No tiene suficiente PS para alterarse!");
        }
    }

    private void applyIngrainEffect(Pokemon pokemon, StringBuilder message) {
        int heal = pokemon.maxHealth / 16;
        pokemon.heals(heal);
        message.append("¡").append(pokemon.getName()).append(" se arraigó y recuperó PS!");
    }

    private void applyDisableEffect(Pokemon pokemon, StringBuilder message) {
        pokemon.disableLastMove();
        message.append("¡Un movimiento de ").append(pokemon.getName()).append(" fue anulado!");
    }

    private void applyRechargeEffect(Pokemon pokemon, StringBuilder message) {
        pokemon.modifyStat("speed",0.3);
        message.append("¡").append(pokemon.getName()).append(" debe recargar energía!");
    }

    private void applyRageEffect(Pokemon pokemon, StringBuilder message) {
        pokemon.modifyStat("attack",1.3);
        message.append("¡").append(pokemon.getName()).append(" entra en cólera!");
    }

    private void applyTrappedEffect(Pokemon pokemon, StringBuilder message) {
        pokemon.setTrapped(true);
        message.append("¡").append(pokemon.getName()).append(" no puede escapar!");
    }

    private void applyTypeChangeEffect(Pokemon pokemon, StringBuilder message) {
        // Necesitas especificar el nuevo tipo
        // pokemon.setType(newType);
        message.append("¡El tipo de ").append(pokemon.getName()).append(" cambió!");
    }

    private void applyMagicCoatEffect(Pokemon pokemon, StringBuilder message) {
        pokemon.setProtected(true);
        message.append("¡").append(pokemon.getName()).append(" activó manto mágico!");
    }

    private void applySnatchEffect(Pokemon pokemon, StringBuilder message) {
        pokemon.modifyStat("speed", 1.6);
        pokemon.modifyStat("attack", 1.6);
        message.append("¡").append(pokemon.getName()).append(" prepara un arrebato!");
    }

    private void applyGrudgeEffect(Pokemon pokemon, StringBuilder message) {
        pokemon.modifyStat("attack", 0.8);
        message.append("¡").append(pokemon.getName()).append(" guarda rencor!");
    }

    private void applyImprisonEffect(Pokemon pokemon, StringBuilder message) {
        pokemon.setProtected(true);
        message.append("¡").append(pokemon.getName()).append(" se Protege!");
    }

    /**
     * Verifica si el Pokémon es inmune a un estado específico
     * @return true si el Pokémon es inmune, false en caso contrario
     */
    public boolean isImmune( Pokemon target) {
        if ( this.getType() == null) {
            return false;
        }

        String stateType = String.valueOf(this.getType());
        String pokemonType = target.type.trim().toUpperCase();

        switch (stateType) {
            case "PARALYSIS":
                // Pokémon Eléctricos son inmunes a parálisis
                return pokemonType.equalsIgnoreCase("ELECTRIC");

            case "POISON":
                return pokemonType.equalsIgnoreCase("POISON");
            case "BAD_POISON":
                // Pokémon de tipo Veneno y Acero son inmunes a envenenamiento
                return pokemonType.equalsIgnoreCase("POISON") || pokemonType.equals("STEEL");

            case "BURN":
                // Pokémon de tipo Fuego son inmunes a quemaduras
                return pokemonType.equalsIgnoreCase("FIRE");

            case "FREEZE":
                // Pokémon de tipo Hielo son inmunes a congelación
                return pokemonType.equalsIgnoreCase("ICE");

            case "SLEEP":
                return false;

            default:
                return false;
        }
    }
}