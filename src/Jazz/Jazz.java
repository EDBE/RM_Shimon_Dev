package Jazz;

import Embodiment.PhysicalConstraints_ShimonComplete;
import Embodiment.PhysicalConstraints_ShimonLowerHalf;
import Embodiment.PhysicalConstraints_ShimonLowerOctave;
import Embodiment.PhysicalConstraints_ShimonReduced;
import Embodiment.PhysicalConstraints_ShimonUpperHalf;
import Embodiment.PhysicalConstraints_ShimonUpper3Octaves;
import Embodiment.Physicality;
import MusicXML.MusicXML;
import MachineLearning.TonalCentersPerceptron;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*The primary class for all the jazz stuff*/

public class Jazz {
	
	
	Song currentSong;
	List<Song> songList = new ArrayList<Song>();
	List<Integer> currentNoteSequence = new ArrayList<Integer>();
	List<Integer> currentScaleSequence = new ArrayList<Integer>();
	//public NoteGenerator phraseGenerator = new NoteGenerator();
	//public ConstrainedNoteGenerator phraseGenerator;
	public PitchGenerator phraseGenerator;
	public RhythmGenerator rhythmGenerator = new RhythmGenerator();
	Map<Integer,PitchGenerator> phraseGenerators = new HashMap<Integer,PitchGenerator>();

	int divisionOfSong;
	int bpm = 145;
	public Jazz(){
		
	}
	
	public Jazz(String fileName){
		
		MusicXML musicXML = new MusicXML(fileName);
		currentSong = musicXML.parse();
		
		songList.add(currentSong);
		divisionOfSong = currentSong.divisions;
		
		//currentSong.analyzeSong();
		

		
	}
	
	public void createGenerators(List<PhraseParameters> params){
		Set<Integer> constraintTypes = new HashSet<Integer>();
		for(int i=0;i<params.size();i++){
			constraintTypes.add(params.get(i).constraints);
		}
		
		
		for (Integer type : constraintTypes) {
			switch(type){
			case 0:
				Physicality pConstraints_Complete = new PhysicalConstraints_ShimonComplete();
				phraseGenerators.put(type, new ConstrainedNoteGenerator(pConstraints_Complete));
				break;
			case 1:
				Physicality pConstraints_Reduced = new PhysicalConstraints_ShimonReduced();
				phraseGenerators.put(type,new ConstrainedNoteGenerator(pConstraints_Reduced));
				break;
			case 2:
				Physicality pConstraints_LowerHalf = new PhysicalConstraints_ShimonLowerHalf();
				phraseGenerators.put(type, new ConstrainedNoteGenerator(pConstraints_LowerHalf));
				break;
			case 3:
				Physicality pConstraints_UpperHalf = new PhysicalConstraints_ShimonUpperHalf();
				phraseGenerators.put(type, new ConstrainedNoteGeneratorUpperHalf(pConstraints_UpperHalf));
				break;
			case 4:
				Physicality pConstraints_Upper3Octaves = new PhysicalConstraints_ShimonUpper3Octaves();
				phraseGenerators.put(type, new ConstrainedNoteGeneratorUpperHalf(pConstraints_Upper3Octaves));
				break;
			case 5:
				Physicality pConstraints_LowerOctave = new PhysicalConstraints_ShimonLowerOctave();
				phraseGenerators.put(type, new ConstrainedNoteGenerator(pConstraints_LowerOctave));
				break;
			}
			
		}

	}
	
	public void trainTonalCenters(){
		TonalCentersPerceptron tonalCentersPerceptron = new TonalCentersPerceptron();
		tonalCentersPerceptron.trainStructuredPerceptron(songList);
	}
	
	public void createImprovisationForSong(List<PhraseParameters> params){
		List<List<Integer>> phraseLocs =segmentSongIntoPhrases();
		generatePhrases(phraseLocs,params);
		
	}
	
	public void generateMelodyScript(List<PhraseParameters> params){
		
	}
	
	public void createImprovisationForSong(List<List<Integer>> phraseLocs, List<PhraseParameters> params){
		generatePhrases(phraseLocs,params);
	}
	
