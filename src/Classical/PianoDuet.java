package Classical;

//
// To do list:
    /*
    1, print out the note one by one instead of print the whole arraylist at each timestamp .. solved
    2, append the keywords like "chord", "note" .. solved
    3, if there are more than one note in one time stamp, append "CHORD" .. solved
    4, mismatch the measure: piano2's measure two maps to piano1's measure one
    5, how to simplify the measure and time stamp writing
    6, convert decimal to fraction
    7, remove "null" and 0 in the chord or note
     */


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by Liang Tang on 9/22/15.
 */
public class PianoDuet {

    //Inner Class (MusicEvent): 4 elements for each timestamp:
    //   instru1_pitch, instru1_duration, instru2_pitch, instru2_duration
    class MusicEvent {

        //Constructor
        public MusicEvent() {

        }

        public List<Integer> instru1_pitch_num = new ArrayList<Integer>();    //just need the number!!!!
        public List<String> instru1_pitch = new ArrayList<String>();
        public List<String> instru2_pitch = new ArrayList<String>();  //midi pitch in number can be used directly in Max
        public List<Integer> instru1_duration = new ArrayList<Integer>();
        public List<Integer> instru2_duration = new ArrayList<Integer>();
//        public List<Float> instru2_waitTime = new ArrayList<Float>();   //this will be used as the first parameter of Piano 2; typically, '0', '0.5', '1'
    }

    //key signaure
    protected Integer key_1;    //corresponding to the 'fifth' in the music XML. This is for piano 1
    protected Integer key_2;    //This is for piano 2
    protected String mode_1;    //'major' or 'minor'. This is for piano 1
    protected String mode_2;    //This is for piano 2

    //time signature
    protected Integer beats;   //numerator of signature
    protected Integer beatType;    //denumerator of signature
    protected Integer bpm;     //corresponding to the 'tempo' in music XML

    //MIDI divisions per quarter
    protected Integer divisions_1;
    protected Integer divisions_2;

    //value of the TreeMap
    protected MusicEvent musicEvent = new MusicEvent();

    //initialize a instance of AntescofoScore
    protected AntescofoScore antescofoScore = new AntescofoScore();

    //two parts of score
//    public static String score_part;  //typical value: 'P1' or 'P2'
    protected static Integer measureNum = 1;

//    //Keyword using in print out the score: CHORD, NOTE
//    protected static String chord_notation = "CHORD";
//    protected static String note_notation = "NOTE";

    //Constructor
    public PianoDuet() {

    }


    //private static Map<Integer, MusicEvent> musicEventList = new HashMap<Integer, MusicEvent>();   //<timeStamp, musicEvent>
    protected static Map<Integer, MusicEvent> musicEventList = new TreeMap<Integer, MusicEvent>();


    //add new music event (timeStamp, pitch, duration) to the list (for piano 1)
    protected void addMusicEvent2List_1(Integer timeStamp, String p1, Integer d1) {
        MusicEvent event_1 = null;
//        if (p1 == 0) return;
        if (!musicEventList.containsKey(timeStamp)) {
            event_1 = new MusicEvent();
            event_1.instru1_pitch.add(p1);
            event_1.instru1_duration.add(d1);
            musicEventList.put(timeStamp, event_1);
        } else {
            event_1 = musicEventList.get(timeStamp);
            event_1.instru1_pitch.add(p1);
            event_1.instru1_duration.add(d1);
        }
    }


    //add new music event (timeStamp, pitch, duration) to the list (for piano 2)
    protected void addMusicEvent2List_2(Integer timeStamp, String p2, Integer d2) {
        MusicEvent event_2 = null;
//        if (p2 == 0) return;
        if (!musicEventList.containsKey(timeStamp)) {
            event_2 = new MusicEvent();
            event_2.instru2_pitch.add(p2);
            event_2.instru2_duration.add(d2);
            musicEventList.put(timeStamp, event_2);
        } else {
            event_2 = musicEventList.get(timeStamp);
            event_2.instru2_pitch.add(p2);
            event_2.instru2_duration.add(d2);

//            musicEvent.instru2_pitch.add(p2);
//            musicEvent.instru2_duration.add(d2);
//            musicEventList.put(timeStamp, musicEvent);
        }
    }

    //get music event from the list at specific time stamp
    public MusicEvent getMusicEvent(String timeStamp) {
        MusicEvent me = musicEventList.get(timeStamp);
        return me;
    }

