package Embodiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicalConstraints_ShimonLowerHalf extends Physicality {

	public PhysicalConstraints_ShimonLowerHalf(){

		super();
		
		distancesBetweenArms = new int[]{3};
		noteSize = 19;
		numberOfArms = 2;
		configurations = new HashMap<Integer,List<Integer>>(noteSize,1.0f);
		
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

		for(int arm0 =0;arm0<12;arm0++){ //only the first octave
			for(int arm1 =13;arm1<noteSize;arm1++){ //2 middle octaves

				validConfiguration = true;
				for(int i =0;i<distancesBetweenArms.length;i++){

					switch(i){
					case 0:
						distance = arm1-arm0;
						break;
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
					armLocations.add(new int[]{arm0,arm1});

					//pointers to possible arm locations
					configurations.get(arm0).add(configCount); 
					configurations.get(arm1).add(configCount);
					configCount++;

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
