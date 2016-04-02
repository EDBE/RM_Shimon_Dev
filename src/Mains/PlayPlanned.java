package Mains;

/**
 * Created by musictechnology on 2/15/16.
 */

import java.util.ArrayList;
import java.util.List;

import InputOutput.ReadFile;
import com.cycling74.max.*;

public class PlayPlanned extends MaxObject{
    //script variables
    int[][] instructions;
    int instructionCount = 0;
    int timeUnitCount = 1;

    int[][] improvInstructions;
    int improvInstructionCount = 0;
    int iCurrentVelocity = 0;

    //outlets
    int[] ARM_COMMANDS = new int[]{0,1,2,3};


    public PlayPlanned(Atom[] args)
    {
        declareInlets(new int[]{DataTypes.ALL});
        declareOutlets(new int[]{DataTypes.ALL,DataTypes.ALL,DataTypes.ALL,DataTypes.ALL, DataTypes.ALL});
        System.out.println("PlayPlanned mxj object is updated!!!");
    }

    public void bang()
    {



    }

    public void inlet(int i)
    {


    }


    public void inlet(float f)
    {


    }

    public void list(Atom[] list)
    {


    }


    public void loadScript(String scoreFile){

//        ReadFile fileReader = new ReadFile("/Users/musictechnology/Dropbox/1_Spring_2016/7100MUSI/WeeklyDemo/3.2/liang_song/liang_song_p1_instructions.txt");
//        ReadFile fileReader = new ReadFile("/Users/musictechnology/Dropbox/1_Spring_2016/7100MUSI/WeeklyDemo/3.9/PathPlan/liang_song_p2_instructions.txt");
        ReadFile fileReader = new ReadFile(scoreFile);
        List<String> lines = new ArrayList<String>(fileReader.getLines());
        instructions = fileReader.interpretScript(lines);
        System.out.println("melody script loaded, instruction count = " + instructions.length);

    }



    public void initializeScript(){
        instructionCount = 0;
        improvInstructionCount = 0;
        timeUnitCount=1;
        outputArmCommands(0);
    }

    public void performScript(){
        if(instructionCount < instructions.length){
            outputArmCommands(instructionCount);
            instructionCount++;
            System.out.println("instruction count = "+ instructionCount);
        }
    }

    public void performScriptFrom (int counter) {
        instructionCount = counter;
        performScript();
    }

    public void performNonMidiScript(){
        if(instructionCount < instructions.length){
            if(timeUnitCount == instructions[instructionCount][6]){
                outputArmCommands(instructionCount);
                instructionCount++;
            }
        }
        timeUnitCount++;
    }

    public void playInstructionCount(int count){
        //outputArmCommands(i);
        System.out.println("playing instruction count " + count);
        instructionCount = count;
        timeUnitCount = instructions[instructionCount][6];
        int[] command = new int[3];
        for(int i=3;i>-1;i--){
            if(instructions[count][i]!=-1){
                command[2] = 0;
                if(instructions[count][i] == instructions[count][4]){
                    if (ARM_COMMANDS[i] == 0) {
                        command[2] = 45;
                    } else if (ARM_COMMANDS[i] == 1) {
                        command[2] = 37;
                    } else if (ARM_COMMANDS[i] == 2){
                        command[2] = 55;
//                    command[2] = iCurrentVelocity;
                    } else {
                        command[2] = 45;
                    }
                }
                command[0] = i;
                command[1] = instructions[count][i];
                outlet(ARM_COMMANDS[i],command);

            }
        }
    }

    private void outputArmCommands(int instructionCount){

        int[] command = new int[3]; //arm, position and velocity
        if(instructionCount>=1){
            for(int i=0;i<4;i++){
                if(instructions[instructionCount][i]!=-1){
                    command[2] = 0;
                    if(instructions[instructionCount][i] == instructions[instructionCount][4]){
                        if (ARM_COMMANDS[i] == 0) {
                            command[2] = 45;
                        } else if (ARM_COMMANDS[i] == 1) {
                            command[2] = 52;
                        } else if (ARM_COMMANDS[i] == 2){
                            command[2] = 50;
//                    command[2] = iCurrentVelocity;
                        } else {
                            command[2] = 45;
                        }
                    }

                    if(instructions[instructionCount][i] != instructions[instructionCount-1][i] || command[2] != 0){
                        command[0] = i;
                        command[1] = instructions[instructionCount][i];
                        outlet(ARM_COMMANDS[i],command);
                        if (command[2] != 0) {
                            outlet(4, command[1]);
                        }
                    }

                }
            }
        }else{
            for(int i=3;i>-1;i--){
                if(instructions[instructionCount][i]!=-1){
                    command[2] = 0;
                    if(instructions[instructionCount][i] == instructions[instructionCount][4]){
                        if (ARM_COMMANDS[i] == 0) {
                            command[2] = 45;
                        } else if (ARM_COMMANDS[i] == 1) {
                            command[2] = 37;
                        } else if (ARM_COMMANDS[i] == 2){
                            command[2] = 55;
//                    command[2] = iCurrentVelocity;
                        } else {
                            command[2] = 45;
                        }
                    }
                    command[0] = i;
                    command[1] = instructions[instructionCount][i];
                    outlet(ARM_COMMANDS[i],command);
                    if (command[2] != 0) {
                        outlet(4, command[1]);
                    }
                }
            }
        }
    }

    public void performImprovScript(){

        if(improvInstructionCount < improvInstructions.length){
            outputImprovArmCommands(improvInstructionCount);
            improvInstructionCount++;
        }
    }

    private void outputImprovArmCommands(int instructionCount){
        int[] command = new int[3]; //arm, position and velocity
        if(instructionCount>=1){
            for(int i=2;i<4;i++){
                if(improvInstructions[instructionCount][i]!=-1){
                    command[2] = 0;
                    if(improvInstructions[instructionCount][i] == improvInstructions[instructionCount][4]){
//                        command[2] = 43;
                        command[2] = iCurrentVelocity;
                    }

                    if(improvInstructions[instructionCount][i] != improvInstructions[instructionCount-1][i] || command[2] != 0){
                        command[0] = i;
                        command[1] = improvInstructions[instructionCount][i];
                        outlet(ARM_COMMANDS[i],command);
                    }

                }
            }
        }else{
            for(int i=3;i>-1;i--){
                if(improvInstructions[instructionCount][i]!=-1){
                    command[2] = 0;
                    if(improvInstructions[instructionCount][i] == improvInstructions[instructionCount][4]){
//                        command[2] = 43;
                        command[2] = iCurrentVelocity;
                    }

                    command[0] = i;
                    command[1] = improvInstructions[instructionCount][i];
                    outlet(ARM_COMMANDS[i],command);
                }
            }
        }
    }

    public void velocityControl(int vel) {
        if (vel != 0 && vel <= 80) {
            iCurrentVelocity = vel + 5;
        }
    }
}
