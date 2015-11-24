/**
 * Created by Liang Tang
 */

package Classical;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class MusicXMLParserForPianoDuet {

    private String fileName;    //filename of the music XML


    private static String pitch1_step = "";
    private static String pitch1_octave = "";
    private static String pitch1 = "";   //This is for piano 1
//    private static Integer pitch1_num = 0;

    private static String pitch2_step = "";
    private static String pitch2_octave = "";
    private static String pitch2 = "";   //This is for piano 2
//    private static Integer pitch2_num = 0;

    private static Integer duration1 = 0; // piano 1
    private static Integer duration2 = 0; // piano 2

    private static Integer alter1 = 0;    // piano 1
    private static Integer alter2 = 0;    // piano 2

    private static Integer timeStamp1 = 0;// piano 1: measure*1000 + counter
    private static Integer timeStamp2 = 0;// piano 2: measure*1000 + counter
    private static Integer counter = 0;     //counter is useful to deal with index issue especially for 'chord' in Music XMl
    private static Integer num_noteInChord = 1; //to record the number of notes in one chord. When it greater than 2, the counter should not be decreased by the duration





    //Constructor
    public MusicXMLParserForPianoDuet(String file) {
        fileName = file;
    }

    //Method: Parse
    //Return type: PianoDuet
    public PianoDuet parse() {
        PianoDuet pianoDuet = new PianoDuet();
        try {
            File file = new File(fileName);
            SAXBuilder builder = new SAXBuilder();

            Document document = builder.build(file);
            Element rootNode = document.getRootElement();   //rootnode is 'score-partwise'
            if (!rootNode.getName().equalsIgnoreCase("score-partwise")) {
                throw new JDOMException("Unsupported Music XML file");
            }
            List list = rootNode.getChildren("part");
            //for (int i = 0; i < list.size(); i++) {
            for (int i = 0; i < list.size(); i++) {
                Element branch_1 = (Element) list.get(i);   //branch_1 is at the position of 'part'

                //This is for piano 1
                if(branch_1.getAttributeValue("id").equalsIgnoreCase("P1")) {
                    System.out.println("Iterating through instrument 1......");
                    List list_1 = branch_1.getChildren("measure");  //list_1 is a list of measure

                    for (int j = 0; j < list_1.size(); j++) {
                        Element branch_2 = (Element) list_1.get(j);  //branch_2 is at the position of 'measure'
                        counter = 0;    //assign counter to 0 at the beginning of each measure

                        //There are some important information in measure 1
                        if(branch_2.getAttributeValue("number").equalsIgnoreCase("1")) {
                            pianoDuet.divisions_1 = Integer.valueOf(branch_2.getChild("attributes").getChildText("divisions"));
                            pianoDuet.key_1 = Integer.valueOf(branch_2.getChild("attributes").getChild("key").getChildText("fifths"));   //get # of sharp or flat
                            pianoDuet.mode_1 = branch_2.getChild("attributes").getChild("key").getChildText("mode");    //get mode: major or minor
                            pianoDuet.beats = Integer.valueOf(branch_2.getChild("attributes").getChild("time").getChildText("beats"));  //get numerator of time signature
                            pianoDuet.beatType = Integer.valueOf(branch_2.getChild("attributes").getChild("time").getChildText("beat-type"));   //get denumerator of time signature
                            pianoDuet.bpm = Integer.valueOf(branch_2.getChild("sound").getAttributeValue("tempo"));     //get bpm
                        }

                        List list_2 = branch_2.getChildren();   //list_2 is a list of notes and "backup" in one measure

                        for (int k = 0; k < list_2.size(); k++) {
                            Element branch_3 = (Element) list_2.get(k); //branch_3 is at the position of 'note' or 'backup'
                            if (branch_3.getName() == "note") {

                                //First: check the 'chord', if it is a single note, will return 'null'
                                if (branch_3.getChild("chord") == null) {

                                    //Second: check the 'rest'. if it is not the rest, this is a actual note
                                    if (branch_3.getChild("rest") == null) {
                                        pitch1_step = branch_3.getChild("pitch").getChildText("step");
                                        pitch1_octave = branch_3.getChild("pitch").getChildText("octave");
                                        pitch1 = pitch1_step + pitch1_octave;
//                                        pitch1_num = pitchValues.get(pitch1_step) + octaveValues.get(Integer.valueOf(branch_3.getChild("pitch").getChildText("octave")));    //map the pitch to the number
//                                        pitch1 = pitchValues_inverse.get(pitch1_num%12);
//                                        pitch1 = new StringBuffer(pitch1).append(pitch1_num/12).toString();

                                        if (branch_3.getChild("pitch").getChild("alter") != null) {
                                            alter1 = Integer.valueOf(branch_3.getChild("pitch").getChildText("alter"));
                                            if (alter1 == -1) {
                                                pitch1 = new StringBuffer(pitch1).insert(1, "b").toString();
                                            } else {
                                                pitch1 = new StringBuffer(pitch1).insert(1, "#").toString();
                                            }
                                            alter1 = 0;
                                        }
                                    }

                                    //if it is the rest
                                    else {
                                        pitch1 = "";
                                    }

                                    duration1 = Integer.valueOf(branch_3.getChildText("duration"));
                                    timeStamp1 = Integer.valueOf(branch_2.getAttributeValue("number")) * 1000 + counter;
                                    counter = counter + duration1;

                                    pianoDuet.addMusicEvent2List_1(timeStamp1, pitch1, duration1);

                                } else {    //if it is a note in a chord, counter and duration should be recalculated
                                    pitch1_step = branch_3.getChild("pitch").getChildText("step");
                                    pitch1_octave = branch_3.getChild("pitch").getChildText("octave");
                                    pitch1 = pitch1_step + pitch1_octave;

                                    if (branch_3.getChild("pitch").getChild("alter") != null) {
                                        alter1 = Integer.valueOf(branch_3.getChild("pitch").getChildText("alter"));
                                        if (alter1 == -1) {
                                            pitch1 = new StringBuffer(pitch1).insert(1, "b").toString();
                                        } else {
                                            pitch1 = new StringBuffer(pitch1).insert(1, "#").toString();
                                        }
                                        alter1 = 0;
                                    }

                                    counter = counter - duration1;  //This duration1 is the duration of previous note
                                    duration1 = Integer.valueOf(branch_3.getChildText("duration")); //this duration1 is the duration of current note

                                    timeStamp1 = Integer.valueOf(branch_2.getAttributeValue("number")) * 1000 + counter;
                                    pianoDuet.addMusicEvent2List_1(timeStamp1, pitch1, duration1);
                                    counter = counter + duration1;  //move the counter by the current duration
                                }
                            }

                            else if (branch_3.getName() == "backup") {
                                counter = counter - Integer.valueOf(branch_2.getChild("backup").getChildText("duration"));
                            }
                        }

                    }

                }

                // Below is for Piano 2
                else if(branch_1.getAttributeValue("id").equalsIgnoreCase("P2")) {
                    System.out.println("Iterating through instrument 2......");
                    List list_1 = branch_1.getChildren("measure");

                    for (int j = 0; j < list_1.size(); j++) {
                        Element branch_2 = (Element) list_1.get(j);  //branch_2 is at the position of 'measure'
                        counter = 0;    //assign counter to 0 at the beginning of each measure

                        //There are some important information in measure 1
                        if(branch_2.getAttributeValue("number").equalsIgnoreCase("1")) {
                            pianoDuet.divisions_2 = Integer.valueOf(branch_2.getChild("attributes").getChildText("divisions")); //get division
                            pianoDuet.key_2 = Integer.valueOf(branch_2.getChild("attributes").getChild("key").getChildText("fifths"));   //get # of sharp or flat
                            pianoDuet.mode_2 = branch_2.getChild("attributes").getChild("key").getChildText("mode");    //get mode: major or minor
                        }

                        List list_2 = branch_2.getChildren();   //list_2 is a list of notes and "backup" in one measure

                        for (int k = 0; k < list_2.size(); k++) {
                            Element branch_3 = (Element) list_2.get(k); //branch_3 is at the position of 'note' or 'backup'
                            if (branch_3.getName() == "note") {

                                //First: check the 'chord', if it is a single note, will return 'null'
                                if (branch_3.getChild("chord") == null) {

                                    if (branch_3.getChild("rest") == null) {
                                        pitch2_step = branch_3.getChild("pitch").getChildText("step");
                                        pitch2_octave = branch_3.getChild("pitch").getChildText("octave");
                                        pitch2 = pitch2_step + pitch2_octave;
//                                        pitch2_num = pitchValues.get(pitch2) + octaveValues.get(Integer.valueOf(branch_3.getChild("pitch").getChildText("octave")));    //map the pitch to the number

                                        if (branch_3.getChild("pitch").getChild("alter") != null) {
                                            alter2 = Integer.valueOf(branch_3.getChild("pitch").getChildText("alter"));
                                            if (alter2 == -1) {
                                                pitch2 = new StringBuilder(pitch2).insert(1, "b").toString();
                                            } else {
                                                pitch2 = new StringBuilder(pitch2).insert(1, "#").toString();
                                            }
                                            alter2 = 0;
                                        }
                                    }

                                    //if it is the rest
                                    else {
                                        pitch2 = "";
                                    }

                                    duration2 = Integer.valueOf(branch_3.getChildText("duration"));
                                    timeStamp2 = Integer.valueOf(branch_2.getAttributeValue("number")) * 1000 + counter;
                                    counter = counter + duration2;

                                    pianoDuet.addMusicEvent2List_2(timeStamp2, pitch2, duration2);

                                } else {    //if it is a note in a chord, counter and duration should be recalculated
                                    pitch2_step = branch_3.getChild("pitch").getChildText("step");
                                    pitch2_octave = branch_3.getChild("pitch").getChildText("octave");
                                    pitch2 = pitch2_step + pitch2_octave;
//
                                    if (branch_3.getChild("pitch").getChild("alter") != null) {
                                        alter2 = Integer.valueOf(branch_3.getChild("pitch").getChildText("alter"));
                                        if (alter2 == -1) {
                                            pitch2 = new StringBuilder(pitch2).insert(1, "b").toString();
                                        } else {
                                            pitch2 = new StringBuilder(pitch2).insert(1, "#").toString();
                                        }
                                        alter2 = 0;
                                    }

                                    counter = counter - duration2;  //This duration2 is the duration of previous note
                                    duration2 = Integer.valueOf(branch_3.getChildText("duration")); //this duration2 is the duration of current note

                                    timeStamp2 = Integer.valueOf(branch_2.getAttributeValue("number")) * 1000 + counter;
                                    pianoDuet.addMusicEvent2List_2(timeStamp2, pitch2, duration2);
                                    counter = counter + duration2;  //move the counter by the current duration
                                }
                            }

                            else if (branch_3.getName() == "backup") {
                                counter = counter - Integer.valueOf(branch_2.getChild("backup").getChildText("duration"));
                            }
                        }
                    }
                }

                else {
                    System.out.println("This parser cannot read the third part of the score!");
                }
            }
        } catch (JDOMException jdomex) {
            System.out.println(jdomex.getMessage());
        }

        return pianoDuet;
    }

}
