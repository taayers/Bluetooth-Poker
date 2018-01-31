package com.triadicsoftware.bluetoothpoker;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class StartGameActivity extends AppCompatActivity{

    Boolean gameHost;

    protected static final int REQUEST_ENABLE_BT = 9999;
    protected static final int REQUEST_DISCOVERABLE = 9998;
    BluetoothAdapter mBluetoothAdapter;
    BroadcastReceiver mReceiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);

        init();

        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            System.out.println("Phone does not support Bluetooth.");
            Toast.makeText(getApplicationContext(), "No Bluetooth detected.", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            if(!mBluetoothAdapter.isEnabled()) {
                turnOnBT();
            }

        }

    }

    void init(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    public void turnOnBT(){
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    public void createGame(View view) {
        gameHost = true;
        sendHostIntent();
        //sendHostIntent();
    }

    public void joinGame(View view){
        gameHost = false;
        sendClientIntent();
        //Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        //discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        //startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void sendHostIntent(){
        Intent intent = new Intent(this, BTListActivity.class);
        intent.putExtra("GameHost", gameHost);
        startActivity(intent);
    }

    public void sendClientIntent(){
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("GameHost", gameHost);
        startActivity(intent);
    }

}
