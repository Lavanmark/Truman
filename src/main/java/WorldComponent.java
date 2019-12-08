package main.java;

import main.java.truman.Truman;

import javax.swing.*;
import java.awt.*;

public class WorldComponent extends JComponent {
	
	private int winWidth, winHeight;
	private double sqrWdth, sqrHght;
	private Color grey = new Color(170, 170, 170, 100);

    private boolean showValueIteration = true;
	
	private Truman trumanPointer;
	
	private int gameState = 0; //Alive = 0, Dead = 1
	
	public WorldComponent(int windowWidth, int windowHeight, Truman truman){
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
	
	public void setOldManWin() {
		causeOfDeath = "Truman lived a \"happy\" life and died of old age.";
		gameState = 2;
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
                    col /= 3; // reduce overall heat map transparency
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

                    // if (showValueIteration) {
                        // g.setColor(Color.BLACK);
                        // g.drawLine(tlx,tly,tlx+(int)sqrWdth, tly+(int)sqrHght);
                    // }
                    g.setColor(new Color(0, 0, 0, 155));
                    g.fillRect(tlx, tly, (int) sqrWdth, (int) sqrHght);
				}
			}
		}
		
		g.setColor(Color.BLACK);
		g.drawOval((int) (trumanPointer.getX() * sqrWdth) + 1, (int) (((world.height-1) - trumanPointer.getY()) * sqrHght) + 1, (int) (sqrWdth - 1.4), (int) (sqrHght - 1.4));
		
		
		g.drawLine(0,winHeight, winWidth, winHeight);
		
		int year = trumanPointer.getCurrentAge()/(365*24);
		int day = trumanPointer.getCurrentAge()%(365*24)/24;
		int hour = trumanPointer.getCurrentAge()%(365*24)%24;
		
		g.setColor(Color.BLACK);
		g.drawString("Year: " + year + " Day: " + day + " Hour: " + hour, 320, 515);
		g.drawString("Time Step: " + trumanPointer.getCurrentAge(), 380, 530);
		
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
		g.drawString(actionString, 5, 515);
		
		// LEFT side
		g.drawString("Health: "+ trumanPointer.getCurrentHealth(), 5,565);
		g.drawString("Hunger: "+ trumanPointer.getCurrentHunger(), 5,580);
		g.drawString("Thirst: "+ trumanPointer.getCurrentThirst(), 5,595);
		
		//RIGHT side
		g.drawString("Tiredness: "+ trumanPointer.getCurrentTiredness(), 400,595);
		g.drawString("Variety: "+ trumanPointer.getCurrentVariety(), 400,580);
		
		if (gameState == 1) {
			g.setColor(Color.RED);
			g.drawString(causeOfDeath, 150, 550);
		} else if (gameState == 2) {
			g.setColor(new Color(34, 139, 34));
			g.drawString(causeOfDeath, 100, 550);
		}
	}
	
}