	public List<List<Integer>> segmentSongIntoPhrases(){
		return identifyPhraseLocations(currentSong.formattedMelody);
		//return identifyPhraseLocations(changeDivisionsTo12_Melody(formatIntegerMeasuredList(currentSong.instaneousMelody,0,currentSong.instaneousMelody.size()),currentSong.divisions));
	}
	
	
	private List<List<Integer>> identifyPhraseLocations(List<Integer> melody){
		
		int iterCount=0, phraseCount=0, tempCount=0;;
		int[] rhythm = new int[melody.size()];
		List<List<Integer>> phraseLocations = new ArrayList<List<Integer>>();
		boolean endOfPhrase;
		
		int restSpaceLevel=2; //3 =more space between phrases, 2 medium space between phrases, 1 smaller space between phrases
		int totalBeatCount, beatNum;
		int start,end;
		while(iterCount < melody.size()){

			totalBeatCount = (int)(iterCount /12.0);
			beatNum = totalBeatCount%4;
			
			rhythm[iterCount] = melody.get(iterCount);
			if(melody.get(iterCount) != -1){
				System.out.println(melody.get(iterCount));
				ArrayList<Integer> phraseTuple = new ArrayList<Integer>();
				start = totalBeatCount;
				endOfPhrase = false;
				phraseCount=0;
				tempCount = 1;
				while(endOfPhrase == false && tempCount+iterCount < melody.size()){
					if(melody.get(tempCount+iterCount) !=-1){
						phraseCount=0;
					}
					if(phraseCount > (12 * restSpaceLevel)){
						endOfPhrase = true;
					}
					
					phraseCount++;
					tempCount++;
				}
				end = (int)((iterCount+tempCount -phraseCount) /12.0)+1;
				phraseTuple.add(start);
				phraseTuple.add(end);
				phraseLocations.add(phraseTuple);
				//rhythm[totalBeatCount] =rhythm[totalBeatCount]+100;
				//rhythm[(iterCount+tempCount) %12] =rhythm[totalBeatCount]-100;
				iterCount+=tempCount;
				//createRhythmicPhrase(totalBeatCount, restSpaceLevel, noteDensity, rhythmicComplexity, iterCount, iterCount+tempCount, melody);
			}
			iterCount++;
		}
		return phraseLocations;
	}
	
	public List<Integer> updatePhrase(List<Integer> phraseLoc, PhraseParameters params){
		
		List<Integer> melody = getRelevantMelody(phraseLoc.get(0),phraseLoc.get(1));
		List<Integer> phrase = generatePhrase(melody,params,phraseLoc.get(0));
		System.out.println("phrase = " +phrase);
		
		//create sequence of commands for final output playing
		int startPoint = (int) (phraseLoc.get(0)*(divisionOfSong));
		for(int j=0;j<phrase.size();j++){
			currentNoteSequence.set(startPoint+j, phrase.get(j));
			
		}
		return currentNoteSequence;
	}
	
	public List<Integer>  generatePhrases(List<List<Integer>> phraseLocs, List<PhraseParameters> params){
		
		List<Integer> finalSequence = new ArrayList<Integer>();
		currentScaleSequence = new ArrayList<Integer>();
		for(int i=0;i<1;i++){
		//for(int i=0;i<phraseLocs.size();i++){
			bpm = params.get(i).bpm;
			System.out.println("generating phrase number "+ i);
			System.out.println(" using physical constraints generator " +params.get(i).constraints + " with bpm = " + bpm);
			phraseGenerator = phraseGenerators.get(params.get(i).constraints);
			
			//create the phrase
			List<Integer> melody = getRelevantMelody(phraseLocs.get(i).get(0),phraseLocs.get(i).get(1));
			List<Integer> phrase;
			if(params.get(i).readMelody == true){	
				List<List<Integer>> melodyTimings = getMelodyTimings(melody);
				phrase = playMelody(melodyTimings);
			}else{
				phrase = generatePhrase(melody,params.get(i),phraseLocs.get(i).get(0));
			}


			//create sequence of commands for final output playing
			for(int j=0;j<phrase.size();j++){
				finalSequence.add(phrase.get(j));
			}
			if(i+1 < phraseLocs.size()){
				for(int j=0;j<(phraseLocs.get(i+1).get(0)*divisionOfSong - phraseLocs.get(i).get(1)*divisionOfSong);j++){
					finalSequence.add(-1);
					currentScaleSequence.add(currentScaleSequence.get(currentScaleSequence.size()-1));
				}
			}
		}
		currentNoteSequence= finalSequence;
		return finalSequence;
		
	}
	
	private List<Integer> playMelody(List<List<Integer>> melody){
		
		float unitTime =  60000.0f / bpm / divisionOfSong; 
		
		int timeLimit = (int) (185.0f / unitTime + 1);
		
		List<List<Integer>>	sequences = phraseGenerator.generatePhrase(melody,timeLimit);
		return sequences.get(0);
		
	}
	
