/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bluetooth;

/**
 *
 * @author PROGRAMARE
 */
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;


import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;




public class Server {
    Robot r = null;
    Grafica gu=null;
    int lastx;
    int lasty;
    int lastz;
    String state="";
    String leftmousestate="";
    String rightmousestate="";

    public void startServer() throws IOException{
        try {
            r = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

        UUID uuid = new UUID("1101", true);
        String connectionString = "btspp://localhost:" + uuid +";name=Server";

        StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier)Connector.open( connectionString );
        System.out.println("\nServer pornit! Astept conexiune...");
        StreamConnection connection=streamConnNotifier.acceptAndOpen();
        System.out.println("\nConexiune acceptata...");
        RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);
        System.out.println("Remote device address: "+dev.getBluetoothAddress());
        System.out.println("Remote device name: "+dev.getFriendlyName(true));
        InputStream inStream=connection.openInputStream();
        BufferedReader bReader=new BufferedReader(new InputStreamReader(inStream));
        System.out.println("\nDevice paired! Astept date...");
        boolean gigi=true;
        while(gigi){
            String mesaj=bReader.readLine();
            System.out.println("Primit de la client: "+mesaj);
            if(mesaj!=null){
                if(mesaj.contains("close")&&gigi){
                    gigi=false;
                    System.out.println("\nCLOSE");
                    System.out.println("\nAplicatie terminata");
                    System.exit(0);

                }
                if(mesaj.contains("clicks")&&gigi){
                    clicks();
                    System.out.println("CLICK S");
                }
                if(mesaj.contains("clickd")&&gigi){
                    clickd();
                    System.out.println("CLICK D");
                }
                if(mesaj.contains("volan")&&gigi){
                    volan();
                    System.out.println("VOLAN");
                }
                if(mesaj.contains("obj3d")&&gigi){
                    obj3d();
                    System.out.println("OBJ3D");
                }
                if(mesaj.contains("mouse")&&gigi){
                    mouse();
                    System.out.println("MOUSE");
                }
                
                 if(mesaj.contains(":")&&gigi){
            String axa[]=mesaj.split(":");
            if(state.equals("volan")){
                miscavolan(Double.parseDouble(axa[2]), Double.parseDouble(axa[1]));
            }
            if(state.equals("obj3d")){
                miscaobj3d(Double.parseDouble(axa[0]), Double.parseDouble(axa[1]));
            }
            if(state.equals("mouse")){
                miscamouse(Integer.parseInt(axa[0]), Integer.parseInt(axa[1]));
            }
                        }
            }
       // bReader.close();
        }
        System.out.println(gigi);
        bReader.close();
        inStream.close();
        streamConnNotifier.close();      

    }
public void clicks(){
    r.mousePress(InputEvent.BUTTON1_MASK);
    r.mouseRelease(InputEvent.BUTTON1_MASK);
    System.out.println("\nExecut click stanga...");
}
public void clickd(){
    r.mousePress(InputEvent.BUTTON3_MASK);
    r.mouseRelease(InputEvent.BUTTON3_MASK);
    System.out.println("\nExecut click dreapta...");
}
public void miscamouse(int x,int y){
    r.mouseMove((int)MouseInfo.getPointerInfo().getLocation().getX()+(x*-5), (int)MouseInfo.getPointerInfo().getLocation().getY()+(y*-5));
    System.out.println("\nExecut misca mouse... x: "+x+" y: "+y);
}
public void miscavolan(double z,double y){
    if(z>3){
        System.out.println("\nExecut accelereaza: "+z);
        r.keyPress(KeyEvent.VK_UP);
        try {
                Thread.sleep((long) (110-(z * 10)));
            } catch (InterruptedException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        r.keyRelease(KeyEvent.VK_UP);
    }
    if(z<-2){
        System.out.println("\nExecut franeaza: "+z);
        r.keyPress(KeyEvent.VK_DOWN);
        try {
                Thread.sleep((long) (110-(-z * 10)));
            } catch (InterruptedException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        r.keyRelease(KeyEvent.VK_DOWN);
        }

    if(z==0){

    }
    if(y>3){
        System.out.println("\nExecut volan dreapta: "+y);
        r.keyPress(KeyEvent.VK_RIGHT);
        try {
                Thread.sleep((long) (110-(y * 10)));
            } catch (InterruptedException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        r.keyRelease(KeyEvent.VK_RIGHT);

    }
    if(y<-3){
        System.out.println("\nExecut volan stanga: "+y);
        r.keyPress(KeyEvent.VK_LEFT);
            try {
                Thread.sleep((long) (110-(-y * 10)));
            } catch (InterruptedException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        r.keyRelease(KeyEvent.VK_LEFT);

    }
    if(y==0){

    }
}
public void miscaobj3d(double x,double y){
System.out.println("\nExecut roteste obiect 3d: x->"+x+" y->"+y);
    lastx=(int)x;
        lasty=(int)y;
        gu.rotate((double)lastx,(double)lasty);
}
public void volan(){
    System.out.println("\nSetez stare curenta: Volan");
    state="volan";
    if(gu!=null){
    gu.setVisible(false);
    }
    
}
public void obj3d(){
    System.out.println("\nSetez stare curenta: OBJ3D");
    state="obj3d";
    if(gu==null){
    gu=new Grafica();
    }
 else
     gu.setVisible(true);
}
public void mouse(){
    System.out.println("\nSetez stare curenta: Mouse");
    state="mouse";
    if(gu!=null){
    gu.setVisible(false);
    }
}

}

