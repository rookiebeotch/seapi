/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seapi;

import com.pi4j.gpio.extension.ads.ADS1015GpioProvider;
import com.pi4j.gpio.extension.ads.ADS1015Pin;
import com.pi4j.gpio.extension.ads.ADS1x15GpioProvider;
import com.pi4j.gpio.extension.pca.PCA9685GpioProvider;
import com.pi4j.gpio.extension.pca.PCA9685Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.wiringpi.Spi;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Thread.sleep;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
//import javax.swing.Timer;
import java.util.Timer;
import javax.swing.SwingUtilities;
import org.usb4java.ConfigDescriptor;
import org.usb4java.Context;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;
import static seapi.SeaPIMainFrame.SPI_WRITE_CMD;
import static seapi.SeaPIMainFrame.log;

/**
 *
 * @author jorge
 */
public class SeaPI implements Runnable{
    private static Thread           mainThread;
    public static Logger           log;
    public volatile ControllerData  gamepad_data;
    private org.usb4java.Device     usbControllerDevice;
    private DeviceHandle            usbDeviceHandle;
    private Timer                   rxPacketTimer;
    private PCA9685GpioProvider     gpioProvider;
    private ADS1015GpioProvider     gpioProviderADC;
    private volatile StateMachineController      sm;
    private int                     motor_pwm   =   0;
    private Timer                   gamepadTimer;
    //Defines
    private static final int    I2C_ADDR_PWM_CONTROLLER1    =     0x40;
    private static final int    SERVO_FREQUENCY             =       50;
    private static final int    SERVO_FREQUENCY_ADJUSTMENT  =       1;
    private static final int    rpi_i2c_bus_addr            =     I2CBus.BUS_1;
    private static final int    ERROR_CODE_SUCESS           =   0;
    private static final int    ERROR_CODE_FAILURE          =   -1;
    private static final int    ERROR_CODE_WARNING          =   1;
    public final static byte   SPI_READ_CMD                = (byte)0x00;
    public final static byte    SPI_WRITE_CMD               = (byte)0x80;
    public  int                 SEAPI_MASTER_MODE           =   0;
    private static final byte   RFM22_REG05_VAL             =   (byte)0x02;
    public static final byte   SEAPI_MSGTYPE_SERVO         =   (byte)0x01;
    public static final byte   SEAPI_MSGTYPE_MOTOR         =   (byte)0x02;
    public static final byte   SEAPI_MSGTYPE_BALLAST       =   (byte)0x03;
    public static final byte   SEAPI_MSGTYPE_LIGHTS        =   (byte)0x04;
    public static final byte   SEAPI_MSGTYPE_ANALOG_CTL    =   (byte)0x05;
    private static final int    SEAPI_MIN_SERVO_NUMBER      =    1;
    private static final int    SEAPI_MAX_SERVO_NUMBER      =   2;
    private static final int    SEAPI_MIN_SERVO_POS_MSEC    =   100;//old 950
    private static final int    SEAPI_MAX_SERVO_POS_MSEC    =   3000;//old 2600
    private static final int    SEAPI_SPI_SPEED_HZ          =   10000000;
    private static final int    SEAPI_RFM22B_POLL_TIME_MSEC =   100;
    //USB IDs
    private static int      USB_HID_BBCCONTROLLER_IDVENDOR  =   0x79;
    private static int      USB_HID_BBCCONTROLLER_IDPRODUCT =   0x181c;
    private static int      USB_HID_BBCCONTROLLER_MFR       =   0x1;
    private static int      USB_HID_BBCCONTROLLER_PRODUCT   =   0x2;
    
    private static short    USB_HID_XBOXCONTROLLER_IDVENDOR  =   (short)0x045e;
    private static short    USB_HID_XBOXCONTROLLER_IDPRODUCT =   (short)0x028e;
    private static byte     USB_HID_XBOXCONTROLLER_MFR       =   (byte)0x1;
    private static byte     USB_HID_XBOXCONTROLLER_PRODUCT   =   (byte)0x2;
    //end defines
    public SeaPI(){
        this.initPI();
    }
    
