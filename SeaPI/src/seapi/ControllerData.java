/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seapi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 *
 * @author jorge
 */
public class ControllerData {
    public static final int     XBOX_TYPE  = 100;
    public static final int     BBC_TYPE   = 101;
    private int    controller_type;
    public volatile int     button_x;
    public int     button_x_previous;
    public volatile int     button_y;
    public int     button_y_previous;
    public volatile int     button_a;
    public int     button_a_previous;
    public volatile int     button_b;
    public int     button_b_previous;
    public volatile int     button_start;
    public int     button_start_previous;
    public volatile int     button_select;
    public int     button_select_previous;
    public volatile int     button_left1;
    public int     button_left1_previous;
    public volatile int     button_right1;
    public int     button_right1_previous;
    public volatile int     button_leftthumb;
    public int     button_leftthumb_previous;
    public volatile int     button_rightthumb;
    public int     button_rightthumb_previous;
    public volatile int     thumbleft_x;
    public volatile int     thumbleft_y;
    public volatile int     thumbright_x;
    public volatile int     thumbright_y;
    public volatile int     dpad;
    
    private volatile int    left_trigger;
    private volatile int    right_trigger;
    
    private boolean button_x_pressed;

    
    private boolean button_y_pressed;
    private boolean button_a_pressed;
    private boolean button_b_pressed;
    private boolean button_start_pressed;
    private boolean button_select_pressed;
    private boolean button_left1_pressed;
    private boolean button_right1_pressed;
    private boolean button_leftthumb_pressed;
    private boolean button_rightthumb_pressed;
    //Masks
    private int     BBC_MASK_X_BUTTON    = 0x08;
    private int     BBC_MASK_Y_BUTTON    = 0x10;
    private int     BBC_MASK_A_BUTTON    = 0x01;
    private int     BBC_MASK_B_BUTTON    = 0x02;
    private int     BBC_MASK_START_BUTTON    = 0x08;
    private int     BBC_MASK_SELECT_BUTTON    = 0x04;
    private int     BBC_MASK_L1_BUTTON    = 0x40;
    private int     BBC_MASK_R1_BUTTON    = 0x80;
    private int     BBC_MASK_LB_BUTTON    = 0x20;
    private int     BBC_MASK_RB_BUTTON    = 0x40;
    
    ControllerData()
    {
        controller_type     =   0;
        button_x            =   0;
        button_x_previous   =   0;
        button_y            =   0;
        button_y_previous   =   0;
        button_a            =   0;
        button_a_previous   =   0;
        button_b            =   0;
        button_b_previous   =   0;
        button_start        =   0;
        button_start_previous        =   0;
        button_select       =   0;
        button_select_previous       =   0;
        button_left1        =   0;
        button_left1_previous        =   0;
        button_right1       =   0;
        button_right1_previous       =   0;
        button_leftthumb    =   0;
        button_leftthumb_previous    =   0;
        button_rightthumb   =   0;
        button_rightthumb_previous   =   0;
        thumbleft_x         =   128;
        thumbleft_y         =   128;
        thumbright_x        =   128;
        thumbright_y        =   128;
        dpad                =   0;
        left_trigger        =   0;
        right_trigger        =   0;
    }
    public void setControllerType(int x)
    {
        switch(x)
        {
            case ControllerData.XBOX_TYPE:
                controller_type = x;
                break;
            case ControllerData.BBC_TYPE:
                controller_type = x;
                break;
            default:
                controller_type = 0;
                break;
        }
        
    }
    
    public int getControllerType()
    {
        return controller_type;
    }
    public int getButton_x() {
        return button_x;
    }

    public int getButton_y() {
        return button_y;
    }

    public int getButton_a() {
        return button_a;
    }

    public int getButton_b() {
        return button_b;
    }

    public int getButton_start() {
        return button_start;
    }

    public int getButton_select() {
        return button_select;
    }

    public int getButton_left1() {
        return button_left1;
    }

    public int getButton_right1() {
        return button_right1;
    }

    public int getButton_leftthumb() {
        return button_leftthumb;
    }

    public int getButton_rightthumb() {
        return button_rightthumb;
    }

    public int getThumbleft_x() {
        return thumbleft_x;
    }

    public int getThumbleft_y() {
        return thumbleft_y;
    }

