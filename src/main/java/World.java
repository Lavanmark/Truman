import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class World {
	
	int width, height;
	
	int[][] map = null;
	private HashMap<Point, Integer> berries;
	private HashMap<Point, Integer> apples;
	
	private final int MAX_BERRIES_ON_BUSH = 8;
	private final int MAX_APPLES_ON_TREE = 4;
	
	static final int APPLE_HUNGER_VALUE = 3;
	static final int BERRY_HUNGER_VALUE = 1;
	
	static final int GRASS = 0;
	static final int APPLE_TREE = 1;
	static final int BUSH = 2;
	static final int ROCK = 3;
	
	public World(String mapFileLocation) throws TrumanDiedException {
		importMap(mapFileLocation);
		if(map == null)
			throw new TrumanDiedException("Could not create a world for Truman to live in..."); //TODO modify to throw an exception to kill the game
	}
	
	private Point toPoint(int x, int y){
		return new Point(x,y);
	}
	
	public int pickBerries(int x, int y){
		if(map[x][y] != BUSH)
			return 0;
		
		int berriesOnBush = berries.get(toPoint(x,y));
		Random r = new Random();
		int berriesPicked = r.nextInt()%berriesOnBush;
		berries.put(toPoint(x,y), berriesOnBush - berriesPicked);
		return berriesPicked;
	}
	
	public void grow(){
		berries.replaceAll((p, v) -> v + 1);
		apples.replaceAll((p, v) -> v + 1);
	}
	
	
	
	private boolean importMap(String mapFileName){
		//TODO get width,height from file
		//TODO read in map
		try {
			FileReader fileReader = new FileReader("./maps/" + mapFileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			width = Integer.parseInt(bufferedReader.readLine());
			height = Integer.parseInt(bufferedReader.readLine());
			
			System.out.println("Width: " + width + "; Height = " + height);
			
			map = new int[width][height];
			berries = new HashMap<>();
			apples = new HashMap<>();
			
			for (int y = height - 1; y > -1; y--) {
				String line = bufferedReader.readLine();
				for(int x = 0; x < width; x++) {
					if(line.charAt(x) == 'G') { // Grass
						map[x][y] = GRASS;
					} else if(line.charAt(x) == '@'){ // Apple Tree
						map[x][y] = APPLE_TREE;
						apples.put(toPoint(x,y), MAX_APPLES_ON_TREE);
					}else if(line.charAt(x) == '*'){ // Bush
						map[x][y] = BUSH;
						berries.put(toPoint(x,y), MAX_BERRIES_ON_BUSH);
					}else if (line.charAt(x) == '#') // Rock/Wall
						map[x][y] = ROCK;
				}
			}
			
			bufferedReader.close();
			fileReader.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
