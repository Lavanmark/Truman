import javax.swing.*;
import java.awt.*;

public class WorldComponent extends JComponent {
	
	private int winWidth, winHeight;
	private double sqrWdth, sqrHght;
	private Color grey = new Color(170, 170, 170);
	private Color myWhite = new Color(220, 220, 220);
	
	private World world;
	
	private int gameState = 0; //Alive = 0, Dead = 1
	
	public WorldComponent(World world, int windowWidth, int windowHeight){
		this.world = world;
		
		winWidth = windowWidth;
		winHeight = windowHeight;
		
		sqrWdth = (double) windowWidth / world.width;
		sqrHght = (double) windowHeight / world.height;
	}
	
	
	
	public void addNotify() {
		super.addNotify();
		requestFocus();
	}
	
	public void setWin() {
		gameState = 100;
		repaint();
	}
	
	public void setLoss() {
		gameState = 1;
		repaint();
	}
	
	
	
	public void paint(Graphics g) {
		paintWorld(g);
	}
	
	private void paintWorld(Graphics g) {
		int mx = 0, my = 0;
		
		// for (int y = world.height - 1; y > -1; y--) {
		for(int y = 0; y < world.height; y++){
			for (int x = 0; x < world.width; x++) {
				
				if (world.map[x][y] == World.GRASS) {
					g.setColor(Color.GREEN);
					g.fillRect((int) (x * sqrWdth), (int) (((world.height-1) - y) * sqrHght), (int) sqrWdth, (int) sqrHght);
				} else if (world.map[x][y] == World.BUSH) {
					g.setColor(Color.RED);
					g.fillRect((int) (x * sqrWdth), (int) (((world.height-1) - y) * sqrHght), (int) sqrWdth, (int) sqrHght);
				} else if (world.map[x][y] == World.APPLE_TREE) {
					g.setColor(new Color(139,69,19));
					g.fillRect((int) (x * sqrWdth), (int) (((world.height-1) - y) * sqrHght), (int) sqrWdth, (int) sqrHght);
				} else if (world.map[x][y] == World.ROCK) {
					g.setColor(Color.WHITE);
					g.fillRect((int) (x * sqrWdth), (int) (((world.height-1) - y) * sqrHght), (int) sqrWdth, (int) sqrHght);
				}
				
				
				
			}
		}
		for (int x = 0; x < world.width; x++) {
			g.setColor(grey);
			g.drawLine((int) (x * sqrWdth), 0, (int) (x * sqrWdth), (int) winHeight);
		}
		for(int y = 0; y < world.height; y++) {
			g.setColor(grey);
			g.drawLine(0, (int) (y * sqrHght), (int) winWidth, (int) (y * sqrHght));
		}
		
		// System.out.println("repaint maxProb: " + maxProbs + "; " + mx + ", " + my);
		
		g.setColor(Color.green);
		g.drawOval((int) (mx * sqrWdth) + 1, (int) (my * sqrHght) + 1, (int) (sqrWdth - 1.4), (int) (sqrHght - 1.4));
//
//		if (gameStatus == 1) {
//			g.setColor(Color.green);
//			g.drawString("You Won!", 8, 25);
//		} else if (gameStatus == 2) {
//			g.setColor(Color.red);
//			g.drawString("You're a Loser!", 8, 25);
//		}
	}
	
}
