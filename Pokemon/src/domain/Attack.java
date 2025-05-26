package domain;


import java.io.Serializable;

/**
 * Represents an attack with specific properties such as name, type, power, accuracy, and power points (PP).
 * This class is used to model an action that can be performed in a battle, with details about its usage and effects.
 * It supports serialization*/
public class Attack implements Serializable {

	private String name;

	private String type;

	private int power;

	private int presition;

	private int ppMax;
	protected int ppActual;

	private int idCSV;

	private int idInside;

	private String description;

	/**
	 * Constructs an instance of an Attack with the specified id and information.
	 * The information is parsed from the provided array to initialize the attack's properties.
	 *
	 * @param idInside The internal ID of the attack within the system.
	 * @param info A String array containing the attack's details in the following order:
	 *             [0] - The CSV ID of the attack (numeric).
	 *             [1] - The name of the attack.
	 *             [2] - The description of the attack.
	 *             [3] - The type of the attack.
	 *             [5] - The power of the attack (numeric).
	 *             [6] - The precision of the attack (numeric).
	 *             [7] - The maximum power points (PP) for the attack (numeric).
	 * @throws POOBkemonException If the information array is incomplete, has invalid formatting, or if another unexpected error occurs.
	 */
	public Attack(int idInside, String[] info) throws POOBkemonException {
		try {
			this.idInside = idInside;
			this.idCSV = Integer.parseInt(info[0]);
			this.name = info[1];
			this.type = info[3];
			this.power = Integer.parseInt(info[5]);
			this.presition = Integer.parseInt(info[6]);
			this.ppMax = Integer.parseInt(info[7]);
			this.ppActual = this.ppMax;
			this.description = info[2];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new POOBkemonException("Error al crear ataque: información incompleta en el array" + e.getMessage());
		} catch (NumberFormatException e) {
			throw new POOBkemonException("Error al crear ataque: formato numérico inválido" + e.getMessage());
		} catch (Exception e) {
			throw new POOBkemonException("Error inesperado al crear ataque" + e.getMessage());
		}
	}

	/**
	 * Decreases the current power points (PP) of the attack by 1. This method is intended to track
	 * the*/
	public void usePP(){
		this.ppActual--;
	}

	/**
	 * Retrieves an array of strings containing information about the attack.
	 * The information includes the name, type, power, precision, current PP, maximum PP,
	 * CSV ID, description, and internal ID of the attack.
	 *
	 * @return A String array containing the following details about the attack:
	 *         [0] - Name of the attack.
	 *         [1] - Type of the attack.
	 *         [2] - Power of the attack.
	 *         [3] - Precision of the attack.
	 *         [4] - Current PP of the attack.
	 *         [5] - Maximum PP of the attack.
	 *         [6] - CSV ID of the attack.
	 *         [7] - Description of the attack.
	 *         [8] - Internal ID of the attack within the game.
	 */
	public String[] getInfo() {
		String[] info = new String[9];
		info[0] = this.name;               // Nombre del ataque
		info[1] = this.type;               // Tipo del ataque
		info[2] = String.valueOf(this.power);      // Poder del ataque
		info[3] = String.valueOf(this.presition);  // Precisión del ataque
		info[4] = String.valueOf(this.ppActual);         // Puntos de poder (PP)
		info[5] = String.valueOf(this.ppMax);         // Puntos de poder máximos (PP)
		info[6] = String.valueOf(this.idCSV);         // ID del ataque en el CSV
		info[7] = this.description;        // Descripción del ataque
		info[8] = String.valueOf(this.idInside); //Id interno del juego
		return info;
	}

	/**
	 * Returns a string representation of the Attack object, including its name, type, power,
	 * accuracy percentage, current PP, and maximum PP.
	 *
	 * @return A formatted string representing the details of the attack.
	 */
	@Override
	public String toString() {
		return String.format("%s (Type: %s, Power: %d, Accuracy: %d%%, PP: %d/%d)",
				name, type, power, presition, ppActual, ppMax);
	}

	/**
	 * Retrieves the internal identifier of the attack within the system.
	 *
	 * @return The internal ID of the attack.
	 */
	public int getIdInside() {
		return this.idInside;
	}
	public int getIdCSV(){
		return this.idCSV;
	}

	public int getPPActual(){
		return this.ppActual;
	}
	public int getPPMax(){
		return this.ppMax;
	}
	public int getAccuracy() {
		return this.presition;
	}
	public String getName() {
		return this.name;
	}
	public String getType() {
		return this.type;
	}
	public void setPPActual(int ppActual) {
		this.ppActual = ppActual;
	}

    public int getPower() {
		return this.power;
    }
}