    public int getThumbright_x() {
        return thumbright_x;
    }

    public int getThumbright_y() {
        return thumbright_y;
    }

    public int getDpad() {
        return dpad;
    }
    public int getLeftTrigger() {
        return left_trigger;
    }
    public int getRightTrigger() {
        return right_trigger;
    }
    public void updateData(ByteBuffer in_data)
    {
        //System.out.println("Updating...");
        if(in_data==null)
            return;
        
        switch(this.controller_type)
        {
            case ControllerData.BBC_TYPE:
                updateBBCData(in_data);
                break;
            case ControllerData.XBOX_TYPE:
                updateXboxData(in_data);
                break;
            default:
                break;
        }
        
        
    }
    private void updateXboxData(ByteBuffer in_data)
    {
        
        //20 Byes long msg
        
        //Byte 3
        byte XBOX_BUTTON_X = (byte)0x40;
        byte XBOX_BUTTON_Y = (byte)0x80;
        byte XBOX_BUTTON_A = (byte)0x10;
        byte XBOX_BUTTON_B = (byte)0x20;
        byte XBOX_BUTTON_LBUMPER = (byte)0x01;
        byte XBOX_BUTTON_RBUMPER = (byte)0x02;
        byte XBOX_BUTTON_CENTER = (byte)0x04;
        
        //byte 2
        byte XBOX_BUTTON_BACK = (byte)0x20;
        byte XBOX_BUTTON_START = (byte)0x10;
        byte XBOX_BUTTON_DLEFT = (byte)0x04;
        byte XBOX_BUTTON_DRIGHT = (byte)0x08;
        byte XBOX_BUTTON_DDOWN = (byte)0x02;
        byte XBOX_BUTTON_DUP = (byte)0x01;
        byte XBOX_BUTTON_LEFTCLICK = (byte)0x40;
        byte XBOX_BUTTON_RIGHTCLICK = (byte)0x80;
        
        //byte 4 XBOX_BUTTON_LEFT_TRIGGER
        //byte 5 XBOX_BUTTON_RIGHT_TRIGGER
        
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        
        
        bb.put(in_data.get(6));
        bb.put(in_data.get(7));
        thumbleft_x             =    bb.getShort(0);
        
        bb.clear();
        bb.put(in_data.get(8));
        bb.put(in_data.get(9));
        thumbleft_y             =   bb.getShort(0);
        
        bb.clear();
        bb.put(in_data.get(10));
        bb.put(in_data.get(11));
        thumbright_x            =   bb.getShort(0);
        
        bb.clear();
        bb.put(in_data.get(12));
        bb.put(in_data.get(13));
        thumbright_y            =   bb.getShort(0);
        
        this.button_a           =   in_data.get(3)&XBOX_BUTTON_A&0xff;
        this.button_b           =   in_data.get(3)&XBOX_BUTTON_B&0xff;
        this.button_x           =   in_data.get(3)&XBOX_BUTTON_X&0xff;
        this.button_y           =   in_data.get(3)&XBOX_BUTTON_Y&0xff;
        this.button_left1       =   in_data.get(3)&XBOX_BUTTON_LBUMPER&0xff;
        this.button_right1      =   in_data.get(3)&XBOX_BUTTON_RBUMPER&0xff;
        
        this.button_leftthumb   =   in_data.get(2)&XBOX_BUTTON_LEFTCLICK&0xff;
        this.button_rightthumb  =   in_data.get(2)&XBOX_BUTTON_RIGHTCLICK&0xff;
        this.dpad               =   in_data.get(2)&0x0f;
        this.button_start       =   in_data.get(2)&XBOX_BUTTON_START&0xff;
        this.button_select      =   in_data.get(2)&XBOX_BUTTON_BACK&0xff;
        
        this.left_trigger       =   in_data.get(4);
        this.right_trigger      =   in_data.get(5);
    }
    private void updateBBCData(ByteBuffer in_data)
    {
        //5 and 6 x y for right thumb
        //byte 2 d pad
        //byte 0 and 1 buttons
        //byte 0  x,y,a,b,left,right
        //byte 1 thumb click, start, select
        //DPAD
        //up is 0
        //none is 0F
        //left 06
        //right 02
        //down 04
        //up left 07
        //up right 1
        //down left 05
        //down right 03
        
        thumbleft_x         =   in_data.get(3)& 0x00ff;
        thumbleft_y         =   in_data.get(4)& 0x00ff;
        thumbright_x        =   in_data.get(5)& 0x00ff;
        thumbright_y        =   in_data.get(6)& 0x00ff;
        
        button_x_previous   = button_x;
        button_x            =   in_data.get(0)&BBC_MASK_X_BUTTON&0xff;
        if(button_x_previous>0 && button_x==0)button_x_pressed = true;
            
        
        button_y_previous = button_y;
        button_y            =   in_data.get(0)&BBC_MASK_Y_BUTTON&0xff;
        if(button_y_previous>0 && button_y==0)button_y_pressed = true;
        
        button_a_previous = button_a;
        button_a            =   in_data.get(0)&BBC_MASK_A_BUTTON&0xff;
        if(button_a_previous>0 && button_a==0)button_a_pressed = true;
        
        button_b_previous = button_b;
        button_b            =   in_data.get(0)&BBC_MASK_B_BUTTON&0xff;
        if(button_b_previous>0 && button_b==0)button_b_pressed = true;
        
        button_left1_previous = button_left1;
        button_left1        =   in_data.get(0)&BBC_MASK_L1_BUTTON&0xff;
        if(button_left1_previous>0 && button_left1==0)button_left1_pressed = true;
        
        button_right1_previous = button_right1;
        button_right1       =   in_data.get(0)&BBC_MASK_R1_BUTTON&0xff;
        if(button_right1_previous>0 && button_right1==0)button_right1_pressed = true;
        
        button_start_previous = button_start;
        button_start        =   in_data.get(1)&BBC_MASK_START_BUTTON&0xff;
        if(button_start_previous>0 && button_start==0)button_start_pressed = true;
        
        button_select_previous = button_select;
        button_select       =   in_data.get(1)&BBC_MASK_SELECT_BUTTON&0xff;
        if(button_select_previous>0 && button_select==0)button_select_pressed = true;
        
        button_leftthumb_previous = button_leftthumb;
        button_leftthumb    =   in_data.get(1)&BBC_MASK_LB_BUTTON&0xff;
        if(button_leftthumb_previous>0 && button_leftthumb==0)button_leftthumb_pressed = true;
        
        button_rightthumb_previous = button_rightthumb;
        button_rightthumb   =   in_data.get(1)&BBC_MASK_RB_BUTTON&0xff;
        if(button_rightthumb_previous>0 && button_rightthumb==0)button_rightthumb_pressed = true;
        
        dpad                =   in_data.get(2)&0xff;
    }
    public boolean isButton_x_pressed() {
        if(button_x_pressed)
        {
            button_x_pressed = false;
            return true;
        }
        else
            return false;
    }

