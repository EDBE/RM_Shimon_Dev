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
    int iInitialBPM = 95;
    int iValidTempoRange = 20;
    int iNumOfNoteObserve = 3;
    int iNodCounter = 0;
    int iNodeType = 2;
    int iNumOfInputNote = 0;
    int iNthTimesTempo = 1;

    long lLastTime;

    float fHeadNodInterval = 0.f;

    boolean bObjectOnOff = false;
    boolean bIsListening = true;
    boolean bIsResponding = false;
    boolean bNewNodIntervalReady = false;
    boolean bAutoBasePan = false;

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
        iNodeType = 2;
        iNthTimesTempo = 1;

        fHeadNodInterval = 0.f;

        bIsListening = true;
        bIsResponding = false;
        bObjectOnOff = false;
        bNewNodIntervalReady = false;
        bAutoBasePan = false;

        if (tHeadTempoMoving == null) {
            tHeadTempoMoving = new Timer();
        }

        if (lfRealtimeTempo.isEmpty() == false) {
            lfRealtimeTempo.clear();
        }
    }

    /*
    * Input: boolean -- objectOnAndOff
    * This object will be turned on or off at particular time indices
     */
    public void objectOnAndOff (int toggle) {
        if (toggle == 1 && bObjectOnOff == false) {
            bObjectOnOff = true;
            bIsListening = true;
            bIsResponding = false;
            bNewNodIntervalReady = false;
            if (tHeadTempoMoving == null) {
                tHeadTempoMoving = new Timer();
            }
            System.out.println("HeadNodding object is ON!!!");
        } else if (toggle == 0 && bObjectOnOff == true) {
            bObjectOnOff = false;
            bIsListening = false;
            bIsResponding = false;
            iNthTimesTempo = 1;
            if (tHeadTempoMoving != null) {
                tHeadTempoMoving.cancel();
            }
            if(!(lfRealtimeTempo.isEmpty())) {
                lfRealtimeTempo.clear();
            }
            System.out.println("HeadNodding object is OFF!!!");
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
    public void setNodType(String s) {
        if (s.equals("Low") && iNodeType != 1) {
            iNodeType = 1;
        } else if (s.equals("Mid") && iNodeType != 2) {
            iNodeType = 2;
        } else if (s.equals("High") && iNodeType != 3) {
            iNodeType = 3;
        }
        System.out.println("Head move is " + iNodeType);
    }

    /*Input: boolean -- setAutoBase
    * Setter of bAutoBasePan
    */
    public void setAutoBase(int toggle) {
        if (bAutoBasePan==false && toggle == 1) {
            bAutoBasePan = true;
            outlet(4, "/autoBasePan"); // need to check for the message
        } else if (bAutoBasePan == true && toggle == 0) {
            bAutoBasePan = false;
            outlet(4, "/autoBasePan");
        }
    }

    /* Input: integer -- the ratio between beats per measure and nod per measure
    * if you want to have robot nod twice per measure in a 4/4 piece, set this value to 2
    */
    public void setNthTimes(int nthTimes) {
        if (nthTimes > 0 && nthTimes < 8 && nthTimes != iNthTimesTempo) {
            iNthTimesTempo = nthTimes;
        } else {
            System.out.println("Invalid nth times value");
        }
    }

    /*
    Cancel the head movement -- cancel the timer task
     */
    public void cancelHeadMove() {
        if (tHeadTempoMoving != null) {
            tHeadTempoMoving.cancel();
        }
//        bIsResponding = false;
    }

    /*
    * Input: float -- real-time tempo value
    * Listening to the real-time tempo and then output the control meesage to
    * the head of robot
     */
    public void listenToTempo(float tempo) {
        if(bObjectOnOff) {
            if (bIsListening) {
                if (iNumOfNoteObserve > iNumOfInputNote) {
                    if (tempo > (iInitialBPM - iValidTempoRange) && tempo < (iInitialBPM + iValidTempoRange)) {
                        lfRealtimeTempo.add(tempo);
                        iNumOfInputNote++;
                    }
                } else {
                    fHeadNodInterval = intervalCalculation(smoothedTempo(lfRealtimeTempo, iNumOfNoteObserve));
                    System.out.println("The interval of nodding is " + fHeadNodInterval);
                    iNumOfInputNote = 0;
                    bNewNodIntervalReady = true;

                }
            }

            if (bNewNodIntervalReady) {
                if (!bIsResponding) {
                    // suppose start the timer task here
                    startHeadNod((long)(fHeadNodInterval/2));
                    bIsResponding = true;
                }
            }
        }
    }

    public void startHeadNod(long delay) {
        tHeadTempoMoving = new Timer();
        tHeadTempoMoving.schedule(new HeadNod(), delay);
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
            lWaitTime = (long) (fHeadNodInterval * iNthTimesTempo);
            outlet(iNodeType, fHeadNodInterval); //1->low, 2->mid, 3->high
            iNodCounter++;
            tHeadTempoMoving.schedule(new HeadNod(), lWaitTime);
        }
    }

}
