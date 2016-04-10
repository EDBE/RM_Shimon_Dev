package ProjectStudioConcert;

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

import java.util.*;

/**
 * Created by Liang Tang on 4/9/16.
 */
public class ProjectStudio extends MaxObject{

    int iSection = 0;
    int iDensityCounter1 = 0;
    int iDensityCounter2 = 0;
    int iDensityCounter3 = 0;
    int iDensityCounter4 = 0;
    int iPatternIdx = -1;
    int iRepetitionTimes = 1;

    boolean bListen2NoteDensity = false;
    boolean bRhythmPatternIsDecided = false;
    boolean bListen2Drum = false;

    Timer tTimer1 = new Timer();

    public ProjectStudio() {
        declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        reset();
        System.out.println("Project Studio Object is updated!!!");
    }

    public void reset() {
        iSection = 0;
        iRepetitionTimes = 1;
        iPatternIdx = -1;

        bListen2NoteDensity = false;
        bRhythmPatternIsDecided = false;
        bListen2Drum = false;

        densityCounterClear();

//        if (tTimer1 != null) {
//            tTimer1.cancel();
//        }
    }

    public void sectionSelector(int sec) {
        if (sec == 36 && iSection != 1) {
            iSection = 1;
            bListen2NoteDensity = false;
        } else if (sec == 38 && iSection != 2) {
            iSection = 2;
            bListen2NoteDensity = false;
        } else if (sec == 40 && iSection != 31) {
            iSection = 31;
            bListen2Drum = true;
        } else if (sec == 37 && iSection != 4) {
            iSection = 4;
            bListen2NoteDensity = false;
        } else if (sec == 45 && iSection != 32) {
            iSection = 32;
            bListen2NoteDensity = true;
            outlet(1, "start 1024");
        }
        System.out.println("We are in section " + iSection);
    }

    /*
    one to one triggering: drummer -> prothetic arm
     */
    public void followDrumTrigger() {
        if (bListen2Drum) {
            outlet(2, 1);
        }
    }

    public void noteDensity (float density) {
        if (bListen2NoteDensity) {
            if (density > 60.f && density < 99.f) {
                iDensityCounter1++;
            } else if (density > 160.f && density < 199.f) {
                iDensityCounter2++;
            } else if (density > 260.f && density < 299.f) {
                iDensityCounter3++;
            } else if (density > 360.f && density < 399.f) {
                iDensityCounter4++;
            }
        }
    }

    public void setPatternRepetition(int rep) {
        if (rep > 0 && rep < 5) {
            iRepetitionTimes = rep;
        }
    }

    public void startPlayRhythmPattern() {
        tTimer1.schedule(new startPlayPattern(), 300);
    }

    public void bridge() {
        if (whichPattern() != -1) {
            outlet(0, whichPattern());
            densityCounterClear();
            startPlayRhythmPattern();
        } else {
            startPlayRhythmPattern();
        }
    }

    private int whichPattern() {
        int patternIdx = -1;
        if (maxValue(iDensityCounter1, iDensityCounter2, iDensityCounter3, iDensityCounter4)>=5) {
            patternIdx = iPatternIdx;
            System.out.println("pattern index is " + iPatternIdx);
        }
        return patternIdx;
    }

    private void densityCounterClear() {
        iDensityCounter1 = 0;
        iDensityCounter2 = 0;
        iDensityCounter3 = 0;
        iDensityCounter4 = 0;
    }

    //return the maximum of 4 value
    private int maxValue(int i1, int i2, int i3, int i4) {
        int max = 0;
        List<Integer> valueList = new ArrayList<Integer>(4);
        valueList.add(0, i1);
        valueList.add(1, i2);
        valueList.add(2, i3);
        valueList.add(3, i4);
        max = Collections.max(valueList);
        iPatternIdx = valueList.indexOf(max);
        return max;
    }

    class startPlayPattern extends TimerTask {
        public void run() {
            outlet(1, "start 1024");
        }
    }

}
