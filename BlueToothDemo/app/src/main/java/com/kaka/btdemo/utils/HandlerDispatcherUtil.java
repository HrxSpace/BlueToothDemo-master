package com.kaka.btdemo.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hrx on 2017/2/27.
 * Handler分发工具
 */

public class HandlerDispatcherUtil {

    private static HandlerDispatcherUtil instance;
    private ConcurrentHashMap<String, Handler> mHandlerMap;

    private HandlerDispatcherUtil() {
        mHandlerMap = new ConcurrentHashMap<>();
    }

    public static HandlerDispatcherUtil getInstance() {
        if (instance == null) {
            synchronized (HandlerDispatcherUtil.class) {
                if (instance == null) {
                    instance = new HandlerDispatcherUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 注册Handler
     *
     * @param tag
     * @param handler
     */
    public void registerHandler(String tag, Handler handler) {
        Handler hd = mHandlerMap.get(tag);
        if (hd == null) {
            mHandlerMap.put(tag, handler);
        }
    }

    /**
     * 反注册Handler
     *
     * @param tag
     */
    public void unregisterHandler(String tag, Handler handler) {
        handler.removeCallbacksAndMessages(null);
        if (tag == null) {
            mHandlerMap.clear();
        } else {
            mHandlerMap.remove(tag);
        }
    }

    public void onMessage(String tag, Message msg) {
        Handler hd = mHandlerMap.get(tag);
        if (hd != null) {
            Log.d("onMessage", "onMessage: "+hd.hasMessages(msg.what));
            Message message = hd.obtainMessage();
            message.what = msg.what;
            message.obj = msg.obj;
            message.arg1 = msg.arg1;
            message.arg2 = msg.arg2;
            hd.sendMessage(msg);
        }
    }

    public void onMessage(String tag, Message msg, int delay) {
        Handler hd = mHandlerMap.get(tag);
        if (hd != null) {
            hd.sendMessageDelayed(msg, delay);
        }
    }

    public void onMessage(String tag, int what, Object obj) {
        Handler hd = mHandlerMap.get(tag);
        if (hd != null) {
            if (obj != null) {
                Message msg = new Message();
                msg.what = what;
                msg.obj = obj;
                hd.sendMessage(msg);
            } else {
                hd.sendEmptyMessage(what);
            }
        }
    }

    public void onMessage(String tag, int what, Object obj, int delay) {
        Handler hd = mHandlerMap.get(tag);
        if (hd != null) {
            if (obj != null) {
                Message msg = new Message();
                msg.what = what;
                msg.obj = obj;
                hd.sendMessageDelayed(msg, delay);
            } else {
                hd.sendEmptyMessageDelayed(what, delay);
            }
        }
    }

}
