 package MusicXML;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Jazz.Song;
import Jazz.Chords;

public class MusicXML {

	
	private static String currentNodeName;
	private static String currentattrName;
	

	private String fileName;
	
    private static final Map<String, Integer> midiValues;
    static
    {	//midi values for octave 0
    	midiValues = new HashMap<String, Integer>();
    	midiValues.put("C", 0);
    	midiValues.put("C#",1);
    	midiValues.put("Db", 1);
    	midiValues.put("D", 2);
    	midiValues.put("D#",3);
    	midiValues.put("Eb",3);
    	midiValues.put("E", 4);
    	midiValues.put("F", 5);
    	midiValues.put("F#", 6);
    	midiValues.put("Gb",6);
    	midiValues.put("G", 7);
    	midiValues.put("G#",8);
    	midiValues.put("Ab",8);
    	midiValues.put("A", 9);
    	midiValues.put("A#",10);
    	midiValues.put("Bb",10);
    	midiValues.put("B", 11);
    	midiValues.put("Cb", 11);
    	
    	midiValues.put("c", 0);
    	midiValues.put("c#",1);
    	midiValues.put("db", 1);
    	midiValues.put("d", 2);
    	midiValues.put("d#",3);
    	midiValues.put("eb",3);
    	midiValues.put("e", 4);
    	midiValues.put("f", 5);
    	midiValues.put("f#", 6);
    	midiValues.put("gb",6);
    	midiValues.put("g", 7);
    	midiValues.put("g#",8);
    	midiValues.put("ab",8);
    	midiValues.put("a", 9);
    	midiValues.put("a#",10);
    	midiValues.put("bb",10);
    	midiValues.put("b", 11);
    	midiValues.put("cb", 11);
    }
    
    private static final Map<String, Integer> romanNumeralValues;
    static
    {	//midi values for octave 0
    	romanNumeralValues = new HashMap<String, Integer>();
    	romanNumeralValues.put("i", 0);
    	romanNumeralValues.put("bii",1);
    	romanNumeralValues.put("ii", 2);
    	romanNumeralValues.put("iii", 3);
    	romanNumeralValues.put("biii", 4);
    	romanNumeralValues.put("iv", 5);
    	romanNumeralValues.put("bv", 6);
    	romanNumeralValues.put("v", 7);
    	romanNumeralValues.put("bvi", 8);
    	romanNumeralValues.put("vi", 9);
    	romanNumeralValues.put("bvii", 10);
    	romanNumeralValues.put("vii", 11);

    }
	
    
	
    private static Integer pitch;
    private static Integer duration;
    private static Integer chordRoot;
    private static String chordType = "";
    private static Integer chordDuration;
    private static int measureNum = 0;
    private static List<Integer> measurePitches = new ArrayList<Integer>();
    private static List<Integer> measureDurations = new ArrayList<Integer>();
    private static List<String> measureChordTypes = new ArrayList<String>();
    private static List<Integer> measureChordRoots = new ArrayList<Integer>();
    private static List<Integer> measureChordsDurations = new ArrayList<Integer>();
    private static List<Integer> measureInstaneousPitches = new ArrayList<Integer>();
    private static List<String> measureInstaneousChordTypes = new ArrayList<String>();
    private static List<Integer> measureInstaneousChordRoots = new ArrayList<Integer>();
    private static List<int[]> tonalCenters = new ArrayList<int[]>();
    private static int[] currentTonalCenter;
    private static List<String> chordTypesList = new ArrayList<String>();
    private static List<Integer> chordRootsList = new ArrayList<Integer>();
   
  
    
    private static int durationSinceLastChord;
    
    private static Chords chords = new Chords();
    
	public MusicXML(String file){
		fileName = file;
	}

