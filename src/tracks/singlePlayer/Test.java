package tracks.singlePlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import core.logging.Logger;
import eveqt.EquationNode;
import eveqt.EquationParser;
import eveqt.EvEqT;
import tools.Utils;
import tracks.ArcadeMachine;
import tracks.singlePlayer.advanced.boostedMCTS.Agent;
import tutorialGeneration.MechanicParser;
import tutorialGeneration.MCTSRewardEvolution.Chromosome;
import video.basics.GameEvent;


/**
 * Created with IntelliJ IDEA. User: Diego Date: 04/10/13 Time: 16:29 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Test {

    public static void main(String[] args) throws IOException {

		// Available tracks:
		String sampleRandomController = "tracks.singlePlayer.simple.sampleRandom.Agent";
		String doNothingController = "tracks.singlePlayer.simple.doNothing.Agent";
		String sampleOneStepController = "tracks.singlePlayer.simple.sampleonesteplookahead.Agent";
		String sampleFlatMCTSController = "tracks.singlePlayer.simple.greedyTreeSearch.Agent";

		String sampleMCTSController = "tracks.singlePlayer.advanced.sampleMCTS.Agent";
		String boostedMCTSController = "tracks.singlePlayer.advanced.boostedMCTS.Agent";
        String sampleRSController = "tracks.singlePlayer.advanced.sampleRS.Agent";
        String sampleRHEAController = "tracks.singlePlayer.advanced.sampleRHEA.Agent";
		String sampleOLETSController = "tracks.singlePlayer.advanced.olets.Agent";

		//Load available games
		String spGamesCollection =  "examples/atDelfi_games.csv";
		String[][] games = Utils.readGames(spGamesCollection);

		//Game settings
		boolean visuals = true;
		int seed = new Random().nextInt();

		// Game and level to play
		int gameIdx = 30;
		int levelIdx = 0; // level names from 0 to 4 (game_lvlN.txt).
		String gameName = games[gameIdx][1];
		String game = games[gameIdx][0];
		String level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);

		String recordActionsFile = null;// "actions_" + games[gameIdx] + "_lvl"
						// + levelIdx + "_" + seed + ".txt";
						// where to record the actions
						// executed. null if not to save.

		// 1. This starts a game, in a level, played by a human.
//		ArcadeMachine.playOneGame(game, level1, recordActionsFile, seed);

		// generates reward equation
		try {
			List<GameEvent> rules = parseTutorialRules("rules/mechanics_zelda.json");
			HashSet<String> varNames = convertToRuleNames(rules);
	
			EquationParser parser = new EquationParser(new Random(), varNames, EvEqT.generateConstants(20, 1000));
			EquationNode n = parser.parse("lg(0.08333333333333333,rand(max(monsterNormal monsterSlow StepBack,mul(min(-5.0,pow(14.0,ls(pow(sin(sin(mul(floor(tanh(divide(cos(neg(inv(sub(sub(sub(neg(neg(ceil(inv(eq(floor(mod(min(abs(mul(sigmoid(inv(inv(add(ln(mod(cos(inv(mod(pow(ls(sigmoid(neg(min(lg(floor(floor(inv(sin(mod(sin(sub(min(inv(pow(eq(ln(lg(eq(floor(add(sigmoid(rand(cos(mul(neg(lg(ls(mod(min(cos(ln(ls(add(divide(sin(divide(floor(ls(rand(neg(lg(min(sigmoid(inv(rand(abs(ls(pow(ln(sigmoid(ls(pow(sigmoid(floor(pow(tanh(eq(abs(cos(tanh(rand(sub(min(floor(min(eq(sigmoid(floor(sin(cos(pow(add(neg(rand(sigmoid(neg(mod(cos(eq(lg(max(neg(mul(mod(sin(sin(abs(sin(pow(pow(ceil(ln(rand(abs(eq(mul(floor(max(sigmoid(lg(sin(mod(sin(floor(abs(min(inv(rand(abs(mod(sigmoid(divide(sub(sin(mul(pow(divide(sub(-11.0,monsterSlow monsterNormal StepBack),withkey monsterQuick KillSprite),monsterNormal sword KillSprite),7.0)),monsterSlow wall StepBack),monsterSlow monsterNormal StepBack)),monsterSlow monsterNormal StepBack)),10.0)),monsterSlow wall StepBack)))),monsterSlow wall StepBack)),10.0)),-200.0)),monsterQuick sword KillSprite),withkey monsterQuick KillSprite)),nokey monsterNormal KillSprite))),-17.0),monsterNormal sword KillSprite))))),-2.0),monsterQuick wall StepBack)),monsterQuick monsterQuick StepBack),0.0625),monsterQuick sword KillSprite)),withkey monsterQuick KillSprite))),-16.0)),-200.0),nokey goal StepBack))))),0.5),monsterNormal monsterNormal StepBack)),-0.3333333333333333),0.05263157894736842),monsterNormal sword KillSprite)))),withkey monsterQuick KillSprite)),0.09090909090909091))),-0.05555555555555555),monsterQuick monsterNormal StepBack))),-0.25),monsterQuick sword KillSprite)),monsterQuick wall StepBack))),-0.07142857142857142),nokey monsterQuick KillSprite)),-15.0),-0.1)),4.0)),monsterQuick monsterQuick StepBack),-0.125),nokey wall StepBack))),nokey goal StepBack),0.125),withkey wall StepBack),20.0)),-0.08333333333333333)),monsterNormal wall StepBack)),goal null Win)),20.0),nokey monsterSlow KillSprite)),10.0),monsterQuick wall StepBack)),monsterSlow monsterNormal StepBack),-19.0)),4.0))))),0.3333333333333333),-7.0))),nokey monsterSlow KillSprite),withkey monsterNormal KillSprite),-18.0))),monsterQuick wall StepBack)),-0.05263157894736842)))),0.005)),0.09090909090909091),nokey withkey Lose)),-0.2))))),-0.3333333333333333),wall wall StepBack),monsterNormal sword KillSprite)))),nokey monsterNormal KillSprite))),-2.0))),monsterNormal monsterQuick StepBack),0.0625))),nokey goal StepBack)),abs(monsterSlow monsterNormal StepBack)))");
			System.out.println(n.getTreeDepth());
			
			Agent._rewardEquation = n;
			Agent._critPath = rules;
			// 2. This plays a game in a level by the controller.
			
			String actionFile = "F:\\Google Drive\\Projects\\Tutorial Generation\\Critical Mechanic Discovery Methods\\Attention Mechanisms\\raw\\agents\\agents.ICELab.Agent_lvl0_playthrough0_-1974834944.txt";
//			double[] results = ArcadeMachine.runOneGame(game, level1, visuals, sampleMCTSController, recordActionsFile, seed, 0);
			ArcadeMachine.replayGame(game, level1, visuals, actionFile);
			System.out.println("");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		// 3. This replays a game from an action file previously recorded
	//	 String readActionsFile = recordActionsFile;
	//	 ArcadeMachine.replayGame(game, level1, visuals, readActionsFile);

		// 4. This plays a single game, in N levels, M times :
//		String level2 = new String(game).replace(gameName, gameName + "_lvl" + 1);
//		int M = 10;
//		for(int i=0; i<games.length; i++){
//			game = games[i][0];
//			gameName = games[i][1];
//			level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);
//			ArcadeMachine.runGames(game, new String[]{level1}, M, sampleMCTSController, null);
//		}

		//5. This plays N games, in the first L levels, M times each. Actions to file optional (set saveActions to true).
//		int N = games.length, L = 2, M = 1;
//		boolean saveActions = false;
//		String[] levels = new String[L];
//		String[] actionFiles = new String[L*M];
//		for(int i = 0; i < N; ++i)
//		{
//			int actionIdx = 0;
//			game = games[i][0];
//			gameName = games[i][1];
//			for(int j = 0; j < L; ++j){
//				levels[j] = game.replace(gameName, gameName + "_lvl" + j);
//				if(saveActions) for(int k = 0; k < M; ++k)
//				actionFiles[actionIdx++] = "actions_game_" + i + "_level_" + j + "_" + k + ".txt";
//			}
//			ArcadeMachine.runGames(game, levels, M, sampleRHEAController, saveActions? actionFiles:null);
//		}


    }

	//sets the game interaction set (rules) for the dimensionality
	private static List<GameEvent> parseTutorialRules(String mechanicFile) {
		List<GameEvent> mechanics = MechanicParser.readMechFile(mechanicFile);

		return mechanics;
	}
	
	// Converts the raw mechanic info into strings for variables in the equation trees
	private static HashSet<String> convertToRuleNames(List<GameEvent> mechanics) {
		List<String> mechNames = new ArrayList<String>();
		for (GameEvent event : mechanics) {
			mechNames.add(event.toString());
		}
		HashSet<String> varSet = new HashSet<String>(mechNames);
		return varSet;
	}
}
