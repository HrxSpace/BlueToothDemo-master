package com.kaka.btdemo.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.kaka.btdemo.R;
import com.kaka.btdemo.utils.BtUtil;
import com.kaka.btdemo.utils.HandlerDispatcherUtil;
import com.kaka.btdemo.utils.Utils;

import java.util.Arrays;

import static com.kaka.btdemo.global.GlobalSign.HANDLER_A2DP_CONNECTED;
import static com.kaka.btdemo.global.GlobalSign.HANDLER_A2DP_DISCONNECTED;
import static com.kaka.btdemo.global.GlobalSign.HANDLER_SERIAL_PORT;
import static com.kaka.btdemo.global.GlobalSign.HANDLER_SPP_CONNECTED;
import static com.kaka.btdemo.global.GlobalSign.HANDLER_SPP_DISCONNECTED;
import static com.kaka.btdemo.global.GlobalSign.HANDLER_UPDATE_RECEIVE;

/**
 * Created by hrx on 2017/4/16.
 * 连接到蓝牙串口
 */

public class LinkSerialPortActivity extends BaseActivity implements View.OnClickListener, BtUtil.BtInterface {

    private TextView mA2dpStatus;
    private TextView mSppStatus;
    private EditText mSendContent;
    private TextView mReceiveContent;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //蓝牙A2DP连接广播
                case HANDLER_A2DP_CONNECTED:
                    mA2dpStatus.setTextColor(getResources().getColor(R.color.text_green));
                    mA2dpStatus.setText("A2DP已连接");
                    BluetoothDevice btDevice = (BluetoothDevice) msg.obj;
                    BtUtil.getInstance().connect(btDevice);
                    break;
                //蓝牙A2DP断开广播
                case HANDLER_A2DP_DISCONNECTED:
                    mA2dpStatus.setTextColor(getResources().getColor(R.color.text_gray));
                    mA2dpStatus.setText("A2DP未连接");
                    break;
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
        setContentView(R.layout.activity_serial_port);
        init();
        initView();
    }

    private void init() {
        BtUtil.getInstance().init(this);
        BtUtil.getInstance().setBtListener(this);
        HandlerDispatcherUtil.getInstance().registerHandler(HANDLER_SERIAL_PORT, mHandler);
    }

    private void initView() {
        mA2dpStatus = (TextView) this.findViewById(R.id.tx_a2dp_status);
        mSppStatus = (TextView) this.findViewById(R.id.tx_spp_status);
        mSendContent = (EditText) this.findViewById(R.id.et_send_content);
        mReceiveContent = (TextView) this.findViewById(R.id.tx_receive_content);
        this.findViewById(R.id.btn_back).setOnClickListener(this);
        this.findViewById(R.id.btn_open_system_bt).setOnClickListener(this);
        this.findViewById(R.id.btn_connect_exist_device).setOnClickListener(this);
        this.findViewById(R.id.btn_send).setOnClickListener(this);

        mReceiveContent.setMovementMethod(ScrollingMovementMethod.getInstance());
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
            case R.id.btn_open_system_bt:
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));//跳转到系统蓝牙设置界面
                break;
            case R.id.btn_connect_exist_device:
                BtUtil.getInstance().connectExistDevice();
                break;
            case R.id.btn_send:
                String str = mSendContent.getText().toString();
                BtUtil.getInstance().write(str.getBytes());
                Utils.getInstance().hideInputMethod(this);
                break;
        }
    }

    @Override
    public void readBtCallBack(byte[] data, int length) {
        String beforeStr = mReceiveContent.getText().toString();
        String newStr = beforeStr + "\n" + new String(data) + "(" + Arrays.toString(data) + ")";
        HandlerDispatcherUtil.getInstance().onMessage(HANDLER_SERIAL_PORT, HANDLER_UPDATE_RECEIVE, newStr);
    }
}
