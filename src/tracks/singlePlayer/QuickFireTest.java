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
import video.constants.InteractionStaticData;


/**
 * Created with IntelliJ IDEA. User: Diego Date: 04/10/13 Time: 16:29 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class QuickFireTest {

    public static void main(String[] args) throws IOException {

		// Available tracks:
		
		String boostedMCTSController = "tracks.singlePlayer.advanced.boostedMCTS.Agent";
		//Load available games
		String spGamesCollection =  "examples/atDelfi_games.csv";
		String[][] games = Utils.readGames(spGamesCollection);

		//Game settings
		boolean visuals = false;
		int seed = new Random().nextInt();

		// Game and level to play
		int gameIdx = 30;
		int levelIdx = Integer.parseInt(args[0]);
		int playthough = Integer.parseInt(args[1]);
		// level names from 0 to 4 (game_lvlN.txt).
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
			List<GameEvent> rules = parseTutorialRules("critical_mechanics/critical_mechanics_plants.json");
			
			Agent._critPath = rules;
			
			InteractionStaticData.agentName = "boosted_mcts";
			InteractionStaticData.gameName= gameName;
			InteractionStaticData.levelCount = args[0];
			InteractionStaticData.playthroughCount = args[1];
			// 2. This plays a game in a level by the controller.
			ArcadeMachine.runOneGame(game, level1, visuals, boostedMCTSController, null, seed, 0);
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