    //write the augmented score text file
    public void write_antescofo() {
        try {

            File file = new File("/Users/musictechnology/Documents/Finale_Files/Augmented_Score/score_Antescofo_t1.txt");

            // if file doesnt exit, then creat it
            if(!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("BPM = " + bpm );  //WRITE THE TITLE INFO of a score； BPM
            bw.newLine();
            bw.newLine();
            bw.newLine();

            //write piano1
            Iterator entries = musicEventList.entrySet().iterator();

            // clone a music event list which starts from the second measure of score
//            Map<Integer, MusicEvent> musicEventList_instru2 = new TreeMap<Integer, MusicEvent>();
//            for (int i = 1; i < musicEventList.size(); i++) {
//                musicEventList_instru2.put(i, musicEventList.get(i-1));
//            }
//            musicEventList_instru2.putAll(musicEventList);

//            Iterator entries2 =  musicEventList_instru2.entrySet().iterator();
//            while (entries2.hasNext()) {
//                Map.Entry pair2 = (Map.Entry) entries.next();
//                System.out.println("This is instrument 2 measure" + pair2.getKey() + pair2.getValue());
//            }
//            //key word
//            String keyword1 = "";
//            String keyword2 = "";
            boolean flagStop;
            int measureNum2;
            while (entries.hasNext()) {
                Map.Entry pair = (Map.Entry) entries.next();

                measureNum = (Integer) pair.getKey() / 1000;
//                System.out.println(" Add first NOTE1st ITER : "+ measureNum);
//
//                flagStop = false;
//                while (entries2.hasNext()&&!flagStop) {
//
//                    Map.Entry pair2 = (Map.Entry) entries2.next();
//                    measureNum2 = (Integer) pair2.getKey() / 1000;
//                    System.out.println(" 2nd ITER Hello  MM" + measureNum2+  " TS "+(Integer) pair2.getKey() );
//                    if(measureNum2 > (measureNum+1)){
//                        flagStop = true;
//                    }else{
//                        if(measureNum2 == (measureNum+1)){
//                            //Add Bussiness Logic here
//                        }
//                        System.out.println(" 2nd ITER : "+ measureNum2 + " ts "+ (Integer) pair2.getKey());
//                    }
//                }

                String content_1 =  "; ----------- measure " + measureNum + " ---";

                MusicEvent event = ((MusicEvent) pair.getValue());
                System.out.println(event.instru1_pitch.size());


//                content_2 = "Time Stamp = " + pair.getKey() + " piano part " + ", " + ((MusicEvent) pair.getValue()).instru1_pitch + ((MusicEvent) pair.getValue()).instru1_duration + " \n\n"
//                        + "             " + pair.getKey() + " marimba part " + ", " + ((MusicEvent) pair.getValue()).instru2_pitch + ((MusicEvent) pair.getValue()).instru2_duration + " \n\n";
//                if (((MusicEvent) pair.getValue()).instru1_pitch.size() > 1) {
//                    keyword1 = "CHORD";
//                } else {
//                    keyword1 = "NOTE";
//                }
//
//                if (((MusicEvent) pair.getValue()).instru2_pitch.size() > 1) {
//                    keyword2 = "CHORD";
//                } else {
//                    keyword2 = "NOTE";
//                }

                //This is for instrument 1

//                content_2 = antescofoScore.toChord(((MusicEvent) pair.getValue()).instru1_pitch);

//                if (((MusicEvent) pair.getValue()).instru1_pitch.size() > 1 ) {

                //Write piano part first
                String content_2 = antescofoScore.toAntescofoScore(((MusicEvent) pair.getValue()).instru1_pitch) +
                            + antescofoScore.toDuration(((MusicEvent) pair.getValue()).instru1_duration, divisions_1, ((MusicEvent) pair.getValue()).instru1_pitch) + " \n\n";
//                }


                //Then, write instrument 2
                String content_3 = antescofoScore.toAntescofoScore(antescofoScore.pitchToValue(((MusicEvent) pair.getValue()).instru2_pitch), ((MusicEvent) pair.getValue()).instru2_duration, "mnote", divisions_2);

//                if (((MusicEvent) pair.getValue()).instru2_pitch.size() > 1 ) {
//                    content_3 = "       CHORD " + " ( " + ((MusicEvent) pair.getValue()).instru2_pitch + " ) " + ((MusicEvent) pair.getValue()).instru2_duration.get(0) / (float) divisions_2  + " \n\n";
//                } else if (((MusicEvent) pair.getValue()).instru2_pitch.size() == 1) {
//                    content_3 = "       NOTE " + ((MusicEvent) pair.getValue()).instru2_pitch + " " + ((MusicEvent) pair.getValue()).instru2_duration.get(0)/(float)divisions_2 + " \n\n";
//                }

                if ((Integer)pair.getKey() > measureNum*1000 && (Integer)pair.getKey() < measureNum*1000 + 999) {
                    bw.write(content_2);
                    bw.write(content_3);
                } else {
                    bw.write(content_1);    //write the info of measure number: "---------measure # ----"

                    bw.newLine();
                    bw.write(content_2);    //write the real content in each measure within each time stamp
                    bw.write(content_3);
                }
                content_2 = "";
                content_3 = "";
            }
            bw.close();
            System.out.println("BPM  " + bpm);
            System.out.println("Antescofo Score Writing is done");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /*
    This method write the score in original format
     */
    public void write_originalScore() {
        try {
//            String content_3 = "";

            File file = new File("/Users/musictechnology/Documents/Finale_Files/Augmented_Score/score_raw.txt");

            // if file doesnt exit, then creat it
            if(!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("BPM = " + bpm );  //WRITE THE TITLE INFO of a score； BPM
            bw.newLine();
            bw.newLine();
            bw.newLine();

            //write piano1
            Iterator entries = musicEventList.entrySet().iterator();


            while (entries.hasNext()) {
                Map.Entry pair = (Map.Entry) entries.next();
                measureNum = (Integer) pair.getKey() / 1000;
                String content_1 =  "; ----------- measure " + measureNum + " ---";

//                MusicEvent event = ((MusicEvent) pair.getValue());

                String content_2 = "Time Stamp = " + pair.getKey() + " piano part " + ", " + ((MusicEvent) pair.getValue()).instru1_pitch + ((MusicEvent) pair.getValue()).instru1_duration + " \n\n"
                        + "             " + pair.getKey() + " marimba part " + ", " + ((MusicEvent) pair.getValue()).instru2_pitch + ((MusicEvent) pair.getValue()).instru2_duration + " \n\n";


                if ((Integer)pair.getKey() > measureNum*1000 && (Integer)pair.getKey() < measureNum*1000 + 999) {
                    bw.write(content_2);
//                    bw.write(content_3);
                } else {
                    bw.write(content_1);    //write the info of measure number: "---------measure # ----"

                    bw.newLine();
                    bw.write(content_2);    //write the real content in each measure within each time stamp
//                    bw.write(content_3);
                }
//                content_3 = "";
            }
            bw.close();
            System.out.println("BPM  " + bpm);
            System.out.println("Raw Score Writing is done");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

}
