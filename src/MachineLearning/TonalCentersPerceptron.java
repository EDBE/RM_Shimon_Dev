package MachineLearning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import Jazz.ScaleTheory;
import Jazz.Chords;
import Jazz.Song;

public class TonalCentersPerceptron {
	
	
	ScaleTheory scaleTheory = new ScaleTheory();
	//int[][][] chordTrigrams = new int[432][432][432];
	int[][] chordBigrams = new int[432][432];
	int[][] romanNumeralBigrams = new int[12][12];
	int[][] tonalCentersBigram = new int[12][12];
	Chords chords = new Chords();
	Map<String,Integer> chordIndex = new HashMap<String,Integer>();
	
	
	
	//features
	//chord is part of major, melodic, or harmonic scale harmony
	int[] majorHarmony = new int[2];
	int[] melodicHarmony =new int[2];
	int[] harmonicHarmony =new int[2];
	
	
	int[][][] chordTypeToRomanNumeral = new int[chords.possibleChords.length][12][3]; //weight of chord type to roman numeral
	int tonalCenterIsKey=0;
	int tonalCenterStaysSame=0;
	

	//chord root is song key root
	
	
	
	public TonalCentersPerceptron(){
		
		//this.scaleTheory = scaleTheory;
		
		int count = 0;
		String tempString;
		for(int i=0;i<12;i++){ //12 keys
			for(int j=0;j<12;j++){ //12 possible roman numerals (though 7 are most likely)
				for(int z=0;z<3;z++){ //major, melodic minor, harmonic minor harmony
					tempString = String.valueOf(i)+"-"+String.valueOf(j) + "-"+String.valueOf(z);
					chordIndex.put(tempString,count);
					count++;
				}
			}
		}
		
		Random rand = new Random();
		for(int i=0;i<2;i++){
			majorHarmony[i] = rand.nextInt(50);
			melodicHarmony[i] = rand.nextInt(50);
			harmonicHarmony[i] = rand.nextInt(50);
		}
		
		for(int i =0;i<12;i++){
			for(int j=0;j<12;j++){
				romanNumeralBigrams[i][j] = rand.nextInt(100);
				tonalCentersBigram[i][j] = rand.nextInt(100);
			}
		}
		
		for(int i =0;i<432;i++){
			for(int j=0;j<432;j++){
				chordBigrams[i][j] =0;// rand.nextInt(20);
			}
		}
		
		
	}
	
	
	public void trainStructuredPerceptron(List<Song> songs){

		//this takes a list of songs and does the training one song at a time

		Collections.shuffle(songs); //shuffle the songs for randomized training
		while(true){
			for(int song=0;song<songs.size();song++){

				//TODO STEP 1: put the data in more convenient format (so that it is not listed by measures, but instead listed by chords)

				//STEP 1: Predict the path
				List<int[]> predicted = new ArrayList<int[]>(bigramViterbi(songs.get(song).chordTypesList,songs.get(song).chordRootsList,songs.get(song).key,songs.get(song).mode));

				//STEP 2: Update the feature weights
				updateWeights(songs.get(song), predicted);


			}
		}

	}
	
	
	public List<int[]> bigramViterbi(List<String> chordTypes, List<Integer> chordRoots, int songKeyRoot, String songKeyMode){

		/*
		 * generate a score for optimal path using Viterbi
		 * where observations are chords and outputs are keys
		 */

		
		System.out.println("song key: " +songKeyRoot + "  "+ songKeyMode);
		//initialize some vars
		int state = 0,state0=0;
		int max, maxState;
		int[] value = new int[3];
		Map<Integer, int[] >stateToType = new HashMap<Integer,int[]>();
		for(int key=0;key<12;key++){
			for(int degree=0;degree<12;degree++){
				for(int mode = 0;mode<3;mode++){
					value = new int[3];
					value[0] = key;
					value[1] = degree;
					value[2] = mode;
					stateToType.put(state, value);
					state++;
				}
			}
		}

		
		

		List<int[][][]> V = new ArrayList<int[][][]>();
		Map<Integer,List<Integer>> paths = new HashMap<Integer,List<Integer>>();
		Map<Integer,List<Integer>> newPath = new HashMap<Integer,List<Integer>>();
		int score;
		int[][][] emissions = new int[3][12][12];
		
		
		//for first chord
		V.add(new int[3][12][12]);
		state = 0;
		for(int key=0;key<12;key++){
			for(int degree=0;degree<12;degree++){ //degree refers to the chord roman numeral (such as I, ii, iii, IV, V, etc)			
				for(int mode = 0;mode<3;mode++){
					score = 0;
					switch(mode){
					case 0:
						score = majorHarmony[chordInMajorKey(key,chordRoots.get(0),chordTypes.get(0),degree)];//probability that chord belongs to specific major key
						break;
					case 1:
						score= melodicHarmony[chordInMelodicMinorKey(key,chordRoots.get(0),chordTypes.get(0),degree)];//probability that chord belongs to specific melodic minor key
						break;
					case 2:
						score = harmonicHarmony[chordInMelodicMinorKey(key,chordRoots.get(0),chordTypes.get(0),degree)];//probability that chord belongs to specific melodic minor key
						break;
						
					default:
						break;
					
					}
					score += chordTypeToRomanNumeral[chords.chordTypeMap.get(chordTypes.get(0))][degree][mode];
					V.get(0)[mode][key][degree] = score;
					
					paths.put(state, Arrays.asList(state));
					state++;
				}
			}
		}
		
		//for all other chords/observations
		for(int i=1;i<chordTypes.size();i++){
			//System.out.println("chord: " +chordRoots.get(i).get(j) + " "+ chordTypes.get(i).get(j));
			//Integer[][][] allKeys = new Integer[2][12][7]; //2 modes, 12 possible roots, 12 roman numerals
			emissions = new int[3][12][12];
			for(int key=0;key<12;key++){
				for(int degree=0;degree<12;degree++){ //degree refers to the chord roman numeral (such as I, ii, iii, IV, V, etc)			
					emissions[0][key][degree] = chordTypeToRomanNumeral[chords.chordTypeMap.get(chordTypes.get(i))][degree][0] + majorHarmony[chordInMajorKey(key,chordRoots.get(i),chordTypes.get(i),degree)];//probability that chord belongs to specific major key		
					emissions[1][key][degree] = chordTypeToRomanNumeral[chords.chordTypeMap.get(chordTypes.get(i))][degree][1] + melodicHarmony[chordInMelodicMinorKey(key,chordRoots.get(i),chordTypes.get(i),degree)];//probability that chord belongs to specific melodic minor key
					emissions[2][key][degree] = chordTypeToRomanNumeral[chords.chordTypeMap.get(chordTypes.get(i))][degree][2] + harmonicHarmony[chordInHarmonicMinorKey(key,chordRoots.get(i),chordTypes.get(i),degree)];//probability that chord belongs to specific melodic minor key

				}
			}

			//transition probabilities
			state=0;
			state0=0;
			V.add(new int[3][12][12]);
			newPath = new HashMap<Integer,List<Integer>>();
			ArrayList<Integer> tempPath;
			int temp,  multiplier;
			for(int key=0;key<12;key++){
				for(int degree=0;degree<12;degree++){
					for(int mode=0; mode<3;mode++){

						max = Integer.MIN_VALUE;
						maxState=-1;
						state0=0;
						for(int key0=0;key0<12;key0++){
							for(int degree0=0;degree0<12;degree0++){
								for(int mode0=0; mode0<3;mode0++){


									if(key == key0){
										//multiplier = 5;
										score = (emissions[mode][key][degree] + romanNumeralBigrams[degree0][degree] + V.get(V.size()-2)[mode0][key0][degree0] + 
												tonalCentersBigram[key0][key] + chordBigrams[state0][state]);

									}else{
										//multiplier = 0;
										score = (emissions[mode][key][degree]  + V.get(V.size()-2)[mode0][key0][degree0] + chordBigrams[state0][state]) +
												tonalCentersBigram[key0][key];
									}



									//other features
									/*if(chordRoots.get(i).get(j) == songKeyRoot){
											score +=5;
										}*/
									/*if(key == key0){
											score+=5;
										}*/


									/*temp = scaleTheory.majorScaleForm[degree]+songKeyRoot;
										if(temp > 11) temp -=12;
										if(chordRoots.get(i).get(j) == temp){
											score+=5;
										}

									 */
									if(score>max){
										max = score;
										maxState = state0;
									}
									state0++;
								}
							}
						}


						V.get(V.size()-1)[mode][key][degree] = max;
						tempPath  = new ArrayList<Integer>(paths.get(maxState));
						tempPath.add(state);
						newPath.put(state, tempPath);
						state++;
					}
				}
			}
			paths = new HashMap<Integer,List<Integer>>(newPath);
		}

		//get optimal path
		max = Integer.MIN_VALUE;
		maxState=-1;
		state=0;
		for(int key=0;key<12;key++){
			for(int degree=0;degree<12;degree++){
				for(int mode=0; mode<3;mode++){
					if(V.get(V.size()-1)[mode][key][degree] > max){
						maxState = state;
						max = V.get(V.size()-1)[mode][key][degree];
					}
					state++;
				}
			}
		}
		
		//System.out.println("optimal end state = " +maxState);
		List<Integer> optimalPath = new ArrayList<Integer>( paths.get(maxState));
		//System.out.println(optimalPath);
		//translate values into readable format
		List<int[]> formattedPath = new ArrayList<int[]>();
		for(int i=0;i<optimalPath.size();i++){
			System.out.println(stateToType.get(optimalPath.get(i))[0] + "  " + stateToType.get(optimalPath.get(i))[1]+"  "+ stateToType.get(optimalPath.get(i))[2]);
		
			value = new int[3];
			value[0] = stateToType.get(optimalPath.get(i))[0];
			value[1] = stateToType.get(optimalPath.get(i))[1];
			value[2] = stateToType.get(optimalPath.get(i))[2];
			formattedPath.add(value);
		
		}
		return formattedPath;
	}
	
