/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seapi;

/**
 *
 * @author jorge
 */
abstract class State {
    public void on()  { 
        System.out.println( "error" );
    }  // 7. The State base 
    public void off() { 
        System.out.println( "error" );
    }  //    class specifies 
    public void ack() { 
        System.out.println( "error" );
    }  //    default behavior
}
