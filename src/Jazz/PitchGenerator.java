package Jazz;

import java.util.ArrayList;
import java.util.List;

import Embodiment.PhysicalConstraints_ShimonComplete;

public abstract class PitchGenerator {

	private static final List<float[]> ArrayUtils = null;
	//PhysicalConstraints_ShimonComplete pConstraints = new PhysicalConstraints_ShimonComplete();
	ScaleTheory scaleTheory = new ScaleTheory();
	Chords chords = new Chords();
	int[] currentTonalCenter = new int[3];

	int noteSize = 48;
	int closePitches=0;

	public PitchGenerator(){
		
	}

	//public abstract List<List<Integer>> generatePhrase();
	public abstract List<List<Integer>> generatePhrase(List<List<Integer>> melody, int moveTimeConstraint);
	public abstract List<List<Integer>> generatePhrase(List<float[]> melody, List<String> chordType, List<Integer> chordRoot, List<int[]> tonalCenters, PhraseParameters params, int totalDuration);


	List<Integer> createScalePath(List<float[]> melody, List<String> chordType, List<Integer> chordRoot, List<int[]> tonalCenters){

		String type = chordType.get(0);
		Integer root = chordRoot.get(0);
		List<Integer>scalePath = new ArrayList<Integer>();
		float score = -1f;
		for(int i=0;i<melody.size();i++){

			for(int scale=0;scale<scaleTheory.scales.size();scale++){
				if(checkHarmony(chordType.get(i), scaleTheory.scaleHarmony.get(scaleTheory.scaleMap.get(scale)))){

					scalePath.add(scale);
					break;
					//score = scaleTheory.scalePriors.get(scaleTheory.scaleMap.get(scale));
				}
			}

			/*if(type.equals(chordType.get(i)) == false && root!= chordRoot.get(i)){
				for(int scale=0;scale<scaleTheory.scales.size();scale++){
					if(checkHarmony(chordType.get(i), scaleTheory.scaleHarmony.get(scaleTheory.scaleMap.get(scale)))){

						scalePath.
						score = scaleTheory.scalePriors.get(scaleTheory.scaleMap.get(scale));
					}
				}
			}*/

		}
		return scalePath;


	}

	float melodyEmissionScore(int note, int melodyValue){

		float score;
		if(note==melodyValue-24){
			score=100f;
		}else{
			if(areOctaves(note,(int)melodyValue-24)){
				score=80f;
				score -=Math.abs(melodyValue-24 - note); // favor closer notes 
			}else{
				score=-100f;
			}
		}

		return score;

	}

	float melodyTransitionScore(int note, int prevNote, int melodyValue, int prevMelodyValue){

		float score = 0f;


		int diff1 = prevNote - note;
		int diff2 = prevMelodyValue - melodyValue;
		if(  (diff1>0 && diff2>0)   || (diff1<0 && diff2<0) || (diff1==0 && diff2==0)   ){
			score = 90;
		}
		return score;

	}

	List<Integer> returnOctaves(int note){
		int octave = note%12;
		List<Integer> octaves=new ArrayList<Integer>();
		octaves.add(octave);
		octave+=12;
		while(octave <noteSize){
			octaves.add(octave);
			octave+=12;
		}
		return octaves;
	}

	float featureCalculation(int note, int prevNote, float melodyValue, float beatNum,int scale, int chordRoot, String chordType, int noteDuration, int prevChordRoot, String prevChordType,float pitchTarget,float pitchWeight, float prevPitchTarget, float colorTarget, float colorWeight, float harmonicTensionTarget, float harmonicTensionWeight){
		closePitches=0;
		float score = 0f;

		score += emissionFeatures(note, scale, melodyValue,beatNum, chordRoot, chordType,noteDuration, pitchTarget,pitchWeight,colorTarget,colorWeight,harmonicTensionTarget,harmonicTensionWeight);
		score += transitionFeatures(note,prevNote,beatNum,scale,pitchTarget,prevPitchTarget,chordRoot,chordType,prevChordRoot,prevChordType,harmonicTensionTarget,harmonicTensionWeight);
		return score;
	}

