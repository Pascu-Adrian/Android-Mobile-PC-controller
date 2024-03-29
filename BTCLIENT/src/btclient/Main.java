/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package btclient;

/**
 *
 * @author MindSlave
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

/**

21.
 * A simple SPP client that connects with an SPP server

22.
 */

public class Main implements DiscoveryListener{

    //object used for waiting

    private static Object lock=new Object();

    //vector containing the devices discovered

    private static Vector vecDevices=new Vector();

    private static String connectionURL=null;

    public static void main(String[] args) throws IOException {

        Main client=new Main();

        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Address: "+localDevice.getBluetoothAddress());

        System.out.println("Name: "+localDevice.getFriendlyName());
        //find devices
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        System.out.println("Starting device inquiry...");
        agent.startInquiry(DiscoveryAgent.GIAC, client);
        try {
            synchronized(lock){
                lock.wait();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Device Inquiry Completed. ");
        //print all devices in vecDevices
        int deviceCount=vecDevices.size();
        if(deviceCount <= 0){
            System.out.println("No Devices Found .");
            System.exit(0);
        }

        else{
            //print bluetooth device addresses and names in the format [ No. address (name) ]
            System.out.println("Bluetooth Devices: ");

            for (int i = 0; i <deviceCount; i++) {

                RemoteDevice remoteDevice=(RemoteDevice)vecDevices.elementAt(i);

                System.out.println((i+1)+". "+remoteDevice.getBluetoothAddress()+" ("+remoteDevice.getFriendlyName(true)+")");

            }
        }

        System.out.print("Choose Device index: ");
        BufferedReader bReader=new BufferedReader(new InputStreamReader(System.in));

        String chosenIndex=bReader.readLine();

        int index=Integer.parseInt(chosenIndex.trim());

        //check for spp service

        RemoteDevice remoteDevice=(RemoteDevice)vecDevices.elementAt(index-1);

        UUID[] uuidSet = new UUID[1];

        uuidSet[0]=new UUID("1101",true);

        System.out.println("\nSearching for service...");

        agent.searchServices(null,uuidSet,remoteDevice,client);

        try {

            synchronized(lock){

                lock.wait();
System.out.println("\nSincronizat");
            }

        }

        catch (InterruptedException e) {

            e.printStackTrace();

        }
System.out.println("\nCaut conexiune url");
        if(connectionURL==null){


            System.out.println("Device does not support Simple SPP Service.");


            System.exit(0);


        }


System.out.println("\nConecting to server");

        //connect to the server and send a line of text


        StreamConnection streamConnection=(StreamConnection)Connector.open(connectionURL);



        //send string
System.out.println("\nOpen stream");
        OutputStream outStream=streamConnection.openOutputStream();
System.out.println("\nOpen writer");
        PrintWriter pWriter=new PrintWriter(new OutputStreamWriter(outStream));


boolean gigi=true;
System.out.println("\nOpen inputstream from sistem");
InputStreamReader inputtast=new InputStreamReader(System.in);
BufferedReader bReader1=new BufferedReader(inputtast);
System.out.println("\nGoing in while loop");
String msg;
while(gigi){
    System.out.println("\nWaiting for text");
    msg=bReader1.readLine()+"\n";
    pWriter.write("");
    pWriter.flush();
        pWriter.write(msg);
        System.out.println("INPUT: "+msg);
        pWriter.flush();
        System.out.println("\nFlushed");
        if(msg.equals("close\n")){
            System.out.println("\nCloseing");
            gigi=false;
        }
        }
bReader1.close();
System.out.println("\nClosed");
        //read response


       // InputStream inStream=streamConnection.openInputStream();


       // BufferedReader bReader2=new BufferedReader(new InputStreamReader(inStream));


       // String lineRead=bReader2.readLine();

       // System.out.println(lineRead);

    }//main
    //methods of DiscoveryListener

    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {

        //add the device to the vector

        if(!vecDevices.contains(btDevice)){


            vecDevices.addElement(btDevice);

        }

    }

    //implement this method since services are not being discovered


    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {


        if(servRecord!=null && servRecord.length>0){

            connectionURL=servRecord[0].getConnectionURL(0,false);

        }


        synchronized(lock){


            lock.notify();


        }

    }

    //implement this method since services are not being discovered


    public void serviceSearchCompleted(int transID, int respCode) {


        synchronized(lock){


            lock.notify();

        }


    }


    public void inquiryCompleted(int discType) {


        synchronized(lock){

            lock.notify();


        }

    }//end method

}

