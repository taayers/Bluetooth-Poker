package com.triadicsoftware.bluetoothpoker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

public class BTListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ArrayAdapter<String> listAdapter;
    ListView listView;

    ArrayList<BluetoothDevice> btDevices;
    Set<BluetoothDevice> deviceArray;
    ArrayList<String> pairedDevices;
    BroadcastReceiver mReceiver;
    BluetoothAdapter mBluetoothAdapter;
    IntentFilter filter;

    boolean gameHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btlist);

        init();

        getPairedDevices();
        //startDiscovery();
    }

    private void init() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        gameHost = getIntent().getExtras().getBoolean("GameHost");
        listView = (ListView)findViewById(R.id.btList);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        btDevices = new ArrayList<BluetoothDevice>();
        pairedDevices = new ArrayList<String>();
        // Create a BroadcastReceiver for ACTION_FOUND
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    String s = "";
                    for (int a = 0; a < pairedDevices.size(); a++) {
                        if (device.getName().equals(pairedDevices.get(a))) {
                            s = "(Paired)";
                            break;
                        }

                    }

                    listAdapter.add(device.getName() + " " + s + " " + "\n" + device.getAddress());
                    btDevices.add(device);

                }
//                }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
//
//                }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
//
//                }else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
//
//                }
            }
        };

        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
//        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//        registerReceiver(mReceiver, filter);
//        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        registerReceiver(mReceiver, filter);
//        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void getPairedDevices(){
        deviceArray = mBluetoothAdapter.getBondedDevices();
        if(deviceArray.size() > 0) {
            for (BluetoothDevice device : deviceArray) {
                pairedDevices.add(device.getName());
                listAdapter.add(device.getName() + "\n" + device.getAddress());
                btDevices.add(device);
            }
        }
    }

    private void startDiscovery() {
        mBluetoothAdapter.cancelDiscovery();
        boolean result = mBluetoothAdapter.startDiscovery();
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        BluetoothDevice selectedDevice = btDevices.get(position);
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("BTDevice", selectedDevice);
        intent.putExtra("GameHost", gameHost);
        startActivity(intent);
    }

}
