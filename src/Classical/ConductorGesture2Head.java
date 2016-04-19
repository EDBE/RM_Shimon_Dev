package Classical;

/**
 * Created by Liang Tang on 02/19/16.
 */

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

import java.util.*;

public class ConductorGesture2Head extends MaxObject {
    Timer tTimer1 = new Timer();
    int iNumOfPass = 8;
    int iCurrentNumOfPass = 0;
    int iNodNum = 12;
    int iNodCounter = 0;
    int iNodExtentCounter = 0;
    int iNodType = 0;
    volatile List<Long> llIntervalBuffer = new ArrayList<Long>(iNumOfPass);
    long lLastTimeStamp, lSecondLastTimeStamp = System.currentTimeMillis();
    long lMoveStartTimeStamp = 0;
    float fLastPosition, fSecondLastPosition = 0.f;

    enum eHandDirection {
        ArmLeft,
        ArmRight,
        ArmUp,
        ArmDown
    }

    eHandDirection ehCurrentDirection = eHandDirection.ArmRight;

    float fShimonHeadNodInterval = 0.f;
    boolean bIsWatching = false;
    boolean bIsValidData = false;
    boolean bIsBreath = false;
    boolean bIsMoving = false;
    boolean bIsCapturing = false;
    boolean bObjectIsInit = false;
    boolean bNormalPlay = false;

    float fUpperBoundLeftHand = 300.f;
    float fLowerBoundLeftHand = -300.f;
    float fUpperBoundRightHand = 400.f;
    float fLowerBoundRightHand = -400.f;

    float fKeyPointUpperBound = 30.f;
    float fKeyPointLowerBound = -30.f;

    Random rRandGen = new Random();

