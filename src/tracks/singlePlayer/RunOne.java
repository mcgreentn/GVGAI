package tracks.singlePlayer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Random;

import core.logging.Logger;
import tools.Utils;
import tracks.ArcadeMachine;
import video.constants.InteractionStaticData;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 04/10/13 Time: 16:29 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class RunOne {

    public static void main(String[] args) throws IOException {
    	
		//Load available games
		String spGamesCollection =  "examples/atDelfi_games.csv";
		String[][] games = Utils.readGames(spGamesCollection);

		//Load available agents
		String[] agents = RunOne.generateAgents();
		
		//Game settings
		int seed = new Random().nextInt();		
		int gameIdx = Integer.parseInt(args[0]);
		int agentStart = Integer.parseInt(args[1]);
		int totalCount = Integer.parseInt(args[2]);
		boolean visuals = false;
		boolean recordActions = Boolean.parseBoolean(args[3]);
		
		runMany(games, agents, agentStart, totalCount, totalCount, totalCount, totalCount, visuals, recordActions);
    }
    
    public static void runMany(String[][] games, String[] agents, int agentStart, int gameIdx, int totalCount, int playthroughTotal, int seed, boolean visuals, boolean recordActions) {
		String gameName = games[gameIdx][1];
		String game = games[gameIdx][0];
		
    	for(int k = 0; k < totalCount; k++) {
    		String agent = agents[agentStart * totalCount + k];
    		InteractionStaticData.agentName = agent;
    		for (int j = 0; j < 5; j++) {
    			String level = game.replace(gameName, gameName + "_lvl" + j);
    			for(int i = 0; i < playthroughTotal; i++) {
    				InteractionStaticData.playthroughCount = "" + i;
    				InteractionStaticData.levelCount = "" + j;
    				String recordActionsFile = agent + "_lvl"
    						+ j + "_playthrough" + i + "_" + seed + ".txt";
    				try {
    				ArcadeMachine.runOneGame(game, level, visuals, agent, recordActionsFile, seed, 0);
    				}
    				catch(Exception e) {
    					System.err.println("Failed for the following reason:\n" + e);
    				}
    			}
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
				    return new File(current, name).isDirectory();
				  }
				});
			
			for(int i = 0; i < agents.length; i++) {
				agents[i] = "agents." + agents[i] + ".Agent";
			}
			
			return agents;
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
    
}
