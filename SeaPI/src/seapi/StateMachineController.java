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
import javax.swing.Timer;
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
    
    //events
    final static int        SEAPI_EVENT_PKT_RCV     =   1000;
    final static int        SEAPI_EVENT_TIMER_CTL   =   1001;
    final static int        SEAPI_EVENT_INIT_DONE   =   1002;
    final static int        SEAPI_EVENT_START       =   1003;
    final static int        SEAPI_EVENT_RCV_EXPIRED       =   1004;
    final static int        SEAPI_EVENT_TIMER_STATUS_DATA = 1005;
    final static int        SEAPI_EVENT_BUTTON_PRESSED  =   2000;
    
    //vars
    private volatile SeaPIMainFrame mainPtr;
    private int current_state;
    private ControllerData  myControlData;
    private Timer           control_data_timer;
    private Timer           rcvExpiredTimer;
    List<byte[]>            pendingTransmitBytes;
    private int             control_mode;
    private RFPacketPendingThread   rcvPktThread;
    StateMachineController(int mode,ControllerData in_dataCntlr,SeaPIMainFrame mptr){
        mainPtr = mptr;
        control_mode = mode;
        current_state   =   SEAPI_STATE_INIT;
        myControlData   = in_dataCntlr;
        //make sure timers are all off at start
        //clear any counters
        pendingTransmitBytes = new ArrayList();
        
        control_data_timer = new Timer(100,new StateMachineTimerControlDataListener(this));
        control_data_timer.setRepeats(false);
        
        rcvExpiredTimer = new Timer(100,new StateMachineTimerRcvWaitListener(this));
        rcvExpiredTimer.setRepeats(false);
        Thread a = new Thread();
        rcvPktThread = new RFPacketPendingThread(this);
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
            default:
                statestr="unknown";
        }
        return statestr;
    }
    public void setControlDataTimer(Timer in_timer)
    {
        this.control_data_timer = in_timer;
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
                this.control_data_timer.restart();
                break;
                
            case SEAPI_EVENT_PKT_RCV:
                //kill timer
                this.rcvExpiredTimer.stop();
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
                }
                //Send one packet
                sendBufferedPacket();
                this.rcvExpiredTimer.restart();
                break;
            case SEAPI_EVENT_TIMER_CTL:
                
                //Grab data from data control for analog controls
                //grab button info
                this.pendingTransmitBytes.add(myControlData.getAnalogMessage());
                //this.pendingTransmitBytes.add(myControlData.getButtonMessage());
                this.control_data_timer.restart();
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
                this.control_data_timer.restart();
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
                    //send any data
                    this.sendBufferedPacket();
                }
                
                
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
                sendBufferedPacket();
                //set wait timer
                this.rcvExpiredTimer.restart();
                next_state = SEAPI_STATE_RX;
                break;
            case SEAPI_EVENT_TIMER_CTL:
                //need to send controll data
                //Grab data from data control for analog controls
                //grab button info
                this.pendingTransmitBytes.add(myControlData.getAnalogMessage());
                //this.pendingTransmitBytes.add(myControlData.getButtonMessage());
                this.control_data_timer.restart();
                
                //then go to state TX
                next_state = SEAPI_STATE_IDLE;
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
        }
        return 0;
    }
    private int txMessage(byte[] msgbytes)
    {
        byte packet[] = new byte[2];
        //DateFormat df = new SimpleDateFormat("HH:mm:ss");
        //Date today = Calendar.getInstance().getTime(); 
        
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
            Thread.sleep(20);
            //Check reg for valid sent
            packet[0]   =   (byte)(0x03|SPI_READ_CMD);
            Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
            byte result = (byte)(0x04&packet[1]);
            
            //System.out.println("Reg 3: "+Arrays.toString(packet));
    
            if(result!=4)
            {
                //print some debug status info
                //Dev Status
                packet[0]   =   (byte)(0x02|SPI_READ_CMD);
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
                System.out.println("Reg 2: "+Arrays.toString(packet));
                packet[0]   =   (byte)(0x04|SPI_READ_CMD);
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
                System.out.println("Reg 4: "+Arrays.toString(packet));
                packet[0]   =   (byte)(0x07|SPI_READ_CMD);
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
                System.out.println("Reg 7: "+Arrays.toString(packet));
                packet[0]   =   (byte)(0x08|SPI_READ_CMD);
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
                System.out.println("Reg 8: "+Arrays.toString(packet));
                packet[0]   =   (byte)(0x3E|SPI_READ_CMD);
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
                System.out.println("Reg 3E(PktLength): "+Arrays.toString(packet));
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
