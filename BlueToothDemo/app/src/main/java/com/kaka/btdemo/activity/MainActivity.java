package com.kaka.btdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.kaka.btdemo.R;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    private void initView() {
        findViewById(R.id.btn_connect_serial_port).setOnClickListener(this);
        findViewById(R.id.btn_connect_phone).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_connect_serial_port:
                startActivity(new Intent(MainActivity.this, LinkSerialPortActivity.class));
                break;
            case R.id.btn_connect_phone:
                startActivity(new Intent(MainActivity.this, LinkPhoneActivity.class));
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
