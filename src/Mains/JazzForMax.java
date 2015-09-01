package Mains;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

import InputOutput.ReadFile;
import Jazz.Jazz;
import Jazz.PhraseParameters;
import Jazz.Chords;
import Jazz.ScaleTheory;

import com.cycling74.max.*;

public class JazzForMax extends MaxObject
{
	Jazz jazzGenerator;
	List<Integer> phrase;
	List<Integer> harmonyRoots;
	List<String> harmonyTypes;
	List<Integer> melodyNotes;
	List<Integer> scaleSequence;
	List<List<Integer>> phraseLocations;
	int phraseNum;
	Chords chords = new Chords();
	ScaleTheory scaleTheory = new ScaleTheory();
	Integer lastRoot = -1;
	String lastType = "";
	Random rand = new Random();
	PhraseParameters phraseVars = new PhraseParameters();
	List<PhraseParameters> phraseParams;
	
	
	//script variables
	int[][] instructions;
	int instructionCount = 0;
	int timeUnitCount = 1; 
	
	int[][] improvInstructions;
	int improvInstructionCount = 0;
	
	int step = 0,stepMin=0,stepMax = 10;

	//outlets
	int PITCH_PARAM = 4;
	int COLOR_PARAM = 5;
	int HARMONIC_PARAM = 6;
	int DENSITY_PARAM = 7;
	int COMPLEXITY_PARAM = 8;
	int[] ARM_COMMANDS = new int[]{9,10,11,12};
	
	boolean playingBafana=false;

	
	public JazzForMax(Atom[] args)
	{
		declareInlets(new int[]{DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL});
		declareOutlets(new int[]{DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL});
		
	}

	public void bang()
	{


		
		if(lastType.equalsIgnoreCase(harmonyTypes.get(step))!=true || lastRoot!= harmonyRoots.get(step)){
			
		//if(rand.nextFloat() <.1){
			int[] chordNotes = chords.chordTexts.get(harmonyTypes.get(step));
			for(int i=0;i<chordNotes.length;i++){
				if(chordNotes[i] == 1){
					outlet(2,harmonyRoots.get(step)+i);
				}
			}
		}
		lastType = harmonyTypes.get(step);
		lastRoot = harmonyRoots.get(step);
		
		String tempString = chords.rootNames.get(harmonyRoots.get(step))+" " +scaleTheory.scales.get(scaleSequence.get(step));
		outlet(3,"set",tempString);
		
		/*
		if(melodyNotes.get(step)!=-1){
			outlet(1,127);
		}else{
			outlet(1,100);
		}*/
		if(phrase.get(step)!=-1){
		outlet(1,127);
		outlet(0,phrase.get(step));
		}
		
		step++;
		if(step >=stepMax){
			step=stepMin;
		}
	}
	public void goToStep(int i){
		step = i;
	}

	public void inlet(int i)
	{
		
		if(playingBafana == true){
			
			
			
		}
		
	}
/*
	public void inlet(float f)
	{
		
		int inlet = getInlet();
		switch(inlet){
		case 0:
			phraseVars.pitchWeight = f;
			break;
		case 1:
			phraseVars.colorWeight = f;
			break;
		case 2:
			phraseVars.densityWeight = f;
			break;
		case 3:
			phraseVars.complexityWeight = f;
			break;
		case 4:
			phraseVars.harmonicTensionWeight = f;
			break;
		}
		
	}
*/

	/*
	public void list(Atom[] list)
	{
		
		int inlet = getInlet();
		switch(inlet){
		case 0:
			for(int i=0;i<list.length;i++){
				phraseVars.pitchContours[i] = list[i].toFloat();
			}
			break;
		case 1:
			for(int i=0;i<list.length;i++){
				phraseVars.colorContours[i] = list[i].toFloat();
			}
			break;
		case 2:
			for(int i=0;i<list.length;i++){
				phraseVars.densityContours[i] = list[i].toFloat();
			}
			break;
		case 3:
			for(int i=0;i<list.length;i++){
				phraseVars.complexityContours[i] = list[i].toFloat();
			}
			break;
		case 4:
			for(int i=0;i<list.length;i++){
				phraseVars.harmonicTensionContours[i] = list[i].toFloat();
			}
			break;
		}
	}
	*/
	
