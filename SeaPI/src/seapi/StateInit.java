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
public class StateInit extends State {
    public void on()  { System.out.println( "A + on  = C" ); }
   public void off() { System.out.println( "A + off = B" ); }
   public void ack() { System.out.println( "A + ack = A" ); }
}
