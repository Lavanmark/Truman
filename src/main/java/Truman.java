import java.util.Random;

public class Truman implements ITruman{
	
	private Random random = new Random();
	
	private int currentLocationX,currentLocationY;
	private enum Move {STAY, NORTH, SOUTH, EAST, WEST}
	
	private int currentHealth = MAX_HEALTH;
	private final int HEALTH_REGEN = 5;
	
	private int currentLove = MAX_LOVE; //TODO do something with this...
	
	private int currentImportance = MAX_IMPORTANCE; //TODO do something with this...
	
	private int currentVariety = MAX_VARIETY;
	
	private int[][] mapMemory; // Stores the portions of the world that Truman knows
	private int[][] mapMemoryStrength; // Stores the strength of a particular value on the map, once it is 0, it is removed from the memory.
	private int viewRadius = 5;
	
	private final int APPLE_INDEX = 0;
	private final int BERRY_INDEX = 1;
	private final int WOOD_INDEX = 2;
	private final int WATER_INDEX = 3;
	private int[] inventory = new int[4]; //0 = apples, 1 = berries, 2 = wood
	
	private final int MAX_WATER_STORAGE = 5;
	private final int MAX_APPLE_STORAGE = 5;
	private final int MAX_BERRY_STORAGE = 20;
	
	private final int HUNGER_HURT = 5;
	private final int HUNGER_UPDATE_TIME = 5;
	private int hungerCountdown = HUNGER_UPDATE_TIME;
	private int currentHunger = NO_HUNGER;
	
	private final int THIRST_HURT = 2;
	private final int THIRST_UPDATE_TIME = 5;
	private int thirstCountdown = THIRST_UPDATE_TIME;
	private int currentThirst = NO_THIRST;
	
	private final int AWAKE_COST = 1;
	private final int SLEEP_VALUE = 5;
	private final int MIN_SLEEP = 2;
	private int sleepLength = 0;
	private int currentTiredness = NO_TIREDNESS;
	
	public enum Acts {NO_ACTION, EAT, DRINK, SLEEP, WAKE_UP, EXPLORE, FORAGE, COLLECT_WATER, SEEKING}
	private Acts currentAction = Acts.NO_ACTION;
	private Acts nextAction = Acts.NO_ACTION;
	
	
	public Truman(int worldSizeX, int worldSizeY) {
		mapMemory = new int[worldSizeX][worldSizeY];
		mapMemoryStrength = new int[worldSizeX][worldSizeY];
		currentLocationX = worldSizeX/2;
		currentLocationY = worldSizeY/2;
	}
	
	public void update() throws TrumanDiedException {
		updateVariety();
		
		doAction(); // is this the right place for this and is the update in the right place?
		
		updateSleep();
		updateHunger();
		updateThirst();
		updateMemory();
		updateHealth();
		currentAction = nextAction; //Note: this will cause truman to keep doing the same thing until he changes actions.
	}
	
