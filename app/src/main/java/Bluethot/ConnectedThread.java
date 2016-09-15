package Bluethot;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by r3tx on 11/09/16.
 */
public class ConnectedThread extends Thread  {
    private final InputStream mis;
    private final OutputStream mos;
    private final Handler handler;

    public ConnectedThread(BluetoothSocket bs, Handler handler){
        InputStream is = null;
        OutputStream outputStream = null;
        this.handler= handler;
        try{
            //create I/O streams for conection
            is =bs.getInputStream();
            outputStream=bs.getOutputStream();
        }catch (IOException e){

        }
        mis=is;
        mos=outputStream;
    }
    public void write(int input){
        //byte[] msgBuffer = input.getBytes();
        try {
            mos.write(input);
           // System.out.println("la concha "+ input+"    bytes  "+msgBuffer);
        }catch (IOException e){
            //if you cant write close

        }
    }

    public void run(){
        byte[] buffer = new byte[256];
        int bytes;
        //keep looping to liten for received messages
        while(true){
            try{
                bytes= mis.read(buffer);
                String readMessage = new String(buffer, 0, bytes);
                //send the obtained bytes via handler
                //handler.obtainMessage(0,bytes,-1,readMessage).sendToTarget();
            }catch (IOException e){
                break;
            }
        }
    }

}
