package Interactive;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

public class CallAndRespond_Simple extends MaxObject{

	Timer timer1 = new Timer();
	Timer timer2 = new Timer();
	volatile List<Integer> noteBuffer = new ArrayList<Integer>(5000);
	volatile List<Long> timeStamps = new ArrayList<Long>(5000);
	long lastTime;
	volatile int timerIndex;
	volatile boolean listening = true;
	boolean hiphopOn = false;
	float headNodInterval;

	volatile int hipHopSection=0;
	volatile int currentSection=0;
	volatile int hiphopIndex=0;
	float[][] hipHopMotif1 = new float[][]{{62,62,62,62,62,62,62,62,  62,64,65,62,64,62,69  ,67,65,64},{.5f,.5f,.5f,.5f,.5f,.5f,.5f,.5f, .25f,.25f,.25f,.25f,.25f,.25f,1f,.5f,.5f,.5f}};
	float[][] hipHopMotif2 = new float[][]{{69,71,72,71,69, 69,71,72,71,69,  76,74,72, 74,71,72,},{.25f,.25f,.25f,.25f,1.f, .25f,.25f,.25f,.25f,1.f, .75f,.75f,1.5f,.25f,.25f,.5f}};
	float[][] hipHopMotif3 = new float[][]{{65,64,62,57, 65,64,62,57, 65,64,62,57, 70,69, 65,64,62,57, 65,64,62,57, 65,64,62,57, 67,65},{.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.5f,.5f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.25f,.5f,.5f}};
	float[][] hipHopMotif4 = new float[][]{{70,67,69,65,67,64,65,62}, {.5f,.5f,.5f,.5f,.5f,.5f,.5f,.5f}};

	int triggerCount=0;
	int mode =0;
	int hiphopTriggerNote=38;

	int ShimonListenPosition;
	int ShimonPlayPosition;
	int nodCount=0;

	int endNodCount=0;
	int behaviorNodCount=0;
	int nodType = 3;

	Random rand = new Random();

	public CallAndRespond_Simple(){
		declareInlets(new int[]{DataTypes.ALL,DataTypes.ALL});
		declareOutlets(new int[]{DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL});
		lastTime = System.currentTimeMillis();
	}

	public void printStates(){
		System.out.println("listening: "+ listening);
		System.out.println("hiphopNod: "+ hiphopOn);
		System.out.println("notes in buffer: " + noteBuffer.size());
	}


	public void bang(){

		int inlet = getInlet();
		if(inlet == 0){


			if(hiphopOn == false){

				playBack();

			}else{
				hipHopSection++;
				System.out.println(hipHopSection);
			}

		}
	}

	public void listen(int notePlayed){

		if(listening){
			if(hiphopOn){
				//hip hop listening
				if (notePlayed %12 == 2 && notePlayed != hiphopTriggerNote){
					noteBuffer.add(notePlayed);
					timeStamps.add(System.currentTimeMillis());

					if(noteBuffer.size()==8){
						float duration = (float)(timeStamps.get(timeStamps.size()-1) - timeStamps.get(1));
						duration = duration/6.0f;
						headNodInterval = (int)(duration *2);
						System.out.println("tempo detected = "+ headNodInterval);
						listening = false;
						startHeadNod((int)(headNodInterval/2));
					}
				}

			}else{

				//regular listening
				if(System.currentTimeMillis() - lastTime> 60){
					noteBuffer.add(notePlayed);
					timeStamps.add(System.currentTimeMillis());
				}

				if(notePlayed == hiphopTriggerNote){
					triggerHiphop();
				}
			}
		}
	}

	public void playBack(){
		listening = false;
		timer1 = new Timer();
		timerIndex=0;
		timer1.schedule(new playBackThread(),0);
	}

	public void playBackReverse(){
		listening = false;
		timer1 = new Timer();
		timerIndex=0;
		timer1.schedule(new playBackThread(),0);
	}

	public void cancelPlayBack(){
		timer1.cancel();
		timer2.cancel();
	}

	public void stopListening(){
		listening = false;
	}

	public void startListening(){
		clearBuffers();
		listening = true;
	}

	public void triggerHiphop(){
		clearBuffers();
		listening = true;
		hiphopOn = true;
	}

	public void playHipHop(int delay){
		listening = false;
		timer2 = new Timer();
		timer2.schedule(new playHipHop(),delay);
	}

	private void clearBuffers(){
		noteBuffer.clear();
		timeStamps.clear();
	}

	public void startHeadNod(int delay){
		listening = false;
		timer1 = new Timer();
		timer1.schedule(new headNod(),delay);
	}
	public void startHeadNodBehavior(){
		listening = false;
		timer1 = new Timer();
		timer1.schedule(new headNodBehavior(),0);
	}

