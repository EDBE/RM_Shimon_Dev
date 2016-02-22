package Classical;

/*
The problem is at the method 'pitchToValue'. When the instrument 2 is in rest, the pitch value of it is "". This cannot
be mapped to the hashmap of pitchValue and octave. (It is kind of solved, but not stable.
 */

/*
To do list:
1, method of group all the notes of instrument 2 which are in the same measure into a string (or arraylist of string)
2, mismatch the instrument 1 and instrument 2 by one measure. Probably use two iterator.
 */

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Liang Tang on 10/22/15.
 */
public class AntescofoScore {

    private static final Map<String, Integer> pitchValues;
    static {
        pitchValues = new HashMap<String, Integer>();

        pitchValues.put("C", 0);
        pitchValues.put("C#", 1);
        pitchValues.put("Db", 1);
        pitchValues.put("D", 2);
        pitchValues.put("D#", 3);
        pitchValues.put("Eb", 3);
        pitchValues.put("E", 4);
        pitchValues.put("F", 5);
        pitchValues.put("F#", 6);
        pitchValues.put("Gb", 6);
        pitchValues.put("G", 7);
        pitchValues.put("G#", 8);
        pitchValues.put("Ab", 8);
        pitchValues.put("A", 9);
        pitchValues.put("A#", 10);
        pitchValues.put("Bb", 10);
        pitchValues.put("B", 11);
        pitchValues.put("Cb", 11);

//        pitchValues.put("c", 0);
//        pitchValues.put("c#",1);
//        pitchValues.put("db", 1);
//        pitchValues.put("d", 2);
//        pitchValues.put("d#",3);
//        pitchValues.put("eb",3);
//        pitchValues.put("e", 4);
//        pitchValues.put("f", 5);
//        pitchValues.put("f#", 6);
//        pitchValues.put("gb",6);
//        pitchValues.put("g", 7);
//        pitchValues.put("g#",8);
//        pitchValues.put("ab",8);
//        pitchValues.put("a", 9);
//        pitchValues.put("a#",10);
//        pitchValues.put("bb",10);
//        pitchValues.put("b", 11);
//        pitchValues.put("cb", 11);
    }

    // switch the key and value in the hashmap of pitchValue
    private static final Map<Integer, String> pitchValues_inverse;
    static {
        pitchValues_inverse = new HashMap<Integer, String>();
        for (String key: pitchValues.keySet()) {
            pitchValues_inverse.put(pitchValues.get(key), key);
        }
    }

    private static final Map<Integer, Integer> octaveValues;
    static{
        octaveValues = new HashMap<Integer, Integer>();

        octaveValues.put(0, 21);
        octaveValues.put(1, 24);
        octaveValues.put(2, 36);
        octaveValues.put(3, 48);
        octaveValues.put(4, 60);
        octaveValues.put(5, 72);
        octaveValues.put(6, 84);
        octaveValues.put(7, 96);
        octaveValues.put(8, 108);
    }

    //switch the key and value in the hashmap of octaveValues
    private static final Map<Integer, Integer> octaveValues_inverse;
    static {
        octaveValues_inverse = new HashMap<Integer, Integer>();
        for (Integer key: octaveValues.keySet()) {
            octaveValues_inverse.put(pitchValues.get(key), key);
        }
    }

