public class TrumanProject {
	
	
	public TrumanProject(){
		
		try {
			World world = new World("world1.txt");
			ProjectFrame pf = new ProjectFrame(world);
		} catch(TrumanDiedException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args){
		new TrumanProject();
	}
}
