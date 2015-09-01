package Jazz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RhythmGenerator {

	List<List<int[]>> rhythmDict;
	int[][] rhythmDictArray;
	float[] noteDensityScores;
	float[] rhythmicComplexityScores;
	int[] rhythmicTypes;
	
	int nRhythms = 31;
	
	int tensionLevel;
	int noteDensity;
	
	float[] priors = new float[]{16,17,5,
			6,4,3,2,2,0,
			22,2,0,0,3,3,6,0,2,0,0,
			4,0,0,0,0,2,3,2,0,3,3};
	
	public RhythmGenerator(){
		adjustPriors();
	
		int[][][] tempDict = new int[][][]{
		{{1,1}, //eighths  	//0
				{1,0},		//1
				{0,1}},		//2
		{{1,1,1}, //triplets	3
			{0,1,1},		//4
			{1,0,1},		//5
			{1,1,0},		//6
			{0,1,0},		//7
			{0,0,1}},		//8
		{{1,1,1,1}, //sixteenths	//9
			{0,1,1,1},		//10
			{1,0,1,1},		//11
			{1,1,0,1},		//12
			{1,1,1,0},		//13
			{0,1,1,0},		//14
			{0,0,1,1},		//15
			{1,0,0,1},		//16
			{1,1,0,0},		//17
			{0,0,0,1},		//18
			{0,1,0,0}},		//19
		{{1,1,1,1,1,1}, //sixteenth triplets	//20
			{0,1,1,1,1,1},	//21
			{1,0,1,1,0,1},	//22
			{1,0,1,1,1,1},	//23
			{1,1,1,1,0,1},	//24
			{1,1,0,1,0,1},	//25
			{1,1,1,1,0,0},	//26
			{1,0,0,1,1,1},	//27
			{0,1,0,1,0,1},	//28
			{0,0,0,1,1,1},	//29
			{0,0,1,1,1,1}}	//30
		};
		
		int temp;
		int[] tempArray;
		int count=0, onsetCount=0;
		rhythmDictArray = new int[nRhythms][12];
		rhythmicComplexityScores = new float[nRhythms];
		noteDensityScores = new float[nRhythms];
		rhythmDict = new ArrayList<List<int[]>>();
		rhythmicTypes= new int[tempDict.length+1];
		float maxDensity =0,maxComplexity=0;
		for(int i=0;i<tempDict.length;i++){
			rhythmicTypes[i]=count;
			rhythmDict.add(new ArrayList<int[]>());
			for(int j=0;j<tempDict[i].length;j++){
				temp = 0;
				tempArray = new int[12];
				onsetCount=0;
				for(int z=0;z<tempDict[i][j].length;z++){
					tempArray[temp] = tempDict[i][j][z];
					if(tempArray[temp] > 0){
						onsetCount++;
					}
					
					temp++;
					for(int x=0;x<(12/tempDict[i][j].length)-1;x++){
						tempArray[temp] = 0;
						temp++;
					}
					
				}
				rhythmDict.get(i).add(tempArray);
				rhythmDictArray[count]=tempArray;
				noteDensityScores[count] = onsetCount/12.0f;
				//rhythmicComplexityScores[count] = (float) (calculateRhythmicComplexity(tempArray)+Math.pow(noteDensityScores[count],.01)*.05f);
				rhythmicComplexityScores[count] =calculateRhythmicComplexity(tempArray);
				if(maxDensity < noteDensityScores[count]){
					maxDensity = noteDensityScores[count];
				}
				if(maxComplexity < rhythmicComplexityScores[count]){
					maxComplexity = rhythmicComplexityScores[count];
				}
				
				//System.out.println(rhythmicComplexityScores[count]);
				count++;
			}
		}
		rhythmicTypes[rhythmicTypes.length-1]=count;
		
		for(int i=0;i<nRhythms;i++){
			rhythmicComplexityScores[i] /= maxComplexity;
			noteDensityScores[i] /= maxDensity;
			System.out.println(i + "  " + rhythmicComplexityScores[i] + "  "+ noteDensityScores[i]);
		}

	}
	
	
	private float calculateRhythmicComplexity(int[] seq){
		float score = 0;
		List<Integer> intervals = new ArrayList<Integer>();
		int count=0;
		for(int i=0;i<seq.length;i++){
			if(i==0 && seq[i] ==0){
				score+=.3f;
			}
			if(i ==0){
				count++;
			}
			
			if(i>0){
				intervals.add(count);
				count=0;
				switch(i){
					case 2:
						if (seq[i] == 1) score+=.8; //16_3
						break;
					case 3:
						if (seq[i] == 1) score+=.6;  //16
						break;
					case 4:
						if (seq[i] == 1) score+=.55;  //3, 16_3
						break;
					case 6:
						if (seq[i] == 1) score+=.25;  //16,8,16_3
						break;
					case 8:
						if (seq[i] == 1) score+=.6;  //3,16_3
						break;
					case 9:
						if (seq[i] == 1) score+=.55;  //16
						break;
					case 10:
						if (seq[i] == 1) score+=.8;  //16_3
						break;
					default:
						break;
				}
			}
		}
		float total=0;
		for(int i=0;i<intervals.size();i++){
			total+=intervals.get(i);
		}
		float avgInterval = total / intervals.size();
		
		return score + ((1 / avgInterval)*.1f);
	}
	
	private void adjustPriors(){
		float total=0.0f;
		for(int i=0;i<priors.length;i++){
			total+=priors[i];
		}
		for(int i=0;i<priors.length;i++){
			priors[i] /= total;
		}
	}
	
	public int[] generateRhythm(List<Integer> melody, Integer divisions, List<int[]> tonalCenters){
		
		//returns a list of int arrays
		/*
		 * 
		 * 
		 * 
		 */
	
		
		if(divisions<12){
			//need a division to support sixteenth and sixteenth triplets
			int additional = 12/divisions;
			System.out.println("WARNING Divsions var is being modified! from "+divisions +" to "+ 12);
			for(int i=melody.size()-1;i>-1;i--){
				for(int j=1;j<additional;j++){
					melody.add(i, -1);
				}
			}
		}
		
		
		int iterCount=0, phraseCount=0, tempCount=0;;
		int[] rhythm = new int[melody.size()];
		boolean endOfPhrase;
		
		int restSpaceLevel=3; //3 =more space between phrases, 2 medium space between phrases, 1 smaller space between phrases
		int totalBeatCount, beatNum;
		int noteDensity, rhythmicComplexity;
		
		while(iterCount < melody.size()){

			totalBeatCount = iterCount %12;
			beatNum = totalBeatCount%4;
			
			rhythm[iterCount] = melody.get(iterCount);
			if(melody.get(iterCount) != -1){
				
				endOfPhrase = false;
				phraseCount=0;
				tempCount = 1;
				while(endOfPhrase == false){

					if(melody.get(tempCount+iterCount) !=-1){
						phraseCount=0;
					}
					if(phraseCount > (12 * restSpaceLevel)){
						endOfPhrase = true;
					}
					
					phraseCount++;
					tempCount++;
				}
				
				//createRhythmicPhrase(totalBeatCount, restSpaceLevel, noteDensity, rhythmicComplexity, iterCount, iterCount+tempCount, melody);
				
			}
			iterCount++;
		}

		return rhythm;
		
	}
	
	
	public List<float[]> createRhythmicPhrase(List<Integer> melody, PhraseParameters params){
		
		/*
		 * Returns a list of arrays
		 * where each array has 3 values
		 * 1 = the midi value (-1 or no assigned value yet)
		 * 2 = the duration (12 is the length of 1 beat)
		 * 3 = the beat number (where the value before the decimal is the cumulative beat integer, aka doesn't consider location of beat within measure yet,
		 *  						and the floating point after the decimal is where within the beat the note occurs)
		 */
		
		List<int[]> rhythmicSequence = viterbiRhythmicSequence(melody,params);
		return addBeatNums(rhythmicSequence);
		
	}
	
	private List<float[]> addBeatNums(List<int[]> rhythm){
		
		int count=0;
		List<float[]> modified = new ArrayList<float[]>();
		for(int i=0;i<rhythm.size();i++){
			float[] temp = new float[4];
			temp[0] = rhythm.get(i)[0]; //midi note
			temp[1] = rhythm.get(i)[1]; //
			temp[2] = rhythm.get(i)[2] / 12.0f; //location
			temp[3] = rhythm.get(i)[2];//raw value
			modified.add(temp);
			//count+= rhythm.get(i)[1];
		}
		return modified;
	}
	/*
	public void createRhythmicPhrase(List<Integer> melody, float noteDensity, float rhythmicComplexity){
		
		viterbiRhythmicSequence(melody,noteDensity,rhythmicComplexity);
	}*/
	
	private float featureCalculation(int lastRhythm,int currentRhythm, List<Integer> beatMelody, float targetDensity, float densityWeight, float targetComplexity, float complexityWeight, float melodyVariation){
		
		float score=0;
		//emissions
		score += (noteDensityFeature(currentRhythm,targetDensity) * densityWeight);
		score += melodyVariationFeature(currentRhythm,beatMelody,melodyVariation);
		score += priors[currentRhythm];
		
		//transition
		score += (rhythmicComplexityFeature(lastRhythm, currentRhythm,targetComplexity) * complexityWeight);
		
		return score;
	}
	
	private float noteDensityFeature(int rhythm, float targetDensity){
		return gaussianPDF(noteDensityScores[rhythm], targetDensity, .4f); //NOTE: hardcoded standard deviation for now!*************
	}
	
	private float rhythmicComplexityFeature(int lastRhythm,int currentRhythm, float targetComplexity){
	
		float score=0;
		score += rhythmicComplexityScores[currentRhythm] * .5; //40% weight to current state complexity
		
		int range1=0,range2=0;
		for(int i=0;i<rhythmicTypes.length-1;i++){
			if(currentRhythm >= rhythmicTypes[i]&& currentRhythm < rhythmicTypes[i]){
				range1 = rhythmicTypes[i];
			}
			if(lastRhythm >= rhythmicTypes[i]&& lastRhythm < rhythmicTypes[i]){
				range2 = rhythmicTypes[i];
			}
		}
		if(range1 !=range2){
			score+=0.5; //60% weight to transition state complexity
		}
		return gaussianPDF(score,targetComplexity,.4f);
	}
	
	private float melodyVariationFeature(int rhythm, List<Integer> melodySegment, float degreeMelodyVariation){
	
		float score=0;
		for(int i=0;i<melodySegment.size();i++){
			if(melodySegment.get(i) != -1 && rhythmDictArray[rhythm][i] == 1){
				score+=degreeMelodyVariation;
			}
		}
		return score;
	}
	
	
	private float startingRhythmScore(int rhythm, List<Integer> melody, int beatNum, float targetDensity, float targetComplexity, float melodyVariation){
		
		float score=0;
		score += rhythmicComplexityScores[rhythm];
		score += noteDensityFeature(rhythm,targetDensity);
		score += melodyVariationFeature(rhythm, melody, melodyVariation);
		
		Set<Integer> beat1Set = new HashSet<Integer>(Arrays.asList(2,4,8,10,14,15,18,19,20,28));
		Set<Integer> beat2Set = new HashSet<Integer>(Arrays.asList(0,1,2,5,9,15));
		Set<Integer> beat3Set = new HashSet<Integer>(Arrays.asList(2,4,8,10,14,15,18,19,20,28));
		float beatParam;
		switch(beatNum){
		case 1:
			beatParam = (beat1Set.contains(rhythm)) ? .0625f : .0208f;
			break;
		case 2:
			beatParam = (beat2Set.contains(rhythm)) ? .075f : .025f;
			break;
		case 3:
			beatParam = (beat3Set.contains(rhythm)) ? .0625f : .0208f;
			break;
		default:
			beatParam=(1.0f/nRhythms);
			break;
		}
		score+=beatParam;
		return score;
	}
	
	
	private List<int[]> viterbiRhythmicSequence(List<Integer> melody, PhraseParameters params){
		
		System.out.println(melody.size());
		List<float[]> V = new ArrayList<float[]>();
		Map<Integer,List<Integer>> paths = new HashMap<Integer,List<Integer>>();
		Map<Integer,List<Integer>> newPath = new HashMap<Integer,List<Integer>>();
		List<Integer> tempPath;
		float score,max;
		int maxState=-1;
		
		int startingBeat = 1;///need to DO THIS!!!*********
		
		max=-1;
		V.add(new float[nRhythms]);
		List<Integer> specificBeatMelody = melody.subList(0, 12);
		int totalBeats=melody.size()/12;
		float contourWidth =  1000.f / totalBeats;
		System.out.println("contour width = " +contourWidth);
		for(int i=0;i<nRhythms;i++){
			V.get(0)[i] = startingRhythmScore(i,specificBeatMelody,startingBeat,params.densityContours[0],params.complexityContours[0], 1);
			paths.put(i, Arrays.asList(i));
		}

		
		for(int beat = 1;beat<totalBeats;beat++){
			specificBeatMelody = melody.subList(beat*12, (beat+1)*12);
			V.add(new float[nRhythms]);
			newPath = new HashMap<Integer,List<Integer>>();
			for(int i=0;i<nRhythms;i++){
				max=-1;
				for(int j=0;j<nRhythms;j++){
					score =0;
					score+=featureCalculation(j,i,specificBeatMelody, 
							params.densityContours[(int)(contourWidth*beat)], params.densityWeight,
							params.complexityContours[(int)(contourWidth*beat)], params.complexityWeight,
							1);
					score+=V.get(V.size()-2)[j];

					if(score>max){
						max=score;
						maxState = j;
					}
				}
				V.get(V.size()-1)[i] = max;
				tempPath = new ArrayList<Integer>(paths.get(maxState));
				tempPath.add(i);
				newPath.put(i, tempPath);
			}
			paths = new HashMap<Integer,List<Integer>>(newPath);
		}
		
		//get optimal path
		List<Integer> keyset=new ArrayList<Integer>(paths.keySet());  
		max = -1;
		maxState=-1;
		for(int i=0;i<nRhythms;i++){
			if(V.get(V.size()-1)[i] > max){
				maxState = i;
				max = V.get(V.size()-1)[i];
			}
		}
		
		List<Integer> optimalPath = new ArrayList<Integer>(paths.get(maxState));
		System.out.println(optimalPath);
		List<int[]> rhythmicInfo = new ArrayList<int[]>();
		int duration=0,noteCount=0,loc=0;
		for(int i=0;i<totalBeats;i++){
			specificBeatMelody = melody.subList(i*12, (i+1)*12);
			for(int j =0;j<12;j++){
				duration++;
				if(rhythmDictArray[optimalPath.get(i)][j] !=0){
					if(noteCount>0){
						rhythmicInfo.get(noteCount-1)[1] = duration;
					}
					rhythmicInfo.add(new int[3]); //0 = midi melody note, 1 = duration, 2 = starting location
					rhythmicInfo.get(noteCount)[0] = specificBeatMelody.get(j); 
					rhythmicInfo.get(noteCount)[2] = loc; 
					noteCount++;
					duration=0;
				}
				loc++;
			}
		}
		duration++;
		rhythmicInfo.get(noteCount-1)[1] = duration;
		
		/*for(int i=0;i<rhythmicInfo.size();i++){
			System.out.println(rhythmicInfo.get(i)[0] + "  "+rhythmicInfo.get(i)[1]);
		}*/
		return rhythmicInfo;
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
