package com.kaka.btdemo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by hrx on 2016/10/4.
 * 通用工具
 */
public class Utils {
    private static Utils instance;

    private Utils() {
    }

    public static Utils getInstance() {
        if (instance == null) {
            synchronized (Utils.class) {
                if (instance == null) {
                    instance = new Utils();
                }
            }
        }
        return instance;
    }

    /**
     * 隐藏输入法
     */
    public void hideInputMethod(Activity activity) {
        if (activity != null) {
            ((InputMethodManager) activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 获取当前系统版本号
     *
     * @return 系统版本号
     */
    public int getSDKVersionNumber() {
        int sdkVersion;
        try {
            sdkVersion = Integer.valueOf(Build.VERSION.SDK);
        } catch (NumberFormatException e) {
            sdkVersion = 0;
        }
        return sdkVersion;
    }

    /**
     * 获取包名
     *
     * @param context 上下文
     * @return 包名
     */
    public String getPkName(Context context) {
        return context.getApplicationContext().getPackageName();
    }

    /**
     * 获取版本名
     *
     * @param context 上下文
     * @return 版本名
     */
    public String getVersionName(Context context) {
        String pkName = context.getApplicationContext().getPackageName();
        String vsName = "";
        try {
            vsName = context.getApplicationContext().getPackageManager().getPackageInfo(pkName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return vsName;
    }

    /**
     * 获取版本号
     *
     * @param context 上下文
     * @return 版本号
     */
    public int getversionCode(Context context) {
        String pkName = context.getApplicationContext().getPackageName();
        int vsvCode = 1;
        try {
            vsvCode = context.getApplicationContext().getPackageManager().getPackageInfo(pkName, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return vsvCode;
    }

    /**
     * 获取编译信息
     * @param context 上下文
     * @return 第0位--日期，第1位时间，第2位版本名
     */
    public String[] getBuildInfo(Context context) {
        String data = "";
        try {
            InputStream open = context.getApplicationContext().getAssets().open("buildInfo.txt");
            InputStreamReader isr = new InputStreamReader(open);
            BufferedReader br = new BufferedReader(isr);
            data = br.readLine();
            br.close();
            isr.close();
            open.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] dataS = data.split(" ");
        return dataS;
    }

    /**
     * 创建在sd卡一个新文件夹
     *
     * @param path 例如：/data/data
     * @return 创建的文件夹路径
     */
    public String newFolder(Context context, String path) {

        String filePath = null;
        boolean storageState = getStorageState();
        if (storageState) {
            String storagePath = Environment.getExternalStorageDirectory().toString();
            File file_sd = new File(storagePath + path);
            if (!file_sd.exists()) {
                boolean mkdirs = file_sd.mkdirs();
                Log.d("mkdirs", "newFolder: " + mkdirs);
            }
            filePath = file_sd.getPath();
            Log.d("filePath---", filePath);
        } else {
            String fileDir = String.valueOf(context.getApplicationContext().getFilesDir());
            Log.d("fileDir---", fileDir);
            File file = new File(fileDir + path);
            if (!file.exists()) {
                file.mkdirs();
            }
            filePath = file.getPath();
        }
        return filePath;
    }

    /**
     * 获取sd卡是否可用状态
     *
     * @return
     */
    private boolean getStorageState() {
        //Environment.MEDIA_MOUNTED表示被挂载
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }
}
