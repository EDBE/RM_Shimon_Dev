package Embodiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Physicality {

	public int numberOfArms;
	public List<int[]> armLocations = new ArrayList<int[]>();
	int[] distancesBetweenArms;
	public int noteSize;
	public Map<Integer,List<Integer>> configurations;
	public int[][] stateDict;
	public Map<Integer,int[]> stateMap;
	public float[][] transitionScores;
	public int nStates;
	public Map<int[],Integer> armStateMap;
	public int lowestNote =0;


	public Physicality(){

	}
	
	abstract void findPossibleConfigurations();

	void createStateMap(int max){		
		//creates list of ALL possible states including configurations, and notes
		int count=0;
		stateDict = new int[noteSize][max];

		stateMap = new HashMap<Integer,int[]>(max,1.0f);
		int dist, maxDistState=-1;
		int maxDist=-1;
		for(int j=0;j<noteSize;j++){
			for(int z=0;z<configurations.get(j).size();z++){
				dist=0;
				for(int strikingArm = 0;strikingArm<numberOfArms;strikingArm++){
					if(j == armLocations.get(configurations.get(j).get(z))[strikingArm]){
						stateMap.put(count,new int[]{j,z,strikingArm});
					}
					if(strikingArm<(numberOfArms-1)){
						dist+=(armLocations.get(configurations.get(j).get(z))[strikingArm+1] - armLocations.get(configurations.get(j).get(z))[strikingArm]);
					}else{
						if(dist>maxDist){
							maxDist = dist;
							maxDistState = count;
						}
					}
				}			

				stateDict[j][z] = count;
				count++;
			}
		}
		System.out.println("max distance state = " + maxDistState + "," + stateMap.get(maxDistState)[0] + ", "+ stateMap.get(maxDistState)[1] +", " + maxDist);
		nStates=count;
		System.out.println(count);

		//transition scores
		transitionScores = new float[armLocations.size()][armLocations.size()];
		float maxi=-1;
		for(int i=0;i<armLocations.size();i++){
			for(int j=0;j<armLocations.size();j++){
				transitionScores[i][j] = physicalConstraintCalculation(armLocations.get(i),armLocations.get(j));
				if(transitionScores[i][j] > maxi){
					maxi = transitionScores[i][j];
				}
			}
		}
		System.out.println("max distance value = "+maxi);

		/*armStateMap = new HashMap<int[],Integer>(armLocations.size(),1.0f);
		for(int i=0;i<armLocations.size();i++){
			armStateMap.put(new int[]{armLocations.get(i)[0],armLocations.get(i)[1],armLocations.get(i)[2],armLocations.get(i)[3]},i);
		}*/

		System.out.println("complete state space  = " + count);
	}

	float physicalConstraintCalculation(int[] config1, int[] config2){

		float distance =0.0f;
		for(int i=0;i<config1.length;i++){
			distance+= Math.abs(config1[i] - config2[i]);
		}
		distance = (float) Math.max(.7, distance); // so it's not zero and not too big
		distance = distance/192; //192 is 48*4 which comes from 48 possible locs for each of the 4 arms
		distance = 1f/distance;
		return distance*0.05f;
		//return 0;
	}


}
