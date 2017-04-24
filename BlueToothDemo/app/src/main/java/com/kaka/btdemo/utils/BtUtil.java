package com.kaka.btdemo.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.kaka.btdemo.global.GlobalSign.HANDLER_SERIAL_PORT;
import static com.kaka.btdemo.global.GlobalSign.HANDLER_SPP_CONNECTED;
import static com.kaka.btdemo.global.GlobalSign.HANDLER_SPP_DISCONNECTED;

/**
 * Created by hrx on 2016/7/29.
 * 蓝牙操作工具类,用于与蓝牙串口设备连接
 */
public class BtUtil {
    private final static String TAG = "BtUtil---";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//00001101-0000-1000-8000-00805F9B34FB用于android和蓝牙串口通讯
    private static BtUtil instance;
    private static BluetoothSocket mBTSocket;
    private Context mContext;
    private BluetoothAdapter mBtAdapter;

    private ExecutorService mBtWriteThreadExecutor;//单线程线程池

    private int repeatConnect = 3;//自动重连次数
    /**
     * 线程类
     */
    private ConnectThread mConnectThread;//连接蓝牙
    private InOutThread mIOThread;//蓝牙接收输入流读取线程

    private BtInterface mBtInterface;//数据接收接口

    /**
     * 蓝牙监听接口
     */
    public interface BtInterface {
        void readBtCallBack(byte[] data, int length);
    }

    /**
     * 构造方法
     */
    private BtUtil() {
        mBtWriteThreadExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * 单例,双重校验锁
     */
    public static BtUtil getInstance() {
        if (instance == null) {
            synchronized (BtUtil.class) {
                if (instance == null) {
                    instance = new BtUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public void init(Context context) {
        this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mContext = context.getApplicationContext();
    }

    public void setBtListener(BtInterface btInterface) {
        mBtInterface = btInterface;
    }

    /**
     * 连接蓝牙
     *
     * @param device 远程设备
     */
    public void connect(BluetoothDevice device) {

        //如果之前存在那么关闭，并置空
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mIOThread != null) {
            mIOThread.cancel();
            mIOThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    //输出内容
    public void write(final byte[] out) {
        mBtWriteThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mIOThread != null) {
                    mIOThread.write(out);
                }
            }
        });
    }

    //输入输出流管理器
    private void inOutManager() {
        if (mIOThread != null) {
            mIOThread.cancel();
            mIOThread = null;
        }
        mIOThread = new InOutThread();
        mIOThread.start();
    }

    /**
     * 检查已连接的蓝牙设备
     */
    public void connectExistDevice() {
        if (mBtAdapter == null) {
            return;
        }
        mBtAdapter.getProfileProxy(mContext, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceDisconnected(int profile) {
            }

            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
                if (mDevices != null && mDevices.size() > 0) {
                    for (BluetoothDevice d : mDevices) {
                        Log.d(TAG, "onServiceConnected: ");
                        connect(d);//建立SPP通讯
                    }
                }
            }
        }, BluetoothProfile.A2DP);
    }

    /**
     * 关闭所有
     */
    private void cancel() {
        //如果之前存在那么关闭，并置空
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mIOThread != null) {
            mIOThread.cancel();
            mIOThread = null;
        }
    }

    /**
     * 连接选定的蓝牙设备
     */
    private class ConnectThread extends Thread {

        private ConnectThread(BluetoothDevice device) {
            BluetoothSocket blSocket = null;
            try {
                //mMY_UUID为00001101-0000-1000-8000-00805F9B34FB
                blSocket = device.createRfcommSocketToServiceRecord(MY_UUID);//通过UUID创建一个BluetoothSocket
            } catch (IOException e) {
                e.printStackTrace();
            }
            mBTSocket = blSocket;
        }

        @Override
        public void run() {
            super.run();
            //先把扫描关闭
            mBtAdapter.cancelDiscovery();
            try {
                mBTSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mBTSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                if (repeatConnect > 0) {
                    TimerUtils.getInstance().addTimerTask(new TimerTask() {
                        @Override
                        public void run() {
                            connectExistDevice();
                            repeatConnect--;
                        }
                    }, 1500, "repeatConnect");
                }
                return;
            }
            // 把创建的socket传入，提供输出流
            if (mBTSocket != null) {
                inOutManager();
                repeatConnect = 3;
            }
        }

        /**
         * 取消socket
         */
        private void cancel() {
            try {
                mBTSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 读写线程
     */
    private class InOutThread extends Thread {

        private InputStream mInStream;//读取别的蓝牙传输过来的信息的流
        private OutputStream mOutStream;//输出到别的蓝牙信息的输出流

        private InOutThread() {

            try {
                mInStream = mBTSocket.getInputStream();
                mOutStream = mBTSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();
            byte[] buffer = new byte[1024];//存放数据的内容
            int length;//数据内容的长度
            HandlerDispatcherUtil.getInstance().onMessage(HANDLER_SERIAL_PORT, HANDLER_SPP_CONNECTED, null);
            // 当连接状态的时候持续监听
            while (true) {
                try {
                    // 从输入流读取信息
                    length = mInStream.read(buffer);
                    byte[] bytes = Arrays.copyOfRange(buffer, 0, length);
                    delBackInfo(bytes, length);
                } catch (IOException e) {
                    BtUtil.getInstance().cancel();
                    HandlerDispatcherUtil.getInstance().onMessage(HANDLER_SERIAL_PORT, HANDLER_SPP_DISCONNECTED, null);
                    break;
                }
            }
        }

        //输出
        private void write(byte[] buffer) {
            try {
                mOutStream.write(buffer);
                mOutStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        private void cancel() {
            try {
                mOutStream.close();
                mInStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理HUD端返回回来的信息
     */
    private void delBackInfo(byte[] data, int length) {
        if (mBtInterface != null) {
            mBtInterface.readBtCallBack(data, length);
        }
    }
}