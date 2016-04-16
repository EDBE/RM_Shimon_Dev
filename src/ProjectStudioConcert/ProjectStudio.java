package ProjectStudioConcert;

import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

import java.util.*;

/**
 * Created by Liang Tang on 4/9/16.
 */
public class ProjectStudio extends MaxObject{

    int iSection = 0;
    int iDensityCounter1 = 0;
    int iDensityCounter2 = 0;
    int iDensityCounter3 = 0;
    int iDensityCounter4 = 0;
    int iPatternIdx = -1;
    int iRepetitionTimes = 1;
    int iShimonHeadMoveHeight = 6;
    int iSection1NodCount = 0;
    int iSection2NodCount = 0;
    int iSection3NodCount = 0;
    int iSection4NodCount = 0;
    int iBehaviorNodCount = 0;

    float fHeadNodInterval = 882.f;

    boolean bListen2NoteDensity = false;
    boolean bRhythmPatternIsDecided = false;
    boolean bListen2Drum = false;
    boolean bShimonIsPlay = false;

    Random rRandomGen = new Random();

    Timer tTimer1;    //for prothetic arm
    Timer tTimer2;    //for Shimon arm
    Timer tHeadControl;  // for Shimon Head Nodding
    Timer tBasePanControl;   //for Shimon base pan control
    Timer tMetronome;

    public ProjectStudio() {
        declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL,
                                 DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL,
                                 DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL,
                                 DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        reset();
        System.out.println("Project Studio Object is updated!!!");
    }

    public void reset() {
        iSection = 0;
        iRepetitionTimes = 1;
        iPatternIdx = -1;
        iShimonHeadMoveHeight = 6;
        iSection1NodCount = 0;
        iSection2NodCount = 0;
        iSection3NodCount = 0;
        iSection4NodCount = 0;
        iBehaviorNodCount = 0;

        bListen2NoteDensity = false;
        bRhythmPatternIsDecided = false;
        bListen2Drum = false;
        bShimonIsPlay = false;

        densityCounterClear();
        headNodCounterClear();
        setMetronome(1);

//        if (tTimer1 != null) {
//            tTimer1.cancel();
//        }
    }

    public void sectionSelector(int sec) {
        if (sec == 36 && iSection != 1) {
            iSection = 1;
            stopALL();
            timerCancel();
            outlet(0, 4);    //select midi file of section 1 for arm
            outlet(10, 0.f); //neck pan go to zero
            bListen2NoteDensity = false;
            headNodCounterClear();
            fHeadNodInterval = 800.f;
            tHeadControl = new Timer();
            tHeadControl.schedule(new headmoveSection1(), (int)fHeadNodInterval*8); //start head move
            setMetronome(iSection);
            outlet(15, 1);  //start metronome
//            startPlayRhythmPattern();   //start prothetic arm
            System.out.println("We are in section " + iSection + " and play");
        } else if (sec == 38 && iSection != 2) {
            iSection = 2;
            bListen2NoteDensity = false;
            if (!bShimonIsPlay) {
                stopShimonPlay();
            }
            headNodCounterClear();
            timerCancel();  //cancel all of the timer
            outlet(4, 0);   //prepare the midi file for shimon
            outlet(9, 0);   //stop auto base pan
            outlet(10,0.f); //neck pan go to zero
            outlet(11, 0);  //stop the arm follow human
            outlet(12, 0);  //turn off the mic
            outlet(15, 0);  //stop metronome
            fHeadNodInterval = 1000.f;
            setMetronome(iSection); //set new metronome
            System.out.println("We are in section " + iSection);
        } else if (sec == 40 && iSection != 31) {
            iSection = 31;
            bListen2Drum = true;
            if (!bShimonIsPlay) {
                stopShimonPlay();
            }
            timerCancel();
            headNodCounterClear();
            outlet(4, 1);   //prepare teh midi file for shimon
            outlet(9, 0);   //stop auto base pan
            outlet(10,0.f); //neck pan go to zero
            outlet(11,1);   //let the arm follow human
            outlet(12,1);   // turn on the mic
            outlet(15, 0);  //stop metronome
            fHeadNodInterval = 660.f;
            setMetronome(iSection); //set new metronome
            System.out.println("We are in section " + iSection);
        } else if (sec == 37 && iSection != 4) {
            iSection = 4;
            bListen2NoteDensity = false;
            if (!bShimonIsPlay) {
                stopShimonPlay();
            }
            timerCancel();
            headNodCounterClear();
            outlet(0, 5);   //prepare midi file for arm
            outlet(4, 2);   //prepare midi for shimon
            outlet(9, 0);   //stop auto base pan
            outlet(10,0.f); //neck pan go to zero
            outlet(11, 0);  //stop the arm follow human directly
            outlet(12, 0);  //turn off the mic
            outlet(3, 0);   //switch arm to the non-loop mode
            outlet(15, 0);  //stop metronome
            setMetronome(iSection); //set new metronome
            System.out.println("We are in section " + iSection);
        } else if (sec == 45 && iSection != 32) {
            iSection = 32;
            bListen2NoteDensity = true;
            if (!bShimonIsPlay) {
                stopShimonPlay();
            }
            headNodCounterClear();
//            tHeadControl.cancel();    //cancel the current execution
            outlet(0, 0);
            outlet(1, "start 1024");  //start to play the arm rhythm pattern
            outlet(3, 1);   //switch arm to the loop mode
            outlet(10,0.f); //neck pan go to zero
            outlet(11, 0);  //stop the arm follow human directly
            fHeadNodInterval = 1000.f;
            setMetronome(iSection); //set new metronome
            System.out.println("We are in section " + iSection);
        }
    }

