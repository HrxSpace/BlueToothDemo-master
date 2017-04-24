package com.kaka.btdemo.global;

/**
 * Created by hrx on 2017/4/16.
 */

public class GlobalSign {

    /**
     * Handler标记
     */
    public static final String HANDLER_SERIAL_PORT = "serial_port";//LinkSerialPortActivity
    public static final String HANDLER_PHONE = "phone";//LinkPhoneActivity

    /**
     * Handler标记
     */
    public static final int HANDLER_A2DP_CONNECTED = 1;//蓝牙A2DP连接广播
    public static final int HANDLER_A2DP_DISCONNECTED = 2;////蓝牙A2DP断开广播
    public static final int HANDLER_SPP_CONNECTED = 3;//建立SPP通道建立成功
    public static final int HANDLER_SPP_DISCONNECTED = 4;//SPP通道断开
    public static final int HANDLER_UPDATE_RECEIVE = 5;//更新接收到的数据
}
