package org.simplebluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;

public class Bluetooth extends Activity {
	
	
	//------------------GIRATOR
	private SensorManager myManager;
	private List<Sensor> sensors;
	private Sensor accSensor;
	//------------------GIRATOR
	
	private Vibrator v;
	

    private static final int BT_DISCOVERABLE_DURATION = 300;
    private static final String DEBUG_TAG = "Bluetooth";
    private String controlSTATE="";
    private static final UUID SIMPLE_BT_APP_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final String SIMPLE_BT_NAME = "1808130054";
    
    private static final int DEVICE_PICKER_DIALOG = 1001;
    
    private final Handler handler = new Handler();
    private BluetoothAdapter btAdapter;
    private BtReceiver btReceiver;
    private ServerListenThread serverListenThread;
    private ClientConnectThread clientConnectThread;
    private BluetoothDataCommThread bluetoothDataCommThread;
    private BluetoothDevice remoteDevice;
    private BluetoothSocket activeBluetoothSocket;
    private Button connect;
    private Button clicks;
    private Button clickd;
    private Button volan;
    private Button mouse;
    private Button obj3d;

    
    // for sound

    private HashMap<String, BluetoothDevice> discoveredDevices = new HashMap<String, BluetoothDevice>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        clicks=(Button)findViewById(R.id.clicks);
        volan=(Button)findViewById(R.id.volan);
        clickd=(Button)findViewById(R.id.clickd);
        connect=(Button)findViewById(R.id.connect);
        mouse=(Button)findViewById(R.id.mouse);
        obj3d=(Button)findViewById(R.id.obj3d);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
      //----------------GIRATOR
        myManager=(SensorManager)getSystemService(getApplicationContext().SENSOR_SERVICE);
        sensors=myManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if(sensors.size()>0){
        	accSensor=sensors.get(0);
        }
        //----------------GIRATOR
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            // no bluetooth available on device
            setStatus("No bluetooth available. :(");
            disableAllButtons();
        } else {
            setStatus("Bluetooth available! :)");
            // we need a broadcast receiver now
            btReceiver = new BtReceiver();
            // register for state change broadcast events
            IntentFilter stateChangedFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            this.registerReceiver(btReceiver, stateChangedFilter);
            // register for discovery events
            IntentFilter actionFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(btReceiver, actionFoundFilter);
            // check current state
            int currentState = btAdapter.getState();
            setUIForBTState(currentState);
            if (currentState == BluetoothAdapter.STATE_ON) {
                findDevices();
            }
        }
    }

    
    private void findDevices() {
        String lastUsedRemoteDevice = getLastUsedRemoteBTDevice();
        if (lastUsedRemoteDevice != null) {
            setStatus("Checking for known paired devices, namely: "+lastUsedRemoteDevice);
            // see if this device is in a list of currently visible (?), paired devices
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            for (BluetoothDevice pairedDevice : pairedDevices) {
                if (pairedDevice.getAddress().equals(lastUsedRemoteDevice)) {
                    setStatus("Found device: " + pairedDevice.getName() + "@" + lastUsedRemoteDevice);
                    remoteDevice = pairedDevice;
                }
            }
        } 
       
        if (remoteDevice == null) {
            setStatus("Starting discovery...");
            // start discovery
            if (btAdapter.startDiscovery()) {
                setStatus("Discovery started...");
            }


        }
        
        // also set discoverable
        setStatus("Enabling discoverable, user will see dialog...");
        Intent discoverMe = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverMe.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BT_DISCOVERABLE_DURATION);
        startActivity(discoverMe);
        
        // also start listening for connections
        setStatus("Enabling listening socket thread");
        serverListenThread = new ServerListenThread();
        serverListenThread.start();
    }

    private String getLastUsedRemoteBTDevice() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        String result = prefs.getString("LAST_REMOTE_DEVICE_ADDRESS", null);
        return result;
    }
    
    private void setLastUsedRemoteBTDevice(String name) {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        Editor edit = prefs.edit();
        edit.putString("LAST_REMOTE_DEVICE_ADDRESS", name);
        edit.commit();
    }

    private void disableAllButtons() {
        Button button;
        int[] buttonIds = { R.id.bt_toggle, R.id.connect, R.id.clicks, R.id.close };
        for (int buttonId : buttonIds) {
            button = (Button) findViewById(buttonId);
            button.setEnabled(false);
        }
    }

    private ToggleButton btToggle;

    private void setUIForBTState(int state) {
        if (btToggle == null) {
            btToggle = (ToggleButton) findViewById(R.id.bt_toggle);
        }
        switch (state) {
        case BluetoothAdapter.STATE_ON:
            btToggle.setChecked(true);
            btToggle.setEnabled(true);
            setStatus("BT state now on");
            connect.setEnabled(true);
            break;
        case BluetoothAdapter.STATE_OFF:
            btToggle.setChecked(false);
            btToggle.setEnabled(true);
            setStatus("BT state now off");
            clicks.setEnabled(false);
            clickd.setEnabled(false);
            volan.setEnabled(false);
            mouse.setEnabled(false);
            obj3d.setEnabled(false);
            connect.setEnabled(false);
            break;
        case BluetoothAdapter.STATE_TURNING_OFF:
            btToggle.setChecked(true);
            btToggle.setEnabled(false);
            setStatus("BT state turning off");
            break;
        case BluetoothAdapter.STATE_TURNING_ON:
            btToggle.setChecked(false);
            btToggle.setEnabled(false);
            setStatus("BT state turning on");
            break;
        }
    }

    private TextView statusField;

    private void setStatus(String string) {
        if (statusField == null) {
            statusField = (TextView) findViewById(R.id.output_display);
        }
        String current = (String) statusField.getText();
        current = string + "\n" + current;
        if (current.length() > 1500) {
            int truncPoint = current.lastIndexOf("\n");
            current = (String) current.subSequence(0, truncPoint);
        }
        statusField.setText(current);
    }

    public void doToggleBT(View view) {
        Log.v(DEBUG_TAG, "doToggleBT() called");
        if (btToggle == null) {
            btToggle = (ToggleButton) findViewById(R.id.bt_toggle);
        }
        if (btToggle.isChecked() == false) {
            if (serverListenThread != null) {
                serverListenThread.stopListening();
            }
            if (clientConnectThread != null) {
                clientConnectThread.stopConnecting();
            }
            if (bluetoothDataCommThread != null) {
                bluetoothDataCommThread.disconnect();
            }
            btAdapter.cancelDiscovery();
            if (!btAdapter.disable()) {
                setStatus("Disable adapter failed");
            }
            
            remoteDevice = null;
            activeBluetoothSocket = null;
            serverListenThread = null;
            clientConnectThread = null;
            bluetoothDataCommThread = null;
            discoveredDevices.clear();
        } else {
            if (!btAdapter.enable()) {
                setStatus("Enable adapter failed");
            }
        }
    }

    public void doConnectBT(View view) {
        Log.v(DEBUG_TAG, "doConnectBT() called");
        if (remoteDevice != null) {
            // connect to remoteDevice
        	volan.setEnabled(true);
            mouse.setEnabled(true);
            obj3d.setEnabled(true);
            doConnectToDevice(remoteDevice);
        } else {
            // get the device the user wants to connect to
            showDialog(DEVICE_PICKER_DIALOG);
        }
    }

    public void doConnectToDevice(BluetoothDevice device) {
        // halt discovery
        btAdapter.cancelDiscovery();
        setStatus("Starting connect thread");
        clientConnectThread = new ClientConnectThread(device);
        clientConnectThread.start();
    }

    public void doStartDataCommThread() {
        if (activeBluetoothSocket == null) {
            setStatus("Can't start datacomm");
            Log.w(DEBUG_TAG, "Something is wrong, shouldn't be trying to use datacomm when no socket");
        } else {
            setStatus("Data comm thread starting");
            bluetoothDataCommThread = new BluetoothDataCommThread(activeBluetoothSocket);
            bluetoothDataCommThread.start();
        }
    }

    public void doSendclose(View view) {
        Log.v(DEBUG_TAG, "doSendclose() called");
        
        if (bluetoothDataCommThread != null) {
        	bluetoothDataCommThread.send("\nclose\n");
        }
        onDestroy();
    }

    public void doSendclicks(View view) {
        Log.v(DEBUG_TAG, "doSendclicks() called");
        if (bluetoothDataCommThread != null) {
        	try {
				bluetoothDataCommThread.outData.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
        	bluetoothDataCommThread.send("clicks\n");
        }
    }
    public void doSendclickd(View view) {
    	if (bluetoothDataCommThread != null) {
        	bluetoothDataCommThread.send("clickd\n");
        }
    }
    public void doSetMOUSE(View view) {
    	if (bluetoothDataCommThread != null) {
        	bluetoothDataCommThread.send("mouse\n");
        	mouse.setEnabled(false);
        	clickd.setEnabled(true);
        	clicks.setEnabled(true);
        	volan.setEnabled(true);
            obj3d.setEnabled(true);
        }
    }
    public void doSetVOLAN(View view) {
 
    	if (bluetoothDataCommThread != null) {
        	bluetoothDataCommThread.send("volan\n");
        	volan.setEnabled(false);
            mouse.setEnabled(true);
            obj3d.setEnabled(true);
            clickd.setEnabled(false);
        	clicks.setEnabled(false);
        }
    }
    public void doSetOBJ3D(View view) {

    	if (bluetoothDataCommThread != null) {
        	bluetoothDataCommThread.send("obj3d\n");
        	obj3d.setEnabled(false);
        	volan.setEnabled(true);
            mouse.setEnabled(true);
            clickd.setEnabled(false);
        	clicks.setEnabled(false);
        }
    }
    
    public void doHandleReceivedCommand(String rawCommand) {
        //String command = rawCommand.trim();
    }

    private class BtReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                setStatus("Broadcast: Got ACTION_STATE_CHANGED");
                int currentState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                setUIForBTState(currentState);
                if (currentState == BluetoothAdapter.STATE_ON) {
                    findDevices();
                }
            } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                setStatus("Broadcast: Got ACTION_FOUND");
                BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                setStatus("Device: " + foundDevice.getName() + "@" + foundDevice.getAddress());
                discoveredDevices.put(foundDevice.getName(), foundDevice);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (serverListenThread != null) {
            serverListenThread.stopListening();
        }
        if (clientConnectThread != null) {
            clientConnectThread.stopConnecting();
        }
        if (bluetoothDataCommThread != null) {
            bluetoothDataCommThread.disconnect();
        }
        if (activeBluetoothSocket != null) {
            try {
                activeBluetoothSocket.close();
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Failed to close socket", e);
            }
        }
        btAdapter.cancelDiscovery();
        this.unregisterReceiver(btReceiver);
        btAdapter.disable();
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case DEVICE_PICKER_DIALOG:
            if (discoveredDevices.size() > 0) {
                ListView list = new ListView(this);
                String[] deviceNames = discoveredDevices.keySet()
                        .toArray(new String[discoveredDevices.keySet().size()]);
                ArrayAdapter<String> deviceAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, deviceNames);
                list.setAdapter(deviceAdapter);
                list.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                        String name = (String) ((TextView) view).getText();
                        removeDialog(DEVICE_PICKER_DIALOG);
                        setStatus("Remote device chosen: " + name);
                        doConnectToDevice(discoveredDevices.get(name));
                    }
                });
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(list);
                builder.setTitle(R.string.pick_device);
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeDialog(DEVICE_PICKER_DIALOG);
                        setStatus("No remote BT picked.");
                    }
                });
                dialog = builder.create();
            } else {
                setStatus("No devices found to pick from");
            }
            break;
        }
        return dialog;
    }

   private class ServerListenThread extends Thread {
        private final BluetoothServerSocket btServerSocket;

        public ServerListenThread() {
            BluetoothServerSocket btServerSocket = null;
            try {
                btServerSocket = btAdapter.listenUsingRfcommWithServiceRecord(SIMPLE_BT_NAME, SIMPLE_BT_APP_UUID);
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Failed to start listening", e);
            }
            // finalize
            this.btServerSocket = btServerSocket;
        }

        public void run() {
            BluetoothSocket socket = null;
            try {
                while (true) {
                    handler.post(new Runnable() {
                        public void run() {
                            setStatus("ServerThread: calling accept");
                        }
                    });
                    socket = btServerSocket.accept();
                    if (socket != null) {
                        activeBluetoothSocket = socket;
                        // Do work to manage the connection (in a separate thread)
                        handler.post(new Runnable() {
                            public void run() {
                                setStatus("Got a device socket");
                                doStartDataCommThread();
                            }
                        });
                        btServerSocket.close();
                        break;
                    }
                }
            } catch (Exception e) {
                handler.post(new Runnable() {
                    public void run() {
                        setStatus("Listening socket done - failed or cancelled");
                    }
                });
            }
        }

        public void stopListening() {
            try {
                btServerSocket.close();
            } catch (Exception e) {
                Log.e(DEBUG_TAG, "Failed to close listening socket", e);
            }
        }
    }

    private class ClientConnectThread extends Thread {
        private final BluetoothDevice remoteDevice;
        private final BluetoothSocket clientSocket;

        public ClientConnectThread(BluetoothDevice remoteDevice) {
            this.remoteDevice = remoteDevice;
            BluetoothSocket clientSocket = null;
            try {
                clientSocket = remoteDevice.createRfcommSocketToServiceRecord(SIMPLE_BT_APP_UUID);
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Failed to open local client socket");
            }
            // finalize
            this.clientSocket = clientSocket;
        }

        public void run() {
            boolean success = false;
            try {
                clientSocket.connect();
                success = true;
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Client connect failed or cancelled");
                try {
                    clientSocket.close();
                } catch (IOException e1) {
                    Log.e(DEBUG_TAG, "Failed to close socket on error", e);
                }
            }
            final String status;
            if (success) {
                status = "Connected to remote device";
                activeBluetoothSocket = clientSocket;
                // we don't need to keep listening
                serverListenThread.stopListening();
            } else {
                status = "Failed to connect to remote device";
                activeBluetoothSocket = null;
            }
            handler.post(new Runnable() {
                public void run() {
                    setStatus(status);
                    setLastUsedRemoteBTDevice(remoteDevice.getAddress());
                    doStartDataCommThread();
                }
            });
        }

        public void stopConnecting() {
            try {
                clientSocket.close();
            } catch (Exception e) {
                Log.e(DEBUG_TAG, "Failed to stop connecting", e);
            }
        }
    }

    private class BluetoothDataCommThread extends Thread {
        private final BluetoothSocket dataSocket;
        private final OutputStream outData;
        private final InputStream inData;

        public BluetoothDataCommThread(BluetoothSocket dataSocket) {
            this.dataSocket = dataSocket;
            OutputStream outData = null;
            InputStream inData = null;
            try {
                outData = dataSocket.getOutputStream();
                inData = dataSocket.getInputStream();
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Failed to get iostream", e);
            }
            this.inData = inData;
            this.outData = outData;
        }

        public void run() {
            byte[] readBuffer = new byte[64];
            int readSize = 0;
            try {
                while (true) {
                    readSize = inData.read(readBuffer);

                    final String inStr = new String(readBuffer, 0, readSize);
                    handler.post(new Runnable() {
                       public void run() {
                           doHandleReceivedCommand(inStr);
                       }
                    });
                }
            } catch (Exception e) {
            }
        }

        public boolean send(String out) {
            boolean success = false;
            try {
                outData.write(out.getBytes(), 0, out.length());
                success = true;
            } catch (IOException e) {
                setStatus("Send failed");
            }
            return success;
        }
        
        public void disconnect() {
            try {
                dataSocket.close();
            } catch (Exception e) {
            	
            }
        }
    }
    
    private void updateaxe(float x,float y,float z){
    	if(x>9||x<-9){
    		//capetele lu z
    	}
    	try {
            if(y>9||y<-9){
			long milliseconds = 100;
			v.vibrate(milliseconds);
		}
        } catch (Exception e) {
           setStatus("Eroare vibrator:"+ e);
        }
        if(z>9||z<-9){
    		//capetele lu z
    	}
    	
    	if (bluetoothDataCommThread != null) {
    		bluetoothDataCommThread = new BluetoothDataCommThread(activeBluetoothSocket);
    		int xint=(int)x;
    		int yint=(int)y;
    		int zint=(int)z;
            bluetoothDataCommThread.send(xint+":"+yint+":"+zint+"\n");
        }
    }
    private final SensorEventListener mySensorListener = new SensorEventListener() {
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			updateaxe(event.values[0], event.values[1], event.values[2]);
			
			
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	};
	@Override
    protected void onResume()
    {
     super.onResume();
     myManager.registerListener(mySensorListener, accSensor, SensorManager.SENSOR_DELAY_GAME);  
    }
	@Override
    public void onPause() {
            super.onPause();
    }
   
    @Override
    protected void onStop()
    {     
     myManager.unregisterListener(mySensorListener);
     super.onStop();
    }
}