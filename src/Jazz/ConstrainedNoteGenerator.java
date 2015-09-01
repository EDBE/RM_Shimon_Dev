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


public class ConstrainedNoteGenerator extends PitchGenerator{
	
	/*private static final List<float[]> ArrayUtils = null;
	
	ScaleTheory scaleTheory = new ScaleTheory();
	Chords chords = new Chords();
	int[] currentTonalCenter = new int[3];
	
	int noteSize = 36;
	int closePitches=0;*/
	
	
	Physicality pConstraints;
	volatile List<float[]> V; //= new ArrayList<float[]>();
	volatile Map<Integer,List<Integer>> paths;
	
	public ConstrainedNoteGenerator(Physicality constraints){
		super();
		
		pConstraints = constraints;
	}
	
	@Override
	public List<List<Integer>> generatePhrase(List<List<Integer>> melody, int moveTimeConstraint){
		
		return viterbiMelody(melody,moveTimeConstraint);	
	}
	
	@Override
	public List<List<Integer>> generatePhrase(List<float[]> melody, List<String> chordType, List<Integer> chordRoot, List<int[]> tonalCenters, PhraseParameters params, int totalDuration){
		
		return null;	
	}
	
	
	
	private List<List<Integer>> viterbiMelody(List<List<Integer>> melody, int moveTimeConstraint){		
		
		//List<float[]> V = new ArrayList<float[]>();
		V = new ArrayList<float[]>();
		//Map<Integer,List<Integer>> paths = new HashMap<Integer,List<Integer>>(pConstraints.nStates,1.0f);
		paths = new HashMap<Integer,List<Integer>>(pConstraints.nStates,1.0f);
		Map<Integer,List<Integer>> newPath;// = new HashMap<Integer,List<Integer>>();
		int state,maxState,temp;
		float score,max;
		float musicScore;
		float physicalityScore;
		//noteSize = pConstraints.noteSize;
		float[][] musicScores = new float[noteSize][noteSize];
		
		//System.out.println("p constraints length "+ pConstraints.stateDict[0].length);
		float[] minArray = new float[pConstraints.nStates];
		Arrays.fill(minArray, -10000);
		V.add(minArray);
		
		
		int startState = 22; //hardcoded as note=0 and i=0
		state = 0;
		List<Integer> possibleNotes = new ArrayList<Integer>(returnOctaves(melody.get(0).get(0)));
		//int note,note0;
		noteSize = pConstraints.noteSize;
		for(int note=0;note<noteSize;note++){
			//int note = possibleNotes.get(j);
			musicScore = melodyEmissionScore(note,melody.get(0).get(0));
			for(int i=0;i<pConstraints.configurations.get(note).size();i++){
				//physicalityScore = 1;
				physicalityScore = pConstraints.transitionScores[pConstraints.configurations.get(note).get(i)][pConstraints.configurations.get(0).get(0)];
				state = pConstraints.stateDict[note][i];
				V.get(0)[state] = musicScore + physicalityScore;
				state = pConstraints.stateDict[note][i];
				paths.put(state, Arrays.asList(state));
			}
		}
		

		ArrayList<Integer> tempPath;
		for(int i=1;i<melody.size();i++){
			//System.out.println("melody int = " +i);
			
			//create a beam of good nodes to include in the search
			//int[] searchBeam = createSearchBeam(V.get(0),true);
			//searchBeam = Arrays.copyOfRange(searchBeam, searchBeam.length-10000, searchBeam.length);
			
			List<Integer> prevPossibleNotes = new ArrayList<Integer>(returnOctaves(melody.get(i-1).get(0)));
			int[] searchBeam = createSearchBeamFromPreviousNote(prevPossibleNotes);
			
			minArray = new float[pConstraints.nStates];
			Arrays.fill(minArray, -10000);
			V.add(minArray);
			newPath = new HashMap<Integer,List<Integer>>(pConstraints.nStates,1.0f);
			
			
			//first compute the music phrase scores
			possibleNotes = new ArrayList<Integer>(returnOctaves(melody.get(i).get(0)));
			//musicScores = new float[noteSize][noteSize];
			for(int note=0;note<noteSize;note++){
				//note = possibleNotes.get(j);
				for(int note0=0;note0<noteSize;note0++){
					//note0 = prevPossibleNotes.get(z);
					score= melodyEmissionScore(note,melody.get(i).get(0));//						
					musicScores[note][note0] = score + melodyTransitionScore(note,note0,melody.get(i).get(0), melody.get(i-1).get(0));
				}
			}
			
			long begTest = new java.util.Date().getTime();
			int n1,c1, strikingArm;
			ExecutorService executor = Executors.newFixedThreadPool(16);
			List<Future<Map<Integer,List<Integer>>>> list = new ArrayList<Future<Map<Integer,List<Integer>>>>();
			for(int j=0;j<possibleNotes.size();j++){
				int note = possibleNotes.get(j);
				
				if(pConstraints.configurations.get(note).size()>500){
					int width = (int)(pConstraints.configurations.get(note).size()/10.0f);
					int start=0,end=width;
					
					for(int z=0;z<10;z++){
						//System.out.println("start = "+ start +" end = "+ end);
						Callable worker = new ViterbiCallable(note, start,end,musicScores[note],searchBeam,melody.get(i));
						Future<Map<Integer,List<Integer>>> submit = executor.submit(worker);
						list.add(submit);
						start=end;
						end+=width;
						if(z==18){end = pConstraints.configurations.get(note).size();}
					}
				}else{
					Callable worker = new ViterbiCallable(note, 0,pConstraints.configurations.get(note).size(),musicScores[note],searchBeam,melody.get(i));
					Future<Map<Integer,List<Integer>>> submit = executor.submit(worker);
					list.add(submit);
				}

				//Callable worker = new ViterbiCallable(note, 0,pConstraints.configurations.get(note).size(),musicScores[note],searchBeam,melody.get(i));
				//Future<Map<Integer,List<Integer>>> submit = executor.submit(worker);
				//list.add(submit);



				/*for(int z=0;z<pConstraints.configurations.get(note).size();z++){//current configuration
					max = Integer.MIN_VALUE;
					maxState = 0;

					strikingArm = pConstraints.stateMap.get(pConstraints.stateDict[note][z])[2];
					for(int pState=0;pState<searchBeam.length;pState++){
						n1 = pConstraints.stateMap.get(searchBeam[pState])[0]; //the note
						c1 = pConstraints.stateMap.get(searchBeam[pState])[1]; //the configuration
						score = musicScores[note][n1];
						score += pConstraints.transitionScores[pConstraints.configurations.get(note).get(z)][pConstraints.configurations.get(n1).get(c1)];	
						
						if(pConstraints.configurations.get(note).get(z) !=searchBeam[pState]){
							if(melody.get(i).get(1) < moveTimeConstraint && strikingArm == pConstraints.stateMap.get(searchBeam[pState])[2] &&note!= n1 ){
								score -= 1000;//cannot make the move in time
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
				}*/
			}
			//paths = new HashMap<Integer,List<Integer>>(newPath);
			
			


			
			// This will make the executor accept no new threads
			// and finish all existing threads in the queue
			executor.shutdown();
			// Wait until all threads are finish
			try {
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("Finished all threads");

			paths = new HashMap<Integer,List<Integer>>(pConstraints.nStates,1.0f);
			for (Future<Map<Integer,List<Integer>>> future : list) {
				try {
					paths.putAll(future.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}


			Double secs = new Double((new java.util.Date().getTime() - begTest)*0.001);
			//System.out.println("run time " + secs + " secs");




			/*
			//then jointly optimize with physical constraints (transition scores previously computed in PhysicalConstraints() constructor)
			for(int note=0;note<noteSize;note++){
				for(int z=0;z<pConstraints.configurations.get(note).size();z++){//current configuration
					max = Integer.MIN_VALUE;
					maxState = 0;
					for(int note0=0;note0<noteSize;note0++){
						for(int j=0;j<pConstraints.configurations.get(note0).size();j++){//previous configuration state
							score = musicScores[note][note0];
							score += pConstraints.transitionScores[pConstraints.configurations.get(note).get(z)][pConstraints.configurations.get(note0).get(j)];
							
							if(pConstraints.configurations.get(note).get(z) != pConstraints.configurations.get(note0).get(j)){
								if(melody.get(i).get(1) < moveTimeConstraint){
									score -= 1000;//cannot make the move in time
								}
								
							}
							
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
			*/
			
		}

		
		//get optimal path
		max = Integer.MIN_VALUE;
		maxState=-1;
		state=0;

		for(int i=0;i<pConstraints.nStates;i++){
			if(V.get(V.size()-1)[i] > max){
				maxState = i;//pConstraints.stateDict[note][config];
				max = V.get(V.size()-1)[state];
			}
		}
		
		
		List<Integer> optimalPath = new ArrayList<Integer>(paths.get(maxState));
		List<Integer> noteSequence = new ArrayList<Integer>();
		int timeStamp=0;
		int armsInUse=0;
		for(int i=0;i<optimalPath.size();i++){ //decode the state indices 
			
			temp = pConstraints.stateMap.get(optimalPath.get(i))[0]; //the note
			noteSequence.add(temp);
			armsInUse = pConstraints.armLocations.get(pConstraints.configurations.get(temp).get(pConstraints.stateMap.get(optimalPath.get(i))[1])).length;
			for(int j=0;j<armsInUse;j++){
				System.out.print(pConstraints.armLocations.get(pConstraints.configurations.get(temp).get(pConstraints.stateMap.get(optimalPath.get(i))[1]))[j] +" ");
			}
			for(int j=0;j<(4-armsInUse);j++){
				System.out.print("-1 ");
			}
			
			timeStamp+=melody.get(i).get(1);
			System.out.println(noteSequence.get(i) + " "+ optimalPath.get(i) + " " + timeStamp);
		}
		List<List<Integer>> sequences = new  ArrayList<List<Integer>>();
		sequences.add(noteSequence);
		System.out.println("state sequence = " + optimalPath);
		System.out.println("note Sequence = " + noteSequence);
		return sequences;
	}
	
	
	private int[] createSearchBeamFromPreviousNote(List<Integer> notes){
		

		int size=0;
		for(int i=0;i<notes.size();i++){
			size+=pConstraints.stateDict[notes.get(0)].length;
		}
		int[] searchBeam = new int[size];
		
		System.arraycopy(pConstraints.stateDict[notes.get(0)],0 , searchBeam, 0, pConstraints.stateDict[notes.get(0)].length);
		int destPos = pConstraints.stateDict[notes.get(0)].length;
		for(int i=1;i<notes.size();i++){
			System.arraycopy(pConstraints.stateDict[notes.get(i)],0 , searchBeam, destPos, pConstraints.stateDict[notes.get(i)].length);
			destPos+=pConstraints.stateDict[notes.get(i)].length;
		}
		
			
		return searchBeam;

		
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
