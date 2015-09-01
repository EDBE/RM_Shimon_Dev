package Embodiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicalConstraints_ShimonReduced extends Physicality {

	public PhysicalConstraints_ShimonReduced(){

		super();
		
		numberOfArms = 4;
		distancesBetweenArms = new int[]{4,8,4};
		noteSize = 40;
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
			for(int arm1 =8;arm1<26;arm1++){ //2 middle octaves
				for(int arm2 =17;arm2<36;arm2++){
					for(int arm3 =32;arm3<40;arm3++){

						validConfiguration = true;
						for(int i =0;i<distancesBetweenArms.length;i++){

							switch(i){
							case 0:
								distance = arm1-arm0;
								break;
							case 1:
								distance = arm2-arm1;
								break;
							case 2:
								distance = arm3-arm2;
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
							armLocations.add(new int[]{arm0,arm1,arm2,arm3});

							//pointers to possible arm locations
							configurations.get(arm0).add(configCount); 
							configurations.get(arm1).add(configCount);
							configurations.get(arm2).add(configCount);
							configurations.get(arm3).add(configCount);
							configCount++;

						}

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