	private void updateVariety() throws TrumanDiedException {
		if(currentAction == nextAction && currentAction != Acts.SLEEP){
			currentVariety--;
		} else {
			int varietyBoost = Math.abs(random.nextInt()%10);
			currentVariety += Math.max(varietyBoost, MIN_VARIETY_BOOST);
			if(currentVariety > MAX_VARIETY){
				currentVariety = MAX_VARIETY;
			}
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
			sleepLength--;
			if(sleepLength <= 0){
				setState(Acts.WAKE_UP);
				sleepLength = 0;
			}
		} else {
			currentTiredness += AWAKE_COST;
			if(currentTiredness > MAX_TIREDNESS){
				sleep(); // too tired? force him to sleep.
				setState(Acts.SLEEP);
			} else if(currentTiredness > MAX_TIREDNESS*.66){
				viewRadius = 2;
			} else if(currentTiredness > MAX_TIREDNESS*.33){
				viewRadius = 3;
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
	
	private void updateMemory() {
		int memloss = Math.abs(random.nextInt()%100);
		if(memloss < 50){
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
		if(currentHealth < MAX_HEALTH) {
			if(currentHunger < MAX_HUNGER / 2 && currentThirst < MAX_THIRST / 2 && currentTiredness < MAX_TIREDNESS/3){
				currentHealth += HEALTH_REGEN;
			}
		}
	}
	
	private boolean checkDead() {
		return currentHealth <= 0;
	}
	
	private boolean setState(Acts state) {
		if((currentAction == Acts.SLEEP  && state != Acts.WAKE_UP) || (nextAction == Acts.SLEEP && sleepLength > 0)){
			return false;
		}
		nextAction = state;
		return true;
	}
	
	private void doAction(){
		//TODO does the currentAction at the start of update funciton.
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
				stayPut();
				break;
			case SEEKING:
				System.out.println("Seeking!");
				goToGoalLocation();
				break;
		}
	}
	
	@Override
	public void makeDecision() {
		//TODO this is where we will figure out everything.
		if(currentAction == Acts.SEEKING || (goalX != -1 && goalY != -1)){
			setState(Acts.SEEKING);
			return;
		}
		setState(Acts.EXPLORE);
		double hungerRatio = ((double)currentHunger/(double)MAX_HUNGER);
		double thristRatio = ((double)currentThirst/(double)MAX_THIRST);
		if(hungerRatio >= thristRatio && (currentHunger > MAX_HUNGER/2 ||
				(((inventory[APPLE_INDEX] + inventory[BERRY_INDEX] < 1) ||
						inventory[APPLE_INDEX]+inventory[BERRY_INDEX] < inventory[WATER_INDEX]) && currentThirst < MAX_THIRST/2))){
			if(inventory[APPLE_INDEX] < 1 && inventory[BERRY_INDEX] < 1){
				setState(Acts.FORAGE);
			} else {
				setState(Acts.EAT);
			}
		} else if(currentThirst > MAX_THIRST/2 || inventory[WATER_INDEX] < 1){
			if(inventory[WATER_INDEX] < 1){
				setState(Acts.COLLECT_WATER);
			} else {
				setState(Acts.DRINK);
			}
		} else if(currentTiredness > MAX_TIREDNESS/3){
			setState(Acts.SLEEP);
		} else if(currentVariety < MAX_VARIETY/2){
			setState(Acts.NO_ACTION);
		}
	}
	
	private boolean haveSeenTree(){
		for(int[] ints : mapMemory) {
			for(int anInt : ints) {
				if(anInt == World.APPLE_TREE) {
					return true;
				}
			}
		}
		return false;
	}
	private boolean haveSeenBush(){
		for(int[] ints : mapMemory) {
			for(int anInt : ints) {
				if(anInt == World.BUSH) {
					return true;
				}
			}
		}
		return false;
	}
	private boolean haveSeenWater(){
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
		double closeDistance = Double.MAX_VALUE;
		for(int x = 0; x < mapMemory.length; x++){
			for(int y = 0; y < mapMemory[x].length; y++){
				if(mapMemory[x][y] == World.WATER){
					double dist = Math.sqrt(Math.pow(x-getX(), 2) + Math.pow(y - getY(), 2));
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
	
	@Override
	public boolean sleep() {
		int sleepFor = Math.max(Math.abs(random.nextInt()%8), MIN_SLEEP); // Max 8 hours of sleep
		
		System.out.println("Truman to sleep for " + sleepFor + " time steps.");
		sleepLength = sleepFor;
		return sleepLength > 0;
	}
	
	@Override
	public void explore() {
		//TODO walks around to places he doesn't know or places he might forget and tries to learn
		//TODO this is where we should add some value iteration stuff maybe
		int dirNum = Math.abs(random.nextInt()%5);
		Move move = Move.STAY;
		switch(dirNum){
			case 0:
				move = Move.STAY;
				break;
			case 1:
				move = Move.NORTH;
				break;
			case 2:
				move = Move.SOUTH;
				break;
			case 3:
				move = Move.EAST;
				break;
			case 4:
				move = Move.WEST;
				break;
		}
		switch(move){
			case NORTH:
				if(currentLocationY + 1 < mapMemory[0].length)
					currentLocationY += 1;
				break;
			case SOUTH:
				if(currentLocationY - 1 > -1)
					currentLocationY -= 1;
				break;
			case EAST:
				if(currentLocationX + 1 < mapMemory.length)
					currentLocationX += 1;
				break;
			case WEST:
				if(currentLocationX - 1 > -1)
					currentLocationX -= 1;
				break;
		}
	}
	
	private int goalX = -1;
	private int goalY = -1;
	
	private void goToGoalLocation(){
		if(goalX != getX()){
			currentLocationX += goalX > getX() ? 1 : -1;
		}else if(goalY != getY()){
			currentLocationY += goalY > getY() ? 1 : -1;
		} else {
			goalX = -1;
			goalY = -1;
			setState(Acts.NO_ACTION);
		}
	}
	
	@Override
	public void stayPut() {
		//TODO idk what to do here lol...
	}
	
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
	
	private void seek(int x, int y){
		goalX = x;
		goalY = y;
		setState(Acts.SEEKING);
	}
	
	@Override
	public boolean forage() {
		//TODO basically he looks over his memory and finds a tree/bush and goes to find it.
		//TODO if there are no bushes in his memory then he explores?
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
	public void expressThoughts() {
		
		System.out.println("");
	}
	
	@Override
	public void addViewToMemory(int[][] worldPortion){
		for(int x = 0; x < worldPortion.length; x++){
			for(int y = 0; y < worldPortion[x].length; y++){
				if(worldPortion[x][y] != World.ABYSS){
					if(mapMemory[x][y] == worldPortion[x][y]){
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
}
