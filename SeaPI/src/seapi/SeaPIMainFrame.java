/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seapi;

import com.pi4j.component.servo.Servo;
import com.pi4j.component.servo.impl.GenericServo;
import com.pi4j.component.servo.impl.PCA9685GpioServoProvider;
import com.pi4j.device.Device;
import com.pi4j.gpio.extension.ads.ADS1015GpioProvider;
import com.pi4j.gpio.extension.ads.ADS1015Pin;
import com.pi4j.gpio.extension.ads.ADS1x15GpioProvider.ProgrammableGainAmplifierValue;
import com.pi4j.gpio.extension.pca.PCA9685GpioProvider;
import com.pi4j.gpio.extension.pca.PCA9685Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;
import com.pi4j.io.gpio.event.PinListener;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.wiringpi.Spi;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Math.round;
import static java.lang.Thread.sleep;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import static java.lang.Math.round;
import static java.lang.Thread.sleep;
import static java.lang.Math.round;
import static java.lang.Thread.sleep;
import static java.lang.Math.round;
import static java.lang.Thread.sleep;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import static javax.bluetooth.DataElement.UUID;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import org.usb4java.BosDescriptor;
import org.usb4java.BufferUtils;
import org.usb4java.ConfigDescriptor;
import org.usb4java.Context;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;
import org.usb4java.Transfer;
import static java.lang.Math.round;
import static java.lang.Thread.sleep;
import static java.lang.Math.round;
import static java.lang.Thread.sleep;
import static java.lang.Math.round;
import static java.lang.Thread.sleep;
import static java.lang.Math.round;
import static java.lang.Thread.sleep;

/**
 *
 * @author jorge
 */
public class SeaPIMainFrame extends javax.swing.JFrame {

    
    private GpioController      gpioCntrl;
    private SpiInterface        spiDevice;
    private PWMController       pwmController;
    private static final int    I2C_ADDR_PWM_CONTROLLER1    =     0x40;
    private static final int    SERVO_FREQUENCY             =       50;
    private static final int    SERVO_FREQUENCY_ADJUSTMENT  =       1;
    private static final int    rpi_i2c_bus_addr            =     I2CBus.BUS_1;
    
    private static final int    ERROR_CODE_SUCESS           =   0;
    private static final int    ERROR_CODE_FAILURE          =   -1;
    private static final int    ERROR_CODE_WARNING          =   1;
   
    private final static byte   SPI_READ_CMD                = (byte)0x00;
    public final static byte    SPI_WRITE_CMD               = (byte)0x80;
    
    public  int                 SEAPI_MASTER_MODE           =   0;
    
    private static final byte   RFM22_REG05_VAL             =   (byte)0x02;
    private static final byte   SEAPI_MSGTYPE_SERVO         =   (byte)0x01;
    private static final byte   SEAPI_MSGTYPE_MOTOR         =   (byte)0x02;
    private static final byte   SEAPI_MSGTYPE_BALLAST       =   (byte)0x03;
    private static final byte   SEAPI_MSGTYPE_LIGHTS        =   (byte)0x04;
    private static final int    SEAPI_MIN_SERVO_NUMBER      =    1;
    private static final int    SEAPI_MAX_SERVO_NUMBER      =   2;
    private static final int    SEAPI_MIN_SERVO_POS_MSEC    =   950;
    private static final int    SEAPI_MAX_SERVO_POS_MSEC    =   2600;
    private static final int    SEAPI_SPI_SPEED_HZ          =   10000000;
    private static final int    SEAPI_RFM22B_POLL_TIME_MSEC =   100;
    private PCA9685GpioProvider gpioProvider;
    private ADS1015GpioProvider gpioProviderADC;
    private Timer               rxPacketTimer;
    public static Logger        log;

    private static Object lock=new Object();
    
    //USB IDs
    private static int      USB_HID_BBCCONTROLLER_IDVENDOR  =   0x79;
    private static int      USB_HID_BBCCONTROLLER_IDPRODUCT =   0x181c;
    private static int      USB_HID_BBCCONTROLLER_MFR       =   0x1;
    private static int      USB_HID_BBCCONTROLLER_PRODUCT   =   0x2;
    private org.usb4java.Device          usbControllerDevice;
    private DeviceHandle    usbDeviceHandle;
    public volatile ControllerData  gamepad_data;
    //controller  idVendor=0x0079  idProduct=0x181c Mfr=0x1, Product=0x2, SerialNumber=0
    //Product: BBC-GAME
    //Manufacturer: ZhiXu
    
    

    private StateMachineController      sm;

    //Msg Protocol
    //Byte1 msg type 00 to 255
    
    //1 servo control: 4 bytes per servo
    //2 motor control:
    //3 ballast control:
    //4 lights
    
    
    
    
    //servo 6
    