    public boolean isButton_y_pressed() {
      
        if(button_y_pressed)
        {
            button_y_pressed = false;
            return true;
        }
        else
            return false;
    }

    public boolean isButton_a_pressed() {
        if(button_a_pressed)
        {
            button_a_pressed = false;
            return true;
        }
        else
            return false;
    }

    public boolean isButton_b_pressed() {
        if(button_b_pressed)
        {
            button_b_pressed = false;
            return true;
        }
        else
            return false;
    }

    public boolean isButton_start_pressed() {
        if(button_start_pressed)
        {
            button_start_pressed = false;
            return true;
        }
        else
            return false;
    }

    public boolean isButton_select_pressed() {
        if(button_select_pressed)
        {
            button_select_pressed = false;
            return true;
        }
        else
            return false;
    }

    public boolean isButton_left1_pressed() {
        if(button_left1_pressed)
        {
            button_left1_pressed = false;
            return true;
        }
        else
            return false;
    }

    public boolean isButton_right1_pressed() {
        if(button_right1_pressed)
        {
            button_right1_pressed = false;
            return true;
        }
        else
            return false;
    }

    public boolean isButton_leftthumb_pressed() {
        if(button_leftthumb_pressed)
        {
            button_leftthumb_pressed = false;
            return true;
        }
        else
            return false;
    }