    /*
    one to one triggering: drummer -> prothetic arm
     */
    public void followDrumTrigger() {
        if (bListen2Drum) {
            outlet(2, 1);
        }
    }

    public void noteDensity (float density) {
        if (bListen2NoteDensity) {
            if (density > 60.f && density < 100.f) {
                iDensityCounter1++;
            } else if (density > 150.f && density < 500.f) {
                iDensityCounter2++;
            }
//            } else if (density > 550.f && density < 750.f) {
//                iDensityCounter3++;
//            } else if (density > 780.f && density < 1050.f) {
//                iDensityCounter4++;
//            }
        }
    }

    public void setPatternRepetition(int rep) {
        if (rep > 0 && rep < 5) {
            iRepetitionTimes = rep;
        }
    }

    public void startPlayRhythmPattern() {
        tTimer1 = new Timer();
        tTimer1.schedule(new startPlayPattern(), 500);
    }

    public void startShimon(int i) {
        // play back midi file
        if (i == 50 && !bShimonIsPlay) {
            tTimer2 = new Timer();
            tTimer2.schedule(new ShimonStartPlay(), 0);
            if (iSection == 4) {
                startPlayRhythmPattern();
            }
            bShimonIsPlay = true;

            // head movement
            if (iSection == 2) {
                tHeadControl = new Timer();
                tBasePanControl = new Timer();
                tHeadControl.schedule(new headmoveSection2(), 500);
                tBasePanControl.schedule(new startAutoBasePan(), 500 + (int) (16 * fHeadNodInterval));
            } else if (iSection == 31) {
                tHeadControl = new Timer();
                tBasePanControl = new Timer();
                tHeadControl.schedule(new headMoveSection3(), 500);
                tBasePanControl.schedule(new startAutoBasePan(), 500 + (int) (16 * fHeadNodInterval));
            } else if (iSection == 32) {
//            tHeadControl = new Timer();4
//            tHeadControl.schedule(new headMoveSection3(), 500);
            } else if (iSection == 4) {
                tHeadControl = new Timer();
                tBasePanControl = new Timer();
                tHeadControl.schedule(new headMoveSection4(), 500);
                tBasePanControl.schedule(new startAutoBasePan(), 500 + (int) (16 * fHeadNodInterval));
            }
            if (iSection != 2) {
                tMetronome = new Timer();
                tMetronome.schedule(new startNewMetronome(), 500);
            }
        }
    }

    public void stopShimon(int i) {
        if(bShimonIsPlay && i == 48) {
            outlet(5, "stop");
            bShimonIsPlay = false;
        }
    }

