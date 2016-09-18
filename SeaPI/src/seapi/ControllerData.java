/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seapi;

import java.nio.ByteBuffer;

/**
 *
 * @author jorge
 */
public class ControllerData {
    public int     button_x;
    public int     button_x_previous;
    public int     button_y;
    public int     button_y_previous;
    public int     button_a;
    public int     button_a_previous;
    public int     button_b;
    public int     button_b_previous;
    public int     button_start;
    public int     button_start_previous;
    public int     button_select;
    public int     button_select_previous;
    public int     button_left1;
    public int     button_left1_previous;
    public int     button_right1;
    public int     button_right1_previous;
    public int     button_leftthumb;
    public int     button_leftthumb_previous;
    public int     button_rightthumb;
    public int     button_rightthumb_previous;
    public int     thumbleft_x;
    public int     thumbleft_y;
    public int     thumbright_x;
    public int     thumbright_y;
    public int     dpad;
    
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
    private int     MASK_X_BUTTON    = 0x08;
    private int     MASK_Y_BUTTON    = 0x10;
    private int     MASK_A_BUTTON    = 0x01;
    private int     MASK_B_BUTTON    = 0x02;
    private int     MASK_START_BUTTON    = 0x08;
    private int     MASK_SELECT_BUTTON    = 0x04;
    private int     MASK_L1_BUTTON    = 0x40;
    private int     MASK_R1_BUTTON    = 0x80;
    private int     MASK_LB_BUTTON    = 0x20;
    private int     MASK_RB_BUTTON    = 0x40;
    
    ControllerData()
    {
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
    
    public void updateData(ByteBuffer in_data)
    {
        if(in_data==null)
            return;
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
        
        thumbleft_x         =   in_data.get(3)& 0xff;
        thumbleft_y         =   in_data.get(4)& 0xff;
        thumbright_x        =   in_data.get(5)& 0xff;
        thumbright_y        =   in_data.get(6)& 0xff;
        
        button_x_previous = button_x;
        button_x            =   in_data.get(0)&MASK_X_BUTTON&0xff;
        if(button_x_previous>0 && button_x==0)button_x_pressed = true;
            
        
        button_y_previous = button_y;
        button_y            =   in_data.get(0)&MASK_Y_BUTTON&0xff;
        
        button_a_previous = button_a;
        button_a            =   in_data.get(0)&MASK_A_BUTTON&0xff;
        
        button_b_previous = button_b;
        button_b            =   in_data.get(0)&MASK_B_BUTTON&0xff;
        
        button_left1_previous = button_left1;
        button_left1        =   in_data.get(0)&MASK_L1_BUTTON&0xff;
        
        button_right1_previous = button_right1;
        button_right1       =   in_data.get(0)&MASK_R1_BUTTON&0xff;
        
        button_start_previous = button_start;
        button_start        =   in_data.get(1)&MASK_START_BUTTON&0xff;
        
        button_select_previous = button_select;
        button_select       =   in_data.get(1)&MASK_SELECT_BUTTON&0xff;
        
        button_leftthumb_previous = button_leftthumb;
        button_leftthumb    =   in_data.get(1)&MASK_LB_BUTTON&0xff;
        
        button_rightthumb_previous = button_rightthumb;
        button_rightthumb   =   in_data.get(1)&MASK_RB_BUTTON&0xff;
        
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
            button_msg[0] = (byte)((byte)button_msg[0]&(byte)0b00100000);
        
        if(this.isButton_b_pressed())
            button_msg[0] = (byte)((byte)button_msg[0]&(byte)0b00010000);
        
        if(this.isButton_x_pressed())
            button_msg[0] = (byte)((byte)button_msg[0]&(byte)0b10000000);
        
        if(this.isButton_y_pressed())
            button_msg[0] = (byte)((byte)button_msg[0]&(byte)0b01000000);
        
        if(this.isButton_left1_pressed())
            button_msg[0] = (byte)((byte)button_msg[0]&(byte)0b00001000);
        
        if(this.isButton_right1_pressed())
            button_msg[0] = (byte)((byte)button_msg[0]&(byte)0b00000100);
        
        if(this.isButton_select_pressed())
            button_msg[0] = (byte)((byte)button_msg[0]&(byte)0b00000010);
        
        if(this.isButton_start_pressed())
            button_msg[0] = (byte)((byte)button_msg[0]&(byte)0b00000001);
        
        
        if(this.isButton_leftthumb_pressed())
            button_msg[1] = (byte)((byte)button_msg[1]&(byte)0b10000000);
        
        if(this.isButton_rightthumb_pressed())
            button_msg[1] = (byte)((byte)button_msg[1]&(byte)0b01000000);
        
        return button_msg;
    }
    public byte[] getAnalogMessage()
    {
        byte[] analog_msg = new byte[5];
        
        //0 is left stick x
        //1 is left stick y
        //2 is right stick x
        //3 is right stick y
        //4 is dpad
        
        analog_msg[0] = (byte)this.getThumbleft_x();
        analog_msg[1] = (byte)this.getThumbleft_y();
        analog_msg[2] = (byte)this.getThumbright_x();
        analog_msg[3] = (byte)this.getThumbright_y();
        analog_msg[4] = (byte)this.getDpad();
        
        return analog_msg;
    }
}
