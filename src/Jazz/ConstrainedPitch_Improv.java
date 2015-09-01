package Jazz;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import Embodiment.Physicality;


public class ConstrainedPitch_Improv extends PitchGenerator{
	

	
	Physicality pConstraints;
	volatile List<float[]> V; //= new ArrayList<float[]>();
	volatile Map<Integer,List<Integer>> paths;
	
	public ConstrainedPitch_Improv(Physicality constraints){
		super();
		
	}
	
	@Override
	public List<List<Integer>> generatePhrase(List<List<Integer>> melody, int moveTimeConstraint){
		
		return null;	
	}
	
	@Override
	public List<List<Integer>> generatePhrase(List<float[]> melody, List<String> chordType, List<Integer> chordRoot, List<int[]> tonalCenters, PhraseParameters params, int totalDuration){
		
		return viterbi(melody,chordType,chordRoot,tonalCenters, params, totalDuration);	
	}
	
	
	
	private List<List<Integer>> viterbi(List<float[]> melody, List<String> chordType, List<Integer> chordRoot, List<int[]> tonalCenters,PhraseParameters params,int totalDuration){		
		
		//List<float[][]> V = new ArrayList<float[][]>();
		List<float[]> V = new ArrayList<float[]>();
		Map<Integer,List<Integer>> paths = new HashMap<Integer,List<Integer>>(pConstraints.nStates,1.0f);
		Map<Integer,List<Integer>> newPath;// = new HashMap<Integer,List<Integer>>();
		int state,maxState,temp;
		float score,max;
		float musicScore;
		float physicalityScore;
		int noteSize = pConstraints.noteSize;
		float[][] musicScores = new float[noteSize][noteSize];
		//float[][] physicalityScores = new float[pConstraints.stateDict[0].length][pConstraints.stateDict[0].length];
		//float[][] minArray= new float[scaleTheory.scales.size()][noteSize];
		
		System.out.println("p constraints length "+ pConstraints.stateDict[0].length);
		/*float[][] minArray = new float[noteSize][pConstraints.stateDict[0].length];
		for (float[] row: minArray)
		    Arrays.fill(row, -10000);
		for(int i=0;i<noteSize;i++){
			for(int j=0;j<pConstraints.stateDict[0].length;j++){
				minArray[i][j] = -100000;
			}
		}*/

		//System.arraycopy( src, 0, dest, 0, src.length );
		float[] minArray = new float[pConstraints.nStates];
		Arrays.fill(minArray, -10000);
		V.add(minArray);
		
		//List<Integer> searchBeam = new ArrayList<Integer>();
		
		int chordChangeCount=0;
		currentTonalCenter=tonalCenters.get(0);
		
		state = 0;
		float accumulatedDuration=0.0f;

		List<Integer> scalePath = createScalePath(melody, chordType, chordRoot,tonalCenters);

		int scale = scalePath.get(0);
		
		for(int note=0;note<noteSize;note++){
			musicScore = emissionFeatures(note, scale, melody.get(0)[0],melody.get(0)[2],chordRoot.get(0), chordType.get(0),(int)melody.get(0)[1],
					params.pitchContours[0],params.pitchWeight,
					params.colorContours[0],params.colorWeight,
					params.harmonicTensionContours[0],params.harmonicTensionWeight);

			musicScore *=20;
			System.out.println("musicscore "+ note + " = "+musicScore);

			for(int i=0;i<pConstraints.configurations.get(note).size();i++){
				physicalityScore = 1;
				//physicalityScore = pConstraints.transitionScores[pConstraints.configurations.get(note).get(i)][pConstraints.configurations.get(24).get(10)];

				state = pConstraints.stateDict[note][i];
				V.get(0)[state] = musicScore + physicalityScore;
				state = pConstraints.stateDict[note][i];
				paths.put(state, Arrays.asList(state));
			}
		}
		
		

		//accumulatedDuration+=melody.get(0)[1];

		
		

		ArrayList<Integer> tempPath;
		int contourLocation;
		for(int i=1;i<melody.size();i++){
			System.out.println("melody int = " +i);
			/*minArray = new float[noteSize][pConstraints.stateDict[0].length];
			for (float[] row: minArray)
			    Arrays.fill(row, -10000);*/
			
			
			//create a beam of good nodes to include in the search
			int[] searchBeam = createSearchBeam(V.get(0),true);
			searchBeam = Arrays.copyOfRange(searchBeam, searchBeam.length-(Math.min(400, searchBeam.length)), searchBeam.length);
			
			minArray = new float[pConstraints.nStates];
			Arrays.fill(minArray, -10000);
			V.add(minArray);
			newPath = new HashMap<Integer,List<Integer>>(pConstraints.nStates,1.0f);
			accumulatedDuration+=melody.get(i-1)[1];
			contourLocation = (int)(accumulatedDuration/totalDuration * 1000);
			//System.out.println(accumulatedDuration + "  " + params.pitchContours[contourLocation]);
			//System.out.println(i +  "   "+ chordRoot.get(i) + "   " + chordType.get(i));

			if(chordType.get(i).equalsIgnoreCase(chordType.get(i-1))!=true || chordRoot.get(i)!=chordRoot.get(i-1)){
				chordChangeCount++;
				currentTonalCenter = tonalCenters.get(chordChangeCount);
			}

			

			
			
			//first compute the music phrase scores
			scale = scalePath.get(i);
			for(int note=0;note<noteSize;note++){
				for(int note0=0;note0<noteSize;note0++){
					score=featureCalculation(note, note0, melody.get(i)[0],melody.get(i)[2], scale, chordRoot.get(i),chordType.get(i),(int)melody.get(i)[1],chordRoot.get(i-1),chordType.get(i-1),
							params.pitchContours[contourLocation],params.pitchWeight,params.pitchContours[contourLocation-1],
							params.colorContours[contourLocation],params.colorWeight,
							params.harmonicTensionContours[contourLocation],params.harmonicTensionWeight);							
					musicScores[note][note0] = score*20;
				}
			}
			
			/*
			int n1,c1;
			for(int note=0;note<noteSize;note++){
				for(int z=0;z<pConstraints.configurations.get(note).size();z++){//current configuration
					max = Integer.MIN_VALUE;
					maxState = 0;
					for(int pState=0;pState<searchBeam.length;pState++){
						n1 = pConstraints.stateMap.get(searchBeam[pState])[0]; //the note
						c1 = pConstraints.stateMap.get(searchBeam[pState])[1]; //the configuration
						score = musicScores[note][n1];
						score += pConstraints.transitionScores[pConstraints.configurations.get(note).get(z)][pConstraints.configurations.get(n1).get(c1)];			
						score += V.get(V.size()-2)[searchBeam[pState]];
						if(score>max){
							max = score;
							maxState = searchBeam[pState];//pConstraints.stateDict[note0][j];
						}
					}
					state = pConstraints.stateDict[note][z];
					V.get(V.size()-1)[state] = max;
					tempPath = new ArrayList<Integer>(paths.get(maxState));
					tempPath.add(state);
					newPath.put(state, tempPath);
				}
			}
			paths = new HashMap<Integer,List<Integer>>(newPath);
			*/
			
			
			//then jointly optimize with physical constraints (transition scores previously computed in PhysicalConstraints() constructor)
			for(int note=0;note<noteSize;note++){
				for(int z=0;z<pConstraints.configurations.get(note).size();z++){//current configuration
					max = Integer.MIN_VALUE;
					maxState = 0;
					for(int note0=0;note0<noteSize;note0++){
						for(int j=0;j<pConstraints.configurations.get(note0).size();j++){//previous configuration state
							score = musicScores[note][note0];
							score += pConstraints.transitionScores[pConstraints.configurations.get(note).get(z)][pConstraints.configurations.get(note0).get(j)];
							
							state = pConstraints.stateDict[note0][j];
							score += V.get(V.size()-2)[state];
							if(score>max){
								max = score;
								maxState = state;//pConstraints.stateDict[note0][j];
							}
						}
					}
					state = pConstraints.stateDict[note][z];
					V.get(V.size()-1)[state] = max;
					tempPath = new ArrayList<Integer>(paths.get(maxState));
					tempPath.add(state);
					newPath.put(state, tempPath);
				}
			}
			paths = new HashMap<Integer,List<Integer>>(newPath);
			
		}

		
		//get optimal path
		//List<Integer> keyset=new ArrayList<Integer>(paths.keySet());  
		max = Integer.MIN_VALUE;
		maxState=-1;
		state=0;

		for(int i=0;i<pConstraints.nStates;i++){
			if(V.get(V.size()-1)[i] > max){
				maxState = i;//pConstraints.stateDict[note][config];
				max = V.get(V.size()-1)[state];
			}
		}
		
		/*
		for(int note=0;note<noteSize;note++){
			for(int config=0;config<pConstraints.configurations.get(note).size();config++){
				if(V.get(V.size()-1)[note][config] > max){
					maxState = pConstraints.stateDict[note][config];
					max = V.get(V.size()-1)[note][config];
				}
			}
		}*/

		
		List<Integer> optimalPath = new ArrayList<Integer>(paths.get(maxState));
		List<Integer> noteSequence = new ArrayList<Integer>();
		List<Integer> scaleSequence = new ArrayList<Integer>();
		int armsInUse = 0;
		for(int i=0;i<optimalPath.size();i++){ //decode the state indices 
			
			temp = pConstraints.stateMap.get(optimalPath.get(i))[0]; //the note
			noteSequence.add(temp);
			armsInUse = pConstraints.armLocations.get(pConstraints.configurations.get(temp).get(pConstraints.stateMap.get(optimalPath.get(i))[1])).length;
			for(int j=0;j<armsInUse;j++){
				System.out.println(j + "  " + pConstraints.armLocations.get(pConstraints.configurations.get(temp).get(pConstraints.stateMap.get(optimalPath.get(i))[1]))[j]);
			}		
		}
		List<List<Integer>> sequences = new  ArrayList<List<Integer>>();
		sequences.add(noteSequence);
		sequences.add(scalePath);
		System.out.println("state sequence = " + optimalPath);
		System.out.println("note Sequence = " + noteSequence);
		System.out.println("scale Sequence = " + scalePath);
		return sequences;
	}
	

	
	private int[] createSearchBeam(final float[] v, boolean keepUnsorted){
		
		/*ArrayIndexComparator comparator = new ArrayIndexComparator(scores);
		Integer[] indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);
		return Arrays.copyOfRange(indexes, 0, endIndex);*/

		final Integer[] II = new Integer[v.length];
		for (int i = 0; i < v.length; i++) II[i] = i;
		Arrays.sort(II, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return Float.compare(v[o1],v[o2]);
			}
		});
		int[] ii = new int[v.length];
		for (int i = 0; i < v.length; i++) ii[i] = II[i];
		if (!keepUnsorted) {
			float[] clon = v.clone();
			for (int i = 0; i < v.length; i++) v[i] = clon[II[i]];
		}
		return ii;

	}
	
	

    
    private class ViterbiCallable implements Callable {
    	private int moveTimeConstraint = 39;
    	private List<Integer> melody;
    	private int note,start,end;
    	int[] searchBeam;
    	float[] musicScores;
    	ViterbiCallable(int note,int start,int end,float []musicScores, int[] searchBeam, List<Integer> melody) {
    		this.note = note;
    		this.musicScores = musicScores;
    		this.searchBeam = searchBeam;
    		this.melody = melody;
    		this.start = start;
    		this.end =end;
    	}

    	@Override
    	public Map<Integer,List<Integer>> call() {
    		int n1,c1, strikingArm,prevStrikingArm;
    		int maxState,state;
    		float score, max;
    		//Map<Integer,List<Integer>>newPath = new HashMap<Integer,List<Integer>>(pConstraints.configurations.get(note).size(),1.0f);
    		int size = end - start;
    		Map<Integer,List<Integer>>newPath = new HashMap<Integer,List<Integer>>(size,1.0f);
    		List<Integer> tempPath;
    		//for(int z=start;z<pConstraints.configurations.get(note).size();z++){//current configuration
    		//System.out.println(start + "  ,  " + end);
    		for(int z=start;z<end;z++){//current configuration
    			max = Integer.MIN_VALUE;
    			maxState = 0;

    			strikingArm = pConstraints.stateMap.get(pConstraints.stateDict[note][z])[2];
    			for(int pState=0;pState<searchBeam.length;pState++){
    				n1 = pConstraints.stateMap.get(searchBeam[pState])[0]; //the note
    				c1 = pConstraints.stateMap.get(searchBeam[pState])[1]; //the configuration
    				prevStrikingArm = pConstraints.stateMap.get(searchBeam[pState])[2];
    				score = musicScores[n1];
    				score += pConstraints.transitionScores[pConstraints.configurations.get(note).get(z)][pConstraints.configurations.get(n1).get(c1)];	

    				if(pConstraints.configurations.get(note).get(z) !=searchBeam[pState]){
    					if(melody.get(1) < moveTimeConstraint){
    						if(strikingArm == pConstraints.stateMap.get(searchBeam[pState])[2] &&note!= n1){
    							//if the same arm is used to strike both notes
    							score -= 1000;//cannot make the move in time
    						}

    						if(pConstraints.armLocations.get(pConstraints.configurations.get(note).get(z))[prevStrikingArm] != pConstraints.armLocations.get(pConstraints.configurations.get(n1).get(c1))[prevStrikingArm]){ //if the previous striking arm has to move)
    							//if the previous striking arm moves right away
    							score-=1000;
    						}
    					}

    				}
    				score += V.get(V.size()-2)[searchBeam[pState]];
    				if(score>max){
    					max = score;
    					maxState = searchBeam[pState];//pConstraints.stateDict[note0][j];
    				}
    			}
    			state = pConstraints.stateDict[note][z];
    			V.get(V.size()-1)[state] = max;
    			tempPath = new ArrayList<Integer>(paths.get(maxState));
    			tempPath.add(state);
    			newPath.put(state, tempPath);

    		}
    		return newPath;
    	}

    }



}
