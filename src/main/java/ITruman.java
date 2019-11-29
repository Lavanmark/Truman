public interface ITruman {
	int MAX_HEALTH = 100;
	int MAX_HUNGER = 10;
	int NO_HUNGER = 0;
	int MAX_THIRST = 10;
	int NO_THIRST = 0;
	int MAX_LOVE = 10;
	int NO_LOVE = -10;
	int MAX_IMPORTANCE = 25;
	int NO_IMPORTANCE = -25;
	int MAX_TIREDNESS = 72; //low key "hours"
	int NO_TIREDNESS = 0;
	double MAX_RISK = 0.6; //maximum risk Truman is willing to take
	
	boolean sleep();
	void explore();
	void stayPut();
	boolean forage();
	boolean buildFire();
	boolean cook();
	boolean eat();
	boolean buildShelter();
	boolean cutDownTree();
	void expressThoughts();
}
