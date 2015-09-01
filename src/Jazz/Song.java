package Jazz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Song {

	//list of lists is equivalent to a list of measures with each measure containing the specified data (such as melody notes, chords, etc)
	
	public List<List<Integer>> melody = new ArrayList<List<Integer>>(); //melody notes, just onsets (list of measures)
	public List<List<Integer>> durations = new ArrayList<List<Integer>>(); //durations for each melody onset using the divisions as metric
	public List<List<String>> chordTypes = new ArrayList<List<String>>(); //chord changes
	public List<List<Integer>> chordRoots = new ArrayList<List<Integer>>();
	public List<List<Integer>> chordDurations = new ArrayList<List<Integer>>();
	
	public List<List<Integer>> instaneousMelody = new ArrayList<List<Integer>>(); //melody list with zeros in between onsets
	public List<List<String>> instaneousHarmonyTypes = new ArrayList<List<String>>(); //harmony list without negations in between chords
	public List<List<Integer>> instaneousHarmonyRoots = new ArrayList<List<Integer>>(); //harmony list without negations in between chords
	
	public List<Integer> formattedMelody = new ArrayList<Integer>(); //non measured melody list with divisions = 12
	public List<String> formattedHarmonyTypes = new ArrayList<String>(); //non measured melody harmony list without negations in between chords with divisions =12
	public List<Integer> formattedHarmonyRoots = new ArrayList<Integer>(); //non measured harmony list without negations in between chords with divisions = 12
	
	public List<List<Integer>> melodyHarmonyRelationship = new ArrayList<List<Integer>>(); //the melody that goes with each chord (as defined by chordRoots and chordTypes)
	
	//listed without using measures
	public List<int[]> tonalCenters = new ArrayList<int[]>(); //(0 is key, 1 is degree (roman numeral), 2 is scale as in major, harmonic, melodic)
	public List<String> chordTypesList = new ArrayList<String>();
	public List<Integer> chordRootsList = new ArrayList<Integer>();
	
	//key signature
	public Integer key;
	public String mode;
	
	//time
	public Integer beats; //beats per measure
	public Integer beatType; //duration equal to one beat (i.e. 4 for quarter, 8 for eighth)
	
	
	// midi divisions per beat
	public Integer divisions;
	
	//other miscellaneous descriptors
	public int nMeasures=0;
	
	// all of the division types in the piece, used later for reducing 
	public Set<Integer> divisionsSet = new HashSet<Integer>();
	
	
	//
	public SongAnalysis songAnalysis = new SongAnalysis();
	
	//keys = {0,1,2,3,4,5,6,-1,-2,-3,-4,-5,-6};
    public static final Map<Integer, Integer> keyToMidi; //key variable in mxl file uses circle of fifths
    static
    {
    	keyToMidi = new HashMap<Integer, Integer>();
    	keyToMidi.put(0, 0);
        keyToMidi.put(1, 7);
        keyToMidi.put(2, 2);
        keyToMidi.put(3, 9);
        keyToMidi.put(4, 4);
        keyToMidi.put(5, 11);
        keyToMidi.put(6, 6);
        keyToMidi.put(7, 1);
        keyToMidi.put(-1, 5);
        keyToMidi.put(-2, 10);
        keyToMidi.put(-3, 3);
        keyToMidi.put(-4, 8);
        keyToMidi.put(-5, 1);
        keyToMidi.put(-6, 6);
        keyToMidi.put(-7, 11);
    }

	
	public Song(){
		
	}
	
	public void analyzeSong(){
		songAnalysis.findKeyCenters(chordTypes, chordRoots, key, mode);
	}
	
	public void reduceDivisions(){
		
		int lowestDivision = gcd(divisionsSet);
		System.out.println(lowestDivision);
		
		if(lowestDivision > 1){
			List<Integer> temp1 = new ArrayList<Integer>();
			List<String> temp2 = new ArrayList<String>();
			List<Integer> temp3 = new ArrayList<Integer>();
			
			for(int i=0;i<nMeasures;i++){
				temp1 = new ArrayList<Integer>();
				temp2 = new ArrayList<String>();
				temp3 = new ArrayList<Integer>();
				for(int j=0;j<instaneousMelody.get(i).size();j+=lowestDivision){
					temp1.add(instaneousMelody.get(i).get(j));
					temp2.add(instaneousHarmonyTypes.get(i).get(j));
					temp3.add(instaneousHarmonyRoots.get(i).get(j));
				}
				instaneousMelody.set(i,temp1);
				instaneousHarmonyTypes.set(i,temp2);
				instaneousHarmonyRoots.set(i,temp3);
			}
		}
		
		divisions = lowestDivision;
	}
	
	public void formatLists(){
		
		//put into non measured format
		int count=0;
		for(int i =0;i<instaneousMelody.size();i++){
			for(int j=0;j<instaneousMelody.get(i).size();j+=1){
				formattedMelody.add(instaneousMelody.get(i).get(j));
				formattedHarmonyTypes.add(instaneousHarmonyTypes.get(i).get(j));
				formattedHarmonyRoots.add(instaneousHarmonyRoots.get(i).get(j));

			}
		}
		
		//put into 12 divisions
		if(divisions<12){
			//need a division to support sixteenth and sixteenth triplets
			int additional = 12/divisions;
			System.out.println("WARNING Divsions var is being modified! from "+divisions +" to "+ 12);
			for(int i=formattedMelody.size();i>0;i--){
				for(int j=1;j<additional;j++){
					formattedHarmonyTypes.add(i, formattedHarmonyTypes.get(i-1));
					formattedHarmonyRoots.add(i, formattedHarmonyRoots.get(i-1));
					formattedMelody.add(i, -1);
				}
			}
		}
	}
	
	
	/*
	 * 
	 * simple math functions below
	 * 
	 */
	
	private static int gcd(int a, int b)
	{
	    while (b > 0)
	    {
	        int temp = b;
	        b = a % b; // % is remainder
	        a = temp;
	    }
	    return a;
	}
	
	private static int gcd(Set<Integer> input)
	{
		int result = input.iterator().next();
	    for (int i : input) {
	    	result = gcd(result, i);
	    }
	    return result;
	}
	
	private static int lcm(int a, int b)
	{
	    return a * (b / gcd(a, b));
	}

	private static int lcm(Set<Integer> input)
	{
	    int result = input.iterator().next();
	    //for(int i = 1; i < input.length; i++) result = lcm(result, input[i]);
	    for (int i : input) {
	    	result = lcm(result, i);
	    }
	    return result;
	}
	
	
}
