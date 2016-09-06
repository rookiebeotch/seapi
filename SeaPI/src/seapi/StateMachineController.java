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
public class StateMachineController {
    //states
    final static int        SEAPI_STATE_INIT    =   100;
    final static int        SEAPI_STATE_TX      =   101;
    final static int        SEAPI_STATE_RX      =   102;
    final static int        SEAPI_STATE_IDLE    =   103;
    //events
    final static int        SEAPI_EVENT_PKT_RCV     =   1000;
    final static int        SEAPI_EVENT_TIMER_CTL   =   1001;
    final static int        SEAPI_EVENT_INIT_DONE   =   1002;
    
    //vars
    private int current_state;
    
    
    StateMachineController(){
        current_state   =   SEAPI_STATE_INIT;
        //make sure timers are all off at start
        //clear any counters
    }
    
    public void processEvent(int event)
    {
        //call the current state process event
        switch(current_state)
        {
            case SEAPI_STATE_INIT:
                current_state   =   state_init(event,current_state);
                break;
            case SEAPI_STATE_TX:
                current_state   =   state_tx(event,current_state);
                break;    
            case SEAPI_STATE_RX:
                current_state   =   state_rx(event,current_state);
                break;        
            case SEAPI_STATE_IDLE:
                current_state   =   state_idle(event,current_state);
                break;        
            default:
                   //do nothing
                
                
        }
    }
    
    public  int state_init(int event,int current_state)
    {
        int next_state = current_state;
        
        switch(event)
        {
            
            default:
                //do nada!!!
        }
        
        return next_state;
    }
    public  int state_tx(int event,int current_state)
    {
        int next_state = current_state;
        
        switch(event)
        {
            
            default:
                //do nada!!!
        }
        
        return next_state;
    }
    public  int state_rx(int event,int current_state)
    {
        int next_state = current_state;
        
        switch(event)
        {
            
            default:
                //do nada!!!
        }
        
        return next_state;
    }
    public  int state_idle(int event,int current_state)
    {
        int next_state = current_state;
        
        switch(event)
        {
            
            default:
                //do nada!!!
        }
        
        return next_state;
    }
}
