package com.kaka.btdemo.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kaka.btdemo.R;
import com.kaka.btdemo.recyclerview.HolderDelegate;
import com.kaka.btdemo.utils.BtDriver;
import com.kaka.recyclerviewlib.base.RecyclerAdp;
import com.kaka.recyclerviewlib.listener.OnItemClickListener;
import com.kaka.recyclerviewlib.mode.ItemData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by hrx on 2017/4/16.
 */

public class BtListActivity extends BaseActivity implements View.OnClickListener, OnItemClickListener {

    public static final int requestCode = 1000;

    private RecyclerAdp mRecyclerAdp;
    private List<ItemData> mItemDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_list);
        initView();
        initData();
    }

    private void initView() {
        this.findViewById(R.id.btn_back).setOnClickListener(this);
        mItemDataList = new ArrayList<>();
        mRecyclerAdp = new RecyclerAdp(mItemDataList, new HolderDelegate());
        mRecyclerAdp.setItemClickListener(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_bt_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mRecyclerAdp);
    }

    private void initData() {
        //获取蓝牙管理器，并设置广播监听，开启扫描
        IntentFilter bluetoothIntentFilter = new IntentFilter();
        bluetoothIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(new BtReceiver(), bluetoothIntentFilter);
        BtDriver.getInstance().startDiscovery();
        initBoundDevice();
    }

    private void initBoundDevice() {
        mItemDataList.add(new ItemData(HolderDelegate.HEADER_TYPE));
        Iterator<BluetoothDevice> iterator = BtDriver.getInstance().getBondedDevices().iterator();
        while (iterator.hasNext()) {
            mItemDataList.add(new ItemData(HolderDelegate.DEVICE_TYPE, iterator.next()));
        }
        mItemDataList.add(new ItemData(HolderDelegate.HEADER_TYPE));
        mRecyclerAdp.notifyItemRangeChanged(0, mRecyclerAdp.getItemCount());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
        }
    }

    @Override
    public void onClick(View view, int position, Object data) {
        ItemData itemData = (ItemData) data;
        if (itemData.data instanceof BluetoothDevice) {
            BluetoothDevice btDevice = (BluetoothDevice) itemData.data;
            Intent intent = new Intent();
            intent.putExtra("btDevice", btDevice);
            setResult(requestCode, intent);
            finish();
        }
    }

    @Override
    public boolean onLongClick(View view, int position, Object data) {
        return true;
    }

    private class BtReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                //找到设备
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    boolean contains = BtDriver.getInstance().getBondedDevices().contains(device);
                    if (!contains && !"".equals(device.getName())) {
                        mItemDataList.add(new ItemData(HolderDelegate.DEVICE_TYPE, device));
                        mRecyclerAdp.notifyItemInserted(mRecyclerAdp.getItemCount());
                        mRecyclerAdp.notifyItemChanged(mRecyclerAdp.getItemCount());
                    }
                    break;
                //扫描结束
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    BtDriver.getInstance().cancelDiscovery();//取消扫描
                    break;
            }
        }
    }
}
