package Classical;

import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

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

    long lLastTime;

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

    Timer tSwitchModeTask;

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
        Output 1: Tempo
        Output 2: Velocity
        Output 3: gesture section?
        Output 4:n
         */
        declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        lLastTime = System.currentTimeMillis();

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
            System.out.println("The score label is " + sScoreLabel);
            headFollowHand();   //communicate with head follows hand patch
            normalHeadNod();    //communicate with nodding head patch
            velocityVariation();//communicate with path planning patch
            bpmChange();        //set bpm for each individual section of the piece
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
            tSwitchModeTask.schedule(new SwitchModeOff2On(), 1000);
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
    Robot head follows human's hand movement
    Swiching this function on at particular time
     */
    private void headFollowHand() {
        if (iPathPlanSection == 1) {
            if (sScoreLabel.equals("s") || sScoreLabel.equals("measure1")) {
                HeadFollowHandSwitcher(true);
            } else if (sScoreLabel.equals("e3")) {
                HeadFollowHandSwitcher(false);
            } else if (sScoreLabel.equals("measure31")) {
                HeadFollowHandSwitcher(true);
            } else if (sScoreLabel.equals("e34")) {
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
                if (sScoreLabel.equals("e5")) {
                    normalHeadNodSwitcher(true);
                } else if (sScoreLabel.equals("e30")) {
                    normalHeadNodSwitcher(false);
                } else if (sScoreLabel.equals("e35")) {
                    normalHeadNodSwitcher(true);
                } else if (sScoreLabel.equals("e38")) {
                    normalHeadNodSwitcher(false);
                } else if (sScoreLabel.equals("e43")) {
                    normalHeadNodSwitcher(true);
                } else if (sScoreLabel.equals("e50")) {
                    normalHeadNodSwitcher(false);
                } else if (sScoreLabel.equals("e54")) {
                    normalHeadNodSwitcher(true);
                } else if (sScoreLabel.equals("e66")) {
                    normalHeadNodSwitcher(false);
                } else if (sScoreLabel.equals("e68")) {
                    normalHeadNodSwitcher(true);
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
            } else if (sScoreLabel.equals("e68")) {
                normalHeadNodBasePan(0);
            }
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
                System.out.println("Shimon starts to solo");
                iSwitchModeMessageCounter = 0;
            } else {
                tSwitchModeTask.schedule(new SwitchModeOn2Off(), waitTime);
                iSwitchModeMessageCounter++;
            }
        }
    }
    class SwitchModeOff2On extends TimerTask {
        long waitTime = 500;
        public void run() {
            if(!bIsPathPlaning) {
                if (iSwitchModeMessageCounter < 5) {
                    outlet(2, "/pathPlanningON 1");
                } else {
                    bIsPathPlaning = true;
                    outlet(4, 1);   //select the second instruction
                    outlet(11, "/scoreNum 1");    //load the second path planning score
                }
            }
            if (iSwitchModeMessageCounter >= 5) {
                tSwitchModeTask.cancel();
                System.out.println("Path Planing On");
                outlet(11, "/startstop start");
                System.out.println("Human-robot unison!");
                iSwitchModeMessageCounter = 0;
            } else {
                tSwitchModeTask.schedule(new SwitchModeOff2On(), waitTime);
                iSwitchModeMessageCounter++;
            }
        }
    }
}
