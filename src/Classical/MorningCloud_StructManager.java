package Classical;

import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Liang on 3/31/16.
 */
public class MorningCloud_StructManager extends MaxObject {

    int iRealTimeTempo = 0;
    int iInitialBPM = 90;
    int iTempoRange = 20;
    int iEventNum   = 0;
    int iPathPlanSection = 1;
    int iSwitchModeMessageCounter = 0;
    int iLocalBPM = 95;

    float fGlobalBeat = 0.f;

    String sScoreLabel;
    String sCurrentRobotVelocity = "Mid";

    boolean bVelCtlIsWaiting = false;
    boolean bVelCtlIsResponding = false;
    boolean bIsPathPlaning = false;
    boolean bKinectIsOn = false;
    boolean bHeadIsFollowHand = false;
    boolean bHeadIsNormalNod = false;
    boolean bIsAutoBasePan = false;
    boolean bHeadIsSpecialMove = false;
    boolean bHeadIsFollowHead = false;
    boolean bHumanIsSolo = false;

    Timer tSwitchModeTask;
    Timer tBlinkTrigger;
    Timer tConductorGestureSwitcher;

    Random rRandGen;

    /*
    Constructor: declare the input and output of the object
    2 inputs, 4 outputs
     */
    public MorningCloud_StructManager() {
        /*
        Input 1: receive control message
        Input 2: receive event number
        Input 3: receive real time tempo
        Input 4: receive score label
        Input 5: receive global beat number
        =========================
        Output 0: Tempo
        Output 1: Velocity
        Output 2: Shimon playing mode switch
        Output 3: Communicate with normal play patch
        Output 4: Communicate with path plan playing
        Output 5: Communicate with Kinect
        Output 6: Communicate with HeadFollowHand patch
        Output 7: Turn Normal Head Gesture object ON
        Output 8: Communicate with Normal Head Gesture (velocity)
        Output 9: Communicate with Normal Head Gesture (base pan)
        Output 10:Communicate with Normal Play to start play back MIDI
        Output 11:Communicate with Score Follower to start listen to second part
        Output 12:Communicate with the nodding head patch to get default BPM
        Output 13:Blinking message
        Output 14:neck pan control
        Output 15:communicate with HeadFollowHead patch (switch On and Off)
         */
        declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL,
                                DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL,
                                DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL,
                                DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});

        System.out.println("MorningCloud_StructManager Object is updated!");
        reset();
    }

    /*
    Reset: reset all of the parameters
     */
    public void reset() {
        iEventNum   = 0;
        iSwitchModeMessageCounter = 0;
        iPathPlanSection = 1;
        iLocalBPM = 85;

        fGlobalBeat = 0.f;

        sScoreLabel = "";
        sCurrentRobotVelocity = "Mid";

        bVelCtlIsWaiting = true;
        bVelCtlIsResponding = false;
        bIsPathPlaning = true;
        bHeadIsFollowHand = false;
        bHeadIsNormalNod = false;
        bIsAutoBasePan = false;
        bHeadIsFollowHead = false;
        bHumanIsSolo = false;

        outlet(2, "/pathPlanningON 1");
    }

    /*
    Input: BPM value
    Setter of iInitialBPM
     */
    public void initialBPM(int bpm) {
        iInitialBPM = bpm;
    }

    /*
    Input: BPM range value
    Setter of iTempoRange
     */
    public void setTempoRange(int range) {
        if (range > 0) {
            iTempoRange = range;
        } else {
//            System.out.println("Tempo range has to be positive.");
        }
    }

    /*
    Input: integer number -- event number
     */
    public void inlet(int i) {
        int inletIdx;
        inletIdx = getInlet();
        if (inletIdx == 1) {
            iEventNum = i;
//            System.out.println("I am getting value of " + iEventNum + " from " + inletIdx);
        }
    }

    /*
    Input: float number -- real time tempo and global beat
     */
    public void inlet(float f) {
        int inletIdx;
        inletIdx = getInlet();
        if (inletIdx == 2) {
            if(f != iRealTimeTempo) {
                if (f < iInitialBPM + iTempoRange && f > iInitialBPM + iTempoRange) {
                    iRealTimeTempo = (int) f;
//                    System.out.println("I am getting value of " + iRealTimeTempo + " from " + inletIdx);
                }
            }
        } else if (inletIdx == 4) {
            fGlobalBeat = f;
//            System.out.println("I am getting value of " + fGlobalBeat + " from " + inletIdx);
        }
    }

    /*
    Input: string -- score label
     */
    public void getScoreLabel(String s) {
        if (!s.equals(sScoreLabel)) {
            sScoreLabel = s;
//            System.out.println("The score label is " + sScoreLabel);
            headFollowHand();   //communicate with head follows hand patch
            normalHeadNod();    //communicate with nodding head patch
            velocityVariation();//communicate with path planning patch
            bpmChange();        //set bpm for each individual section of the piece
            blink();            //robot blinks
            neckMove();         //control neck pan seperately
            headFollowHead();   //control head follow head
            // manage play mode switching
            if (sScoreLabel.equals("e68")) {
                pathPlanOff();
            }
//              else if (sScoreLabel.equals("dummy")) {//need to figure this one out
//                pathPlanOn();
//            }
        }
    }

    public void section(int section) {
        iPathPlanSection = section;
    }

    /*
    Input: message -- 'velocityVariation'
     */
    private void velocityVariation() {
        // First section of path planing
        if (iPathPlanSection == 1) {
            if (sScoreLabel.equals("e1") && bVelCtlIsWaiting == true && bVelCtlIsResponding == false) {
                pathPlanVelLow();
                bVelCtlIsResponding = true;
                bVelCtlIsWaiting = false;
            } else if (sScoreLabel.equals("e8")) {
                pathPlanVelMid();
            } else if (sScoreLabel.equals("e16")) {
                pathPlanVelHigh();
            } else if (sScoreLabel.equals("e18")) {
                pathPlanVelMid();
            } else if (sScoreLabel.equals("e104")) {
                pathPlanVelLow();
            } else if (sScoreLabel.equals("e23")) {
                pathPlanVelMid();
            } else if (sScoreLabel.equals("e30")) {
                pathPlanVelLow();
            } else if (sScoreLabel.equals("e34")) {
                pathPlanVelMid();
            } else if (sScoreLabel.equals("e36")) {
                pathPlanVelHigh();
            } else if (sScoreLabel.equals("e43")) {
                pathPlanVelMid();
            } else if (sScoreLabel.equals("e48")) {
                pathPlanVelHigh();
            } else if (sScoreLabel.equals("e51")) {
                pathPlanVelMid();
            } else if (sScoreLabel.equals("e54")) {
                pathPlanVelLow();
            } else if (sScoreLabel.equals("e61")) {
                pathPlanVelMid();
            } else if (sScoreLabel.equals("e66")) {
                pathPlanVelLow();
                bVelCtlIsResponding = false;
                bVelCtlIsWaiting = true;
            }
        } else if (iPathPlanSection == 2) {

            //Second section of path planing
            if (sScoreLabel.equals("ee1") && !bVelCtlIsResponding && bVelCtlIsWaiting) {
                pathPlanVelMid();
            } else if (sScoreLabel.equals("ee5")) {
                pathPlanVelHigh();
            } else if (sScoreLabel.equals("ee7")) {
                pathPlanVelLow();
            } else if (sScoreLabel.equals("ee26")) {
                pathPlanVelMid();
            } else if (sScoreLabel.equals("ee30")) {
                pathPlanVelHigh();
            } else if (sScoreLabel.equals("ee36")) {
                pathPlanVelMid();
            } else if (sScoreLabel.equals("ee41")) {
                pathPlanVelLow();
            } else if (sScoreLabel.equals("ee44")) {
                pathPlanVelHigh();
            } else if (sScoreLabel.equals("ee45")) {
                pathPlanVelLow();
            } else if (sScoreLabel.equals("ee46")) {
                pathPlanVelHigh();
            }
        }
    }

    /*
    Input: message -- 'switchmode'
     */
    private void pathPlanOff() {
        if (bIsPathPlaning) {
            tSwitchModeTask = new Timer();
            tSwitchModeTask.schedule(new SwitchModeOn2Off(), 2500);
        }
    }
    private void pathPlanOn() {
        if (!bIsPathPlaning) {
            tSwitchModeTask = new Timer();
            tSwitchModeTask.schedule(new SwitchModeOff2On(), 500);
        }
    }

    /*
    * Kinect switcher
    * Output 1 when user wants to have the kinect work, otherwise, output 0
     */
    public void initialKinect() {
        if (!bKinectIsOn) {
            outlet(5, 1);
        } else {
            System.out.println("Kinect is already ON!");
        }
    }
    public void stopKinect() {
        if (bKinectIsOn) {
            outlet(5, 0);
            bKinectIsOn = false;
        } else {
            System.out.println("Kinect is already OFF!");
        }
    }

    /*
    Kinect working condition
    If the string return from the Kinect-Shimon patch is identical to the target
    the Kinect is ON, otherwise Kinect is not working properly.
     */
    public void kinectCondition(int i) {
        if (i == 1) {
            System.out.println("Kinect is turning ON!!!");
            bKinectIsOn = true;
        } else {
            bKinectIsOn = false;
            System.out.println("Fails to turn Kinect ON");
        }
    }

    /*
    The transition from normal play to path planning play
    Human performer uses pedal to switch give the trigger
     */
    public void pedalOn() {
        pathPlanOn();
    }

    /*
    when receive the ending signal from Shimon solo part, starts the human solo part
    when receive the ending signal from Human solo part, ends this function
     */
    public void setbHumanIsSolo(int switcher) {
        if (!bHumanIsSolo && switcher == 1) {
            bHumanIsSolo = true;
            outlet(16, 1);
        } else if (bHumanIsSolo && switcher == 0) {
            bHumanIsSolo = false;
            outlet(16, 0);
        }
    }

    /*
    Robot head follows human's hand movement
    Swiching this function on at particular time
     */
    private void headFollowHand() {
        if (iPathPlanSection == 1) {
            if (sScoreLabel.equals("s") || sScoreLabel.equals("measure1")) {
                HeadFollowHandSwitcher(true);
            } else if (sScoreLabel.equals("e2")) {
                HeadFollowHandSwitcher(false);
            } else if (sScoreLabel.equals("measure31")) {
                HeadFollowHandSwitcher(true);
            } else if (sScoreLabel.equals("e32")) {
                HeadFollowHandSwitcher(false);
            }
        } else {
            if (sScoreLabel.equals("ee20")) {
                HeadFollowHandSwitcher(true);
            } else if (sScoreLabel.equals("ee21")) {
                HeadFollowHandSwitcher(false);
            } else if (sScoreLabel.equals("ee46")) {
                HeadFollowHandSwitcher(true);
            }
        }
    }
    /*
    Switching the head following hand function ON and OFF
     */
    private void HeadFollowHandSwitcher(boolean b) {
        if (b != bHeadIsFollowHand) {
            bHeadIsFollowHand = b;
            if(bHeadIsFollowHand == true) {
                outlet(6, 1);
            } else {
                outlet(6, 0);
            }
            System.out.println("Head follows hand is " + bHeadIsFollowHand);
        }
    }

    /*
    Robot's head follows the real-time tempo and velocity to make nodding gestures
    This cannot be used at the same time with the HeadFollowHand function
     */
    private void normalHeadNod() {
        // turn the normal head nod object ON or OFF
        if (!bHeadIsFollowHand) {
            if (iPathPlanSection == 1) {
                if (sScoreLabel.equals("e3")) {
                    normalHeadNodSwitcher(true);
                } else if (sScoreLabel.equals("e30")) {
                    normalHeadNodSwitcher(false);
                } else if (sScoreLabel.equals("e33")) {
                    normalHeadNodSwitcher(true);
                } else if (sScoreLabel.equals("e38")) {
                    normalHeadNodSwitcher(false);
                } else if (sScoreLabel.equals("e43")) {
                    normalHeadNodSwitcher(true);
                } else if (sScoreLabel.equals("e53")) {
                    normalHeadNodSwitcher(false);
                } else if (sScoreLabel.equals("e54")) {
                    normalHeadNodSwitcher(true);
                } else if (sScoreLabel.equals("e67")) {
                    normalHeadNodSwitcher(false);
                }
            } else if (iPathPlanSection == 2) {
                if (sScoreLabel.equals("ee2")) {
                    normalHeadNodSwitcher(true);
                } else if (sScoreLabel.equals("ee9")) {
                    normalHeadNodSwitcher(false);
                } else if (sScoreLabel.equals("ee22")) {
                    normalHeadNodSwitcher(true);
                } else if (sScoreLabel.equals("ee29")) {
                    normalHeadNodSwitcher(false);
                } else if (sScoreLabel.equals("ee43")) {
                    normalHeadNodSwitcher(true);
                } else if (sScoreLabel.equals("ee45")) {
                    normalHeadNodSwitcher(false);
                }
            }
        }
        // if the object is ON, then map the current velocity to the amplitude of the nod
        if (bHeadIsNormalNod) {
            outlet(8, sCurrentRobotVelocity);
        }
        // turn ON or OFF auto base pan at particular point. The auto base pan behaviour is
        // independent to the nod head essentially
        if (iPathPlanSection == 1) {
            if (sScoreLabel.equals("e8")) {
                normalHeadNodBasePan(1);
            } else if (sScoreLabel.equals("e104")) {
                normalHeadNodBasePan(0);
            } else if (sScoreLabel.equals("e21")) {
                normalHeadNodBasePan(1);
            } else if (sScoreLabel.equals("measure28")) {
                normalHeadNodBasePan(0);
            } else if (sScoreLabel.equals("e35")) {
                normalHeadNodBasePan(1);
            } else if (sScoreLabel.equals("e51")) {
                normalHeadNodBasePan(0);
            } else if (sScoreLabel.equals("e66")) {
                normalHeadNodBasePan(1);
            }
//            else if (sScoreLabel.equals("e68")) {
//                normalHeadNodBasePan(0);
//            }
        } else if (iPathPlanSection == 2) {
            if (sScoreLabel.equals("ee2")) {
                normalHeadNodBasePan(1);
            } else if (sScoreLabel.equals("ee9")) {
                normalHeadNodBasePan(0);
            } else if (sScoreLabel.equals("ee22")) {
                normalHeadNodBasePan(1);
            } else if (sScoreLabel.equals("ee29")) {
                normalHeadNodBasePan(0);
            } else if (sScoreLabel.equals("ee35")) {
                normalHeadNodBasePan(1);
            } else if (sScoreLabel.equals("ee46")) {
                normalHeadNodBasePan(0);
            }
        }
    }
    /*
    Change the bpm at particular location of the score
     */
    private void bpmChange() {
        if (sScoreLabel.equals("measure1")) {
            setBPM(95);
        } else if (sScoreLabel.equals("e21")) {
            setBPM(68);
        } else if (sScoreLabel.equals("measure32")) {
            setBPM(90);
        } else if (sScoreLabel.equals("e54")) {
            setBPM(90);
        } else if (sScoreLabel.equals("e68")) {
            setBPM(70);
        } else if (sScoreLabel.equals("ee1")) {
            setBPM(85);
        } else if (sScoreLabel.equals("ee20")) {
            setBPM(90);
        }
    }
    /*
    Switcher of the function of normal head nodding
     */
    private void normalHeadNodSwitcher(boolean b) {
        if (!bHeadIsNormalNod && b == true) {
            bHeadIsNormalNod = b;
            outlet(7, 1);
        } else if(bHeadIsNormalNod && b == false) {
            bHeadIsNormalNod = b;
            outlet(7, 0);
        }
        System.out.println("Normal head nod is " + bHeadIsNormalNod);
    }
    /*
    Turn on or off the auto base pan
     */
    private void normalHeadNodBasePan(int toggle) {
        if (!bIsAutoBasePan && toggle == 1) {
            outlet(9, "/autoBasePan 1");
            bIsAutoBasePan = true;
        } else if (bIsAutoBasePan && toggle == 0) {
            outlet(9, "/autoBasePan 0");
            bIsAutoBasePan = false;
        }
    }
    /*
    switch the function of head following head On or Off at particular points
    */
    private void headFollowHead() {
        if (iPathPlanSection == 1) {
            if (sScoreLabel.equals("e39")) {
                head2HeadSwitcher(true);
            }
//            } else if (sScoreLabel.equals("e44")) {
//                head2HeadSwitcher(false);
//            }
        }
    }
    /*
    Robot's head creats some special gestures like varied speed of head up and down,
    exaggerated blinking
     */
    private void specialHeadMove() {
        //starting at: e39,    e50; ee9,  ee30, ee46(ending of the piece)
        //ending at:   e42(43),e54; ee21, ee42,
    }

    /*
    Output velocity level at second outlet
     */
    private void pathPlanVelLow() {
        outlet(1, "low");
        sCurrentRobotVelocity = "Low";
    }
    /*
    Output velocity level at second outlet
     */
    private void pathPlanVelMid() {
        outlet(1, "mid");
        sCurrentRobotVelocity = "Mid";
    }
    /*
    Output velocity level at second outlet
     */
    private void pathPlanVelHigh() {
        outlet(1, "high");
        sCurrentRobotVelocity = "High";
    }
    /*
    For each mini section of the composition, set a default BPM
    */
    private void setBPM(int bpm) {
        if (bpm > 50 && bpm < 500 && bpm != iLocalBPM) {
            iLocalBPM = bpm;
        }
        outlet(12, iLocalBPM);
    }
    /*
    make robot blink at particular point
     */
    private void blink() {
        if (iPathPlanSection == 1) {
            if (sScoreLabel.equals("s") || sScoreLabel.equals("measure1")
                    || sScoreLabel.equals("e15") || sScoreLabel.equals("e102")
                    || sScoreLabel.equals("e31") || sScoreLabel.equals("e45")
                    || sScoreLabel.equals("e54") ) {
                blinkBig1();
            } else if (sScoreLabel.equals("e8") || sScoreLabel.equals("e10")
                    || sScoreLabel.equals("e20") || sScoreLabel.equals("e101")
                    || sScoreLabel.equals("e104") || sScoreLabel.equals("e23")
                    || sScoreLabel.equals("e30") || sScoreLabel.equals("e36")
                    || sScoreLabel.equals("e38") || sScoreLabel.equals("e51")
                    || sScoreLabel.equals("e65")) {
                blinkOnce();
            } else if (sScoreLabel.equals("")) {
                blinkBig2();
            }
        } else {
            if (sScoreLabel.equals("ee3")) {
                blinkBig2();
            }
        }
    }
    /*
    blinking: robot blink
     */
    private void blinkOnce() {
        outlet(13, "/blink");
    }
    /*
    blinking: robot blink with higher amplitude
     */
    private void blinkBig1() {
        outlet(13, "/FastBlink1");
    }
    /*
    blinking: robot blink with higher amplitude (similar idea but different outcome)
     */
    private void blinkBig2() {
        outlet(13, "/FastBlink2");
    }
    /*
    In Shimon solo part, blink will be triggered periodically in order to inform the beat
     */
    private void blinkPeriod() {
        tBlinkTrigger = new Timer();
        rRandGen = new Random();
        tBlinkTrigger.schedule(new blinkPeriodically(), 500);   //offset the arm delay
    }
    /*
    Stop the periodic blink
     */
    public void stopBlinkPeriod() {
        if (tBlinkTrigger != null) {
            tBlinkTrigger.cancel();
        }
    }
    /*
    neck pan control: slowly or quickly look to the left or right
     */
    private void neckPanCtl(String direction, float amplitude, float speed) {
        if (direction.equals("right")) {
            outlet(14, new Atom[]{Atom.newAtom(amplitude), Atom.newAtom(speed)});
        } else {
            outlet(14, new Atom[]{Atom.newAtom(-amplitude), Atom.newAtom(speed)});
        }
    }
    /*
    make robot look at the human performer using neck pan
     */
    private void lookAtMe() {
        neckPanCtl("right", .2f, 3.f);
    }
    /*
    using neck pan at particular points in the piece
     */
    private void neckMove() {
        if (sScoreLabel.equals("measure1") || sScoreLabel.equals("s")) {
            lookAtMe();
        } else if (sScoreLabel.equals("measure19")) {
            neckPanCtl("right", 0.1f, 6.f);
        }
    }
    /*
    make robot's head follow the human's head
    essentially, the necktilt follows the y axis of head position
     */
    private void head2HeadSwitcher(boolean switcher) {
        if (!bHeadIsFollowHead && switcher == true) {
            outlet(15, 1);
            bHeadIsFollowHead = true;
        } else if (bHeadIsFollowHead && switcher == false) {
            outlet(15, 0);
            bHeadIsFollowHead = false;
        }
    }


    class SwitchModeOn2Off extends TimerTask {
        long waitTime = 500;
        public void run() {
            if(bIsPathPlaning) {
                if (iSwitchModeMessageCounter < 5) {
                    outlet(2, "/pathPlanningOFF 1");

                } else {
                    bIsPathPlaning = false;
                    outlet(3, 0);//load the midi score that is numbered with 0
                }
            }
            if (iSwitchModeMessageCounter >= 5) {
                tSwitchModeTask.cancel();
                System.out.println("Path Planing OFF");
//                outlet(10, "start");      //using 'detonate' to play back midi
                outlet(10, "start 1024");   //using 'seq' object to play back midi
                blinkPeriod();
                lookAtMe(); //Shimon look at human
                tConductorGestureSwitcher = new Timer();
                tConductorGestureSwitcher.schedule(new startConductorGesture(), 500);
                System.out.println("Shimon starts to wait and watch conductor gesture!");
                iSwitchModeMessageCounter = 0;
            } else {
                tSwitchModeTask.schedule(new SwitchModeOn2Off(), waitTime);
                iSwitchModeMessageCounter++;
            }
        }
    }
    class SwitchModeOff2On extends TimerTask {
        long waitTime = 750;
        public void run() {
            if(!bIsPathPlaning) {
                if (iSwitchModeMessageCounter < 3) {
                    outlet(2, "/pathPlanningON 1");
                    outlet(4, 1);   //select the second instruction
                    outlet(11, "/scoreNum 1");    //load the second path planning score
                } else {
                    bIsPathPlaning = true;
//                    outlet(4, 1);   //select the second instruction
//                    outlet(11, "/scoreNum 1");    //load the second path planning score
                }
            }
            if (iSwitchModeMessageCounter >= 4) {
                tSwitchModeTask.cancel();
                System.out.println("Path Planing On");
                outlet(11, "/startstop start");
                System.out.println("Human-robot unison!");
                iSwitchModeMessageCounter = 0;
            } else {
//                outlet(11, "/startstop start");
                tSwitchModeTask.schedule(new SwitchModeOff2On(), waitTime);
                iSwitchModeMessageCounter++;
            }
        }
    }
    class blinkPeriodically extends TimerTask {
        public void run() {
            float randomValue = rRandGen.nextFloat();
            if (randomValue > .2f) {
                float randomValue2 = rRandGen.nextFloat();
                if (randomValue2 > .8f) {
                    blinkBig1();
                } else if (randomValue2 > .6f) {
                    blinkBig2();
                } else {
                    blinkOnce();
                }
            }
            tBlinkTrigger.schedule(new blinkPeriodically(), 3333);  //assume Shimon solo is 72 BPM
        }
    }
    class startConductorGesture extends TimerTask {
        public void run() {
            outlet(17, 1);
        }
    }
    class stopConductorGesture extends TimerTask {
        public void run() {
            outlet(17, 0);
        }
    }
}