	public Song parse(){

		Song song = new Song();
		try {

			File file = new File(fileName);

			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();

			Document doc = dBuilder.parse(file);

			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

			
			currentNodeName = doc.getDocumentElement().getNodeName();
			if (doc.hasChildNodes()) {
				printNote(doc.getChildNodes(), song);
			}
			
			measureChordsDurations.add(durationSinceLastChord);
			song.melody.add(measurePitches);
			song.durations.add(measureDurations);
			song.chordTypes.add(measureChordTypes);
			song.chordRoots.add(measureChordRoots);
			song.chordDurations.add(measureChordsDurations);
			song.instaneousMelody.add(measureInstaneousPitches);
			song.instaneousHarmonyTypes.add(measureInstaneousChordTypes);
			song.instaneousHarmonyRoots.add(measureInstaneousChordRoots);
			song.tonalCenters.addAll(tonalCenters);
			song.chordRootsList.addAll(chordRootsList);
			song.chordTypesList.addAll(chordTypesList);
			

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		
		System.out.println(song.melody.size());
		int count=0;
		for(int i=0;i<song.melody.size();i++){
			count+=song.chordRoots.get(i).size();
		}
		System.out.println(count + "   "+song.tonalCenters.size());
		//song.reduceDivisions();
		song.key = song.keyToMidi.get(song.key);
		
		song.formatLists();
		
		return song;

	}

	private static void printNote(NodeList nodeList, Song song) {
		int[] tempTonalCenter;
		for (int count = 0; count < nodeList.getLength(); count++) {

			Node tempNode = nodeList.item(count);

			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {

				// get node name and value
				//System.out.println("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
				//System.out.println("Node Value =" + tempNode.getTextContent());
				
				currentNodeName = tempNode.getNodeName();
				
				
				//song parameters
				if(currentNodeName.equalsIgnoreCase("divisions")){
					song.divisions = Integer.valueOf(tempNode.getTextContent());
				}
				if(currentNodeName.equalsIgnoreCase("fifths")){
					song.key = Integer.valueOf(tempNode.getTextContent());
				}
				if(currentNodeName.equalsIgnoreCase("mode")){
					song.mode = tempNode.getTextContent();
				}
				if(currentNodeName.equalsIgnoreCase("beats")){
					song.beats = Integer.valueOf(tempNode.getTextContent());
				}
				if(currentNodeName.equalsIgnoreCase("beat-type")){
					song.beats = Integer.valueOf(tempNode.getTextContent());
				}
				
				
				//Keep Track of Each Measure
				if(currentNodeName.equalsIgnoreCase("measure")){

					//first update the lists
					if(measureNum>0){
						measureChordsDurations.add(durationSinceLastChord);
						
						song.melody.add(measurePitches);
						song.durations.add(measureDurations);
						song.chordTypes.add(measureChordTypes);
						song.chordRoots.add(measureChordRoots);
						song.chordDurations.add(measureChordsDurations);
						song.instaneousMelody.add(measureInstaneousPitches);
						song.instaneousHarmonyTypes.add(measureInstaneousChordTypes);
						song.instaneousHarmonyRoots.add(measureInstaneousChordRoots);
					}

					//make new lists
					measurePitches = new ArrayList<Integer>();
					measureDurations = new ArrayList<Integer>();
					measureChordTypes = new ArrayList<String>();
					measureChordRoots = new ArrayList<Integer>();
					measureChordsDurations = new ArrayList<Integer>();
					measureInstaneousPitches = new ArrayList<Integer>();
					measureInstaneousChordTypes = new ArrayList<String>();
					measureInstaneousChordRoots = new ArrayList<Integer>();
					
					durationSinceLastChord = 0;
					measureNum++;
					int measureNumber=0;
					if (tempNode.hasAttributes()) {
						// get attributes names and values
						NamedNodeMap nodeMap = tempNode.getAttributes();
						for (int i = 0; i < nodeMap.getLength(); i++) {
							Node node = nodeMap.item(i);
							//System.out.println("attr name : " + node.getNodeName());
							//System.out.println("attr value : " + node.getNodeValue());
							if(node.getNodeName().equalsIgnoreCase("number")){
								measureNumber = Integer.valueOf(node.getNodeValue());
							}
						}
					}
					
					if(measureNum != measureNumber){
						System.out.println("measure count is out of sync!");
					}
				}
				
				
				//Relevant components of a "harmony" node
					if(currentNodeName.equalsIgnoreCase("harmony")){
						//measureChordsDurations.add(durationSinceLastChord);
					}

					if(currentNodeName.equalsIgnoreCase("root-step")){
						chordRoot = midiValues.get(tempNode.getTextContent());
					}
					if(currentNodeName.equalsIgnoreCase("root-alter")){
						int alter = Integer.valueOf(tempNode.getTextContent());
						chordRoot = (chordRoot +alter)%12;
					}
					if(currentNodeName.equalsIgnoreCase("kind")){
						
						String text = null;
						if (tempNode.hasAttributes()) {

							// get attributes names and values
							NamedNodeMap nodeMap = tempNode.getAttributes();

							for (int i = 0; i < nodeMap.getLength(); i++) {

								Node node = nodeMap.item(i);
								if(node.getNodeName().equalsIgnoreCase("text")){
									text = node.getNodeValue();
									//System.out.println("chord text = " +text);
								}
							}
						}
						
						chordType = text;//tempNode.getTextContent();
						
						int[] value = chords.chordTexts.get(text); //check if chord type is implemented
						if (value == null) {
							System.out.println("missing chord type = " +text);
						}
						
						
						if(measureChordRoots.size() > 0){
							measureChordsDurations.add(durationSinceLastChord);
							durationSinceLastChord = 0;
						}
						measureChordRoots.add(chordRoot);
						measureChordTypes.add(chordType);
						
						chordTypesList.add(chordType);
						chordRootsList.add(chordRoot);
						//tonalCenters.add(currentTonalCenter);
					}
				
					
					
				//Relevant components of a "note" node
				
					//components of pitch
						if(currentNodeName.equalsIgnoreCase("step")){
							pitch = midiValues.get(tempNode.getTextContent());
						}
						if(currentNodeName.equalsIgnoreCase("alter")){
							int alter = Integer.valueOf(tempNode.getTextContent());
							pitch = (pitch +alter)%12;
						}
						if(currentNodeName.equalsIgnoreCase("octave")){
							int octave = Integer.valueOf(tempNode.getTextContent());
							pitch = pitch + (12*octave);
						}
						
					if(currentNodeName.equalsIgnoreCase("rest")){
						pitch = -1;
					}
					
					if(currentNodeName.equalsIgnoreCase("duration")){
						duration = Integer.valueOf(tempNode.getTextContent());
						durationSinceLastChord += duration;
						
						measurePitches.add(pitch);
						measureDurations.add(duration);
						
						for(int i=0;i<duration;i++){
							measureInstaneousChordTypes.add(chordType);
							measureInstaneousChordRoots.add(chordRoot);
						}
						
						measureInstaneousPitches.add(pitch);
						for(int i=1;i<duration;i++){
							measureInstaneousPitches.add(Integer.valueOf(-1));
						}
						
						song.divisionsSet.add(duration);
					}
					
					//from lyric node where the tonal center is being stored
					if(currentNodeName.equalsIgnoreCase("text")){
						parseText(tempNode.getTextContent());
						tonalCenters.add(currentTonalCenter);
					}
				

					
				/*
				if (tempNode.hasAttributes()) {

					// get attributes names and values
					NamedNodeMap nodeMap = tempNode.getAttributes();

					for (int i = 0; i < nodeMap.getLength(); i++) {

						Node node = nodeMap.item(i);
						System.out.println("attr name : " + node.getNodeName());
						System.out.println("attr value : " + node.getNodeValue());
					}
				}*/
				
				if (tempNode.hasChildNodes()) {

					// loop again if has child nodes
					printNote(tempNode.getChildNodes(),song);

				}

				//System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");

			}

		}

	}
	
	private static void parseText(String text){
		
		currentTonalCenter = new int[3];
		String[] split = text.split("\\(");
		currentTonalCenter[0] = midiValues.get(split[0]);
		
		String[] splitnext = split[1].split("\\)");
		currentTonalCenter[1] = romanNumeralValues.get(splitnext[0].toLowerCase());
		
		currentTonalCenter[2] = 0;
		if(splitnext.length > 1){
			if(split[1].contains("har")){
				currentTonalCenter[2]=2;
			}else{
				currentTonalCenter[2] = 1;
			}
		}
		
	}
}
