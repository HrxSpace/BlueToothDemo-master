package com.kaka.btdemo.receiver;

/**
 * Created by hrx on 2016/10/24.
 * 监听蓝牙A2DP连接状态的广播
 */

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kaka.btdemo.utils.HandlerDispatcherUtil;

import static com.kaka.btdemo.global.GlobalSign.HANDLER_A2DP_CONNECTED;
import static com.kaka.btdemo.global.GlobalSign.HANDLER_A2DP_DISCONNECTED;
import static com.kaka.btdemo.global.GlobalSign.HANDLER_SERIAL_PORT;

public class BluetoothReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //A2DP的蓝牙连接状态发生改变
        if (BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
            BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//已连接的远程蓝牙设备
            int A2DPState = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1);//A2DP连接状态，0--未连接，1--正在连接，2--连接成功
            //未连接或者断开连接
            if (A2DPState == 0) {
                HandlerDispatcherUtil.getInstance().onMessage(HANDLER_SERIAL_PORT, HANDLER_A2DP_DISCONNECTED, null);
            }
            //A2DP连接成功
            if (A2DPState == 2) {
                HandlerDispatcherUtil.getInstance().onMessage(HANDLER_SERIAL_PORT, HANDLER_A2DP_CONNECTED, btDevice, 2000);
            }
        }
    }
}