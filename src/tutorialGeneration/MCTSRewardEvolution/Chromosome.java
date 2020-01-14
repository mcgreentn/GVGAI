/*
 * Program by Megan "Milk" Charity
 * GVGAI-compatible version of Chromosome.java from MarioAIExperiment 
 * Creates a chromosome for use with MapElites
 */

package tutorialGeneration.MCTSRewardEvolution;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import tools.IO;
import tracks.ArcadeMachine;
import tracks.levelGeneration.LevelGenMachine;

import tracks.singlePlayer.advanced.boostedMCTS.Agent;
import video.basics.GameEvent;
import eveqt.EquationNode;
import eveqt.EquationParser;
import eveqt.EvEqT;

public class Chromosome implements Comparable<Chromosome>{
	
	/********************
	 * STATIC VARIABLES *
	 ********************/
		
	//taken directly from Chromosome.java [MarioAI]
		static protected Random _rnd;
		//protected int _appendingSize;		//size is dependent on the game itself
	
	//extended variables
		static protected String _gameName;
		static protected String _gamePath;
		static protected String[] _allChar;
		static protected EquationParser _eParser;
		static protected List<GameEvent> _rules;
		static protected double _maxDepth; 
		static protected int _levelCount;
		static protected int _playthroughCount;
	
	/********************
	 * OBJECT VARIABLES *
	 ********************/
		
	
	//taken directly from Chromosome.java [MarioAI]		
		protected double _constraints;	
		protected double _score;
		protected double _win;
		protected int[] _dimensions;		//binary vector for the interactions that occured for this chromosome
		private int _age;					//age of the current chromosome
		

	//extended variables
		protected EquationNode rewardEquation;
		private List<Double> scores;
		private List<Double> wins;
		private int index;
		private int playthrough;
		private int evalCount;
	
	//sets the static variables for the Chromsome class - shared between all chromosomes
	public static void SetStaticVar(Random seed, String gn, String gp, List<GameEvent> rules, HashSet<String> varNames, double maxDepth, int levelCount, int playthroughCount) {
		Chromosome._rnd = seed;
		Chromosome._gameName = gn;
		Chromosome._gamePath = gp;
		Chromosome._eParser = new EquationParser(new Random(), varNames, EvEqT.generateConstants(20, 1000));
		Chromosome._maxDepth = maxDepth;
		Chromosome._rules = rules;
		Chromosome._levelCount = levelCount;
		Chromosome._playthroughCount = playthroughCount;
	}
	
	
	//constructor for random initialization
	public Chromosome() {
		this._constraints = 0;
		this._score = 0;
		this._win = 0;
		this._dimensions = null;
		this._age = 0;
		this.scores = new ArrayList<Double>();
		this.wins = new ArrayList<Double>();
		this.evalCount = 1;
		this.randomInit();
	}

	//constructor for cloning and mutation
	public Chromosome(EquationNode rewardEquation) {
		this._constraints = 0;
		this._score = 0;
		this._win = 0;
		this._dimensions = null;
		this._age = 0;
		this.scores = new ArrayList<Double>();
		this.wins = new ArrayList<Double>();		
		this.evalCount = 1;
		this.rewardEquation = rewardEquation;
	}

	/**
	 * Randomly initialize a new equation tree

	 */
	public void randomInit() {
		try {
			this.rewardEquation = EvEqT.generateRandomTreeEquation(Chromosome._eParser, (int) Chromosome._maxDepth);
//			System.out.println("Random Init Before: " + this.rewardEquation.getTreeDepth());
//			System.out.println("Random Init Equation: " + this.rewardEquation.toString());
			this.rewardEquation = EvEqT.simplifyTree(Chromosome._eParser, this.rewardEquation);
//			System.out.println("Random Init After: " + this.rewardEquation.getTreeDepth());
//			System.out.println("Random Init Equation: " + this.rewardEquation.toString());
//			calculateDimensions();
//			System.out.println("Random Init Dimensions: " + this._dimensions[0]);
		} catch (Exception e) {
			e.printStackTrace();
		};
	}
	
