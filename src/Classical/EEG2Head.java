package Classical;

/**
 * Created by Liang Tang on 2/23/16.
 */
import java.util.*;

import com.cycling74.max.Atom;
import com.cycling74.max.MaxObject;
import com.cycling74.max.DataTypes;

public class EEG2Head extends MaxObject{
    //member variables
    int iBPM                = 0;
    int iHeadNodInterval    = 0;
    int iHeadNodCounter     = 0;
    int iHeadRotateCounter  = 0;
    float f2ndDeriValue     = 0.f;
    float f1stDeriValue     = 0.f;
    float f1stLowerBound    = -700.f;
    float f1stUpperBound    = 700.f;
    float f2ndLowerBound    = -700.f;
    float f2ndUpperBound    = 700.f;
    float f1stKeyPoint      = 220.f;
    float f2ndKeyPoint      = 130.f;
    float fBasePanAmp       = 0.3f;
    float fBasePanVel       = iHeadNodInterval;
    float fNeckPanAmp       = 0.3f;
    float fNeckPanVel       = iHeadNodInterval * .75f;

    boolean bIsMoving       = false;
    boolean bIsBreath       = false;
    boolean bIsNodding      = false;

    Timer tTimer1           = new Timer();
    Timer tTimer2           = new Timer();

    Random rRandomGenerator = new Random();