    public void startArmNormal (int i) {
        if (i == 56) {
            startPlayRhythmPattern();
        }
    }

    public void bridge() {
        if (whichPattern() != -1) {
            outlet(0, whichPattern());
            densityCounterClear();
            startPlayRhythmPattern();
        } else {
            startPlayRhythmPattern();
        }
    }

    public void ShimonFinishPlay() {
        if (bShimonIsPlay) {
            bShimonIsPlay = false;
        }
    }

    public void keyboardVel(int vel) {
        if (vel < 80 && iShimonHeadMoveHeight != 6) {
            iShimonHeadMoveHeight = 6;
        } else  if (vel >=80 & vel < 100 && iShimonHeadMoveHeight != 7) {
            iShimonHeadMoveHeight = 7;
        } else if(vel >= 100 && iShimonHeadMoveHeight != 8) {
            iShimonHeadMoveHeight = 8;
        }
    }

    public void stopALL() {
        stopHeadMove();
        stopArmPlay();
        stopShimonPlay();
        outlet(15, 0);
    }

    public void stopHeadMove() {
        if (tBasePanControl != null) {
            tBasePanControl.cancel();
        }
        if (tHeadControl != null) {
            tHeadControl.cancel();
        }
    }

    public void stopArmPlay() {
        outlet(1, "stop");
    }

    public void stopShimonPlay() {
        stopShimon(48);
    }

    public void stopAllByPad(int i) {
        if (i == 41) {
            stopALL();
            System.out.println("Stop All");
        }
    }

    public void stopArmNormal(int i ) {
        if (i == 39) {
            stopArmPlay();
        }
    }

    private void timerCancel() {
        if (tTimer1 != null) {
            tTimer1.cancel();
        }
        if (tTimer2 != null) {
            tTimer2.cancel();
        }
        if (tHeadControl != null) {
            tHeadControl.cancel();
        }
        if ( tBasePanControl != null) {
            tBasePanControl.cancel();
        }
        if (tMetronome != null) {
            tMetronome.cancel();
        }
    }

    private int whichPattern() {
        int patternIdx = -1;
        if (maxValue(iDensityCounter1, iDensityCounter2, iDensityCounter3, iDensityCounter4)>=2) {
            patternIdx = iPatternIdx;
            System.out.println("pattern index is " + iPatternIdx);
        }
        return patternIdx;
    }

    private void densityCounterClear() {
        iDensityCounter1 = 0;
        iDensityCounter2 = 0;
        iDensityCounter3 = 0;
        iDensityCounter4 = 0;
    }

    //return the maximum of 4 value
    private int maxValue(int i1, int i2, int i3, int i4) {
        int max = 0;
        List<Integer> valueList = new ArrayList<Integer>(4);
        valueList.add(0, i1);
        valueList.add(1, i2);
        valueList.add(2, i3);
        valueList.add(3, i4);
        max = Collections.max(valueList);
        iPatternIdx = valueList.indexOf(max);
        return max;
    }

    private void headNodCounterClear() {
        iSection1NodCount = 0;
        iSection2NodCount = 0;
        iSection3NodCount = 0;
        iSection4NodCount = 0;
        iBehaviorNodCount = 0;
    }

    private void headDefaultPos() {
        outlet(10, 0.f);
        outlet(13, 0.f);
    }

    private void setMetronome(int secNum) {
        int bpm = 0;
        int beatNum = 0;
        if (secNum == 1) {
            bpm = 152; beatNum = 4;
        } else if (secNum == 2) {
            bpm = 120; beatNum = 4;
        } else if (secNum == 31) {
            bpm = 182; beatNum = 6;
        } else if (secNum == 32) {

        } else if (secNum == 4) {
            bpm = 130; beatNum = 6;
        }
        outlet(14, new Atom[]{ Atom.newAtom(bpm), Atom.newAtom(beatNum)});
    }

    class startPlayPattern extends TimerTask {
        public void run() {
            outlet(1, "start 1024");
        }
    }

    class ShimonStartPlay extends TimerTask {
        public void run() {
            outlet(5, "start 1024");
        }
    }

