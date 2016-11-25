/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seapi;

import com.pi4j.wiringpi.Spi;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import static seapi.SeaPIMainFrame.SPI_WRITE_CMD;
import static seapi.SeaPIMainFrame.log;
import java.util.Timer;
import static seapi.SeaPIMainFrame.SPI_READ_CMD;
/**
 *
 * @author jorge
 */
public class StateMachineController {
    //states
    final static int        SEAPI_STATE_INIT    =   100;
    final static int        SEAPI_STATE_TX      =   101;
    final static int        SEAPI_STATE_RX      =   102;
    final static int        SEAPI_STATE_IDLE    =   103;
    final static int        SEAPI_STATE_EXIT    =   110;
    
    //events
    final static int        SEAPI_EVENT_PKT_RCV             =   1000;
    final static int        SEAPI_EVENT_TIMER_CTL           =   1001;
    final static int        SEAPI_EVENT_INIT_DONE           =   1002;
    final static int        SEAPI_EVENT_START               =   1003;
    final static int        SEAPI_EVENT_RCV_EXPIRED         =   1004;
    final static int        SEAPI_EVENT_TIMER_STATUS_DATA   =   1005;
    final static int        SEAPI_EVENT_BUTTON_PRESSED      =   2000;
    final static int        SEAPI_EVENT_READ_TEMP           =   1006;
    //vars
    //private volatile SeaPIMainFrame mainPtr;
    private volatile SeaPI mainPtr;
    private int current_state;
    private ControllerData  myControlData;
    private Timer           control_data_timer;
    private Timer           rcvExpiredTimer;
    private Timer           tempReadTimer;
    List<byte[]>            pendingTransmitBytes;
    private int             control_mode;
    private RFPacketPendingThread   rcvPktThread;
    //StateMachineController(int mode,ControllerData in_dataCntlr,SeaPIMainFrame mptr){
      StateMachineController(int mode,ControllerData in_dataCntlr,SeaPI mptr){
        mainPtr = mptr;
        control_mode = mode;
        current_state   =   SEAPI_STATE_INIT;
        myControlData   = in_dataCntlr;
        //make sure timers are all off at start
        //clear any counters
        pendingTransmitBytes = new ArrayList();
        
        //schedule tasks as needed
        control_data_timer = new Timer();
        rcvExpiredTimer = new Timer();
        tempReadTimer   =   new Timer();
       
        Thread a = new Thread();
        rcvPktThread = new RFPacketPendingThread(this);
    }
    public int getState()
    {
        return this.current_state;
    }
    private String stateToString(int state)
    {
       String statestr;
        switch(state)
        {
            case SEAPI_STATE_INIT:
                statestr= "INIT";
                break;
            case SEAPI_STATE_IDLE:
                statestr="IDLE";
                break;
            case SEAPI_STATE_TX:
                statestr="TX";
                break;
            case SEAPI_STATE_RX:
                statestr="RX";
                break;
            case SEAPI_STATE_EXIT:
                statestr="EXIT";
                break;
            default:
                statestr="unknown";
        }
        return statestr;
    }
    
    public void processEvent(int event)
    {
        
        
        if(control_mode==1)
        {
            //call the current state process event
            switch(current_state)
            {
                case SEAPI_STATE_INIT:
                    current_state   =   state_init(event,current_state);
                    break;
                case SEAPI_STATE_TX:
                    current_state   =   state_tx(event,current_state);
                    break;    
                case SEAPI_STATE_RX:
                    current_state   =   state_rx(event,current_state);
                    break;        
                case SEAPI_STATE_IDLE:
                    current_state   =   state_idle(event,current_state);
                    break;        
                default:
                       //do nothing

            }
        }
        else
        {
            //call the current state process event
            switch(current_state)
            {
                case SEAPI_STATE_INIT:
                    current_state   =   state_init_vehicle(event,current_state);
                    break;
                case SEAPI_STATE_TX:
                    current_state   =   state_tx_vehicle(event,current_state);
                    break;    
                case SEAPI_STATE_RX:
                    current_state   =   state_rx_vehicle(event,current_state);
                    break;        
                case SEAPI_STATE_IDLE:
                    current_state   =   state_idle_vehicle(event,current_state);
                    break;        
                default:
                       //do nothing

            }
        }
        
    }
    
