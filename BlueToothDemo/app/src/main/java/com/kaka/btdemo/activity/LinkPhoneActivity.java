package com.kaka.btdemo.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.kaka.btdemo.R;
import com.kaka.btdemo.utils.BtDriver;
import com.kaka.btdemo.utils.HandlerDispatcherUtil;
import com.kaka.btdemo.utils.Utils;

import java.util.Arrays;

import static com.kaka.btdemo.global.GlobalSign.HANDLER_PHONE;
import static com.kaka.btdemo.global.GlobalSign.HANDLER_SERIAL_PORT;
import static com.kaka.btdemo.global.GlobalSign.HANDLER_SPP_CONNECTED;
import static com.kaka.btdemo.global.GlobalSign.HANDLER_SPP_DISCONNECTED;
import static com.kaka.btdemo.global.GlobalSign.HANDLER_UPDATE_RECEIVE;

/**
 * Created by hrx on 2017/4/23.
 */

public class LinkPhoneActivity extends BaseActivity implements BtDriver.BtInterface, View.OnClickListener {
    private TextView mSppStatus;
    private EditText mSendContent;
    private TextView mReceiveContent;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //建立SPP通道建立成功
                case HANDLER_SPP_CONNECTED:
                    mSppStatus.setTextColor(getResources().getColor(R.color.text_green));
                    mSppStatus.setText("SPP已连接");
                    break;
                //SPP通道断开
                case HANDLER_SPP_DISCONNECTED:
                    mSppStatus.setTextColor(getResources().getColor(R.color.text_gray));
                    mSppStatus.setText("SPP未连接");
                    break;
                case HANDLER_UPDATE_RECEIVE:
                    mReceiveContent.setText((String) msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        init();
        initView();
    }

    private void init() {
        BtDriver.getInstance().startAccept();
        BtDriver.getInstance().setBtListener(this);
        HandlerDispatcherUtil.getInstance().registerHandler(HANDLER_PHONE, mHandler);
    }

    private void initView() {
        mSppStatus = (TextView) this.findViewById(R.id.tx_spp_status);
        mSendContent = (EditText) this.findViewById(R.id.et_send_content);
        mReceiveContent = (TextView) this.findViewById(R.id.tx_receive_content);
        this.findViewById(R.id.btn_back).setOnClickListener(this);
        this.findViewById(R.id.btn_open_bt_list).setOnClickListener(this);
        this.findViewById(R.id.btn_send).setOnClickListener(this);

        mReceiveContent.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @Override
    public void readBtCallBack(byte[] data, int length) {
        String beforeStr = mReceiveContent.getText().toString();
        String newStr = beforeStr + "\n" + new String(data) + "(" + Arrays.toString(data) + ")";
        HandlerDispatcherUtil.getInstance().onMessage(HANDLER_PHONE, HANDLER_UPDATE_RECEIVE, newStr);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HandlerDispatcherUtil.getInstance().unregisterHandler(HANDLER_SERIAL_PORT, mHandler);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_open_bt_list:
                startActivityForResult(new Intent(LinkPhoneActivity.this, BtListActivity.class), BtListActivity.requestCode);
                break;
            case R.id.btn_send:
                String str = mSendContent.getText().toString();
                BtDriver.getInstance().write(str.getBytes());
                Utils.getInstance().hideInputMethod(this);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Parcelable device = data.getParcelableExtra("btDevice");
        if (device instanceof BluetoothDevice) {
            BluetoothDevice btDevice = (BluetoothDevice) device;
            BtDriver.getInstance().connect(btDevice);
        }
    }
}