	public void inlet(float f)
	{
		
		int inlet = getInlet();
		switch(inlet){
		case 0:
			phraseParams.get(phraseNum).pitchWeight = f;
			break;
		case 1:
			phraseParams.get(phraseNum).colorWeight = f;
			break;
		case 2:
			phraseParams.get(phraseNum).densityWeight = f;
			break;
		case 3:
			phraseParams.get(phraseNum).complexityWeight = f;
			break;
		case 4:
			phraseParams.get(phraseNum).harmonicTensionWeight = f;
			break;
		}
		
	}
	
	public void list(Atom[] list)
	{
		
		int inlet = getInlet();
		switch(inlet){
		case 0:
			for(int i=0;i<list.length;i++){
				phraseParams.get(phraseNum).pitchContours[i] = list[i].toFloat();
			}
			break;
		case 1:
			for(int i=0;i<list.length;i++){
				phraseParams.get(phraseNum).colorContours[i] = list[i].toFloat();
			}
			break;
		case 2:
			for(int i=0;i<list.length;i++){
				phraseParams.get(phraseNum).densityContours[i] = list[i].toFloat();
			}
			break;
		case 3:
			for(int i=0;i<list.length;i++){
				phraseParams.get(phraseNum).complexityContours[i] = list[i].toFloat();
			}
			break;
		case 4:
			for(int i=0;i<list.length;i++){
				phraseParams.get(phraseNum).harmonicTensionContours[i] = list[i].toFloat();
			}
			break;
		}
	}
	
	
	
	public void createPhrase(){
		//phrase = jazzGenerator.generatePhrase(phraseVars);
		
		harmonyRoots = jazzGenerator.getHarmonyRoots();
		harmonyTypes = jazzGenerator.getHarmonyTypes();
		//melodyNotes = jazzGenerator.getMelodyNotes();
		scaleSequence = jazzGenerator.getScaleSequence();
		
	}
	
	public void createRhythmicPhrase(){
		jazzGenerator.generateRhythmicPhrase1(phraseVars);
	}
	
	public void createImprovisation(){
		phrase = jazzGenerator.generatePhrases(phraseLocations,phraseParams);
		harmonyRoots = jazzGenerator.getHarmonyRoots();
		harmonyTypes = jazzGenerator.getHarmonyTypes();
		scaleSequence = jazzGenerator.getScaleSequence();
	}
	
	public void segmentPhrases(){
		phraseLocations = jazzGenerator.segmentSongIntoPhrases();
		System.out.println("total phrases = " + phraseLocations.size());
	
		phraseParams = new ArrayList<PhraseParameters>();
		for(int i=0;i<phraseLocations.size();i++){
			phraseParams.add(new PhraseParameters());
		}
	}
	
	public void setPhraseNum(int phraseNum){
		this.phraseNum = phraseNum;
		System.out.println(phraseLocations.get(phraseNum));
		
		outlet(PITCH_PARAM,phraseParams.get(phraseNum).pitchContours);
		outlet(COLOR_PARAM,phraseParams.get(phraseNum).colorContours);
		outlet(HARMONIC_PARAM,phraseParams.get(phraseNum).harmonicTensionContours);
		outlet(DENSITY_PARAM,phraseParams.get(phraseNum).densityContours);
		outlet(COMPLEXITY_PARAM,phraseParams.get(phraseNum).complexityContours);
		
	}
	
	public void playPhrase(int phraseNum){
		stepMin = phraseLocations.get(phraseNum).get(0)*12;
		stepMax = phraseLocations.get(phraseNum).get(1)*12;
		step = stepMin;
	}
	
	public void playSong(){
		stepMin = 0;
		stepMax = phrase.size();
		step = stepMin;
	}

