package domain;

public abstract class Machine extends Trainer {
	public Machine(int id, BagPack bagPack) throws POOBkemonException {
		super(id, bagPack);
	}

	public abstract String[] machineMovement( POOBkemon game)  throws POOBkemonException;
}
