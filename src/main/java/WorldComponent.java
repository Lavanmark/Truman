import javax.swing.*;
import java.awt.*;

public class WorldComponent extends JComponent {
	
	private int winWidth, winHeight;
	private double sqrWdth, sqrHght;
	private Color grey = new Color(170, 170, 170);
    private Color myWhite = new Color(220, 220, 220);

    private boolean showValueIteration = false;
	
	private Truman trumanPointer;
	
	private int gameState = 0; //Alive = 0, Dead = 1
	
	private int timeSteps = 0;
	
	public WorldComponent( int windowWidth, int windowHeight, Truman truman){
		trumanPointer = truman;
		winWidth = windowWidth;
		winHeight = windowHeight;
		
		sqrWdth = (double) windowWidth / World.getInstance().width;
		sqrHght = (double) windowHeight / World.getInstance().height;
	}
	
	public void addNotify() {
		super.addNotify();
		requestFocus();
	}
	
	public void setStepNumber(int number){
		timeSteps = number;
	}
	
	public void setWin() {
		gameState = 100;
		repaint();
	}
	
	private String causeOfDeath = null;
	
	public void setDead(String reason) {
		causeOfDeath = reason;
		gameState = 1;
		repaint();
	}
	
	public void paint(Graphics g) {
		paintWorld(g);
	}
	
	private void paintWorld(Graphics g) {
		World world = World.getInstance();
		
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
				else if (world.map[x][y] == World.SNAKE) {
					g.setColor(Color.PINK);
					g.fillRect((int) (x * sqrWdth), (int) (((world.height-1) - y) * sqrHght), (int) sqrWdth, (int) sqrHght);
				}
				else if (world.map[x][y] == World.WATER) {
					g.setColor(Color.CYAN);
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

        int[][] truMemory = trumanPointer.getCurrentMemory();

        // draw value iteration heat map

        if (showValueIteration) {
            double maxVal = -99999, minVal = 99999;
            double[][] vals = trumanPointer.getValueIterationData();

            for (int y = 0; y < world.height; y++) {
                for (int x = 0; x < world.width; x++) {
                    if (vals[x][y] > maxVal) {
                        maxVal = vals[x][y];
                    }
                    if (vals[x][y] < minVal) {
                        minVal = vals[x][y];
                    }
                }
            }

            if (minVal == maxVal) {
                maxVal = minVal + 1;
            }

            for (int y = 0; y < world.height; y++) {
                for (int x = 0; x < world.width; x++) {
                    if (truMemory[x][y] != World.GRASS) {
                        continue;
                    }

                    int tlx = (int)(x * sqrWdth);
                    int tly = (int) (((world.height-1) - y) * sqrHght);

                    int col = (int) (255 * (vals[x][y] - minVal) / (maxVal - minVal));
                    if (col > 255) {
                        col = 255;
                    }
                    col /= 2; // reduce overall heat map transparency by 50%
                    g.setColor(new Color(0, 0, 255, col));
                    g.fillRect(tlx, tly, (int) sqrWdth, (int) sqrHght);
                }
            }
        }
		
		for(int y = 0; y < world.height; y++){
			for (int x = 0; x < world.width; x++) {
				
				if(truMemory[x][y] == World.ABYSS){
					int tlx = (int)(x * sqrWdth);
                    int tly = (int) (((world.height-1) - y) * sqrHght);

					g.setColor(Color.BLACK);
                    g.drawLine(tlx,tly,tlx+(int)sqrWdth, tly+(int)sqrHght);
                    // g.setColor(new Color(0, 0, 0, 200));
                    // g.fillRect(tlx, tly, (int) sqrWdth, (int) sqrHght);
				}
			}
		}
		
		// System.out.println("repaint maxProb: " + maxProbs + "; " + mx + ", " + my);
		
		g.setColor(Color.BLACK);
		g.drawOval((int) (trumanPointer.getX() * sqrWdth) + 1, (int) (((world.height-1) - trumanPointer.getY()) * sqrHght) + 1, (int) (sqrWdth - 1.4), (int) (sqrHght - 1.4));
		
		g.setColor(Color.BLACK);
		g.drawString("Time Step: " + timeSteps, 380, 25);
		
		String actionString = "Nothing.";
		switch(trumanPointer.getCurrentAction()){
			case COLLECT_WATER:
				actionString = "Collecting Water";
				break;
			case FORAGE:
				actionString = "Foraging for food";
				break;
			case SLEEP:
				actionString = "Sleeping...";
				break;
			case EXPLORE:
				actionString = "Exploring!";
				break;
			case DRINK:
				actionString = "Drinking Water";
				break;
			case EAT:
				actionString = "Eating food";
				break;
			case WAKE_UP:
				actionString = "Waking up!";
				break;
			case NO_ACTION:
				actionString = "Doing Nothing.";
				break;
			case SEEKING:
				actionString = "Seeking location...";
				break;
		}
		g.setColor(Color.BLACK);
		g.drawString(actionString, 8, 25);
		
		g.drawString("Health: "+ trumanPointer.getCurrentHealth(), 8,450);
		g.drawString("Hunger: "+ trumanPointer.getCurrentHunger(), 8,465);
		g.drawString("Thirst: "+ trumanPointer.getCurrentThirst(), 8,480);
		g.drawString("Tiredness: "+ trumanPointer.getCurrentTiredness(), 400,480);
		g.drawString("Variety: "+ trumanPointer.getCurrentVariety(), 400,465);
		
		if (gameState == 1) {
			g.setColor(Color.RED);
			g.drawString(causeOfDeath, 100, 250);
		}
//		} else if (gameStatus == 2) {
//			g.setColor(Color.red);
//			g.drawString("You're a Loser!", 8, 25);
//		}
	}
	
}