    public  int state_init(int event,int current_state)
    {
        //System.out.println("DEBUG: Got EVENT: "+String.valueOf(event)+" State: "+stateToString(current_state));
        int next_state = current_state;
        
        switch(event)
        {
            case SEAPI_EVENT_INIT_DONE:
                next_state = SEAPI_STATE_IDLE;
                //start timer to send data
                //this.control_data_timer = new Timer();
                control_data_timer.purge();
                
                break;
                
            case SEAPI_EVENT_PKT_RCV:
                //Not sure what to do in this case...
                next_state = SEAPI_STATE_TX;
                break;
            default:
                //do nada!!!
        }
        
        return next_state;
    }
    public  int state_init_vehicle(int event,int current_state)
    {
        //System.out.println("DEBUG vehicle: Got EVENT: "+String.valueOf(event)+" State: "+stateToString(current_state));
        int next_state = current_state;
        
        switch(event)
        {
            case SEAPI_EVENT_INIT_DONE:
                next_state = SEAPI_STATE_IDLE;
                
                break;
                
            default:
                //do nada!!!
        }
        
        return next_state;
    }
    public  int state_tx(int event,int current_state)
    {
        //System.out.println("DEBUG: Got EVENT: "+String.valueOf(event)+" State: "+stateToString(current_state));
        int next_state = current_state;
        
        switch(event)
        {
            
            default:
                //do nada!!!
        }
        
        return next_state;
    }
    public  int state_tx_vehicle(int event,int current_state)
    {
        //System.out.println("DEBUG vehicle: Got EVENT: "+String.valueOf(event)+" State: "+stateToString(current_state));
        int next_state = current_state;
        
        switch(event)
        {
            
            default:
                //do nada!!!
        }
        
        return next_state;
    }
    public  int state_rx(int event,int current_state)
    {
        //System.out.println("DEBUG: Got EVENT: "+String.valueOf(event)+" State: "+stateToString(current_state));
        int next_state = current_state;
        
        switch(event)
        {
            case SEAPI_EVENT_RCV_EXPIRED:
                //check for rcvd packet via spi rfm
                if(this.rcvPending())
                {
                    //get message
                    byte[] indata = rcvMessage();
                    //TODO: what do we do with incoming data???
                    //TODO: process the pckt..
                }
                control_data_timer.schedule(new ControlDataTimerTask(this),50);
                next_state = SEAPI_STATE_IDLE;
                break;
            case SEAPI_EVENT_TIMER_CTL:
                break;
            case SEAPI_EVENT_READ_TEMP:
                //get and display temp
                byte[] spipacket = new byte[2];
                spipacket[0] = (byte)(0x0f|SeaPIMainFrame.SPI_WRITE_CMD);
                spipacket[1] = (byte) 0x80;
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0, spipacket,2);
                
                spipacket[0] = (byte)(0x11|SeaPIMainFrame.SPI_READ_CMD);
                spipacket[1] = (byte) 0x00;
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0, spipacket,2);
                
