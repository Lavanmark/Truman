package main.java.truman;


import main.java.World;

import java.util.ArrayList;
import java.util.EnumMap;

public class TrumanAI extends Truman {
	
	private final boolean SMART_DECISIONS = false;
	
	// used for seeking
	private int goalX = -1;
    private int goalY = -1;
    
    private double abyssDirs[] = new double[]{0.0, 0.0, 0.0, 0.0};
    private double abyssQuads[] = new double[]{0.0, 0.0, 0.0, 0.0};
	
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
		if(currentAction == Acts.SLEEP) {
			return;
		}
		
		if(SMART_DECISIONS){
			smartDecision();
			return;
		}
		
		if(currentAction == Acts.SEEKING || (goalX != -1 && goalY != -1)){
			setState(Acts.SEEKING);
			return;
		}
		setState(Acts.EXPLORE);
		
		//smartDecisions();
		
		double hungerRatio = ((double)currentHunger/(double)MAX_HUNGER);
		double thristRatio = ((double)currentThirst/(double)MAX_THIRST);
		
		//TODO update this so if you can't find food and you're thirsty, you find water.
		if(hungerRatio >= thristRatio && (currentHunger > MAX_HUNGER/5 ||
				(((inventory[APPLE_INDEX] + inventory[BERRY_INDEX] < MAX_APPLE_STORAGE) ||
						inventory[APPLE_INDEX] + inventory[BERRY_INDEX] < inventory[WATER_INDEX]) && currentThirst < MAX_THIRST/2))){
			
			if(currentHunger > MAX_HUNGER/5 && inventory[APPLE_INDEX]+inventory[BERRY_INDEX] > 0){
				setState(Acts.EAT);
			}else{
				setState(Acts.FORAGE);
			}
			
//			if(inventory[APPLE_INDEX] < MAX_APPLE_STORAGE && inventory[BERRY_INDEX] < MAX_APPLE_STORAGE){
//				setState(Acts.FORAGE);
//			} else {
//				setState(Acts.EAT);
//			}
		} else if(currentTiredness > MAX_TIREDNESS/3){
			sleep();
			setState(Acts.SLEEP);
		} else if(currentThirst > MAX_THIRST/5 || inventory[WATER_INDEX] < MAX_WATER_STORAGE){
			if(currentThirst > MAX_THIRST/5 && inventory[WATER_INDEX] > 0){
				setState(Acts.DRINK);
			} else {
				setState(Acts.COLLECT_WATER);
			}
			
//			if(inventory[WATER_INDEX] < MAX_WATER_STORAGE){
//				setState(Acts.COLLECT_WATER);
//			} else {
//				setState(Acts.DRINK);
//			}
		}  else if(currentVariety < MAX_VARIETY/2){
			//setState(Acts.EXPLORE);
		}
	}
	
	private void smartDecision(){
		EnumMap<Acts, Double> actionValues = new EnumMap<>(Acts.class);
		for(Acts action : Acts.values()){
			double actionValue;
			switch(action){
				case NO_ACTION:
					actionValue = noActionValue(currentHealth, currentTiredness, currentHunger, currentThirst);
					break;
				case SLEEP:
					actionValue = sleepActionValue(currentTiredness, currentHunger, currentThirst);
					break;
				case EAT:
					actionValue = eatActionValue(currentHealth, currentHunger, inventory[APPLE_INDEX], inventory[BERRY_INDEX]);
					break;
				case DRINK:
					actionValue = drinkActionValue(currentHealth, currentThirst, inventory[WATER_INDEX]);
					break;
				case FORAGE:
					actionValue = forageActionValue(currentHunger, inventory[APPLE_INDEX], inventory[BERRY_INDEX]);
					break;
				case COLLECT_WATER:
					actionValue = collectWaterActionValue(currentThirst, inventory[WATER_INDEX]);
					break;
				case EXPLORE:
					actionValue = exploreActionValue();
					break;
					default:
						actionValue = -Double.MAX_VALUE;
						break;
			}
			actionValues.put(action, actionValue);
		}
		
		double bestValue = -Double.MAX_VALUE;
		Acts bestAction = Acts.NO_ACTION;
		for(Acts action : actionValues.keySet()){
			if(actionValues.get(action) > bestValue){
				bestValue = actionValues.get(action);
				bestAction = action;
			}
		}
		setState(bestAction);
		printActionValues(actionValues, bestAction);
	}
	
	private void printActionValues(EnumMap<Acts, Double> actValues, Acts bestAction){
		System.out.println("Time Step: " + getCurrentAge());
		System.out.println("Best Action: " + bestAction.name());
		for(Acts action : actValues.keySet()){
			System.out.println(action.name() + " : " + actValues.get(action));
		}
		System.out.println("-- END --\n");
	}
	
	private double noActionValue(int health, int tiredness, int hunger, int thirst){
		double value = -Double.MAX_VALUE;
		
		if(health < MAX_HEALTH){
			if(tiredness < MAX_TIREDNESS/3){
				if(hunger < MAX_HUNGER/2){
					if(thirst < MAX_THIRST/2){
						value = 100.0 * ((double)MAX_HEALTH)/((double)health);
					}
				}
			}
		}
		return value;
	}
	
	private double sleepActionValue(int tiredness, int hunger, int thirst){
		double value = 0.0;
		
		value -= hunger;
		value -= thirst;
		
		value += (((double)tiredness)/24.0) * (double)MAX_TIREDNESS;
		
		return value;
	}
	
	private double eatActionValue(int health, int hunger, int numApples, int numBerries) {
		if(numApples + numBerries == 0 || hunger == 0){ // no food or hunger? no value in eating.
			return -Double.MAX_VALUE;
		}
		
		double value = 0.0;
		double hmod = 10.0 * (((double)hunger)-((double)MAX_HUNGER));
		if(hunger == MAX_HUNGER) {
			value += HUNGER_HURT/(double)HUNGER_UPDATE_TIME;
			if(health < MAX_HEALTH) {
				value += hmod * (((double)MAX_HEALTH)/((double)health));
			}
		} else if(hunger > MAX_HUNGER/5){
			value += hmod;
		} else if(hunger < World.APPLE_HUNGER_VALUE && numBerries == 0) { // why consume an apple if you aren't going to use its full value?
			value += 10.0; // This is a super arbitrary number...
		} else {
			value += hunger;
		}
		
		return value;
	}
	
	
	private double drinkActionValue(int health, int thirst, int numWater){
		if(numWater == 0 || thirst == 0){
			return -Double.MAX_VALUE;
		}
		double value = 0.0;
		double tmod = 10.0 * (((double)thirst)/((double)MAX_THIRST));
		if(thirst == MAX_THIRST){
			value += THIRST_HURT/(double)THIRST_UPDATE_TIME;
			if(health < MAX_HEALTH) {
				value += tmod * (((double)MAX_HEALTH)/((double)health));
			}
		} else if(thirst > MAX_THIRST/5){
			value += tmod;
		} else {
			value += thirst;
		}
		return value;
	}
	
	private double forageActionValue(int hunger, int numApples, int numBerries) {
		if(numApples == MAX_APPLE_STORAGE && numBerries == MAX_BERRY_STORAGE){
			return -Double.MAX_VALUE;
		}
		double value = 0.0;
		
		int hungerAfterEat = hunger - (numApples * World.APPLE_HUNGER_VALUE) - (numBerries * World.BERRY_HUNGER_VALUE);
		
		if(hungerAfterEat > MAX_HUNGER/5) {
			value += hungerAfterEat * HUNGER_HURT;
		}
		if(haveSeenBush()) {
			value += 10 * World.BERRY_HUNGER_VALUE;
		}
		if(haveSeenTree()) {
			value += 10 * World.APPLE_HUNGER_VALUE;
		}
		
		if(numApples < MAX_APPLE_STORAGE){
			value += 5.0 * (MAX_APPLE_STORAGE - numApples);
		}

		if(numBerries < MAX_BERRY_STORAGE){
			value += 1.0 * (MAX_BERRY_STORAGE - numBerries);
		}
		
		
		return value;
	}
	
	private double collectWaterActionValue(int thirst, int numWater) {
		if(numWater == MAX_WATER_STORAGE){
			return -Double.MAX_VALUE;
		}
		double value = 0.0;
		
		int thirstAfterDrink = thirst - (numWater * World.WATER_THIRST_VALUE);
		
		if(thirstAfterDrink > MAX_THIRST/5) {
			value += thirstAfterDrink * THIRST_HURT;
		}
		if(haveSeenWater()){
			value += 10 * World.WATER_THIRST_VALUE;
		}
		if(numWater < MAX_WATER_STORAGE){
			value += (10.0 * (MAX_WATER_STORAGE - numWater));
		}
		
		return value;
	}
	
	private double exploreActionValue(){
		//TODO if known amounts of the world is small, bigger value
		//TODO if you don't know where water or food is, up the value
		double value = 0.0;
		
		if(!haveSeenWater()){
			value += World.WATER_VALUE;
		}
		if(!haveSeenTree()){
			value += World.APPLE_TREE_VALUE;
		}
		if(!haveSeenBush()){
			value += World.BUSH_VALUE;
		}
		
		
		return value;
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

//		 if(xDist > 1 && goalX != getX() && xDist > yDist){
//		 	currentLocationX += goalX > getX() ? 1 : -1;
//		 }else if( yDist > 1 && goalY != getY()){
//		 	currentLocationY += goalY > getY() ? 1 : -1;
//		 } else {
//		 	goalX = -1;
//		 	goalY = -1;
//		 	setState(Acts.NO_ACTION);
//		 }
		
		if(goalX <= 0 || goalY <= 0 -1){
			setState(Acts.NO_ACTION);
			return;
		} else if ((xDist > 1 && goalX != getX()) || (yDist > 1 && goalY != getY())) {
            smartSeek();
        } else {
			goalX = -1;
			goalY = -1;
			setState(Acts.NO_ACTION);
		}
	}
	
	private void smartSeek(){
        // continue to update values that he's explored
        valueIteration();

		int xDist = Math.abs(goalX - getX());
		int yDist = Math.abs(goalY - getY());
		
		
		boolean snakeToRight = thinkNearSnake(getX()+1, getY());
		boolean snakeToLeft = thinkNearSnake(getX()-1,getY());
		boolean snakeToUp = thinkNearSnake(getX(), getY()+1);
		boolean snakeToDown = thinkNearSnake(getX(), getY()-1);
		
		if(xDist > 1 && goalX != getX() && xDist > yDist && (!snakeToRight || !snakeToLeft)) {
			currentLocationX += goalX > getX() ? 1 : -1;
		} else if(yDist > 1 && goalY != getY() && (!snakeToDown || !snakeToUp)) {
			currentLocationY += goalY > getY() ? 1 : -1;
		}
	}
	
	private boolean thinkNearSnake(int trux, int truy){
		if(trux > -1 && trux < mapSizeX && truy > -1 && truy < mapSizeY) {
			return mapMemory[trux][truy] == World.SNAKE;
		}
		return false;
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
        double bestValue = -Double.MAX_VALUE;
		
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
        
        System.out.println(bestValue);

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

    int calculationTimer = 0;

    private void calcAbyssValues() {
        if (calculationTimer > 0) {
            calculationTimer -= 1;
            return;
        }
        
        calculationTimer = 10;

        // reset global storage
        abyssDirs = new double[]{0.0, 0.0, 0.0, 0.0};
        abyssQuads = new double[]{0.0, 0.0, 0.0, 0.0};

        //    QUADRANTS
        //        |
        //    A   |   B
        // ---------------
        //    C   |   D
        //        |

        int dirs[] = new int[]{0, 0, 0, 0}; // NORTH, SOUTH, EAST, WEST
        int quads[] = new int[]{0, 0, 0, 0}; // A, B, C, D

        for (int x = 0; x < mapSizeX; x++) {
            for (int y = 0; y < mapSizeX; y++) {
                if (mapMemory[x][y] != World.ABYSS) {
                    continue;
                }

                // directions

                if (y > currentLocationY) {
                    dirs[0] += 1;
                }
                if (y < currentLocationY) {
                    dirs[1] += 1;
                }
                if (x > currentLocationX) {
                    dirs[2] += 1;
                }
                if (x < currentLocationX) {
                    dirs[3] += 1;
                }  

                // quadrants

                if (x >= mapSizeX/2 && y < mapSizeY/2) {
                    quads[0] += 1; // quadA
                } else if (x >= mapSizeX/2 && y >= mapSizeY/2) {
                    quads[1] += 1; // quadB
                } else if (x < mapSizeX/2 && y < mapSizeY/2) {
                    quads[2] += 1; // quadC
                } else if (x < mapSizeX/2 && y >= mapSizeY/2) {
                    quads[3] += 1; // quadD
                } else {
                    System.err.println("ERROR: COULD NOT CALCULATE THE QUADRANTS CORRECTLY");
                    System.exit(1);
                }
            }
        }

        System.out.println();

        // System.out.println(quads[0] + "\t" + quads[1] + "\n" + quads[2] + "\t" + quads[3]);
        
        int dirIndex = -1;
        int dirMax = Integer.MIN_VALUE;
        double dirVal = 100.0;

        int quadIndex = -1;
        int quadMax = Integer.MIN_VALUE;
        double quadVal = 100.0;
        
        for (int x = 0; x < 4; x++) {
            for (int i = 0; i < 4; i++) {
                int quadValue = quads[i];
                int dirValue = dirs[i];

                if (dirValue > dirMax) {
                    dirIndex = i;
                    dirMax = dirValue;
                }

                if (quadValue > quadMax) {
                    quadIndex = i;
                    quadMax = quadValue;
                }
            }

            if (dirIndex != -1) {
                abyssDirs[dirIndex] = dirVal;
                dirs[dirIndex] = Integer.MIN_VALUE;
                dirVal /= 2.0;
            }

            if (quadIndex != -1) {
                abyssQuads[dirIndex] = quadVal;
                quads[quadIndex] = Integer.MIN_VALUE;
                quadVal /= 2.0;
            }

            dirIndex = -1;
            quadIndex = -1;
            dirMax = Integer.MIN_VALUE;
            quadMax = Integer.MIN_VALUE;
        }
    }

    private double getAbyssValue(int x, int y) {
        double val = 0.0;
        int count = 0;

        if (y > currentLocationY) {
            val += abyssDirs[0];
            count += 1;
        }
        if (y < currentLocationY) {
            val += abyssDirs[1];
            count += 1;
        }
        if (x > currentLocationX) {
            val += abyssDirs[2];
            count += 1;
        }
        if (x < currentLocationX) {
            val += abyssDirs[3];
            count += 1;
        }  

        if (count == 0) {
            System.out.println("ERROR: SHOULD NOT GET HERE pt 7");
            System.exit(1);
        }

        val /= (double) count;

        if (x >= mapSizeX/2 && y < mapSizeY/2) {
            return (abyssQuads[0] + val) / 2.0; // quadA
        } else if (x >= mapSizeX/2 && y >= mapSizeY/2) {
            return (abyssQuads[1] + val) / 2.0; // quadB
        } else if (x < mapSizeX/2 && y < mapSizeY/2) {
            return (abyssQuads[2] + val) / 2.0; // quadC
        } else if (x < mapSizeX/2 && y >= mapSizeY/2) {
            return (abyssQuads[3] + val) / 2.0; // quadD
        } else {
            System.out.println("ERROR: COULD NOT CALCULATE THE QUADRANTS CORRECTLY pt 3");
            return Double.MIN_VALUE;
        }
    }

	private double getValue(int x, int y, double priorResults) {
        if (x < 0 || x >= mapSizeX || y < 0 || y >= mapSizeY) {
            return 0;
        }
        
        double discountValue = .95;

        // if (x == 0 || x == mapSizeX - 1 || y == 0 || y == mapSizeY - 1) {
        //     discountValue *= -1.0;
        // }
        
        if (x == goalX && y == goalY) {
            return World.GOAL_VALUE;
        }
		
		if (mapMemory[x][y] == World.ABYSS) {
            // return discountValue * World.ABYSS_VALUE + priorResults;
            return discountValue * getAbyssValue(x, y) + priorResults;
		}
		
        // TODO grass?
        
        if (currentAction == Acts.FORAGE) {
            if (mapMemory[x][y] == World.APPLE_TREE) {
                return World.APPLE_TREE_VALUE;
            }
            
            if (mapMemory[x][y] == World.BUSH) {
                return World.BUSH_VALUE;
            }
        } else if (currentAction == Acts.COLLECT_WATER) {
            if (mapMemory[x][y] == World.WATER) {
                return World.WATER_VALUE;
            }
        }
		
		// if (mapMemory[x][y] == World.APPLE_TREE) {
		// 	return discountValue * World.APPLE_TREE_VALUE + priorResults;
		// }
		
		// if (mapMemory[x][y] == World.BUSH) {
		// 	return discountValue * World.BUSH_VALUE + priorResults;
		// }
		
		if (mapMemory[x][y] == World.ROCK) {
			return World.ROCK_VALUE;
		}
		
		if (mapMemory[x][y] == World.SNAKE) {
			return discountValue * World.SNAKE_VALUE + priorResults;
		}
		
		// if (mapMemory[x][y] == World.WATER) {
		// 	return discountValue * World.WATER_VALUE + priorResults;
		// }
		
		return discountValue * Vs[x][y] + priorResults;
	}
	
	private double iterate(double priorResults, int lastX, int lastY) {
        if (currentAction == Acts.FORAGE) {
            if (mapMemory[lastX][lastY] == World.APPLE_TREE) {
                return World.APPLE_TREE_VALUE;
            }
            
            if (mapMemory[lastX][lastY] == World.BUSH) {
                return World.BUSH_VALUE;
            }
        } else if (currentAction == Acts.COLLECT_WATER) {
            if (mapMemory[lastX][lastY] == World.WATER) {
                return World.WATER_VALUE;
            }
        }

        if (lastX == goalX && lastY == goalY) {
            return World.GOAL_VALUE;
        }
		
		// if (mapMemory[lastX][lastY] == World.APPLE_TREE) {
		// 	return World.APPLE_TREE_VALUE;
		// }
		
		// if (mapMemory[lastX][lastY] == World.BUSH) {
		// 	return World.BUSH_VALUE;
		// }
		
		if (mapMemory[lastX][lastY] == World.ROCK) {
			return World.ROCK_VALUE;
		}
		
		if (mapMemory[lastX][lastY] == World.SNAKE) {
			return World.SNAKE_VALUE;
		}
		
		// if (mapMemory[lastX][lastY] == World.WATER) {
		// 	return World.WATER_VALUE;
		// }
		
		priorResults += World.GRASS_VALUE;
		
		// TODO check +1/-1
		
        double sum = 0;
        
        //northwest
        sum += getValue(lastX - 1, lastY + 1, priorResults);

		// north
        sum += getValue(lastX, lastY + 1, priorResults);
        
        //northeast
        sum += getValue(lastX + 1, lastY + 1, priorResults);

        // west
        sum += getValue(lastX - 1, lastY, priorResults);

        // east
        sum += getValue(lastX + 1, lastY, priorResults);

        // southwest
        sum += getValue(lastX - 1, lastY - 1, priorResults);

		// south
        sum += getValue(lastX, lastY - 1, priorResults);

        // southeast
        sum += getValue(lastX + 1, lastY - 1, priorResults);
        
        return sum / 8;
	}
	
	private void valueIteration() {
		// INITIALIZE VALUE ARRAY
		for (int x = 0; x < mapSizeX; x++) {
			for (int y = 0; y < mapSizeY; y++) {
				Vs[x][y] = 0.0;
			}
        }
        
        calcAbyssValues();
		
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