    public static final Map<Integer, Integer> keyToMidi;
    static {
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

//    protected PianoDuet originalScore;
//    protected PianoDuet.MusicEvent originalEvent;
//
//    protected int antescofoNote_num = 0;
//    protected String antescofoNote_pitch = "";
//    private int antescofoDivision = 0;
//    private int antescofoDuration = 0;

    //Constructor
    public AntescofoScore() {

    }

    /*
    input: list of string
    output: string
    function: return every element in the list, without bracket and comma
     */
    protected String returnNoBracket(List<String> ls) {
        String listString = "";
        for (String s: ls) {
            listString += s + " ";
        }
        return listString;
    }

    /*
    input: pitch array list
    output: pitch array list without 0
     */
    protected List<String> removeZeroInPitch (List<String> notes) {
        for (int i = notes.size()-1; i >= 0; i--) {
            if (notes.get(i).equalsIgnoreCase("")) {
                notes.remove(i);
            }
        }
        return notes;
    }

    /*
    input: duration array list
    output: duration array list without 0
     */
    protected List<Integer> removeZeroInDuration (List<Integer> durs) {
        for (int i = durs.size() - 1; i >= 0; i--) {
            if (durs.get(i) == 0) {
                durs.remove(i);
            }
        }
        return durs;
    }

    /*
    input: pitch array list
    output: pitch in string
    This is for instrument 1
     */
    protected String toAntescofoScore(List<String> notes) {
        String afterReorganize = "";
        notes = removeZeroInPitch(notes);
        if (notes.size() > 1) {
            afterReorganize = toChord(notes);
        } else if (notes.size() == 1){
            afterReorganize = toNote(notes);
        }
        return afterReorganize;
    }

    /*
    input: pitch array list in string
    output: pitch array list in Integer (midi pitch to value)
    This is for instrument 2
     */
    protected List<Integer> pitchToValue(List<String> notes) {
        List<Integer> midiValues = new ArrayList<Integer>();
//        removeZeroInPitch(notes);
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).equalsIgnoreCase("")) {
                continue;
            } else {
                midiValues.add(midiToValue(getPitchStep(notes.get(i)), getOctave(notes.get(i))));
            }
            midiValues = removeZeroInDuration(midiValues);  //just use 'removeZeroInDuraion' function since it can remove zero in Integer array list
        }
        return midiValues;
    }

    /*
    input: pitch array list in integer (without 0), duration array list (with/without 0), keyword: could be "mnote"
    output: several lines of midi info including the pitch value, duration value, and the delay ()
     */
    protected String toAntescofoScore(List<Integer> notes, List<Integer> durs, String keyword, Integer div) {
        String afterReoganize = "";
        if (notes.size() == 0) {
            afterReoganize = "";
        }
//        durs = removeZeroInDuration(durs);
        else if (notes.size() == 1) {
            //This is a single note
            afterReoganize = "      " + keyword + " " + notes.get(0) + " " + durs.get(0);
        } else if (notes.size() > 1) {
            //This is a chord
            for (int i = 0; i < notes.size(); i++) {
                afterReoganize = "      " + i * 0.1 + "ms" + " mnote " + notes.get(i) + " " + durs.get(i) / div + "\n\n";
            }
        }

        return afterReoganize;
    }

    /*
    input: chord in the format of raw score (list of string)
    output: chord in the format of Antescofo (string)
    */
    protected String toChord(List<String> notes) {
        String chord = "";
        if (notes.size() > 1) {
            chord = "CHORD ( " + returnNoBracket(notes) + ") ";
        } else System.out.println("This is not a chord");
        return chord;
    }

    /*
    input: note in the format of raw score (list of string)
    output: note in the format of Antescofo (string)
    */
    protected String toNote(List<String> notes) {
        String note = "";
        if (notes.size() == 1) {
            note = "NOTE " +  returnNoBracket(notes) + " ";
        } else {
            System.out.println("This is not a note");
        }
        return note;
    }

    /*
    This method convert the duration of note to the format of Antescofo
     */
    protected float toDuration(List<Integer> dur, int div, List<String> notes) {
        if (dur.size() <= 0) {
            return 0;
        }
        float antescofo_dur = 0;
        removeZeroInDuration(dur);
        Collections.sort(dur);
        antescofo_dur = dur.get(dur.size() - 1);
        return antescofo_dur / div;
    }

    /*
    input: MIDI pitch in string
    output: get the pitch step
     */
    protected String getPitchStep(String pitch) {
        String step = "";
        if (pitch.length() == 2) {
            step = pitch.substring(0, 1);
        } else if (pitch.length() == 3){
            step = pitch.substring(0, 2);
        }
        return step;
    }

    /*
    input: MIDI pitch in string
    output: get the octave
     */
    protected String getOctave(String pitch) {
        String octave = "";
        if (pitch.length() == 2) {
            octave = pitch.substring(1, 2);
        } else if (pitch.length() == 3){
            octave = pitch.substring(2, 3);
        }
        return octave;
    }

    /*
    input: MIDI pitch in String
    output: MIDI pitch value
     */
    protected Integer midiToValue(String step, String octave) {
        Integer midiValue = 0;
        if (step != "" && octave != "") {
            midiValue = pitchValues.get(step) + octaveValues.get(Integer.valueOf(octave));
        } else {
            System.out.println("midi value is not exist");
        }
        return midiValue;
    }


//    private String iteration(PianoDuet p) {
//        String Antescofo_content = "";
//        Iterator instru1 = p.musicEventList.entrySet().iterator();
//        Iterator instru2 = p.musicEventList.entrySet().iterator();
//
//        while (instru1.hasNext()) {
//            Map.Entry pair = (Map.Entry) instru1.next();
//            Antescofo_content = toChord(
//        }
//
//        return Antescofo_content;
//    }
}
