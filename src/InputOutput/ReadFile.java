package InputOutput;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import Jazz.PhraseParameters;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
 


public class ReadFile {
	 
	String path;
	public ReadFile(String file_path){

		path = file_path;
	}
	
	public List<String> getLines(){
		
		FileReader fr;
		List<String> lines = new ArrayList<String>();
		try {
			fr = new FileReader(path);
			BufferedReader textReader = new BufferedReader(fr);
			
			String line;
			while((line = textReader.readLine()) != null){		
				lines.add(line);
			}
			textReader.close();
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return lines;
		
	}
	
	public List<List<Integer>> interpretPhraseParams(List<String> phraseBoundaries){
			
		List<List<Integer>> phraseLocs = new ArrayList<List<Integer>>();
		for(int i=0;i<phraseBoundaries.size();i++){
			
			String[] temp =  phraseBoundaries.get(i).split(",");

			if(temp[0].contains(":")){
				
				//format 1:3,3:4  = measure 1 beat 3, measure 3 beat 5
				String[] start = temp[0].split(":");
				String[] end = temp[1].split(":");
				phraseLocs.add(new ArrayList<Integer>());
				
				Integer pStart = (Integer.parseInt(start[0])-1)*4 + Integer.parseInt(start[1])-1; //****ASSUMES 4 beats per measure for now!!!!!!!*********
				Integer pEnd = (Integer.parseInt(end[0])-1)*4 + Integer.parseInt(end[1])-1;
				
				phraseLocs.get(i).add(pStart);
				phraseLocs.get(i).add(pEnd);

			}else{

				//format 4, 15 = beat 4, beat 15
				phraseLocs.add(new ArrayList<Integer>());
				phraseLocs.get(i).add(Integer.parseInt(temp[0]));
				phraseLocs.get(i).add(Integer.parseInt(temp[1]));
			}

		}
		
		System.out.println(phraseLocs);
		return phraseLocs;	
	}
	
	public int[][]interpretScript(List<String> movementInstructions){
		
		int[][] instructions = new int[movementInstructions.size()][7];
		for(int i=0;i<movementInstructions.size();i++){
			
			/* Format looks like this "5 9 23 32 23 24896 1"  
				first 4 are arm positions
				5th is note to play
				6th is configuration number,
				7th is play time using divisions as time unit			
			*/
			String[] temp =  movementInstructions.get(i).split(" ");
			for(int j=0;j<temp.length;j++){
				instructions[i][j] = Integer.parseInt(temp[j])+48;
				
			}
		}
		return instructions;	
	}
	
	public List<PhraseParameters> readPhrasesJSONFile(){
		
		List<PhraseParameters> phraseInfo = new ArrayList<PhraseParameters>();
		
		
		
		try {
			// read the json file
			FileReader reader = new FileReader(path);

			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);

			// get a String from the JSON object
			String songName = (String) jsonObject.get("song");
			System.out.println("The song name is: " + songName);


			// get an array from the JSON object
			JSONArray phrases= (JSONArray) jsonObject.get("phrases");
			

			Iterator iter = phrases.iterator();
			// take each value from the json array separately
			while (iter.hasNext()) {
				phraseInfo.add(new PhraseParameters());
				JSONObject phraseObject = (JSONObject) iter.next();
				
				
				JSONObject innerObj = (JSONObject) phraseObject.get("phrase");
				phraseInfo.get(phraseInfo.size()-1).bpm =  Integer.parseInt((String) innerObj.get("bpm"));
				phraseInfo.get(phraseInfo.size()-1).constraints =  Integer.parseInt((String) innerObj.get("constraints"));
				if(  ((String) innerObj.get("style")).equals("melody") ){
					phraseInfo.get(phraseInfo.size()-1).readMelody = true;
				}else if( ((String) innerObj.get("style")).equals("improvise") ){
					phraseInfo.get(phraseInfo.size()-1).readMelody = false;
				}
				
				
				JSONObject locObj = (JSONObject) innerObj.get("start");
				phraseInfo.get(phraseInfo.size()-1).startMeasure = Integer.parseInt((String) locObj.get("measure"));
				phraseInfo.get(phraseInfo.size()-1).startBeat = Integer.parseInt((String) locObj.get("beat"));
				
				locObj = (JSONObject) innerObj.get("end");
				phraseInfo.get(phraseInfo.size()-1).endMeasure = Integer.parseInt((String) locObj.get("measure"));
				System.out.println("measure num = "+ phraseInfo.get(phraseInfo.size()-1).endMeasure);
				phraseInfo.get(phraseInfo.size()-1).endBeat = Integer.parseInt((String) locObj.get("beat"));
				
				System.out.println("");
				
				
			}

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ParseException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}

		
		
		return phraseInfo;
		
	}

}
