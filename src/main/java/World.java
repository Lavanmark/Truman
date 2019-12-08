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
	
	static final int APPLE_HUNGER_VALUE = 3;
	static final int BERRY_HUNGER_VALUE = 1;
	static final int WATER_THIRST_VALUE = 2;
	
	static final int SNAKE_BITE_VALUE = 50;
	
	static final int ABYSS = -1;
	static final int GRASS = 0;
	static final int APPLE_TREE = 1;
	static final int BUSH = 2;
	static final int ROCK = 3;
	static final int SNAKE = 4;
    static final int WATER = 5;
    
    static final double ABYSS_VALUE = 10;
	static final double GRASS_VALUE = -1.0;
	static final double APPLE_TREE_VALUE = 5.0;
	static final double BUSH_VALUE = 5.0;
	static final double ROCK_VALUE = -1000.0;
	static final double SNAKE_VALUE = -10.0;
	static final double WATER_VALUE = 5.0;
	
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
		moveSnakes();
		if(!truman.isSleeping()) {
			truman.addViewToMemory(getCurrentView(truman.getX(), truman.getY(), truman.getViewRadius()));
		}
	}
	
	private void moveSnakes(){
		for(Point snake : snakes) {
			int shouldMove = Math.abs(random.nextInt()%100);
			if(shouldMove < 25){
				int direction = Math.abs(random.nextInt()%5);
				Truman.Move move = Truman.Move.STAY;
				switch(direction){
					case 0:
						move = Truman.Move.STAY;
						break;
					case 1:
						move = Truman.Move.NORTH;
						break;
					case 2:
						move = Truman.Move.SOUTH;
						break;
					case 3:
						move = Truman.Move.EAST;
						break;
					case 4:
						move = Truman.Move.WEST;
						break;
				}
				switch(move){
					case NORTH:
						if(canSnakeMove(0, 1, snake)) {
							snake.y += 1;
							updateSnakeOnMap(0, 1, snake);
						}
						break;
					case SOUTH:
						if(canSnakeMove(0, -1, snake)) {
							snake.y -= 1;
							updateSnakeOnMap(0, -1, snake);
						}
						break;
					case EAST:
						if(canSnakeMove(1, 0, snake)) {
							snake.x += 1;
							updateSnakeOnMap(1, 0, snake);
						}
						break;
					case WEST:
						if(canSnakeMove(-1, 0, snake)) {
							snake.x -= 1;
							updateSnakeOnMap(-1, 0, snake);
						}
						break;
				}

			}
		}
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
		for(int x = -1; x <= 1; x++){
			int xmod = x + truman.getX();
			if(xmod < width && xmod > -1) {
				for(int y = -1; y <= 1; y++) {
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
				//TODO this makes a square and not a circle...
				int xmod = x + rx;
				int ymod = y + ry;
				if(xmod < width && xmod > -1) {
					currentView[xmod][y] = map[xmod][y];
				}
				if(ymod < height && ymod > -1) {
					currentView[x][ymod] = map[x][ymod];
				}
				if(xmod < width && xmod > -1) {
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
