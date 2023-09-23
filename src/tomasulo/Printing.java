
package tomasulo;
import java.util.*;
public class Printing {


    //Prints off the cycles in which each instruction is isCycled, Dispatched, and Written Back
    public void printInstInfo(Vector<Instruction> aq, int numInstr, int[] isCycle, int[] dispatch, int[] write)
    {
        System.out.println("_________________________________________________________________\n");
        System.out.println("Instruction        isCycle       Dispatch/EX     Write Back");
        //Does this operation for the total number of instructions
        for(int i = 0; i < numInstr; i++){
            //Case switch: Gets the opcode for each instruction and depending on the type it will print off the instruction and its cycles
            switch (aq.get(i).getOpCode()) {
                //Add instruction
                case 0:
                    System.out.print("Add R" + aq.get(i).getDestOp()+ ", R" + aq.get(i).getSourceOp1()+ ", R" + aq.get(i).getSourceOp2() + "       ");
                    System.out.print(isCycle[i] + "             ");//Print isCycle
                    System.out.print((dispatch[i]) + "              ");// Print Dispatch
                    System.out.println(write[i]); //Print Broadcast
                    break;
                //Subtraction instruction
                case 1:
                    System.out.print("Sub R" + aq.get(i).getDestOp()+ ", R" + aq.get(i).getSourceOp1()+ ", R" + aq.get(i).getSourceOp2() + "       ");
                    System.out.print(isCycle[i] + "            ");//Print isCycle
                    System.out.print((dispatch[i]) + "              ");// Print Dispatch
                    System.out.println(write[i]);//Print Broadcast
                    break;
                case 2:
                    System.out.print("Mul R" + aq.get(i).getDestOp()+ ", R" + aq.get(i).getSourceOp1()+ ", R" + aq.get(i).getSourceOp2() + "       ");
                    System.out.print(isCycle[i] + "             ");//Print isCycle
                    System.out.print((dispatch[i]) + "              ");// Print Dispatch
                    System.out.println(write[i]);//Print Broadcast
                    break;
                case 3:
                    System.out.print("Div R" + aq.get(i).getDestOp()+ ", R" + aq.get(i).getSourceOp1()+ ", R" + aq.get(i).getSourceOp2() + "       ");
                    System.out.print(isCycle[i] + "            ");//Print isCycle
                    System.out.print((dispatch[i]) + "              ");// Print Dispatch
                    System.out.println(write[i]);//Print Broadcast
                    break;
                case 4:
                    System.out.print("Load R" + aq.get(i).getDestOp()+"," + aq.get(i).getSourceOp1()+ "            ");
                    System.out.print(isCycle[i] + "            ");//Print isCycle
                    System.out.print((dispatch[i]) + "              ");// Print Dispatch
                    System.out.println(write[i]);//Print Broadcast
                    break;
                    case 5:
                    System.out.print("Store  " + aq.get(i).getDestOp()+ ", R" + aq.get(i).getSourceOp1()+ "                 ");
                    System.out.print(isCycle[i] + "            ");//Print isCycle
                    System.out.print((dispatch[i]) + "              ");// Print Dispatch
                    System.out.println(write[i]);//Print Broadcast
                    break;
                default:
                    break;
            }
        }
        System.out.println("_________________________________________________________________");
        System.out.println();
    }


    //Function to print the RF and RAT
    public void printRFRAT(Vector<Double> RF, Vector<String> RAT){

        System.out.println("   RF                   RAT");
        System.out.println("-----------------------------");
        for(int i = 0; i < 32; i++){
            int length = String.valueOf(RF.get(i)).length();
            if(length == 2){
                System.out.println("R" + i + ":     " + RF.get(i) + "      |        " + RAT.get(i) + "");
            }
            else
                System.out.println("R" + i + ":     " + RF.get(i) + "       |        " + RAT.get(i) + "");
        }
        System.out.println();

    }