    /**
     * Creates new form SeaPIMainFrame
     */
    public SeaPIMainFrame() {
        initComponents();
        //init logger
        log = Logger.getLogger(SeaPIMainFrame.class.getName());
        log.setLevel(Level.SEVERE);
        //init SPI for RF comms
        spiSetup();
        readConfigFile();

        //Init I2C for PWM/Servo
        seaPiInit(SEAPI_MASTER_MODE);
        
        sm = new StateMachineController(this.gamepad_data);
       
        sm.processEvent(StateMachineController.SEAPI_EVENT_INIT_DONE);
        sm.processEvent(StateMachineController.SEAPI_EVENT_START);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButtonMoveServo = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jButtonDumpRegisters = new javax.swing.JButton();
        jButtonInitRFM = new javax.swing.JButton();
        jTextFieldPktDataTx = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jButtonSendPacket = new javax.swing.JButton();
        jTextFieldPktDataRx = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jButtonGetPacket = new javax.swing.JButton();
        jTextFieldRSSI = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jButtonGetRssi = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldFreq = new javax.swing.JTextField();
        jButtonSetFreq = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jTextFieldValidPkt = new javax.swing.JTextField();
        jTextFieldPktLength = new javax.swing.JTextField();
        jButtonServo1Left = new javax.swing.JButton();
        jButtonServo1Right = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldServo1 = new javax.swing.JTextField();
        jButtonStopTimer = new javax.swing.JButton();
        jButtonStartTimer = new javax.swing.JButton();
        jButtonRxOn = new javax.swing.JButton();
        jTextFieldServo2 = new javax.swing.JTextField();
        jButtonServo2Left = new javax.swing.JButton();
        jButtonServo2Right = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(580, 400));
        setPreferredSize(new java.awt.Dimension(580, 400));
        setSize(new java.awt.Dimension(580, 400));
        getContentPane().setLayout(null);

        jLabel1.setText("This is the SeaPI Controller GUI");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(210, 10, 175, 17);

