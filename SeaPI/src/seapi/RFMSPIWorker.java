/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seapi;

import java.util.List;
import javax.swing.SwingWorker;

/**
 *
 * @author jorge
 */
public class RFMSPIWorker extends SwingWorker<Integer, String> {

    @Override
    protected Integer doInBackground() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    protected void process(List<String> chunks) {
    // Messages received from the doInBackground() (when invoking the publish() method)
    }
}
