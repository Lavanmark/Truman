package main.java.truman;


import main.java.World;

public class TrumanAI extends Truman {
	
	// used for seeking
	private int goalX = -1;
	private int goalY = -1;
	
	// store computed value of being in each state (x, y)
	protected double[][] Vs;
	
	
	public TrumanAI(int worldSizeX, int worldSizeY){
		super(worldSizeX,worldSizeY);
		
		Vs = new double[worldSizeX][worldSizeY];
		
		// INITIALIZE VALUE ARRAY
		for (int y = 0; y < worldSizeY; y++) {
			for (int x = 0; x < worldSizeX; x++) {
				Vs[x][y] = 0.0;
			}
		}
	}
	
	
	/* ******************************************************
	*
	*
	*               DECISION MAKING METHODS
	*
	*
	* ****************************************************** */
	
	
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
		
		//TODO update this so if you can't find food and you're thirsty, you find water.
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
		} else if(currentTiredness > MAX_TIREDNESS/3/2){
			setState(Acts.SLEEP);
		} else if(currentVariety < MAX_VARIETY/2){
			setState(Acts.EXPLORE);
		}
	}
	
	
	
	
	
	/* ******************************************************
	 *
	 *
	 *               SEEKING METHODS
	 *
	 *
	 * ****************************************************** */
	
	
	
	protected void seek(int x, int y){
		// Note: not so smart yet...
		//TODO make this/goToLocation evaluate risks in its pathing (i.e. snakes)
		goalX = x;
		goalY = y;
		setState(Acts.SEEKING);
	}
	
	protected void goToGoalLocation(){
		int xDist = Math.abs(goalX - getX());
		int yDist = Math.abs(goalY - getY());
		
		if(xDist > 1 && goalX != getX() && xDist > yDist){
			currentLocationX += goalX > getX() ? 1 : -1;
		}else if( yDist > 1 && goalY != getY()){
			currentLocationY += goalY > getY() ? 1 : -1;
		} else {
			goalX = -1;
			goalY = -1;
			setState(Acts.NO_ACTION);
		}
	}
	
	
	
	
	/* ******************************************************
	 *
	 *
	 *                  EXPLORING METHODS
	 *
	 *
	 * ****************************************************** */
	
	
	
	
	@Override
	public void explore() {
		// when he doesnt have food
		valueIteration();
		
		// 0. each square is one value
		// 1. calc valueIteration to find the path that will lead to the most unknown spaces
		// 2. pick max
		
		// take into variety ?
		
		//TODO walks around to places he doesn't know or places he might forget and tries to learn
		//TODO this is where we should add some value iteration stuff maybe
		int move = Math.abs(random.nextInt()%TOTAL_MOVES);
		
		switch(move){
			case MOVE_NORTH:
				if(currentLocationY + 1 < mapMemory[0].length)
					currentLocationY += 1;
				break;
			case MOVE_SOUTH:
				if(currentLocationY - 1 > -1)
					currentLocationY -= 1;
				break;
			case MOVE_EAST:
				if(currentLocationX + 1 < mapMemory.length)
					currentLocationX += 1;
				break;
			case MOVE_WEST:
				if(currentLocationX - 1 > -1)
					currentLocationX -= 1;
				break;
		}
	}
	
	
	
	
	/* ******************************************************
	 *
	 *
	 *               VALUE ITERATION METHODS
	 *
	 *
	 * ****************************************************** */
	
	
	
	
	public double calcSpaceUtility(int move) {
		// currentLocationX currentLocationY
		return 0.0;
	}
	
	public void calcMovementUtilities() {
		// if not abyss, he can see
		// mapMemory;
		
		// might forget so go back and revisit to keep it in memory
		// mapMemoryStrength;
		
		// double[][] mapUtils = new double[mapSize][mapSize];
		return;
	}
	
	private double getValue(int x, int y, double priorResults) {
		double discountValue = .95;
		
		if (mapMemory[x][y] == World.ABYSS) {
			return discountValue * World.ABYSS_VALUE + priorResults;
		}
		
		// TODO grass?
		
		if (mapMemory[x][y] == World.APPLE_TREE) {
			return discountValue * World.APPLE_TREE_VALUE + priorResults;
		}
		
		if (mapMemory[x][y] == World.BUSH) {
			return discountValue * World.BUSH_VALUE + priorResults;
		}
		
		if (mapMemory[x][y] == World.ROCK) {
			return World.ROCK_VALUE;
		}
		
		if (mapMemory[x][y] == World.SNAKE) {
			return discountValue * World.SNAKE_VALUE + priorResults;
		}
		
		if (mapMemory[x][y] == World.WATER) {
			return discountValue * World.WATER_VALUE + priorResults;
		}
		
		return discountValue * Vs[x][y] + priorResults;
	}
	
	private double iterate(double priorResults, int lastX, int lastY) {
		
		if (mapMemory[lastX][lastY] == World.APPLE_TREE) {
			return World.APPLE_TREE_VALUE;
		}
		
		if (mapMemory[lastX][lastY] == World.BUSH) {
			return World.BUSH_VALUE;
		}
		
		if (mapMemory[lastX][lastY] == World.ROCK) {
			return World.ROCK_VALUE;
		}
		
		if (mapMemory[lastX][lastY] == World.SNAKE) {
			return World.SNAKE_VALUE;
		}
		
		if (mapMemory[lastX][lastY] == World.WATER) {
			return World.WATER_VALUE;
		}
		
		priorResults += World.GRASS_VALUE;
		
		// TODO check +1/-1
		
		int northX = lastX;
		int northY = lastY + 1;
		int southX = lastX;
		int southY = lastY - 1;
		int eastX = lastX + 1;
		int eastY = lastY;
		int westX = lastX - 1;
		int westY = lastY;
		
		double bestValue = Double.MIN_VALUE;
		
		// check north
		if (northX > 0 && northX < mapSizeX && northY > 0 && northY < mapSizeY && mapMemory[northX][northY] != World.ROCK) {
			double value = getValue(northX, northY, priorResults);
			if (value > bestValue) {
				bestValue = value;
			}
		}
		
		// check south
		if (southX > 0 && southX < mapSizeX && southY > 0 && southY < mapSizeY && mapMemory[southX][southY] != World.ROCK) {
			double value = getValue(southX, southY, priorResults);
			if (value > bestValue) {
				bestValue = value;
			}
		}
		
		// check east
		if (eastX > 0 && eastX < mapSizeX && eastY > 0 && eastY < mapSizeY && mapMemory[eastX][eastY] != World.ROCK) {
			double value = getValue(eastX, eastY, priorResults);
			if (value > bestValue) {
				bestValue = value;
			}
		}
		
		// check west
		if (westX > 0 && westX < mapSizeX && westY > 0 && westY < mapSizeY && mapMemory[westX][westY] != World.ROCK) {
			double value = getValue(westX, westY, priorResults);
			if (value > bestValue) {
				bestValue = value;
			}
		}
		
		return bestValue;
	}
	
	private void valueIteration() {
		// INITIALIZE VALUE ARRAY
		for (int x = 0; x < mapSizeX; x++) {
			for (int y = 0; y < mapSizeY; y++) {
				Vs[x][y] = 0.0;
			}
		}
		
		int steps = 0;
		double biggestChange = 1;
		
		while (biggestChange > .000001) {
			biggestChange = 0.0;
			for (int y = 0; y < mapSizeY; y++) {
				for (int x = 0; x < mapSizeX; x++) {
					if (mapMemory[x][y] == World.ROCK || mapMemory[x][y] == World.ABYSS) {
						continue;
					}
					
					double last = Vs[x][y];
					Vs[x][y] = iterate(0.0, x, y);
					double change = Math.abs(last - Vs[x][y]);
					
					if (change > biggestChange) {
						biggestChange = change;
					}
				}
			}
			steps++;
		}
		
		System.out.println("ITERATION TOOK " + steps + " STEPS.");
		
		String center = Integer.toString((int) Vs[currentLocationX][currentLocationY]);
		String north = currentLocationY+1 >= 0 && currentLocationY+1 <= mapSizeY-1 ? Integer.toString((int) Vs[currentLocationX][currentLocationY+1]) : "-";
		String south = currentLocationY-1 >= 0 && currentLocationY-1 <= mapSizeY-1 ? Integer.toString((int) Vs[currentLocationX][currentLocationY-1]) : "-";
		String east = currentLocationX+1 >= 0 && currentLocationX+1 <= mapSizeX-1 ? Integer.toString((int) Vs[currentLocationX+1][currentLocationY]) : "-";
		String west = currentLocationX-1 >= 0 && currentLocationX-1 <= mapSizeX-1 ? Integer.toString((int) Vs[currentLocationX-1][currentLocationY]) : "-";
		
		System.out.println("\t" + north + "\n" + west + "\t" + center + "\t" + east + "\n\t" + south);
		
	}
	
	public double[][] getValueIterationData() {
		return Vs;
	}
	
}