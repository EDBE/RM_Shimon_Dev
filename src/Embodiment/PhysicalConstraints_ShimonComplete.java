package Embodiment;

/**
 * Created by musictechnology on 9/1/15.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicalConstraints_ShimonComplete extends Physicality {

    public PhysicalConstraints_ShimonComplete(){

        super();

        numberOfArms = 4;
        distancesBetweenArms = new int[]{4,6,4};
        noteSize = 48;
        configurations = new HashMap<Integer,List<Integer>>(noteSize,1.0f);

        findPossibleConfigurations();

    }

    @Override
    void findPossibleConfigurations(){

        boolean validConfiguration= false;
        int distance=0;
        numberOfArms = 4;
        for(int i=0;i<noteSize;i++){
            configurations.put(i,new ArrayList<Integer>());
        }
        int configCount =0;

        for(int arm0 =0;arm0<noteSize;arm0++){
            for(int arm1 =0;arm1<noteSize;arm1++){
                for(int arm2 =0;arm2<noteSize;arm2++){
                    for(int arm3 =0;arm3<noteSize;arm3++){

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