	//overwrites the results from an already calculated chromosome of a child process
	public void parseOutputFile(String fileContents) {
		String[] fileStuff = fileContents.split("\n");
		
		this._age = Integer.parseInt(fileStuff[0]);
		this._constraints = Double.parseDouble(fileStuff[1]);
		this._score = Double.parseDouble(fileStuff[2]);
		this._win = Double.parseDouble(fileStuff[3]);

		
		// take care of fitnesses array
		String[] score1 = fileStuff[4].replace("[", "").replace("]", "").split(",");
		this.scores.clear();
		for(String s : score1) {
			this.scores.add(Double.parseDouble(s));
		}
		String[] win1 = fileStuff[5].replace("[", "").replace("]", "").split(",");

		this.wins.clear();
		for(String s: win1) {
			this.wins.add(Double.parseDouble(s));
		}
		
		String[] d = fileStuff[6].split("");
		this._dimensions = new int[d.length];
		for(int i=0;i<d.length;i++) {
			this._dimensions[i] = Integer.parseInt(d[i]);
		}
		this.index = Integer.parseInt(fileStuff[7]);
		this.playthrough = Integer.parseInt(fileStuff[8]);
	}
	
	public void addScore(double fitness) {
		scores.add(fitness);
		this._score = averageScores();
	}
	
	public void addWin(double win) {
		wins.add(win);
		this._win = averageWins();
	}
	
	public double averageScores() {
		double average = 0.0;
		for(Double fit : scores) {
			average += fit;
		}
		
		return average / scores.size();
	}
	
	public double averageWins() {
		double average = 0.0;
		for(Double fit : wins) {
			average += fit;
		}
		
		return average / wins.size();
	}

	// run a chromosome with an MCTS agent
	public void calculateResults(String aiAgent, int id) throws IOException {

		// run on all levels multiple times
		double avgScore = 0.0;
		double avgWin = 0.0;
		int levelCount = Chromosome._levelCount;
		int playthroughCount = Chromosome._playthroughCount;
		for (int i = 0; i < levelCount; i++) {
			for(int j = 0; j < playthroughCount; j++) {
				String levelName = Chromosome._gamePath.replace(".txt", "") + "_lvl" + i + ".txt";
				Agent._rewardEquation = rewardEquation;
				Agent._critPath = Chromosome._rules;
				System.out.println("Playing! \n * Level: " + i + "\n * Playthrough: " + j);

				double[] results = ArcadeMachine.runOneGame(Chromosome._gamePath, levelName, false, aiAgent, null, Chromosome._rnd.nextInt(), 0);
				double win = results[0];
				double score = results[1];
				double runFitness = win * 0.7 + score * 0.3;
				
				scores.add(score);
				wins.add(win);
				avgScore += score;
				avgWin += win;
			}
		}
//		average = average / (levelCount * playthroughCount);
		
		setConstraints(0); 	//set the constraints (win or lose)
		this._score = avgScore / (levelCount * playthroughCount);		//set the fitness
		this._win = avgWin / (levelCount * playthroughCount);
		this.calculateDimensions();
	}


	/*
	 * sets the constraints of the chromosome from the results of a run
	 */
	private void setConstraints(double value) {
		//just uses the win condition
		this._constraints = value;
	}

