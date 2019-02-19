package com.vnbamboo.helloguest;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.vnbamboo.helloguest.Utility.DEFAULT_URL;

public class ViewInfoGuestActivity extends AppCompatActivity {
    boolean receivedDataFromServer = false;
    Button btnConfirm, btnCancel;
    ProgressDialog progressDialog;
    TextView txtName, txtDoB, txtHomeTown, txtAddress, txtSeat, txtGender, txtDepartment;
    CircleImageView imgAvatar;
    String usercode;
    JSONObject jsonReturn;
    long beginTime, progress;
    Context thisContext = this.getBaseContext();

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
        usercode = getIntent().getStringExtra("usercode");
        sendRequestInfo();

    }
    private void setControl(){
        imgAvatar = findViewById(R.id.imgAvatar);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);
        txtName = findViewById(R.id.txtName);
        txtDoB = findViewById(R.id.txtDoB);
        txtHomeTown = findViewById(R.id.txtHomeTown);
        txtAddress = findViewById(R.id.txtAddress);
        txtSeat = findViewById(R.id.txtSeat);
        txtGender = findViewById(R.id.txtGender);
        txtDepartment = findViewById(R.id.txtDepartment);
    }
    private void addEvent(){
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                onBackPressed();
            }
        });
        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                final Dialog settingsDialog = new Dialog(v.getContext());
                settingsDialog.setContentView(getLayoutInflater().inflate(R.layout.image_dialog, null));
                Button btnOK = settingsDialog.findViewById(R.id.btnOK);
                ImageView imgDialogAvatar = settingsDialog.findViewById(R.id.imgDialogAvatar);
                    imgDialogAvatar.setImageDrawable(imgAvatar.getDrawable());
//                    imgDialogAvatar.setImageURI(Uri.parse(jsonReturn.getString("avatar")));
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        settingsDialog.dismiss();
                    }
                });
                settingsDialog.show();
            }
        });
    }

    private void boundData( JSONObject jsonObject){
        if(jsonObject.length() > 0){
            try {
                txtName.setText(jsonObject.getString("name"));
                txtDoB.setText(jsonObject.getString("dob"));
                txtHomeTown.setText(jsonObject.getString("hometown"));
                txtAddress.setText(jsonObject.getString("address"));
                txtSeat.setText(usercode.substring(usercode.length()-2));
                txtGender.setText(jsonObject.getString("sex").equals("false") ? "Nam" : "Nữ");
                txtDepartment.setText(jsonObject.getString("department"));
                Picasso.get().load(jsonObject.getString("avatar")).into(imgAvatar);
            }catch (Exception e){
                e.printStackTrace();

            }
        }else{
            setTextContent("Đang tải dữ liệu...");
        }
    }
    private void setTextContent(String content){
        txtName.setText(content);
        txtDoB.setText(content);
        txtHomeTown.setText(content);
        txtAddress.setText(content);
        txtSeat.setText(content);
        txtGender.setText(content);
        txtDepartment.setText(content);
    }
    private void sendRequestInfo() {
        beginTime = System.currentTimeMillis();
        receivedDataFromServer = false;
        progressDialog = ProgressDialog.show(ViewInfoGuestActivity.this, "",
                "Đang tải dữ liệu khách mời. Xin đợi 1 chút...", true);
        progress = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (progress <= 100) {
                        Thread.sleep(50);
                        handle.sendMessage(handle.obtainMessage());
                        if (progress == 100 && !receivedDataFromServer) {
                            setTextContent("Không thể tải dữ liệu...");
                            progressDialog.dismiss();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        AsyncHttpClient client = new AsyncHttpClient();
//        client.setTimeout(5000);
//        client.setConnectTimeout(5000);
//        client.setResponseTimeout(5000);
        client.get(DEFAULT_URL + "users/" + usercode, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                // called before request is started
                jsonReturn = new JSONObject();
                Log.e("callAPI", "begin");
                boundData(jsonReturn);
            }

            @Override
            public void onSuccess( int statusCode, Header[] headers, JSONObject response ) {
                jsonReturn = response;
                boundData(jsonReturn);
                receivedDataFromServer = true;
                Log.e("callAPI", jsonReturn.toString());
                progressDialog.dismiss();
            }

            @Override
            public void onFailure( int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse ) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                setTextContent("Không thể tải dữ liệu...");
                progressDialog.dismiss();
            }
        });
    }
    Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e("seeeeee", String.valueOf(progress));
            progress++;
        }
    };
}

