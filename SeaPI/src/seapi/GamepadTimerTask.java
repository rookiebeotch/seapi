/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seapi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.TimerTask;
import org.usb4java.BufferUtils;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;

/**
 *
 * @author jorge
 */
public class GamepadTimerTask extends TimerTask{
    private DeviceHandle                usbDeviceHandle;
    private java.nio.IntBuffer          numBytesRcvd;
    volatile ByteBuffer                 dataBuffer;
    volatile ControllerData             gamepadData;
    GamepadTimerTask(DeviceHandle theDeviceHandle, ControllerData dataCtl)
    {
        numBytesRcvd    =   java.nio.IntBuffer.allocate(3);
        usbDeviceHandle =   theDeviceHandle;
        dataBuffer      =   BufferUtils.allocateByteBuffer(64).order(ByteOrder.LITTLE_ENDIAN);
        gamepadData     = dataCtl;
    }
    @Override
    public void run() {
        
        int result = 0;
        result = getUsbInterruptData();
        

        if(LibUsb.SUCCESS == result)
        {
            switch(gamepadData.getControllerType())
            {
                case ControllerData.BBC_TYPE:
                    if(9==this.numBytesRcvd.get())
                    {
                        //System.out.println("Got #"+String.valueOf(this.numBytesRcvd.get())+" of bytes.");
                        //System.out.println("data "+String.valueOf(this.dataBuffer.get()));
                        //post data to controller status...
                        gamepadData.updateData(dataBuffer);
                    }
                    break;
                case ControllerData.XBOX_TYPE:
                    if(20==this.numBytesRcvd.get())
                    {
                        //System.out.println("Got #"+String.valueOf(this.numBytesRcvd.get())+" of bytes.");
                        //System.out.println("data "+String.valueOf(this.dataBuffer.get()));
                        //post data to controller status...
                        gamepadData.updateData(dataBuffer);
                    }
                    break;
                default:
                    break;
            }

            //System.out.println(String.valueOf(Math.random()));
            
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
