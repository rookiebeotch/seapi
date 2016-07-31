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
public class StateTx extends State {
    // 8. The State derived classes only override the messages they need to
   public void on()  { System.out.println( "C + on  = B" ); }

}
