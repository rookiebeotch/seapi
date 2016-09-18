/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seapi;

import com.pi4j.wiringpi.Spi;
import java.util.Arrays;
import static seapi.SeaPIMainFrame.SPI_WRITE_CMD;
import static seapi.SeaPIMainFrame.log;
import javax.swing.Timer;
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
    final static int        SEAPI_EVENT_BUTTON_PRESSED  =   2000;
    
    //vars
    private int current_state;
    private ControllerData  myControlData;
    private Timer           control_data_timer;
    
    StateMachineController(ControllerData in_dataCntlr){
        current_state   =   SEAPI_STATE_INIT;
        myControlData   = in_dataCntlr;
        //make sure timers are all off at start
        //clear any counters
    }
    public void setControlDataTimer(Timer in_timer)
    {
        this.control_data_timer = in_timer;
    }
    public void processEvent(int event)
    {
        System.out.println("DEBUG: Got EVENT: "+String.valueOf(event)+" State: "+String.valueOf(current_state));
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
    
    public  int state_init(int event,int current_state)
    {
        int next_state = current_state;
        
        switch(event)
        {
            case SEAPI_EVENT_INIT_DONE:
                next_state = SEAPI_STATE_IDLE;
                //start timer to send data
                this.control_data_timer.start();
                break;
            default:
                //do nada!!!
        }
        
        return next_state;
    }
    public  int state_tx(int event,int current_state)
    {
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
        int next_state = current_state;
        
        switch(event)
        {
            
            default:
                //do nada!!!
        }
        
        return next_state;
    }
    public  int state_idle(int event,int current_state)
    {
        int next_state = current_state;
        
        switch(event)
        {
            case SEAPI_EVENT_TIMER_CTL:
                //need to send controll data
                //Grab data from data control for analog controls
                //grab button info
                
                txMessage(myControlData.getAnalogMessage());
                txMessage(myControlData.getButtonMessage());
                
                //then go to state TX
                next_state = SEAPI_STATE_IDLE;
                break;
            
            default:
                //do nada!!!
        }
        
        return next_state;
    }
    
    private int txMessage(byte[] msgbytes)
    {
        System.out.println("Sending: "+Arrays.toString(msgbytes));
        //clear tx fifo and errors
        //clear fifos
        byte packet[] = new byte[2];
        packet[0]   =   (byte)(0x08|SPI_WRITE_CMD);
        packet[1]   =   (byte) (0x00|0x03);
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        packet[0]   =   (byte)(0x08|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
            
        byte spiPacket[] = new byte[msgbytes.length+1];
        
        //write cmd to fifo (0x7F|0x80)
        spiPacket[0] = (byte)0xFF;
        //copy data over
        System.arraycopy(msgbytes, 0, spiPacket, 1, msgbytes.length);   
            
        //write to fifo    
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,spiPacket,spiPacket.length);
        log.fine("Packet Pushed..."+msgbytes);
        
        //set pkt length
        packet[0]   =   (byte)(0x3e|SPI_WRITE_CMD);
        packet[1]   =   (byte) msgbytes.length;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        log.fine("Set Tx pkt length to "+String.valueOf(msgbytes.length));
            
       //set to tx on
        packet[0] = (byte)(0x87);
        packet[1] = (byte)0x09;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        try{
            Thread.sleep(10);
        }
        catch(Exception e)
        {
            
        }
        
        
        return 0;
    }
}