    //Constructor: 2 inlets, 4 outlets mxj object
    public EEG2Head() {
        declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL,});
        resetAll();
        System.out.println("The EEG2Head mxj object is updated!");
    }

    public void resetAll() {
        bIsMoving        = false;
        iBPM             = 0;
        iHeadNodCounter  = 0;
        iHeadNodInterval = 0;
        f1stDeriValue    = 0;
        f2ndDeriValue    = 0;
        bIsBreath        = false;
        bIsNodding       = false;
        iHeadRotateCounter = 0;
    }

    public void setBPM(int bpmValue) {
        if (bpmValue > 40 && bpmValue < 200) {
            iBPM = bpmValue;
            BPM2Interval(bpmValue);
            System.out.println("The neck tilt interval has been set to " + iHeadNodInterval);
        } else {
            System.out.println("The BPM should be in the range of (40, 200)");
        }
    }

    private int BPM2Interval (int bpm) {
        int interval = (int) 60000 / bpm;
        iHeadNodInterval = interval;
        return interval;
    }

    public void set1stKeyPoint(float keyPoint) {
        if (Math.abs(keyPoint) < f1stUpperBound) {
            System.out.println("key point for 1st derivative is set!");
            f1stKeyPoint = Math.abs(keyPoint);
        } else {
            System.out.println("key point is out of boundary");
        }
    }

    public void set2ndKeyPoint(float keyPoint) {
        if (Math.abs(keyPoint) < f2ndUpperBound) {
            System.out.println("key point for 2nd derivative is set!");
            f2ndKeyPoint = Math.abs(keyPoint);
        } else {
            System.out.println("key point is out of boundary");
        }
    }

    public void start() {
        if (bIsMoving == false && iBPM != 0) {
            System.out.println("Shimon is moving!");
            if (!bIsBreath) {
                outlet(5, "/breathing");
                bIsBreath = true;
                tTimer1 = new Timer();
                tTimer2 = new Timer();
            }
            bIsMoving = true;
        } else if (iBPM == 0) {
            System.out.println("User should set BPM before starting!");
        }
    }

    public void stop() {
        if (bIsMoving == true) {
            tTimer1.cancel();
            tTimer2.cancel();
            if (bIsBreath) {
                outlet(5, "/stopBreath");
            }
            resetAll();
            System.out.println("Shimon is stop!!!");
        }
    }

    /*
    Using neck tilt to follow the beat of the piece
     */
    public void startHeadNod(int delay) {
        tTimer1.schedule(new headNod(), delay);
    }

    public void startHeadGesture(int delay) {
        tTimer2.schedule(new headGesture(), delay);
    }

    /*
    Taking the first derivative of teh original signal in, compare the value with multiple ranges.
    If the value matches the range, change the amplitude of neckpan and basepan.
    1, Value from low -> high: amplitude increase
    2, Value from high -> low: amplitude decrease
    adding some random factors to make the a few more variations
    */
    public void firstDerMapping(float firstDer) {
        if (bIsMoving) {
            if (firstDer > f1stLowerBound && firstDer < f1stUpperBound) {
                if (Math.abs(firstDer) > f1stKeyPoint) {
                    if (Math.abs(f1stDeriValue) < f1stKeyPoint ) {
                        // increase the base pan ampitude, or increase the neck pan amplitude or both
                        float randomValue = rRandomGenerator.nextFloat();
                        if (randomValue > 0.2f) {
                            fNeckPanAmp = 0.8f;
                            if (randomValue > 0.5f) {
                                fBasePanAmp = 0.6f;
                                if (randomValue > 0.7) {
                                    fNeckPanAmp = 1.1f;
                                    fBasePanAmp = 0.7f;
                                }
                                if (!bIsNodding) {
                                    //Robot starts to move the head when the data coming in
                                    //before the data come in, robot only breath
                                    startHeadNod(iHeadNodInterval);
                                    bIsNodding = true;
                                }
                            }
                        }
                    }
                } else if (Math.abs(firstDer) < f1stDeriValue) {
                    if (Math.abs(f1stDeriValue) > f1stKeyPoint) {
                        // decrease the base pan amplitude, and decrease the neck pan amplitude
                        fNeckPanAmp = 0.4f;
                        fBasePanAmp = 0.2f;
                    }
                }
                f1stDeriValue = firstDer;

                if (bIsBreath && bIsMoving) {
                    outlet(5, "/stopBreath");
                    bIsBreath = false;
                }
            }
        }
    }
    /*
    Taking the second derivative of the original signal in, compare the value with multiple range.
    If the value matches the range, change the velocity of neckpan and basepan.
    1, Value from low -> high: velocity increase
    2, Value from high -> low: velocity decrease
    adding some random factors to make the a few more variations
     */
    public void secondDerMapping(float secondDer) {
        if (bIsMoving) {
            if (secondDer > f2ndLowerBound && secondDer < f2ndUpperBound) {
                float randomValue = rRandomGenerator.nextFloat();
                if (Math.abs(secondDer) > f2ndKeyPoint && Math.abs(f2ndDeriValue) < f2ndKeyPoint) {
                 // increase the base pan velocity, or increase the neck pan velocity or both
                    if (randomValue > 0.2f) {
                        fNeckPanVel = 0.6f * iHeadNodInterval;
                        if (randomValue > 0.5f) {
                            fBasePanVel = 0.8f * iHeadNodInterval;
                            if (randomValue > 0.7f) {
                                fNeckPanVel = 0.5f * iHeadNodInterval;
                                fBasePanVel = 0.75f * iHeadNodInterval;
                            }
                        }
                    }

                } else if (Math.abs(secondDer) < f2ndKeyPoint && Math.abs(f2ndDeriValue) > f2ndKeyPoint) {
                    if (randomValue > 0.2f) {
                        fNeckPanVel = iHeadNodInterval;
                        if (randomValue > 0.5) {
                            fBasePanVel = 1.15f * iHeadNodInterval;
                            if (randomValue > 0.8f) {
                                fNeckPanVel = 1.1f * iHeadNodInterval;
                                fBasePanVel = 1.25f * iHeadNodInterval;
                            }
                        }
                    }
                }
                f2ndDeriValue = secondDer;
                if(iHeadNodCounter >= 4) {
                    if(!bIsBreath && bIsMoving && bIsNodding) {
                        startHeadGesture(4 * iHeadNodInterval);
                        iHeadNodCounter = 0;
//                        System.out.println("the neckpan velocity is " + fNeckPanVel + " and the basepan velocity is " + fBasePanVel);
                    }
                }
            }
        }
    }

    class headNod extends TimerTask {
        long waitTime;
        public void run() {
            waitTime = 2*iHeadNodInterval;
            outlet(3, iHeadNodInterval);
            iHeadNodCounter++;
            tTimer1.schedule(new headNod(), waitTime);
        }
    }

    class headGesture extends TimerTask {

        float [] afNeckPan2 = new float[2];
        float [] afBasePan2 = new float[2];

        long waitTime;

        public void run() {
//            System.out.println("headGesutre timer task is being called.");
            // switch between the positive and negtive value of amplitude
            if (iHeadRotateCounter % 2 == 0) {
                afBasePan2[0] = fBasePanAmp;
                afBasePan2[1] = fBasePanVel;
                afNeckPan2[0] = fNeckPanAmp;
                afNeckPan2[1] = fNeckPanVel;
            } else {
                afBasePan2[0] = -fBasePanAmp;
                afBasePan2[1] = fBasePanVel;
                afNeckPan2[0] = -fNeckPanAmp;
                afNeckPan2[1] = fNeckPanVel;
            }
            iHeadRotateCounter++;
            outlet(2, afNeckPan2);
            outlet(1, afBasePan2);

            waitTime = 4 * iHeadNodInterval;
            if (iHeadRotateCounter % 5 == 0) {
                tTimer2.schedule(new headGesture(), waitTime);
            }
        }

    }
}