    private void initPI()
    {
        //SeaPI seapi = new SeaPI();
        initLogging();
        
        
        //init SPI for RF comms
        spiSetup();
        readConfigFile();

        //Init I2C for PWM/Servo
        initUsbControllers(SEAPI_MASTER_MODE);
        
        initPWM(SEAPI_MASTER_MODE);
        initRadio(SEAPI_MASTER_MODE);
        
        //Main State Machine loop
        sm = new StateMachineController(SEAPI_MASTER_MODE,gamepad_data,this);
        sm.processEvent(StateMachineController.SEAPI_EVENT_INIT_DONE);
        sm.processEvent(StateMachineController.SEAPI_EVENT_START);
    }
    public static void main(String [ ] args)
    {
        /*
       SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
               SeaPI m =  new SeaPI();
            }
        });
       */
       new Thread(new SeaPI()).start();
       
        
        
    }
    private void initLogging()
    {
        //init logger
        log = Logger.getLogger(SeaPIMainFrame.class.getName());
        FileHandler fh; 
        try {  

            // This block configure the logger with handler and formatter  
            fh = new FileHandler("SeaPILogFile.log");  
            //log.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);  
            log.setLevel(Level.ALL);
            log.info("start...");

        } 
        catch (IOException e) {  
            System.out.println(e.getMessage());
        }  
    }
    private void spiSetup()
    {
         // setup SPI for communication
        int fd = Spi.wiringPiSPISetup(0, SEAPI_SPI_SPEED_HZ);
        if (fd <= -1) {
            log.info(" ==>> SPI SETUP FAILED");
            return;
        }
        else
        {
             log.info("SPI SETUP....OK!");
        }
        
        
    }
    private int readConfigFile()
    {
       
        FileInputStream file = null;
        try {
            file = new FileInputStream("seapi.ini");
            BufferedReader breader = new BufferedReader(new InputStreamReader(file));
            
            //look for master
            String strLine;

            //Read File Line By Line
            while ((strLine = breader.readLine()) != null)   {
                // Print the content on the console
                //log.info(strLine);
                if(strLine.contains("controller"))
                {
                    //config for controller
                    SEAPI_MASTER_MODE = 1;
                    log.info("Setting mode to Controller!");
                }
                else
                {
                    log.info("Setting mode to Submarine!");
                }
            }

            //Close the input stream
            breader.close();
        }
        catch(Exception e)
        {
            log.severe(e.getMessage());
            log.info("No configfile seapi.ini found.");
            return -1;
        }
        return 0;
    }
    
