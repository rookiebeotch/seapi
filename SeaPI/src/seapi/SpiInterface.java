/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seapi;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

/**
 *
 * @author jorge
 */
public class SpiInterface {
    private SpiDevice spidev;
    public static byte INIT_CMD = (byte) 0xD0;
    
    //Constructor
    SpiInterface()
    {
        spidev = null;
        
    }
    public int InitDevice()
    {
        int result = 0;
        // create SPI object instance for SPI for communication
        try {
            spidev = SpiFactory.getInstance(SpiChannel.CS0,
                                     SpiDevice.DEFAULT_SPI_SPEED, // default spi speed 1 MHz
                                     SpiDevice.DEFAULT_SPI_MODE); // default spi mode 0
            if(spidev != null)
                System.out.print("SPI Device initialized...");
            else
                System.out.print("ERROR: Failed to init SPI Device!");
        }
        catch(Exception e)
        {
            result = -1;
        }
        
        return result;
    }
}
