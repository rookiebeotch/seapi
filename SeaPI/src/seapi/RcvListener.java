/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seapi;

import com.pi4j.wiringpi.Spi;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author jorge
 */

public class RcvListener implements ActionListener {
    SeaPIMainFrame mainPtr;
    RcvListener(SeaPIMainFrame mptr)
    {
        mainPtr = mptr;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        
        //check for packets
        byte packet[] = new byte[2];
        
        //First make sure we are in rxmode
        //OR for a valiprxpkt
        //rx mode is exitted on a rcvd packet valid
        packet[0] = (byte)0x07;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0, packet,2);
        if((packet[1]&04)!=4 )
        {
            //not in rx mode....
            mainPtr.log.fine("Not in Rx Mode!!");
            
        }
        
        //grab status
        packet[0] = (byte)0x03;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        //check for crc error
        if((packet[1]&0x01)==1)mainPtr.log.fine("CRC rx error!!");
        
        
        if((packet[1]&0x02)==2)
        {
            mainPtr.log.fine("Packet Valid!!");
            //packet to process
            //get length of msg
            packet[0] = (byte)0x4b;
            Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
            short length = (short)packet[1];
            //grab data
            byte data[] = new byte[length+1];
            data[0] = (byte)0x7f;
            Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,data,length+1);
            
            //clear rx fifo
            //clear fifos
            packet[0]   =   (byte)(0x08|mainPtr.SPI_WRITE_CMD);
            packet[1]   =   (byte) (0x00|0x02);
            Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);

            packet[0]   =   (byte)(0x08|mainPtr.SPI_WRITE_CMD);
            packet[1]   =   (byte) 0x00;
            Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
            
            //copy data send to process
            byte msg[] = new byte[length];
            for(int x=0;x<length;x++)
            {
                msg[x] = data[x+1];
            }
            this.mainPtr.processMsg(msg);
            
            //go back top rcv mode
            packet[0]   =   (byte)(0x07|mainPtr.SPI_WRITE_CMD);
            packet[1]   =   (byte) 0x07;
            Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
            mainPtr.log.fine("Back to RX Mode...");
        }
        else
        {
            mainPtr.log.fine("No packets...");
            mainPtr.log.fine("Reg 3 Status: "+Byte.toString((byte)(0xff&packet[1])));
            
        }
    }
    
}
