package com.linone.android.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter Ba;
    private Set<BluetoothDevice> pairedDevices;
    private Button Bt;
    private ListView devicelist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Ba = BluetoothAdapter.getDefaultAdapter();

        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn,0);

        Bt=(Button)findViewById(R.id.getdevice);
        devicelist = (ListView)findViewById(R.id.Device_list);



        Bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Ba.startDiscovery();

                getdevice();
            }
        });
    }

    private void getdevice(){
        BroadcastReceiver BTReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context,Intent intent){
                String action = intent.getAction();
                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(device.getBondState()!=BluetoothDevice.BOND_BONDED){

                    }
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    setTitle("OK");
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(BTReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(BTReceiver, filter);
    }
}