    private void initUsbControllers(int mode)
    {
         int     result = ERROR_CODE_SUCESS;
        if(mode==1)
        {
            //USB Device Detection/Setup (Hard wired not Bluetooth USB Devices)
            
            //controller  idVendor=0x0079  idProduct=0x181c Mfr=0x1, Product=0x2, SerialNumber=0
            //Product: BBC-GAME
            //Manufacturer: ZhiXu
            gamepad_data = new ControllerData();
            
            System.out.println("Setting up usb HID...");
            usbControllerDevice = null;
            Context context = new Context();
            int myresult = LibUsb.init(context);
            if (myresult != LibUsb.SUCCESS) throw new LibUsbException("Unable to initialize libusb.", result);
            org.usb4java.DeviceList list = new org.usb4java.DeviceList();
            myresult = LibUsb.getDeviceList(null, list);
            if(myresult>0)
            {
                System.out.println("Found "+String.valueOf(myresult)+" Devices!!");
                System.out.println("---------------------------------");
                for (org.usb4java.Device device: list)
                {
                    
                    DeviceDescriptor descriptor = new DeviceDescriptor();
                    result = LibUsb.getDeviceDescriptor(device, descriptor);
                    
                    if (result >= 0)
                    {
                        int type_controller = this.usbControllerFilter(descriptor.idProduct(), descriptor.idVendor(), descriptor.iManufacturer(), descriptor.iProduct());
                        if(0!=type_controller)
                        {
                            //valid controller
                            System.out.println("This IS the Device we are looking for!!!");
                            System.out.println(descriptor.dump());
                            
                            usbControllerDevice = device;
                            ConfigDescriptor configDes = new ConfigDescriptor();
                            LibUsb.getConfigDescriptor(usbControllerDevice, (byte)0, configDes);
                            System.out.println("Config Descriptor: "+configDes.dump());
                            //Set type of usb controller so we know how to parse data
                            this.gamepad_data.setControllerType(type_controller);
                        }
                        
                        System.out.println("Vendor "+String.valueOf(descriptor.idVendor())+" Product ID: "+descriptor.idProduct());
                        System.out.println("Manufacturer: "+descriptor.iManufacturer()+"  Product: "+descriptor.iProduct());
                    }
                    System.out.println("---------------------------------");
                    
                }
            }
            else
            {
                System.out.println("None found :(");
            }
            if(usbControllerDevice!=null)
            {
                // Open the device
                usbDeviceHandle = new DeviceHandle();
                
                if(LibUsb.SUCCESS != LibUsb.open(usbControllerDevice, usbDeviceHandle))
                {
                    //error here
                    System.out.println("Error opening usb hib device!!!");
                }
                try{
                    // Check if kernel driver is attached to the interface
                    int attached = LibUsb.kernelDriverActive(usbDeviceHandle, 1);
                    if (attached < 0)
                    {
                       System.out.println("Unable to check kernel driver active");
                    }
                    else
                    {
                        System.out.println("Interface 0: "+String.valueOf(LibUsb.kernelDriverActive(usbDeviceHandle,0)));
                        System.out.println("Interface 1: "+String.valueOf(LibUsb.kernelDriverActive(usbDeviceHandle,1)));
                    }
                    // Detach kernel driver from interface 0 and 1. This can fail if
                    // kernel is not attached to the device or operating system
                    // doesn't support this operation. These cases are ignored here.
                    myresult = LibUsb.detachKernelDriver(usbDeviceHandle, 0);
                    if (myresult != LibUsb.SUCCESS &&
                        myresult != LibUsb.ERROR_NOT_SUPPORTED &&
                        myresult != LibUsb.ERROR_NOT_FOUND)
                    {
                        System.out.println("Unable to detach kernel driver: "+String.valueOf(myresult));
                    }
                    else
                    {
                        // Claim interface
                        myresult = LibUsb.claimInterface(usbDeviceHandle, 0);
                        if (myresult != LibUsb.SUCCESS)
                        {
                           System.out.println("Unable to claim interface!!");
                        }
                        else
                        {
                            System.out.println("I claimed the device!!!!!");
                            //start monitor thread
                            /*
                            UsbInterruptThread usbThread = new UsbInterruptThread(usbDeviceHandle,this.gamepad_data);
                            usbThread.start();
                            */
                            gamepadTimer = new Timer();
                            gamepadTimer.schedule(new GamepadTimerTask(usbDeviceHandle,this.gamepad_data), 0,250);
                        }
                    }
                }
                catch(Exception e)
                {
                    System.out.println("ERROR: Something bad happened trying to connect to USB device...");
                    System.out.println(e.getMessage());
                }
            }
            //These would be done after we are done with device (i.e. shutdown)
            
            
            
            //-------------done shutdown
            System.out.println("Done Setting up usb HID...");
            
            System.out.println("Try bluetooth...");
            //http://www.aviyehuda.com/blog/2010/01/08/connecting-to-bluetooth-devices-with-java/
            System.out.println("This isnt working yet.... :(");
            
            System.out.println("...done bluetooth!");
            
                        
            
        }
    }
    private int usbControllerFilter(short idProd,short idVen,byte iManu, byte iProd)
    {
        int cntlr_valid = 0;
        
        if(SeaPI.USB_HID_BBCCONTROLLER_IDPRODUCT == idProd && 
           SeaPI.USB_HID_BBCCONTROLLER_IDVENDOR == idVen &&
           SeaPI.USB_HID_BBCCONTROLLER_MFR == iManu &&
           SeaPI.USB_HID_BBCCONTROLLER_PRODUCT == iProd )
        {
            cntlr_valid = ControllerData.BBC_TYPE;
        }
        
        if(SeaPI.USB_HID_XBOXCONTROLLER_IDPRODUCT == idProd && 
           SeaPI.USB_HID_XBOXCONTROLLER_IDVENDOR == idVen &&
           SeaPI.USB_HID_XBOXCONTROLLER_MFR == iManu &&
           SeaPI.USB_HID_XBOXCONTROLLER_PRODUCT == iProd )
        {
            cntlr_valid = ControllerData.XBOX_TYPE;
        }
        
        return cntlr_valid;
    }
    public void initRadio(int mode)
    {
        this.initRFMBRegisters();
        if(mode == 1)
        {
            //has Radio RFM
            //this is util.timer
            rxPacketTimer = new Timer();
            rxPacketTimer.schedule(new RcvTimerTask(this), SEAPI_RFM22B_POLL_TIME_MSEC);
            
            //set up timer
            /*
            This was swing timer
            this.rxPacketTimer = new Timer(SEAPI_RFM22B_POLL_TIME_MSEC,new RcvListener(this));
            rxPacketTimer.start();
            */
            log.fine("Timer started...");
        }
    }
    public void initRFMBRegisters()
    {
        byte[] packet = new byte[2];
        log.info("Initializing RFM Registers...");
        
        //do sw reset first
        packet[0]   =   (byte)(0x07|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x80;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        try{
            sleep(1000);
        }
        catch(Exception e)
        {
            
        }
        //1C	9A
        packet[0]   =   (byte)(0x1C|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x9A;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //1D	40
        packet[0]   =   (byte)(0x1D|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x40;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //20	3C
        packet[0]   =   (byte)(0x20|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x3C;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //21	02
        packet[0]   =   (byte)(0x21|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x02;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //22	22
        packet[0]   =   (byte)(0x22|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x22;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //23	22
        packet[0]   =   (byte)(0x23|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x22;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //24	07
        packet[0]   =   (byte)(0x24|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x07;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //25	FF
        packet[0]   =   (byte)(0x25|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0xFF;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //2A	48
        packet[0]   =   (byte)(0x2A|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x48;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //2C	28
        packet[0]   =   (byte)(0x2C|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x28;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //2D	0C
        packet[0]   =   (byte)(0x2D|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x0C;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //2E	28
        packet[0]   =   (byte)(0x2E|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x28;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        // 30	CC
        packet[0]   =   (byte)(0x30|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0xCC;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //32	00
        packet[0]   =   (byte)(0x32|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //33	02
        packet[0]   =   (byte)(0x33|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x02;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //34	08
        packet[0]   =   (byte)(0x34|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x08;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //35	22
        packet[0]   =   (byte)(0x35|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x22;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //36	2D
        packet[0]   =   (byte)(0x36|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x2D;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //37	D4
        packet[0]   =   (byte)(0x37|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0xD4;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //38	00
        packet[0]   =   (byte)(0x38|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //39	00
        packet[0]   =   (byte)(0x39|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //3A	00
        packet[0]   =   (byte)(0x3A|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //3B	00
        packet[0]   =   (byte)(0x3B|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //3C	00
        packet[0]   =   (byte)(0x3C|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //3D	00
        packet[0]   =   (byte)(0x3D|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //3E	3c
        packet[0]   =   (byte)(0x3E|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x3c;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //3F	00
        packet[0]   =   (byte)(0x3F|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //40	00
        packet[0]   =   (byte)(0x40|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //41	00
        packet[0]   =   (byte)(0x41|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //42	00
        packet[0]   =   (byte)(0x42|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //43	FF
        packet[0]   =   (byte)(0x43|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0xFF;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //44	FF
        packet[0]   =   (byte)(0x44|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0xFF;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //45	FF
        packet[0]   =   (byte)(0x45|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0xFF;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //46	FF
        packet[0]   =   (byte)(0x46|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0xFF;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
            
        //6d 1f  max power
        packet[0]   =   (byte)(0x6D|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x1f;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        
        //6E	19
        packet[0]   =   (byte)(0x6E|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x19;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //6F	9A
        packet[0]   =   (byte)(0x6F|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x9A;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);

        //70	0C
        packet[0]   =   (byte)(0x70|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x0C;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //71	23
        packet[0]   =   (byte)(0x71|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x23;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //72	50
        packet[0]   =   (byte)(0x72|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x50;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);

        //75	53
        packet[0]   =   (byte)(0x75|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x53;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //76	64
        packet[0]   =   (byte)(0x76|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x64;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        //77	00
        packet[0]   =   (byte)(0x77|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        
        //Set ADC for Temperature
        //0f    00
        packet[0]   =   (byte)(0x0f|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        //12    E0
        packet[0]   =   (byte)(0x12|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0xE0;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        
        //05 RFM22_REG05_VAL
        packet[0]   =   (byte)(0x05|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0xff;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        //06 00
        packet[0]   =   (byte)(0x06|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        //07 07, rx on
        packet[0]   =   (byte)(0x07|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x07;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        //08 08 set auto tx off
        packet[0]   =   (byte)(0x08|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        //clear fifos
        packet[0]   =   (byte)(0x08|SPI_WRITE_CMD);
        packet[1]   =   (byte) (0x03);
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        packet[0]   =   (byte)(0x08|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        log.info("Finished Initializing RFM Registers!");
    }
    private int initPWM(int mode)
    {
        int result = SeaPI.ERROR_CODE_SUCESS;
        if(mode == 1)
        {
            //we dont use PCA9685 here
            result = SeaPI.ERROR_CODE_SUCESS;
            
            //Disabling the ADC chip since we are using USB for now
            boolean adc_chip_enabled = false;
            
            if(adc_chip_enabled == true)
            {
                //Set up i2C for joystick reading ADC
                try{
                    gpioProviderADC = new ADS1015GpioProvider(I2CBus.BUS_1,ADS1015GpioProvider.ADS1015_ADDRESS_0x48);

                    gpioProviderADC.setProgrammableGainAmplifier(ADS1x15GpioProvider.ProgrammableGainAmplifierValue.PGA_4_096V, ADS1015Pin.ALL);
                    gpioProviderADC.setMonitorInterval(100);
                    gpioProviderADC.setEventThreshold(50, ADS1015Pin.ALL);
                    // create analog pin value change listener
                    GpioPinListenerAnalog listener = new GpioPinListenerAnalog()
                    {
                        @Override
                        public void handleGpioPinAnalogValueChangeEvent(GpioPinAnalogValueChangeEvent event)
                        {
                             System.out.println("get val...");
                            // RAW value
                            double value = event.getValue();

                            // percentage
                            double percent =  ((value * 100) / ADS1015GpioProvider.ADS1015_RANGE_MAX_VALUE);

                            // approximate voltage ( *scaled based on PGA setting )
                            double voltage = gpioProviderADC.getProgrammableGainAmplifier(event.getPin()).getVoltage() * (percent/100);

                            // display output
                            System.out.println(" (" + event.getPin().getName() +") : VOLTS=" + String.valueOf(voltage) + "  | PERCENT=" + String.valueOf(percent) + "% | RAW=" + value + "       ");
                        }
                    };
                        // create gpio controller
                    final GpioController gpiooye = GpioFactory.getInstance();

                    gpiooye.provisionAnalogInputPin(gpioProvider, ADS1015Pin.INPUT_A0, "MyAnalogInput-A0").addListener(listener);
                    gpiooye.provisionAnalogInputPin(gpioProvider, ADS1015Pin.INPUT_A1, "MyAnalogInput-A1").addListener(listener);

                }
                catch(Exception e)
                {

                }
            }
            
        }
        else
        {
             //PMW Controller
            try{       
                 log.info("Configuring PWM Controller...");

                 gpioProvider = new PCA9685GpioProvider(I2CFactory.getInstance(I2CBus.BUS_1), I2C_ADDR_PWM_CONTROLLER1,new BigDecimal(SERVO_FREQUENCY), new BigDecimal(SERVO_FREQUENCY_ADJUSTMENT));
                 if(gpioProvider == null)
                 {
                     log.info("Failure: Unable to connect to PWM Controller...\n");
                     return ERROR_CODE_FAILURE;
                 }
                 GpioController gpio = GpioFactory.getInstance();

                 //Set Pins
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_00, "not used");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_01, "not used");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_02, "not used");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_03, "not used");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_04, "Servo 1");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_05, "Servo 2");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_06, "Servo 3");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_07, "Servo 4");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_08, "Servo 5");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_09, "not used");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_10, "not used");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_11, "not used");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_12, "not used");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_13, "not used");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_14, "Motor");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_15, "not used");

                 //gpioProvider.reset();
                 log.info("**************************");
                 log.info("System Configuration Summary");
                 log.info("Frequency is: "+gpioProvider.getFrequency().toString());

            }
            catch(Exception e)
            {
                //serious error
                log.severe(e.getMessage());
                //TODO: Set an LED indicator for an error
                result = ERROR_CODE_FAILURE;
            }
        }
        return result;
    }
    public void processMsg(byte[] msg)
    {
        
        int left_x=0,left_y=0;
        int right_x=0,right_y=0;
        byte dpad=0;
        int lefttrigger=0,righttrigger=0;
        
        
        
        if(msg.length<1)
        {
            //emtpy msg....
            return;
        }
        //log.fine("Processing Msg Type: "+String.valueOf(msg[1]));
        //first byte is msg type
        int msgtype = msg[1];
        int datalength = msg.length-1;
        
        switch(msgtype)
        {
            case SeaPIMainFrame.SEAPI_MSGTYPE_SERVO:
                //get each servo position
                for(int servo=1;servo<=Math.floor(datalength/2);servo++)
                {
                    //grab two bytes
                    log.info("Grabbing bytes "+String.valueOf(servo*2-1)+" and "+String.valueOf(servo*2));
                    log.info("Grabbing these bytes-> "+String.format("%02X",(byte)(msg[servo*2]))+" "+String.format("%02X",(byte)(msg[servo*2-1])));
                    short servo_pos = (short)( (msg[servo*2]<<8) | (0xff&msg[servo*2-1]) );
                    
                    log.info("Servo "+String.valueOf(servo)+" to move "+String.valueOf(servo_pos));
                    commandServo(servo,servo_pos);
                }
                break;
            case SeaPIMainFrame.SEAPI_MSGTYPE_BALLAST:
                log.info("BALLAST: not implemented..");
                break;
            case SeaPIMainFrame.SEAPI_MSGTYPE_MOTOR:
                log.info("MOTOR: not implemented..");
                break;
            case SeaPIMainFrame.SEAPI_MSGTYPE_ANALOG_CTL:
                //Second Byte tells controller type
                
                switch((int)msg[2])
                {
                    case ControllerData.BBC_TYPE:
                        //thumb left x dir stick msg[2]
                        left_x = 0x00ff&msg[3];
                        left_y = 0x00ff&msg[4];
                        //scale this , range is 1650, input is 0 255
                        commandServo(1,11*left_x);
                        //if(left_x<128)commandServo(1,0);
                        //if(left_x>128)commandServo(1,3000);
                        //if(left_x==128)commandServo(1,1850);
                        log.info("Commaind Servo 1 to "+String.valueOf(7*left_x+950)+" Input: "+String.valueOf(left_x));

                        commandServo(2,11*left_y);
                        //if(left_y<128)commandServo(2,0);
                        //if(left_y>128)commandServo(2,3000);
                        //if(left_y==128)commandServo(2,1850);
                        log.info("Commaind Servo 2 to "+String.valueOf(7*left_y+950)+" Input: "+String.valueOf(left_y));

                        break;
                    case ControllerData.XBOX_TYPE:
                        
                        ByteBuffer bb;
                        try{
                            bb = ByteBuffer.allocate(2);
                            bb.order(ByteOrder.LITTLE_ENDIAN);
                            //get left stick x
                            bb.put((byte)msg[3]);
                            bb.put((byte)msg[4]);
                            left_x = (int)bb.getShort(0);

                            //get left stick y
                            bb.clear();
                            bb.put((byte)msg[5]);
                            bb.put((byte)msg[6]);
                            left_y = (int)bb.getShort(0);

                            //get right stick x
                            bb.clear();
                            bb.put((byte)msg[7]);
                            bb.put((byte)msg[8]);
                            right_x = (int)bb.getShort(0);
                            //get right stick y
                            bb.clear();
                            bb.put((byte)msg[9]);
                            bb.put((byte)msg[10]);
                            right_y = (int)bb.getShort(0);
                        }
                        catch(Exception e)
                        {
                            System.out.println(e.getMessage());
                        }
                        

                        
                        
                        //get dpad
                        dpad = (byte)msg[11];
                        //get left trigger
                        lefttrigger = (int)msg[12]&0xff;
                        //get right trigger
                        righttrigger = (int)msg[13]&0xff;
                        
                        
                        try{
                            commandServo(1,(int)(left_x/36)+1850);
                            commandServo(2,(int)(left_y/36)+1850);
                            commandServo(3,(int)(right_x/36)+1850);
                            commandServo(4,(int)(right_y/36)+1850);
                            commandServo(5,(int)(7*lefttrigger+950));
                            
                            //commandMotor(dpad);
                        }
                        catch(Exception e)
                        {
                            System.out.println(e.getMessage());
                        }
                        
                        break;
                    default:
                        //unknown
                        break;
                }
                                
                
                break;
            default:
                //unknown msg type
                log.info("Unknown msg type "+String.valueOf(msgtype)+" rcvd!");
        }
        
    }
    public void commandServo(int servo_number,int position)
    {
        //2600 is far left
        //1850 middle
        //950  right

        if(SEAPI_MIN_SERVO_POS_MSEC>position)position=SEAPI_MIN_SERVO_POS_MSEC;
        if(SEAPI_MAX_SERVO_POS_MSEC<position)position=SEAPI_MAX_SERVO_POS_MSEC;
        
        
        //The value fed to setPWM is millisecond duration
        switch(servo_number){
            case 1:
                //log.info("Servo 1 (4) Moving ..."+String.valueOf(position));
                gpioProvider.setPwm(PCA9685Pin.PWM_04, position);
                break;
            case 2:
                //log.info("Servo 2 (5) Moving ..."+String.valueOf(position));
                gpioProvider.setPwm(PCA9685Pin.PWM_05, position);
                break;    
            case 3:
                //log.info("Servo 3 (6) Moving ..."+String.valueOf(position));
                gpioProvider.setPwm(PCA9685Pin.PWM_06, position);
                break;
            case 4:
                //log.info("Servo 4 (7) Moving ..."+String.valueOf(position));
                gpioProvider.setPwm(PCA9685Pin.PWM_07, position);
                break;   
            case 5:
                //log.info("Servo 5 (8) Moving ..."+String.valueOf(position));
                gpioProvider.setPwm(PCA9685Pin.PWM_08, position);
                break;   
            default:
                //log.fine("Servo not defined...");
                break;
        }
        
        //servo numbers 1 -6
    }
    public void commandMotor(byte motor_input)
    {
        //int current_pwm = gpioProvider.getPwm(PCA9685Pin.PWM_14);
        
        int i = motor_input&(byte)0x03;
        switch(i)
        {
            case 1:
                //increase motor speed
                motor_pwm+=100;
               
                break;
            case 2:
                motor_pwm-=100;
                break;
            default:
                break;
        }
        gpioProvider.setPwm(PCA9685Pin.PWM_14, motor_pwm);
        
    }

    @Override
    public void run() {
       
        while(StateMachineController.SEAPI_STATE_EXIT != this.sm.getState())
        {
            try{
                Thread.sleep(1000);
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
    }

    
}
