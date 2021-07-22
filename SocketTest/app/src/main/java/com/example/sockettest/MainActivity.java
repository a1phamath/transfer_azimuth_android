package com.example.sockettest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /** ボタンをクリックした時に呼ばれる */
    public void connect(View view) {
        new Thread (new Runnable() {
            public void run() {
                EditText editText = findViewById(R.id.editTextIp);
                String ip = editText.getText().toString();
                int port = 12345;

                InetSocketAddress address = new InetSocketAddress(ip,  port);
                Socket socket = new Socket();
                try {
                    socket.connect(address, 3000);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run () {
                            Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_LONG).show();
                        }
                    });

                    // メッセージ送信
                    PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                    Date dTime = new Date();
                    String message = dTime.toString();
                    pw.println(message);

                    // メッセージ受信
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String response = in.readLine();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run () {
                            TextView tRes = findViewById(R.id.textResponse);
                            tRes.setText(response);
                        }
                    });

                    socket.close();
                } catch (Exception e) {
                    Log.e("hoge", e.toString());
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run () {
                            Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }
}