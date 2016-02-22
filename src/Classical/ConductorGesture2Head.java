package Classical;

/**
 * Created by Liang Tang on 02/19/16.
 */

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class ConductorGesture2Head extends MaxObject {
    Timer tTimer1                             = new Timer();
    int iNumOfPass                            = 6;
    int iCurrentNumOfPass                     = 0;
    volatile List<Long> llIntervalBuffer      = new ArrayList<Long>(iNumOfPass);
    long lLastTimeStamp, lSecondLastTimeStamp = System.currentTimeMillis();
    long lMoveStartTimeStamp                  = 0;
    float fLastPosition, fSecondLastPosition  = 0.f;

    enum eHandDirection {
        ArmLeft,
        ArmRight,
        ArmUp,
        ArmDown
    }
    eHandDirection ehCurrentDirection = eHandDirection.ArmRight;

    float fShimonHeadNodInterval              = 0.f;
    boolean bIsWatching                       = false;
    boolean bIsValidData                      = false;
    boolean bIsBreath                         = false;
    boolean bIsMoving                         = false;

    float fUpperBoundLeftHand                 = 100.f;
    float fLowerBoundLeftHand                 = -100.f;
    float fUpperBoundRightHand                = 400.f;
    float fLowerBoundRightHand                = -400.f;

    float fKeyPointUpperBound                 = 5.f;
    float fKeyPointLowerBound                 = -5.f;

    //Constructor: 2 inlet, 6 outlet mxj object
    public ConductorGesture2Head() {
        declareInlets(new int[] {DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[] {DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL,});
        resetAll();
        System.out.println("The ConductorGesture2Head mxj object is updated!");
    }

    /*
    Input: float position data (for example: left hand y axis data)
    Output: boolean-switch between the watching and not watching.
    Starting breath if left hand is in the range, Stop breath if left hand move out of range
    Caller: calculateDuration function
     */
    public void shimonIsWatching (float fPositionData) {
        if (bIsWatching == false) {
            if (fPositionData > fLowerBoundLeftHand && fPositionData < fUpperBoundLeftHand) {
                bIsWatching = true;
                if (!bIsBreath) {
                    outlet(5, "/breathing");
                    bIsBreath = true;
                }
//                lLastTimeStamp = System.currentTimeMillis();
            }
        }
        else if (bIsWatching == true) {
            if (fPositionData < fLowerBoundLeftHand || fPositionData > fUpperBoundLeftHand) {
                bIsWatching = false;
                if(bIsBreath) {
                    outlet(5, "/stopBreath");
                    bIsBreath = false;
                }
            }
        }
    }

    /*
    Input: float position data (for example: right hand x axis data)
    Output: float-valid data and assign true to the boolean of bIsValidData
    Caller: calculateDuration function
     */
    public float filterIncomingPositionData(float fPositionData) {
        float fAfterFilter = 0.f;
        if (fPositionData < fUpperBoundRightHand && fPositionData > fLowerBoundRightHand) {
            fAfterFilter = fPositionData;
            bIsValidData = true;
        } else {
            bIsValidData = false;
        }
        return fAfterFilter;
    }

    /*
    Input: float position data (for example: right hand x axis data)
    Output: number of passing key point
    Caller:
     */
    public void passKeyPoint (float fPositionData) {
        if (fPositionData < fKeyPointUpperBound && fPositionData > fKeyPointLowerBound) {
            if (fPositionData > (fKeyPointLowerBound + fKeyPointUpperBound) / 2  && ehCurrentDirection == eHandDirection.ArmRight) {
                lSecondLastTimeStamp = lLastTimeStamp;
                lLastTimeStamp = System.currentTimeMillis();
                ehCurrentDirection = eHandDirection.ArmLeft;
                iCurrentNumOfPass += 1;
                System.out.println("counting!Right to Left");
            } else if (fPositionData < (fKeyPointLowerBound + fKeyPointUpperBound) / 2 && ehCurrentDirection == eHandDirection.ArmLeft) {
                lSecondLastTimeStamp = lLastTimeStamp;
                lLastTimeStamp = System.currentTimeMillis();
                ehCurrentDirection = eHandDirection.ArmRight;
                iCurrentNumOfPass += 1;
                System.out.println("Counting!Left to Right");
            }
        }
    }

    /*
    Input: append the message with 'calculateDuration' in Max/MSP
    Output: time intervals between two conductor gestures
    Caller: Max/MSP
     */
    public void calculateDuration(float fPositionData) {
        if (System.currentTimeMillis() - lLastTimeStamp > 3000) {
            resetAll();
        }
        //Only excute the processing when Shimon is not moving
        if (!bIsMoving) {
            long duration = 0;
//            if (shimonIsWatching(fPositionData) == true) {
            if (bIsWatching) {
                float fAfterFilter;
                fAfterFilter = filterIncomingPositionData(fPositionData);
                if (bIsValidData == true) {
                    passKeyPoint(fAfterFilter);
                    duration = lLastTimeStamp - lSecondLastTimeStamp;
                    addNewValueToIntervalBuffer(duration);
                }
            }
            if (iCurrentNumOfPass == iNumOfPass) {
                float avgTimeInterval = averageInterval(llIntervalBuffer);
                if (bIsBreath) {
                    outlet(5, "/stopBreath");
                }
                //output number of pass to be used to count the number of Shimon's head movement
                outlet(4, iNumOfPass);
                //output the time interval which will make Shimon bob his head in the same tempo
                outlet(0, avgTimeInterval);
                bIsMoving = true;
                lMoveStartTimeStamp = System.currentTimeMillis();
                resetAll();
            }
        }
    }

    public void addNewValueToIntervalBuffer (long timeInterval) {
        if (timeInterval > 200 && timeInterval < 2000 && bIsWatching == true && iCurrentNumOfPass < iNumOfPass) {
            llIntervalBuffer.add(timeInterval);
        }
    }

    public float averageInterval(List<Long> buffer) {
        float sum = 0;
        float mean;
        for (int i = 1; i < buffer.size(); i++) {
            sum += buffer.get(i);
        }
        mean = sum / (buffer.size() - 1);
        return mean;
//        if (buffer.size() < numberOfValue && buffer.size() >= 0) {
//            for (int i = 1; i < buffer.size(); i++) {
//                sum += buffer.get(i);
//            }
//            mean = sum / buffer.size();
//            mean = 0;
//        } else {
//            for (int i = buffer.size()-1; i > buffer.size() - numberOfValue; i--) {
//                sum += buffer.get(i);
//            }
//            mean = sum / (numberOfValue);
//        }
//        return mean;
    }

    private void resetAll() {
        llIntervalBuffer.clear();
        ehCurrentDirection = eHandDirection.ArmRight;
        iCurrentNumOfPass = 0;
        bIsWatching = false;
        bIsValidData = false;
        lLastTimeStamp = System.currentTimeMillis();
        lSecondLastTimeStamp = System.currentTimeMillis();
    }

    private int bpmConversion(float timeInterval) {
        int BPMvalue = (int) (60000 / timeInterval);
        return BPMvalue;
    }

}