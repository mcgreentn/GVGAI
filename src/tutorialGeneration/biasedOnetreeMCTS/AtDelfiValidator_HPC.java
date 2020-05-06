package tutorialGeneration.biasedOnetreeMCTS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import core.competition.CompetitionParameters;
import core.game.Game;
import core.game.StateObservation;
import core.vgdl.VGDLFactory;
import core.vgdl.VGDLParser;
import core.vgdl.VGDLRegistry;
import ontology.Types;
import tools.Utils;
import tracks.ArcadeMachine;
import tutorialGeneration.MechanicParser;
import video.basics.GameEvent;
import video.basics.Interaction;
import video.basics.PlayerAction;
import video.constants.InteractionStaticData;

public class AtDelfiValidator_HPC {

	public boolean VERBOSE = true;
	String atDelfiGamesCollection =  "examples/atDelfi_games.csv";
    String gamesPath = "examples/gridphysics/";
    String physicsGamesPath = "examples/contphysics/";
    String generateTutorialPath = gamesPath;

	String[][] games = Utils.readAtDelfiGames(atDelfiGamesCollection);
    int levelIdx = 4; // level names from 0 to 4 (game_lvlN.txt).
    int gameIdx = 47;
    
//    public static int gameID;
	
	public StateObservation startup(int randomSeed, String game_file, String level_file) throws IOException {
		VGDLFactory.GetInstance().init(); // This always first thing to do.
		VGDLRegistry.GetInstance().init();

		if (VERBOSE)
			System.out.println(" ** Playing game " + game_file + ", level " + level_file + " **");

		if (CompetitionParameters.OS_WIN)
		{
			System.out.println(" * WARNING: Time limitations based on WALL TIME on Windows * ");
		}

		// First, we create the game to be played..
		Game toPlay = new VGDLParser().parseGame(game_file);
		toPlay.buildLevel(level_file, randomSeed);

		if(InteractionStaticData.saveSpriteGroup) {
			InteractionStaticData.createJSONInfo(toPlay);
		}
		
		// Warm the game up.
		ArcadeMachine.warmUp(toPlay, CompetitionParameters.WARMUP_TIME);
		
		return toPlay.getObservation();
	}
	public static void main(String[] args) {
		
		
		int seed = 100;
		AtDelfiValidator_HPC validator = new AtDelfiValidator_HPC();
		int id = Integer.parseInt(args[0]);
		SingleMCTSPlayer.numIterations = Integer.parseInt(args[2]);
		SingleTreeNode.ROLLOUT_DEPTH = Integer.parseInt(args[3]);

		
		int u=id%10;
	    int t=(id/10)%10;
		String criticalFile = "critical_mechanics.json";
	    
	    if(id > 299) {
			validator.gameIdx = 34;
			criticalFile = "critical_mechanics_realPortals.json";
	    }
	    else if(id > 199) { 
			validator.gameIdx = 39;
			criticalFile = "critical_mechanics_solarfox.json";
	    }
		else if(id > 99) {
			validator.gameIdx = 30;
			criticalFile = "critical_mechanics_plants.json";
		}
		else {
			validator.gameIdx = 47;
			criticalFile = "critical_mechanics_zelda.json";
		}
		

		// TODO remove for normal experiments
//		validator.gameIdx = 30;
//		criticalFile = "critical_mechanics_plants.json";

		// KEEP THIS
		int withoutH = u + t*10;
		
		// TODO reinsert for normal experiments
		if (withoutH > 79) 
			validator.levelIdx = 4;
		else if(withoutH > 59)
			validator.levelIdx = 3;
		else if(withoutH > 39)
			validator.levelIdx = 2;
		else if(withoutH > 19)
			validator.levelIdx = 1;
		else
			validator.levelIdx = 0;
		
		// TODO remove for normal experiments
//		if (withoutH > 799) 
//			validator.levelIdx = 4;
//		else if(withoutH > 599)
//			validator.levelIdx = 3;
//		else if(withoutH > 399)
//			validator.levelIdx = 2;
//		else if(withoutH > 199)
//			validator.levelIdx = 1;
//		else
//			validator.levelIdx = 0;
		

		

		boolean improved = Boolean.parseBoolean(args[1]);
		
		System.out.println("******************");
		System.out.println("Improved: " + improved + "\nCritical file: " + criticalFile + "\nGame: " 
		+ validator.gameIdx + "\nLevel: " + validator.levelIdx + "\nTree Size: " + SingleMCTSPlayer.numIterations + "\nRollout: " + SingleTreeNode.ROLLOUT_DEPTH);
		validator.runXperiments(criticalFile, improved, seed, false, id);
	}
	
	public String[] getGame(int id) {
		return games[id];
	}
	
	public void runXperiments(String criticalFile, boolean improved, int seed, boolean seeded, int id) {
		
		String gameFile = generateTutorialPath + getGame(gameIdx)[1] + ".txt";
		String levelFile = gamesPath + getGame(gameIdx)[1] + "_lvl" + levelIdx + ".txt";
		try{
			// make experiments directory and main experiment file //
			File directory = new File(String.valueOf("experiments"));

			if(!directory.exists()){

				directory.mkdir();
			}
			File mainFile = new File("experiments/main_file_" + gameIdx + "_" + id +".txt");
			
			mainFile.createNewFile();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		File mainFile = new File("experiments/main_file_" + gameIdx + "_" + id +".txt");
		int winning = 0;
			try{
				
				File expFile = new File("experiments/experiment_" + gameIdx + "_" + id + ".txt");
				expFile.createNewFile();
				
				// more setup stuff
				if(!seeded)
					seed = new Random().nextInt();
				StateObservation root = startup(seed, gameFile, levelFile);
		        ArrayList<Types.ACTIONS> act = root.getAvailableActions();
		        Types.ACTIONS[] actions = new Types.ACTIONS[act.size()];
		        
		        for(int k = 0; k < actions.length; ++k)
		        {
		            actions[k] = act.get(k);
		        }
		        
		        int num_actions = actions.length;
		        		        
		        // create the mcts player //
				if(!seeded)
					seed = new Random().nextInt();
		        SingleMCTSPlayer player = new SingleMCTSPlayer(new Random(seed), num_actions, actions, improved, expFile, mainFile);
		        player.critPath = MechanicParser.readMechFile(criticalFile);
		     
		        
		        player.init(root);
		        winning += player.run(id);	


			}catch(Exception e) {
				e.printStackTrace();
			}
	}
	
	
	
	
//	public void visualize() {
//		String game = generateTutorialPath + getGame(gameIdx)[1] + ".txt";
//		String level1 = gamesPath + getGame(gameIdx)[1] + "_lvl" + levelIdx + ".txt";
//		// 2. This plays a game in a level by the controller.
//		ArcadeMachine.runOneGame(game, level1, true, "tutorialGeneration.biasedOnetreeMCTS.Agent", recordActionsFile, seed, 0);
//	}
}
