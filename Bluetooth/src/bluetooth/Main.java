/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bluetooth;

import java.io.IOException;
import javax.bluetooth.LocalDevice;

/**
 *
 * @author PROGRAMARE
 */
public class Main {
    public static void main(String[] args) throws IOException {
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Address SERVER: "+localDevice.getBluetoothAddress());
        System.out.println("Name SERVER: "+localDevice.getFriendlyName());
        Server sampleSPPServer=new Server();
        sampleSPPServer.startServer();
    }

}
