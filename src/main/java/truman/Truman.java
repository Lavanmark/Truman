package main.java.truman;

import main.java.World;

import java.util.Arrays;
import java.util.Random;

public abstract class Truman implements ITruman{
	
	protected Random random = new Random(System.currentTimeMillis());
	
	// AGE/TIME
	protected int currentAge = 0; // stored as time steps (basically hours...)
	
	// MOVEMENT
	protected int currentLocationX,currentLocationY;
	public static final int MOVE_STAY = 0;
	public static final int MOVE_NORTH = 1;
	public static final int MOVE_SOUTH = 2;
	public static final int MOVE_EAST = 3;
	public static final int MOVE_WEST = 4;
	public static final int TOTAL_MOVES = 5;
	
	// HEALTH
	protected int currentHealth = MAX_HEALTH;
	protected final int HEALTH_REGEN = 5;
	
	// OTHER STATS
	protected int currentLove = MAX_LOVE; //TODO do something with this...
	protected int currentImportance = MAX_IMPORTANCE; //TODO do something with this...
	
	// VARIETY
	protected int currentVariety = MAX_VARIETY;
	
	// MEMORY
	protected int[][] mapMemory; // Stores the portions of the world that Truman knows
	protected int[][] mapMemoryStrength; // Stores the strength of a particular value on the map, once it is 0, it is removed from the memory.
	protected final int MEMORY_LOSS_CHANCE = 10;
    protected int viewRadius = 5;
	protected int mapSizeX = 0;
	protected int mapSizeY = 0;
    
    // INVENTORY
	protected final int APPLE_INDEX = 0;
	protected final int BERRY_INDEX = 1;
	protected final int WOOD_INDEX = 2;
	protected final int WATER_INDEX = 3;
	protected int[] inventory = new int[4]; //0 = apples, 1 = berries, 2 = wood
	
	protected final int MAX_WATER_STORAGE = 5;
	protected final int MAX_APPLE_STORAGE = 5;
	protected final int MAX_BERRY_STORAGE = 20;
	
	// HUNGER
	protected final int HUNGER_HURT = 5;
	protected final int HUNGER_UPDATE_TIME = 10;
	protected int hungerCountdown = HUNGER_UPDATE_TIME;
	protected int currentHunger = NO_HUNGER;
	
	// THIRST
	protected final int THIRST_HURT = 2;
	protected final int THIRST_UPDATE_TIME = 10;
	protected int thirstCountdown = THIRST_UPDATE_TIME;
	protected int currentThirst = NO_THIRST;
	
	// SLEEP
	protected final int AWAKE_COST = 1;
	protected final int SLEEP_VALUE = 5;
	protected final int MIN_SLEEP = 2;
	protected int sleepLength = 0;
	protected int currentTiredness = NO_TIREDNESS;
	
	// ACTIONS
	public enum Acts {NO_ACTION, EAT, DRINK, SLEEP, WAKE_UP, EXPLORE, FORAGE, COLLECT_WATER, SEEKING}
	protected Acts currentAction = Acts.NO_ACTION;
    protected Acts nextAction = Acts.NO_ACTION;
    
   
	
	public Truman(int worldSizeX, int worldSizeY) {
		mapMemory = new int[worldSizeX][worldSizeY];
		mapMemoryStrength = new int[worldSizeX][worldSizeY];
		currentLocationX = worldSizeX/2;
        currentLocationY = worldSizeY/2;
        mapSizeX = worldSizeX;
        mapSizeY = worldSizeY;
        
        for(int[] ints : mapMemory){
	        Arrays.fill(ints, World.ABYSS);
        }
        for(int[] ints : mapMemoryStrength){
        	Arrays.fill(ints, 0);
        }
	}
	
	
	/* ******************************************************
	*                   ABSTRACT METHODS
	* ****************************************************** */
	
	protected abstract void goToGoalLocation();
	protected abstract void seek(int x, int y);
	public abstract double[][] getValueIterationData();
	
	
	
	/* ******************************************************
	 *
	 *
	 *                  UPDATE METHODS
	 *
	 *
	 * ****************************************************** */
	
	
	
	
	public void update() throws TrumanDiedException {
		updateVariety();
		
		doAction(); // is this the right place for this and is the update in the right place?
		
		updateSleep();
		updateHunger();
		updateThirst();
		updateHealth();
		currentAction = nextAction; //Note: this will cause truman to keep doing the same thing until he changes actions.
	}
	
