package Classical;

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Liang Tang on 4/3/16.
 * This class is used for the HUman-robot collaboration demo, that is named Morning
 * Cloud Piano Marimba Duet demo. It has several functions which controls the head
 * movement of the robot based on the real-time tempo. Further, this class is similar
 * to 'head_move_to_tempo' class in the same package, but with particular optimization
 * for the demo
 */
public class NoddingHeadWithTempo extends MaxObject {
    int iRealTimeTempo = 0;
    int iInitialBPM = 90;
    int iValidTempoRange = 20;
    int iNumOfNoteObserve = 3;
    int iNodCounter = 0;
    int iNodeType = -1;
    int iNumOfInputNote = 0;

    long lLastTime;

    float fHeadNodInterval = 0.f;

    boolean bObjectOnOff = false;
    boolean bIsListening = false;
    boolean bIsResponding = false;

    volatile List<Float> lfRealtimeTempo = new ArrayList<Float>();

    Timer tHeadTempoMoving = new Timer();

    DateFormat dfDateFormat;
    Date dDate;

    public NoddingHeadWithTempo() {
        /*
        * Input 1: control messages
        * Input 2: real-time tempo
        * Input 3:
        * Input 4:
        * Input 5:
        * ===========================
        * Output 1: object On or Off message
        * Output 2:
        * Output 3:
        * Output 4:
        * Output 5:
        */
        declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        lLastTime = System.currentTimeMillis();
        reset();
        dfDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dDate = new Date();
        System.out.println("NoddingHeadWithTempo Object is updated at " + dfDateFormat.format(dDate) + " !");
    }
    /*
    * Reset critical parameter to the default value
     */
    public void reset() {
        iNumOfNoteObserve = 3;
        iNumOfInputNote = 0;
        iNodCounter = 0;
        iNodeType = -1;

        fHeadNodInterval = 0.f;

        bIsListening = false;
        bIsResponding = false;
        bObjectOnOff = false;

        lfRealtimeTempo.clear();

        if (tHeadTempoMoving == null) {
            tHeadTempoMoving = new Timer();
        }
    }

    /*
    * Input: boolean -- objectOnAndOff
    * This object will be turned on or off at particular time indices
     */
    public void objectOnAndOff (int toggle) {
        if (toggle == 1 && bObjectOnOff == false) {
            bObjectOnOff = true;
            if (tHeadTempoMoving == null) {
                tHeadTempoMoving = new Timer();
            }
        } else if (toggle == 0 && bObjectOnOff == true) {
            bObjectOnOff = false;
            if (tHeadTempoMoving != null) {
                tHeadTempoMoving.cancel();
            }
        }
        outlet(0, bObjectOnOff);
    }

    /*
    Input: integer value -- original BPM
    Setter of iInitialBPM
     */
    public void setBPM(int bpm) {
        if (bpm > 50 && bpm < 300) {
            iInitialBPM = bpm;
        } else {
            System.out.println("Invalid BPM value");
        }
    }

    /*Input: integer -- valid tempo range
    * if the real-time tempo is in the range, it would be added to the tempo value buffer
     */
    public void setTempoRange(int range) {
        if (range > 0 && range < 40) {
            iValidTempoRange = range;
        } else {
            System.out.println("The range value either too large or too small");
        }
    }

    /*
    Input: string -- either low, mid, or high
    * setter of the iNodeType, in order to send out the message at corresponding
    * outlet index
     */
    public void setNodeType(String s) {
        if (s.equals("Low")) {
            iNodeType = 1;
        } else if (s.equals("Mid")) {
            iNodeType = 2;
        } else if (s.equals("High")) {
            iNodeType = 3;
        }
        System.out.println("Head move is " + iNodeType);
    }

    /*
    Cancel the head movement -- cancel the timer task
     */
    public void cancelHeadMove() {
        if (tHeadTempoMoving != null) {
            tHeadTempoMoving.cancel();
        }
    }

    /*
    * Input: float -- real-time tempo value
    * Listening to the real-time tempo and then output the control meesage to
    * the head of robot
     */
    public void listenToTempo(float tempo) {
        if(bObjectOnOff) {
            if(bIsListening) {
                if (iNumOfNoteObserve > iNumOfInputNote) {
                    if (tempo > (iInitialBPM - iValidTempoRange) && tempo < (iInitialBPM + iValidTempoRange)) {
                        lfRealtimeTempo.add(tempo);
                        iNumOfInputNote++;
                    }
                } else {
                    fHeadNodInterval = intervalCalculation(smoothedTempo(lfRealtimeTempo, iNumOfNoteObserve));
                    System.out.println("The interval of nodding is " + fHeadNodInterval);
                    iNumOfInputNote = 0;
                }
            }

            if (!bIsResponding) {
                // suppose start the timer task here
                tHeadTempoMoving.schedule(new HeadNod(), (long) fHeadNodInterval);
                bIsResponding = true;
            }
        }
    }

    /*
    * input integer value -- tempo value
    * Calculate the time interval between two beats
     */
    private float intervalCalculation(float tempo) {
        float interval = 60000.f / tempo;
        return interval;
    }

    /*
    * Input: arraylist of tempo, the number of values would be involved
    * Return: mean of these tempo values (float)
     */
    private float smoothedTempo(List<Float> tempoBuffer, int numberOfValue) {
        float sum = 0;
        float mean;
        if (tempoBuffer.size() < numberOfValue && tempoBuffer.size() >= 0) {
            for (Float tempo : tempoBuffer) {
                sum += tempo;
            }
            mean = sum / tempoBuffer.size();
        } else {
            for (int i = tempoBuffer.size() - 1; i > tempoBuffer.size() - numberOfValue - 1; i--) {
                sum += tempoBuffer.get(i);
            }
            mean = sum / numberOfValue;
        }
        return mean;
    }

    class HeadNod extends TimerTask {
        long lWaitTime;
        public void run() {
            if (iNodCounter < 2 * iNumOfNoteObserve) {
                if (iNodCounter == iNumOfNoteObserve) {
                    bIsListening = true;
                }
                lWaitTime = (int) fHeadNodInterval;
                outlet(iNodeType, fHeadNodInterval); //1->low, 2->mid, 3->high
                iNodCounter++;
                tHeadTempoMoving.schedule(new HeadNod(), lWaitTime);
            } else {
                tHeadTempoMoving.cancel();
                iNodCounter = 0;
                bIsResponding = false;
            }
        }
    }

}
