package Mains;
import java.util.ArrayList;
import java.util.List;

import Jazz.Jazz;
import Jazz.PhraseParameters;
import InputOutput.ReadFile;
public class Main {

	
	static Jazz jazzGenerator;
	static List<PhraseParameters> params= new ArrayList<PhraseParameters>();
	public static void main(String args[]) {
		
		
		//jazzGenerator = new Jazz("/Users/masonbretan/Desktop/iltur_short.xml");
		//ReadFile fileReader = new ReadFile("/Users/masonbretan/Desktop/ilturParams.txt");
		
		
		jazzGenerator = new Jazz("/Users/masonbretan/Desktop/what you say/whatusay.xml");
		ReadFile fileReader = new ReadFile("/Users/masonbretan/Desktop/what you say/what_you_say_params.txt");
		
		
		//jazzGenerator = new Jazz("/Users/masonbretan/Desktop/The Love Tango of Jupiter and Venus.xml");
		//ReadFile fileReader = new ReadFile("/Users/masonbretan/Desktop/DanceOfJupiterAndVenusParams.txt");
		params = fileReader.readPhrasesJSONFile();
		//ReadFile fileReader = new ReadFile("/Users/masonbretan/Desktop/phrase_params.txt");
		//List<String> lines = fileReader.getLines();
		//List<List<Integer>> phraseLocs = fileReader.interpretPhraseParams(lines);

		
		List<List<Integer>> phraseLocs = new ArrayList<List<Integer>>();
		for(int i=0;i<params.size();i++){
			phraseLocs.add(new ArrayList<Integer>());
			Integer pStart = (params.get(i).startMeasure-1)*4 + params.get(i).startBeat-1; //****ASSUMES 4 beats per measure for now!!!!!!!*********
			Integer pEnd = (params.get(i).endMeasure-1)*4 + params.get(i).endBeat-1;
			
			phraseLocs.get(i).add(pStart);
			phraseLocs.get(i).add(pEnd);
		}
		jazzGenerator.createGenerators(params);
		jazzGenerator.createImprovisationForSong(phraseLocs,params);
		
		
		
		/*
		
		
		//generate library of improvisations
		jazzGenerator = new Jazz("/Users/masonbretan/Desktop/improvisationScore.xml");
		ReadFile fileReader = new ReadFile("/Users/masonbretan/Desktop/phrase_params_improv.txt");
		params = fileReader.readPhrasesJSONFile();

		
		List<List<Integer>> phraseLocs = new ArrayList<List<Integer>>();
		for(int i=0;i<params.size();i++){
			phraseLocs.add(new ArrayList<Integer>());
			Integer pStart = (params.get(i).startMeasure-1)*4 + params.get(i).startBeat-1; //****ASSUMES 4 beats per measure for now!!!!!!!*********
			Integer pEnd = (params.get(i).endMeasure-1)*4 + params.get(i).endBeat-1;
			
			phraseLocs.get(i).add(pStart);
			phraseLocs.get(i).add(pEnd);
		}
		jazzGenerator.createGenerators(params);
		jazzGenerator.createImprovisationForSong(phraseLocs,params);
		
		*/
		
		
		
		
		/*
		
		
		//jazzGenerator = new Jazz("/Users/masonbretan/Desktop/music xml/all the things you are alt.xml");
 		jazzGenerator = new Jazz("/Users/masonbretan/Desktop/The Love Tango of Jupiter and Venus.xml");
		
		
		ReadFile fileReader = new ReadFile("/Users/masonbretan/Desktop/phrase_params.txt");
		List<String> lines = fileReader.getLines();
		List<List<Integer>> phraseLocs = fileReader.interpretPhraseParams(lines);
		
		fileReader = new ReadFile("/Users/masonbretan/Desktop/songScript.txt");
		lines = new ArrayList<String>(fileReader.getLines());
		int[][] instructions = fileReader.interpretScript(lines);
		
		
		for(int i=0;i<50;i++){
			params.add(new PhraseParameters());
		}
		//jazzGenerator.createImprovisationForSong(params);
		jazzGenerator.createImprovisationForSong(phraseLocs,params);
		
		*/

	}
	
}