	public void updatePhrase(){
		phrase = jazzGenerator.updatePhrase(phraseLocations.get(phraseNum), phraseParams.get(phraseNum));
	}
	
	
	public void loadScript(){
		
		//ReadFile fileReader = new ReadFile("/Users/masonbretan/Desktop/songScript109_4.txt"); //dance of jupiter and venus
		ReadFile fileReader = new ReadFile("/Users/masonbretan//iltur_short.txt"); //iltur
		List<String> lines = new ArrayList<String>(fileReader.getLines());
		instructions = fileReader.interpretScript(lines);
		System.out.println("melody script loaded, instruction count = "+instructions.length);
		
		
		
		fileReader = new ReadFile("/Users/masonbretan/Desktop/improvScript.txt");
		lines = new ArrayList<String>(fileReader.getLines());
		improvInstructions = fileReader.interpretScript(lines);
		System.out.println("improv script loaded, instruction count = "+improvInstructions.length);
		
	}
	
	public void loadJupiter(){
		
		ReadFile fileReader = new ReadFile("/Users/musictechnology/Documents/IntelliJ_Shimon/Songs/dance of jupiter and venus/songScript109_4.txt");
		List<String> lines = new ArrayList<String>(fileReader.getLines());
		instructions = fileReader.interpretScript(lines);
		System.out.println("jupiter melody script loaded, instruction count = "+instructions.length);	
		
		fileReader = new ReadFile("/Users/musictechnology/Documents/IntelliJ_Shimon/Songs/dance of jupiter and venus/improvScript.txt");
		lines = new ArrayList<String>(fileReader.getLines());
		improvInstructions = fileReader.interpretScript(lines);
		System.out.println("jupiter improv script loaded, instruction count = "+improvInstructions.length);
		
		
	}
	
	public void loadIltur_Short(){
		
		ReadFile fileReader = new ReadFile("/Users/masonbretan/Desktop/iltur3 Today Show/Tiltur_short.txt"); //iltur
		List<String> lines = new ArrayList<String>(fileReader.getLines());
		instructions = fileReader.interpretScript(lines);
		System.out.println("iltur_short melody script loaded, instruction count = "+instructions.length);
	}
	
	
	public void loadIltur2(){
		
		ReadFile fileReader = new ReadFile("/Users/musictechnology/Documents/IntelliJ_Shimon/Songs/iltur 2/iltur2Script.txt"); //iltur
		List<String> lines = new ArrayList<String>(fileReader.getLines());
		System.out.println(lines.size());
		instructions = fileReader.interpretScript(lines);
		System.out.println("iltur2 melody/improv script loaded, instruction count = "+instructions.length);
	}
	
	public void loadPitty(){
		
		ReadFile fileReader = new ReadFile("/Users/masonbretan/Desktop/pittyScript.txt"); //iltur
		List<String> lines = new ArrayList<String>(fileReader.getLines());
		instructions = fileReader.interpretScript(lines);
		System.out.println("pitty melody script loaded, instruction count = "+instructions.length);
	}
	
	
	public void loadSteadyAsSheGoes(){
		
		ReadFile fileReader = new ReadFile("/Users/musictechnology/Documents/IntelliJ_Shimon/Songs/steady as she goes/steady as she goes script.txt"); //iltur
		List<String> lines = new ArrayList<String>(fileReader.getLines());
		instructions = fileReader.interpretScript(lines);
		System.out.println("steady as she goes marimba script loaded, instruction count = "+instructions.length);
	}
	
	public void loadIltur3(){
		
		ReadFile fileReader = new ReadFile("/Users/musictechnology/Documents/IntelliJ_Shimon/Songs/iltur 3/iltur3 Script.txt"); //iltur
		List<String> lines = new ArrayList<String>(fileReader.getLines());
		System.out.println(lines.size());
		instructions = fileReader.interpretScript(lines);
		System.out.println("iltur3 melody script loaded, instruction count = "+instructions.length);
	}

	
	public void loadWhatYouSay(){
		
		ReadFile fileReader = new ReadFile("/Users/musictechnology/Documents/IntelliJ_Shimon/Songs/what you say/whatyousay_script.txt"); //iltur
		List<String> lines = new ArrayList<String>(fileReader.getLines());
		System.out.println(lines.size());
		instructions = fileReader.interpretScript(lines);
		System.out.println("what you say melody script loaded, instruction count = "+instructions.length);
	}

	
	
