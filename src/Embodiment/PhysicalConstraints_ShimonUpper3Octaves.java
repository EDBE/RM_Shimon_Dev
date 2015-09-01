package Embodiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicalConstraints_ShimonUpper3Octaves extends Physicality {

	public PhysicalConstraints_ShimonUpper3Octaves(){

		super();
		
		distancesBetweenArms = new int[]{8,4};
		noteSize = 44;
		numberOfArms = 3;
		lowestNote = 13;
		configurations = new HashMap<Integer,List<Integer>>(noteSize-lowestNote,1.0f);
		
		findPossibleConfigurations();

	}

	@Override
	void findPossibleConfigurations(){


		boolean validConfiguration= false;
		int distance=0;
		for(int i=0;i<noteSize;i++){
			configurations.put(i,new ArrayList<Integer>());
		}
		int configCount =0;

		for(int arm0 =13;arm0<25;arm0++){
			for(int arm1=22;arm1<38;arm1++){
				for(int arm2=33;arm2<noteSize;arm2++){

					validConfiguration = true;
					for(int i =0;i<distancesBetweenArms.length;i++){

						switch(i){
						case 0:
							distance = arm1-arm0;
							break;
						case 1:
							distance = arm2-arm1;
						default:
							break;
						}

						if(distance < distancesBetweenArms[i]){
							validConfiguration = false;
							break;
						}
					}

					if(validConfiguration == true){
						//all possible arm configurations described each arms location
						armLocations.add(new int[]{arm0,arm1,arm2});

						//pointers to possible arm locations
						configurations.get(arm0).add(configCount); 
						configurations.get(arm1).add(configCount);
						configurations.get(arm2).add(configCount);
						configCount++;

					}
				}

			}
		}

		int totalSize=0;
		int max=-1;
		for(int i=0;i<noteSize;i++){
			if(configurations.get(i).size()>max){
				max = configurations.get(i).size();
			}
			totalSize +=configurations.get(i).size();
		}


		System.out.println("max " + max);
		System.out.println("configuration state space  = " + armLocations.size() +  "    "+ totalSize);		
		createStateMap(max);

	}

}
