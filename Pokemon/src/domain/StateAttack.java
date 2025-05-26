package domain;

import java.io.Serializable;

/**
 * Representa un ataque que aplica efectos de estado o modificaciones
 * a las estadísticas de un Pokémon. Puede tener efectos positivos (mejoras)
 * o negativos (estados alterados) y puede afectar al usuario o al objetivo.
 */
public class StateAttack extends Attack implements Serializable {

    private String stateName;
    private int effectValue;
    private boolean affectsSelf;
    private boolean isPersistent;

    /**
     * Constructor para crear un ataque de estado
     * @param idInside ID interno del ataque
     * @param infoAttack Información básica del ataque [nombre, tipo, poder, precision, pp, etc.]
     * @param infoState Información del estado [nombre, efecto, duración, etc.]
     * @throws POOBkemonException Si hay error en la creación
     */
    public StateAttack(int idInside, String[] infoAttack, String[] infoState) throws POOBkemonException {
        super(idInside, infoAttack);

        if (infoState == null || infoState.length < 3) {
            if (infoState == null){
                throw new POOBkemonException("Informacion de estado nula");
            }
            throw new POOBkemonException("Información de estado inválida");

        }
        this.stateName = infoState[0];
        this.effectValue = Integer.parseInt(infoState[1]);
        this.affectsSelf = infoAttack.length > 8 && infoAttack[8].equalsIgnoreCase("ally");
        this.isPersistent = Boolean.parseBoolean(infoState[2]);
    }

    // Getters
    public String getState() { return this.stateName; }
    /**
     * Obtiene información completa del ataque
     * @return Array con toda la información
     */
    @Override
    public String[] getInfo() {
        String[] baseInfo = super.getInfo();
        String[] fullInfo = new String[baseInfo.length + 4];

        System.arraycopy(baseInfo, 0, fullInfo, 0, baseInfo.length);

        fullInfo[baseInfo.length] = this.stateName;
        fullInfo[baseInfo.length + 1] = String.valueOf(this.effectValue);
        fullInfo[baseInfo.length + 2] = String.valueOf(this.affectsSelf);
        fullInfo[baseInfo.length + 3] = String.valueOf(this.isPersistent);

        return fullInfo;
    }
    public boolean affectsSelf(){
        return this.affectsSelf;
    }
}