    public boolean isButton_rightthumb_pressed() {
        if(button_rightthumb_pressed)
        {
            button_rightthumb_pressed = false;
            return true;
        }
        else
            return false;
    }
    public byte[] getButtonMessage()
    {
        byte[] button_msg = new byte[2];
        //  xyab L1R1SelStart ThmbL ThmbR
        //0b1111 1 1 1  1     1     1     000000
        
        //clear all
        button_msg[0]=(byte)0x00;
        button_msg[1]=(byte)0x00;
        
        
        if(this.isButton_a_pressed())
            button_msg[0] = (byte)((byte)button_msg[0]|(byte)0b00100000);
        
        if(this.isButton_b_pressed())
            button_msg[0] = (byte)((byte)button_msg[0]|(byte)0b00010000);
        
        if(this.isButton_x_pressed())
            button_msg[0] = (byte)((byte)button_msg[0]|(byte)0b10000000);
        
        if(this.isButton_y_pressed())
            button_msg[0] = (byte)((byte)button_msg[0]|(byte)0b01000000);
        
        if(this.isButton_left1_pressed())
            button_msg[0] = (byte)((byte)button_msg[0]|(byte)0b00001000);
        
        if(this.isButton_right1_pressed())
            button_msg[0] = (byte)((byte)button_msg[0]|(byte)0b00000100);
        
        if(this.isButton_select_pressed())
            button_msg[0] = (byte)((byte)button_msg[0]|(byte)0b00000010);
        
        if(this.isButton_start_pressed())
            button_msg[0] = (byte)((byte)button_msg[0]|(byte)0b00000001);
        
        
        if(this.isButton_leftthumb_pressed())
            button_msg[1] = (byte)((byte)button_msg[1]|(byte)0b10000000);
        
        if(this.isButton_rightthumb_pressed())
            button_msg[1] = (byte)((byte)button_msg[1]|(byte)0b01000000);
        
        return button_msg;
    }
    public byte[] getAnalogMessage()
    {
        byte[] analog_msg;
        switch(this.controller_type)
        {
            case ControllerData.BBC_TYPE:
                analog_msg = new byte[6];
                //first byte type
                analog_msg[0] = SeaPIMainFrame.SEAPI_MSGTYPE_ANALOG_CTL;
                //0 is left stick x
                //1 is left stick y
                //2 is right stick x
                //3 is right stick y
                //4 is dpad
               // System.out.println("Thumb Left: "+String.valueOf(this.getThumbleft_x())+" "+String.valueOf(this.getThumbleft_y()));
                analog_msg[1] = (byte)this.getThumbleft_x();
                analog_msg[2] = (byte)this.getThumbleft_y();
                analog_msg[3] = (byte)this.getThumbright_x();
                analog_msg[4] = (byte)this.getThumbright_y();
                analog_msg[5] = (byte)this.getDpad();
                break;
            case ControllerData.XBOX_TYPE:
                analog_msg = new byte[13];
                //first byte type
                analog_msg[0] = SeaPIMainFrame.SEAPI_MSGTYPE_ANALOG_CTL;
                analog_msg[1] = (byte)this.controller_type;
                //2/3 is left stick x
                //4/5 is left stick y
                //6/7 is right stick x
                //8/9 is right stick y
                //10 is dpad
                //11 is left trigger
                //12 is right trigger
                //System.out.println("Thumb Left: "+String.valueOf(this.getThumbleft_x())+" "+String.valueOf(this.getThumbleft_y()));
                
                
                analog_msg[2] = (byte)(this.getThumbleft_x() & 0xff);
                analog_msg[3] = (byte)((this.getThumbleft_x() >> 8) & 0xff);
                
                analog_msg[4] = (byte)(this.getThumbleft_y() & 0xff);
                analog_msg[5] = (byte)((this.getThumbleft_y() >> 8) & 0xff);
                
                analog_msg[6] = (byte)(this.getThumbright_x() & 0xff);
                analog_msg[7] = (byte)((this.getThumbright_x() >> 8) & 0xff);
                
                analog_msg[8] = (byte)(this.getThumbright_y() & 0xff);
                analog_msg[9] = (byte)((this.getThumbright_y() >> 8) & 0xff);
                
                analog_msg[10] = (byte)this.getDpad();
                
                analog_msg[11] = (byte)this.getLeftTrigger();
                analog_msg[12] = (byte)this.getRightTrigger();
                
                
                break;
            default:
                analog_msg = null;
        }
        
        
        
        return analog_msg;
    }
}