	// calculates chromosomes dimensions based on the depth of the equation tree
	public void calculateDimensions() {
		//System.out.println("calculating dimensions...");
		
		//create a new dimension set based on the size of max tree depth and set all values to 0
		this._dimensions = new int[4];
		
		// dimension = tree depth
		this._dimensions[0] = this.rewardEquation.getTreeDepth();
		this._dimensions[1] = this.rewardEquation.getTreeNodeCount();
		
		this._dimensions[2] = (this.rewardEquation.checkVariableInTree("win") ? 1 : 0);
		this._dimensions[3] = (this.rewardEquation.checkVariableInTree("score") ? 1 : 0);
	}
	
	
	/**
	 * Mutating
	 * @param coinFlip
	 */
	public void mutate(double coinFlip) {
		double f = 0.0;
		//int ct = 0;

		//if it meets the coin flip, then pick a tile and mutate
		do {
			try {
				f = Math.random();
				if (f < 0.33) {
					// Delete a random node from a clone copy of the input equation
					this.rewardEquation = EvEqT.deleteNode(Chromosome._eParser, this.rewardEquation);
				}
				else if (f < 0.66) {
					// Change a random node from a clone copy of the input equation
					this.rewardEquation = EvEqT.changeNode(Chromosome._eParser, this.rewardEquation);
				}
				else if(f >= 0.66) {
					// Insert a new node to a clone copy of the input equation
					this.rewardEquation = EvEqT.insertNode(Chromosome._eParser, this.rewardEquation, (int) Chromosome._maxDepth);
				}
//				System.out.println("Mutation Before: " + this.rewardEquation.getTreeDepth());
//				System.out.println("Mutation Equation: " + this.rewardEquation.toString());
				this.rewardEquation = EvEqT.simplifyTree(Chromosome._eParser, this.rewardEquation);
//				System.out.println("Mutation After: " + this.rewardEquation.getTreeDepth());
//				System.out.println("Mutation Equation: " + this.rewardEquation.toString());
//				calculateDimensions();
//				System.out.println("Mutation Dimensions: " + this._dimensions[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			f = Math.random();
		}while(f < coinFlip);


		
	}
	
	//clone chromosome function
	public Chromosome clone() {
		return new Chromosome(this.rewardEquation);
	}

	//override class toString() function
	public String toString() {
		return this.rewardEquation.toString();
	}

	//creates an input file format for the level (for use with parallelization)
	public String toInputFile(int index, int playthrough) {
		String output = "";
		output += index +"\n";
		output += playthrough + "\n";
		output += this._age + "\n";
		output += (this.toString());
		return output;
	}
	
	//creates an output file format for the level (for use with parallelization)
	public String toOutputFile() {
		String output = "";
		output += (this._age) + "\n";
		output += (this._constraints) + "\n";
		output += (this._score) + "\n";
		output += (this._win) + "\n"; 
		output += (Arrays.toString(this.scores.toArray())) + "\n";
		output += (Arrays.toString(this.wins.toArray())) + "\n";
		for(int i=0;i<this._dimensions.length;i++) {output += ("" + this._dimensions[i]);} output += "\n";
		output += (this.index) + "\n";
		output += (this.playthrough);
		//output += (this.toString());
		return output;
	}

	/**
	 * compares the constraints and fitness of 2 chromosomes
	 * taken directly from Chromosome.java [MarioAI]
	 * 
	 * @param o the compared Chromosome object
	 */
	@Override
	public int compareTo(Chromosome o) {
		int better = (int) Math.signum(this._win - o._win);
		if (better == 0) {
			better = (int) Math.signum(this._score - o._score);
		}
		return better;
	}

	//////////  GETTER FUNCTIONS  ///////////
	public int get_age() {
		return _age;
	}

	public double getConstraints() {
		return this._constraints;
	}

	public double getScore() {
		return this.averageScores();
	}
	
	public double getWin() {
		return this.averageWins();
	}

	public int[] getDimensions() {
		return this._dimensions;
	}
	public int getFitnessLength() {
		return this.scores.size();
	}
	
	////////// SETTER FUNCTIONS ///////////
	public void incrementAge() {
		this._age++;
	}
	public void setEvalCount(int count) {
		this.evalCount = count;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	
	///////////   HELPER FUNCTIONS   ////////////
	//gets the index of a character in an array (helper function)
	public int indexOf(char[] arr, char elem) {
		for(int i=0;i<arr.length;i++) {
			if(arr[i] == elem)
				return i;
		}
		return -1;
	}

	//log base 2 converter (helper function)
	public double log2(double x)
	{
		return (Math.log(x) / Math.log(2.0));
	}


	public void fileInit(String string) {
		// read in reward equation into this chromosome's reward equation
		try {
			String[] info = string.split("\n");
			this.rewardEquation = Chromosome._eParser.parse(info[3]);
			this.index = Integer.parseInt(info[0]);
			this.playthrough = Integer.parseInt(info[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public int getPlaythrough() {
		return this.playthrough;
	}
	
	public int getEvalCount() {
		return this.evalCount;
	}

}