	public void trigramViterbi(){
		
	}
	
	
	private int chordInMajorKey(int queryKey, int chordRoot, String chordType, int degree){
		int score = 0;
		//first check if the scale has the chordRoot
		int temp;
		if(queryKey <= chordRoot){
			temp = (chordRoot - queryKey) %12;
		}else{
			temp = (chordRoot+12 - queryKey)%12;
		}
		
		if(scaleTheory.majorModes.get(0)[temp] == 1){
			score=0;
			
			
			if(temp == degree){
				score=0;
				int nOnes = 0;
				for(int i=0;i<temp+1;i++){
					if(scaleTheory.majorModes.get(0)[i]==1){
						nOnes+=1;
					}
				}
				
				for(int i =0;i<scaleTheory.majorScaleHarmony.get(nOnes-1).length;i++){
					if(scaleTheory.majorScaleHarmony.get(nOnes-1)[i].equalsIgnoreCase(chordType)){
						score=1;
						break;
					}
				}
			}else{
				score=0;
			}
		}
		//System.out.println(score + "   "+ chordRoot+chordType + "   "+queryKey + "   "+degree);
		return score;
	}
	
	private int chordInMelodicMinorKey(int queryKey, int chordRoot, String chordType, int degree){
		int score = 0;
		//first check if the scale has the chordRoot
		int temp;
		if(queryKey <= chordRoot){
			temp = (chordRoot - queryKey) %12;
		}else{
			temp = (chordRoot+12 - queryKey)%12;
		}
		if(scaleTheory.melodicModes.get(0)[temp] == 1){
			score=0;
			
			
			if(temp == degree){
				score=0;
				int nOnes = 0;
				for(int i=0;i<temp+1;i++){
					if(scaleTheory.melodicModes.get(0)[i]==1){
						nOnes+=1;
					}
				}
				
				for(int i =0;i<scaleTheory.melodicMinorScaleHarmony.get(nOnes-1).length;i++){
					if(scaleTheory.melodicMinorScaleHarmony.get(nOnes-1)[i].equalsIgnoreCase(chordType)){
						score=1;
						break;
					}
				}
			}else{
				score=0;
			}
		}
		//System.out.println(score + "   "+ chordRoot+chordType + "   "+queryKey + "   "+degree);
		return score;
	}
	
