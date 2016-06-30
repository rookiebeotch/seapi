/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seapi;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import java.io.IOException;
import static java.lang.Thread.sleep;

//NOTES
/*
registers are split in two (H and L)
L is 8 bits
H is 3 bits
H 4th bit means always on or always off depedning on reg
being the ON or oFF one.
*/
/**
 *
 * @author jorge
 */
public class PWMController {
    private final static byte   REGISTER_PRE_SCALE      =   (byte) 0xFE;
    private final static byte   REGISTER_ALL_LED_ON_L   =   (byte) 0xFA;
    private final static byte   REGISTER_ALL_LED_ON_H   =   (byte) 0xFB;
    private final static byte   REGISTER_ALL_LED_OFF_L  =   (byte) 0xFC;
    private final static byte   REGISTER_ALL_LED_OFF_H  =   (byte) 0xFD;
    
    private final static byte   REGISTER_PWM0_ON_L      =   (byte) 0x06;
    private final static byte   REGISTER_PWM0_ON_H      =   (byte) 0x07;
    private final static byte   REGISTER_PWM0_OFF_L      =   (byte) 0x08;
    private final static byte   REGISTER_PWM0_OFF_H      =   (byte) 0x09;
    private final static byte   REGISTER_PWM1_ON_L      =   (byte) 0x0a;
    private final static byte   REGISTER_PWM1_ON_H      =   (byte) 0x0b;
    private final static byte   REGISTER_PWM1_OFF_L      =   (byte) 0x0c;
    private final static byte   REGISTER_PWM1_OFF_H      =   (byte) 0x0d;
    private final static byte   REGISTER_PWM2_ON_L      =   (byte) 0x0e;
    private final static byte   REGISTER_PWM2_ON_H      =   (byte) 0x0f;
    private final static byte   REGISTER_PWM2_OFF_L      =   (byte) 0x10;
    private final static byte   REGISTER_PWM2_OFF_H      =   (byte) 0x11;
    private final static byte   REGISTER_PWM3_ON_L      =   (byte) 0x12;
    private final static byte   REGISTER_PWM3_ON_H      =   (byte) 0x13;
    private final static byte   REGISTER_PWM3_OFF_L      =   (byte) 0x14;
    private final static byte   REGISTER_PWM3_OFF_H      =   (byte) 0x15;
    private final static byte   REGISTER_PWM4_ON_L      =   (byte) 0x16;
    private final static byte   REGISTER_PWM4_ON_H      =   (byte) 0x17;
    private final static byte   REGISTER_PWM4_OFF_L      =   (byte) 0x18;
    private final static byte   REGISTER_PWM4_OFF_H      =   (byte) 0x19;
    
    
    private I2CDevice i2cdev;
    
    public PWMController(I2CBus bus, int addr) throws IOException {
        
            i2cdev = bus.getDevice(addr);
        
    }
    
    public int readData(int addr)
    {
        int bytes_read = 0;
        int maxRead  = 8;
        byte[] databuff = new byte[maxRead];
        
        // 1st param, address tostart reading from, 
        //2nd param, arry to store data
        //3rd offset?
        //4th number of bytes to read
        //return, number of bytes read
        try {
            bytes_read  =   i2cdev.read(0,databuff,0,maxRead);
        }
        catch(Exception e)
        {
            bytes_read = 0;
        }
        
        return bytes_read;
    }
    public void setPWM4()
    {
        //4096 = 20ms at 50hz
         //1ms to 2 ms is -90 to 90
        //10% to 5%
        //410(3686) to 205(3891)
        
        try{
            short on_time = 410;
            short off_time = 3686;
            for(int i=0;i<100;i++)
            {
                /*
                System.out.print("Setting Low "+String.valueOf(((off_time+i*2)&0xff)+((((off_time+i*2) >> 8)& 0xff)<<8))+"...\n");
                System.out.print("Setting On "+String.valueOf(((on_time-i*2)&0xff)+((((on_time-i*2) >> 8)& 0xff)<<8))+"...\n");
                
                i2cdev.write(PWMController.REGISTER_PWM4_OFF_L,(byte)((off_time+i*2)&0xff));
                
                i2cdev.write(PWMController.REGISTER_PWM4_OFF_H,(byte)(((off_time+i*2) >> 8)& 0xff));
                
               // System.out.print("Setting High"+String.valueOf()+"...\n");
                i2cdev.write(PWMController.REGISTER_PWM4_ON_L,(byte)((on_time-i*2)&0xff));
                
                i2cdev.write(PWMController.REGISTER_PWM4_ON_H,(byte)(((on_time-i*2) >> 8)& 0xff));
                */
                System.out.print("Set 410\n");
                i2cdev.write(PWMController.REGISTER_PWM4_OFF_L,(byte)0x66);
                i2cdev.write(PWMController.REGISTER_PWM4_OFF_H,(byte)0x0E);
                i2cdev.write(PWMController.REGISTER_PWM4_ON_L,(byte)0x00);
                i2cdev.write(PWMController.REGISTER_PWM4_ON_H,(byte)0x00);
                
                sleep(3000);
                
                System.out.print("Set 205\n");
                i2cdev.write(PWMController.REGISTER_PWM4_OFF_L,(byte)0x33);
                i2cdev.write(PWMController.REGISTER_PWM4_OFF_H,(byte)0x0F);
                i2cdev.write(PWMController.REGISTER_PWM4_ON_L,(byte)0x00);
                i2cdev.write(PWMController.REGISTER_PWM4_ON_H,(byte)0x00);
            }
        }
        catch(IOException | InterruptedException e)
        {
            System.out.print("Failure...\n");
        }
    }
    
    public void setAllOff()
    {
        
    }
    /*
    Set frequency
    Param in hertz
    0x79 for 50Hz
    */
    public void setFreq(int frequency)
    {
       byte prescale = (byte)((25000000/(4096*frequency))-1);
       try {
           System.out.print("Setting frequency");
            i2cdev.write(0xFE, (byte)0x79);
       }
       catch(Exception e)
       {
               
       }
    }
}
