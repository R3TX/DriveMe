package co.edu.udea.driveme;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.util.UUID;

import Bluethot.ConnectedThread;
import Joystick.JoystickMovedListener;
import Joystick.JoystickView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    JoystickView mJoystick;
    TextView mTxtDataL;
    private double mRadiusL = 0;
    private double mAngleL = 0;
    private boolean mCenterL = true;
    Handler bluetoothIn;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder redDataString = new StringBuilder();

    private ConnectedThread connectedThread;

    //SPP UUID service
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //mac addres
    private static String address=null;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mJoystick = (JoystickView)findViewById(R.id.joystickView);
        mTxtDataL = (TextView) findViewById(R.id.txt_dataL);
        mJoystick.setOnJostickMovedListener(_listenerLeft);
        mJoystick.setAutoReturnToCenter(true);
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //con esto buscamos los cambios que hayan en los dispositivos conectados o no con el cel
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        //this.registerReceiver(BTReceiver, filter1);

        checkBTState();

    }

    private JoystickMovedListener _listenerLeft = new JoystickMovedListener() {

        public void OnMoved(int pan, int tilt) {
            mRadiusL = Math.sqrt((pan*pan) + (tilt*tilt));
            // mAngleL = Math.atan2(pan, tilt);
            mAngleL = Math.atan2(-pan, -tilt);
            float grados = (float) (mAngleL * 180 / Math.PI);
           mTxtDataL.setText(String.format("( r%.0f, %.0f\u00B0 )", Math.min(mRadiusL, 10), grados));
            //mTxtDataL.setText((int) (mAngleL * 180 / Math.PI));
            if(grados<22.5&&grados>-22.5) {
                connectedThread.write(1);//adelante ---antes f
            }
            if(grados<-22.5&&grados>-67.5) {
                connectedThread.write(2);//diagonal derecha superior
            }
            if(grados<-67.5&&grados>-112.5){
                connectedThread.write(3);//derecha --antes d

            }
            if(grados<-112.5 && grados> -157.5){
                connectedThread.write(4);//diagonal derecha inferior

            }
            System.out.println("grados "+grados);
            if(grados<-157.5&&grados>=-180){
                System.out.println("reverso");
                connectedThread.write(5);//atras--antes r
            }
            if(grados<=180&&grados>157.5){
                System.out.println("reverso2");
                connectedThread.write(5);//atras--antes r
            }
            if(grados>112.5 && grados< 157.5){
                connectedThread.write(6);//diagonal izquierda inferior

            }
            if(grados>67.5 && grados<112.5){
                connectedThread.write(7);//dizquierda --antes i

            }
            if(grados<67.5 && grados> 22.5){
                connectedThread.write(8);//diagonal izquierda superior

            }
            if(mRadiusL==0){

                connectedThread.write(0); //parar antes s
            }
            mCenterL = false;
        }

        public void OnReleased() {

            //
        }

        public void OnReturnedToCenter() {
            mRadiusL = mAngleL = 0;
            //UpdateMethod();
            mCenterL = true;
        }
    };

    @Override
    public void onClick(View v) {

    }

    public void checkBTState(){
        if (btAdapter==null){
            Toast.makeText(this, "El disositivo no soporta bluetooth", Toast.LENGTH_SHORT).show();
        }else{
            if (btAdapter.isEnabled()){

            }else{
                Intent enaIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enaIntent,1);
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
    /*
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();


        // Add previosuly paired devices to the array
        if (pairedDevices.size() > 0) {
           for (BluetoothDevice device :  pairedDevices) {
               //device.
                if(device.getBondState()== BluetoothDevice.BOND_BONDED){
                    address=device.getAddress();
                }
            }
        } else {
            String noDevices = "Ningun dispositivo pudo ser emparejado";
            //mPairedDevicesArrayAdapter.add(noDevices);
        }

*/




        //Get MAC address from Bluethot.DeviceListActivity via intent
       Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceList.EXTRA_DEVICE_ADDRESS);


        TextView t = (TextView) findViewById(R.id.mac);
        TextView y = (TextView) findViewById(R.id.name);

        t.setText(address);



        //create device and set the MAC address
        //Log.i("ramiro", "adress : " + address);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
            y.setText(btSocket.getRemoteDevice().getName());
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
            System.out.println(btSocket.isConnected());
        } catch (IOException e) {
            try
            {
                btSocket.close();
                System.out.println("error");
            } catch (IOException e2)
            {System.out.println("error del error");
                //insert code to deal with this
            }
        }
        connectedThread = new ConnectedThread(btSocket, bluetoothIn);
        connectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        connectedThread.write(9);
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }


    public void open(View view){
       /* Intent intentOpenBluetoothSettings = new Intent();
        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intentOpenBluetoothSettings);
*/
        Intent i = new Intent(this, DeviceList.class);
        startActivity(i);

    }

//esto es para ejecutar la accion de los cuando el estado del dispositivos conectados cambia
    private final BroadcastReceiver BTReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            //Do something if connected
           // address=BluetoothDevice.
            address=device.getAddress();
            Toast.makeText(getApplicationContext(), "BT Connected", Toast.LENGTH_SHORT).show();
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            //Do something if disconnected
            Toast.makeText(getApplicationContext(), "BT Disconnected", Toast.LENGTH_SHORT).show();
        }
        //else if...
    }
};





}