	private int chordInHarmonicMinorKey(int queryKey, int chordRoot, String chordType, int degree){
		int score = 0;
		//first check if the scale has the chordRoot
		int temp;
		if(queryKey <= chordRoot){
			temp = (chordRoot - queryKey) %12;
		}else{
			temp = (chordRoot+12 - queryKey)%12;
		}
		if(scaleTheory.harmonicModes.get(0)[temp] == 1){
			score=0;
			
			
			if(temp == degree){
				score=0;
				int nOnes = 0;
				for(int i=0;i<temp+1;i++){
					if(scaleTheory.harmonicModes.get(0)[i]==1){
						nOnes+=1;
					}
				}
				
				for(int i =0;i<scaleTheory.harmonicMinorScaleHarmony.get(nOnes-1).length;i++){
					if(scaleTheory.harmonicMinorScaleHarmony.get(nOnes-1)[i].equalsIgnoreCase(chordType)){
						score=1;
						break;
					}
				}
			}else{
				score=0;
			}
		}
		//System.out.println(score + "   "+ chordRoot+chordType + "   "+queryKey + "   "+degree);
		return score;
	}


	private void updateWeights(Song song, List<int[]> predicted){
		int temp,temp1, temp2;
		int chordRoot;
		String chordType;
		float correct = 0;
		for(int i=0;i<predicted.size();i++){
			
			if(Arrays.equals(song.tonalCenters.get(i), predicted.get(i)) != true){
				
				chordRoot = song.chordRootsList.get(i);
				chordType = song.chordTypesList.get(i);

				if(song.tonalCenters.get(i)[2] == 0){ //major scale harmony
					temp1 = chordInMajorKey(predicted.get(i)[0], chordRoot,chordType,predicted.get(i)[1]);
					temp2 = chordInMajorKey(song.tonalCenters.get(i)[0], chordRoot,chordType,song.tonalCenters.get(i)[1]);
					majorHarmony[temp1] -=1;
					majorHarmony[temp2] +=1;
				}

				if(song.tonalCenters.get(i)[2] == 1){ //melodic minor scale harmony
					temp1 = chordInMelodicMinorKey(predicted.get(i)[0], chordRoot,chordType,predicted.get(i)[1]);
					temp2 = chordInMelodicMinorKey(song.tonalCenters.get(i)[0], chordRoot,chordType,song.tonalCenters.get(i)[1]);
					melodicHarmony[temp1] -=1;
					melodicHarmony[temp2] +=1;
				}

				if(song.tonalCenters.get(i)[2] == 2){ //harmonic minor scale harmony
					temp1 = chordInHarmonicMinorKey(predicted.get(i)[0], chordRoot,chordType,predicted.get(i)[1]);
					temp2 = chordInHarmonicMinorKey(song.tonalCenters.get(i)[0], chordRoot,chordType,song.tonalCenters.get(i)[1]);
					harmonicHarmony[temp1] -=1;
					harmonicHarmony[temp2] +=1;
				}

				
				chordTypeToRomanNumeral[chords.chordTypeMap.get(chordType)][song.tonalCenters.get(i)[1]][song.tonalCenters.get(i)[2]] +=1;
				chordTypeToRomanNumeral[chords.chordTypeMap.get(chordType)][predicted.get(i)[1]][predicted.get(i)[2]] -=1;

				
				if(i > 0){

					if(song.tonalCenters.get(i-1)[0] == song.tonalCenters.get(i)[0]){ //same key
						romanNumeralBigrams[song.tonalCenters.get(i-1)[1]][song.tonalCenters.get(i)[1]]+=3;
						romanNumeralBigrams[predicted.get(i-1)[1]][predicted.get(i)[1]]-=3;
					}

					temp1 = getIndex(song.tonalCenters.get(i-1)[0],song.tonalCenters.get(i-1)[1],song.tonalCenters.get(i-1)[2]);
					temp2 = getIndex(song.tonalCenters.get(i)[0],song.tonalCenters.get(i)[1],song.tonalCenters.get(i)[2]);
					chordBigrams[temp1][temp2]+=1;
					
					temp1 = getIndex(predicted.get(i-1)[0],predicted.get(i-1)[1],predicted.get(i-1)[2]);
					temp2 = getIndex(predicted.get(i)[0],predicted.get(i)[1],predicted.get(i)[2]);
					chordBigrams[temp1][temp2]-=1;
					
					tonalCentersBigram[song.tonalCenters.get(i-1)[0]][song.tonalCenters.get(i)[0]]+=2;
					tonalCentersBigram[predicted.get(i-1)[0]][predicted.get(i)[0]]-=2;
						
				}
			}else{
				correct+=1.0;
			}
			
		}
		
		System.out.println(correct/song.tonalCenters.size());
	}
	
	private int getIndex(int key, int degree, int scale){
		
		return key*36+degree*3 + scale;
		
	}
}
