import java.util.Random;

public class Truman implements ITruman{
	
	private int currentHealth = MAX_HEALTH;
	
	private int currentLove = MAX_LOVE;
	private int currentImportance = MAX_IMPORTANCE;
	private int currentTiredness = NO_TIREDNESS;
	
	private int[][] mapMemory; // Stores the portions of the world that Truman knows
	private int[][] mapMemoryStrength; // Stores the strength of a particular value on the map, once it is 0, it is removed from the memory.
	
	private final int APPLE_INDEX = 0;
	private final int BERRY_INDEX = 1;
	private final int WOOD_INDEX = 2;
	private int[] inventory = new int[3]; //0 = apples, 1 = berries, 2 = wood
	
	private final int HUNGER_HURT = 5;
	private final int HUNGER_UPDATE_TIME = 5;
	private int hungerCountdown = HUNGER_UPDATE_TIME;
	private int currentHunger = NO_HUNGER;
	
	private final int THIRST_HURT = 2;
	private final int THIRST_UPDATE_TIME = 5;
	private int thirstCountdown = THIRST_UPDATE_TIME;
	private int currentThirst = NO_THIRST;
	
	
	private enum Acts {NO_ACTION,EAT,DRINK,SLEEP}
	private Acts currentAction = Acts.NO_ACTION;
	
	public Truman(int worldSizeX, int worldSizeY){
		mapMemory = new int[worldSizeX][worldSizeY];
		mapMemoryStrength = new int[worldSizeX][worldSizeY];
	}
	
	public void update() throws TrumanDiedException {
		if(currentAction == Acts.SLEEP){
		
		} else {
		
		}
		updateHunger();
		updateThirst();
		
	}
	
	private void updateHunger() throws TrumanDiedException {
		hungerCountdown--;
		if(hungerCountdown <= 0){
			if(currentHunger == MAX_HUNGER) {
				currentHealth -= 5;
				if(!checkAlive()){
					throw new TrumanDiedException("Truman died of Hunger.");
				}
			}else{
				currentHunger++;
			}
			hungerCountdown = HUNGER_UPDATE_TIME;
		}
	}
	
	private void updateThirst() throws TrumanDiedException {
		thirstCountdown--;
		if(thirstCountdown <= 0){
			if(currentThirst == MAX_THIRST) {
				currentHealth -= 5;
				if(!checkAlive()){
					throw new TrumanDiedException("Truman died of Thirst.");
				}
			}else{
				currentThirst++;
			}
			thirstCountdown = THIRST_UPDATE_TIME;
		}
	}
	
	//private void update
	
	private boolean checkAlive(){
		return currentHealth > 0;
	}
	
	@Override
	public boolean sleep() {
		Random random = new Random();
		int sleepFor = random.nextInt()%8;
		
		return false;
	}
	
	@Override
	public void explore() {
	
	}
	
	@Override
	public void stayPut() {
	
	}
	
	@Override
	public boolean forage() {
		return false;
	}
	
	@Override
	public boolean buildFire() {
		return false;
	}
	
	@Override
	public boolean cook() {
		return false;
	}
	
	@Override
	public boolean eat() {
		return false;
	}
	
	@Override
	public boolean buildShelter() {
		return false;
	}
	
	@Override
	public boolean cutDownTree() {
		//TODO this one would search the surrounding 8 blocks for a tree to cut down then ask the world to remove it
		return false;
	}
	
	@Override
	public void expressThoughts() {
		
		System.out.println("");
	}
}
