package tutorialGeneration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import video.basics.PlayerAction;
import video.basics.GameEvent;
import video.basics.Interaction;


public class PlaytraceParser {
	private String game;
	private ArrayList<ArrayList<GameEvent>> events;
	private ArrayList<Integer> eventsResults;
	private ArrayList<GameEvent> criticalSet;
	private int criticalWinTime;
	
	
	public PlaytraceParser(String gameName) {
		this.game = gameName;
		this.events = new ArrayList<ArrayList<GameEvent>>();
		this.eventsResults = new ArrayList<Integer>();
	}
	
	public void parsePlaytraces() {
		// run thru directory of same name as game

		File dir = new File(game);
		if(!dir.exists()) {
			System.out.println("Error!" + game + " does not exist as a playtrace directory!");
			return;
		}
		//JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
		String[] pathnames = dir.list();
		for (String playtrace : pathnames) {
			//for each playtrace, read in and track the first time each unique mechanic happens
			try(FileReader reader = new FileReader(Paths.get(game, playtrace).toString())) {
				JSONObject obj = (JSONObject) jsonParser.parse(reader);
				
				JSONObject playResult = (JSONObject) ((JSONArray) obj.get("results")).get(0);
				JSONArray playEvents = (JSONArray) obj.get("events");
				
				if(playResult.get("result").equals("1")) {
					ArrayList<GameEvent> eventSet = new ArrayList<GameEvent>();
					for(Object o  : playEvents) {
						JSONObject event = (JSONObject) o;
						if (event.containsKey("action")) {
							// this is a PlayerAction
							PlayerAction a = new PlayerAction(
								event.get("tick").toString(),
								event.get("action").toString());
							if (this.isResident(eventSet,a)) {
								for (GameEvent sEvent : eventSet) {
							        if (this.equalEvents(sEvent, a)
							        		&& Integer.parseInt(a.gameTick) < Integer.parseInt(sEvent.gameTick)) {
							        	// check if a has lower frame
							        	eventSet.remove(sEvent);
							        	eventSet.add(a);
							        }
							      }
							} else {
								eventSet.add(a);
							}
						} else {
							// this is an interaction
							Interaction i = new Interaction(
								event.get("tick").toString(),
								event.get("interaction").toString(),
								event.get("sprite1").toString(),
								event.get("sprite2").toString());
							if (this.isResident(eventSet,i)) {
								// check if i has a lower frame
								for (GameEvent sEvent : eventSet) {
							        if (this.equalEvents(sEvent, i) 
							        		&& Integer.parseInt(i.gameTick) < Integer.parseInt(sEvent.gameTick)) {
							        	// check if a has lower frame
							        	eventSet.remove(sEvent);
							        	eventSet.add(i);
							        }
							      }
							} else {
								eventSet.add(i);
							}
						}
					}
					this.events.add(eventSet);
					this.eventsResults.add(Integer.parseInt((String)playResult.get("tick")));
				}
		
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// now we have a list of event sets, pick the one with the lowest mechanic count and store it as the key set
		ArrayList<GameEvent> lowestSet = this.events.get(0);
		int lowestWin = this.eventsResults.get(0);
		int count = 0;
		for(ArrayList<GameEvent> s : this.events) {
			if (lowestSet.size() > s.size()) {
				lowestSet = s;
				lowestWin = this.eventsResults.get(count);
			}
			count++;
		}
		this.criticalSet = (ArrayList<GameEvent>)lowestSet;
		this.criticalWinTime = lowestWin;
	}
	
	public int queryCriticalSet(Mechanic mech) {
		String condition = mech.getConditions().get(0).getName();
		String action = mech.getActions().get(0).getName();
		for(Object o : this.criticalSet) {
			if(condition.equals("Collides") && Interaction.class.isInstance(o)) {
				// interaction
				Interaction e = (Interaction) o;
				if(mech.getSprites().get(0).getName().equals(e.sprite1) 
						&& mech.getSprites().get(1).getName().equals(e.sprite2)
						&& this.equalActions(mech, e.rule)) {
					return Integer.parseInt(e.gameTick);
					
				} 
			}
			else if(action.equals("Spawn") && Interaction.class.isInstance(o)){
				Interaction e = (Interaction) o;

				if(mech.getSprites().get(0).getName().equals(e.sprite1)) {
					return Integer.parseInt(e.gameTick);
				
				} 
			} else if(condition.equals("Press Space") && PlayerAction.class.isInstance(o)) {
				PlayerAction e = (PlayerAction) o;
				if(e.action.equals("ACTION_USE")) {
					return Integer.parseInt(e.gameTick);
				}
			} else if(action.equals("Win")) {
				return this.criticalWinTime;
			}
			
				
		}
		// event not found
		return -1;
		
	}
	
	public boolean equalEvents(GameEvent e1, GameEvent e2) {
		if (PlayerAction.class.isInstance(e1) && PlayerAction.class.isInstance(e2)) {
			// there can only be one player action type per game
			return true;
			
			
		} else if (Interaction.class.isInstance(e1) && Interaction.class.isInstance(e2)) {
			Interaction i1 = (Interaction) e1;
			Interaction i2 = (Interaction) e2;
			// check all fields
			if(i1.rule.equals(i2.rule) && i1.sprite1.equals(i2.sprite1) && i1.sprite2.equals(i2.sprite2)) {
				return true;
			}
							
		} 
		// otherwise return false
		return false;
	}
	
	public boolean isResident(ArrayList<GameEvent> residents, GameEvent newGuy) {
		for (GameEvent resident : residents) {
			if(this.equalEvents(resident, newGuy)) {
				return true;
			}
		}
		
		return false;
	}
	public boolean equalActions(Mechanic mech, String rule) {
		for (Node action : mech.getActions()) {
			if(action.getName().equals(rule)) {
				return true;
			} else if(action.getName().equals("KillSprite") && rule.equals("KillBoth")) {
				return true;
			}
		}
		return false;
	}
}