    class headmoveSection1 extends TimerTask {
        long waitTime;
        public void run() {
            if (iSection1NodCount <= 16) {
                if (iSection1NodCount < 8) {
                    outlet(10, .8f);
                } else {
                    outlet(10, -.8f);
                }
                waitTime = (int)fHeadNodInterval;
                outlet(6, fHeadNodInterval);    //HIP HOP NOD low
                iSection1NodCount++;
                tHeadControl.schedule(new headmoveSection1(), waitTime);
            } else {
                waitTime = (int)(fHeadNodInterval * 2);
                float neck = rRandomGen.nextFloat()*1.1f - 0.55f;
                outlet(10, neck);
                outlet(7, fHeadNodInterval*2);
                tHeadControl.schedule(new headmoveSection1(), waitTime);
            }
        }
    }
    class headmoveSection2 extends TimerTask {
        long waitTime;
        public void run() {
            if (iSection2NodCount <= 8) {
                if (iSection2NodCount < 4) {
                    outlet(10, 0.8f);
                } else {
                    outlet(10, -0.8f);
                }
                waitTime = (int)fHeadNodInterval;
                outlet(6, fHeadNodInterval);    //HIP HOP NOD low
                iSection2NodCount++;
                tHeadControl.schedule(new headmoveSection2(), waitTime);
            } else {
                waitTime = (int)(fHeadNodInterval * 4);
                outlet(10, 0.f);
                tHeadControl.schedule(new headNodBehavior(), waitTime);
            }
        }
    }
    class headMoveSection3 extends TimerTask {//the height of the head depends on the velocity of the keyboard
        long waitTime;
        public void run() {
            if (iSection3NodCount <= 16) {
                if (iSection3NodCount < 8) {
                    outlet(10, -.7f);
                } else {
                    outlet(10, .7f);
                }
            }
            waitTime = (int)fHeadNodInterval;
            outlet(10, 0.1f);
            outlet(iShimonHeadMoveHeight, fHeadNodInterval);
            iSection3NodCount++;
            tHeadControl.schedule(new headMoveSection3(), waitTime);
        }
    }
    class headMoveSection4 extends TimerTask {//the height of the head depends on the velocity of the keyboard
        long waitTime;
        public void run() {
            if (iSection4NodCount <= 8) {
                if (iSection4NodCount < 4) {
                    outlet(10, .7f);
                } else {
                    outlet(10, -.7f);
                }
                waitTime = (int)fHeadNodInterval;
                outlet(iShimonHeadMoveHeight, fHeadNodInterval);
                iSection4NodCount++;
                tHeadControl.schedule(new headMoveSection4(), waitTime);
            } else {
                waitTime = (int) (fHeadNodInterval * 4);
                if (tBasePanControl != null) {
                    tBasePanControl.cancel();
                }
                outlet(10, 0.f);
                tHeadControl.schedule(new headNodBehavior(), waitTime);
            }
        }
    }
    class headNodBehavior extends TimerTask {
        public void run() {
            if (rRandomGen.nextFloat()>.7f) {
                float lookAtPosition = rRandomGen.nextFloat()*1.3f - .65f;
                outlet(10, lookAtPosition);
            }
            if (iBehaviorNodCount < 10) {
                outlet(8, fHeadNodInterval);
            } else {
                if (rRandomGen.nextFloat() > .7f) {
                    float randNum = rRandomGen.nextFloat();
                    if (randNum < .5f) {
                        iShimonHeadMoveHeight = 6;
                    } else if (randNum >= .5f && randNum<.85f) {
                        iShimonHeadMoveHeight = 7;
                    } else {
                        iShimonHeadMoveHeight = 8;
                    }
                }
                outlet(iShimonHeadMoveHeight, fHeadNodInterval);
            }
            iBehaviorNodCount++;
            tHeadControl.schedule(new headNodBehavior(), (int)fHeadNodInterval);
        }
    }
    class startAutoBasePan extends TimerTask {
        public void run() {
            outlet(9, 1);   //start the auto base pan
        }
    }
    class startNewMetronome extends TimerTask {
        public void run() {
            outlet(15, 1);
        }
    }
}
