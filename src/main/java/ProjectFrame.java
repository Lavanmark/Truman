package main.java;

import main.java.truman.Truman;
import main.java.truman.TrumanAI;
import main.java.truman.TrumanDiedException;

import javax.swing.*;
import java.awt.*;

public class ProjectFrame extends JFrame {
	Color bkgroundColor = new Color(255, 255, 255);
	
	private WorldComponent myWorldComponent;
	private Truman truman;
	
	public ProjectFrame(int actionDelay) throws TrumanDiedException {
		// set up the GUI that displays the information you compute
		int width = 500;
		int height = 500;
		int bar = 20;
		setSize(width, height + bar);
		getContentPane().setBackground(bkgroundColor);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, width, height + bar + 100);
		truman = new TrumanAI(World.getInstance().width, World.getInstance().height);
		myWorldComponent = new WorldComponent(width, height, truman);
		getContentPane().add(myWorldComponent);
		
		
		setVisible(true);
		setTitle("The Truman Project");
		
		doStuff(actionDelay);
	}
	
	private void doStuff(int actionDelay) {
		try {
			while(truman.getCurrentAge() <= Truman.AGE_OF_MAN) {
				truman.growOlder();
				truman.updateMemory();
				World.getInstance().update(truman);
				truman.makeDecision();
				truman.update();
				truman.expressThoughts();
				myWorldComponent.repaint();
				try {
					Thread.sleep(actionDelay);
				} catch(InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			if(truman.getCurrentAge() >= Truman.AGE_OF_MAN){
				myWorldComponent.setOldManWin();
				myWorldComponent.repaint();
			}
		} catch(TrumanDiedException e){
			e.printStackTrace();
			myWorldComponent.setDead(e.getMessage());
			myWorldComponent.repaint();
		}
	}
}
