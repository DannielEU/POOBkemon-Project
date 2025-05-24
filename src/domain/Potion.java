package domain;

public class Potion extends Item {
	private int healthPoints;

	public Potion(int number, int health) {
		super(number);
		this.healthPoints = health;
		this.createName();
	}
	@Override
	public void itemEffect(Pokemon pokemon) {
		String[] info = new String[4];
		info[0] = "Potion"; // nombre del item
		info[1] = "Heals"; // indicacion de efecto
		info[2] = Integer.toString(this.healthPoints); //puntos que da
		info[3] = "1"; //turnos del efecto
		pokemon.effect(info);
	}
	private void createName() {
		if(this.healthPoints >=1 &&this.healthPoints <= 25) this.name = "potion";
		if(this.healthPoints > 25 && this.healthPoints <= 50) this.name = "superPotion";
		if(this.healthPoints > 50 && this.healthPoints <= 100) this.name = "hyperPotion";
		if(this.healthPoints > 100) this.name = "Mega";
	}
	public String getName() {
		return this.name;
	}
	public String[] getItemInfo(){
		String[] info = new String[2];
		info[0] = ""+this.name; // nombre del item
		info[1] = ""+this.number;
		return  info;
	}
}