	private void updateVariety() throws TrumanDiedException {
		if(currentAction == Acts.SLEEP){
			return;
		}
		if(currentAction == nextAction){
			//currentVariety--;
		} else {
			int varietyBoost = Math.abs(random.nextInt()%MAX_VARIETY_BOOST); //TODO maybe fix this up...
			currentVariety += Math.max(varietyBoost, MIN_VARIETY_BOOST);
			if(currentVariety > MAX_VARIETY){
				currentVariety = MAX_VARIETY;
			}
		}
		int knowledgeCount = 0;
		for(int[] ints : mapMemory){
			for(int anInt : ints){
				if(anInt != World.ABYSS){
					knowledgeCount++;
				}
			}
		}
		
		if(knowledgeCount < viewRadius * viewRadius *2){
			//currentVariety -= 2;
		}
		
		if(currentVariety <= NO_VARIETY){
			throw new TrumanDiedException("Truman died of boredom.");
		}
	}
	
	private void updateSleep() {
		if(currentAction == Acts.SLEEP){
			currentTiredness -= SLEEP_VALUE;
			if(currentTiredness < 0){
				currentTiredness = 0;
			}
			//He can still sleep even after accounting for all of lost sleep. He must complete the sleep length.
			//This will provide for some risk in just always sleeping away the edge since he could die of hunger in his sleep.
			if(sleepLength <= 0){
				setState(Acts.WAKE_UP);
				sleepLength = 0;
			} else {
				setState(Acts.SLEEP);
			}
			sleepLength--;
		} else {
			currentTiredness += AWAKE_COST;
			if(currentTiredness > MAX_TIREDNESS){
				sleep(); // too tired? force him to sleep.
				setState(Acts.SLEEP);
			} else if(currentTiredness > MAX_TIREDNESS*.66){
				viewRadius = 1;
			} else if(currentTiredness > MAX_TIREDNESS*.33){
				viewRadius = 2;
			} else if(currentTiredness < MAX_TIREDNESS*.33){
				viewRadius = 5;
			}
		}
	}
	