	private List<Integer> generatePhrase(List<Integer> melody, PhraseParameters params, int startBeat){
		
		List<float[]> rhythmicPhrase = generateRhythmicPhrase2(melody,params);
		int duration = getDuration(rhythmicPhrase);
		
		List<List<Integer>>	sequences = phraseGenerator.generatePhrase(
				rhythmicPhrase,
				getHarmonyTypesAtIndices(rhythmicPhrase,startBeat),
				getHarmonyRootsAtIndices(rhythmicPhrase,startBeat),
				currentSong.tonalCenters,
				params,
				duration);
		
		List<Integer> tempNoteSequence = new ArrayList<Integer>();
		List<Integer> tempScaleSequence = new ArrayList<Integer>();
		for(int i=0;i<rhythmicPhrase.get(0)[3];i++){
			tempNoteSequence.add(-1);
			tempScaleSequence.add(sequences.get(1).get(0));
		}
		
		for(int i=0;i<rhythmicPhrase.size();i++){
			tempNoteSequence.add(sequences.get(0).get(i));
			tempScaleSequence.add(sequences.get(1).get(i));
			for(int j=1;j<rhythmicPhrase.get(i)[1];j++){
				tempNoteSequence.add(-1);
				tempScaleSequence.add(sequences.get(1).get(i));
			}
		}
		
		for(int j=0;j<tempScaleSequence.size();j++){
			currentScaleSequence.add(tempScaleSequence.get(j));
		}

		
		//System.out.println("adjusted size = " + tempNoteSequence.size());
		return tempNoteSequence;
	}
	
	public void generateRhythmicPhrase(){
		rhythmGenerator.generateRhythm(formatIntegerMeasuredList(currentSong.instaneousMelody,0,36), currentSong.divisions, currentSong.tonalCenters);
	}
	public List<float[]> generateRhythmicPhrase1(PhraseParameters params){
		return rhythmGenerator.createRhythmicPhrase(changeDivisionsTo12_Melody(formatIntegerMeasuredList(currentSong.instaneousMelody,0,4),currentSong.divisions), params);
	}
	
	public List<float[]> generateRhythmicPhrase2(List<Integer> melody, PhraseParameters params){
		return rhythmGenerator.createRhythmicPhrase(melody, params);

	}
	
	private List<List<Integer>> getMelodyTimings(List<Integer> relevantMelody){
		
		int count = 1;
		List<List<Integer>> melodyInfo = new ArrayList<List<Integer>>();
		for(int i=0;i<relevantMelody.size();i++){
			if(relevantMelody.get(i)!= -1){
				melodyInfo.add(new ArrayList<Integer>());
				melodyInfo.get(melodyInfo.size()-1).add(relevantMelody.get(i));
				melodyInfo.get(melodyInfo.size()-1).add(count);
				count = 1;
			}else{
				count++;
			}
		}
		return melodyInfo;
	}

	
	private List<Integer> getRelevantMelody(int startBeat, int endBeat){
		
		int startPoint = startBeat*divisionOfSong;
		int endPoint = endBeat * divisionOfSong;	
		return currentSong.formattedMelody.subList(startPoint, endPoint);
	}
	
	/*
	public List<Integer> generatePhrase(PhraseParameters params, int startBeat){
		
		List<float[]> melody = generateRhythmicPhrase1(params);
		int duration = getDuration(melody);
		
		List<List<Integer>>	sequences = phraseGenerator.generatePhrase(
				melody,
				getHarmonyTypesAtIndices(melody,startBeat),
				getHarmonyRootsAtIndices(melody,startBeat),
				currentSong.tonalCenters,
				params,
				duration);
		

		currentNoteSequence = new ArrayList<Integer>();
		currentScaleSequence = new ArrayList<Integer>();
		for(int i=0;i<melody.size();i++){
			currentNoteSequence.add(sequences.get(0).get(i));
			currentScaleSequence.add(sequences.get(1).get(i));
			for(int j=1;j<melody.get(i)[1];j++){
				currentNoteSequence.add(-1);
				currentScaleSequence.add(sequences.get(1).get(i));
			}
		}
		System.out.println("adjusted size = " + currentNoteSequence.size());
		return currentNoteSequence;
			
	}
*/
	public List<Integer>getScaleSequence(){
		return currentScaleSequence;
	}
	
	public List<Integer>getHarmonyRoots(){
		return currentSong.formattedHarmonyRoots;
		//return changeDivisionsTo12_Integer(formatIntegerMeasuredList(currentSong.instaneousHarmonyRoots,0,36),currentSong.divisions);
	}
	public List<String>getHarmonyTypes(){
		return currentSong.formattedHarmonyTypes;
		//return changeDivisionsTo12_String(formatStringMeasuredList(currentSong.instaneousHarmonyTypes,0,36),currentSong.divisions);
	}
	public List<Integer>getMelodyNotes(){
		return currentSong.formattedMelody;
		//return formatIntegerMeasuredList(currentSong.instaneousMelody,0,36);
	}
	
	
	private List<String> formatStringMeasuredList(List<List<String>> measuredList, int start, int stop){
		
		List<String> nonMeasuredList = new ArrayList<String>();
		int count=0;
		for(int i =start;i<stop;i++){
			for(int j=0;j<measuredList.get(i).size();j+=1){
				nonMeasuredList.add(measuredList.get(i).get(j));
				/*if(count >=stop){
					return nonMeasuredList;
				}
				count++;*/
			}
		}
		return nonMeasuredList;
	}
	
