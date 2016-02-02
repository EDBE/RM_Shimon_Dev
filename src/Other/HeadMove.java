package Other;

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by musictechnology on 11/30/15.
 */
public class HeadMove extends MaxObject {
    Timer timer3 = new Timer();
    volatile List<Float> realTimeTempoBuffer = new ArrayList<Float>();

    int lastSeveralNotes = 5;
    long lastTime;
    int nodCount = 0;
    float headNodInterval;

    /*
    Constructor: delcare the input and output of the object
    2 inputs, 3 outputs
     */
    public HeadMove() {
        /*
        input 1: receive tempo value
        input 2:
        =============================
        output 1: neck pan
        output 2: base pan
        output 3: hiphop mid
         */
        declareInlets(new int[] {DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        lastTime = System.currentTimeMillis();
        System.out.println("This object updated!");
    }

    public void tempo(float _tempo) {
        float _tempo_ = _tempo;
        if (_tempo >= 60 && _tempo <= 120) {
            _tempo_ = _tempo;
        } else if (_tempo < 60 && _tempo > 30) {
            _tempo_ = _tempo * 2;
        } else if (_tempo > 120 && _tempo < 240) {
            _tempo_ = _tempo / 2;
        } else {
            System.out.println("BPM is outof range! Slow down or Speed up.");
        }
        realTimeTempoBuffer.add(_tempo_);
        System.out.println("tempo detected: " + realTimeTempoBuffer.get(realTimeTempoBuffer.size() - 1));
        headNodInterval = intervalCalculation(smoothedTempo(realTimeTempoBuffer, lastSeveralNotes));
        startHeadMove((int)(headNodInterval / 2));
    }

    /*
    input: tempo value
    output: time interval of quarter note in million second
    */
    public float intervalCalculation(float tempo) {
        float interval = 60000 / tempo;
        return interval;
    }

    /*
    input: arraylist of tempo, the number of tempo would be calculate
    output: mean of last several tempo value (float)
     */
    public float smoothedTempo(List<Float> tempoBuffer, int numberOfValue) {
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

    public void startHeadMove(int delay) {
        timer3 = new Timer();
        timer3.schedule(new headNod(), delay);
    }

    public void stopHeadMove() {
        timer3.cancel();
        nodCount = 0;
    }


    public void lookRight() {
        outlet(0, .5f);
    }

    public void lookLeft() {
        outlet(0, -.5f);
    }

    public void lookCenter() {
        outlet(0, 0f);
    }

    class headNod extends TimerTask {
        long waitTime;

        public void run() {
            if (nodCount < 24) {
                    // look human player first
                lookLeft();
                System.out.println("I am looking left");
            } else if (nodCount >= 25 && nodCount < 50) {
                lookRight();
                System.out.println("I am looking right");
            } else {
                lookCenter();
                System.out.println("I am looking center");
            }
            nodCount++;
            System.out.println("number of nod " + nodCount);
            waitTime = (int) headNodInterval;
            outlet(2, headNodInterval);
            timer3.schedule(new headNod(), waitTime);
        }
    }
}
