public class TrumanProject {
	private static final int MAP_INDEX = 0;
	private static final int ACTION_DELAY_INDEX = 1;
	
	public TrumanProject(String mapFileLocation, int actionDelay){
		
		try {
			World.createInstance(mapFileLocation);
			ProjectFrame pf = new ProjectFrame(actionDelay);
		} catch(TrumanDiedException e) {
			e.printStackTrace();
		}
	}
	
	public TrumanProject(String mapFileLocation){
		this(mapFileLocation, 500);
	}
	
	public TrumanProject(){
		this("world10x10.txt", 500);
	}
	
	
	public static void main(String[] args){
		if(args.length == 1){
			new TrumanProject(args[TrumanProject.MAP_INDEX]);
		} else if(args.length == 2) {
			new TrumanProject(args[TrumanProject.MAP_INDEX],
					Integer.getInteger(args[TrumanProject.ACTION_DELAY_INDEX]));
		} else {
			new TrumanProject();
		}
	}
}