    //Function to print off the Reservations stations
    public void printRS(ReservationStation rs1, ReservationStation rs2, ReservationStation rs3, ReservationStation rs4, ReservationStation rs5){
        System.out.println("_________________________________________________________________");
        System.out.println("       Busy     OP       VJ       VK       Qj       Qk       Disp");

        if(rs1.getVj().length() == 1 && rs1.getVk().length() == 1){
            System.out.println("RS" + 1 + "    " + rs1.getBusy() + "        "  + rs1.getOp() + "       " + rs1.getVj() + "        " + rs1.getVk() + "       " + rs1.getQj() + "      " + rs1.getQk() + "       " + rs1.getDisp());
        }
        else if(rs1.getVj().length() == 2 && rs1.getVk().length() == 1){
            System.out.println("RS" + 1 + "    " + rs1.getBusy() + "        "  + rs1.getOp() + "      " + rs1.getVj() + "        " + rs1.getVk() + "       " + rs1.getQj() + "      " + rs1.getQk() + "       " + rs1.getDisp());
        }
        else if(rs1.getVj().length() == 1 && rs1.getVk().length() == 2){
            System.out.println("RS" + 1 + "    " + rs1.getBusy() + "        "  + rs1.getOp() + "       " + rs1.getVj() + "       " + rs1.getVk() + "       " + rs1.getQj() + "      " + rs1.getQk() + "       " + rs1.getDisp());
        }
        else {
            System.out.println("RS" + 1 + "    " + rs1.getBusy() + "        "  + rs1.getOp() + "      " + rs1.getVj() + "       " + rs1.getVk() + "       " + rs1.getQj() + "      " + rs1.getQk() + "       " + rs1.getDisp());
        }

        if(rs2.getVj().length() == 1 && rs2.getVk().length() == 1){
            System.out.println("RS" + 2 + "    " + rs2.getBusy() + "        "  + rs2.getOp() + "       " + rs2.getVj() + "        " + rs2.getVk() + "       " + rs2.getQj() + "      " + rs2.getQk() + "       " + rs2.getDisp());
        }
        else if(rs2.getVj().length() == 2 && rs2.getVk().length() == 1){
            System.out.println("RS" + 2 + "    " + rs2.getBusy() + "        "  + rs2.getOp() + "      " + rs2.getVj() + "        " + rs2.getVk() + "       " + rs2.getQj() + "      " + rs2.getQk() + "       " + rs2.getDisp());
        }
        else if(rs2.getVj().length() == 1 && rs2.getVk().length() == 2){
            System.out.println("RS" + 2 + "    " + rs2.getBusy() + "        "  + rs2.getOp() + "       " + rs2.getVj() + "       " + rs2.getVk() + "       " + rs2.getQj() + "      " + rs2.getQk() + "       " + rs2.getDisp());
        }
        else {
            System.out.println("RS" + 2 + "    " + rs2.getBusy() + "        "  + rs2.getOp() + "      " + rs2.getVj() + "       " + rs2.getVk() + "       " + rs2.getQj() + "      " + rs2.getQk() + "       " + rs2.getDisp());
        }

        if(rs3.getVj().length() == 1 && rs3.getVk().length() == 1){
            System.out.println("RS" + 3 + "    " + rs3.getBusy() + "        "  + rs3.getOp() + "       " + rs3.getVj() + "        " + rs3.getVk() + "       " + rs3.getQj() + "      " + rs3.getQk() + "       " + rs3.getDisp());
        }
        else if(rs3.getVj().length() == 2 && rs3.getVk().length() == 1){
            System.out.println("RS" + 3 + "    " + rs3.getBusy() + "        "  + rs3.getOp() + "      " + rs3.getVj() + "        " + rs3.getVk() + "       " + rs3.getQj() + "      " + rs3.getQk() + "       " + rs3.getDisp());
        }
        else if(rs3.getVj().length() == 1 && rs3.getVk().length() == 2){
            System.out.println("RS" + 3 + "    " + rs3.getBusy() + "        "  + rs3.getOp() + "       " + rs3.getVj() + "       " + rs3.getVk() + "       " + rs3.getQj() + "      " + rs3.getQk() + "       " + rs3.getDisp());
        }
        else {
            System.out.println("RS" + 3 + "    " + rs3.getBusy() + "        "  + rs3.getOp() + "      " + rs3.getVj() + "       " + rs3.getVk() + "       " + rs3.getQj() + "      " + rs3.getQk() + "       " + rs3.getDisp());
        }

        if(rs4.getVj().length() == 1 && rs4.getVk().length() == 1){
            System.out.println("RS" + 4 + "    " + rs4.getBusy() + "        "  + rs4.getOp() + "       " + rs4.getVj() + "        " + rs4.getVk() + "       " + rs4.getQj() + "      " + rs4.getQk() + "       " + rs4.getDisp());
        }
        else if(rs4.getVj().length() == 2 && rs4.getVk().length() == 1){
            System.out.println("RS" + 4 + "    " + rs4.getBusy() + "        "  + rs4.getOp() + "      " + rs4.getVj() + "        " + rs4.getVk() + "       " + rs4.getQj() + "      " + rs4.getQk() + "       " + rs4.getDisp());
        }
        else if(rs4.getVj().length() == 1 && rs4.getVk().length() == 2){
            System.out.println("RS" + 4 + "    " + rs4.getBusy() + "        "  + rs4.getOp() + "       " + rs4.getVj() + "       " + rs4.getVk() + "       " + rs4.getQj() + "      " + rs4.getQk() + "       " + rs4.getDisp());
        }
        else {
            System.out.println("RS" + 4 + "    " + rs4.getBusy() + "        "  + rs4.getOp() + "      " + rs4.getVj() + "       " + rs4.getVk() + "       " + rs4.getQj() + "      " + rs4.getQk() + "       " + rs4.getDisp());
        }

        if(rs5.getVj().length() == 1 && rs5.getVk().length() == 1){
            System.out.println("RS" + 5 + "    " + rs5.getBusy() + "        "  + rs5.getOp() + "       " + rs5.getVj() + "        " + rs5.getVk() + "       " + rs5.getQj() + "      " + rs5.getQk() + "       " + rs5.getDisp());
        }
        else if(rs5.getVj().length() == 2 && rs5.getVk().length() == 1){
            System.out.println("RS" + 5 + "    " + rs5.getBusy() + "        "  + rs5.getOp() + "      " + rs5.getVj() + "        " + rs5.getVk() + "       " + rs5.getQj() + "      " + rs5.getQk() + "       " + rs5.getDisp());
        }
        else if(rs5.getVj().length() == 1 && rs5.getVk().length() == 2){
            System.out.println("RS" + 5 + "    " + rs5.getBusy() + "        "  + rs5.getOp() + "       " + rs5.getVj() + "       " + rs5.getVk() + "       " + rs5.getQj() + "      " + rs5.getQk() + "       " + rs5.getDisp());
        }
        else {
            System.out.println("RS" + 5 + "    " + rs5.getBusy() + "        "  + rs5.getOp() + "      " + rs5.getVj() + "       " + rs5.getVk() + "       " + rs5.getQj() + "      " + rs5.getQk() + "       " + rs5.getDisp());
        }

        System.out.println("_________________________________________________________________");
        System.out.println();
    }

    //Functions to print off the queue








}
