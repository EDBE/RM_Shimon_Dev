package Classical;

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

/**
 * Created by musictechnology on 4/1/16.
 */
public class ConductorGesture2Play extends MaxObject {

    float fTorsoZAxis = 0.f;
    float fLeftHandYAxis = 0.f;
    float fRightHandXAxis = 0.f;
    float fTorsoActiveUpperBound = 0.f;
    float fTorsoActiveLowerBound = 0.f;
    float fLeftHandUpperBound = 0.f;
    float fLeftHandLowerBound = 0.f;
    float fRightHandLowerBound = 0.f;
    float fRightHandUpperBound = 0.f;

    boolean bIsTorsoIn = false;
    boolean bIsLeftHandIn = false;
    boolean bIsRightHandIn = false;
    boolean bRobotIsWatching = false;

    long lLastTime = System.currentTimeMillis();
    /*
    Constructor: declare the input and output of the object
    2 inputs, 4 outputs
     */
    public ConductorGesture2Play() {
        /*
        2 Input:
        4 Output:
         */
        declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        lLastTime = System.currentTimeMillis();
        System.out.println("ConductorGesture2Play Object is updated!");
    }

    private void resetAll() {
        bIsTorsoIn = false;
        bIsLeftHandIn = false;
        bIsRightHandIn = false;
        bRobotIsWatching = false;
    }

    /*
    Input: the lower and upper bound of the torso
    To set the active area
     */
    public void setActiveRange(float lower, float upper) {
        if(lower < upper) {
            fTorsoActiveLowerBound = lower;
            fTorsoActiveUpperBound = upper;
        } else {
            System.out.println("Parameter error. Make sure the order of them is lower and upper");
        }
    }
    /*
    Input: the lower and upper bound of the left hand
    To set the active area
     */
    public void setLeftHandRange(float lower, float upper) {
        if(lower < upper) {
            fLeftHandLowerBound = lower;
            fLeftHandUpperBound = upper;
        } else {
            System.out.println("Parameter error. Make sure the order of them is lower and upper");
        }
    }
    /*
    Input: the lower and upper bound of the right hand
    To set the active area
     */
    public void setRightHandRange(float lower, float upper) {
        if(lower < upper) {
            fRightHandLowerBound = lower;
            fRightHandUpperBound = upper;
        } else {
            System.out.println("Parameter error. Make sure the order of them is lower and upper");
        }
    }

    /*
    Input: float value like left hand y axis data
    map the range of the value to velocity of playing
     */
    public void conductVelocity(float posData) {

    }

    /*
    Input: float value like left hand y axis data
    map the range of the value to velocity of playing
     */
    public void conductTempo(float posData) {

    }

    /*
    Input: float value like torso z axis data
    Overload the inlet function to get the float value
     */
    public void inlet(float f) {
        int inletIdx;
        inletIdx = getInlet();
        if (inletIdx == 1) {
            fTorsoZAxis = f;
        } else if (inletIdx == 2) {
            fLeftHandYAxis = f;
        } else if (inletIdx == 3) {
            fRightHandXAxis = f;
        }
        System.out.println("I am receiving data from  " + inletIdx + "th inlet");
    }

    /*
    Input: float value like fTorsoZAxis
    Swich between the condition of watching or ignoring
     */
    private void isWatching(float f) {
        if (bIsTorsoIn == true) {
            if (bIsLeftHandIn == true) {
                if (bIsRightHandIn == true) {
                    bRobotIsWatching = true;
                }
            }
        }
    }

}
