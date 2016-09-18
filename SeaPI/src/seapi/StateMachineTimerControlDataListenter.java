/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seapi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author jorge
 */
public class StateMachineTimerControlDataListenter implements ActionListener{
    StateMachineController my_sm;
    StateMachineTimerControlDataListenter(StateMachineController sm)
    {
        my_sm = sm;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        my_sm.processEvent(StateMachineController.SEAPI_EVENT_TIMER_CTL);
        
    }
    
}
