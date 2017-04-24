package com.kaka.btdemo.recyclerview;

import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaka.btdemo.R;
import com.kaka.recyclerviewlib.base.BaseViewHolder;
import com.kaka.recyclerviewlib.mode.ItemData;

import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_CLASSIC;
import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_DUAL;
import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_LE;
import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_UNKNOWN;

/**
 * Created by hrx on 2017/4/23.
 */

public class DeviceViewHolder extends BaseViewHolder<ItemData> {
    private TextView deviceName;
    private TextView deviceType;

    public DeviceViewHolder(ViewGroup parent, View itemView) {
        super(parent, itemView);
    }

    @Override
    public void findViews() {
        deviceName = (TextView) itemView.findViewById(R.id.item_device_name);
        deviceType = (TextView) itemView.findViewById(R.id.item_device_type);
    }

    @Override
    public void onBindViewHolder(ItemData itemData) {
        if (itemData.data instanceof BluetoothDevice) {
            BluetoothDevice btDevice = (BluetoothDevice) itemData.data;
            deviceName.setText(btDevice.getName());
            switch (btDevice.getType()) {
                case DEVICE_TYPE_UNKNOWN:
                    deviceType.setText("未知");
                    break;
                case DEVICE_TYPE_CLASSIC:
                    deviceType.setText("BR/EDR");
                    break;
                case DEVICE_TYPE_LE:
                    deviceType.setText("LE-only");
                    break;
                case DEVICE_TYPE_DUAL:
                    deviceType.setText("BR/EDR/LE");
                    break;
            }

        }
    }

    @Override
    public boolean clickAble() {
        return true;
    }
}
