import javax.swing.*;
import java.awt.*;

public class ProjectFrame extends JFrame {
	Color bkgroundColor = new Color(255, 255, 255);
	
	private WorldComponent myWorldComponent;
	private World world;
	private Truman truman;
	
	public ProjectFrame(World world) throws TrumanDiedException {
		this.world = world;
		
		
		// set up the GUI that displays the information you compute
		int width = 500;
		int height = 500;
		int bar = 20;
		setSize(width, height + bar);
		getContentPane().setBackground(bkgroundColor);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, width, height + bar);
		myWorldComponent = new WorldComponent(world, width, height);
		getContentPane().add(myWorldComponent);
		truman = new Truman(world.height, world.width);
		
		setVisible(true);
		setTitle("The Truman Project");
		
		doStuff();
	}
	
	private void doStuff() throws TrumanDiedException {
		while(true){
			//truman.update();
			myWorldComponent.repaint();
		}
	}
}
