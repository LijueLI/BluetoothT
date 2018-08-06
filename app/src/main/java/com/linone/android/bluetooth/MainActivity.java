package com.linone.android.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter Ba;
    private Button Bt,Sin,Tri,Rec,Noo,fa,fs,Hza,Hzs;
    private ListView DevL;
    private String[] Dev;
    private List<BluetoothDevice> DEVL = new ArrayList<BluetoothDevice>();
    private static final UUID MYUUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private TextView Hz,f;
    private int i=0,fp = 5;
    private int hz=1;
    private BluetoothSocket S;
    private OutputStream os;
    private boolean conF = false;
    private View cv = null;
    private BroadcastReceiver BTReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context,Intent intent){
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device!=null) {
                    DEVL.add(device);
                    i++;
                }
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                if(i!=0) {
                    Dev = new String[i];
                    i = 0;
                    for (BluetoothDevice d : DEVL) {
                        Dev[i] = d.getName();
                        i++;
                    }
                    ArrayAdapter adapter = new ArrayAdapter(MainActivity.this,
                            android.R.layout.simple_list_item_1,
                            Dev);
                    DevL.setAdapter(adapter);
                    i=0;
                }
                Bt.setEnabled(true);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        Ba = BluetoothAdapter.getDefaultAdapter();

        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn,0);

        Bt = findViewById(R.id.getdevice);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(BTReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(BTReceiver, filter);

        DevL = findViewById(R.id.DevL);

        Sin = findViewById(R.id.sin);
        Tri = findViewById(R.id.tri);
        Rec = findViewById(R.id.rec);
        Noo = findViewById(R.id.noout);
        fs = findViewById(R.id.fd);
        fa = findViewById(R.id.fa);
        Hza = findViewById(R.id.Hzadd);
        Hzs = findViewById(R.id.Hzsub);
        Hz = findViewById(R.id.Hz);
        f = findViewById(R.id.f);


        Bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Ba.isDiscovering()) {
                    Ba.cancelDiscovery();
                }
                DEVL.clear();
                Ba.startDiscovery();
                Bt.setEnabled(false);
            }
        });

        DevL.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(conF == true) close();
                if(cv == null || cv!=view) {
                    view.setBackgroundColor(Color.parseColor("#9E9E9E"));
                    if(cv!=null) cv.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    cv = view;
                }
                link(i);
            }
        });

        Sin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendmsg("S001");
            }
        });
        Tri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendmsg("S002");
            }
        });
        Rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendmsg("S003");
            }
        });
        Noo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendmsg("S004");
            }
        });
        Hza.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hz < 200){
                    hz+=1;
                }
                sendmsg("H"+String.format("%03d",hz));
                double H = (double)hz/2;
                Hz.setText(Double.toString(H)+" Hz");
            }
        });
        Hza.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    updateAddOrSubtract(view.getId());
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    stopAddOrSubtract();
                }
                return true;
            }
        });
        Hzs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hz > 1){
                    hz-=1;
                }
                sendmsg("H"+String.format("%03d",hz));
                double H = (double)hz/2;
                Hz.setText(Double.toString(H)+" Hz");
            }
        });
        Hzs.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    updateAddOrSubtract(view.getId());
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    stopAddOrSubtract();
                }
                return true;
            }
        });
        fa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fp < 10){
                    fp+=1;
                }
                sendmsg("A"+String.format("%03d",fp));
                f.setText(Integer.toString(fp));
            }
        });
        fs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fp > 1){
                    fp-=1;
                }
                sendmsg("A"+String.format("%03d",fp));
                f.setText(Integer.toString(fp));
            }
        });
    }
    private void link(final int i){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    S=DEVL.get(i).createRfcommSocketToServiceRecord(MYUUID);
                    S.connect();
                    os = S.getOutputStream();
                    //if(os!=null) os.write("OK".getBytes("ASCII"));
                }catch (IOException e){

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(os!=null) {
                            Sin.setEnabled(true);
                            Tri.setEnabled(true);
                            Rec.setEnabled(true);
                            Noo.setEnabled(true);
                            fs.setEnabled(true);
                            fa.setEnabled(true);
                            Hza.setEnabled(true);
                            Hzs.setEnabled(true);
                            conF = true;
                        }
                        else conF = false;
                    }
                });
            }
        }).start();
    }
    private void close(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    S.close();
                }catch (IOException e){

                }
            }
        }).start();
    }
    private void sendmsg(final String S){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    os.write(S.getBytes("ASCII"));
                }catch (Exception e){

                }
            }
        }).start();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //.makeText(this, "onDestroy", Toast.LENGTH_LONG).show();
        unregisterReceiver(BTReceiver);
    }
    private ScheduledExecutorService scheduledExecutor;
    private void updateAddOrSubtract(int viewId) {
        final int vid = viewId;
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = vid;
                handler.sendMessage(msg);
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void stopAddOrSubtract() {
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdownNow();
            scheduledExecutor = null;
        }
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int viewId = msg.what;
            switch (viewId){
                case R.id.Hzadd:
                    if(hz < 200){
                        hz+=1;
                    }
                    sendmsg("H"+String.format("%03d",hz));
                    double H = (double)hz/2;
                    Hz.setText(Double.toString(H)+" Hz");
                    break;
                case R.id.Hzsub:
                    if(hz > 1){
                        hz-=1;
                    }
                    sendmsg("H"+String.format("%03d",hz));
                    double Hs = (double)hz/2;
                    Hz.setText(Double.toString(Hs)+" Hz");
                    break;
            }
        }
    };
}

