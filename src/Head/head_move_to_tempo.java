package Head;

import com.cycling74.max.MaxObject;
import com.cycling74.max.DataTypes;

import java.util.*;

/**
 * Created by Liang on 10/29/15.
 */

public class head_move_to_tempo extends MaxObject {
    Timer timer1 = new Timer();
    Random rand = new Random();
    volatile List<Float> realTimeTempoBuffer = new ArrayList<Float>();

    int scoreBPM = 88;   //set BPM by score
    int lastSeveralNotes = 5;
    int beatPosition = 1;
    long lastTime;
    volatile boolean listening = true;
    volatile boolean breath = false;
    //    volatile boolean playing = true;
    float headNodInterval;
    int behaviorNodCount = 0;
    int nodCount = 0;
    int nodType = 2;

    int countNodA = 0;

    //constructor: declare the input and output of the object
    //2 inputs, and 5 outputs
    public head_move_to_tempo() {
		/*
		Input 1: receive message
		Input 2:
		=========================
		Output 1: neck pan
		Output 2: hiphop low
		Output 3: hiphop mid
		Output 4: hiphop high
		Output 5:
		*/
        declareInlets(new int[]{DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        lastTime = System.currentTimeMillis();
    }

    /*
    input: the bpm from score
    output: bpm as reference
     */
    public void setBPM(int bpm) {
        scoreBPM = bpm;
    }

    public void beatPosition(int beat) {
        beatPosition = beat;
//        System.out.println("here is the beat position " + beatPosition);
    }

    public void startListening() {
        listening = true;
    }

    public void stopListening() {
        listening = false;
        nodCount = 0;
    }

    public void listen(float tempo) {
        if (listening) {
            if (tempo < scoreBPM + 15 && tempo > scoreBPM - 15) {
                realTimeTempoBuffer.add(tempo);
                System.out.println("tempo detected  = " + realTimeTempoBuffer.get(realTimeTempoBuffer.size() - 1));
            }
            headNodInterval = intervalCalculation(smoothedTempo(realTimeTempoBuffer, lastSeveralNotes));
            System.out.println("the interval of nod is " + headNodInterval);
//            System.out.println("beat position is " + beatPosition);
            if (beatPosition % 16 == 2) {    //update the head move task every two measures
                startHeadMove((int) (headNodInterval / 2));
            }
            if (beatPosition == 78) {
                cancelHeadMove();
                breathing();
            }
        }
    }

    public void breathing() {
        if (listening) {
            if (beatPosition == 1) {
                outlet(5, "/breathing");
                breath = true;
            }
        }
    }

    public void stopBreath() {
        if (beatPosition == 3) {
            outlet(5, "/stopBreath");
            breath = false;
        }
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
//        System.out.println("average tempo is " + mean);
        return mean;
    }

    public void startHeadMove(int delay) {
        timer1 = new Timer();
        timer1.schedule(new headNod(), delay);
    }

//    public void startHeadNodBehavior() {
////        listening = false;
//        timer1 = new Timer();
//        timer1.schedule(new headNodBehavior(), 0);
//    }

    public void cancelHeadMove() {
        listening = true;
//        playing = false;
        timer1.cancel();
        nodCount = 0;
        behaviorNodCount = 0;
    }

    public void lookRight() {
        outlet(0, 1.1f);
//        outlet(2, 835);
    }

    public void lookLeft() {
        outlet(0, -1.1f);
//        outlet(2, 835);
    }

    public void lookCenter() {
        outlet(0, 0f);
//        outlet(2, 835);
    }


    class headNod extends TimerTask {
        long waitTime;

        public void run() {

            stopBreath();
            if (nodCount <= 64) {
                if (nodCount < 32) {
                    // look human player first
                    lookLeft();
                    System.out.println("I am looking left");
                } else if (nodCount > 48 && nodCount < 64) {
                    // look center
//                    lookCenter();
                    lookRight();
                    System.out.println("I am looking right");
                } else {
                    lookCenter();
                    System.out.println("I am looking center");
                }
                nodCount++;
                System.out.println("number of nod " + nodCount);
                waitTime = (int) headNodInterval;
                outlet(1, headNodInterval);
//                outlet(0, 0);
                timer1.schedule(new headNod(), waitTime);
            } else {
                waitTime = (int) (headNodInterval * 2);
//                lookCenter();
                //outlet(0, 0);
                timer1.schedule(new headNodBehavior(), waitTime);
//                timer1.schedule(new headNodBehavior(), 0);  //This is a testing
            }
        }
    }

//    class headNodBehavior extends TimerTask {
//        public void run() {
//            //nodType = 1;
//            headNodInterval *= 4;
//            if (rand.nextFloat() > .7f) {
//                float lookAtPosition = rand.nextFloat() * 2 - 1.1f;
//                outlet(0, lookAtPosition);
//                System.out.println("I am looking at " + lookAtPosition);
//            }
//
//            if(countNodA<10){
//
//            }else{
//                    float randNum = rand.nextFloat();
//                    if (randNum < .5f) {
//                        //outlet(2,headNodInterval);
//                        nodType = 1;
//                    } else if (randNum < .85f) {
//                        //outlet(3,headNodInterval);
//                        nodType = 2;
//                    } else {
//                        nodType = 3;
//                        //outlet(4,headNodInterval);
//                    }
//
//                countNodA =0;
//            }
//
//            outlet(nodType, headNodInterval);
//            countNodA++;
//            /*
//            if (behaviorNodCount < 10) {
//                outlet(2, headNodInterval);
//                System.out.println("I am using hiphop Middle");
//            } else {
//
//                if (rand.nextFloat() > .7f) {
//
//                    float randNum = rand.nextFloat();
//                    if (randNum < .5f) {
//                        //outlet(2,headNodInterval);
//                        nodType = 1;
//                    } else if (randNum < .85f) {
//                        //outlet(3,headNodInterval);
//                        nodType = 2;
//                    } else {
//                        nodType = 3;
//                        //outlet(4,headNodInterval);
//                    }
//                }
//
//                outlet(nodType, headNodInterval);
//                System.out.println("nodType is " + nodType);
//
//            }*/
//            behaviorNodCount++;
//
//            System.out.println("number of behavior nod is " + behaviorNodCount);
//
//            timer1.schedule(new headNodBehavior(), (int) headNodInterval);
////            timer1.schedule(new headNodBehavior(), 0);  //This is a test
//        }
//    }



        class headNodBehavior extends TimerTask {
            public void run() {
                //nodType = 1;
                headNodInterval *= 1;
                if (rand.nextFloat() > .7f) {
//                    float lookAtPosition = rand.nextFloat() * 2 - 1.1f;
                    float lookAtPosition = rand.nextFloat() * 1.1f - 1.1f;
                    outlet(0, lookAtPosition);
                    System.out.println("I am looking at " + lookAtPosition);
                }

                if (behaviorNodCount < 10) {
                    outlet(2, headNodInterval);
                    System.out.println("I am using hiphop Middle");
                } else {

                    if (rand.nextFloat() > .7f) {

                        float randNum = rand.nextFloat();
                        if (randNum < .5f) {
                            //outlet(2,headNodInterval);
                            nodType = 1;
                        } else if (randNum < .85f) {
                            //outlet(3,headNodInterval);
                            nodType = 2;
                        } else {
                            nodType = 3;
                            //outlet(4,headNodInterval);
                        }
                    }
                    behaviorNodCount=0;
                    outlet(nodType, headNodInterval);
                    System.out.println("nodType is " + nodType);

                }
                behaviorNodCount++;
                System.out.println("number of behavior nod is " + behaviorNodCount);

                timer1.schedule(new headNodBehavior(), (int) headNodInterval);
//            timer1.schedule(new headNodBehavior(), 0);  //This is a test
            }
        }
    }
