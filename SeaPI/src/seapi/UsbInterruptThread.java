/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seapi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.usb4java.BufferUtils;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

/**
 *
 * @author jorge
 */
public class UsbInterruptThread extends Thread {
    
    private DeviceHandle usbDeviceHandle;
    private volatile boolean    abort;
    private java.nio.IntBuffer          numBytesRcvd;
    volatile ByteBuffer                  dataBuffer;
    volatile ControllerData          gamepadData;
    //Endpoint beboncool game pad is 0x81
    UsbInterruptThread(DeviceHandle theDeviceHandle, ControllerData dataCtl)
    {
        numBytesRcvd    =   java.nio.IntBuffer.allocate(3);
        usbDeviceHandle =   theDeviceHandle;
        abort           =   false;
        dataBuffer      =   BufferUtils.allocateByteBuffer(64).order(ByteOrder.LITTLE_ENDIAN);
        gamepadData     = dataCtl;
    }
    
    /**
     * Aborts the event handling thread.
     */
    public void abort()
    {
        this.abort = true;
    }
    
     @Override
    public void run()
    {
        
        while (!this.abort)
        {
            int result = 0;
            result = getUsbInterruptData();
            /*
            System.out.println("Transfer Result: "+String.valueOf(result)+" "+String.valueOf(this.numBytesRcvd.get()));
            System.out.println();
            for(int i=0;i<64;i++)
            {
                System.out.print(String.valueOf(dataBuffer.get(i)));
            }
            */
            
            if(LibUsb.SUCCESS == result)
            {
                if(9==this.numBytesRcvd.get())
                {
                    //System.out.println("Got #"+String.valueOf(this.numBytesRcvd.get())+" of bytes.");
                    //System.out.println("data "+String.valueOf(this.dataBuffer.get()));
                    //post data to controller status...
                    gamepadData.updateData(dataBuffer);
                }
                
            }
            try{
                Thread.sleep(10);
            }
            catch(Exception e)
            {
            }
        }
    }
    
    public int getUsbInterruptData()
    {
        int result = 0;
        
        try{
            dataBuffer.clear();
            numBytesRcvd.clear();
            //result = LibUsb.bulkTransfer(usbDeviceHandle,(byte)0x81, dataBuffer, numBytesRcvd, 5000);
            result = LibUsb.interruptTransfer(this.usbDeviceHandle, (byte)0x81, dataBuffer, numBytesRcvd, 50);
            //if(result == 0)
            //    gamepadData.updateData(dataBuffer);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        return result;
    }
}
