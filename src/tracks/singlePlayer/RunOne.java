package tracks.singlePlayer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import core.logging.Logger;
import tools.Utils;
import tracks.ArcadeMachine;
import video.constants.InteractionStaticData;

/**
 * Author: Michael Green
 */
public class RunOne {

    public static void main(String[] args) throws IOException {
    	
		//Load available games
		String spGamesCollection =  "examples/atDelfi_games.csv";
		String[][] games = Utils.readGames(spGamesCollection);

		//Load available agents
		String[] agents = RunOne.generateAgents();
		
		//Game settings
		// A seed
		int seed = new Random().nextInt();	
		// the IDX of the game to play
		int gameIdx = Integer.parseInt(args[0]);
		// the index within the agents[] array from which to start playing
		int agentStart = Integer.parseInt(args[1]);
		// ???
		int totalCount = Integer.parseInt(args[2]);
		// how many playthroughs an individual level should have
		int playthroughTotal = Integer.parseInt(args[3]);
		// whether or not to have a display
		boolean visuals = false;
		// whether to record all agent actions for replay
		boolean recordActions = Boolean.parseBoolean(args[4]);
		
		runMany(games, agents, agentStart, gameIdx, totalCount, playthroughTotal, seed, visuals, recordActions);
    }
    
    public static void runMany(String[][] games, String[] agents, int agentStart, int gameIdx, int totalCount, int playthroughTotal, int seed, boolean visuals, boolean recordActions) {
		String gameName = games[gameIdx][1];
		String game = games[gameIdx][0];
		InteractionStaticData.gameName = gameName;
		File gameDir = new File(gameName);
		
		System.out.println(gameName);
		try {
			if(!gameDir.exists()) {
				gameDir.mkdir();
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	for(int k = 0; k < totalCount; k++) {
    		if (agents.length - 1 > agentStart * totalCount + k) {
	    		String agent = agents[agentStart * totalCount + k];
	    		System.out.println(agent);
	    		InteractionStaticData.agentName = agent;
	    		for (int j = 0; j < 1; j++) {
	    			String level = game.replace(gameName, gameName + "_lvl" + j);
	    			for(int i = 0; i < playthroughTotal; i++) {
	    				InteractionStaticData.playthroughCount = "" + i;
	    				InteractionStaticData.levelCount = "" + j;
	    				String recordActionsFile = null;
	    				if (recordActions) {
		    				recordActionsFile = agent + "_lvl"
		    						+ j + "_playthrough" + i + "_" + seed + ".txt";
	    				}
	    				try {
	    				ArcadeMachine.runOneGame(game, level, visuals, agent, recordActionsFile, seed, 0);
	    				}
	    				catch(Exception e) {
	    					System.err.println("Failed for the following reason:\n" + e);
	    				}
	//    				System.out.println("Agent: " + agent + ", level: " + j + ", playthrough: " + i);
	    			}
	    		}
    		} else {
    			System.out.println("Over Agent size. Skipping!");
    		}
    	}
    }
    
    public void runOne() {
    	
    }
    
	public static String[] generateAgents() {
		try {
			File agentsDirectory = new File("src/agents");
			String[] agents = agentsDirectory.list(new FilenameFilter() {
				  @Override
				  public boolean accept(File current, String name) {
					File agentFile = new File(current, name + "/Agent.java");
				    return new File(current, name).isDirectory() && agentFile.exists();
				  }
				});
			
			for(int i = 0; i < agents.length; i++) {
				if (!agents[i].equals("Heuristics")) {
					agents[i] = "agents." + agents[i] + ".Agent";
				}
			}
			Arrays.sort(agents);
			return agents;
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
    
}
