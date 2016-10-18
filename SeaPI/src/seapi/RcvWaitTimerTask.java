/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seapi;

import java.util.TimerTask;

/**
 *
 * @author jorge
 */
public class RcvWaitTimerTask extends TimerTask{
    StateMachineController my_sm;
    RcvWaitTimerTask(StateMachineController sm)
    {
        my_sm = sm;
    }
    @Override
    public void run() {
         my_sm.processEvent(StateMachineController.SEAPI_EVENT_RCV_EXPIRED);
    }
    
}
