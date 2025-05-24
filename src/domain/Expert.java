package domain;

public class Expert extends Machine {

	public Expert(int id, BagPack bagPack) throws POOBkemonException {
        super(id,bagPack);
	}
	@Override
	public String[] machineMovement( POOBkemon game)  throws POOBkemonException {
		return null;
	}

}
