package tomasulo;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Vector;

public class GUI implements ActionListener {
    int cycle = 0;
    JLabel cycles = new JLabel("Cycle: " + cycle);
    JLabel error = new JLabel("Error");

    JFrame frame;
    JButton goCycle = new JButton("Go to cycle");
    JButton run = new JButton("Run");
    JButton next = new JButton("Next Cycle");
    JButton prev = new JButton("Previous Cycle");

    JButton newInstruction = new JButton("+ Add instruction");
    JButton lessInstruction = new JButton("+ Remove instruction");


    JTextField addLatencyText = new JTextField(20);
    JTextField subLatencyText = new JTextField(20);
    JTextField mulLatencyText = new JTextField(20);
    JTextField divLatencyText = new JTextField(20);
    JTextField loadLatencyText = new JTextField(20);
    JTextField storeLatencyText = new JTextField(20);




    JTextField gotoCycleField = new JTextField(20);

    Table instructionQueue;
    Table RAT;
    Table addStation;
    Table mulStations;
    Table loadBuffer;
    Table storeBuffer;
    Table memory;
    JTable RegisterFile;

    Vector<String> instructions = new Vector<>();
    ImagePanel panel;
    public GUI()
    {
        frame = new JFrame();
        BufferedImage myImage = null;
        try {
            myImage = ImageIO.read(new File("./src/tomasulo/background.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        frame.setSize(1500,800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new ImagePanel(myImage);
        frame.getContentPane().add(panel);

        panel.setLayout(null);

        error.setBounds(650,120,500,200);
        error.setFont(new Font("Serif", Font.BOLD, 24));
        error.setVisible(false);
        panel.add(error);

        //add latency
        JLabel addLatency = new JLabel("Add latency");
        addLatency.setBounds(580,50,100,25);
        panel.add(addLatency);
        addLatencyText.setBounds(650,50,50,25);
        panel.add(addLatencyText);

        //mul latency
        JLabel mulLatency = new JLabel("Multiply latency");
        mulLatency.setBounds(880,50,100,25);
        panel.add(mulLatency);
        mulLatencyText.setBounds(970,50,50,25);
        panel.add(mulLatencyText);

        //Sub latency
        JLabel subLatency = new JLabel("Sub latency");
        subLatency.setBounds(580,100,100,25);
        panel.add(subLatency);
        subLatencyText.setBounds(650,100,50,25);
        panel.add(subLatencyText);

        //Div latency
        JLabel divLatency = new JLabel("Divide latency");
        divLatency.setBounds(880,100,100,25);
        panel.add(divLatency);
        divLatencyText.setBounds(970,100,50,25);
        panel.add(divLatencyText);

        //Load latency
        JLabel loadLatency = new JLabel("Load latency");
        loadLatency.setBounds(575,150,100,25);
        panel.add(loadLatency);
        loadLatencyText.setBounds(650,150,50,25);
        panel.add(loadLatencyText);

        //store latency
        JLabel storeLatency = new JLabel("Store latency");
        storeLatency.setBounds(880,150,100,25);
        panel.add(storeLatency);
        storeLatencyText.setBounds(970,150,50,25);
        panel.add(storeLatencyText);


        run.setBounds(725,250,150,25);

        run.addActionListener(this);

        panel.add(run);
        //next cycle button
        next.setBounds(725,290,150,25);

        next.addActionListener(this);

        panel.add(next);

        prev.setBounds(550,290,150,25);
        prev.addActionListener(this);

        panel.add(prev);

        //go to cycle button
        goCycle.setBounds(900,290,150,25);
        goCycle.addActionListener(this);
        panel.add(goCycle);
        gotoCycleField.setBounds(1070,290,50,25);
        panel.add(gotoCycleField);

        JLabel message1 = new JLabel("Please enter the instructions in this table in order of execution:");
        message1.setBounds(10,-80,500,200);
        message1.setFont(new Font("Serif", Font.BOLD, 18));
        panel.add(message1);

        JLabel message2 = new JLabel("Enter instructions in 'Inst', 'Dest', 'J', 'K' fields");
        message2.setBounds(10,-40,500,200);
        message2.setFont(new Font("Serif", Font.BOLD, 18));
        panel.add(message2);

        JLabel message3 = new JLabel("If it is load/store, type in 'inst', 'Dest', 'J' only.");
        message3.setBounds(10,0,500,200);
        message3.setFont(new Font("Serif", Font.BOLD, 18));
        panel.add(message3);

        //draw Instruction Queue table
        String[] InstructionColumnNames = {"Instruction", "Dest",
                "J",
                "K",
                "Issue", "Execute",
                "Write Result"};


        newInstruction.setBounds(10,120,200,25);
        newInstruction.addActionListener(this);
        panel.add(newInstruction);
        lessInstruction.setBounds(220,120,200,25);
        lessInstruction.addActionListener(this);
        panel.add(lessInstruction);


        int[]instructionQueueBounds = {10,150,530,200};
        instructionQueue =  drawTable(InstructionColumnNames,1,instructionQueueBounds,"");
        instructionQueue.setBorder(BorderFactory.createEmptyBorder(0, 10, 80, 0));

        System.out.println(instructionQueue.getModel().getValueAt(0, 0));


       RegisterFile = drawRegFile();

        String[] RATColumnNames = {"Content"};
        int[]RATBounds = {1350,0,100,1000};
        RAT = drawTable(RATColumnNames,32,RATBounds,"F");
        RAT.table.setRowHeight(23);
        cycles.setBounds(550,550,200,200);
        cycles.setFont(new Font("Serif", Font.BOLD, 34));
        panel.add(cycles);


        //drawing loadBuffer
        String[] loadBuffersColumnNames = {"Station","Busy",
                "Address",
        };
        int[] loadBuffersBounds = {50,380,200,175};
        loadBuffer = drawTable(loadBuffersColumnNames,5,loadBuffersBounds, "L");

        //drawing loadBuffer
        String[] memoryColumns = {"Address","Content"
        };
        int[] memoryColumnBounds = {450,380,200,175};
        memory = drawTable(memoryColumns,1000,memoryColumnBounds, "Mem");
        memory.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        String[] storeBuffersColumnNames = {"Station","Busy",
                "Address","Vj","Qj"
        };
        int[] storeBuffersBounds = {850,380,300,175};
        storeBuffer = drawTable(storeBuffersColumnNames,5,storeBuffersBounds, "S");


        //drawing add stations
        String[] addStationColumnNames = {"Station","Busy","Op","Vj","Vk","Qj","Qk"};
        int[] addStationBounds = {50,600,350,120};
        addStation = drawTable(addStationColumnNames,3,addStationBounds,"A");

        //drawing mul stations
        String[] mulStationColumnNames = {"Station","Busy","Op","Vj","Vk","Qj","Qk"};
        int[] mulStationBounds = {800,600,350,90};
        mulStations = drawTable(mulStationColumnNames,2,mulStationBounds, "M");
        frame.setVisible(true);


        //drawing labels of the tables
        JLabel loadBufferLabel = new JLabel("Load Stations");
        loadBufferLabel.setBounds(50,265,500,200);
        loadBufferLabel.setFont(new Font("Serif", Font.BOLD, 16));
        panel.add(loadBufferLabel);

        JLabel storeBufferLabel = new JLabel("Store Stations");
        storeBufferLabel.setBounds(850,265,500,200);
        storeBufferLabel.setFont(new Font("Serif", Font.BOLD, 16));
        panel.add(storeBufferLabel);

        JLabel addBufferLabel = new JLabel("Add Stations");
        addBufferLabel.setBounds(50,485,500,200);
        addBufferLabel.setFont(new Font("Serif", Font.BOLD, 16));
        panel.add(addBufferLabel);

        JLabel mulBufferLabel = new JLabel("Multiply Stations");
        mulBufferLabel.setBounds(800,485,500,200);
        mulBufferLabel.setFont(new Font("Serif", Font.BOLD, 16));
        panel.add(mulBufferLabel);

        JLabel memoryLabel = new JLabel("Memory");
        memoryLabel.setBounds(450,265,500,200);
        memoryLabel.setFont(new Font("Serif", Font.BOLD, 16));
        panel.add(memoryLabel);

        JLabel message4 = new JLabel("**Valid instructions: ADD, SUB, MUL, DIV, L.D, S.D**");
        message4.setBounds(10,640,800,200);
        message4.setFont(new Font("Serif", Font.ITALIC, 20));
        panel.add(message4);
    }

    public Table drawTable(String[] columnNames, int rowsNumber, int[] bounds, String stationType)
    {

        String[][] tableData = new String[rowsNumber][columnNames.length];

        for(int i=0; i<rowsNumber; i++) {
            switch (stationType)
            {
                case "": tableData[i][0]= ""; break;
                case "A": tableData[i][0]= "RS"+(i+1); break;
                case "M": tableData[i][0]= "RS"+(i+4); break;
                case "L": tableData[i][0]= "L"+(i); break;
                case "S": tableData[i][0]= "S"+(i); break;
                case "Mem": tableData[i][0]= i+""; break;


            }
//            tableData[i][0] = stationType.equals("")?"":stationType+i;
            for (int j = 1; j < tableData[0].length; j++)
                tableData[i][j] = "";
        }

//        panel.add(loadBufferLabel);
        Table table = new Table(tableData, columnNames,bounds);
        table.setBounds(bounds[0],bounds[1],bounds[2],bounds[3]);



//        table.setBackground(Color.CYAN);
        panel.add(table);
        return table;
    }

    public JTable drawRegFile()
    {
        String[] RegFileColumnNames = {"Reg",
                "Qi",
        };
        int[] RegFileBounds = {1200,5,100,1000};
        Object[][] tableData = new Object[33][RegFileColumnNames.length];
        tableData[0]=RegFileColumnNames;

        for(int i=1; i<33; i++) {
            tableData[i][0]="F"+ (i-1);
            for (int j = 1; j < tableData[0].length; j++)
                tableData[i][j] = 0;
        }
        JLabel RegFileLabel = new JLabel("Register File");
        RegFileLabel.setBounds(RegFileBounds[0],RegFileBounds[1]-100,RegFileBounds[2],RegFileBounds[3]);
//        panel.add(loadBufferLabel);
        JTable RegFile = new JTable(tableData, RegFileColumnNames);
        RegFile.setBounds(RegFileBounds[0],RegFileBounds[1],RegFileBounds[2],RegFileBounds[3]);
        RegFile.setRowHeight(23);



//        RegFile.setRowHeight(15);

//        table.setBackground(Color.CYAN);
        panel.add(RegFile);
        return RegFile;
    }

    public void getInstructions()
    {
        instructions=new Vector<>();
        for(int i=0; i<instructionQueue.getModel().getRowCount(); i++)
        {
            String op = (String) instructionQueue.getModel().getValueAt(i, 0);
            if(op.equals(""))
                break;
            String dest = (String) instructionQueue.getModel().getValueAt(i, 1);
            dest = dest.substring(1,dest.length());
            String j = (String) instructionQueue.getModel().getValueAt(i, 2);
            String k = "";
            if(!op.equals("L.D") && !op.equals("S.D"))
            {
                j = j.substring(1,j.length());
                k = (String) instructionQueue.getModel().getValueAt(i, 3);
                k = k.substring(1,k.length());
            }
            switch (op)
            {
                case "ADD":
                    op="0";
                    break;
                case "SUB":
                    op="1";
                    break;
                case "MUL":
                    op="2";
                    break;
                case "DIV":
                    op="3";
                    break;
                case "L.D":
                    op="4";
                    break;
                case "S.D":
                    op="5";
                    break;
                default:
                {
                    error.setVisible(true);
                    error.setText("Invalid Instructions");
                    return;
                }

            }
            instructions.add(op + " " + dest + " " + j + " "+ k);
        }
        System.out.println(instructions.toString());
    }
    public static void main(String[] args) {
        try {
            System.out.println(new File(".").getCanonicalPath());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        new GUI();
    }

    @Override
    public void actionPerformed(ActionEvent e) {


        if(e.getSource()==newInstruction) {
            DefaultTableModel model = (DefaultTableModel) instructionQueue.getModel();
            model.addRow(new Object[]{"", "", "", "", ""});
        }
        else if(e.getSource()==lessInstruction) {
            DefaultTableModel model = (DefaultTableModel) instructionQueue.getModel();
            model.removeRow(model.getRowCount()-1);
        }
        else if(e.getSource()==run) {
                getInstructions();
                try {
                    int addLatency = Integer.parseInt(addLatencyText.getText().toString());
                    int mulLatency = Integer.parseInt(mulLatencyText.getText().toString());
                    int subLatency = Integer.parseInt(subLatencyText.getText().toString());
                    int divLatency = Integer.parseInt(divLatencyText.getText().toString());
                    int loadLatency = Integer.parseInt(loadLatencyText.getText().toString());
                    int storeLatency = Integer.parseInt(storeLatencyText.getText().toString());
                    if(addLatency<0 || mulLatency<0 || subLatency<0 || divLatency<0 || loadLatency<0 || storeLatency<0)
                        throw new Exception();
                    Algo.runAlgorithm(instructions,addLatency,mulLatency,divLatency,subLatency,loadLatency,storeLatency);

                }catch(Exception ex)
                {
                    error.setText("Please enter valid latencies!");
                    error.setVisible(true);
                }

//                Algo.runAlgorithm(instructions,addLatency,mulLatency,divLatency,subLatency,loadLatency,storeLatency);

            cycles.setText("Cycle: " + 1);
                cycle=1;
            setRAT();
            setAddRS();
            setMulRS();
            setRegisterFile();
            setLoadBuffer();
            setStoreBuffer();
            setMemory();
            clearInstructionQueue();
            setInstructionQueue();
            System.out.println("SUIII" + Algo.RATsToBeStored.size());
        }
        else if(e.getSource()==next)
        {
            if(cycle==0)
            {
                getInstructions();
                try {
                    int addLatency = Integer.parseInt(addLatencyText.getText().toString());
                    int mulLatency = Integer.parseInt(mulLatencyText.getText().toString());
                    int subLatency = Integer.parseInt(subLatencyText.getText().toString());
                    int divLatency = Integer.parseInt(divLatencyText.getText().toString());
                    int loadLatency = Integer.parseInt(loadLatencyText.getText().toString());
                    int storeLatency = Integer.parseInt(storeLatencyText.getText().toString());
                    if(addLatency<0 || mulLatency<0 || subLatency<0 || divLatency<0 || loadLatency<0 || storeLatency<0)
                        throw new Exception();
                    Algo.runAlgorithm(instructions,addLatency,mulLatency,divLatency,subLatency,loadLatency,storeLatency);

                }catch(Exception ex)
                {
                    error.setText("Please enter valid latencies!");
                    error.setVisible(true);
                }

//                Algo.runAlgorithm(instructions,addLatency,mulLatency,divLatency,subLatency,loadLatency,storeLatency);

            }

            if(cycle>=Algo.RegisterFilesToBeStored.size())
                return;
            cycles.setText("Cycle: " + ++cycle);
            setRAT();
            setAddRS();
            setMulRS();
            setRegisterFile();
            setLoadBuffer();
            setStoreBuffer();
            setMemory();
            setInstructionQueue();


        }
        else if(e.getSource()==prev)
        {
            if(cycle==1 || cycle==0)
                return;
            cycles.setText("Cycle: " + --cycle);
            setRAT();
            setAddRS();
            setMulRS();
            setRegisterFile();
            setLoadBuffer();
            setStoreBuffer();
            setMemory();
            clearInstructionQueue();
            setInstructionQueue();


        }
        else if(e.getSource()==goCycle)
        {
            if(cycle==0)
            {
                getInstructions();
                try {
                    int addLatency = Integer.parseInt(addLatencyText.getText().toString());
                    int mulLatency = Integer.parseInt(mulLatencyText.getText().toString());
                    int subLatency = Integer.parseInt(subLatencyText.getText().toString());
                    int divLatency = Integer.parseInt(divLatencyText.getText().toString());
                    int loadLatency = Integer.parseInt(loadLatencyText.getText().toString());
                    int storeLatency = Integer.parseInt(storeLatencyText.getText().toString());
                    Algo.runAlgorithm(instructions,addLatency,mulLatency,divLatency,subLatency,loadLatency,storeLatency);

                }catch(Exception ex)
                {
                    error.setText("Please enter valid latencies!");
                    error.setVisible(true);
                }
            }
            try {
                cycle=Integer.parseInt(gotoCycleField.getText());

            }
            catch(Exception ex)
            {
                error.setText("Invalid Cycle");
                error.setVisible(true);
                return;
            }
            if(cycle<1 || cycle>Algo.RATsToBeStored.size())
            {
                error.setText("Invalid Cycle");
                error.setVisible(true);
                return;
            }
            cycles.setText("Cycle: " + cycle);
            setRAT();
            setAddRS();
            setMulRS();
            setLoadBuffer();
            setStoreBuffer();
            setMemory();
            clearInstructionQueue();
            setInstructionQueue();



        }
    }
    public void clearInstructionQueue()
    {
        for(int i=0; i<instructionQueue.getModel().getRowCount(); i++)
        {
            instructionQueue.getModel().setValueAt("",i,4);
            instructionQueue.getModel().setValueAt("",i,5);
            instructionQueue.getModel().setValueAt("",i,6);


        }
    }
    public void setAddRS()
    {
        Vector <ReservationStation> ReservationStationsToBeStored=Algo.ReservationStationsToBeStored;
        for(int i=0; i<3  /*RAT.getModel().getRowCount()*/; i++)
        {
            addStation.getModel().setValueAt(ReservationStationsToBeStored.get((cycle-1)*5+i).Busy,i,1);
            addStation.getModel().setValueAt(ReservationStationsToBeStored.get((cycle-1)*5+i).Op,i,2);
            addStation.getModel().setValueAt(ReservationStationsToBeStored.get((cycle-1)*5+i).Vj,i,3);
            addStation.getModel().setValueAt(ReservationStationsToBeStored.get((cycle-1)*5+i).Vk,i,4);
            addStation.getModel().setValueAt(ReservationStationsToBeStored.get((cycle-1)*5+i).Qj,i,5);
            addStation.getModel().setValueAt(ReservationStationsToBeStored.get((cycle-1)*5+i).Qk,i,6);

        }
    }
    public void setMulRS()
    {
        Vector <ReservationStation> ReservationStationsToBeStored=Algo.ReservationStationsToBeStored;
        for(int i=0; i<2  /*RAT.getModel().getRowCount()*/; i++)
        {
            mulStations.getModel().setValueAt(ReservationStationsToBeStored.get((cycle-1)*5+3+i).Busy,i,1);
            mulStations.getModel().setValueAt(ReservationStationsToBeStored.get((cycle-1)*5+3+i).Op,i,2);
            mulStations.getModel().setValueAt(ReservationStationsToBeStored.get((cycle-1)*5+3+i).Vj,i,3);
            mulStations.getModel().setValueAt(ReservationStationsToBeStored.get((cycle-1)*5+3+i).Vk,i,4);
            mulStations.getModel().setValueAt(ReservationStationsToBeStored.get((cycle-1)*5+3+i).Qj,i,5);
            mulStations.getModel().setValueAt(ReservationStationsToBeStored.get((cycle-1)*5+3+i).Qk,i,6);
        }
    }
    public void setRAT()
    {
        Vector <Vector<String>>RATsToBeStored=Algo.RATsToBeStored;

        for(int i=0; i<32  /*RAT.getModel().getRowCount()*/; i++)
        {
            RAT.getModel().setValueAt(RATsToBeStored.get(cycle-1).get(i),i,0);
        }
    }

    public void setRegisterFile()
    {
        Vector <Vector<Integer>>RegisterFilesToBeStored=Algo.RegisterFilesToBeStored;
        for(int i=1; i<33  /*RAT.getModel().getRowCount()*/; i++)
        {
            RegisterFile.getModel().setValueAt(RegisterFilesToBeStored.get(cycle-1).get(i-1),i,1);
        }
    }

    public void setLoadBuffer()
    {
        Vector <LoadBuffer> loadBuffers=Algo.loadBuffers;
        for(int i=0; i<5  /*RAT.getModel().getRowCount()*/; i++)
        {
            loadBuffer.getModel().setValueAt(loadBuffers.get((cycle-1)*5+i).busy,i,1);
            loadBuffer.getModel().setValueAt(loadBuffers.get((cycle-1)*5+i).address,i,2);

        }


    }
    public void setStoreBuffer()
    {
        Vector <StoreBuffer> storeBuffers=Algo.storeBuffers;
        for(int i=0; i<5  /*RAT.getModel().getRowCount()*/; i++)
        {
            storeBuffer.getModel().setValueAt(storeBuffers.get((cycle-1)*5+i).busy,i,1);
            storeBuffer.getModel().setValueAt(storeBuffers.get((cycle-1)*5+i).address,i,2);
            storeBuffer.getModel().setValueAt(storeBuffers.get((cycle-1)*5+i).vj,i,3);
            storeBuffer.getModel().setValueAt(storeBuffers.get((cycle-1)*5+i).qj,i,4);

        }

    }
    public void setInstructionQueue(){
         int[] isCycle = Algo.isCycle;
         int[] exCycle = Algo.exCycle;
         int[] writeCycle=Algo.writeCycle;
         for(int i=0; i<isCycle.length; i++)
         {
             if(isCycle[i]<=cycle)
                 instructionQueue.getModel().setValueAt(isCycle[i],i,4);
             if(exCycle[i]<=cycle)
                instructionQueue.getModel().setValueAt(exCycle[i],i,5);
             if(writeCycle[i]<=cycle)
                instructionQueue.getModel().setValueAt(writeCycle[i],i,6);
         }
    }
    public void setMemory()
    {
        double []memory = Algo.memoryVersions.get(cycle-1);
        System.out.println("HHHHHH"+ memory[cycle-1]);
        for(int i=0; i<this.memory.getModel().getRowCount(); i++)
        {
            this.memory.getModel().setValueAt(memory[i],i,1);
        }
    }
}