	float emissionFeatures(int note, int scale, float melodyValue,float beatNum,int chordRoot, String chordType,int noteDuration,float pitchTarget,float pitchWeight, float colorTarget, float colorWeight,float harmonicTensionTarget, float harmonicTensionWeight){

		float score=0f;

		/*
		if (checkNoteToScaleHarmony(note,chordRoot,scaleTheory.scalePatterns.get(scaleTheory.scaleMap.get(scale)))){ //note is part of scale
			score +=20;
		}

		if (checkNoteToChordHarmony(note, chordRoot, chordType)){ //note is part of chord
			score +=0;
		}*/
		if(note==melodyValue-24){
			score=0;
		}else{
			if(areOctaves(note,(int)melodyValue-24)&&melodyValue!=-1){
				score=0;
			}else{
				score=0;
			}
		}


		int scaleDegree =checkNoteToScaleHarmony(note,chordRoot,scaleTheory.scalePatterns.get(scaleTheory.scaleMap.get(scale)));
		int chordHarmonyScore = checkNoteToChordHarmony(note, chordRoot, chordType);
		score += (pitchContourFeature(note,pitchTarget,scaleDegree) * pitchWeight);
		score += (colorContourFeature(note,colorTarget,scaleDegree,chordHarmonyScore,scale) * colorWeight);
		score += (pitchToDurationFeature(note,noteDuration,scaleDegree));



		score += (harmonicTensionContourFeature(harmonicTensionTarget,colorTarget, scaleDegree,scale,beatNum) * harmonicTensionWeight);

		if(scaleHarmony(scale) == 0){
			score +=10f;
		}



		return score;
	}

	float transitionFeatures(int note, int prevNote, float beatNum,int scale,float pitchTarget,float prevPitchTarget,int chordRoot, String chordType,int prevChordRoot, String prevChordType, float harmonicTensionTarget, float harmonicTensionWeight ){
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

	float harmonicTransitionScoreForSameScale(int note, int prevNote, int scaleDegree, int scale, int chordRoot){

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

	boolean checkHarmony(String chordType, String[] harmony){

		//return true if scale can be used for specific chord type
		for(int i=0;i<harmony.length;i++){
			if(chordType.equalsIgnoreCase(harmony[i])){
				return true;
			}
		}
		return false;
	}

	int checkNoteToScaleHarmony(int note, int chordRoot, int[] scale){

		//return scale degree if note works in particular scale otherwise return -1
		int temp = (note + 12 - chordRoot)%12;
		if(scale[temp] == 1){
			return temp;
		}
		return -1;
	}

	int checkNoteToChordHarmony(int note,int chordRoot, String chordType){

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

	int scaleHarmony(int scale){

		//return 0 for major, 1 for melodic, 2 for harmonic etc.
		return (int)(scale / 7.0);
	}

	boolean isMelodyNote(int note){
		if(note > -1){
			return true;
		}
		return false;
	}

	boolean areOctaves(int note1, int note2){

		if(Math.abs(note1-note2)%12 ==0){
			return true;
		}

		return false;
	}

	float pitchContourFeature(int note, float targetPitch, int scaleHarmony){
		if(scaleHarmony == -1){
			//return -10;
		}
		float score = note / (noteSize *1.0f);
		return gaussianPDF(score,targetPitch,.1f);
	}

	float colorContourFeature(int note, float targetColor, int scaleDegree, int chordHarmony, int scale){

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

	float harmonicTensionContourFeature(float targetTension,float targetColor, int scaleDegree, int scale,float beatNum){

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

	boolean isNonHarmonic(int scaleDegree, int scale){
		if(scaleDegree == -1){
			return true;
		}

		String scaleName= scaleTheory.scales.get(scale);
		if(scaleTheory.diatonicTensions.get(scaleName).length==3){
			return valueInArray(scaleDegree,scaleTheory.diatonicTensions.get(scaleName)[2]);
		}
		return false;
	}

	boolean valueInArray(int val, int[] array){

		for(int i=0;i<array.length;i++){
			if(val+1 == array[i]){
				return true;
			}
		}
		return false;
	}

	float pitchToDurationFeature(int note, int duration, int scaleDegree){

		if(scaleDegree == -1){
			return -10;
		}
		float score = scaleTheory.inverseColorRatings[scaleDegree] / ((float)(duration));
		return score;
	}

	/*private float tensionFeature(){

	}*/

	int tensionModelFeature(int chordRoot, String chordType, int scale, int note){
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

	int majorTensionScale(int chordRoot, String chordType,int romanNumeral, int scale, int note){

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

	int harmonicTensionScale(int romanNumeral, int scale){

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


	int tensionScale(int romanNumeral){
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