	public void initializeScript(){
		instructionCount = 0;
		improvInstructionCount = 0;
		timeUnitCount=1;
		outputArmCommands(0);
		
	}
	public void performScript(){
		
		if(instructionCount < instructions.length){
			outputArmCommands(instructionCount);
			instructionCount++;
			System.out.println("instruction count = "+ instructionCount);

		}

		
		/*if(instructionCount < instructions.length){
			if(timeUnitCount == instructions[instructionCount][6]){
				outputArmCommands(instructionCount);
				instructionCount++;
			}
		}
		timeUnitCount++;
		*/	
	}

	public void performNonMidiScript(){
		if(instructionCount < instructions.length){
			if(timeUnitCount == instructions[instructionCount][6]){
				outputArmCommands(instructionCount);
				instructionCount++;
			}
		}
		timeUnitCount++;
	}
	
	public void playInstructionCount(int count){
		//outputArmCommands(i);
		System.out.println("playing instruction count " + count);
		instructionCount = count;
		timeUnitCount = instructions[instructionCount][6];
		int[] command = new int[3];
		for(int i=3;i>-1;i--){
			if(instructions[count][i]!=-1){
				command[2] = 0;
				if(instructions[count][i] == instructions[count][4]){
					command[2] = 115;
				}
				command[0] = i;
				command[1] = instructions[count][i];
				outlet(ARM_COMMANDS[i],command);
			}
		}
		
	}
	
	private void outputArmCommands(int instructionCount){
		
		int[] command = new int[3]; //arm, position and velocity 
		if(instructionCount>=1){
			for(int i=0;i<4;i++){
				if(instructions[instructionCount][i]!=-1){
					command[2] = 0;
					if(instructions[instructionCount][i] == instructions[instructionCount][4]){
						command[2] = 110;
					}

					if(instructions[instructionCount][i] != instructions[instructionCount-1][i] || command[2] != 0){
						command[0] = i;
						command[1] = instructions[instructionCount][i];
						outlet(ARM_COMMANDS[i],command);
					}
					
				}
			}
		}else{
			for(int i=3;i>-1;i--){
				if(instructions[instructionCount][i]!=-1){
					command[2] = 0;
					if(instructions[instructionCount][i] == instructions[instructionCount][4]){
						command[2] = 115;
					}
					command[0] = i;
					command[1] = instructions[instructionCount][i];
					outlet(ARM_COMMANDS[i],command);
				}
			}

		}
	}
	
	public void performImprovScript(){
		
		if(improvInstructionCount < improvInstructions.length){
			outputImprovArmCommands(improvInstructionCount);
			improvInstructionCount++;

		}
	}
		
	private void outputImprovArmCommands(int instructionCount){
		int[] command = new int[3]; //arm, position and velocity 
		if(instructionCount>=1){
			for(int i=2;i<4;i++){
				if(improvInstructions[instructionCount][i]!=-1){
					command[2] = 0;
					if(improvInstructions[instructionCount][i] == improvInstructions[instructionCount][4]){
						command[2] = 115;
					}

					if(improvInstructions[instructionCount][i] != improvInstructions[instructionCount-1][i] || command[2] != 0){
						command[0] = i;
						command[1] = improvInstructions[instructionCount][i];
						outlet(ARM_COMMANDS[i],command);
					}
					
				}
			}
		}else{
			for(int i=3;i>-1;i--){
				if(improvInstructions[instructionCount][i]!=-1){
					command[2] = 0;
					if(improvInstructions[instructionCount][i] == improvInstructions[instructionCount][4]){
						command[2] = 115;
					}
					command[0] = i;
					command[1] = improvInstructions[instructionCount][i];
					outlet(ARM_COMMANDS[i],command);
				}
			}
		}
	}
	

}


