package com.menthoven.arduinoandroid;

import java.util.UUID;

/**
 * Created by da Ent on 1-11-2015.
 */
public interface Constants {

    String TAG = "Arduino - Android";
    int REQUEST_ENABLE_BT = 1;

    // message types sent from the BluetoothChatService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_SNACKBAR = 4;

    // Constants that indicate the current connection state
    int STATE_NONE = 0;       // we're doing nothing
    int STATE_ERROR = 1;
    int STATE_CONNECTING = 2; // now initiating an outgoing connection
    int STATE_CONNECTED = 3;  // now connected to a remote device


    UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Key names received from the BluetoothChatService Handler
    String EXTRA_DEVICE  = "EXTRA_DEVICE";
    String SNACKBAR = "toast";
//0停车，2后退，4左行，6右行，8前行，7顺时针转，9逆时针转，1切回找灯模式
    int STOP_CAR=0;
    int GO_BACKWARD=2;
    int MOVE_LEFT=4;
    int MOVE_RIGHT=6;
    int GO_FORWARD=8;
    int TURN_RIGHT=7;
    int TURN_LEFT=9;
    int TURN_ROUND=1;

}