	public void lookRight(){
		outlet(1,1.1f);
		outlet(3,1000);
	}

	public void lookCenter(){
		outlet(1,0f);
		outlet(3,1000);
	}


	class headNod extends TimerTask {

		long waitTime;
		public void run(){

			if(nodCount <=16){
				if(nodCount<8){
					outlet(1,1.1f);
				}else{
					outlet(1,-1.1f);
				}
				waitTime = (int)headNodInterval;
				outlet(2,headNodInterval);
				nodCount++;
				timer1.schedule(new headNod(),waitTime);
				System.out.println();

			}else{
				waitTime = (int)(headNodInterval * 4);
				int waitTimeToPlay = (int)(headNodInterval * 4.5) - 500;
				playHipHop(waitTimeToPlay);
				outlet(1,0);
				timer1.schedule(new headNodBehavior(),waitTime);
			}


		}
	}


	class headNodBehavior extends TimerTask {

		public void run(){


			if(rand.nextFloat()>.7f){
				float lookAtPosition = rand.nextFloat()*2 - 1.1f;
				outlet(1,lookAtPosition);
			}

			if(behaviorNodCount<10){
				outlet(3,headNodInterval);
			}else{

				if(rand.nextFloat()>.7f){

					float randNum = rand.nextFloat();
					if(randNum < .5f){
						//outlet(2,headNodInterval);
						nodType = 2;
					}else if(randNum >=.5f && randNum<.85f){
						//outlet(3,headNodInterval);
						nodType = 3;
					}else{
						nodType=3;
						//outlet(4,headNodInterval);
					}
				}

				outlet(nodType,headNodInterval);

			}
			behaviorNodCount++;


			timer1.schedule(new headNodBehavior(),(int)headNodInterval);

		}

	}

	class playBackThread extends TimerTask {
		long waitTime;
		public void run() {
			// do stuff here
			if(timerIndex == 0){
				lookCenter();
			}

			if(timerIndex <noteBuffer.size()){
				if(rand.nextFloat()<0.87f){
					outlet(0,noteBuffer.get(timerIndex));
				}else{
					outlet(0,noteBuffer.get(timerIndex)+3);
				}
			}

			if(timerIndex <noteBuffer.size()-1){
				waitTime = timeStamps.get(timerIndex+1) - timeStamps.get(timerIndex);
				timer1.schedule(new playBackThread(),waitTime);

			}else{
				listening = true;
				lookRight();
				clearBuffers();
			}
			timerIndex++;
		}
	}

	class playHipHop extends TimerTask{

		long waitTime;
		public void run(){

			switch(currentSection){
				case 0:
					outlet(0,hipHopMotif1[0][hiphopIndex]);
					waitTime = (int)(headNodInterval * hipHopMotif1[1][hiphopIndex]);
					hiphopIndex++;
					if(hiphopIndex >= hipHopMotif1[0].length){
						checkSection();
					}

					break;
				case 1:
					outlet(0,hipHopMotif2[0][hiphopIndex]);
					waitTime = (int)(headNodInterval * hipHopMotif2[1][hiphopIndex]);
					hiphopIndex++;
					if(hiphopIndex >= hipHopMotif2[0].length){
						checkSection();
					}
					break;
				case 2:
					outlet(0,hipHopMotif3[0][hiphopIndex]);
					waitTime = (int)(headNodInterval * hipHopMotif3[1][hiphopIndex]);
					hiphopIndex++;
					if(hiphopIndex >= hipHopMotif3[0].length){
						checkSection();
					}
					break;
				case 3:
					outlet(0,hipHopMotif4[0][hiphopIndex]);
					waitTime = (int)(headNodInterval * hipHopMotif4[1][hiphopIndex]);
					hiphopIndex++;
					if(hiphopIndex >= hipHopMotif4[0].length){
						checkSection();
					}
					break;
				case 4:
					outlet(0,hipHopMotif1[0][hiphopIndex]);
					waitTime = (int)(headNodInterval * hipHopMotif1[1][hiphopIndex]);
					hiphopIndex++;
					if(hiphopIndex >= hipHopMotif1[0].length){
						checkSection();
					}
					break;

				case 5:
					endNodCount++;
					waitTime = (int)headNodInterval;
					if(endNodCount>=8){
						currentSection =6;
					}
					break;
				default:
					break;

			}


			if(currentSection<6){

				timer1.schedule(new playHipHop(),waitTime);
			}else{
				cancelPlayBack();
			}
		}

		private void checkSection(){
			hiphopIndex = 0;
			currentSection = hipHopSection;
		}
	}
}
