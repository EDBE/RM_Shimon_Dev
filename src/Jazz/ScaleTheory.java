package Jazz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScaleTheory {

	
	/*
	 * Scales
	 */
	
    private static final Map<Integer, Integer> degreeMap;
    static
    {	//midi values for octave 0
    	degreeMap = new HashMap<Integer, Integer>();
    	degreeMap.put(0, 0);
    	degreeMap.put(1,1);
    	degreeMap.put(2,1);
    	degreeMap.put(3,2);
    	degreeMap.put(4,2);
    	degreeMap.put(5,3);
    	degreeMap.put(6,4);
    	degreeMap.put(7,4);
    	degreeMap.put(8,5);
    	degreeMap.put(9,5);
    	degreeMap.put(10,6);
    	degreeMap.put(11,6);

    }
    
    public static List<String> scales = Arrays.asList("ionian","dorian","phrygian","lydian","myxolydian","aeolian","locrian",
    		"minor-major","dorian b9","lydian augmented","lydian dominant","myxolydian b6","half diminished", "altered",
    		"harmonic", "locrian #6","ionian augmented","romanian","phrygian dominant","lydian #2","ultra locrian",
    		"half-whole","whole-half","whole");
    
    
    public static final Map<Integer, String> scaleMap;
    static
    {	
    	
    	scaleMap = new HashMap<Integer, String>();
    	for(int i=0;i<scales.size();i++){
    		scaleMap.put(i,scales.get(i));
    	}
    
    }
	
    
    public static final Map<String, int[]> scalePatterns;
    static
    {	
    	scalePatterns = new HashMap<String, int[]>();
    	scalePatterns.put("ionian", new int[]{1,0,1,0,1,1,0,1,0,1,0,1});
    	scalePatterns.put("dorian", new int[]{1,0,1,1,0,1,0,1,0,1,1,0});
    	scalePatterns.put("phrygian",new int[]{1,1,0,1,0,1,0,1,1,0,1,0});
    	scalePatterns.put("lydian", new int[]{1,0,1,0,1,0,1,1,0,1,0,1});
    	scalePatterns.put("myxolydian",new int[]{1,0,1,0,1,1,0,1,0,1,1,0});
    	scalePatterns.put("aeolian", new int[]{1,0,1,1,0,1,0,1,1,0,1,0});
    	scalePatterns.put("locrian", new int[]{1,1,0,1,0,1,1,0,1,0,1,0});
    	
    	scalePatterns.put("minor-major", new int[]{1,0,1,1,0,1,0,1,0,1,0,1});
    	scalePatterns.put("dorian b9", new int[]{1,1,0,1,0,1,0,1,0,1,1,0});
    	scalePatterns.put("lydian augmented",new int[]{1,0,1,0,1,0,1,0,1,1,0,1});
    	scalePatterns.put("lydian dominant", new int[]{1,0,1,0,1,0,1,1,0,1,1,0});
    	scalePatterns.put("myxolydian b6",new int[]{1,0,1,0,1,1,0,1,1,0,1,0});
    	scalePatterns.put("half diminished", new int[]{1,0,1,1,0,1,1,0,1,0,1,0});
    	scalePatterns.put("altered", new int[]{1,1,0,1,1,0,1,0,1,0,1,0});
    	
    	scalePatterns.put("harmonic", new int[]{1,0,1,1,0,1,0,1,1,0,0,1});
    	scalePatterns.put("locrian #6", new int[]{1,1,0,1,0,1,1,0,0,1,1,0});
    	scalePatterns.put("ionian augmented",new int[]{1,0,1,0,1,1,0,0,1,1,0,1});
    	scalePatterns.put("romanian", new int[]{1,0,1,1,0,0,1,1,0,1,1,0});
    	scalePatterns.put("phrygian dominant",new int[]{1,1,0,0,1,1,0,1,1,0,1,0});
    	scalePatterns.put("lydian #2", new int[]{1,0,0,1,1,0,1,1,0,1,0,1});
    	scalePatterns.put("ultra locrian", new int[]{1,1,0,1,1,0,1,0,1,1,0,0});
    	
    	scalePatterns.put("half-whole", new int[]{1,1,0,1,1,0,1,1,0,1,1,0});
    	scalePatterns.put("whole-half", new int[]{1,0,1,1,0,1,1,0,1,1,0,1});
    	scalePatterns.put("whole",new int[]{1,0,1,0,1,0,1,0,1,0,1,0});


    }
    
    
    public static final Map<String, int[][]> diatonicTensions;
    static
    {	
    	diatonicTensions = new HashMap<String, int[][]>();
    	diatonicTensions.put("ionian", new int[][]{{1,3,5,7},{2,4,6},{4}});
    	diatonicTensions.put("dorian", new int[][]{{1,3,5,7},{2,4,6}});
    	diatonicTensions.put("phrygian",new int[][]{{1,3,5,7},{2,4,6},{2}});
    	diatonicTensions.put("lydian", new int[][]{{1,3,5,7},{2,4,6}});
    	diatonicTensions.put("myxolydian",new int[][]{{1,3,5,7},{2,4,6},{4}});
    	diatonicTensions.put("aeolian", new int[][]{{1,3,5,7},{2,4,6},{6}});
    	diatonicTensions.put("locrian", new int[][]{{1,3,5,7},{2,4,6},{2}});
    	
    	diatonicTensions.put("minor-major", new int[][]{{1,3,5,7},{2,4,6}});
    	diatonicTensions.put("dorian b9", new int[][]{{1,3,5,7},{2,4,6},{2}});
    	diatonicTensions.put("lydian augmented",new int[][]{{1,3,5,7},{2,4,6},{6}});
    	diatonicTensions.put("lydian dominant", new int[][]{{1,3,5,7},{2,4,6}});
    	diatonicTensions.put("myxolydian b6",new int[][]{{1,3,5,7},{2,4,6},{4,6}});
    	diatonicTensions.put("half diminished", new int[][]{{1,3,5,7},{2,4,6}});
    	diatonicTensions.put("altered", new int[][]{{1,4,7},{2,3,5,6}});
    	
    	diatonicTensions.put("harmonic", new int[][]{{1,3,5,7},{2,4,6},{6}});
    	diatonicTensions.put("locrian #6", new int[][]{{1,3,5,7},{2,4,6},{2}});
    	diatonicTensions.put("ionian augmented",new int[][]{{1,3,5,7},{2,4,6},{4,6}});
    	diatonicTensions.put("romanian", new int[][]{{1,3,5,7},{2,4,6},{4}});
    	diatonicTensions.put("phrygian dominant",new int[][]{{1,3,5,7},{2,4,6},{4}});
    	diatonicTensions.put("lydian #2", new int[][]{{1,3,5,7},{2,4,6},{2}});
    	diatonicTensions.put("ultra locrian", new int[][]{});
    	
    	diatonicTensions.put("half-whole", new int[][]{{1,4,6,8},{2,3,5,7}});
    	diatonicTensions.put("whole-half", new int[][]{{1,3,5,7},{2,4,5,6,8}});
    	diatonicTensions.put("whole",new int[][]{{1,3,5},{2,4,6}});


    }
    
    
    
	//major
	public int[] majorScaleForm = new int[]{0,2,4,5,7,9,11};
	public List<int[]> majorModes = Arrays.asList(
			new int[]{1,0,1,0,1,1,0,1,0,1,0,1}, //ionian
			new int[]{1,0,1,1,0,1,0,1,0,1,1,0}, //dorian
			new int[]{1,1,0,1,0,1,0,1,1,0,1,0}, //phrygian
			new int[]{1,0,1,0,1,0,1,1,0,1,0,1}, //lydian
			new int[]{1,0,1,0,1,1,0,1,0,1,1,0}, //myxoldian
			new int[]{1,0,1,1,0,1,0,1,1,0,1,0}, //aeolian
			new int[]{1,1,0,1,0,1,1,0,1,0,1,0} //locrian
			);
	
	//melodic minor
	public List<int[]> melodicModes = Arrays.asList(
			new int[]{1,0,1,1,0,1,0,1,0,1,0,1}, //minor-major
			new int[]{1,1,0,1,0,1,0,1,0,1,1,0}, //dorian b9
			new int[]{1,0,1,0,1,0,1,0,1,1,0,1}, //lydian augmented
			new int[]{1,0,1,0,1,0,1,1,0,1,1,0}, //lydian dominant
			new int[]{1,0,1,0,1,1,0,1,1,0,1,0}, //myxolydian b6
			new int[]{1,0,1,1,0,1,1,0,1,0,1,0}, //half diminished
			new int[]{1,1,0,1,1,0,1,0,1,0,1,0} //altered
			);

	
	//harmonic minor
	public List<int[]> harmonicModes = Arrays.asList(
			new int[]{1,0,1,1,0,1,0,1,1,0,0,1}, //harmonic
			new int[]{1,1,0,1,0,1,1,0,0,1,1,0}, //locrian #6
			new int[]{1,0,1,0,1,1,0,0,1,1,0,1}, //ionian augmented
			new int[]{1,0,1,1,0,0,1,1,0,1,1,0}, //romanian
			new int[]{1,1,0,0,1,1,0,1,1,0,1,0}, //phrygian dominant
			new int[]{1,0,0,1,1,0,1,1,0,1,0,1}, //lydian #2
			new int[]{1,1,0,1,1,0,1,0,1,1,0,0} //ultra locrian
			);
	
	
	//Diminished
	public int[] halfWhole = new int[]{1,1,0,1,1,0,1,1,0,1,1,0};
	public int[] wholeHalf = new int[]{1,0,1,1,0,1,1,0,1,1,0,1};
	
	//whole tone
	public int[] whole = new int[]{1,0,1,0,1,0,1,0,1,0,1,0};
	
	//pentatonic
	
	
	/*
	 * Scale Harmony
	 */
	
    public static Map<String, Float> scalePriors;
    static{
    	scalePriors = new HashMap<String,Float>();
    	scalePriors.put("ionian", 10f);
    	scalePriors.put("dorian", 10f);
    	scalePriors.put("phrygian", 3f);
    	scalePriors.put("lydian", 10f);
    	scalePriors.put("myxolydian", 10f);
    	scalePriors.put("aeolian", 5f);
    	scalePriors.put("locrian", 2f);
    	
    	scalePriors.put("minor-major", 3f);
    	scalePriors.put("dorian b9", 1f);
    	scalePriors.put("lydian augmented", 3f);
    	scalePriors.put("lydian dominant", 3f);
    	scalePriors.put("myxolydian b6", 0f);
    	scalePriors.put("half diminished", 3f);
    	scalePriors.put("altered", 6f);
    	
    	scalePriors.put("harmonic", 3f);
    	scalePriors.put("locrian #6", 2f);
    	scalePriors.put("ionian augmented", 1f);
    	scalePriors.put("romanian", 0f);
    	scalePriors.put("phrygian dominant", 3f);
    	scalePriors.put("lydian #2", 1f);
    	scalePriors.put("ultra locrian", 0f);
    	
    	scalePriors.put("half-whole", 5f);
    	scalePriors.put("whole-half", 5f);
    	scalePriors.put("whole", 5f);

    }

    public static final Map<String, String[]> scaleHarmony;
    static
    {	
    	scaleHarmony = new HashMap<String, String[]>();
    	scaleHarmony.put("ionian", new String[]{"major"});
    	scaleHarmony.put("dorian", new String[]{"m7","m"});
    	scaleHarmony.put("phrygian",new String[]{"susb9"});
    	scaleHarmony.put("lydian", new String[]{"Maj7#11","Maj7","","6","maj69","major"});
    	scaleHarmony.put("myxolydian",new String[]{"7","9","sus","13"});
    	scaleHarmony.put("aeolian", new String[]{"m"});
    	scaleHarmony.put("locrian", new String[]{"dim","dim7"});
    	
    	scaleHarmony.put("minor-major", new String[]{"mMaj7","m+7","m","m6","m(add9)","m69"});
    	scaleHarmony.put("dorian b9", new String[]{"susb9","m","m7b9","m11b9","m13b9","m7"});
    	scaleHarmony.put("lydian augmented",new String[]{"+","+Maj7","Maj7#5","susb9"});
    	scaleHarmony.put("lydian dominant", new String[]{"7b5","","7","9","7#11","9#11","13#11"});
    	scaleHarmony.put("myxolydian b6",new String[]{"7#5","","7","9","7b13","9b13","sus","7sus","9sus"});
    	scaleHarmony.put("half diminished", new String[]{"m7b5","m9b5"});
    	scaleHarmony.put("altered", new String[]{"alt","7alt","7#5","7b9","7#9","+7","7+","7b5b9","7#5#9","7b9#9#11b13","7#5#9#11","7"});
    	
    	scaleHarmony.put("harmonic", new String[]{"m#5","m","m9Maj7","mb6","m(add9)","m(addb13)"});
    	scaleHarmony.put("locrian #6", new String[]{"m7b5","m7b5b9",""});
    	scaleHarmony.put("ionian augmented",new String[]{"+","+Maj7","Maj7#5"});
    	scaleHarmony.put("romanian", new String[]{"m","m7","m9","m7#11","m9#11","m13#11"});
    	scaleHarmony.put("phrygian dominant",new String[]{"7","7b9","7b9b13"});
    	scaleHarmony.put("lydian #2", new String[]{"Maj7","Maj7#11","Maj7","6","maj69","major","Maj7#9#11"});
    	scaleHarmony.put("ultra locrian", new String[]{});
    	
    	scaleHarmony.put("half-whole", new String[]{"7b9"});
    	scaleHarmony.put("whole-half", new String[]{"dim","dim7"});
    	scaleHarmony.put("whole",new String[]{"7+5","7b13"});
    }
	public List<String[]> majorScaleHarmony = Arrays.asList(
			new String[]{"major"},//ionian
			new String[]{"m7","m"}, //dorian
			new String[]{"susb9"}, //phrygian
			new String[]{"Maj7#11","Maj7","6","maj69","major"}, //lydian
			new String[]{"7","9","sus","13"}, //myxolydian
			new String[]{"m"}, //aeloian
			new String[]{"dim","dim7"} //locrian
			);
	

	public List<String[]> melodicMinorScaleHarmony = Arrays.asList(
			new String[]{"mMaj7","m+7","m","m6","m(add9)","m69"}, //minor major
			new String[]{"susb9","m","m7b9","m11b9","m13b9","m7"}, //dorian b9
			new String[]{"+","+Maj7","Maj7#5","susb9"}, //lydian augmented
			new String[]{"7b5","","7","9","7#11","9#11","13#11"}, //lydian dominant
			new String[]{"7#5","","7","9","7b13","9b13","sus","7sus","9sus"}, //myxolydian b13
			new String[]{"m7b5","m9b5"}, //half diminished or semilocrian
			new String[]{"alt","7alt","7#5","7b9","7#9","+7","7+","7b5b9","7#5#9","7b9#9#11b13","7#5#9#11","7"} //altered
			);
	

	public List<String[]> harmonicMinorScaleHarmony = Arrays.asList(
			new String[]{"m#5","m","m9Maj7","mb6","m(add9)","m(addb13)"}, //harmonic minor
			new String[]{"m7b5","m7b5b9",""}, // locrian #6
			new String[]{"+","+Maj7","Maj7#5"}, //ioninan augmented
			new String[]{"m","m7","m9","m7#11","m9#11","m13#11"}, //romanian
			new String[]{"7","7b9","7b9b13"}, //phrygian dominant
			new String[]{"Maj7","Maj7#11","Maj7","6","maj69","major","Maj7#9#11"}, //lydian #2
			new String[]{"dim","dim7"} //ultra locrian
			);
	
	
	public List<String[]> diminishedScaleHarmony = Arrays.asList(
			new String[]{"7b9"}, //half whole
			new String[]{"dim","dim7"} //whole half
			);
	
	
	public String[] wholeToneScaleHarmony = new String[]{"7+5","7b13"};

	public int[] colorRatings = {1,8,7,4,3,9,10,2,12,11,6,5};
	public int[] inverseColorRatings = {12,5,6,9,10,4,3,11,1,2,7,8};
	
	public ScaleTheory(){

		float total=0f;
		for(int i=0;i<scales.size();i++){
			total +=scalePriors.get(scales.get(i));
		}
		for(int i=0;i<scales.size();i++){
			scalePriors.put(scales.get(i), scalePriors.get(scales.get(i))/total);
		}
		
	}
	
}
