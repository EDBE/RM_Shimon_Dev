package Jazz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SongAnalysis {

	ScaleTheory scaleTheory = new ScaleTheory();


	
	int[][] transitionMatrix = new int[][]{ //int[first chord][next chord]
			{1,0,0,3,0,1,0},
			{0,0,0,0,3,0,0},
			{0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0},
			{3,0,0,0,0,0,0},
			{0,1,0,0,0,0,0},
			{0,0,0,0,0,0,0}
	};
	
	
	

	public SongAnalysis(){
		System.out.println("transition " + transitionMatrix[4][0]);
	}

	public void findKeyCenters(List<List<String>> chordTypes, List<List<Integer>> chordRoots, int songKeyRoot, String songKeyMode){

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
			for(int degree=0;degree<7;degree++){
				for(int mode = 0;mode<2;mode++){
					value = new int[3];
					value[0] = mode;
					value[1] = key;
					value[2] = degree;
					stateToType.put(state, value);
					state++;
				}
			}
		}

		
		

		List<int[][][]> V = new ArrayList<int[][][]>();
		Map<Integer,List<Integer>> paths = new HashMap<Integer,List<Integer>>();
		Map<Integer,List<Integer>> newPath = new HashMap<Integer,List<Integer>>();
		int score;
		int[][][] emissions = new int[2][12][7];
		
		
		//for first chord
		V.add(new int[2][12][7]);
		state = 0;
		for(int key=0;key<12;key++){
			for(int degree=0;degree<7;degree++){ //degree refers to the chord roman numeral (such as I, ii, iii, IV, V, etc)			
				for(int mode = 0;mode<2;mode++){
					switch(mode){
					case 0:
						V.get(0)[0][key][degree] = chordInMajorKey(key,chordRoots.get(0).get(0),chordTypes.get(0).get(0),degree);//probability that chord belongs to specific major key
						break;
					case 1:
						//V.get(0)[1][key][degree] = chordInMelodicMinorKey(key,chordRoots.get(0).get(0),chordTypes.get(0).get(0),degree);//probability that chord belongs to specific melodic minor key
						break;
					default:
						break;
					
					}
					paths.put(state, Arrays.asList(state));
					state++;
				}
			}
		}
		
		//for all other chords/observations
		for(int i=1;i<chordTypes.size();i++){
			for(int j=0;j<chordTypes.get(i).size();j++){
				System.out.println("chord: " +chordRoots.get(i).get(j) + " "+ chordTypes.get(i).get(j));
				//Integer[][][] allKeys = new Integer[2][12][7]; //2 modes, 12 possible roots, 7 roman numerals
				emissions = new int[2][12][7];
				for(int key=0;key<12;key++){
					for(int degree=0;degree<7;degree++){ //degree refers to the chord roman numeral (such as I, ii, iii, IV, V, etc)			
						emissions[0][key][degree] = chordInMajorKey(key,chordRoots.get(i).get(j),chordTypes.get(i).get(j),degree);//probability that chord belongs to specific major key		
						//emissions[1][key][degree] = chordInMelodicMinorKey(key,chordRoots.get(i).get(j),chordTypes.get(i).get(j),degree);//probability that chord belongs to specific melodic minor key
					
					}
				}

				//transition probabilities
				state=0;
				state0=0;
				V.add(new int[2][12][7]);
				newPath = new HashMap<Integer,List<Integer>>();
				ArrayList<Integer> tempPath;
				int temp,  multiplier;
				for(int key=0;key<12;key++){
					for(int degree=0;degree<7;degree++){
						for(int mode=0; mode<2;mode++){
							
							max = Integer.MIN_VALUE;
							maxState=-1;
							state0=0;
							for(int key0=0;key0<12;key0++){
								for(int degree0=0;degree0<7;degree0++){
									for(int mode0=0; mode0<2;mode0++){

										
										if(key == key0){
											multiplier = 5;
											score = (emissions[mode][key][degree] + transitionMatrix[degree0][degree]*multiplier + V.get(V.size()-2)[mode0][key0][degree0]);
										}else{
											multiplier = 0;
											score = (emissions[mode][key][degree]  + V.get(V.size()-2)[mode0][key0][degree0]);
										}
										//score = (emissions[mode][key][degree] + transitionMatrix[degree0][degree] + V.get(V.size()-2)[mode0][key0][degree0]);
										//score = (emissions[mode][key][degree]  + V.get(V.size()-2)[mode0][key0][degree0]);
										
										
										//other features
										if(chordRoots.get(i).get(j) == songKeyRoot){
											score +=5;
										}
										if(key == key0){
											score+=5;
										}
										
										if(chordTypes.get(i).get(j).equalsIgnoreCase("Maj7")){
											if(degree == 0){
												score+=15;//most likely a root
											}
										}
										
										if(chordTypes.get(i).get(j).equalsIgnoreCase("Maj7")){
											if(degree == 3){
												score+=10;// likely to use Maj7 on 4 chord
											}
										}
										
										
										temp = scaleTheory.majorScaleForm[degree]+songKeyRoot;
										if(temp > 11) temp -=12;
										if(chordRoots.get(i).get(j) == temp){
											score+=5;
										}

										
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
		}
		
		//get optimal path
		max = Integer.MIN_VALUE;
		maxState=-1;
		state=0;
		for(int key=0;key<12;key++){
			for(int degree=0;degree<7;degree++){
				for(int mode=0; mode<2;mode++){
					if(V.get(V.size()-1)[mode][key][degree] > max){
						maxState = state;
						max = V.get(V.size()-1)[mode][key][degree];
					}
					state++;
				}
			}
		}
		System.out.println("optimal end state = " +maxState);
		List<Integer> optimalPath = new ArrayList<Integer>( paths.get(maxState));
		System.out.println(optimalPath);
		//translate values into readable format
		for(int i=0;i<optimalPath.size();i++){
			System.out.println(stateToType.get(optimalPath.get(i))[0] + "  " + stateToType.get(optimalPath.get(i))[1]+"  "+ stateToType.get(optimalPath.get(i))[2]);
		}
	}
	
	private int chordInMajorKey(int queryKey, int chordRoot, String chordType, int degree){
		int score = -100;
		//first check if the scale has the chordRoot
		if(scaleTheory.majorModes.get(0)[Math.abs((queryKey - chordRoot)%12)] == 1){
			score=+2;
			
			int nOnes = 0;
			for(int i=0;i<Math.abs((queryKey - chordRoot)%12)+1;i++){
				if(scaleTheory.majorModes.get(0)[i]==1){
					nOnes+=1;
				}
			}
			
			if(nOnes == degree+1){
				score=+2;
				for(int i =0;i<scaleTheory.majorScaleHarmony.get(degree).length;i++){
					if(scaleTheory.majorScaleHarmony.get(degree)[i].equalsIgnoreCase(chordType)){
						score=20;
						break;
					}
				}
			}else{
				score=-3;
			}
		}
		//System.out.println(score + "   "+ chordRoot+chordType + "   "+queryKey + "   "+degree);
		return score;
	}
	
	/*private int chordInMelodicMinorKey(int queryKey, int chordRoot, String chordType, int degree){
		int score = -100;
		
		//first check if the scale has the chordRoot
		if(scaleTheory.melodicModes.get(0)[Math.abs((queryKey - chordRoot)%12)] == 1){
			//score=1;
			for(int i =0;i<scaleTheory.melodicMinorScaleHarmony.get(degree).size();i++){
				if(scaleTheory.melodicMinorScaleHarmony.get(degree).equals(chordType)){
					score=1;
					break;
				}
			}
		}
		return score;
	}*/

}
