package com.vnbamboo.helloguest;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class ViewInfoGuestActivity extends AppCompatActivity {
    Button btnConfirm, btnCancel;
    ProgressDialog progressDoalog;
    @Override
    protected void onCreate( @Nullable Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_info_guest);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().setStatusBarColor(getColor(R.color.blue_2));
            }
        }
        setControl();
        addEvent();
    }
    private void setControl(){
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);
    }
    private void addEvent(){
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                onBackPressed();
            }
        });
    }
    private void sendRequestInfo() {
        progressDoalog = ProgressDialog.show(ViewInfoGuestActivity.this, "",
                "Đang tải dữ liệu khách mời. Xin đợi 1 chút...", true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (progressDoalog.getProgress() <= progressDoalog.getMax()) {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Handler handle = new Handler() {
            @Override
            public void handleMessage( Message msg ) {
                super.handleMessage(msg);
                progressDoalog.incrementProgressBy(1);
            }
        };
    }
}
