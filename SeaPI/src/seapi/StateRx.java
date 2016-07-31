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
public class StateRx extends State {
    public void on()  { System.out.println( "B + on  = A" ); }
   public void off() { System.out.println( "B + off = C" ); }
}