                System.out.println("Temp is: "+Byte.toString(spipacket[1]));
                tempReadTimer.schedule(new ReadTempTimerTask(this),10000);
                break;
            default:
                //do nada!!!
        }
        
        return next_state;
    }
    public  int state_rx_vehicle(int event,int current_state)
    {
        //DateFormat df = new SimpleDateFormat("HH:mm:ss");
        //Date today = Calendar.getInstance().getTime(); 
        //System.out.println("DEBUG vehicle: Got EVENT: "+String.valueOf(event)+" State: "+stateToString(current_state));
        int next_state = current_state;
        
        switch(event)
        {
            
            case SEAPI_EVENT_TIMER_STATUS_DATA:
                
                //Grab data from data control for analog controls
                //grab button info
                // this.control_data_timer = new Timer();
                control_data_timer.schedule(new ControlDataTimerTask(this),100);
                break;
            case SEAPI_EVENT_PKT_RCV:
                //kill thread if running
                this.rcvPktThread.abort();
                
                //get packet
                byte[] data = this.rcvMessage();
                if(data!=null)
                {
                    //grab RSSI
                    //read reg 26
                    byte[] packet = new byte[2];
                    packet[0] = (byte)(0x26|SeaPIMainFrame.SPI_READ_CMD);
                    packet[1] = 0;
                    Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
                    int rssi = packet[1];
                    
                    //System.out.println(df.format(today)+" RSSI "+String.valueOf(rssi)+" Msg: "+Arrays.toString(data));
                    //what do we do do with rcvd msg????
                    mainPtr.processMsg(data);
                    
                }
                //Send any needed data here
                
                next_state = SEAPI_STATE_RX;
                rcvPktThread = new RFPacketPendingThread(this);
                this.rcvPktThread.start();
                break;
            default:
                //do nada!!!
        }
        
        return next_state;
    }
    public  int state_idle(int event,int current_state)
    {
        //System.out.println("DEBUG: Got EVENT: "+String.valueOf(event)+" State: "+stateToString(current_state));
        int next_state = current_state;
        
        switch(event)
        {
            case SEAPI_EVENT_START:
                //Send one packet
                 sendPacket(myControlData.getAnalogMessage());
                //set wait timer
                //rcvExpiredTimer = new Timer();
                rcvExpiredTimer.schedule(new RcvWaitTimerTask(this),50);
                this.tempReadTimer.schedule(new ReadTempTimerTask(this), 10000);
                next_state = SEAPI_STATE_RX;
                break;
            case SEAPI_EVENT_TIMER_CTL:
                
                //need to send controll data
                //Grab data from data control for analog controls
                //grab button info
                sendPacket(myControlData.getAnalogMessage());
                //this.pendingTransmitBytes.add(myControlData.getAnalogMessage());
                //this.pendingTransmitBytes.add(myControlData.getButtonMessage());
               // this.control_data_timer = new Timer();
                //control_data_timer.schedule(new ControlDataTimerTask(this),100);
                
                //set the rcv timeout timer
                rcvExpiredTimer.schedule(new RcvWaitTimerTask(this),20);
                
                //then go to state RX
                next_state = SEAPI_STATE_RX;
                break;
            case SEAPI_EVENT_READ_TEMP:
                //get and display temp
                byte[] spipacket = new byte[2];
                spipacket[0] = (byte)(0x0f|SeaPIMainFrame.SPI_WRITE_CMD);
                spipacket[1] = (byte) 0x80;
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0, spipacket,2);
                
                spipacket[0] = (byte)(0x11|SeaPIMainFrame.SPI_READ_CMD);
                spipacket[1] = (byte) 0x00;
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0, spipacket,2);
                short temp = (short)((0x00ff&spipacket[1])-40);
                System.out.println("Temp is: "+String.valueOf(temp));
                tempReadTimer.schedule(new ReadTempTimerTask(this),10000);
                break;
            default:
                //do nada!!!
        }
        
        return next_state;
    }
    public  int state_idle_vehicle(int event,int current_state)
    {
        //System.out.println("DEBUG vehicle: Got EVENT: "+String.valueOf(event)+" State: "+stateToString(current_state));
        int next_state = current_state;
        
        switch(event)
        {
            case SEAPI_EVENT_START:
                next_state = SEAPI_STATE_RX;
                this.rcvPktThread.start();
                break;
            case SEAPI_EVENT_TIMER_STATUS_DATA:
                //need to send status data
                //TODO
                
                
                break;
            
            default:
                //do nada!!!
        }
        
        return next_state;
    }
    private int sendPacket(byte[] inpacket)
    {
        if(inpacket==null)
            return -1;
        //System.out.println("Msgs in queue: "+String.valueOf(pendingTransmitBytes.size()));
        if(inpacket.length>0)
        {
            
            if(inpacket.length>64)
            {
                System.out.println("Msg too long, deleting: "+Arrays.toString(inpacket));
            }
            else
            {
                txMessage(inpacket);
            }
            //System.out.println("After Msgs in queue: "+String.valueOf(pendingTransmitBytes.size()));
        }
        return 0;
    }
    private int sendBufferedPacket()
    {
        byte[] tempbytes;
        //System.out.println("Msgs in queue: "+String.valueOf(pendingTransmitBytes.size()));
        if(pendingTransmitBytes.size()>0)
        {
            tempbytes = this.pendingTransmitBytes.remove(0);
            if(tempbytes.length>64)
            {
                System.out.println("Msg too long, deleting: "+Arrays.toString(tempbytes));
            }
            else
            {
                txMessage(tempbytes);
            }
            //System.out.println("After Msgs in queue: "+String.valueOf(pendingTransmitBytes.size()));
        }
        return 0;
    }
    private int txMessage(byte[] msgbytes)
    {
        byte packet[] = new byte[2];
        //DateFormat df = new SimpleDateFormat("HH:mm:ss");
       // Date today = Calendar.getInstance().getTime(); 
        
        //System.out.println(df.format(today)+" Sending: "+Arrays.toString(msgbytes));
        //set to tx off
        packet[0] = (byte)(0x07|SPI_WRITE_CMD);
        packet[1] = (byte)0x03;//tx off,rx off, pll on xton
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        
        //Clear interrupt reg first 3 &4
        //Done by just reading them
        packet[0]   =   (byte)(0x03|SPI_READ_CMD);
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //System.out.println("Reg 3: "+Arrays.toString(packet));
        
        packet[0]   =   (byte)(0x04|SPI_READ_CMD);
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //System.out.println("Reg 4: "+Arrays.toString(packet));
        //System.out.println("Before Reg 3: "+Arrays.toString(packet));
        
        //clear tx fifo and errors
        //clear fifos
//first get value from 8
        packet[0]   =   (byte)(0x08|SPI_READ_CMD);
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);

        
        //now just change clr tx fifo and no autotx
        //toggle bit 1 then 0 to clear fifo
        packet[0]   =   (byte)(0x08|SPI_WRITE_CMD);
        packet[1]   =   (byte)(0x01|packet[1]);
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //now set ffcltrx(bit0) to zero again
        packet[0]   =   (byte)(0x08|SPI_WRITE_CMD);
        packet[1]   =   (byte) (0xfe&packet[1]);
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
          
        
        //transfer data to new byte buffer for spi
        byte spiPacket[] = new byte[msgbytes.length+1];
        
        //write cmd to fifo (0x7F|0x80)
        spiPacket[0] = (byte)0xFF;
        //copy data over
        System.arraycopy(msgbytes, 0, spiPacket, 1, msgbytes.length);   
            
        //write to fifo    
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,spiPacket,spiPacket.length);
        
        //set pkt length
        packet[0]   =   (byte)(0x3e|SPI_WRITE_CMD);
        packet[1]   =   (byte) msgbytes.length;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //System.out.println("Set Tx pkt length to "+String.valueOf(msgbytes.length));
            
        
       //set to tx on
        packet[0] = (byte)(0x07|SPI_WRITE_CMD);
        packet[1] = (byte)0x0B;//tx on, pll on xton
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        
         
       //Do we wait for the packet to get sent?
        try{
            Thread.sleep(10);
            //Check reg for valid sent
            packet[0]   =   (byte)(0x03|SPI_READ_CMD);
            Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
            byte result = (byte)(0x04&packet[1]);
            
            if(result!=4)
            {
                System.out.println("Sleeping 30msec again since no pktsent interupt...for debug");
                Thread.sleep(10);
                //Check reg for valid sent
                packet[0]   =   (byte)(0x03|SPI_READ_CMD);
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
                result = (byte)(0x04&packet[1]);
                
            }
    
            if(result!=4)
            {
                
                
                System.out.println("Reg 3 (Interrupt Status 1): "+Arrays.toString(packet));
                //print some debug status info
                //Dev Status
                packet[0]   =   (byte)(0x02|SPI_READ_CMD);
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
                System.out.println("Reg 2(Device Status): "+Arrays.toString(packet));
                packet[0]   =   (byte)(0x04|SPI_READ_CMD);
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
                System.out.println("Reg 4(Interrupt Status 2): "+Arrays.toString(packet));
                packet[0]   =   (byte)(0x07|SPI_READ_CMD);
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
                System.out.println("Reg 7 (Op & Fcn Ctl 1): "+Arrays.toString(packet));
                packet[0]   =   (byte)(0x08|SPI_READ_CMD);
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
                System.out.println("Reg 8 (Op & Fcn Ctl 2): "+Arrays.toString(packet));
                packet[0]   =   (byte)(0x3E|SPI_READ_CMD);
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
                System.out.println("Reg 3E (Tx Pkt Length): "+Arrays.toString(packet));
                
                System.out.println("(Msg Bytes Array): "+Arrays.toString(msgbytes));
                
                //Lets try forcing back to rxon and clearing tx fifo?
            }
            
        }catch(Exception e){
            System.out.println(e.getMessage());

        }
       
        
        return 0;
    }
    
    private boolean rcvPending()
    {
        boolean packet_rcvd = false;
        byte packet[] = new byte[2];
        //look for valid pkt register
        packet[0] = (byte)(0x03|SeaPIMainFrame.SPI_READ_CMD);
        packet[1] = (byte)0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        if((0x02&packet[1])>0)
        {
            packet_rcvd = true;
        }
        
        
        return packet_rcvd;
    }
    
    private byte[] rcvMessage()
    {
        byte packet[] = new byte[2];
        int pktlength;
        //look for pkt length register value
        packet[0] = (byte)(0x4b|SPI_READ_CMD);
        packet[1] = (byte)0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //check size
        pktlength = packet[1];
        if(pktlength<1)
            return null;
        
        byte rxdata[] = new byte[pktlength+1];
        rxdata[0] = (byte)0x7f;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,rxdata,pktlength+1);
        
        
        

        //clear
        //clear rx fifo
        packet[0]   =   (byte)(0x08|SPI_WRITE_CMD);
        packet[1]   =   (byte) (0x02);
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);

        packet[0]   =   (byte)(0x08|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);

        //goto idle then rx mode
        packet[0]   =   (byte)(0x07|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x07;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        return rxdata;
    }
}
