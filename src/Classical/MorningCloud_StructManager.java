package Classical;

import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

/**
 * Created by Liang on 3/31/16.
 */
public class MorningCloud_StructManager extends MaxObject {

    int iRealTimeTempo = 0;
    int iInitialBPM = 90;
    int iTempoRange = 20;
    int iEventNum   = 0;
    int iPathPlanSection = 1;

    long lLastTime;

    float fGlobalBeat = 0.f;

    String sScoreLabel;

    boolean bVelCtlIsWaiting = true;
    boolean bVelCtlIsResponding = false;
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
        Output 4:
         */
        declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        lLastTime = System.currentTimeMillis();
        reset();
        System.out.println("MorningCloud_StructManager Object is updated!" + bVelCtlIsWaiting + bVelCtlIsResponding);
    }

    /*
    Reset: reset all of the parameters
     */
    public void reset() {
        iRealTimeTempo = 0;
        iInitialBPM = 90;
        iTempoRange = 20;
        iEventNum   = 0;
        iPathPlanSection = 1;

        fGlobalBeat = 0.f;

        bVelCtlIsWaiting = true;
        bVelCtlIsResponding = false;
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
    Input: float numver -- real time tempo and global beat
     */
    public void inlet(float f) {
        int inletIdx;
        inletIdx = getInlet();
        if (inletIdx == 2) {
            if(f!= iRealTimeTempo) {
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
            velocityVariation();
        }
    }

    public void section(int section) {
        iPathPlanSection = section;
    }

    /*
    Input: message -- 'tracking'
     */
    private void velocityVariation() {
        // First section of path planing
        if (sScoreLabel.equals("e1")&& bVelCtlIsWaiting == true && bVelCtlIsResponding == false) {
            pathPlanVelLow();
            bVelCtlIsResponding = true;
            bVelCtlIsWaiting = false;
            System.out.println("Test1");
        } else if (sScoreLabel.equals("e8")){
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

    /*
    Output velocity level at second outlet
     */
    private void pathPlanVelLow() {
        outlet(1, "low");
    }
    /*
    Output velocity level at second outlet
     */
    private void pathPlanVelMid() {
        outlet(1, "mid");
    }
    /*
    Output velocity level at second outlet
     */
    private void pathPlanVelHigh() {
        outlet(1, "high");
    }

}
