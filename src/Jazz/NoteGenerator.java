package Jazz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NoteGenerator {
	
	ScaleTheory scaleTheory = new ScaleTheory();
	Chords chords = new Chords();
	int[] currentTonalCenter = new int[3];
	
	int closePitches=0;
	
	public NoteGenerator(){
		
	}
	
	public List<List<Integer>> generatePhrase(List<float[]> melody, List<String> chordType, List<Integer> chordRoot, List<int[]> tonalCenters, PhraseParameters params, int totalDuration){
		
		return viterbi(melody,chordType,chordRoot,tonalCenters, params, totalDuration);	
	}
	
	private void rhythmGeneration(List<Integer> melody, List<String> chordType, List<Integer> chordRoot, List<int[]> tonalCenters){
		for(int i=0;i<melody.size();i++){
			tensionScale(tonalCenters.get(i)[1]);
		}
	}
	
	private List<List<Integer>> viterbi(List<float[]> melody, List<String> chordType, List<Integer> chordRoot, List<int[]> tonalCenters,PhraseParameters params,int totalDuration){
		
		
		List<float[][]> V = new ArrayList<float[][]>();
		Map<Integer,List<Integer>> paths = new HashMap<Integer,List<Integer>>();
		Map<Integer,List<Integer>> newPath = new HashMap<Integer,List<Integer>>();
		int state,maxState,temp;
		float score,max;
		float[][] minArray= new float[scaleTheory.scales.size()][48];
		int chordChangeCount=0;
		currentTonalCenter=tonalCenters.get(0);
		//for (int[] row: minArray)
		//    Arrays.fill(row, -10000);
		V.add(minArray);
		state = 0;
		float accumulatedDuration=0.0f;
		for(int scale=0;scale<scaleTheory.scales.size();scale++){
			for(int note=0;note<48;note++){
				if(checkHarmony(chordType.get(0), scaleTheory.scaleHarmony.get(scaleTheory.scaleMap.get(scale)))){
					if(note==melody.get(0)[0]-24){
						score=10;
					}else{
						if(areOctaves(note,(int)melody.get(0)[0]-24)&&melody.get(0)[0]!=-1){
							score=0;
						}else{
							score=0;
						}
					}
					
					V.get(0)[scale][note] =(score + emissionFeatures(note, scale, melody.get(0)[2],chordRoot.get(0), chordType.get(0),(int)melody.get(0)[1],
							params.pitchContours[0],params.pitchWeight,
							params.colorContours[0],params.colorWeight,
							params.harmonicTensionContours[0],params.harmonicTensionWeight));
				}else{
					V.get(0)[scale][note] = 0;
				}
				paths.put(state, Arrays.asList(state));
				state++;
			}
		}
		//accumulatedDuration+=melody.get(0)[1];
		
		

		ArrayList<Integer> tempPath;
		int contourLocation;
		for(int i=1;i<melody.size();i++){
			state =0;
			minArray= new float[scaleTheory.scales.size()][48];
			V.add(minArray);
			newPath = new HashMap<Integer,List<Integer>>();
			accumulatedDuration+=melody.get(i-1)[1];
			contourLocation = (int)(accumulatedDuration/totalDuration * 1000);
			System.out.println(accumulatedDuration + "  " + params.pitchContours[contourLocation]);
			//System.out.println(i +  "   "+ chordRoot.get(i) + "   " + chordType.get(i));
			
			if(chordType.get(i).equalsIgnoreCase(chordType.get(i-1))!=true || chordRoot.get(i)!=chordRoot.get(i-1)){
				chordChangeCount++;
				currentTonalCenter = tonalCenters.get(chordChangeCount);
			}
			
			
			for(int scale=0;scale<scaleTheory.scales.size();scale++){

				if(checkHarmony(chordType.get(i), scaleTheory.scaleHarmony.get(scaleTheory.scaleMap.get(scale)))){

					for(int note=0;note<48;note++){

						//constrain to keep paths within the same scale
						max = Integer.MIN_VALUE;
						maxState = 0;
						if(chordType.get(i).equalsIgnoreCase(chordType.get(i-1)) && chordRoot.get(i)==chordRoot.get(i-1)){
							for(int note0=0;note0<48;note0++){
								if(note==melody.get(i)[0]-24){
									score=10;
								}else{
									if(areOctaves(note,(int)melody.get(i)[0]-24)&&melody.get(i)[0]!=-1){
										score=0;
									}else{
										score=0;
									}
								}

								score+=featureCalculation(note, note0, melody.get(i)[2], scale, chordRoot.get(i),chordType.get(i),(int)melody.get(i)[1],chordRoot.get(i-1),chordType.get(i-1),
										params.pitchContours[contourLocation],params.pitchWeight,params.pitchContours[contourLocation-1],
										params.colorContours[contourLocation],params.colorWeight,
										params.harmonicTensionContours[contourLocation],params.harmonicTensionWeight);

								score+=V.get(V.size()-2)[scale][note0];

								if(score>max){
									max = score;
									maxState = scale*48 + note0;
								}
							}

						}else{

							//a new scale might be desired so test all transitions
							List<Integer> keyset=new ArrayList<Integer>(paths.keySet());  
							for(int scale0=0;scale0<keyset.size();scale0++){
								for(int note0=0;note0<48;note0++){
									if(note==melody.get(i)[0]-24){
										score=10;
									}else{
										if(areOctaves(note,(int)melody.get(i)[0]-24)&&melody.get(i)[0]!=-1){
											score=0;
										}else{
											score=0;
										}
									}

									score+=featureCalculation(note, note0,melody.get(i)[2],scale, chordRoot.get(i),chordType.get(i),(int)melody.get(i)[1],chordRoot.get(i-1),chordType.get(i-1),
											params.pitchContours[contourLocation],params.pitchWeight,params.pitchContours[contourLocation-1],
											params.colorContours[contourLocation],params.colorWeight,
											params.harmonicTensionContours[contourLocation],params.harmonicTensionWeight);
									
									score+=V.get(V.size()-2)[(int)(keyset.get(scale0)/48.0)][note0];

									if(score>max){
										max = score;
										maxState = ((int)(keyset.get(scale0)/48.0)*48) + note0;//keyset.get(scale0);//*48 + note0;
									}
								}
							}
						}

						V.get(V.size()-1)[scale][note] = max;
						tempPath  = new ArrayList<Integer>(paths.get(maxState));

						state = scale*48+note;
						tempPath.add(state);
						newPath.put(state, tempPath);	
					}
				}
			}

			paths = new HashMap<Integer,List<Integer>>(newPath);
		}


		
		//get optimal path
		List<Integer> keyset=new ArrayList<Integer>(paths.keySet());  
		max = Integer.MIN_VALUE;
		maxState=-1;
		state=0;
		for(int scale=0;scale<scaleTheory.scales.size();scale++){
			for(int note=0;note<48;note++){
				//System.out.println(scale + "   " + note + "   " +V.get(V.size()-1)[scale][note]);
				if(V.get(V.size()-1)[scale][note] > max){
					maxState = state;
					max = V.get(V.size()-1)[scale][note];
				}
				state++;
			}
		}
		
		List<Integer> optimalPath = new ArrayList<Integer>(paths.get(maxState));
		List<Integer> noteSequence = new ArrayList<Integer>();
		List<Integer> scaleSequence = new ArrayList<Integer>();
		for(int i=0;i<optimalPath.size();i++){
			
			temp  = (int)(optimalPath.get(i) / 48.0);
			String scaleUsed = scaleTheory.scales.get(temp);
			scaleSequence.add(temp);
			
			temp = optimalPath.get(i) - (temp*48);
			if(melody.get(i)[0]==-1){
				System.out.println(temp + "  " + scaleUsed);
			}else{
				System.out.println(temp +"***"+ "  " + scaleUsed);
			}
			noteSequence.add(temp);
			
		
		}
		List<List<Integer>> sequences = new  ArrayList<List<Integer>>();
		sequences.add(noteSequence);
		sequences.add(scaleSequence);
		
		return sequences;
	}
	
	private float featureCalculation(int note, int prevNote, float beatNum,int scale, int chordRoot, String chordType, int noteDuration, int prevChordRoot, String prevChordType,float pitchTarget,float pitchWeight, float prevPitchTarget, float colorTarget, float colorWeight, float harmonicTensionTarget, float harmonicTensionWeight){
		closePitches=0;
		float score = 0f;
		score += emissionFeatures(note, scale,beatNum, chordRoot, chordType,noteDuration, pitchTarget,pitchWeight,colorTarget,colorWeight,harmonicTensionTarget,harmonicTensionWeight);
		score += transitionFeatures(note,prevNote,beatNum,scale,pitchTarget,prevPitchTarget,chordRoot,chordType,prevChordRoot,prevChordType,harmonicTensionTarget,harmonicTensionWeight);
		return score;
	}
	
	private float emissionFeatures(int note, int scale, float beatNum,int chordRoot, String chordType,int noteDuration,float pitchTarget,float pitchWeight, float colorTarget, float colorWeight,float harmonicTensionTarget, float harmonicTensionWeight){
		
		float score=0f;
		
		/*
		if (checkNoteToScaleHarmony(note,chordRoot,scaleTheory.scalePatterns.get(scaleTheory.scaleMap.get(scale)))){ //note is part of scale
			score +=20;
		}
		
		if (checkNoteToChordHarmony(note, chordRoot, chordType)){ //note is part of chord
			score +=0;
		}*/
		
		int scaleDegree =checkNoteToScaleHarmony(note,chordRoot,scaleTheory.scalePatterns.get(scaleTheory.scaleMap.get(scale)));
		int chordHarmonyScore = checkNoteToChordHarmony(note, chordRoot, chordType);
		score += (pitchContourFeature(note,pitchTarget,scaleDegree) * pitchWeight);
		score += (colorContourFeature(note,colorTarget,scaleDegree,chordHarmonyScore,scale) * colorWeight);
		score += (pitchToDurationFeature(note,noteDuration,scaleDegree));
		score += scaleTheory.scalePriors.get(scaleTheory.scaleMap.get(scale));
		
		score += (harmonicTensionContourFeature(harmonicTensionTarget,colorTarget, scaleDegree,scale,beatNum) * harmonicTensionWeight);
		
		if(scaleHarmony(scale) == 0){
			score +=10f;
		}
		
		//score+= tensionModelFeature(chordRoot, chordType,scale,note);
		
		/*
		switch(scaleHarmony(scale)){
		case 0:
			score+=0;
			break;
		case 1:
			score+=10;
			break;
		case 2:
			score+=10;
			break;
		default:
			score+=0;
			break;
		}
		*/
	
		return score;
	}
	
	private float transitionFeatures(int note, int prevNote, float beatNum,int scale,float pitchTarget,float prevPitchTarget,int chordRoot, String chordType,int prevChordRoot, String prevChordType, float harmonicTensionTarget, float harmonicTensionWeight ){
		float score=0f;
		int scaleDegree =checkNoteToScaleHarmony(note,chordRoot,scaleTheory.scalePatterns.get(scaleTheory.scaleMap.get(scale)));
		
		
		//transition features
		score +=harmonicTransitionScoreForSameScale(note,prevNote,scaleDegree,scale,chordRoot);
		
		
		if(prevPitchTarget - pitchTarget< 0 && prevNote - note <0){
			score+=2;
		}
		
		if(prevPitchTarget - pitchTarget> 0 && prevNote - note >0){
			score+=2;
		}
		if(prevNote != note){
			score+=2;
		}
		
		if(Math.abs(prevNote - note)< 5){ //pitches not too far from each other
			score +=0;
		}
		
		

		if(Math.abs(prevNote - note)>= 5){
			float beatLoc = beatNum % 1;
			score -= (beatLoc*100);
		}

		
		/*
		if(Math.abs(note - prevNote)<2 && Math.abs(note - prevNote)>0){
			score+=(harmonicTensionTarget * harmonicTensionWeight);
		}*

		/*

		if( (prevNote - note) > 0){ //pitch is descending
			score +=0;
		}
		
		if( (note - prevNote) > 0){ //pitch is ascending
			score +=0;
		}
		
		if( prevNote==note){ //pitch remains the same
			score +=0; 
		}
		
		
		//int diff = Math.abs(prevNote - note);
		//score-=diff;
		
		if(Math.abs(prevNote - note)< 5){ //pitches not too far from each other
			score +=20;
		}
		
		if(Math.abs(prevNote - note)> 6){ //pitches farther from each other so Shimon can play
			score +=0;
		}

		if(nextMelodyNote >-1){ //the melody note should be higher in pitch than the previous note (lead to melody note)
			if(note < nextMelodyNote){
				score +=0;
			}
		}
		if(nextMelodyNote >-1){ //the melody note should be higher in pitch than the previous note (lead to melody note)
			if(note > nextMelodyNote){
				score +=10;
			}
		}	
		*/
		return score;
	}
	
	private float harmonicTransitionScoreForSameScale(int note, int prevNote, int scaleDegree, int scale, int chordRoot){
		
		int prevScaleDegree = checkNoteToScaleHarmony(prevNote,chordRoot,scaleTheory.scalePatterns.get(scaleTheory.scaleMap.get(scale)));
		if(prevScaleDegree!= -1){
			if(isNonHarmonic(prevScaleDegree, scale)){			
				if(isNonHarmonic(scaleDegree,scale)){
					return -100f;
				}
				if(Math.abs(prevNote - note) > 1){
					return -100f;
				}
			}
		}else{
			if(isNonHarmonic(scaleDegree,scale)){
				return -100f;
			}
			if(Math.abs(prevNote - note) > 1){
				return -100f;
			}
		}
		return 0;
		
	}
	
	private boolean checkHarmony(String chordType, String[] harmony){
		
		//return true if scale can be used for specific chord type
		for(int i=0;i<harmony.length;i++){
			if(chordType.equalsIgnoreCase(harmony[i])){
				return true;
			}
		}
		return false;
	}
	
	private int checkNoteToScaleHarmony(int note, int chordRoot, int[] scale){
		
		//return scale degree if note works in particular scale otherwise return -1
		int temp = (note + 12 - chordRoot)%12;
		if(scale[temp] == 1){
			return temp;
		}
		return -1;
	}
	
	private int checkNoteToChordHarmony(int note,int chordRoot, String chordType){
		
		//return true if note works in particular chord
		int temp = (note + 12 - chordRoot)%12;
		
		while(temp < chords.chordTexts.get(chordType).length){
			
			if(chords.chordTexts.get(chordType)[temp] == 1){
				return temp;
			}
			temp+=12; //check next octave for 9ths,11ths, etc
		}
		return -1;
	}
	
	private int scaleHarmony(int scale){
		
		//return 0 for major, 1 for melodic, 2 for harmonic etc.
		return (int)(scale / 7.0);
	}
	
	private boolean isMelodyNote(int note){
		if(note > -1){
			return true;
		}
		return false;
	}
	
	private boolean areOctaves(int note1, int note2){
		
		if(Math.abs(note1-note2)%12 ==0){
			return true;
		}
		
		return false;
	}
	
	private float pitchContourFeature(int note, float targetPitch, int scaleHarmony){
		if(scaleHarmony == -1){
			//return -10;
		}
		float score = note / 48.0f;
		return gaussianPDF(score,targetPitch,.1f);
	}
	
	private float colorContourFeature(int note, float targetColor, int scaleDegree, int chordHarmony, int scale){
		
		if(scaleDegree == -1){
			return 0;
		}
		
		
		String scaleName= scaleTheory.scales.get(scale);
		if( valueInArray(scaleDegree,scaleTheory.diatonicTensions.get(scaleName)[1])){ //check if it is a diatonic tension note
			//is a color note
			return targetColor;	
		}else{
			//is a chordal tone
			return 1- targetColor;
		}
		//float score = scaleHarmony(scale) * .5f;
		//score += (scaleTheory.colorRatings[scaleDegree] / 12.0f)*.5f;
		//return gaussianPDF(score,targetColor,.3f);
	}
	
	private float harmonicTensionContourFeature(float targetTension,float targetColor, int scaleDegree, int scale,float beatNum){
		
		if(isNonHarmonic(scaleDegree,scale)){ 
			//is a non harmonic note
			if(beatNum%1==0){
				float beatLoc = beatNum % 1;
				return (targetTension+targetColor - (1 - beatLoc));
			}else{
				return targetTension+targetColor;
			}
		}else{
			//is harmonic tone
			//return 0;
			return 1- targetTension;
		}
		
	}
	
	private boolean isNonHarmonic(int scaleDegree, int scale){
		if(scaleDegree == -1){
			return true;
		}
		
		String scaleName= scaleTheory.scales.get(scale);
		if(scaleTheory.diatonicTensions.get(scaleName).length==3){
			return valueInArray(scaleDegree,scaleTheory.diatonicTensions.get(scaleName)[2]);
		}
		return false;
	}
	
	private boolean valueInArray(int val, int[] array){

		for(int i=0;i<array.length;i++){
			if(val+1 == array[i]){
				return true;
			}
		}
		return false;
	}
	
	private float pitchToDurationFeature(int note, int duration, int scaleDegree){
		
		if(scaleDegree == -1){
			return -10;
		}
		float score = scaleTheory.inverseColorRatings[scaleDegree] / ((float)(duration));
		return score;
	}
	
	/*private float tensionFeature(){
		
	}*/
	
	private int tensionModelFeature(int chordRoot, String chordType, int scale, int note){
		int score=0;
		
		
		/*
		 * NOTE: use lydian dominant for V7 chord that don't resolve to root
		 * and altered for V7 that does resolve to root in order to add tension
		 */
		
		switch(currentTonalCenter[2]){
		
		case 0: //major
			score+=majorTensionScale(chordRoot, chordType,currentTonalCenter[1],scale,note);
			break;
		case 1:
			score+=harmonicTensionScale(currentTonalCenter[1],scale);
			break;	
		case 2:
			score+=harmonicTensionScale(currentTonalCenter[1],scale);
			break;
		
		}
		
		
		return score;
	}
	
	private int majorTensionScale(int chordRoot, String chordType,int romanNumeral, int scale, int note){
		
		int score=0;
		switch(romanNumeral){
		
		case 0:
			if(scale == 0){//i
				score +=10;
			}
			if (checkNoteToChordHarmony(note, chordRoot, chordType)!=-1){ //note is part of chord
				score +=10;
			}
			break;	
		case 2:
			//ii
			if(scale == 1){
				score+=10;
			}
			if (checkNoteToChordHarmony(note, chordRoot, chordType)!=-1){ //note is part of chord
				score +=1;
			}
			break;
		case 3:
			//biii
			if(scale == 22){
				score+=10;
			}
			if (checkNoteToChordHarmony(note, chordRoot, chordType)!=-1){ //note is part of chord
				score +=1;
			}
			closePitches = 20;
			break;

		case 4:
			if(scale == 2){//iii
				score +=10;
			}
			if (checkNoteToChordHarmony(note, chordRoot, chordType)!=-1){ //note is part of chord
				score +=1;
			}
			break;
		case 5:
			if(scale == 3){//iv
				score +=10;
			}
			if (checkNoteToChordHarmony(note, chordRoot, chordType)!=-1){ //note is part of chord
				score +=0;
			}
			break;
		case 7:
			if(scale == 13){//v
				score +=10;
			}
			if (checkNoteToChordHarmony(note, chordRoot, chordType)!=-1){ //note is part of chord
				score +=1;
			}
			closePitches = 20;
			break;
		case 9:
			if(scale == 1){//vi
				score +=10;
			}
			if (checkNoteToChordHarmony(note, chordRoot, chordType)!=-1){ //note is part of chord
				score +=4;
			}
			break;
		case 11:
			if(scale == 22){//vii
				score+=10;
			}
			if (checkNoteToChordHarmony(note, chordRoot, chordType)!=-1){ //note is part of chord
				score +=0;
			}
			break;
			
		}
		
		return score;
	}
	
	private int harmonicTensionScale(int romanNumeral, int scale){
		
		int score=0;
		switch(romanNumeral){
		
		case 0:
			if(scale == 0){//i
				score +=14;
			}
			break;	
		case 2:
			score +=4; //ii
			if(scale == 1){
				score+=15;
			}
			break;

		case 4:
			if(scale == 2){//iii
				score +=16;
			}
			score +=5; //iii
			break;
		case 5:
			if(scale == 3){//iv
				score +=17;
			}
			break;
		case 7:
			score+=12; //v
			if(scale == 13){//iii
				score +=18;
			}
			break;
		case 9:
			score+=5; //vi
			if(scale == 1){//iii
				score +=19;
			}
			break;
		case 11:
			if(scale == 22){//vii
				score+=20;
			}
			break;
			
		}
		return score;
	}
	
	
	private int tensionScale(int romanNumeral){
		int score=0;
		switch(romanNumeral){
		
		case 0:
			score += 0; //i
			break;
		case 1:
			score +=7; //bii
			break;		
		case 2:
			score +=4; //ii
			break;
		case 3:
			score +=6; //biii
			break;
		case 4:
			score +=5; //iii
			break;
		case 5:
			score +=3; //iv
			break;
		case 6:
			score +=5; //bv
			break;
		case 7:
			score+=12; //v
			break;
		case 8:
			score+=10; //bvi
			break;
		case 9:
			score+=5; //vi
			break;
		case 10:
			score+=7;//bvii
			break;
		case 11:
			score+=11;//vii
			break;
			
		}
		return score;
	}
	
    // return phi(x, mu, signma) = Gaussian pdf with mean mu and stddev sigma
    private float gaussianPDF(float x, float mu, float sigma) {
        return phi((x - mu) / sigma) / sigma;
    }
    // return phi(x) = standard Gaussian pdf
    private float phi(float x) {
        return (float) (Math.exp(-x*x / 2) / Math.sqrt(2 * Math.PI));
    }
}
