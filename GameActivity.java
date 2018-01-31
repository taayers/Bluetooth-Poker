package com.triadicsoftware.bluetoothpoker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    protected final static int MESSAGE_READ = 9000;
    protected final static int CONNECT_SUCCESS = 9001;

    boolean gameHost;
    boolean isPlaying = false;

    Card card0;
    Card card1;
    Card card2;
    Card card3;
    Card card4;

    ImageView mOppCard0;
    ImageView mOppCard1;
    ImageView mOppCard2;
    ImageView mOppCard3;
    ImageView mOppCard4;

    ImageView mMyCard0;
    ImageView mMyCard1;
    ImageView mMyCard2;
    ImageView mMyCard3;
    ImageView mMyCard4;

    Button discardBtn;
    Button newGameBtn;

    Deck d;
    Hand hand1;
    Hand hand2;
    Hand myHand;
    Hand oppHand;

    ConnectThread connectThread;
    AcceptThread acceptThread;
    ConnectedThread connectedThread;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice device;
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case CONNECT_SUCCESS:
                    System.out.println("Successfully connected");
                    Toast.makeText(getApplicationContext(), "Successfully Connected", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_READ:
                    if(gameHost == true){
                        hostRead(msg);
                    }else{
                        clientRead(msg);
                    }
                    break;
            }
        }
    };

    UUID MY_UUID = UUID.fromString("bf6ff955-49f9-415d-b26e-ad86a4099a21");

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);

        gameHost = getIntent().getExtras().getBoolean("GameHost");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        init();

        newGame();

        //playPoker();
    }

    private void newGame() {
        if (gameHost == true) {
            if (!isPlaying){
                device = getIntent().getExtras().getParcelable("BTDevice");
                connectThread = new ConnectThread(device);
                connectThread.start();
                isPlaying = true;
                startGame();
                myHand = hand1;
                oppHand = hand2;
                setHandImages();
            }else{
                resetOppCardImages();
                newGameBtn.setEnabled(false);
                startGame();
                myHand = hand1;
                oppHand = hand2;
                setHandImages();
                String s = oppHand.handToString();
                connectedThread.write(s.getBytes());

            }
        }else{
            if (!isPlaying){
                acceptThread = new AcceptThread();
                acceptThread.start();
                isPlaying = true;
            }else{
                resetOppCardImages();
            }
        }
    }

    private void init() {

        discardBtn = (Button)findViewById(R.id.discardBtn);
        newGameBtn = (Button)findViewById(R.id.newGameBtn);

        mOppCard0 = (ImageView)findViewById(R.id.oppCard0);
        mOppCard1 = (ImageView)findViewById(R.id.oppCard1);
        mOppCard2 = (ImageView)findViewById(R.id.oppCard2);
        mOppCard3 = (ImageView)findViewById(R.id.oppCard3);
        mOppCard4 = (ImageView)findViewById(R.id.oppCard4);

        resetOppCardImages();

        mMyCard0 = (ImageView)findViewById(R.id.myCard0);
        mMyCard0.setImageResource(R.drawable.card_back);
        mMyCard1 = (ImageView)findViewById(R.id.myCard1);
        mMyCard1.setImageResource(R.drawable.card_back);
        mMyCard2 = (ImageView)findViewById(R.id.myCard2);
        mMyCard2.setImageResource(R.drawable.card_back);
        mMyCard3 = (ImageView)findViewById(R.id.myCard3);
        mMyCard3.setImageResource(R.drawable.card_back);
        mMyCard4 = (ImageView)findViewById(R.id.myCard4);
        mMyCard4.setImageResource(R.drawable.card_back);

        mMyCard0.setOnClickListener(this);
        mMyCard1.setOnClickListener(this);
        mMyCard2.setOnClickListener(this);
        mMyCard3.setOnClickListener(this);
        mMyCard4.setOnClickListener(this);

        if(gameHost == false){
            newGameBtn.setText("Leave Game");
            discardBtn.setEnabled(false);
            disableClientButtons();

        }else{
            discardBtn.setEnabled(false);
            newGameBtn.setEnabled(false);
        }

        discardBtn.setOnClickListener(this);
        newGameBtn.setOnClickListener(this);

    }

    private void startGame() {
        d = new Deck();
        hand1 = new Hand(d);
        hand2 = new Hand(d);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.myCard0:
                card0 = myHand.getCardAtPosition(0);
                card0.toggleFaceUp();
                configureCard(card0, mMyCard0, 0);
                break;
            case R.id.myCard1:
                card1 = myHand.getCardAtPosition(1);
                card1.toggleFaceUp();
                configureCard(card1, mMyCard1, 1);
                break;
            case R.id.myCard2:
                card2 = myHand.getCardAtPosition(2);
                card2.toggleFaceUp();
                configureCard(card2, mMyCard2, 2);
                break;
            case R.id.myCard3:
                card3 = myHand.getCardAtPosition(3);
                card3.toggleFaceUp();
                configureCard(card3, mMyCard3, 3);
                break;
            case R.id.myCard4:
                card4 = myHand.getCardAtPosition(4);
                card4.toggleFaceUp();
                configureCard(card4, mMyCard4, 4);
                break;
            case R.id.discardBtn:
                if(gameHost == true){
                    for(int i = 0; i < myHand.getCards().size(); i++){
                        Card card = myHand.getCardAtPosition(i);
                        if(card.getFaceUp() == false){
                            myHand.discardCard(i);
                        }
                    }
                    int draw = 5 - myHand.getCards().size();
                    myHand.drawCards(draw, d);
                    setHandImages();
                    playPoker();
                }else{
                    for(int i = 0; i < myHand.getCards().size(); i++){
                        Card card = myHand.getCardAtPosition(i);
                        if(card.getFaceUp() == false){
                            myHand.discardCard(i);
                        }
                    }
                    String s = myHand.handToString();
                    connectedThread.write(s.getBytes());
                    disableClientButtons();

                }
                break;
            case R.id.newGameBtn:
                if(gameHost == true){
                    newGame();
                }else{
                    connectedThread.cancel();
                }
                break;
        }
    }

    public void resetOppCardImages(){
        mOppCard0.setImageResource(R.drawable.card_back);
        mOppCard1.setImageResource(R.drawable.card_back);
        mOppCard2.setImageResource(R.drawable.card_back);
        mOppCard3.setImageResource(R.drawable.card_back);
        mOppCard4.setImageResource(R.drawable.card_back);
    }

    public void setHandImages(){
        mMyCard0.setImageResource(getImageId(this, myHand.displayCard(0)));
        mMyCard1.setImageResource(getImageId(this, myHand.displayCard(1)));
        mMyCard2.setImageResource(getImageId(this, myHand.displayCard(2)));
        mMyCard3.setImageResource(getImageId(this, myHand.displayCard(3)));
        mMyCard4.setImageResource(getImageId(this, myHand.displayCard(4)));
    }

    public void showOppCards(){
        mOppCard0.setImageResource(getImageId(this, oppHand.displayCard(0)));
        mOppCard1.setImageResource(getImageId(this, oppHand.displayCard(1)));
        mOppCard2.setImageResource(getImageId(this, oppHand.displayCard(2)));
        mOppCard3.setImageResource(getImageId(this, oppHand.displayCard(3)));
        mOppCard4.setImageResource(getImageId(this, oppHand.displayCard(4)));
    }

    public void playPoker(){
        myHand.evaluateHand();
        oppHand.evaluateHand();

        int result = myHand.compareTo(oppHand);

        showOppCards();

        if (result > 0){
            System.out.println("Hand one wins");
            Toast.makeText(getApplicationContext(), "You Win!", Toast.LENGTH_SHORT).show();
        }else if(result < 0){
            System.out.println("Hand two wins");
            Toast.makeText(getApplicationContext(), "You Lose...", Toast.LENGTH_SHORT).show();
        }else{
            System.out.println("Game is a draw");
            Toast.makeText(getApplicationContext(), "Game is a draw.", Toast.LENGTH_SHORT).show();
        }
        discardBtn.setEnabled(false);
        newGameBtn.setEnabled(true);
    }

    public void clientRead(Message msg){
        System.out.println("Client Message Read");
        byte[] readBuf = (byte[])msg.obj;
        String s = new String(readBuf);
        String r = s.replaceAll("[^a-z ]", "");
        Hand hand = new Hand(r);
        myHand = hand;
        setHandImages();
        enableClientButtons();
    }

    public void enableClientButtons(){
        mMyCard0.setEnabled(true);
        mMyCard1.setEnabled(true);
        mMyCard2.setEnabled(true);
        mMyCard3.setEnabled(true);
        mMyCard4.setEnabled(true);
        discardBtn.setEnabled(true);
    }

    public void disableClientButtons(){
        mMyCard0.setEnabled(false);
        mMyCard1.setEnabled(false);
        mMyCard2.setEnabled(false);
        mMyCard3.setEnabled(false);
        mMyCard4.setEnabled(false);
        //discardBtn.setEnabled(false);
    }

    private void hostRead(Message msg) {
        byte[] readBuf = (byte[])msg.obj;
        String s = new String(readBuf);
        String r = s.replaceAll("[^a-z ]", "");
        Hand hand = new Hand(r);
        oppHand = hand;
        int draw = 5 - oppHand.getCards().size();
        oppHand.drawCards(draw, d);
        String t = oppHand.handToString();
        connectedThread.write(t.getBytes());
        Toast.makeText(getApplicationContext(), "Continue Game", Toast.LENGTH_SHORT).show();
        discardBtn.setEnabled(true);
    }

    public static int getImageId(Context context, String imageName) {
        return context.getResources().getIdentifier("drawable/" + imageName, null, context.getPackageName());
    }

    void configureCard(Card card, ImageView imageView, int index){
        if(card.getFaceUp()){
            imageView.setImageResource(getImageId(this, myHand.displayCard(index)));
        }else{
            imageView.setImageResource(R.drawable.card_back);
        }
    }

    private void manageConnectedSocket(BluetoothSocket mmSocket) {
        connectedThread = new ConnectedThread(mmSocket);
        connectedThread.start();
        if(gameHost){
            String s = oppHand.handToString();
            connectedThread.write(s.getBytes());
        }

    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Bluetooth Poker", MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    mHandler.obtainMessage(CONNECT_SUCCESS).sendToTarget();
                    try {
                        mmServerSocket.close();
                    }catch(IOException e){}
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            mHandler.obtainMessage(CONNECT_SUCCESS).sendToTarget();
            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
