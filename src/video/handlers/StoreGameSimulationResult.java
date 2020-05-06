package video.handlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import video.basics.GameEvent;
import video.basics.PlayerAction;
import video.basics.Interaction;


public class StoreGameSimulationResult 
{
	public JSONObject gameResult;
	public JSONArray interactions;
	public JSONArray actions;
	
	public JSONArray eventArray;
	
	public StoreGameSimulationResult()
	{
		gameResult = new JSONObject();
		eventArray = new JSONArray();
	}

	public void storeGameSimulationResult(String result, String score, String tick)
	{
		this.gameResult.put("result", result);
		this.gameResult.put("score", score);
		this.gameResult.put("tick", tick);
	}
	
	public void convertEvents(ArrayList<GameEvent> events) {
		for(GameEvent e : events) {
			JSONObject eventObject = new JSONObject();
			eventObject.put("tick", e.gameTick);
			if(PlayerAction.class.isInstance(e)) {
				eventObject.put("action", ((PlayerAction) e).action);
				eventObject.put("sprite1", "Avatar");
			}else {
				eventObject.put("interaction", ((Interaction) e).rule);
				eventObject.put("sprite1", ((Interaction) e).sprite1);
				eventObject.put("sprite2", ((Interaction) e).sprite2);
				eventObject.put("pairInteractionTick", ((Interaction) e).pairInteractionTick);
			}
			eventArray.add(eventObject);
		}
	}
	
	public void writeAllInfo(String myFile) throws IOException {
		System.out.println(myFile);
		File simFile = new File(myFile);
		if (!simFile.exists()) {
			simFile.createNewFile();
		}
		try (FileWriter file = new FileWriter(myFile)) {
			
			file.write("{\"events\":[");
			int i = 0;
			for(Object obj : eventArray) {
				file.write(((JSONObject)obj).toString());
				if(i < interactions.size()-1) {
					file.write(",\n");
					i++;
				}
			}
			file.write("],\n\"results\":[");
			file.write(gameResult.toJSONString());
			file.write("]}");
			file.flush();
			file.close();
		}
	}
	
	public void addMechanics(JSONArray interactions, JSONArray actions) {
		this.interactions = interactions;
		this.actions = actions;
	}
}
