package Jazz;

import java.util.HashMap;
import java.util.Map;

public class Chords {
	
	
    private static final Map<String, String> chordTypes;
    static
    {	//midi values for octave 0
    	chordTypes = new HashMap<String, String>();
    	chordTypes.put("dominant", "10001001001" );
    	chordTypes.put("minor-seventh", "10010001001" );
    	chordTypes.put("major", "10001001" );
    	chordTypes.put("major-sixth", "1000100101" );
    	chordTypes.put("augmented-seventh", "10001000101" );
    	chordTypes.put("minor", "10010001" );
    	chordTypes.put("major-seventh", "100010010001" );
    	chordTypes.put("diminished-seventh", "1001001001" );
    	chordTypes.put("dominant-ninth", "100010010010001" );
    	chordTypes.put("half-diminished", "10010010001" );
    	chordTypes.put("minor-sixth", "1001000101" );
    	chordTypes.put("dominant-13th", "1000100100100010010001" );
    	chordTypes.put("augmented", "100010001");
    	chordTypes.put("suspended-fourth","10000101");
    	chordTypes.put("diminished","1001001");
    	chordTypes.put("dominant-11th","100010010010001001");
    	chordTypes.put("major-minor", "10001001001");
    	chordTypes.put("major-ninth", "100010010001001");
    	chordTypes.put("minor-ninth", "100100010010001");
    	chordTypes.put("minor-major", "100100010001");
    	chordTypes.put("dominant-seventh", "10001001001");
    	chordTypes.put("minor-11th","100100010010001001");
    	chordTypes.put("augmented-ninth","100010001010001");
    	chordTypes.put("major-13th", "1000100100010010010001");
    	chordTypes.put("maj69", "100010010100001");
  
    }
    
    public static final Map<String, int[]> chordTexts;
    static
    {	//midi values for octave 0
    	chordTexts = new HashMap<String, int[]>();
    	chordTexts.put("7", new int[]{1,0,0,0,1,0,0,1,0,0,1});
    	chordTexts.put("major", new int[]{1,0,0,0,1,0,0,1});
    	chordTexts.put("dim", new int[]{1,0,0,1,0,0,1});
    	chordTexts.put("dim7", new int[]{1,0,0,1,0,0,1,0,0,1});
    	chordTexts.put("Maj7",new int[]{1,0,0,0,1,0,0,1,0,0,0,1});
    	chordTexts.put("m7b5",new int[]{1,0,0,1,0,0,1,0,0,0,1});
    	chordTexts.put("m",new int[]{1,0,0,1,0,0,0,1});
    	chordTexts.put("m7",new int[]{1,0,0,1,0,0,0,1,0,0,1});
    	chordTexts.put("", new int[]{1,0,0,0,1,0,0,1}); //major (just the root)
    	chordTexts.put("7b9",new int[]{1,0,0,0,1,0,0,1,0,0,1,0,0,1});
    	chordTexts.put("7#9",new int[]{1,0,0,0,1,0,0,1,0,0,1,0,0,0,0,1});
    	chordTexts.put("6",new int[]{1,0,0,0,1,0,0,1,0,1});
    	chordTexts.put("7+", new int[]{1,0,0,0,1,0,0,0,1,0,1});
    	chordTexts.put("+7", new int[]{1,0,0,0,1,0,0,0,1,0,1});
    	chordTexts.put("9",new int[]{1,0,0,0,1,0,0,1,0,0,1,0,0,0,1});
    	chordTexts.put("13",new int[]{1,0,0,0,1,0,0,1,0,0,1,0,0,0,1,0,0,1,0,0,0,1});
    	chordTexts.put("7#5",new int[]{1,0,0,0,1,0,0,0,1,0,1});
    	chordTexts.put("7#5#9#11",new int[]{1,0,0,0,1,0,0,0,1,0,1,0,0,0,0,1,0,0,1});
    	chordTexts.put("7b9#9#11b13",new int[]{1,0,0,0,1,0,0,1,0,0,1,0,0,1,0,1,0,0,1,0,1});

    }
    
    public static final String[] possibleChords = new String[]{
    	"7","major","dim","dim7","Maj7","m7b5","m","m7","","7b9","7#9","7#9","6","+7","7+","9","13","7#5","7#5#9#11","7b9#9#11b13"
    	
    };
    
    public Map<String, Integer> chordTypeMap;
    {
    	chordTypeMap = new HashMap<String, Integer>();
    	for(int i=0;i<possibleChords.length;i++){
    		chordTypeMap.put(possibleChords[i],i);
    	}
    	
    };
    
    public static final String[] romanNumerals = new String[]{
    	
    	"I","bII","II","bIII","III","IV","bV","V","bVI","VI","bVII","VII",
    	"i","ii","biii","iii","iv","v","vi","bvii","vii",
    	"#Idim","#IVdim","#IIdim","bIIIdim",
    	"bIIIdim","",
    	"I+","I7+","V7+",
    	"I7","bII7","II7","bII7","II7","bIII7","III7","V7","bVI7","VI7","VII7","bVII7",
    	"im7","iim7","biiim7","iiim7","vm7",
    	"iim7b5","iiim7b5","viim7b5",
    	"II7b5",
    	"ivm6",
    	"V7sus",
    	"vimMaj7"
    	
    };
    
    public static final Map<Integer, String> rootNames;
    static
    {	//midi values for octave 0
    	rootNames = new HashMap<Integer, String>();
    	rootNames.put(0,"C");
    	rootNames.put(1,"Db");
    	rootNames.put(2,"D");
    	rootNames.put(3,"Eb");
    	rootNames.put(4,"E");
    	rootNames.put(5,"F");
    	rootNames.put(6,"Gb");
    	rootNames.put(7,"G");
    	rootNames.put(8,"Ab");
    	rootNames.put(9,"A");
    	rootNames.put(10,"Bb");
    	rootNames.put(11,"B");
    }
    
    public Chords(){
    	
    }
	

}