	private void updateHunger() throws TrumanDiedException {
		hungerCountdown--;
		if(hungerCountdown <= 0){
			if(currentHunger == MAX_HUNGER) {
				currentHealth -= HUNGER_HURT;
				if(checkDead()){
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
				currentHealth -= THIRST_HURT;
				if(checkDead()){
					throw new TrumanDiedException("Truman died of Thirst.");
				}
			}else{
				currentThirst++;
			}
			thirstCountdown = THIRST_UPDATE_TIME;
		}
	}
	
	public void updateMemory() {
		int memloss = Math.abs(random.nextInt()%100);
		if(memloss < MEMORY_LOSS_CHANCE){
			for(int x = 0; x < mapMemoryStrength.length; x++){
				for(int y = 0; y < mapMemoryStrength[x].length; y++){
					if(mapMemory[x][y] == World.ABYSS){
						continue;
					}
					mapMemoryStrength[x][y] -= 1;
					if(mapMemoryStrength[x][y] <= 0){
						mapMemoryStrength[x][y] = 0;
						mapMemory[x][y] = World.ABYSS;
					}
				}
			}
		}
	}
	
	private void updateHealth(){
		if(currentHealth > MAX_HEALTH)
			currentHealth = MAX_HEALTH;
	}
	
	
	
	/* ******************************************************
	 *
	 *
	 *                   STATE METHODS
	 *
	 *
	 * ****************************************************** */
	
	
	
	protected boolean setState(Acts state) {
		if((currentAction == Acts.SLEEP  && state != Acts.WAKE_UP) || (nextAction == Acts.SLEEP && sleepLength > 0)){
			return false;
		}
		nextAction = state;
		return true;
	}
	
	protected boolean checkDead() {
		return currentHealth <= 0;
	}
	
	private void doAction(){
		switch(currentAction){
			case WAKE_UP:
				setState(Acts.NO_ACTION);
				break;
			case SLEEP:
				//Do nothing. Handled in the updateSleep function
				break;
			case EAT:
				eat();
				break;
			case DRINK:
				drink();
				break;
			case EXPLORE:
				explore();
				break;
			case FORAGE:
				forage();
				break;
			case COLLECT_WATER:
				findWater();
				break;
			case NO_ACTION:
				healthRegen();
				break;
			case SEEKING:
				System.out.println("Seeking!");
				goToGoalLocation();
				break;
		}
	}
	
	
	
	/* ******************************************************
	 *
	 *
	 *                  FORAGING METHODS
	 *
	 *
	 * ****************************************************** */
	
	
	
	@Override
	public boolean forage() {
		boolean foundTree = false;
		boolean foundBush = false;
		for(int x = -1; x <= 1; x++){
			int xmod = x + getX();
			if(xmod < mapMemory.length && xmod > -1) {
				for(int y = -1; y <= 1; y++) {
					int ymod = y + getY();
					if(ymod < mapMemory[xmod].length && ymod > -1) {
						foundTree = foundTree ? foundTree : mapMemory[xmod][ymod] == World.APPLE_TREE;
						foundBush = foundBush ? foundBush : mapMemory[xmod][ymod] == World.BUSH;
					}
				}
			}
		}
		double appleSupply = ((double)inventory[APPLE_INDEX])/((double)MAX_APPLE_STORAGE);
		double berrySupply = ((double)inventory[BERRY_INDEX])/((double)MAX_BERRY_STORAGE);
		int collected;
		if(foundTree && foundBush){
			if(appleSupply < berrySupply && appleSupply != 1) { // Less apples? pick those because you benefit more from them.
				foundTree = false; // Set so if you fail, you will explore
				collected = World.getInstance().pickApples(getX(), getY());
				if(collected > 0) {
					inventory[APPLE_INDEX] += collected;
					if(inventory[APPLE_INDEX] > MAX_APPLE_STORAGE) inventory[APPLE_INDEX] = MAX_APPLE_STORAGE;
					return true;
				}
			}
			// Failed to pick apples or just need berries more? Pick berries.
			collected = World.getInstance().pickBerries(getX(), getY());
			if(collected > 0) {
				inventory[BERRY_INDEX] += collected;
				if(inventory[BERRY_INDEX] > MAX_BERRY_STORAGE) inventory[BERRY_INDEX] = MAX_BERRY_STORAGE;
				return true;
			}
			foundBush = false; // Set so if you fail, you will explore
		}
		
		if(foundTree && appleSupply != 1){
			collected = World.getInstance().pickApples(getX(), getY());
			if(collected > 0) {
				inventory[APPLE_INDEX] += collected;
				if(inventory[APPLE_INDEX] > MAX_APPLE_STORAGE) inventory[APPLE_INDEX] = MAX_APPLE_STORAGE;
				return true;
			}
		} else if(foundBush && berrySupply != 1){
			collected = World.getInstance().pickBerries(getX(), getY());
			if(collected > 0) {
				inventory[BERRY_INDEX] += collected;
				if(inventory[BERRY_INDEX] > MAX_BERRY_STORAGE) inventory[BERRY_INDEX] = MAX_BERRY_STORAGE;
				return true;
			}
		}
		int[] treeResults = null, bushResults = null;
		if(haveSeenTree()){
			treeResults = getClosestTreeLocation();
		}
		if(haveSeenBush()) {
			bushResults = getClosestBushLocation();
		}
		if(treeResults == null && bushResults == null) {
			explore();
		} else if(treeResults == null){
			seek(bushResults[0],bushResults[1]);
		} else if(bushResults == null){
			seek(treeResults[0],treeResults[1]);
		} else {
			if(bushResults[2] < treeResults[2]){
				seek(bushResults[0], bushResults[1]);
			} else {
				seek(treeResults[0], treeResults[1]);
			}
		}
		
		return false;
	}
	
	protected boolean haveSeenTree(){
		for(int[] ints : mapMemory) {
			for(int anInt : ints) {
				if(anInt == World.APPLE_TREE) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected boolean haveSeenBush(){
		for(int[] ints : mapMemory) {
			for(int anInt : ints) {
				if(anInt == World.BUSH) {
					return true;
				}
			}
		}
		return false;
	}
	
	private int[] getClosestTreeLocation(){
		int closeX = -1;
		int closeY = -1;
		double closeDistance = Double.MAX_VALUE;
		for(int x = 0; x < mapMemory.length; x++){
			for(int y = 0; y < mapMemory[x].length; y++){
				if(mapMemory[x][y] == World.APPLE_TREE){
					double dist = Math.sqrt(Math.pow(x-getX(), 2) + Math.pow(y - getY(), 2));
					if(Math.abs(dist) < Math.abs(closeDistance)){
						closeX = x;
						closeY = y;
						closeDistance = dist;
					}
				}
			}
		}
		int[] result = new int[3];
		result[0] = closeX;
		result[1] = closeY;
		result[2] = (int)closeDistance;
		return result;
	}
	
	private int[] getClosestBushLocation(){
		int closeX = -1;
		int closeY = -1;
		double closeDistance = Double.MAX_VALUE;
		for(int x = 0; x < mapMemory.length; x++){
			for(int y = 0; y < mapMemory[x].length; y++){
				if(mapMemory[x][y] == World.BUSH){
					double dist = Math.sqrt(Math.pow(x-getX(), 2) + Math.pow(y - getY(), 2));
					if(Math.abs(dist) < Math.abs(closeDistance)){
						closeX = x;
						closeY = y;
						closeDistance = dist;
					}
				}
			}
		}
		int[] result = new int[3];
		result[0] = closeX;
		result[1] = closeY;
		result[2] = (int)closeDistance;
		return result;
	}
	
	
	
	
	/* ******************************************************
	 *
	 *
	 *                  WATER FINDING METHODS
	 *
	 *
	 * ****************************************************** */
	
	
	
	
	public boolean findWater(){
		boolean foundWater = false;
		for(int x = -1; x <= 1; x++){
			int xmod = x + getX();
			if(xmod < mapMemory.length && xmod > -1) {
				for(int y = -1; y <= 1; y++) {
					int ymod = y + getY();
					if(ymod < mapMemory[xmod].length && ymod > -1) {
						foundWater = foundWater ? foundWater : (mapMemory[xmod][ymod] == World.WATER);
					}
				}
			}
		}
		
		if(foundWater){
			inventory[WATER_INDEX] += World.getInstance().collectWater(getX(), getY());
			if(inventory[WATER_INDEX] > MAX_WATER_STORAGE) inventory[WATER_INDEX] = MAX_WATER_STORAGE;
			return true;
		}
		
		if(haveSeenWater()){
			int[] closeSource = getClosestWaterLocation();
			seek(closeSource[0], closeSource[1]);
		} else {
			explore();
		}
		
		return false;
	}
	
	protected boolean haveSeenWater(){
		for(int[] ints : mapMemory) {
			for(int anInt : ints) {
				if(anInt == World.WATER) {
					return true;
				}
			}
		}
		return false;
	}
	
	private int[] getClosestWaterLocation(){
		int closeX = -1;
		int closeY = -1;
		double closeDistance = 10000;
		for(int x = 0; x < mapMemory.length; x++){
			for(int y = 0; y < mapMemory[x].length; y++){
				if(mapMemory[x][y] == World.WATER){
					//double dist = Math.sqrt(Math.pow(x-getX(), 2) + Math.pow(y - getY(), 2));
					double dist = Math.abs(x-getX()) + Math.abs(y-getY());
					if(Math.abs(dist) < Math.abs(closeDistance)){
						closeX = x;
						closeY = y;
						closeDistance = dist;
					}
				}
			}
		}
		int[] result = new int[2];
		result[0] = closeX;
		result[1] = closeY;
		return result;
	}
	
	
	/* ******************************************************
	 *
	 *
	 *               OTHER ACTION METHODS
	 *
	 *
	 * ****************************************************** */
	
	
	@Override
	public boolean eat() {
		if(inventory[BERRY_INDEX] > 0 || inventory[APPLE_INDEX] > 0){
			if(currentHunger < World.APPLE_HUNGER_VALUE){
				// Eat berries first so we don't waste the hunger value
				if(inventory[BERRY_INDEX] > 0) {
					inventory[BERRY_INDEX] -= 1;
					currentHunger -= World.BERRY_HUNGER_VALUE;
				} else {
					inventory[APPLE_INDEX] -= 1;
					currentHunger -= World.APPLE_HUNGER_VALUE;
				}
			} else {
				// Eat an apple first if we can since it gives more hunger
				if(inventory[APPLE_INDEX] > 0) {
					inventory[APPLE_INDEX] -= 1;
					currentHunger -= World.APPLE_HUNGER_VALUE;
				} else {
					inventory[BERRY_INDEX] -= 1;
					currentHunger -= World.BERRY_HUNGER_VALUE;
				}
			}
			
			if(currentHunger < NO_HUNGER){
				currentHunger = NO_HUNGER;
			}
			hungerCountdown = HUNGER_UPDATE_TIME;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean drink() {
		if(inventory[WATER_INDEX] > 0){
			currentThirst -= World.WATER_THIRST_VALUE;
			inventory[WATER_INDEX] -= 1;
			if(currentThirst < NO_THIRST){
				currentThirst = NO_THIRST;
			}
			thirstCountdown = THIRST_UPDATE_TIME;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean sleep() {
		int sleepFor = Math.max(Math.abs(random.nextInt()%11), MIN_SLEEP); // Max 8 hours of sleep
		if(currentTiredness >= MAX_TIREDNESS){
			sleepFor = currentTiredness/SLEEP_VALUE;
		}
		
		System.out.println("Truman to sleep for " + sleepFor + " time steps.");
		sleepLength = sleepFor;
		return sleepLength > 0;
	}
	
	@Override
	public void healthRegen() {
		if(currentHealth < MAX_HEALTH) {
			if(currentHunger < MAX_HUNGER / 2 &&
					currentThirst < MAX_THIRST / 2 &&
					currentTiredness < MAX_TIREDNESS/3){
				currentHealth += HEALTH_REGEN;
			}
		}
	}
	
	@Override
	public void expressThoughts() {
		
		System.out.println("");
	}

	
	
	/* ******************************************************
	 *
	 *
	 *               GETTER SETTER METHODS
	 *
	 *
	 * ****************************************************** */
	
	
	
	
	@Override
	public void addViewToMemory(int[][] worldPortion){
		for (int x = 0; x < worldPortion.length; x++){
			for (int y = 0; y < worldPortion[x].length; y++){
				if (worldPortion[x][y] != World.ABYSS){
					if (mapMemory[x][y] == worldPortion[x][y]){
						mapMemoryStrength[x][y]++;
					} else if (mapMemory[x][y] == World.SNAKE || worldPortion[x][y] == World.SNAKE){
						mapMemory[x][y] = worldPortion[x][y];
						mapMemoryStrength[x][y]++;
					} else {
						mapMemory[x][y] = worldPortion[x][y];
						mapMemoryStrength[x][y] = 1;
					}
				}
			}
		}
	}
	
	@Override
	public void snakeBite() throws TrumanDiedException {
		currentHealth -= World.SNAKE_BITE_VALUE;
		if(checkDead()){
			throw new TrumanDiedException("Truman died of a snake bite.");
		}
	}
	
	@Override
	public int getX(){
		return currentLocationX;
	}
	@Override
	public int getY() {
		return currentLocationY;
	}
	@Override
	public int getViewRadius(){
		return viewRadius;
	}
	@Override
	public boolean isSleeping() {
		return currentAction == Acts.SLEEP;
	}
	public Acts getCurrentAction(){
		return currentAction;
	}
	public int getCurrentHealth(){
		return currentHealth;
	}
	public int getCurrentTiredness(){
		return currentTiredness;
	}
	public int getCurrentHunger(){
		return currentHunger;
	}
	public int getCurrentThirst(){
		return currentThirst;
	}
	public int getCurrentVariety(){
		return currentVariety;
	}
	public int[][] getCurrentMemory(){
		return mapMemory;
	}
	public int[][] getMemoryStrength(){
		return mapMemoryStrength;
	}
	public void growOlder(){
		currentAge++;
	}
	public int getCurrentAge(){
		return currentAge;
	}
	public int getApples(){
		return inventory[APPLE_INDEX];
	}
	public int getBerries(){
		return inventory[BERRY_INDEX];
	}
	public int getWaterStorage(){
		return inventory[WATER_INDEX];
	}
	
}
