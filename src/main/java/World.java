package main.java;


import main.java.truman.Truman;
import main.java.truman.TrumanDiedException;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class World {
	
	private static World worldInstance = null;
	
	public static World getInstance(){
		return worldInstance;
	}
	
	public static void createInstance(String mapFileLocation) throws TrumanDiedException {
		if(worldInstance == null){
			worldInstance = new World(mapFileLocation);
		}
	}
	
	int width, height;
	
	private Random random = new Random();
	
	int[][] map = null;
	private HashMap<Point, Integer> berries;
	private HashMap<Point, Integer> apples;
	
	private ArrayList<Point> snakes;
	
	private final int MAX_BERRIES_ON_BUSH = 8;
	private final int MAX_APPLES_ON_TREE = 4;
	
	public static final int APPLE_HUNGER_VALUE = 3;
	public static final int BERRY_HUNGER_VALUE = 1;
	public static final int WATER_THIRST_VALUE = 4;
	
	public static final int SNAKE_BITE_VALUE = 50;
	public static final int SNAKE_ATTACK_RANGE = 4;
	public static final int SNAKE_BITE_RANGE = 0;
	
	public static final int ABYSS = -1;
	public static final int GRASS = 0;
	public static final int APPLE_TREE = 1;
	public static final int BUSH = 2;
	public static final int ROCK = 3;
	public static final int SNAKE = 4;
	public static final int WATER = 5;
	
	public static final double ABYSS_VALUE = 10;
	public static final double GRASS_VALUE = 0.0;
	public static final double APPLE_TREE_VALUE = 5.0;
	public static final double BUSH_VALUE = 5.0;
	public static final double ROCK_VALUE = -1.0;
	public static final double SNAKE_VALUE = -100.0;
    public static final double WATER_VALUE = 5.0;
    public static final double GOAL_VALUE = 50.0;
	
	public World(String mapFileLocation) throws TrumanDiedException {
		boolean imported = importMap(mapFileLocation);
		if(map == null || !imported)
			throw new TrumanDiedException("Could not create a world for Truman to live in...");
	}
	
	private Point toPoint(int x, int y){
		return new Point(x,y);
	}
	
	public int pickBerries(int trux, int truy) {
		if(map[trux][truy] == BUSH) {
			int berriesOnBush = berries.get(toPoint(trux,truy));
			int berriesPicked = Math.abs(random.nextInt()%(berriesOnBush+1));
			if(berriesOnBush > 0 && berriesPicked == 0) berriesPicked = 1;
			berries.put(toPoint(trux,truy), berriesOnBush - berriesPicked);
			return berriesPicked;
		}
		for(int x = -1; x <= 1; x++){
			int xmod = x + trux;
			if(xmod < width && xmod > -1) {
				for(int y = -1; y <= 1; y++) {
					int ymod = y + truy;
					if(ymod < height && ymod > -1) {
						if(map[xmod][ymod] == BUSH){
							int berriesOnBush = berries.get(toPoint(xmod,ymod));
							int berriesPicked = Math.abs(random.nextInt()%(berriesOnBush+1));
							if(berriesOnBush > 0 && berriesPicked == 0) berriesPicked = 1;
							berries.put(toPoint(xmod,ymod), berriesOnBush - berriesPicked);
							return berriesPicked;
						}
					}
				}
			}
		}
		return 0;
	}
	
	public int pickApples(int trux, int truy){
		if(map[trux][truy] == APPLE_TREE) {
			int applesOnTree = apples.get(toPoint(trux,truy));
			if(applesOnTree > 0) {
				apples.put(toPoint(trux, truy), applesOnTree - 1);
				return 1;
			}
		}
		for(int x = -1; x <= 1; x++){
			int xmod = x + trux;
			if(xmod < width && xmod > -1) {
				for(int y = -1; y <= 1; y++) {
					int ymod = y + truy;
					if(ymod < height && ymod > -1) {
						if(map[xmod][ymod] == APPLE_TREE){
							int applesOnTree = apples.get(toPoint(xmod,ymod));
							if(applesOnTree > 0) {
								apples.put(toPoint(xmod, ymod), applesOnTree - 1);
								return 1;
							}
						}
					}
				}
			}
		}
		return 0;
	}
	
	public int collectWater(int trux, int truy) {
		if(map[trux][truy] == WATER)
			return 1;
		for(int x = -1; x <= 1; x++){
			int xmod = x + trux;
			if(xmod < width && xmod > -1) {
				for(int y = -1; y <= 1; y++) {
					int ymod = y + truy;
					if(ymod < height && ymod > -1) {
						if(map[xmod][ymod] == WATER){
							return 1;
						}
					}
				}
			}
		}
		return 0;
	}
	
	private void grow(){
		//System.out.println("The fruit has grown!");
		berries.replaceAll((p, v) -> v + 1);
		apples.replaceAll((p, v) -> v + 1);
	}
	
	public void update(Truman truman) throws TrumanDiedException {
		int shouldGrow = Math.abs(random.nextInt()%100);
		if(shouldGrow < 33){
			grow();
		}
		if(isNearSnake(truman)){
			int biteChance = Math.abs(random.nextInt()%100);
			if(biteChance > 50){
				truman.snakeBite();
			}
		}
		moveSnakes(truman);
		if(!truman.isSleeping()) {
			truman.addViewToMemory(getCurrentView(truman.getX(), truman.getY(), truman.getViewRadius()));
		}
	}
	
	private void moveSnakes(Truman truman){
		for(Point snake : snakes) {
			int shouldMove = Math.abs(random.nextInt()%100);
			if(shouldMove < 10){
				int snakeMove = Math.abs(random.nextInt() % Truman.TOTAL_MOVES);
				if(shouldSnakeSeekTruman(snake, truman.getX(), truman.getY())){
					int distx = snake.x - truman.getX();
					int disty = snake.y - truman.getY();
					
					if(Math.abs(distx) > Math.abs(disty)){
						if(distx > 0){ // to the left (west)
							snakeMove = Truman.MOVE_WEST;
						} else if(distx < 0){ // to the right (east)
							snakeMove = Truman.MOVE_EAST;
						} else {
							snakeMove = Truman.MOVE_STAY;
						}
					} else {
						if(disty > 0){ // truman is below the snake (south)
							snakeMove = Truman.MOVE_SOUTH;
						} else if(disty < 0){ // truman is above the snake (north)
							snakeMove = Truman.MOVE_NORTH;
						} else {
							snakeMove = Truman.MOVE_STAY;
						}
					}
					
				}
				switch(snakeMove){
					case Truman.MOVE_NORTH:
						if(canSnakeMove(0, 1, snake)) {
							snake.y += 1;
							updateSnakeOnMap(0, 1, snake);
						}
						break;
					case Truman.MOVE_SOUTH:
						if(canSnakeMove(0, -1, snake)) {
							snake.y -= 1;
							updateSnakeOnMap(0, -1, snake);
						}
						break;
					case Truman.MOVE_EAST:
						if(canSnakeMove(1, 0, snake)) {
							snake.x += 1;
							updateSnakeOnMap(1, 0, snake);
						}
						break;
					case Truman.MOVE_WEST:
						if(canSnakeMove(-1, 0, snake)) {
							snake.x -= 1;
							updateSnakeOnMap(-1, 0, snake);
						}
						break;
				}
			}
		}
	}
	
	private boolean shouldSnakeSeekTruman(Point snake, int trux, int truy){
		// If a snake is within SNAKE_ATTACK_RANGE tiles of truman, it will hunt him down.
		return (Math.abs(snake.x - trux) < SNAKE_ATTACK_RANGE && Math.abs(snake.y - truy) < SNAKE_ATTACK_RANGE);
	}
	
	private boolean canSnakeMove(int xmod, int ymod, Point snake){
		if(snake.x + xmod > width - 1 || snake.x + xmod < 0){
			return false;
		}
		
		if(snake.y + ymod > height - 1 || snake.y + ymod < 0){
			return false;
		}
		int typeOfFutureTile = map[snake.x+xmod][snake.y+ymod];
		return typeOfFutureTile == GRASS;
	}
	
	private void updateSnakeOnMap(int xmod, int ymod, Point snake){
		map[snake.x-xmod][snake.y-ymod] = GRASS;
		map[snake.x][snake.y] = SNAKE;
	}
	
	private boolean isNearSnake(Truman truman){
		if(map[truman.getX()][truman.getY()] == SNAKE){
			return true;
		}
		for(int x = -SNAKE_BITE_RANGE; x <= SNAKE_BITE_RANGE; x++){
			int xmod = x + truman.getX();
			if(xmod < width && xmod > -1) {
				for(int y = -SNAKE_BITE_RANGE; y <= SNAKE_BITE_RANGE; y++) {
					int ymod = y + truman.getY();
					if(ymod < height && ymod > -1) {
						if(map[xmod][ymod] == SNAKE){
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	private int[][] getCurrentView(int x, int y, int radius) {
		int[][] currentView = new int[width][height];
		
		// need to initialize array with ABYSS value so truman doesn't think everything is grass.
		for(int[] ints : currentView) {
			Arrays.fill(ints, ABYSS);
		}
		
		currentView[x][y] = map[x][y];
		for(int rx = -radius; rx <= radius; rx++){
			for(int ry = -radius; ry <= radius; ry++) {
				if(radius > 1) {
					if(Math.abs(rx) == radius && Math.abs(ry) > (radius / 2))
						continue;
					if(Math.abs(ry) == radius && Math.abs(rx) > (radius / 2))
						continue;
					if(Math.abs(rx) == radius - 1 && Math.abs(ry) > (radius / 2)+1)
						continue;
					if(Math.abs(ry) == radius - 1 && Math.abs(rx) > (radius / 2)+1)
						continue;
				}
				
				int xmod = x + rx;
				int ymod = y + ry;
				if(ymod < height && ymod > -1) {
					currentView[x][ymod] = map[x][ymod];
				}
				if(xmod < width && xmod > -1) {
					currentView[xmod][y] = map[xmod][y];
					if(ymod < height && ymod > -1) {
						currentView[xmod][ymod] = map[xmod][ymod];
					}
				}
			}
		}
		return currentView;
	}
	
	
	
	private boolean importMap(String mapFileName){
		try {
			FileReader fileReader = new FileReader("./maps/" + mapFileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			width = Integer.parseInt(bufferedReader.readLine());
			height = Integer.parseInt(bufferedReader.readLine());
			
			System.out.println("Width: " + width + "; Height = " + height);
			
			map = new int[width][height];
			berries = new HashMap<>();
			apples = new HashMap<>();
			snakes = new ArrayList<>();
			
			for (int y = height - 1; y > -1; y--) {
				String line = bufferedReader.readLine();
				for(int x = 0; x < width; x++) {
					if(line.charAt(x) == 'G') { // Grass
						map[x][y] = GRASS;
					} else if(line.charAt(x) == '@'){ // Apple Tree
						map[x][y] = APPLE_TREE;
						apples.put(toPoint(x,y), MAX_APPLES_ON_TREE);
					} else if(line.charAt(x) == '*'){ // Bush
						map[x][y] = BUSH;
						berries.put(toPoint(x,y), MAX_BERRIES_ON_BUSH);
					} else if (line.charAt(x) == '#') { // Rock/Wall
						map[x][y] = ROCK;
					} else if (line.charAt(x) == 's') { // Snake
						map[x][y] = SNAKE;
						snakes.add(new Point(x,y));
					} else if (line.charAt(x) == 'w') { // Water
						map[x][y] = WATER;
					}
				}
			}
			
			bufferedReader.close();
			fileReader.close();
		}
		catch(IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
