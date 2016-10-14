/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seapi;

import com.pi4j.wiringpi.Spi;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jorge
 */
public class RFPacketPendingThread extends Thread {

    private boolean abort;
    private volatile StateMachineController  my_sm;
    RFPacketPendingThread(StateMachineController in_sm){
        my_sm = in_sm;
        this.abort = false;
    }
    
    /**
     * Aborts the event handling thread.
     */
    public void abort()
    {
        //System.out.println("Aborting Thread....");
        this.abort = true;
    }
    @Override
    public void run()
    {
        this.abort = false;
        //Check SPI for valid pkt rcvd
        //System.out.println("Starting Thread....");
        boolean packet_rcvd = false;
        byte packet[] = new byte[2];
        

        packet[0] = (byte)0x07;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0, packet,2);
        if((packet[1]&04)!=4 )
        {
            //not in rx mode....
            //System.out.println("Not in Rx Mode!!");
            
        }

        //look for valid pkt register
        packet[0] = (byte)(0x03|SeaPIMainFrame.SPI_READ_CMD);
        packet[1] = (byte)0x00;
        
        while(abort==false )
        {
            
            packet[0] = (byte)(0x03|SeaPIMainFrame.SPI_READ_CMD);
            packet[1] = (byte)0x00;
            Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
            if((0x02&packet[1])>0)
            {
                //System.out.println("PACKET!!");
                my_sm.processEvent(StateMachineController.SEAPI_EVENT_PKT_RCV);
                abort = true;
            }
            else
            {
                
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RFPacketPendingThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        //System.out.println("Thread out of loop....");
        
    }
    
}