    //Constructor: 2 inlet, 6 outlet mxj object
    public ConductorGesture2Head() {
        declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL,});
        resetAll();
        System.out.println("The ConductorGesture2Head mxj object is updated!");
    }

    /*
    Input: Binary value 1 or 0
    Output: none
    Turning on or off the entire object
     */
    public void objectOnAndOff(int switcher) {
        if (switcher == 1) {
            bObjectIsInit = true;
            System.out.println("The object is activated!");
        } else if (switcher == 0) {
            bObjectIsInit = false;
            System.out.println("The object is sleeping");
        }
    }

    /*
    Input: float position data (for example: left hand y axis data)
    Output: none
    switch between the watching and not watching. If left hand is in range, start watching
    Then, starting breathing when switching from not watching to watching
    Caller: calculateDuration function
     */
    public void shimonIsWatching(float fPositionData) {
        if (bIsWatching == false && bObjectIsInit == true) {
            if (fPositionData > fLowerBoundLeftHand && fPositionData < fUpperBoundLeftHand) {
                bIsWatching = true;
                System.out.println("Shimon is watching!");
                if (!bIsBreath) {
                    startBreath();
                }
            }
        } else if (bIsWatching == true) {
            if (fPositionData < fLowerBoundLeftHand || fPositionData > fUpperBoundLeftHand) {
                bIsWatching = false;
                System.out.println("Shimon is resting!");
                if (bIsBreath) {
                    stopBreath();
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
    observe the last two values of right hand position in order to get the direction of its movement
     */
    public void distinguishDirection(float fPositionData) {
        fLastPosition = fPositionData;
        //switch right to left
        if ((int)fLastPosition < (int)fSecondLastPosition && ehCurrentDirection != eHandDirection.ArmLeft) {
            ehCurrentDirection = eHandDirection.ArmLeft;
            System.out.println("Hand direction: right -> left");
        } else if ((int)fLastPosition > (int)fSecondLastPosition && ehCurrentDirection != eHandDirection.ArmRight) {
            //switch left to right
            ehCurrentDirection = eHandDirection.ArmRight;
            System.out.println("Hand direction: left -> right");
        }
        fSecondLastPosition = fLastPosition;
    }

    /*
    Input: float position data (for example: right hand x axis data)
    Output: number of passing key point
    Caller:
     */
    public void passKeyPoint(float fPositionData) {
        if (fPositionData < fKeyPointUpperBound && fPositionData > fKeyPointLowerBound) {
            if (fPositionData > fKeyPointLowerBound && fPositionData < fKeyPointUpperBound && bIsCapturing == false) {
                lSecondLastTimeStamp = lLastTimeStamp;
                lLastTimeStamp = System.currentTimeMillis();
                bIsCapturing = true;
                iCurrentNumOfPass += 1;
                System.out.println("Counting " + iCurrentNumOfPass + "th pass!");
            }
//            if (fPositionData > (fKeyPointLowerBound + fKeyPointUpperBound) / 2 && ehCurrentDirection == eHandDirection.ArmRight) {
//                lSecondLastTimeStamp = lLastTimeStamp;
//                lLastTimeStamp = System.currentTimeMillis();
//                ehCurrentDirection = eHandDirection.ArmLeft;
//                iCurrentNumOfPass += 1;
//                System.out.println("counting!Right to Left");
//            } else if (fPositionData < (fKeyPointLowerBound + fKeyPointUpperBound) / 2 && ehCurrentDirection == eHandDirection.ArmLeft) {
//                lSecondLastTimeStamp = lLastTimeStamp;
//                lLastTimeStamp = System.currentTimeMillis();
//                ehCurrentDirection = eHandDirection.ArmRight;
//                iCurrentNumOfPass += 1;
//                System.out.println("Counting!Left to Right");
//            }
        } else if (fPositionData > fKeyPointUpperBound || fPositionData < fKeyPointLowerBound) {
            if (bIsCapturing == true) {
                bIsCapturing = false;
            }
        }
    }

    /*
    let user set the number of gesture observations
    set the iNumOfPass in this class
    By default: iNumOfPass = 6
     */
    public void setNumOfGestureObserve(int number) {
        if (number < 20 && number >= 2) {
            iNumOfPass = number;
            llIntervalBuffer.clear();
            llIntervalBuffer = new ArrayList<Long>(iNumOfPass);
            System.out.println("User is setting the number of observations to " + iNumOfPass);
        }
    }

    /*
    let user set the number of Shimon's responding gesture
    set the iNodNum in this class
    By default: iNodNum = 6
     */
    public void setNumOfShimonResponse(int number) {
        if (number < 16 && number >=2) {
            iNodNum = number;
            System.out.println("Once the tempo detected, Shimon will response with " + iNodNum + " times.");
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
            if (bIsWatching && bObjectIsInit) {
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
                    stopBreath();
                }
                //output number of pass to be used to count the number of Shimon's head movement
                outlet(4, "Shimon is going to reply the conductor by " + iNumOfPass + " times!");
                //output the time interval which will make Shimon bob his head in the same tempo
//                outlet(0, avgTimeInterval);
                fShimonHeadNodInterval = avgTimeInterval;
                bIsMoving = true;
                lMoveStartTimeStamp = System.currentTimeMillis();
                System.out.println("Tempo is detected!");
                //after 500 ms, Shimon starts to move
                startHeadMove((int) fShimonHeadNodInterval / 2);
                resetAll();
            }
        }
    }

    /*
    Append new value to the interval buffer
     */
    public void addNewValueToIntervalBuffer(long timeInterval) {
        if (timeInterval > 200 && timeInterval < 2000 && bIsWatching == true && iCurrentNumOfPass < iNumOfPass) {
            llIntervalBuffer.add(timeInterval);
        }
    }

    /*
    input: the buffer containing several values of time interval
    output: the average of these values, except the first value
     */
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
        iNodExtentCounter = 0;
//        bIsWatching = false;
        bIsValidData = false;
        bNormalPlay = false;
        lLastTimeStamp = System.currentTimeMillis();
        lSecondLastTimeStamp = System.currentTimeMillis();
    }

    private void stopBreath() {
        outlet(1, "/stopBreath");
        bIsBreath = false;
    }

    private void startBreath() {
        outlet(1, "/breathing");
        bIsBreath = true;
    }

    private int bpmConversion(float timeInterval) {
        int BPMvalue = (int) (60000 / timeInterval);
        return BPMvalue;
    }

    public void startHeadMove(int delay) {
        tTimer1 = new Timer();
        tTimer1.schedule(new headNod(), delay);
    }

    /////////////////////////////////////////////////
    /////////////////////////////////////////////////
    class headNod extends TimerTask {
        long waitTime;

        public void run() {
            if (bIsBreath) {
                stopBreath();

//                tTimer1.cancel();
            }
            if (iNodCounter < iNodNum) {
                waitTime = (long) fShimonHeadNodInterval;
                hipHopMidNod();
                iNodCounter++;
                tTimer1.schedule(new headNod(), waitTime);
                if (iNodCounter == iNodNum / 2) {
//                    outlet(2, "This set of gesutures is almost finished!");
                    if (!bNormalPlay) { //start the normal play
                        outlet(2, "This set of gestures is already completed half");
                        bNormalPlay = true;
                    }
                    //set the watching state into false in order to make the synchronization between
                    //watching state and object activation state. This is for dealing with turning off
                    //object during shimon's nodding.
                    bIsWatching = false;
                }
            }
//            } else if (iNodCounter == iNodNum ) {
////                stopNodHead();
////                iNodCounter = 0;
            else {
                waitTime = (int)(fShimonHeadNodInterval * 4);
                tTimer1.schedule(new headNodExtension(), waitTime);
            }
        }
    }

    class headNodExtension extends TimerTask {
        public void run() {
            if (rRandGen.nextFloat() > .7f) {
                float lookAt = rRandGen.nextFloat() * 1.4f - 0.7f;
                outlet(3, lookAt);
            }
            if (iNodExtentCounter < 8) {
                outlet(0, (int) fShimonHeadNodInterval);
            } else {
                if (rRandGen.nextFloat() > 0.5f) {
                    float randNum = rRandGen.nextFloat();
                    if (randNum < .4f) {
                        iNodType = 5;
                    } else if (randNum >= .4f && randNum < .85f) {
                        iNodType = 6;
                    } else {
                        iNodType = 0;
                    }
                }
                outlet(iNodType, (int) fShimonHeadNodInterval);
            }
            iNodExtentCounter++;
            tTimer1.schedule(new headNodExtension(), 2*(int)fShimonHeadNodInterval);
        }
    }

    private void hipHopMidNod() {
        outlet(0, (int)fShimonHeadNodInterval);
    }

    public void stopNodHead() {
        if (bIsMoving) {
            tTimer1.cancel();
            bIsMoving = false;
        }
        outlet(3, .0f); //neck pan go to zero position
    }
}