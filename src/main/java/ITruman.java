public interface ITruman {
	int MAX_HEALTH = 100;
	int MAX_HUNGER = 100;
	int NO_HUNGER = 0;
	int MAX_THIRST = 100;
	int NO_THIRST = 0;
	int MAX_LOVE = 10;
	int NO_LOVE = -10;
	int MAX_IMPORTANCE = 25;
	int NO_IMPORTANCE = -25;
	int MAX_TIREDNESS = 72; //low key "hours"
	int NO_TIREDNESS = 0;
	int MAX_VARIETY = 100;
	int NO_VARIETY = 0;
	int MIN_VARIETY_BOOST = 2;
	double MAX_RISK = 0.6; //maximum risk Truman is willing to take
	
	void makeDecision();
	
	boolean sleep();
	void explore();
	void stayPut();
	boolean forage();
	boolean eat();
	boolean drink();
	void expressThoughts();
	void addViewToMemory(int[][] viewPortion);
	void snakeBite() throws TrumanDiedException;
	int getX();
	int getY();
	int getViewRadius();
	boolean isSleeping();
}
