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
		
		//smartDecisions();
		
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
	
	private int valueState(int health, int variety, int hunger, int thirst, int tiredness){
		int total = variety;
		total -= hunger;
		total -= thirst;
		total += health;
		total -= tiredness;
		return total;
	}
	
	private void smartDecisions(int health, int variety, int hunger, int thirst, int tiredness){
		int curValue = valueState(health, variety, hunger, thirst, tiredness);
		if(hunger > MAX_HUNGER/2){
		
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

		// if(xDist > 1 && goalX != getX() && xDist > yDist){
		// 	currentLocationX += goalX > getX() ? 1 : -1;
		// }else if( yDist > 1 && goalY != getY()){
		// 	currentLocationY += goalY > getY() ? 1 : -1;
		// } else {
		// 	goalX = -1;
		// 	goalY = -1;
		// 	setState(Acts.NO_ACTION);
		// }
        
        if ((xDist > 1 && goalX != getX() && xDist > yDist) || (yDist > 1 && goalY != getY())) {
            explore();
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
		
		// TODO take into account variety?
		// TODO walks around to places he doesn't know or places he might forget and tries to learn
        
        int northX = currentLocationX;
		int northY = currentLocationY + 1;
		int southX = currentLocationX;
		int southY = currentLocationY - 1;
		int eastX = currentLocationX + 1;
		int eastY = currentLocationY;
		int westX = currentLocationX - 1;
		int westY = currentLocationY;
		
        int bestMove = Truman.MOVE_STAY;
        double bestValue = Double.MIN_VALUE;
		
		// check north
		if (northX > 0 && northX < mapSizeX && northY > 0 && northY < mapSizeY && mapMemory[northX][northY] == World.GRASS) {
            double value = Vs[northX][northY];
            if (value > bestValue) {
                bestMove = Truman.MOVE_NORTH;
				bestValue = value;
			}
		}
		
		// check south
		if (southX > 0 && southX < mapSizeX && southY > 0 && southY < mapSizeY && mapMemory[southX][southY] == World.GRASS) {
            double value = Vs[southX][southY];
            if (value > bestValue) {
                bestMove = Truman.MOVE_SOUTH;
				bestValue = value;
			}
		}
		
		// check east
		if (eastX > 0 && eastX < mapSizeX && eastY > 0 && eastY < mapSizeY && mapMemory[eastX][eastY] == World.GRASS) {
            double value = Vs[eastX][eastY];
            if (value > bestValue) {
                bestMove = Truman.MOVE_EAST;
				bestValue = value;
			}
		}
		
		// check west
		if (westX > 0 && westX < mapSizeX && westY > 0 && westY < mapSizeY && mapMemory[westX][westY] == World.GRASS) {
            double value = Vs[westX][westY];
            if (value  > bestValue) {
                bestMove = Truman.MOVE_WEST;
				bestValue = value;
			}
        }
        
		switch(bestMove){
            case Truman.MOVE_NORTH:
                System.out.println("MOVED NORTH");
				currentLocationY += 1;
				break;
            case Truman.MOVE_SOUTH:
            System.out.println("MOVED SOUTH");
                currentLocationY -= 1;
				break;
            case Truman.MOVE_EAST:
            System.out.println("MOVED EAST");
				currentLocationX += 1;
				break;
            case Truman.MOVE_WEST:
                System.out.println("MOVED WEST");
				currentLocationX -= 1;
                break;
            default:
                System.out.println("CHOSE TO STAY");
		}
	}
	
	
	
	
	/* ******************************************************
	 *
	 *
	 *               VALUE ITERATION METHODS
	 *
	 *
	 * ****************************************************** */

    private final double GOAL_VALUE = 50;
	
	private double getValue(int x, int y, double priorResults) {
        double discountValue = .95;
        
        if (x == goalX && y == goalY) {
            return GOAL_VALUE;
        }
		
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

        if (lastX == goalX && lastY == goalY) {
            return GOAL_VALUE;
        }
		
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
