package com.example.socketserver;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class MainActivity extends Activity implements Runnable{

    private ServerSocket mServer;
    private Socket mSocket;
    int port = 12345;
    volatile Thread runner = null;
    Handler mHandler = new Handler();

    private SensorManager sensorManager = null;
    private SensorEventListener sensorEventListener = null;
    private float[] fAccell = null;
    private float[] fMagnetic = null;
    float[] saveAcceleVal  = new float[3];
    float[] saveMagneticVal = new float[3];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 方位角取得
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                switch (event.sensor.getType()){
                    case Sensor.TYPE_ACCELEROMETER:
                        fAccell = event.values.clone();
                        LowPassFilter(fAccell);
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        fMagnetic = event.values.clone();
                        LowPassFilter2(fMagnetic);
                        break;
                }
                if (fAccell != null && fMagnetic != null){
                    float[] inR = new float[9];
                    SensorManager.getRotationMatrix(inR, null, fAccell, fMagnetic);
                    float[] outR = new float[9];
                    SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Y, outR);
                    float[] fAttitude = new float[3];
                    SensorManager.getOrientation(outR, fAttitude);

                    String buf = String.format("%.1f", rad2deg(fAttitude[0]));
                    TextView t = findViewById(R.id.textAzimuth);
                    t.setText(buf);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        // Socket通信
        WifiManager wifiManager =  (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifIinfo = wifiManager.getConnectionInfo();
        int address = wifIinfo.getIpAddress();
        String ipAddressStr = ((address >> 0) & 0xFF) + "."
                + ((address >> 8) & 0xFF) + "." + ((address >> 16) & 0xFF)
                + "." + ((address >> 24) & 0xFF);
        TextView tip = (TextView) findViewById(R.id.textIp);
        tip.setText("Host IP Address: " + ipAddressStr);

        if(runner == null){
            runner = new Thread(this);
            runner.start();
        }
        Toast.makeText(this, "スレッドスタート", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void run() {
        try {
            mServer = new ServerSocket(port);
            while (true) {
                Socket socket = mServer.accept();
                new EchoThread(socket).start();
            }
//            mSocket = mServer.accept();
//            BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
//            String message = in.readLine();
//
//            MainActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run () {
//                    TextView tlog = (TextView) findViewById(R.id.textLog);
//                    tlog.setText(message);
//                }
//            });
//
//            PrintWriter pw = new PrintWriter(mSocket.getOutputStream(), true);
//            TextView tAzimuth = findViewById(R.id.textAzimuth);
//            String response = tAzimuth.getText().toString();
//            pw.println(response);
//
//            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class EchoThread extends Thread {
        private Socket socket;

        public EchoThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message = in.readLine();

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run () {
                        TextView tlog = (TextView) findViewById(R.id.textLog);
                        tlog.setText(message);
                    }
                });

                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                TextView tAzimuth = findViewById(R.id.textAzimuth);
                String response = tAzimuth.getText().toString();
                pw.println(response);

                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private float rad2deg( float rad ) {
        // 0度 ~ 360度 で表示（北を0度として時計回り）
        if (rad > 0){
            return rad * (float) 180.0 / (float) Math.PI;
        }else {
            return (float) 360.0 + rad * (float) 180.0 / (float) Math.PI;
        }
    }
    protected void onStart() { // ⇔ onStop
        super.onStart();

        sensorManager.registerListener(
                sensorEventListener,
                sensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER ),
                (int)1e6 );
        sensorManager.registerListener(
                sensorEventListener,
                sensorManager.getDefaultSensor( Sensor.TYPE_MAGNETIC_FIELD ),
                (int)1e6 );
    }

    protected void onStop() { // ⇔ onStart
        super.onStop();

        sensorManager.unregisterListener( sensorEventListener );
    }

    final float filterVal = 0.8f;

    public void LowPassFilter(float[] target ){
        float outVal[] = new float[3];
        outVal[0] = (float)(saveAcceleVal[0] * filterVal
                + target[0] * (1-filterVal));
        outVal[1] = (float)(saveAcceleVal[1] * filterVal
                + target[1] * (1-filterVal));
        outVal[2] = (float)(saveAcceleVal[2] * filterVal
                + target[2] * (1-filterVal));

        //現在の測定値を次の計算に使うため保存する
        saveAcceleVal = target.clone();

        //加速度センサーから得た値を書き換える
        fAccell = outVal.clone();
        return ;
    }
    public void LowPassFilter2(float[] target ){
        float outVal[] = new float[3];
        outVal[0] = (float)(saveMagneticVal[0] * filterVal
                + target[0] * (1-filterVal));
        outVal[1] = (float)(saveMagneticVal[1] * filterVal
                + target[1] * (1-filterVal));
        outVal[2] = (float)(saveMagneticVal[2] * filterVal
                + target[2] * (1-filterVal));

        //現在の測定値を次の計算に使うため保存する
        saveMagneticVal = target.clone();

        //加速度センサーから得た値を書き換える
        fMagnetic = outVal.clone();
        return ;
    }
}