	private List<Integer> formatIntegerMeasuredList(List<List<Integer>> measuredList, int start, int stop){
		
		List<Integer> nonMeasuredList = new ArrayList<Integer>();
		int count=0;
		for(int i =start;i<stop;i++){
			for(int j=0;j<measuredList.get(i).size();j+=1){
				nonMeasuredList.add(measuredList.get(i).get(j));
				/*if(count >=stop){
					return nonMeasuredList;
				}
				count++;*/
			}
		}
		return nonMeasuredList;
	}
	
	private List<String> useAlteredChords(List<String> chordTypes){
		
		for(int i=0;i<chordTypes.size();i++){
			if(chordTypes.get(i).equalsIgnoreCase("7")){
				chordTypes.set(i,"7b9#9#11b13");
			}
		}
		return chordTypes;
		
	}
	
	private List<Integer> changeDivisionsTo12_Melody(List<Integer> list, int divisions){
		if(divisions<12){
			//need a division to support sixteenth and sixteenth triplets
			int additional = 12/divisions;
			System.out.println("WARNING Divsions var is being modified! from "+divisions +" to "+ 12);
			for(int i=list.size();i>0;i--){
				for(int j=1;j<additional;j++){
					list.add(i, -1);
				}
			}
		}
		
		return list;
	}
	
	private List<Integer> changeDivisionsTo12_Integer(List<Integer> list, int divisions){
		if(divisions<12){
			//need a division to support sixteenth and sixteenth triplets
			int additional = 12/divisions;
			System.out.println("WARNING Divsions var is being modified! from "+divisions +" to "+ 12);
			for(int i=list.size();i>0;i--){
				for(int j=1;j<additional;j++){
					list.add(i, list.get(i-1));
				}
			}
		}
		
		return list;
	}
	
	private List<String> changeDivisionsTo12_String(List<String> list, int divisions){
		if(divisions<12){
			//need a division to support sixteenth and sixteenth triplets
			int additional = 12/divisions;
			System.out.println("WARNING Divsions var is being modified! from "+divisions +" to "+ 12);
			for(int i=list.size();i>0;i--){
				for(int j=1;j<additional;j++){
					list.add(i, list.get(i-1));
				}
			}
		}
		
		return list;
	}
	
	private List<int[]> changeDivisionsTo12_Array(List<int[]> list, int divisions){
		if(divisions<12){
			//need a division to support sixteenth and sixteenth triplets
			int additional = 12/divisions;
			System.out.println("WARNING Divsions var is being modified! from "+divisions +" to "+ 12);
			for(int i=list.size();i>0;i--){
				for(int j=1;j<additional;j++){
					list.add(i, list.get(i-1));
				}
			}
		}
		
		return list;
	}
	
	
	private int getDuration (List<float[]> melody){
		/*
		 * Returns the total duration of the melodic sequence
		 */
		int duration=0;
		for(int i=0;i<melody.size();i++){
			duration += melody.get(i)[1];
		}
		System.out.println(duration);
		return duration;
	}

	private List<String> getHarmonyTypesAtIndices(List<float[]> melody, int startIndex){
		int index=(startIndex*12);
		List<String> indexed = new ArrayList<String>();
		//List<String> formatted = changeDivisionsTo12_String(formatStringMeasuredList(currentSong.instaneousHarmonyTypes,startIndex,startIndex+duration),currentSong.divisions);
		for(int i=0;i<melody.size();i++){
			indexed.add(currentSong.formattedHarmonyTypes.get(index));
			index+=melody.get(i)[1];
		}
		return indexed;
	}
	private List<Integer> getHarmonyRootsAtIndices(List<float[]> melody, int startIndex){
		int index=(startIndex*12);
		List<Integer> indexed = new ArrayList<Integer>();
		//List<Integer> formatted = changeDivisionsTo12_Integer(formatIntegerMeasuredList(currentSong.instaneousHarmonyRoots,startIndex,startIndex+duration),currentSong.divisions);
		for(int i=0;i<melody.size();i++){
			indexed.add(currentSong.formattedHarmonyRoots.get(index));
			index+=melody.get(i)[1];
		}
		return indexed;
	}
	private List<int[]> getTonalCentersAtIndices(List<float[]> melody, int startIndex, int duration){
		int index=startIndex;
		List<int[]> indexed = new ArrayList<int[]>();
		List<int[]> formatted = changeDivisionsTo12_Array(currentSong.tonalCenters,currentSong.divisions);

		for(int i=0;i<melody.size();i++){
			indexed.add(formatted.get(index));
			index+=melody.get(i)[1];
		}
		return indexed;
	}
	
}
