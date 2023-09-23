package tomasulo;
import java.io.*;
import java.util.Scanner;
import java.util.*;

public class Algo {
    //static int addLatency=2;
//static int multLatency=10;
//static int divideLatency=40;
    static Vector <ReservationStation>ReservationStationsToBeStored=new Vector<>();
    static Vector <Vector<Integer>>RegisterFilesToBeStored=new Vector<>();
    static Vector <Vector<String>>RATsToBeStored=new Vector<>();

    static Vector<LoadBuffer> loadBuffers = new Vector<>();
    static Vector<StoreBuffer> storeBuffers = new Vector<>();

    static double []memory=new double[1000];
    static Vector<double[]> memoryVersions = new Vector<>();
    static int[] isCycle;
    static int[] exCycle;
    static int[] writeCycle;

    public static void main(String[] args) throws IOException {
        String inputFile = "cliTomasulo.txt";
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        String line = null;
        Vector<String> inputInstructions = new Vector<>();
        while ((line = br.readLine()) != null)
        {
            inputInstructions.add(line);
        }
        runAlgorithm(inputInstructions,2,10,40,3,2,2);
    }

    public static void runAlgorithm(Vector<String> inputInstructions, int addLatency,int multLatency, int divideLatency,int subLatency,int loadLatency,int storeLatency)
    {

        ReservationStationsToBeStored=new Vector<>();
        RegisterFilesToBeStored=new Vector<>();
        RATsToBeStored=new Vector<>();

        loadBuffers = new Vector<>();
        storeBuffers = new Vector<>();

        memory=new double[1000];
        memoryVersions = new Vector<>();

        memory[0]=3;
        memory[1]=4;
        memory[2]=6;
        memory[3]=21;
        memory[4]=3;
        memory[5]=4;
        memory[6]=9;
        memory[7]=0;

        Printing print = new Printing();
        //This holds the tags or pointers to the RegisterFile(RAT Table)
        Vector<String> RAT = new Vector<String>(32);
        //This array holds all the RegisterFile values
        Vector<Double> RegisterFile = new Vector<Double>(32);
        //Instructions vector
        Vector<Instruction> instructions = new Vector<Instruction>(10);
        //Cli text file for user to input data
        //instruction parameters
        int opCode, destOp, sourceOp1, sourceOp2 = 0;
        String inputFile = "cliTomasulo.txt";
        //Cycles Number
        int numberOfCycles = 0;
        //instructions Number
        int numberOfInstructions = 0;

        //Variable linking RS to an instruction
        int RstoInstructionMatcher = 0;

        LoadBuffer l1=new LoadBuffer(0,0);
        LoadBuffer l2=new LoadBuffer(0,0);
        LoadBuffer l3=new LoadBuffer(0,0);
        LoadBuffer l4=new LoadBuffer(0,0);
        LoadBuffer l5=new LoadBuffer(0,0);
        StoreBuffer s1=new StoreBuffer(0,0,"   ","   ");
        StoreBuffer s2=new StoreBuffer(0,0,"   ","   ");
        StoreBuffer s3=new StoreBuffer(0,0,"   ","   ");
        StoreBuffer s4=new StoreBuffer(0,0,"   ","   ");
        StoreBuffer s5=new StoreBuffer(0,0,"   ","   ");






        //This is a Try-Catch block that handles the file not found exception
        //            Scanner scanner = new Scanner(new File(inputFile));
        numberOfInstructions = inputInstructions.size();
        //Here we give RstoInstructionMatcher a unique value to match rs with instructions
        for(int i = 0; i < numberOfInstructions; i++){
            RstoInstructionMatcher = i+1;
            String[] splited = inputInstructions.get(i).split(" ");
            //System.out.println(inputInstructions.get(i));
            //System.out.println(Arrays.toString(splited));
            opCode = Integer.parseInt(splited[0]);

            if(opCode==4){
                sourceOp2=Integer.parseInt(splited[2]);
            }
            else {
                if(opCode!=5)
                    sourceOp2 = Integer.parseInt(splited[3]);
            }
            if(opCode==5)
            {
                destOp=Integer.parseInt(splited[2]);
                sourceOp1 =Integer.parseInt(splited[1]);
            }
            else{
                destOp = Integer.parseInt(splited[1]);
                sourceOp1 =Integer.parseInt(splited[2]);
            }
            instructions.add(new Instruction(opCode, destOp, sourceOp1, sourceOp2, RstoInstructionMatcher));
        }
        //Making the resitser Table
        for(int j = 0; j < 32; j++){
            RegisterFile.add(j,0.0) ;
        }
        //making the RAT Table
        for(int k = 0; k < 32; k++){
            RAT.add(k,"F" + k) ;

        }


        //Printing the initial RAT, RegisterFile, and INSTRUCTION QUEUE
        System.out.println("Initial RAT, Register File, and Instructions vector");
        print.printRFRAT(RegisterFile, RAT);




        //---------------------FINDING isCycle exCycle writeCycle CYCLES---------------------//

        //These arrays will hold the isCycle, Execution, and writeCycle cycles
        isCycle = new int[numberOfInstructions];
        exCycle = new int[numberOfInstructions];
        writeCycle = new int[numberOfInstructions];
        //Initializing each of those arrays to be -1
        for(int k = 0; k < numberOfInstructions; k++){
            isCycle[k] = -1;
            exCycle[k] = -1;
            writeCycle[k] = -1;
        }
        //Counts the number of previous add or subtract instructions
        int previousAdditions = 0;
        //Counts the number of previous multiply or divide instructions
        int previousMultiplications = 0;
        //This variable is used for a conditional statement if there is a WAW dependency
        int WAWdDenendencyCheck = 0;
        //This variable allows you to enter a conditional statement that compensates for a RAW dependency
        int RAW = 0;
        //This variable determines which of the last three or two instructions (depending on add/sub or mult/div instructions) will broaMulIssCheckast first
        int lowestwriteCycle = 0;
        //This variable determines what instruction has the highest writeCycle cycle with a RAW dependency
        int highestRaw = 0;
        int issueCycle = 1;
        //Keeps track of the current number of in use Reservation Stations for Add or Subtract
        int RSAddition = 0;
        //Keeps track of the current number of in use Reservation Stations for Mult or Divide
        int RSMultiplication = 0;
        //Keeps track of the current number of in use Reservation Stations for Load
        int RSLoad=0;
        //Counts the number of previous loads
        int previousLoads=0;
        //Keeps track of the current number of in use Reservation Stations for stores
        int RSStore=0;
        //Counts the number of previous loads
        int previousStores=0;
        //This allows you get access to the conditional statement that determines what order the instructions are written
        int go = 0;

        //Creates the isCycle, exCycle, and writeCycle cycles for each instruction
        for(int i = 0; i < numberOfInstructions; i++) {
            lowestwriteCycle = 0;
            WAWdDenendencyCheck = 0;
            previousMultiplications = 0;
            previousAdditions = 0;
            go = 1;
            highestRaw=0;
            //-------------------Find isCycle Cycle-------------------//



            //Checks if the opcode is add or subtract and if the reservation stations are not full
            if(((instructions.get(i).getOpCode() == 0) || (instructions.get(i).getOpCode() == 1)) && RSAddition != 3){
                //Increments isCycle cycle and RSAddition after assigning the isCycle cycle
                isCycle[i] = issueCycle;
                issueCycle++;
                RSAddition++;
            }
            //Checks if the opcode is multiply or divide and if the reservation stations are not full
            else if(((instructions.get(i).getOpCode() == 2) || (instructions.get(i).getOpCode() == 3)) && RSMultiplication != 2){
                //Increments isCycle cycle and RSMultiplication after assigning the isCycle cycle
                isCycle[i] = issueCycle;
                issueCycle++;
                RSMultiplication++;
            }
            else if(((instructions.get(i).getOpCode() == 4) && RSLoad != 5))
            {
                isCycle[i] = issueCycle;
                issueCycle++;
                RSLoad++;
            }
            else if(((instructions.get(i).getOpCode() == 5) && RSStore != 5))
            {
                isCycle[i] = issueCycle;
                issueCycle++;
                RSStore++;
            }
            //Checks if the opcdoe is add or subtract and if the reservation stations are full
            else if(((instructions.get(i).getOpCode() == 0) || (instructions.get(i).getOpCode() == 1)) && RSAddition == 3){
                //if the reservation stations are full, we need to check three instruction of the same type before the one we are currently on
                for(int j = i-1; j >= 0; j--){
                    if((instructions.get(j).getOpCode() == 0) || (instructions.get(j).getOpCode() == 1)){
                        //Grabs the lowest writeCycle value for the last three instructions
                        if(previousAdditions != 3){
                            if(go == 1){
                                lowestwriteCycle = writeCycle[j];
                            }
                            if(writeCycle[j] <= lowestwriteCycle){
                                go = 0;
                                lowestwriteCycle = writeCycle[j];
                            }
                        }
                        previousAdditions++;
                    }
                }
                //Makes sure that the lowest writeCycle value + 1 doesnt get assigned as the isCycle cycle for the current instruction if the isCycle cycle is actually greater than lowest writeCycle + 1
                if((lowestwriteCycle+1) >= issueCycle){
                    isCycle[i] = lowestwriteCycle+1;
                    issueCycle = lowestwriteCycle+1;
                    issueCycle++;
                }
                //if the isCycle cycle is greater than lowest writeCycle + 1 then it needs to use the current isCycle cycle
                else{
                    isCycle[i] = issueCycle;
                    issueCycle++;
                }
            }
            //Does the same thing as above excpet for multiply and divide instructions
            else if(((instructions.get(i).getOpCode() == 2) || (instructions.get(i).getOpCode() == 3)) && RSMultiplication == 2){
                for(int j = i-1; j >= 0; j--){
                    if((instructions.get(j).getOpCode() == 2) || (instructions.get(j).getOpCode() == 3)){
                        if(previousMultiplications != 2){
                            if(go == 1){
                                lowestwriteCycle = writeCycle[j];
                            }
                            if(writeCycle[j] <= lowestwriteCycle){
                                go = 0;
                                lowestwriteCycle = writeCycle[j];
                            }
                        }
                        previousMultiplications++;
                    }
                }
                if((lowestwriteCycle+1) >= issueCycle){
                    isCycle[i] = lowestwriteCycle+1;
                    issueCycle = lowestwriteCycle+1;
                    issueCycle++;
                }
                else{
                    isCycle[i] = issueCycle;
                    issueCycle++;
                }
            }
            else if((instructions.get(i).getOpCode() == 4) && (RSLoad == 5)){
                //if the reservation stations are full, we need to check three instruction of the same type before the one we are currently on
                for(int j = i-1; j >= 0; j--){
                    if((instructions.get(j).getOpCode() == 4)){
                        //Grabs the lowest writeCycle value for the last three instructions
                        if(previousLoads != 5){
                            if(go == 1){
                                lowestwriteCycle = writeCycle[j];
                            }
                            if(writeCycle[j] <= lowestwriteCycle){
                                go = 0;
                                lowestwriteCycle = writeCycle[j];
                            }
                        }
                        previousLoads++;
                    }
                }
                //Makes sure that the lowest writeCycle value + 1 doesnt get assigned as the isCycle cycle for the current instruction if the isCycle cycle is actually greater than lowest writeCycle + 1
                if((lowestwriteCycle+1) >= issueCycle){
                    isCycle[i] = lowestwriteCycle+1;
                    issueCycle = lowestwriteCycle+1;
                    issueCycle++;
                }
                //if the isCycle cycle is greater than lowest writeCycle + 1 then it needs to use the current isCycle cycle
                else{
                    isCycle[i] = issueCycle;
                    issueCycle++;
                }
            }
            else if((instructions.get(i).getOpCode() == 5) && (RSStore == 5)){
                //if the reservation stations are full, we need to check three instruction of the same type before the one we are currently on
                for(int j = i-1; j >= 0; j--){
                    if((instructions.get(j).getOpCode() == 5)){
                        //Grabs the lowest writeCycle value for the last three instructions
                        if(previousStores != 5){
                            if(go == 1){
                                lowestwriteCycle = writeCycle[j];
                            }
                            if(writeCycle[j] <= lowestwriteCycle){
                                go = 0;
                                lowestwriteCycle = writeCycle[j];
                            }
                        }
                        previousStores++;
                    }
                }
                //Makes sure that the lowest writeCycle value + 1 doesnt get assigned as the isCycle cycle for the current instruction if the isCycle cycle is actually greater than lowest writeCycle + 1
                if((lowestwriteCycle+1) >= issueCycle){
                    isCycle[i] = lowestwriteCycle+1;
                    issueCycle = lowestwriteCycle+1;
                    issueCycle++;
                }
                //if the isCycle cycle is greater than lowest writeCycle + 1 then it needs to use the current isCycle cycle
                else{
                    isCycle[i] = issueCycle;
                    issueCycle++;
                }
            }
            //---------------------isCycle Cycle Done-------------------//







            //-------------------Find Execution Cycle--------------//

            //Checks for RAW dependencies between the current instruction and every previous instruction
            for(int c = i-1; c >= 0; c--){
                if((instructions.get(i).getOpCode()!=4)&&((instructions.get(i).getSourceOp1() == instructions.get(c).getDestOp()) || (instructions.get(i).getSourceOp2() == instructions.get(c).getDestOp()))){

                    if((writeCycle[c] != -1) && writeCycle[c] > highestRaw){
                        highestRaw = writeCycle[c];
                    }
                    RAW = 1;
                }
            }

            //If raw dependency assign highestRaw + 1 to current exCycle cycle
            if(RAW == 1){
                exCycle[i] = highestRaw + 1;
                RAW = 0;
            }
            else{
                exCycle[i] = issueCycle;
            }

            //If the current isCycle cycle is equal to or greater than the highest Raw, then you need to assign the current exCycle cycle to be the current issueCycle
            if(issueCycle >= highestRaw+1){
                exCycle[i] = issueCycle;
            }

            //Checks if a functional unit is in use; if so, then exCycle cycle for the current instruction get the writeCycle cycle of the previous instruction, since that is when the functional unit is not being used
         /*   if((instructions.get(i).getOpCode() == 0) || (instructions.get(i).getOpCode() == 1)){
                for(int p = i-1; p >= 0; p--){
                    if((instructions.get(p).getOpCode() == 0) || (instructions.get(p).getOpCode() == 1)){
                        if(exCycle[i] >= exCycle[p] && exCycle[i] < writeCycle[p]){
                            exCycle[i] = writeCycle[p];
                        }
                    }
                }
            }
            //same thing as above
            else if((instructions.get(i).getOpCode() == 2) || (instructions.get(i).getOpCode() == 3)){
                for(int p = 0; p < i; p++){
                    if((instructions.get(p).getOpCode() == 2) || (instructions.get(p).getOpCode() == 3)){
                        if(exCycle[i] >= exCycle[p] && exCycle[i] < writeCycle[p]){
                            exCycle[i] = writeCycle[p];
                        }
                    }
                }
            }  */

            //------------------Execution Cycle Done----------------//



            //-----------------Find writeCycle Cycle-------------------//

            // Adds the latencies to the isCycle cycle to give the writeCycle cycle for the current instruction
            if(instructions.get(i).getOpCode() == 0 ){
                writeCycle[i] = exCycle[i] + addLatency;
            }
            else if(instructions.get(i).getOpCode() == 2){
                writeCycle[i] = exCycle[i] +multLatency ;
            }
            else if(instructions.get(i).getOpCode() == 3){
                writeCycle[i] = exCycle[i] + divideLatency;
            }
            else if(instructions.get(i).getOpCode() == 1){
                writeCycle[i] = exCycle[i] + subLatency;
            }
            else if(instructions.get(i).getOpCode() == 4){
                writeCycle[i] = exCycle[i] + loadLatency;
            }
            else if(instructions.get(i).getOpCode() == 5){
                writeCycle[i] = exCycle[i] + storeLatency;
            }
            //Checks for one or more instructions broadcasting at the same time. Multiply and Divide instructions will get precedence over Add and Subtract instructions
            while(WAWdDenendencyCheck == 0){
                WAWdDenendencyCheck = 1;
                for(int h = i-1; h >= 0; h--){
                    if(writeCycle[i] == writeCycle[h]){
                        if(((instructions.get(h).getOpCode() == 2) || (instructions.get(h).getOpCode() == 3)) && ((instructions.get(i).getOpCode() == 0) || (instructions.get(i).getOpCode() == 1))){
                            writeCycle[i] = writeCycle[h] + 1;
                            WAWdDenendencyCheck = 0;
                        }
                        else if(((instructions.get(h).getOpCode() == 0) || (instructions.get(h).getOpCode() == 1)) && ((instructions.get(i).getOpCode() == 2) || (instructions.get(i).getOpCode() == 3))){
                            writeCycle[h] = writeCycle[i] + 1;
                            WAWdDenendencyCheck = 0;
                        }
                        else{
                            writeCycle[i] = writeCycle[h] + 1;
                            WAWdDenendencyCheck = 0;

                        }
                    }
                }

            }

            //---------------writeCycle Cycle Done---------------------//
            //knowing number of cycles to loop on it later
            if(writeCycle[i]>numberOfCycles){
                numberOfCycles=writeCycle[i];
            }

        }



        //---------------Cycle Arrays Done (isCycle, exCycle, writeCycle)---------------//

        //Prints off the cycle diagram
        System.out.println("The cycle diagram for isCycle, exCycle, and writeCycle");
        print.printInstInfo(instructions, numberOfInstructions, isCycle, exCycle, writeCycle);










        //---------------RS/RAT/RegisterFile Updating Algorithm------------------//
        //Declarations for RS/RAT/RegisterFile

        //Update the RS, RAT, and RegisterFile for each instruction until the number of cycles in the text file
        ReservationStation rs1 = new ReservationStation("0", "   ", "  ", "  ", "   ", "   ", " ", 0);
        ReservationStation rs2 = new ReservationStation("0", "   ", "  ", "  ", "   ", "   ", " ", 0);
        ReservationStation rs3 = new ReservationStation("0", "   ", "  ", "  ", "   ", "   ", " ", 0);
        ReservationStation rs4 = new ReservationStation("0", "   ", "  ", "  ", "   ", "   ", " ", 0);
        ReservationStation rs5 = new ReservationStation("0", "   ", "  ", "  ", "   ", "   ", " ", 0);


        int rsClear = 0;
        int instrIndexI = 0;
        int instrIndexE = 0;
        int instrIndexE2 = 0;
        int instrIndexW = 0;
        int activeCycle = 0;
        int sourceOP1 = 0;
        int sourceOP2 = 0;
        String vjIntToString;
        String vkIntToString;

        //indicates if RS is in use
        //if FreeRS# = 0 it is in use
        int freeRS1 = 1;
        int freeRS2 = 1;
        int freeRS3 = 1;
        int freeRS4 = 1;
        int freeRS5 = 1;
        int freeL1 = 1;
        int freeL2 = 1;
        int freeL3 = 1;
        int freeL4 = 1;
        int freeL5 = 1;
        int freeS1 = 1;
        int freeS2 = 1;
        int freeS3 = 1;
        int freeS4 = 1;
        int freeS5 = 1;

        int specialCaseVar = 0;
        int twoFU = 0;


        //Begin looping through the cycles starting at 1
        for(int i = 1; i <= numberOfCycles; i++){
            System.out.println(i);
            activeCycle = 0;
            instrIndexI = -1;
            instrIndexE = -1;
            instrIndexW = -1;

            //Loops through isCycle Cycle array to see if 'i' is stored in there
            for(int j = 0; j < numberOfInstructions; j++){
                if(isCycle[j] == i){
                    instrIndexI = j;
                    activeCycle = 1;
                    break;
                }
            }

            //Loops through Execution Cycle array to see if 'i' is stored in there
            for(int j = 0; j < numberOfInstructions; j++){
                if(exCycle[j] == i){
                    instrIndexE = j;
                    activeCycle = 1;
                    twoFU++;
                }
                if(twoFU == 2){
                    instrIndexE2 = j;
                }
            }

            //Loops through writeCycle Cycle array to see if 'i' is stored in there
            for(int j = 0; j < numberOfInstructions; j++){
                if(writeCycle[j] == i){
                    instrIndexW = j;
                    activeCycle = 1;
                    break;
                }
            }

            //Checks for empty cycles (uncomment if you want to see empty cycles
//            if(activeCycle != 1){
//                System.out.println("Cycle " + i + " was empty");
//            }

            //Clears Reservations stations depending on the value of rsClear
            switch (rsClear) {
                case 1:
                    rsClear = 0;
                    rs1.setBusy("0");
                    rs1.setOp("   ");
                    rs1.setQj("   ");
                    rs1.setQk("   ");
                    rs1.setVj("  ");
                    rs1.setVk("  ");
                    break;
                case 2:
                    rsClear = 0;
                    rs2.setBusy("0");
                    rs2.setOp("   ");
                    rs2.setQj("   ");
                    rs2.setQk("   ");
                    rs2.setVj("  ");
                    rs2.setVk("  ");
                    break;
                case 3:
                    rsClear = 0;
                    rs3.setBusy("0");
                    rs3.setOp("   ");
                    rs3.setQj("   ");
                    rs3.setQk("   ");
                    rs3.setVj("  ");
                    rs3.setVk("  ");
                    break;
                case 4:
                    rsClear = 0;
                    rs4.setBusy("0");
                    rs4.setOp("   ");
                    rs4.setQj("   ");
                    rs4.setQk("   ");
                    rs4.setVj("  ");
                    rs4.setVk("  ");
                    break;
                case 5:
                    rsClear = 0;
                    rs5.setBusy("0");
                    rs5.setOp("   ");
                    rs5.setQj("   ");
                    rs5.setQk("   ");
                    rs5.setVj("  ");
                    rs5.setVk("  ");
                    break;
                case 6:
                    rsClear = 0;
                    l1.setBusy(0);
                    l1.setAddress(0);
                    break;
                case 7:
                    rsClear = 0;
                    l2.setBusy(0);
                    l2.setAddress(0);
                    break;
                case 8:
                    rsClear = 0;
                    l3.setBusy(0);
                    l3.setAddress(0);
                    break;
                case 9:
                    rsClear = 0;
                    l4.setBusy(0);
                    l4.setAddress(0);
                    break;
                case 10:
                    rsClear = 0;
                    l5.setBusy(0);
                    l5.setAddress(0);
                    break;
                case 11:
                    rsClear = 0;
                    s1.setBusy(0);
                    s1.setAddress(0);
                    s1.setVj("  ");
                    s1.setQj("  ");
                    break;
                case 12:
                    rsClear = 0;
                    s2.setBusy(0);
                    s2.setAddress(0);
                    s2.setVj("  ");
                    s2.setQj("  ");
                    break;
                case 13:
                    rsClear = 0;
                    s3.setBusy(0);
                    s3.setAddress(0);
                    s3.setVj("  ");
                    s3.setQj("  ");
                    break;
                case 14:
                    rsClear = 0;
                    s4.setBusy(0);
                    s4.setAddress(0);
                    s4.setVj("  ");
                    s4.setQj("  ");
                    break;
                case 15:
                    rsClear = 0;
                    s5.setBusy(0);
                    s5.setAddress(0);
                    s5.setVj("  ");
                    s5.setQj("  ");
                    break;
                default:
                    break;
            }







            //-----------------SPECIAL CASE: BROAMulIssCheckAST CYCLE == isCycle CYCLE-------------------//
            //In this special case, you need to broaMulIssCheckast before you isCycle so that the issuing instruction can capture the values being written to the RegisterFile
            //Check the writeCycle Stage at the bottom of the page to see comments for Writing
            double vj;
            double vk;
            int Opget = 0;
            int MulIssCheck = 0;
            double value = 0;
            String currentRS =  "";
            if((instrIndexI != -1) && (instrIndexW != -1)){
                Opget = instructions.get(instrIndexW).getOpCode();

                if((Opget!=4)&&(Opget!=5)&&(isCycle[instrIndexI] == writeCycle[instrIndexW])){
                    if(Opget == 2 || Opget == 3){
                        if(rs4.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                            currentRS = "RS4";
                            rsClear = 4;
                            rs4.setBusy("0");
                            if(((!rs4.getVj().equals("  "))  || (!rs4.getVj().equals("   "))) && ((!rs4.getVk().equals("  ")) || (!rs4.getVk().equals("   ")))){
                                vj = Double.parseDouble(rs4.getVj());
                                vk = Double.parseDouble(rs4.getVk());
                                if(Opget == 2){
                                    value = vj * vk;
                                }
                                else if(Opget == 3){
                                    value = vj / vk;
                                }
                            }
                        }
                        else if(rs5.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                            currentRS = "RS5";
                            rsClear = 5;
                            rs5.setBusy("0");
                            if(((!rs5.getVj().equals("  "))  || (!rs5.getVj().equals("   "))) && ((!rs5.getVk().equals("  ")) || (!rs5.getVk().equals("   ")))){
                                vj =Double.parseDouble(rs5.getVj());
                                vk = Double.parseDouble(rs5.getVk());
                                if(Opget == 2){
                                    value = vj * vk;
                                }
                                else if(Opget == 3){
                                    value = vj / vk;
                                }
                            }
                        }

                    }
                    else if(Opget == 0 || Opget == 1){
                        if(rs1.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                            currentRS = "RS1";
                            rsClear = 1;
                            rs1.setBusy("0");
                            if(((!rs1.getVj().equals("  "))  || (!rs1.getVj().equals("   "))) && ((!rs1.getVk().equals("  ")) || (!rs1.getVk().equals("   ")))){
                                vj = Double.parseDouble(rs1.getVj());
                                System.out.println(vj);
                                vk = Double.parseDouble(rs1.getVk());
                                System.out.println(vk);
                                if(Opget == 0){
                                    value = vj + vk;
                                }
                                else if(Opget == 1){
                                    value = vj - vk;
                                }
                            }
                        }
                        else if(rs2.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                            currentRS = "RS2";
                            rsClear = 2;
                            rs2.setBusy("0");
                            if(((!rs2.getVj().equals("  "))  || (!rs2.getVj().equals("   "))) && ((!rs2.getVk().equals("  ")) || (!rs2.getVk().equals("   ")))){
                                vj = Double.parseDouble(rs2.getVj());
                                vk = Double.parseDouble(rs2.getVk());
                                if(Opget == 0){
                                    value = vj + vk;
                                }
                                else if(Opget == 1){
                                    value = vj - vk;
                                }
                            }
                        }
                        else if(rs3.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                            currentRS = "RS3";
                            rsClear = 3;
                            rs3.setBusy("0");
                            if(((!rs3.getVj().equals("  "))  || (!rs3.getVj().equals("   "))) && ((!rs3.getVk().equals("  ")) || (!rs3.getVk().equals("   ")))){
                                vj = Double.parseDouble(rs3.getVj());
                                vk = Double.parseDouble(rs3.getVk());
                                if(Opget == 0){
                                    value = vj + vk;
                                }
                                else if(Opget == 1){
                                    value = vj - vk;
                                }
                            }
                        }
                    }
                   /* else if(Opget==4){
                        if(rs7.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                            currentRS = "RS7";
                            rsClear = 1;
                            rs7.setBusy("0");
                            if(((!rs7.getVj().equals("  "))  || (!rs7.getVj().equals("   "))) && ((!rs7.getVk().equals("  ")) || (!rs7.getVk().equals("   ")))){
                                //vj = Integer.parseInt(rs7.getVj());
                                value = Integer.parseInt(rs7.getVj());;
                            }
                        }
                        else if(rs8.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                            currentRS = "RS8";
                            rsClear = 8;
                            rs8.setBusy("0");
                            if(((!rs8.getVj().equals("  "))  || (!rs8.getVj().equals("   "))) && ((!rs8.getVk().equals("  ")) || (!rs8.getVk().equals("   ")))){
                                value = Integer.parseInt(rs8.getVj());
                            }
                        }
                        else if(rs9.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                            currentRS = "RS9";
                            rsClear = 9;
                            rs3.setBusy("0");
                            if(((!rs9.getVj().equals("  "))  || (!rs9.getVj().equals("   "))) && ((!rs9.getVk().equals("  ")) || (!rs9.getVk().equals("   ")))){
                                value = Integer.parseInt(rs9.getVj());
                            }
                        }
                    }*/


                  /*  if(rs1.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                        currentRS = "RS1";
                        rsClear = 1;
                        rs1.setBusy("0");
                    }
                    if(rs2.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                        currentRS = "RS2";
                        rsClear = 2;
                        rs2.setBusy("0");
                    }
                    if(rs3.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                        currentRS = "RS3";
                        rsClear = 3;
                        rs3.setBusy("0");
                    }
                    if(rs4.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                        currentRS = "RS4";
                        rsClear = 4;
                        rs4.setBusy("0");
                    }
                    if(rs5.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                        currentRS = "RS5";
                        rsClear = 5;
                        rs5.setBusy("0");
                    }
              */
                    if( rs1.getQj().equals(currentRS)){
                        rs1.setVj(Double.toString(value));
                        rs1.setQj("   ");
                    }
                    if (rs1.getQk().equals(currentRS)){
                        System.out.println("hi");

                        rs1.setVk(Double.toString(value));
                        rs1.setQk("   ");
                    }
                    if (rs2.getQj().equals(currentRS)){
                        rs2.setVj(Double.toString(value));
                        rs2.setQj("   ");
                    }
                    if (rs2.getQk().equals(currentRS)){
                        rs2.setVk(Double.toString(value));
                        rs2.setQk("   ");
                    }
                    if (rs3.getQj().equals(currentRS)){
                        rs3.setVj(Double.toString(value));
                        rs3.setQj("   ");
                    }
                    if (rs3.getQk().equals(currentRS)){
                        rs3.setVk(Double.toString(value));
                        rs3.setQk("   ");
                    }
                    if (rs4.getQj().equals(currentRS)){
                        rs4.setVj(Double.toString(value));
                        rs4.setQj("   ");
                    }
                    if (rs4.getQk().equals(currentRS)){
                        rs4.setVk(Double.toString(value));
                        rs4.setQk("   ");
                    }
                    if (rs5.getQj().equals(currentRS)){
                        rs5.setVj(Double.toString(value));
                        rs5.setQj("   ");
                    }
                    if (rs5.getQk().equals(currentRS)){
                        rs5.setVk(Double.toString(value));
                        rs5.setQk("   ");
                    }
                    if (currentRS.equals(s1.getQj())) {
                        System.out.println(currentRS);
                        s1.setVj(Double.toString(value));
                        s1.setQj("   ");
                    }
                    if (currentRS.equals(s2.getQj())) {
                        System.out.println(currentRS);

                        s2.setVj(Double.toString(value));
                        s2.setQj("   ");
                    }
                    if (currentRS.equals(s3.getQj())) {
                        s3.setVj(Double.toString(value));
                        s3.setQj("   ");
                    }
                    if (currentRS.equals(s4.getQj())) {
                        s4.setVj(Double.toString(value));
                        s4.setQj("   ");
                    }
                    if (currentRS.equals(s5.getQj())) {
                        s5.setVj(Double.toString(value));
                        s5.setQj("   ");
                    }

                    for(int q = 0; q < 32; q++){
                        if(RAT.get(q).equals(currentRS)){
                            RegisterFile.set(q,value);
                            RAT.set(q,"F" + q);
                            break;
                        }
                    }
                    //when taking the special case, you dont need to writeCycle then since this is what this case does
                    specialCaseVar = 1;
                }
            }


            //------------isCycle--------------//
            //Makes sure you don't place multiple of the same instruction in all of the RS
            MulIssCheck = 0;
            //If instrIndexI was set to a value besides -1 then you know you have to isCycle this cycle
            if(instrIndexI != -1) {
                //If instruction is add or sub
                if (instructions.get(instrIndexI).getOpCode() == 0 || instructions.get(instrIndexI).getOpCode() == 1) {
                    //If RS1 is free and we have not double counted
                    if ((freeRS1 == 1) && (MulIssCheck != 1)) {
                        //denies access to any other RS
                        MulIssCheck = 1;
                        //Links an instruction to a reservation station
                        rs1.setRstoInstructionMatcher(instructions.get(instrIndexI).getRstoInstructionMatcher());
                        //This reservation station is in use now
                        freeRS1 = 0;
                        //Choose whether the op is add or sub
                        switch (instructions.get(instrIndexI).getOpCode()) {
                            case 0:
                                rs1.setOp("ADD");
                                break;
                            case 1:
                                rs1.setOp("SUB");
                                break;
                        }
                        //RS1 is busy now
                        rs1.setBusy("1");
                        //get the two source ops to look inside the RAT to see if its pointing to the RegisterFile
                        sourceOP1 = instructions.get(instrIndexI).getSourceOp1();
                        sourceOP2 = instructions.get(instrIndexI).getSourceOp2();
                        //If it is pointint to the RegisterFile, then Qj is empty and Vj gets the RegisterFile value
                        if (RAT.get(sourceOP1).equals("F" + (sourceOP1))) {
                            vjIntToString = Double.toString(RegisterFile.get(sourceOP1));
                            rs1.setVj(vjIntToString);
                            rs1.setQj("   ");
                        }
                        //Else Vj is empty and Qj gets the Tag
                        else {
                            rs1.setVj("  ");
                            rs1.setQj(RAT.get(sourceOP1));
                        }
                        //Same thing as above
                        if (RAT.get(sourceOP2).equals("F" + (sourceOP2))) {
                            System.out.println("hii");
                            vkIntToString = Double.toString(RegisterFile.get(sourceOP2));

                            rs1.setVk(vkIntToString);
                            rs1.setQk("   ");
                        } else {
                            rs1.setVk("  ");
                            rs1.setQk(RAT.get(sourceOP2));
                        }
                        //Set the tag in the destination ops location to be the current RS you are in
                        RAT.set(instructions.get(instrIndexI).getDestOp(), "RS1");
                    }
                    //Same thing as above
                    if ((freeRS2 == 1) && (MulIssCheck != 1)) {
                        MulIssCheck = 1;
                        rs2.setRstoInstructionMatcher(instructions.get(instrIndexI).getRstoInstructionMatcher());
                        freeRS2 = 0;
                        switch (instructions.get(instrIndexI).getOpCode()) {
                            case 0:
                                rs2.setOp("ADD");
                                break;
                            case 1:
                                rs2.setOp("SUB");
                                break;
                        }
                        rs2.setBusy("1");
                        sourceOP1 = instructions.get(instrIndexI).getSourceOp1();
                        sourceOP2 = instructions.get(instrIndexI).getSourceOp2();
                        if (RAT.get(sourceOP1).equals("F" + (sourceOP1))) {
                            vjIntToString = Double.toString(RegisterFile.get(sourceOP1));
                            rs2.setVj(vjIntToString);
                            rs2.setQj("   ");
                        } else {
                            rs2.setVj("  ");
                            rs2.setQj(RAT.get(sourceOP1));
                        }
                        if (RAT.get(sourceOP2).equals("F" + (sourceOP2))) {
                            vkIntToString = Double.toString(RegisterFile.get(sourceOP2));
                            rs2.setVk(vkIntToString);
                            rs2.setQk("   ");
                        } else {
                            rs2.setVk("  ");
                            rs2.setQk(RAT.get(sourceOP2));
                        }
                        RAT.set(instructions.get(instrIndexI).getDestOp(), "RS2");
                    }
                    //Same thing as above
                    if ((freeRS3 == 1) && (MulIssCheck != 1)) {
                        rs3.setRstoInstructionMatcher(instructions.get(instrIndexI).getRstoInstructionMatcher());
                        freeRS3 = 0;
                        switch (instructions.get(instrIndexI).getOpCode()) {
                            case 0:
                                rs3.setOp("ADD");
                                break;
                            case 1:
                                rs3.setOp("SUB");
                                break;
                        }
                        rs3.setBusy("1");
                        sourceOP1 = instructions.get(instrIndexI).getSourceOp1();
                        sourceOP1 = instructions.get(instrIndexI).getSourceOp1();
                        sourceOP2 = instructions.get(instrIndexI).getSourceOp2();
                        if (RAT.get(sourceOP1).equals("F" + (sourceOP1))) {
                            vjIntToString = Double.toString(RegisterFile.get(sourceOP1));
                            rs3.setVj(vjIntToString);
                            rs3.setQj("   ");
                        } else {
                            rs3.setVj("  ");
                            rs3.setQj(RAT.get(sourceOP1));
                        }
                        if (RAT.get(sourceOP2).equals("F" + (sourceOP2))) {
                            vkIntToString = Double.toString(RegisterFile.get(sourceOP2));
                            rs3.setVk(vkIntToString);
                            rs3.setQk("   ");
                        } else {
                            rs3.setVk("  ");
                            rs3.setQk(RAT.get(sourceOP2));
                        }
                        RAT.set(instructions.get(instrIndexI).getDestOp(), "RS3");
                    }
                }
                //Same thing as above but its Mul and Div
                else if (instructions.get(instrIndexI).getOpCode() == 2 || instructions.get(instrIndexI).getOpCode() == 3) {
                    if (freeRS4 == 1 && MulIssCheck != 1) {
                        MulIssCheck = 1;
                        rs4.setRstoInstructionMatcher(instructions.get(instrIndexI).getRstoInstructionMatcher());
                        freeRS4 = 0;
                        switch (instructions.get(instrIndexI).getOpCode()) {
                            case 2:
                                rs4.setOp("MUL");
                                break;
                            case 3:
                                rs4.setOp("DIV");
                                break;
                        }
                        rs4.setBusy("1");
                        sourceOP1 = instructions.get(instrIndexI).getSourceOp1();
                        sourceOP2 = instructions.get(instrIndexI).getSourceOp2();
                        if (RAT.get(sourceOP1).equals("F" + (sourceOP1))) {
                            vjIntToString = Double.toString(RegisterFile.get(sourceOP1));
                            rs4.setVj(vjIntToString);
                            rs4.setQj("   ");
                        } else {
                            rs4.setVj("  ");
                            rs4.setQj(RAT.get(sourceOP1));
                        }
                        if (RAT.get(sourceOP2).equals("F" + (sourceOP2))) {
                            vkIntToString = Double.toString(RegisterFile.get(sourceOP2));
                            rs4.setVk(vkIntToString);
                            rs4.setQk("   ");
                        } else {
                            rs4.setVk("  ");
                            rs4.setQk(RAT.get(sourceOP2));
                        }
                        RAT.set((instructions.get(instrIndexI).getDestOp()), "RS4");
                    }
                    //Same thing as above but its Mul and Div
                    if (freeRS5 == 1 && MulIssCheck != 1) {
                        MulIssCheck = 1;
                        rs5.setRstoInstructionMatcher(instructions.get(instrIndexI).getRstoInstructionMatcher());
                        freeRS5 = 0;
                        switch (instructions.get(instrIndexI).getOpCode()) {
                            case 2:
                                rs5.setOp("MUL");
                                break;
                            case 3:
                                rs5.setOp("DIV");
                                break;
                        }
                        rs5.setBusy("1");
                        sourceOP1 = instructions.get(instrIndexI).getSourceOp1();
                        sourceOP2 = instructions.get(instrIndexI).getSourceOp2();
                        if (RAT.get(sourceOP1).equals("F" + (sourceOP1))) {
                            vjIntToString = Double.toString(RegisterFile.get(sourceOP1));
                            rs5.setVj(vjIntToString);
                            rs5.setQj("   ");
                        } else {
                            rs5.setVj("  ");
                            rs5.setQj(RAT.get(sourceOP1));
                        }
                        if (RAT.get(sourceOP2).equals("F" + (sourceOP2))) {
                            vkIntToString = Double.toString(RegisterFile.get(sourceOP2));
                            rs5.setVk(vkIntToString);
                            rs5.setQk("   ");
                        } else {
                            rs5.setVk("  ");
                            rs5.setQk(RAT.get(sourceOP2));
                        }
                        RAT.set(instructions.get(instrIndexI).getDestOp(), "RS5");

                    }
                }
                //Same thing as above but its Mul and Div
                else if (instructions.get(instrIndexI).getOpCode() == 4) {
                    if (freeL1 == 1 && MulIssCheck != 1) {
                        MulIssCheck = 1;
                        freeL1 = 0;
                        l1.setBusy(1);
                        sourceOP1 = instructions.get(instrIndexI).getSourceOp1();
                        l1.setAddress(sourceOP1);
                        RAT.set((instructions.get(instrIndexI).getDestOp()), "L1");
                    }
                    //Same thing as above but its Mul and Div
                    if (freeL2 == 1 && MulIssCheck != 1) {
                        MulIssCheck = 1;
                        freeL2 = 0;
                        l2.setBusy(1);
                        sourceOP1 = instructions.get(instrIndexI).getSourceOp1();
                        l2.setAddress(sourceOP1);
                        RAT.set((instructions.get(instrIndexI).getDestOp()), "L2");
                    }
                    if (freeL3 == 1 && MulIssCheck != 1) {
                        MulIssCheck = 1;
                        freeL3 = 0;
                        l3.setBusy(1);
                        sourceOP1 = instructions.get(instrIndexI).getSourceOp1();
                        l3.setAddress(sourceOP1);
                        RAT.set((instructions.get(instrIndexI).getDestOp()), "L3");
                    }
                    if (freeL4 == 1 && MulIssCheck != 1) {
                        MulIssCheck = 1;
                        freeL4 = 0;
                        l4.setBusy(1);
                        sourceOP1 = instructions.get(instrIndexI).getSourceOp1();
                        l4.setAddress(sourceOP1);
                        System.out.println(instructions.get(instrIndexI).getDestOp());
                        RAT.set((instructions.get(instrIndexI).getDestOp()), "L4");
                    }
                    if (freeL5 == 1 && MulIssCheck != 1) {
                        MulIssCheck = 1;
                        freeL5 = 0;
                        l5.setBusy(1);
                        sourceOP1 = instructions.get(instrIndexI).getSourceOp1();
                        l5.setAddress(sourceOP1);
                        RAT.set((instructions.get(instrIndexI).getDestOp()), "L5");
                    }

                } else if (instructions.get(instrIndexI).getOpCode() == 5) {
                    if (freeS1 == 1 && MulIssCheck != 1) {
                        MulIssCheck = 1;
                        freeS1 = 0;
                        s1.setBusy(1);
                        sourceOP1 = instructions.get(instrIndexI).getSourceOp1();
                        if (RAT.get(sourceOP1).equals("F" + (sourceOP1))) {
                            vjIntToString = Double.toString(RegisterFile.get(sourceOP1));
                            s1.setVj(vjIntToString);
                            s1.setQj("   ");
                        } else {
                            s1.setVj("  ");
                            s1.setQj(RAT.get(sourceOP1));
                        }
                        s1.setAddress(instructions.get(instrIndexI).getDestOp());
                    }
                    if (freeS2 == 1 && MulIssCheck != 1) {
                        MulIssCheck = 1;
                        freeS2 = 0;
                        s2.setBusy(1);
                        sourceOP1 = instructions.get(instrIndexI).getSourceOp1();
                        if (RAT.get(sourceOP1).equals("F" + (sourceOP1))) {
                            vjIntToString = Double.toString(RegisterFile.get(sourceOP1));
                            s2.setVj(vjIntToString);
                            s2.setQj("   ");
                        } else {
                            s2.setVj("  ");
                            s2.setQj(RAT.get(sourceOP1));
                        }
                        s2.setAddress(instructions.get(instrIndexI).getDestOp());
                    }
                    if (freeS3 == 1 && MulIssCheck != 1) {
                        MulIssCheck = 1;
                        freeS3 = 0;
                        s3.setBusy(1);
                        sourceOP1 = instructions.get(instrIndexI).getSourceOp1();
                        if (RAT.get(sourceOP1).equals("F" + (sourceOP1))) {
                            vjIntToString = Double.toString(RegisterFile.get(sourceOP1));
                            s3.setVj(vjIntToString);
                            s3.setQj("   ");
                        } else {
                            s3.setVj("  ");
                            s3.setQj(RAT.get(sourceOP1));
                        }
                        s3.setAddress(instructions.get(instrIndexI).getDestOp());
                    }if (freeS4 == 1 && MulIssCheck != 1) {
                        MulIssCheck = 1;
                        freeS4 = 0;
                        s4.setBusy(1);
                        sourceOP1 = instructions.get(instrIndexI).getSourceOp1();
                        if (RAT.get(sourceOP1).equals("F" + (sourceOP1))) {
                            vjIntToString = Double.toString(RegisterFile.get(sourceOP1));
                            s4.setVj(vjIntToString);
                            s4.setQj("   ");
                        } else {
                            s4.setVj("  ");
                            s4.setQj(RAT.get(sourceOP1));
                        }
                        s4.setAddress(instructions.get(instrIndexI).getDestOp());
                    }if (freeS5 == 1 && MulIssCheck != 1) {
                        MulIssCheck = 1;
                        freeS5 = 0;
                        s5.setBusy(1);
                        sourceOP1 = instructions.get(instrIndexI).getSourceOp1();
                        if (RAT.get(sourceOP1).equals("F" + (sourceOP1))) {
                            vjIntToString = Double.toString(RegisterFile.get(sourceOP1));
                            s5.setVj(vjIntToString);
                            s5.setQj("   ");
                        } else {
                            s5.setVj("  ");
                            s5.setQj(RAT.get(sourceOP1));
                        }
                        s5.setAddress(instructions.get(instrIndexI).getDestOp());
                    }

                }
            }

            //-----------------isCycle Algorithm finished---------------//




            //-----------------exCycle-----------------//


            //Compare using match value associated with both the instruction and the RS
            //setting dispatch to 1 if RS RstoInstructionMatcher = Instuction RstoInstructionMatcher. This means instruction is in that particular RS
            Opget = 0;
            //Allows access if we are executing this cycle
            /*if(instrIndexE != -1){
                //Opget stores the opcode value of the instruction
                Opget = instructions.get(instrIndexE).getOpCode();
                //If add or sub
                if(Opget == 0 || Opget == 1){
                    //This is where the linking system between instruction and reservation station really shines
                    //We know what instruction is in what reservation station now and we know the index of what instruction needs to exCycle so this sets the dispatch notifier
                    if(rs1.getRstoInstructionMatcher() == instructions.get(instrIndexE).getRstoInstructionMatcher()){
                        rs1.setDisp("1");
                    }
                    else if(rs2.getRstoInstructionMatcher() == instructions.get(instrIndexE).getRstoInstructionMatcher()){
                        rs2.setDisp("1");
                    }
                    else if(rs3.getRstoInstructionMatcher() == instructions.get(instrIndexE).getRstoInstructionMatcher()){
                        rs3.setDisp("1");
                    }
                }
                else if(Opget == 2 || Opget == 3){
                    if(rs4.getRstoInstructionMatcher() == instructions.get(instrIndexE).getRstoInstructionMatcher()){
                        rs4.setDisp("1");
                    }
                    else if(rs5.getRstoInstructionMatcher() == instructions.get(instrIndexE).getRstoInstructionMatcher()){
                        rs5.setDisp("1");
                    }
                }
            }*/
            //If we took the special case above where we need to broaMulIssCheckast before issuing then it enters this
            if(instrIndexW != -1 && specialCaseVar == 1){
                //set the var back to 0
                specialCaseVar = 0;
                //free the reservation station for the next cycle
                if(rs1.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                    freeRS1 = 1;
                }
                if(rs2.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                    freeRS2 = 1;
                }
                if(rs3.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                    freeRS3 = 1;
                }
                if(rs4.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                    freeRS4 = 1;
                }
                if(rs5.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                    freeRS5 = 1;
                }
                //set instrIndexW back to -1
                instrIndexW = -1;
            }
            //------------------------exCycle Finished-----------------------//



            //---------------------------writeCycleCycle---------------------------//
            //Stores the value of the evacuated instruction
            value = 0;
            //Set currentRS back to null
            currentRS =  null;
            //Only allows access during a cycle that needs to writeCycle
            if(instrIndexW != -1){
                //Gets the opcode
                Opget = instructions.get(instrIndexW).getOpCode();
                //If add or subtract
                if(Opget == 0 || Opget == 1){
                    //Finds the instruction in the reservation stations
                    if(rs1.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                        //In writeCycle stage now so dispatch notifier needs to be set back to 0
                        //If the vj and vk values are not empty then enter
                        if(((!rs1.getVj().equals("  "))  || (!rs1.getVj().equals("   "))) && ((!rs1.getVk().equals("  ")) || (!rs1.getVk().equals("   ")))){
                            //evaluates the vj and vk values depending on the opcode
                            vj = Double.parseDouble(rs1.getVj());
                            vk = Double.parseDouble(rs1.getVk());
                            if(Opget == 0){
                                value = vj + vk;
                            }
                            else if(Opget == 1){
                                value = vj - vk;
                            }
                        }
                    }
                    //same as above
                    else if(rs2.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                        if(((!rs2.getVj().equals("  "))  || (!rs2.getVj().equals("   "))) && ((!rs2.getVk().equals("  ")) || (!rs2.getVk().equals("   ")))){
                            vj = Double.parseDouble(rs2.getVj());
                            vk = Double.parseDouble(rs2.getVk());
                            if(Opget == 0){
                                value = vj + vk;
                            }
                            else if(Opget == 1){
                                value = vj - vk;
                            }
                        }
                    }
                    //same as above
                    else if(rs3.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                        if(((!rs3.getVj().equals("  "))  || (!rs3.getVj().equals("   "))) && ((!rs3.getVk().equals("  ")) || (!rs3.getVk().equals("   ")))){
                            vj = Double.parseDouble(rs3.getVj());
                            vk = Double.parseDouble(rs3.getVk());
                            if(Opget == 0){
                                value = vj + vk;
                            }
                            else if(Opget == 1){
                                value = vj - vk;
                            }
                        }
                    }
                }
                //same as above except its multiply and divide
                else if(Opget == 2 || Opget == 3){
                    if(rs4.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                        if(((!rs4.getVj().equals("  "))  || (!rs4.getVj().equals("   "))) && ((!rs4.getVk().equals("  ")) || (!rs4.getVk().equals("   ")))){
                            vj = Double.parseDouble(rs4.getVj());
                            vk = Double.parseDouble(rs4.getVk());
                            if(Opget == 2){
                                value = vj * vk;
                            }
                            else if(Opget == 3){
                                value = vj / vk;
                            }
                        }
                    }
                    else if(rs5.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()){
                        if(((!rs5.getVj().equals("  "))  || (!rs5.getVj().equals("   "))) && ((!rs5.getVk().equals("  ")) || (!rs5.getVk().equals("   ")))){
                            vj = Double.parseDouble(rs5.getVj());
                            vk = Double.parseDouble(rs5.getVk());
                            if(Opget == 2){
                                value = vj * vk;
                            }
                            else if(Opget == 3){
                                value = vj / vk;
                            }
                        }
                    }
                }
                else if(Opget == 4){
                    value=memory[instructions.get(instrIndexW).getSourceOp1()];

                    if(l1.getAddress() == instructions.get(instrIndexW).getSourceOp1()){
                        currentRS = "L1";
                        rsClear = 6;
                        freeL1 = 1;
                        l1.setBusy(0);
                    }
                    else if(l2.getAddress() == instructions.get(instrIndexW).getSourceOp1()){
                        currentRS = "L2";
                        rsClear = 7;
                        freeL2 = 1;
                        l2.setBusy(0);
                    }
                    else if(l3.getAddress() == instructions.get(instrIndexW).getSourceOp1()){
                        currentRS = "L3";
                        rsClear = 8;
                        freeL3 = 1;
                        l3.setBusy(0);
                    }
                    else if(l4.getAddress() == instructions.get(instrIndexW).getSourceOp1()){
                        currentRS = "L4";
                        rsClear = 9;
                        freeL4 = 1;
                        l4.setBusy(0);
                    }
                    else if(l5.getAddress() == instructions.get(instrIndexW).getSourceOp1()){
                        currentRS = "L5";
                        rsClear = 10;
                        freeL5=1;
                        l5.setBusy(0);
                    }
                    RegisterFile.set(instructions.get(instrIndexW).getDestOp(), value);
                }
                else if(Opget == 5){

                    if(s1.getAddress() == instructions.get(instrIndexW).getDestOp()) {
                        rsClear = 11;
                        freeS1 = 1;
                        s1.setBusy(0);
                        if (((!s1.getVj().equals("  ")))) {
                            vj = Double.parseDouble(s1.getVj());

                            memory[instructions.get(instrIndexW).getDestOp()]=vj;
                        }
                    }
                   else if(s2.getAddress() == instructions.get(instrIndexW).getDestOp()){
                        rsClear = 12;
                        freeS2 = 1;
                        s2.setBusy(0);
                        if (((!s2.getVj().equals("  ")) )) {
                            vj = Double.parseDouble(s2.getVj());
                            memory[instructions.get(instrIndexW).getDestOp()]=vj;
                        }
                    }
                   else if(s3.getAddress() == instructions.get(instrIndexW).getDestOp()){
                        rsClear = 13;
                        freeS3 = 1;
                        s3.setBusy(0);
                        if (((!s3.getVj().equals("  ")))) {
                            vj = Double.parseDouble(s3.getVj());
                            memory[instructions.get(instrIndexW).getDestOp()]=vj;
                        }
                    }
                   else if(s4.getAddress() == instructions.get(instrIndexW).getDestOp()){
                        rsClear = 14;
                        freeS4 = 1;
                        s4.setBusy(0);
                        if (((!s4.getVj().equals("  ")) )) {
                            vj = Double.parseDouble(s4.getVj());
                            memory[instructions.get(instrIndexW).getDestOp()]=vj;
                        }
                    }
                   else if(s5.getAddress() == instructions.get(instrIndexW).getDestOp()){
                        rsClear = 15;
                        freeS5 = 1;
                        s5.setBusy(0);
                        if (((!s5.getVj().equals("  ")) )) {
                            vj = Double.parseDouble(s5.getVj());
                            memory[instructions.get(instrIndexW).getDestOp()]=vj;
                        }
                    }
                }
                //Sets the current Reservation station for the writing instructions
                //Also set the reservation station to be cleared after this cycle
                //Sets the current reservation station to be open
                //Also sets Busy to be 0 for the current reservation station
                if((Opget!=4)){
                    if (rs1.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()) {
                        currentRS = "RS1";
                        rsClear = 1;
                        freeRS1 = 1;
                        rs1.setBusy("0");
                    }
                    //same as above
                    if (rs2.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()) {
                        currentRS = "RS2";
                        rsClear = 2;
                        freeRS2 = 1;
                        rs2.setBusy("0");
                    }
                    //same as above
                    if (rs3.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()) {
                        currentRS = "RS3";
                        rsClear = 3;
                        freeRS3 = 1;
                        rs3.setBusy("0");
                    }
                    //same as above
                    if (rs4.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()) {
                        currentRS = "RS4";
                        rsClear = 4;
                        freeRS4 = 1;
                        rs4.setBusy("0");
                    }
                    //same as above
                    if (rs5.getRstoInstructionMatcher() == instructions.get(instrIndexW).getRstoInstructionMatcher()) {
                        currentRS = "RS5";
                        rsClear = 5;
                        freeRS5 = 1;
                        rs5.setBusy("0");
                    }
                }
                //Checks all the Qj and Qk fields to see if Vj or Vk need to be updated this cycle
                if(Opget!=5) {
                    if (currentRS.equals(rs1.getQj())) {
                        rs1.setVj(Double.toString(value));
                        rs1.setQj("   ");
                    }
                    //same as above
                    if (currentRS.equals(rs1.getQk())) {

                        rs1.setVk(Double.toString(value));
                        rs1.setQk("   ");
                    }
                    //same as above
                    if (currentRS.equals(rs2.getQj())) {
                        rs2.setVj(Double.toString(value));
                        rs2.setQj("   ");
                    }
                    //same as above
                    if (currentRS.equals(rs2.getQk())) {
                        rs2.setVk(Double.toString(value));
                        rs2.setQk("   ");
                    }
                    //same as above
                    if (currentRS.equals(rs3.getQj())) {
                        rs3.setVj(Double.toString(value));
                        rs3.setQj("   ");
                    }
                    //same as above
                    if (currentRS.equals(rs3.getQk())) {
                        rs3.setVk(Double.toString(value));
                        rs3.setQk("   ");
                    }
                    //same as above
                    if (currentRS.equals(rs4.getQj())) {
                        rs4.setVj(Double.toString(value));
                        rs4.setQj("   ");
                    }
                    //same as above
                    if (currentRS.equals(rs4.getQk())) {
                        rs4.setVk(Double.toString(value));
                        rs4.setQk("   ");
                    }
                    //same as above
                    if (currentRS.equals(rs5.getQj())) {
                        rs5.setVj(Double.toString(value));
                        rs5.setQj("   ");
                    }
                    //same as above
                    if (currentRS.equals(rs5.getQk())) {
                        rs5.setVk(Double.toString(value));
                        rs5.setQk("   ");
                    }
                    if (currentRS.equals(s1.getQj())) {
                        System.out.println(currentRS);
                        s1.setVj(Double.toString(value));
                        s1.setQj("   ");
                    }
                    if (currentRS.equals(s2.getQj())) {
                        System.out.println(currentRS);

                        s2.setVj(Double.toString(value));
                        s2.setQj("   ");
                    }
                    if (currentRS.equals(s3.getQj())) {
                        s3.setVj(Double.toString(value));
                        s3.setQj("   ");
                    }
                    if (currentRS.equals(s4.getQj())) {
                        s4.setVj(Double.toString(value));
                        s4.setQj("   ");
                    }
                    if (currentRS.equals(s5.getQj())) {
                        s5.setVj(Double.toString(value));
                        s5.setQj("   ");
                    }

                }
                //Sets the RAT to point back to the RegisterFile
                //If its a stale result then it won't update the RAT or RegisterFile
                //System.out.println(currentRS);
                for(int q = 0; q < 32; q++){

                    if(RAT.get(q).equals(currentRS)){
                        RegisterFile.set(q, value);
                        RAT.set(q, "F" + q);
                        break;
                    }
                }
            }
            ReservationStation newRs1 = new ReservationStation(rs1.getBusy(),rs1.getOp(),rs1.getVj(),rs1.getVk(),rs1.Qj,rs1.Qk,rs1.getDisp(),rs1.getRstoInstructionMatcher());
            ReservationStation newRs2 = new ReservationStation(rs2.getBusy(),rs2.getOp(),rs2.getVj(),rs2.getVk(),rs2.Qj,rs2.Qk,rs1.getDisp(),rs2.getRstoInstructionMatcher());
            ReservationStation newRs3 = new ReservationStation(rs3.getBusy(),rs3.getOp(),rs3.getVj(),rs3.getVk(),rs3.Qj,rs3.Qk,rs1.getDisp(),rs3.getRstoInstructionMatcher());
            ReservationStation newRs4 = new ReservationStation(rs4.getBusy(),rs4.getOp(),rs4.getVj(),rs4.getVk(),rs4.Qj,rs4.Qk,rs1.getDisp(),rs4.getRstoInstructionMatcher());
            ReservationStation newRs5 = new ReservationStation(rs5.getBusy(),rs5.getOp(),rs5.getVj(),rs5.getVk(),rs5.Qj,rs5.Qk,rs1.getDisp(),rs5.getRstoInstructionMatcher());
            ReservationStationsToBeStored.add(newRs1);
            ReservationStationsToBeStored.add(newRs2);
            ReservationStationsToBeStored.add(newRs3);
            ReservationStationsToBeStored.add(newRs4);
            ReservationStationsToBeStored.add(newRs5);
            LoadBuffer newL1 = new LoadBuffer(l1.busy,l1.address);
            LoadBuffer newL2 = new LoadBuffer(l2.busy,l2.address);
            LoadBuffer newL3 = new LoadBuffer(l3.busy,l3.address);
            LoadBuffer newL4 = new LoadBuffer(l4.busy,l4.address);
            LoadBuffer newL5 = new LoadBuffer(l5.busy,l5.address);
            loadBuffers.add(newL1);
            loadBuffers.add(newL2);
            loadBuffers.add(newL3);
            loadBuffers.add(newL4);
            loadBuffers.add(newL5);

            StoreBuffer news1 = new StoreBuffer(s1.busy,s1.address, s1.vj, s1.qj);
            StoreBuffer news2 = new StoreBuffer(s2.busy,s2.address, s2.vj, s2.qj);
            StoreBuffer news3 = new StoreBuffer(s3.busy,s3.address, s3.vj, s3.qj);
            StoreBuffer news4 = new StoreBuffer(s4.busy,s4.address, s4.vj, s4.qj);
            StoreBuffer news5 = new StoreBuffer(s5.busy,s5.address, s5.vj, s5.qj);
            storeBuffers.add(news1);
            storeBuffers.add(news2);
            storeBuffers.add(news3);
            storeBuffers.add(news4);
            storeBuffers.add(news5);

            Object newRegisterFile = RegisterFile.clone();
            RegisterFilesToBeStored.add((Vector)newRegisterFile);
            Object newRAT=RAT.clone();
            System.out.println(newRAT);
            RATsToBeStored.add((Vector)newRAT);
            print.printRFRAT(RegisterFile, RAT);
            print.printRS(rs1, rs2, rs3, rs4, rs5);
            Object newMemory = memory.clone();
            System.out.println(Arrays.toString((double[])newMemory));

            memoryVersions.add((double[]) newMemory);

        }
        //printing functions that display the RegisterFile, RAT, and Reservation Stations for each cycle

        System.out.println(loadBuffers);
        System.out.println(storeBuffers);
        System.out.println(memory[23]);
        System.out.println(RegisterFilesToBeStored);
    }
}