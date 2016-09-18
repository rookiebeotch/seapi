/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seapi;

import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

/**
 *
 * @author jorge
 */
public class BluetoothDiscoverListener implements DiscoveryListener{
    private static Object lock=new Object();

    @Override
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass arg1) {
        String name;
        try {
            name = btDevice.getFriendlyName(false);
        } catch (Exception e) {
            name = btDevice.getBluetoothAddress();
        }
        
        System.out.println("device found: " + name);
        
    }

    @Override
    public void inquiryCompleted(int arg0) {
        synchronized(lock){
            lock.notify();
        }
    }

    @Override
    public void serviceSearchCompleted(int arg0, int arg1) {
        synchronized (lock) {
           lock.notify();
       }
    }

    @Override
    public void servicesDiscovered(int arg0, ServiceRecord[] services) {
        for (int i = 0; i < services.length; i++) {
           String url = services[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
           if (url == null) {
               continue;
           }
           
           DataElement serviceName = services[i].getAttributeValue(0x0100);
           if (serviceName != null) {
               System.out.println("service " + serviceName.getValue() + " found " + url);
           } else {
               System.out.println("service found " + url);
           }

              
       }
       

    }

}