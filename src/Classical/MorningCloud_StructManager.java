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

    long lLastTime;

    float fGlobalBeat = 0.f;

    String sScoreLabel;

    boolean bIsWaiting = true;
    boolean bIsResponding = false;
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
        Output 1:
        Output 2:
        Output 3:
        Output 4:
         */
        declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        lLastTime = System.currentTimeMillis();
        System.out.println("MorningCloud_StructManager Object is updated!");
    }

    /*
    Reset: reset all of the parameters
     */
    private void reset() {
        iRealTimeTempo = 0;
        iInitialBPM = 90;
        iTempoRange = 20;
        iEventNum   = 0;

        fGlobalBeat = 0.f;

        bIsWaiting = true;
        bIsResponding = false;
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
            System.out.println("Tempo range has to be positive.");
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
            System.out.println("I am getting value of " + iEventNum + " from " + inletIdx);
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
                    System.out.println("I am getting value of " + iRealTimeTempo + " from " + inletIdx);
                }
            }
        } else if (inletIdx == 4) {
            fGlobalBeat = f;
            System.out.println("I am getting value of " + fGlobalBeat + " from " + inletIdx);
        }
    }

    /*
    Input: string -- score label
     */
    public void getScoreLabel(String s) {
        int inletIdx = getInlet();
        if (inletIdx == 3) {
            sScoreLabel = s;
            System.out.println("The score label is " + sScoreLabel);
        }
    }

    /*
    Input: message -- 'tracking'
     */
    public void tracking() {
        int keyPoint = 10;
        switch() {

        }
    }

}
