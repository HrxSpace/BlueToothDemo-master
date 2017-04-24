package com.kaka.btdemo.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.kaka.btdemo.global.GlobalSign.HANDLER_PHONE;
import static com.kaka.btdemo.global.GlobalSign.HANDLER_SPP_CONNECTED;
import static com.kaka.btdemo.global.GlobalSign.HANDLER_SPP_DISCONNECTED;


/**
 * Created by kaka on 2016/7/29.
 * 蓝牙工具类，用于手机之间的连接
 */
public class BtDriver {

    private final static String TAG = "BtDriver";

    private static final String mDriver_NAME = "BlueToothChat";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//00001101-0000-1000-8000-00805F9B34FB用于android和蓝牙串口通讯

    private ExecutorService mBtWriteThreadExecutor;//单线程线程池

    private static BtDriver mBtDriver;
    private BluetoothAdapter mBTAdapter;
    private BluetoothSocket mBTSocket;

    /**
     * 线程类
     */
    private ConnectThread mConnectThread;//连接蓝牙
    private AcceptThread mAcceptThread;//蓝牙接收线程
    private InOutThread mIOThread;//蓝牙接收输入流读取线程

    private BtInterface mBtInterface;//数据接收接口

    /**
     * 构造方法
     */
    private BtDriver() {
        this.mBtWriteThreadExecutor = Executors.newSingleThreadExecutor();
        this.mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * 单例,双重校验锁
     */
    public static BtDriver getInstance() {
        if (mBtDriver == null) {
            synchronized (BtDriver.class) {
                if (mBtDriver == null) {
                    mBtDriver = new BtDriver();
                }
            }
        }
        return mBtDriver;
    }

    /**
     * 蓝牙监听接口
     */
    public interface BtInterface {
        void readBtCallBack(byte[] data, int length);
    }

    public void setBtListener(BtInterface btInterface) {
        mBtInterface = btInterface;
    }

    /**
     * 蓝牙是否开启
     */
    public boolean isEnable() {
        return mBTAdapter.isEnabled();
    }

    /**
     * 开启蓝牙,不提示的方式
     */
    public void enableBluetooth() {
        mBTAdapter.enable();
    }

    /**
     * 关闭蓝牙
     */
    public void disableBluetooth() {
        mBTAdapter.disable();
    }

    /**
     * 开启扫描
     */
    public void startDiscovery() {
        mBTAdapter.startDiscovery();
    }

    /**
     * 关闭扫描
     */
    public void cancelDiscovery() {
        mBTAdapter.cancelDiscovery();
    }

    /**
     * 获取绑定设备
     */
    public Set<BluetoothDevice> getBondedDevices() {
        return mBTAdapter.getBondedDevices();
    }

    /**
     * 连接蓝牙
     *
     * @param device 远程设备
     */
    public synchronized void connect(BluetoothDevice device) {

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

    /**
     * 开启蓝牙通讯线程
     */
    public void startAccept() {
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
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
     * 连接选定的蓝牙设备
     */
    private class ConnectThread extends Thread {

        public ConnectThread(BluetoothDevice device) {
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
            mBTAdapter.cancelDiscovery();
            try {
                mBTSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mBTSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                //如果蓝牙连接失败那么就返回
                return;
            }
            // 把创建的socket传入，提供输出流
            if (mBTSocket != null) {
                inOutManager();
            }
        }

        /**
         * 取消socket
         */
        public void cancel() {
            try {
                mBTSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 蓝牙监听，相当于服务端
     */
    private class AcceptThread extends Thread {

        private BluetoothServerSocket mServerSocket;

        //创建一个蓝牙的serverSocket
        public AcceptThread() {
            try {
                mServerSocket = mBTAdapter.listenUsingRfcommWithServiceRecord(mDriver_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "AcceptThread:---" + "监听失败");
            }
        }

        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    mBTSocket = mServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "run: ---" + "accept失败");
                    startAccept();//结束掉重新创建
                    break;
                }
                if (mBTSocket != null) {
                    inOutManager();
                    if (mAcceptThread != null) {
                        mAcceptThread.cancel();
                        mAcceptThread = null;
                    }
                }
            }
        }

        //取消线程
        public void cancel() {
            try {
                mServerSocket.close();
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
            HandlerDispatcherUtil.getInstance().onMessage(HANDLER_PHONE, HANDLER_SPP_CONNECTED, null);
            // 当连接状态的时候持续监听
            while (true) {
                try {
                    // 从输入流读取信息
                    length = mInStream.read(buffer);
                    byte[] bytes = Arrays.copyOfRange(buffer, 0, length);
                    delBackInfo(bytes, length);
                } catch (IOException e) {
                    BtDriver.getInstance().cancel();
                    HandlerDispatcherUtil.getInstance().onMessage(HANDLER_PHONE, HANDLER_SPP_DISCONNECTED, null);
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
     * 停止所有线程
     */
    public synchronized void stop() {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mIOThread != null) {
            mIOThread.cancel();
            mIOThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
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
     * 处理接收到的数据
     */
    private void delBackInfo(byte[] data, int length) {
        if (mBtInterface != null) {
            mBtInterface.readBtCallBack(data, length);
        }
    }
}