        jButtonMoveServo.setText("move servo");
        jButtonMoveServo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonMoveServoMousePressed(evt);
            }
        });
        getContentPane().add(jButtonMoveServo);
        jButtonMoveServo.setBounds(70, 20, 77, 31);

        jTextField1.setText("1000");
        getContentPane().add(jTextField1);
        jTextField1.setBounds(10, 20, 43, 27);

        jButtonDumpRegisters.setText("Dump Registers");
        jButtonDumpRegisters.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonDumpRegistersMousePressed(evt);
            }
        });
        getContentPane().add(jButtonDumpRegisters);
        jButtonDumpRegisters.setBounds(460, 270, 101, 31);

        jButtonInitRFM.setText("Init RFM");
        jButtonInitRFM.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonInitRFMMousePressed(evt);
            }
        });
        getContentPane().add(jButtonInitRFM);
        jButtonInitRFM.setBounds(200, 40, 80, 31);

        jTextFieldPktDataTx.setText("hello world");
        getContentPane().add(jTextFieldPktDataTx);
        jTextFieldPktDataTx.setBounds(330, 150, 100, 27);

        jLabel3.setText("TX Packet Data");
        getContentPane().add(jLabel3);
        jLabel3.setBounds(220, 160, 94, 17);

        jButtonSendPacket.setText("Send Packet");
        jButtonSendPacket.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonSendPacketMousePressed(evt);
            }
        });
        getContentPane().add(jButtonSendPacket);
        jButtonSendPacket.setBounds(450, 150, 90, 31);

        jTextFieldPktDataRx.setText("hello world");
        jTextFieldPktDataRx.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldPktDataRxActionPerformed(evt);
            }
        });
        getContentPane().add(jTextFieldPktDataRx);
        jTextFieldPktDataRx.setBounds(300, 190, 150, 27);

        jLabel4.setText("RX Packet Data");
        getContentPane().add(jLabel4);
        jLabel4.setBounds(190, 200, 93, 17);

        jButtonGetPacket.setText("Get Pkt");
        jButtonGetPacket.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonGetPacketMousePressed(evt);
            }
        });
        getContentPane().add(jButtonGetPacket);
        jButtonGetPacket.setBounds(470, 190, 75, 31);

        jTextFieldRSSI.setText("0");
        getContentPane().add(jTextFieldRSSI);
        jTextFieldRSSI.setBounds(240, 350, 50, 27);

        jLabel8.setText("RSSI");
        getContentPane().add(jLabel8);
        jLabel8.setBounds(170, 350, 40, 20);

        jButtonGetRssi.setText("Get RSSI");
        jButtonGetRssi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonGetRssiMousePressed(evt);
            }
        });
        getContentPane().add(jButtonGetRssi);
        jButtonGetRssi.setBounds(300, 350, 60, 31);

        jLabel2.setText("Set Frequency");
        getContentPane().add(jLabel2);
        jLabel2.setBounds(12, 221, 79, 17);

        jTextFieldFreq.setText("433");
        getContentPane().add(jTextFieldFreq);
        jTextFieldFreq.setBounds(20, 240, 70, 27);

        jButtonSetFreq.setText("Set Freq");
        jButtonSetFreq.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonSetFreqMousePressed(evt);
            }
        });
        getContentPane().add(jButtonSetFreq);
        jButtonSetFreq.setBounds(10, 280, 80, 31);

        jLabel9.setText("Rx Pkt Length");
        getContentPane().add(jLabel9);
        jLabel9.setBounds(140, 270, 80, 20);

        jLabel10.setText("Valid Pkt");
        getContentPane().add(jLabel10);
        jLabel10.setBounds(140, 240, 60, 20);

        jTextFieldValidPkt.setText("no");
        getContentPane().add(jTextFieldValidPkt);
        jTextFieldValidPkt.setBounds(240, 230, 90, 30);

        jTextFieldPktLength.setText("0");
        getContentPane().add(jTextFieldPktLength);
        jTextFieldPktLength.setBounds(240, 270, 60, 30);

        jButtonServo1Left.setText("Left");
        jButtonServo1Left.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonServo1LeftMousePressed(evt);
            }
        });
        getContentPane().add(jButtonServo1Left);
        jButtonServo1Left.setBounds(10, 90, 50, 31);

        jButtonServo1Right.setText("Right");
        jButtonServo1Right.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonServo1RightMousePressed(evt);
            }
        });
        getContentPane().add(jButtonServo1Right);
        jButtonServo1Right.setBounds(150, 90, 50, 31);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Servo 1");
        getContentPane().add(jLabel5);
        jLabel5.setBounds(20, 70, 90, 17);

        jTextFieldServo1.setText("1000");
        getContentPane().add(jTextFieldServo1);
        jTextFieldServo1.setBounds(70, 90, 70, 27);

        jButtonStopTimer.setText("Stop Timer");
        jButtonStopTimer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonStopTimerMousePressed(evt);
            }
        });
        getContentPane().add(jButtonStopTimer);
        jButtonStopTimer.setBounds(460, 70, 90, 31);

        jButtonStartTimer.setText("Start Timer");
        jButtonStartTimer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonStartTimerMousePressed(evt);
            }
        });
        getContentPane().add(jButtonStartTimer);
        jButtonStartTimer.setBounds(459, 30, 90, 31);

        jButtonRxOn.setText("Rx On");
        jButtonRxOn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonRxOnMousePressed(evt);
            }
        });
        getContentPane().add(jButtonRxOn);
        jButtonRxOn.setBounds(470, 110, 70, 31);

        jTextFieldServo2.setText("1000");
        getContentPane().add(jTextFieldServo2);
        jTextFieldServo2.setBounds(70, 130, 70, 27);

        jButtonServo2Left.setText("Left");
        jButtonServo2Left.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonServo2LeftMousePressed(evt);
            }
        });
        getContentPane().add(jButtonServo2Left);
        jButtonServo2Left.setBounds(10, 130, 50, 31);

        jButtonServo2Right.setText("Right");
        jButtonServo2Right.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonServo2RightMousePressed(evt);
            }
        });
        getContentPane().add(jButtonServo2Right);
        jButtonServo2Right.setBounds(150, 130, 50, 31);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonMoveServoMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonMoveServoMousePressed
        // TODO add your handling code here:
        //grab data
        int value =0;
        String val_txt = this.jTextField1.getText();
        try{
            value = Integer.parseInt(val_txt);
            if(value>0 && value<10000)
            {
                
                gpioProvider.setPwm(PCA9685Pin.PWM_04, value);//middle
                gpioProvider.setPwm(PCA9685Pin.PWM_05, value);//middle
                int onOffValues[] = gpioProvider.getPwmOnOffValues(PCA9685Pin.PWM_04);
               log.fine("\nPOS mid: "+String.valueOf(onOffValues[0])+" "+String.valueOf(onOffValues[1]));
                
            }
        }
        catch(Exception e)
        {
            log.severe(e.getMessage());
        }
    }//GEN-LAST:event_jButtonMoveServoMousePressed

    private void jButtonDumpRegistersMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonDumpRegistersMousePressed
        byte[] packet = new byte[2];
        log.info("Starting Register Dump");
        log.info("----------------------");
        for(long i=0;i<0x80;i++)
        {
            if(i!=0x7f)
            {
                packet[0] = (byte) ((byte)i|SPI_READ_CMD);
                packet[1] = (byte)0x00;
                log.fine("Register "+(0xff&packet[0]));
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
                //System.out.print(" is: "+" "+String.valueOf((byte)(packet[1]&0x00FF)));//valueof
                log.fine(" is: "+" "+(0xff&packet[1]));//short
            }
            else
            {
                log.fine("Dumping RX FIFO...");
             //dumping rcv
                byte[] packetfifo = new byte[65];
                //clear out array
                for(int x=0;x<65;x++)
                {
                    packetfifo[x]=(byte)0x00;
                }
                //set tp rx fifo address
                packet[0] = (byte)(0xff&i|SPI_READ_CMD);
                //System.out.print("Register "+(0xff&packet[0]));
                //fifo is 64 bytes deep max
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packetfifo,65);
                //print out the data
                for(int x=0;x<65;x++)
                    log.fine(packetfifo[x]+" ");
                
                
            }
        }
    }//GEN-LAST:event_jButtonDumpRegistersMousePressed

    private void jButtonInitRFMMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonInitRFMMousePressed
        // TODO add your handling code here:
        initRFMBRegisters();
    }//GEN-LAST:event_jButtonInitRFMMousePressed

    private void jButtonSendPacketMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonSendPacketMousePressed
    
        //clear tx fifo and errors
        //clear fifos
        byte packet[] = new byte[2];
        packet[0]   =   (byte)(0x08|SPI_WRITE_CMD);
        packet[1]   =   (byte) (0x00|0x03);
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        packet[0]   =   (byte)(0x08|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        log.fine("Clear FIFOs");
        
        
        String tx_payload = jTextFieldPktDataTx.getText();
        byte payloadbytes[] = tx_payload.getBytes();
        //byte spiPacket[] = new byte[61];        
        byte spiPacket[] = new byte[payloadbytes.length+1];
        
        //write cmd to fifo (0x7F|0x80)
        spiPacket[0] = (byte)0xFF;
        //copy data over
        for(int i=0;i<(payloadbytes.length);i++)
        {
            spiPacket[i+1] = payloadbytes[i];
        }   
            
        //write to fifo    
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,spiPacket,spiPacket.length);
        log.fine("Packet Pushed..."+tx_payload);
        
        //set pkt length
        packet[0]   =   (byte)(0x3e|SPI_WRITE_CMD);
        packet[1]   =   (byte) payloadbytes.length;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        log.fine("Set Tx pkt length to "+String.valueOf(payloadbytes.length));
            
       //set to tx on
        packet[0] = (byte)(0x87);
        packet[1] = (byte)0x09;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
       
        
       
        
    }//GEN-LAST:event_jButtonSendPacketMousePressed

    private void jButtonGetRssiMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonGetRssiMousePressed
        // TODO add your handling code here:
        try 
        {
            jTextFieldRSSI.setText(String.valueOf(this.getRssi()));
            
        }
        catch(Exception e)
        {
            log.severe(e.getMessage());
        }
    }//GEN-LAST:event_jButtonGetRssiMousePressed

    private void jTextFieldPktDataRxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldPktDataRxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldPktDataRxActionPerformed

    private void jButtonGetPacketMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonGetPacketMousePressed
        // TODO add your handling code here:
        
        //enter rx mode
            byte packet[] = new byte[2];
            int pktvalid    =   0;
            int pktlength   =   0;
            /*
            packet[0] = (byte)(0x87);
            packet[1] = (byte)0x07;
            Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
            System.out.println("Entering RX Mode..." );
            */
            //look for valid pkt register
            packet[0] = (byte)(0x03|SPI_READ_CMD);
            packet[1] = (byte)0x00;
            Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
            if((0x02&packet[1])>0)
            {
                jTextFieldValidPkt.setText("GOT PKT!");
                pktvalid=1;
            }
            else
            {
                jTextFieldValidPkt.setText("NO PKT!");
            }
            //look for pkt length register value
            packet[0] = (byte)(0x4b|SPI_READ_CMD);
            packet[1] = (byte)0x00;
            Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
            try{
                jTextFieldPktLength.setText(String.valueOf((byte)(0xff&packet[1])));
                pktlength=packet[1];
            }
            catch(Exception e)
            {
                log.severe(e.getMessage());
                jTextFieldPktLength.setText("0");
                pktlength=0;
            }
            
            if(pktvalid==1 && pktlength>0)
            {
                log.fine("Got Packet!!");
                log.fine("Size is "+String.valueOf(pktlength));
                byte rxdata[] = new byte[pktlength+1];
                rxdata[0] = (byte)0x7f;
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,rxdata,pktlength+1);
                String data="";
                for(int z=1;z<(pktlength+1);z++)
                {
                    data = data.concat(Character.toString((char)rxdata[z]));
                    log.fine(Byte.toString((byte)(0xff&rxdata[z])));
                    log.fine("("+Character.toString((char)rxdata[z])+") ");
                }
                jTextFieldPktDataRx.setText(data);
                
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
                log.fine("Enter RX Mode...");
            }
            else
            {
                jTextFieldPktDataRx.setText("No Data...");
            }
            
            
    }//GEN-LAST:event_jButtonGetPacketMousePressed

    private void jButtonSetFreqMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonSetFreqMousePressed
        // TODO add your handling code here:
        float   freq;
        int     hbsel   =   0;
        int     fb      =   0;
        short   fc      =   0;
        try{
            freq = Float.valueOf(jTextFieldFreq.getText());
            //convert freq to register settings
            ////413 to 453
            if(freq>=413 && freq<=453)
            {
                log.fine("Valid Frequency...");
                if(freq>480)
                {
                    hbsel=1;
                    log.fine("HBSEL is 1");
                }else
                {
                    log.fine("HBSEL is 0");
                }
                
                //fb
                fb = (int)((freq/(10*(1+hbsel)))-24);
                log.fine("Fb is "+String.valueOf(fb));
                fc = (short)round(((freq/(10*(1+hbsel)))%1)*64000);
                log.fine("Fb is "+String.valueOf(fc));
                
                byte packet[] = new byte[2];
                packet[0] = (byte)(0x75|SPI_WRITE_CMD);
                if(hbsel==1)
                {
                    packet[1]   =  (byte)(0xff&fb|0x20);
                }
                else
                {
                    packet[1]   =  (byte)(0xff&fb);
                }
                //set fb
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
                
                //set fc
                byte packet_fc[] = new byte[3];
                packet_fc[0]    =   (byte)(0x76|SPI_WRITE_CMD);
                packet_fc[1]    =   (byte)(0xff&(fc>>8));
                packet_fc[2]    =   (byte)(0xff&fc);
                Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet_fc,3);
            }
            
        }
        catch(Exception e)
        {
            //invalid conversion
            log.severe(e.getMessage());
            log.severe("Bad Frequency...");
        }
    }//GEN-LAST:event_jButtonSetFreqMousePressed

    private void jButtonServo1LeftMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonServo1LeftMousePressed
        // TODO add your handling code here:
        //Send servo command 1 left
        
        short servo1 = Short.valueOf(jTextFieldServo1.getText());
        servo1-=100;
        if(servo1<600)servo1=600;
        jTextFieldServo1.setText(String.valueOf(servo1));
        /*
        System.out.print("Sending Servo1 val: "+String.format("%02X",(byte)(0xff&(servo1>>8))));
        System.out.println(" "+String.format("%02X",(byte)(0xff&(servo1&0xff))));
        
        
        byte datapacket[]   =   new byte[3];
        datapacket[0]   =   SEAPI_MSGTYPE_SERVO;
        datapacket[1]   =   (byte)(servo1 & 0xff);
        datapacket[2]   =   (byte)((servo1 >> 8) & 0xff);
        
        
        sendSpiPacket(datapacket);    
        */
        sendServoPositions();
    }//GEN-LAST:event_jButtonServo1LeftMousePressed

    private void jButtonStartTimerMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonStartTimerMousePressed
        // TODO add your handling code here:
        this.rxPacketTimer.start();
    }//GEN-LAST:event_jButtonStartTimerMousePressed

    private void jButtonStopTimerMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonStopTimerMousePressed
        // TODO add your handling code here:
        this.rxPacketTimer.stop();
    }//GEN-LAST:event_jButtonStopTimerMousePressed

    private void jButtonRxOnMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonRxOnMousePressed
        // TODO add your handling code here:
        byte packet[] = new byte[2];
        packet[0] = (byte)(0x07|SPI_WRITE_CMD);
        packet[1] = (byte)0x07;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0, packet,2);
        log.fine("Entering RX mode...");
              
    }//GEN-LAST:event_jButtonRxOnMousePressed

    private void jButtonServo1RightMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonServo1RightMousePressed
        // TODO add your handling code here:
                //Send servo command 1 left
        
        short servo1 = Short.valueOf(jTextFieldServo1.getText());
        servo1+=100;
        if(servo1<600)servo1=600;
        jTextFieldServo1.setText(String.valueOf(servo1));
       /*
        System.out.print("Sending Servo1 val: "+String.format("%02X",(byte)(0xff&(servo1>>8))));
        System.out.println(" "+String.format("%02X",(byte)(0xff&(servo1&0xff))));
        
        
        byte datapacket[]   =   new byte[3];
        datapacket[0]   =   SEAPI_MSGTYPE_SERVO;
        datapacket[1]   =   (byte)(servo1 & 0xff);
        datapacket[2]   =   (byte)((servo1 >> 8) & 0xff);
        
        
        sendSpiPacket(datapacket);    
        */
        sendServoPositions();
    }//GEN-LAST:event_jButtonServo1RightMousePressed

    private void jButtonServo2LeftMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonServo2LeftMousePressed
        // TODO add your handling code here:
                  //Send servo command 1 left
        
        short servo2 = Short.valueOf(jTextFieldServo2.getText());
        servo2-=100;
        if(servo2<600)servo2=600;
        jTextFieldServo2.setText(String.valueOf(servo2));
       
        /*
        System.out.print("Sending Servo2 val: "+String.format("%02X",(byte)(0xff&(servo2>>8))));
        System.out.println(" "+String.format("%02X",(byte)(0xff&(servo2&0xff))));
        
        
        byte datapacket[]   =   new byte[3];
        datapacket[0]   =   SEAPI_MSGTYPE_SERVO;
        datapacket[1]   =   (byte)(servo2 & 0xff);
        datapacket[2]   =   (byte)((servo2 >> 8) & 0xff);
        
        
        sendSpiPacket(datapacket);
        */
        sendServoPositions();
    }//GEN-LAST:event_jButtonServo2LeftMousePressed
    public void sendServoPositions()
    {
        short servo1;
        short servo2;
        //grab all desired servo positions
        try{
            servo1 = Short.valueOf(jTextFieldServo1.getText());
            servo2 = Short.valueOf(jTextFieldServo2.getText());
            
            byte datapacket[]   =   new byte[1+2*2];
            datapacket[0]   =   SEAPI_MSGTYPE_SERVO;
            datapacket[1]   =   (byte)(servo1 & 0xff);
            datapacket[2]   =   (byte)((servo1 >> 8) & 0xff);
            datapacket[3]   =   (byte)(servo2 & 0xff);
            datapacket[4]   =   (byte)((servo2 >> 8) & 0xff);

            sendSpiPacket(datapacket);
        }
        catch(Exception e)
        {
            log.severe(e.getMessage());
            log.severe("Bad servo values...");
            return;
        }
        
    }
    private void jButtonServo2RightMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonServo2RightMousePressed
        // TODO add your handling code here:
                //Send servo command 1 left
        
        short servo2 = Short.valueOf(jTextFieldServo2.getText());
        servo2+=100;
        if(servo2<600)servo2=600;
        jTextFieldServo2.setText(String.valueOf(servo2));
       /*
        System.out.print("Sending Servo2 val: "+String.format("%02X",(byte)(0xff&(servo2>>8))));
        System.out.println(" "+String.format("%02X",(byte)(0xff&(servo2&0xff))));
        
        
        byte datapacket[]   =   new byte[3];
        datapacket[0]   =   SEAPI_MSGTYPE_SERVO;
        datapacket[1]   =   (byte)(servo2 & 0xff);
        datapacket[2]   =   (byte)((servo2 >> 8) & 0xff);
        
        
        sendSpiPacket(datapacket);
        */
       sendServoPositions();
    }//GEN-LAST:event_jButtonServo2RightMousePressed
   
    public void sendSpiPacket(byte[] datapkt)
    {
        byte packet[] = new byte[2];
        
        
        log.fine("Sending Packet...");
        //print data in
        log.fine("Msg Length is ");
        log.fine(String.valueOf(datapkt.length));
      
        log.fine("Msg Data is ");
        for(int z=0;z<datapkt.length;z++)
            log.fine(Short.toString(datapkt[z]));
        
        
        //clear tx fifo
        packet[0]   =   (byte)(0x08|SPI_WRITE_CMD);
        packet[1]   =   (byte) (0x00|0x01);
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        packet[0]   =   (byte)(0x08|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        
        //copy pkt .over
        byte datapacket[]   =   new byte[datapkt.length+1];
        datapacket[0] = (byte)(0x7f|SPI_WRITE_CMD);//set to write command and fifo (7f)
        for(int x=0;x<datapkt.length;x++)
        {
            datapacket[x+1] = datapkt[x];
        }
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0, datapacket,datapacket.length);
        
        //set length of tx register
        packet[0] = (byte)(0x3e|SPI_WRITE_CMD);
        packet[1] = (byte)(datapkt.length);
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0, packet,2);
        
        //set tx mode
        packet[0] = (byte)(0x07|SPI_WRITE_CMD);
        packet[1] = (byte)0x0B;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0, packet,2);
        
        //wait for valid tx packet...
        log.finest("Waiting for Valid TX Packet...");
        packet[0] = (byte)0x03;
        packet[1] = (byte)0x00;
        while( (packet[1]&0x04)!=4 && (packet[1]&0x01)!=1)
        {   
            log.finest("Still Waiting...");
            //read status again
            packet[0] = (byte)0x03;
            Spi.wiringPiSPIDataRW(Spi.CHANNEL_0, packet,2);
        }
        //go back to rx mode
        packet[0] = (byte)(0x07|SPI_WRITE_CMD);
        packet[1] = (byte)0x07;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0, packet,2);
        log.finest("Back to RX Mode...");
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
        packet[1]   =   (byte) (0x00|0x03);
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        packet[0]   =   (byte)(0x08|SPI_WRITE_CMD);
        packet[1]   =   (byte) 0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0,packet,2);
        
        log.info("Finished Initializing RFM Registers!");
    }
    public int getRssi()
    {
        int rssi = 0;
        byte[] packet = new byte[2];
        packet[0]   =   (byte)0x26;
        packet[1]   =   (byte)0x00;
        Spi.wiringPiSPIDataRW(Spi.CHANNEL_0, packet,2);
        rssi = (int)packet[1];
               
        
        return rssi;
    }
    public void spiSetup()
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
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SeaPIMainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SeaPIMainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SeaPIMainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SeaPIMainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SeaPIMainFrame().setVisible(true);
            }
        });
    }
    public int readConfigFile()
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
                log.info(strLine);
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
    public int seaPiInit(int mode) 
    {
        int     result = ERROR_CODE_SUCESS;
        // TODO code application logic here
        log.info("Starting SeaPI...");
        
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
                        if(SeaPIMainFrame.USB_HID_BBCCONTROLLER_IDPRODUCT == descriptor.idProduct() && 
                           SeaPIMainFrame.USB_HID_BBCCONTROLLER_IDVENDOR == descriptor.idVendor() &&
                           SeaPIMainFrame.USB_HID_BBCCONTROLLER_MFR == descriptor.iManufacturer() &&
                           SeaPIMainFrame.USB_HID_BBCCONTROLLER_PRODUCT == descriptor.iProduct() )
                        {
                            System.out.println("This IS the Device we are looking for!!!");
                            System.out.println(descriptor.dump());
                            
                            
                            usbControllerDevice = device;
                            ConfigDescriptor configDes = new ConfigDescriptor();
                            LibUsb.getConfigDescriptor(usbControllerDevice, (byte)0, configDes);
                            System.out.println("Config Descriptor: "+configDes.dump());
                           
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
                            UsbInterruptThread usbThread = new UsbInterruptThread(usbDeviceHandle,this.gamepad_data);
                            usbThread.start();
                            /*
                            //Now what
                            ByteBuffer buffer = BufferUtils.allocateByteBuffer(64).order(
                            ByteOrder.LITTLE_ENDIAN);
                            //Transfer transfer = LibUsb.allocTransfer();
                            java.nio.IntBuffer transferred = java.nio.IntBuffer.allocate(3);
                            
                            int result_xfer = LibUsb.bulkTransfer(usbDeviceHandle,(byte)1, buffer, transferred, 3000);
                            if(LibUsb.SUCCESS != result_xfer)
                            {
                                System.out.println("Error Transfer: "+String.valueOf(result_xfer));
                            }
                            else
                            {
                                System.out.println(transferred.get() + " bytes sent");
                            }
                           */ 
                            
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
            
            //Testing controller
            /*
            int mycount = 0;
            while(mycount<300)
            {
                try{
                    mycount++;
                    sleep(1000);
                    if(0<this.gamepad_data.getButton_x())System.out.println("X");
                    if(0<this.gamepad_data.getButton_y())System.out.println("Y");
                    if(0<this.gamepad_data.getButton_a())System.out.println("A");
                    if(0<this.gamepad_data.getButton_b())System.out.println("B");
                    if(0<this.gamepad_data.getButton_start())System.out.println("START");
                    if(0<this.gamepad_data.getButton_select())System.out.println("SELECT");
                    if(0<this.gamepad_data.getButton_left1())System.out.println("L1");
                    if(0<this.gamepad_data.getButton_right1())System.out.println("R1");
                    if(0<this.gamepad_data.getButton_leftthumb())System.out.println("LB");
                    if(0<this.gamepad_data.getButton_rightthumb())System.out.println("RB");
                    
                    if(128!=this.gamepad_data.getThumbleft_x())System.out.println("L Stick X");
                    if(128!=this.gamepad_data.getThumbleft_y())System.out.println("L Stick Y");
                    
                    if(128!=this.gamepad_data.getThumbright_x())System.out.println("R Stick X");
                    if(128!=this.gamepad_data.getThumbright_y())System.out.println("R Stick Y");
                    
                    if(15!=this.gamepad_data.getDpad())System.out.println("D-PAD");
                    
                }
                catch(Exception e)
                {
                    
                }
            
            }
            */
            
            
            //-------------done shutdown
            System.out.println("Done Setting up usb HID...");
            
            System.out.println("Try bluetooth...");
            //http://www.aviyehuda.com/blog/2010/01/08/connecting-to-bluetooth-devices-with-java/
            System.out.println("This isnt working yet.... :(");
            /*
            try{
                LocalDevice localDevice = LocalDevice.getLocalDevice();
                DiscoveryAgent agent = localDevice.getDiscoveryAgent();
                agent.startInquiry(DiscoveryAgent.GIAC, new BluetoothDiscoverListener());
                try {
                    synchronized(lock){
                        lock.wait();
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                System.out.println("Device Inquiry Completed. ");
                
                RemoteDevice[] remoteDevices = agent.retrieveDevices(0);
                System.out.println("# of Found Devices: "+String.valueOf(remoteDevices.length));
                
                if(remoteDevices.length>0)
                {
                    //HumanInterfaceDeviceService 0x1124 
                    UUID[] uuidSet = new UUID[1];
                    uuidSet[0]=new UUID(0x1124); //HumanInterfaceDeviceService

                    int[] attrIDs =  new int[] {
                            0x0100 // Service name
                    };
                    for(int i=0;i<remoteDevices.length;i++)
                    {
                        agent.searchServices(null,uuidSet,remoteDevices[i], new BluetoothDiscoverListener());
                        try {
                            synchronized(lock){
                                lock.wait();
                            }
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                
                
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            
    

            */
            System.out.println("...done bluetooth!");
            
            //has Radio RFM
            this.initRFMBRegisters();
            //set up timer
            this.rxPacketTimer = new Timer(SEAPI_RFM22B_POLL_TIME_MSEC,new RcvListener(this));
            rxPacketTimer.start();
            log.fine("Timer started...");
            //no PWM controller
            //Set up i2C for joystick reading ADC
            try{
                gpioProviderADC = new ADS1015GpioProvider(I2CBus.BUS_1,ADS1015GpioProvider.ADS1015_ADDRESS_0x48);
                
                gpioProviderADC.setProgrammableGainAmplifier(ProgrammableGainAmplifierValue.PGA_4_096V, ADS1015Pin.ALL);
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
        else
        {
            //vehicle
            //has radio RFM
            this.initRFMBRegisters();
            //set up timer
            this.rxPacketTimer = new Timer(SEAPI_RFM22B_POLL_TIME_MSEC,new RcvListener(this));
            rxPacketTimer.start();
            log.fine("Timer started...");
            //has PWM controller(I2C)
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
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_06, "not used");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_07, "not used");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_08, "not used");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_09, "not used");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_10, "not used");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_11, "not used");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_12, "not used");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_13, "not used");
                 gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_14, "not used");




                 //gpioProvider.reset();
                 log.info("**************************");
                 log.info("System Configuration Summary");
                 log.info("Frequency is: "+gpioProvider.getFrequency().toString());
               /*  
                 Collection<GpioPin> pinCollection;
                 pinCollection = gpio.getProvisionedPins();
                 for(int i=0;i<pinCollection.size();i++)
                 {

                 }
               */


                 int u=0;
                 //move servo
                 while(u!=0)
                 {
                     //The value fed to setPWM is millisecon duration
                     System.out.print("MOVE!\n");

                     gpioProvider.setPwm(PCA9685Pin.PWM_04, 1500);//middle
                     int[] onOffValues  =   gpioProvider.getPwmOnOffValues(PCA9685Pin.PWM_04);
                     System.out.print("\nPOS mid: "+String.valueOf(onOffValues[0])+" "+String.valueOf(onOffValues[1]));
                     sleep(3000);


                       gpioProvider.setPwm(PCA9685Pin.PWM_04, 2100);//right 90
                     int[] onOffValues2  =   gpioProvider.getPwmOnOffValues(PCA9685Pin.PWM_04);
                     System.out.print("\nPOS right: "+String.valueOf(onOffValues2[0])+" "+String.valueOf(onOffValues2[1]));
                     sleep(3000);



                     gpioProvider.setPwm(PCA9685Pin.PWM_04, 1000);//left 90
                     int[] onOffValues3  =   gpioProvider.getPwmOnOffValues(PCA9685Pin.PWM_04);
                     System.out.print("\nPOS left: "+String.valueOf(onOffValues3[0])+" "+String.valueOf(onOffValues3[1]));
                     sleep(3000);
                 }

                //pwmController = new PWMController(I2CFactory.getInstance(rpi_i2c_bus_addr),i2c_addr_pwm_controller1);

                 //set freq
                // pwmController.setFreq(50);
                 //pwmController.setPWM4();



            }
            catch(Exception e)
            {
                //serious error
                log.severe(e.getMessage());
                //TODO: Set an LED indicator for an error
                result = ERROR_CODE_FAILURE;
            }
        }
       
       
         /*  
        try{
            System.out.println("---Begin Test Loop Thumbstick----");
            int count = 0;
            while(count < 100)
            {
                //THIS WORK!!
                System.out.println(String.valueOf(gpioProviderADC.getValue(ADS1015Pin.INPUT_A0)));
                Thread.sleep(1000);
                count++;
            }
            //Thread.sleep(60000);
        }
        catch(Exception e)
        {

        }
        */
        
        return result;
    }
    public void commandServo(int servo_number,int position)
    {
        //2600 is far left
        //1850 middle
        //950  right

        if(servo_number>=SEAPI_MIN_SERVO_NUMBER && servo_number<=SEAPI_MAX_SERVO_NUMBER &&
                position>=SEAPI_MIN_SERVO_POS_MSEC && position<=SEAPI_MAX_SERVO_POS_MSEC)
        {
            //The value fed to setPWM is millisecond duration
            switch(servo_number){
                case 1:
                    log.fine("Servo 1 (4) Moving ..."+String.valueOf(position));
                    gpioProvider.setPwm(PCA9685Pin.PWM_04, position);
                    break;
                case 2:
                    log.fine("Servo 2 (5) Moving ..."+String.valueOf(position));
                    gpioProvider.setPwm(PCA9685Pin.PWM_05, position);
                    break;    
                default:
                    log.fine("Servo not defined...");
                    break;
            }
        }
        else
        {
            //bad params
            log.fine("Bad servo params! Servo #"+String.valueOf(servo_number)+" Pos:"+String.valueOf(position));
        }
        //servo numbers 1 -6
    }
    public void close()
    {
        int result;
        //Shutdown things here like USB HID
        //-------------start shutdown
            System.out.println("Shutting down USB HID....");
            // Release the interface
            result = LibUsb.releaseInterface(usbDeviceHandle, 0);
            if (result != LibUsb.SUCCESS)
            {
                //problem;
            }

            // Re-attach kernel driver if needed
            // Check if kernel driver is attached to the interface
            int attached = LibUsb.kernelDriverActive(usbDeviceHandle, 0);
            if (attached == 1)
            {
                LibUsb.attachKernelDriver(usbDeviceHandle, 0);
                if (result != LibUsb.SUCCESS)
                {
                    //error
                }
            }
        
    }
    public void processMsg(byte[] msg)
    {
        log.fine("I gots a message!!!");
        if(msg.length<1)
        {
            //emtpy msg....
            return;
        }
        //first byte is msg type
        int msgtype = msg[0];
        int datalength = msg.length-1;
        
        switch(msgtype)
        {
            case SeaPIMainFrame.SEAPI_MSGTYPE_SERVO:
                //get each servo position
                for(int servo=1;servo<=Math.floor(datalength/2);servo++)
                {
                    //grab two bytes
                    log.fine("Grabbing bytes "+String.valueOf(servo*2-1)+" and "+String.valueOf(servo*2));
                    log.fine("Grabbing these bytes-> "+String.format("%02X",(byte)(msg[servo*2]))+" "+String.format("%02X",(byte)(msg[servo*2-1])));
                    short servo_pos = (short)( (msg[servo*2]<<8) | (0xff&msg[servo*2-1]) );
                    
                    log.fine("Servo "+String.valueOf(servo)+" to move "+String.valueOf(servo_pos));
                    commandServo(servo,servo_pos);
                }
                break;
            case SeaPIMainFrame.SEAPI_MSGTYPE_BALLAST:
                break;
            case SeaPIMainFrame.SEAPI_MSGTYPE_MOTOR:
                break;
                     
            default:
                //unknown msg type
                log.fine("Unknown msg type "+String.valueOf(msgtype)+" rcvd!");
        }
        
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonDumpRegisters;
    private javax.swing.JButton jButtonGetPacket;
    private javax.swing.JButton jButtonGetRssi;
    private javax.swing.JButton jButtonInitRFM;
    private javax.swing.JButton jButtonMoveServo;
    private javax.swing.JButton jButtonRxOn;
    private javax.swing.JButton jButtonSendPacket;
    private javax.swing.JButton jButtonServo1Left;
    private javax.swing.JButton jButtonServo1Right;
    private javax.swing.JButton jButtonServo2Left;
    private javax.swing.JButton jButtonServo2Right;
    private javax.swing.JButton jButtonSetFreq;
    private javax.swing.JButton jButtonStartTimer;
    private javax.swing.JButton jButtonStopTimer;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextFieldFreq;
    private javax.swing.JTextField jTextFieldPktDataRx;
    private javax.swing.JTextField jTextFieldPktDataTx;
    private javax.swing.JTextField jTextFieldPktLength;
    private javax.swing.JTextField jTextFieldRSSI;
    private javax.swing.JTextField jTextFieldServo1;
    private javax.swing.JTextField jTextFieldServo2;
    private javax.swing.JTextField jTextFieldValidPkt;
    // End of variables declaration//GEN-END:variables
}
