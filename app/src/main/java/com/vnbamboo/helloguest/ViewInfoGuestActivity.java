package com.vnbamboo.helloguest;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
    Context thisContext = this;

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

    private void setControl() {
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

    private void setTextContent( String content ) {
        txtName.setText(content);
        txtDoB.setText(content);
        txtHomeTown.setText(content);
        txtAddress.setText(content);
        txtSeat.setText(content);
        txtGender.setText(content);
        txtDepartment.setText(content);
    }

    private void addEvent() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                onBackPressed();
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                confirmAttendant();
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

    private void boundData( JSONObject jsonObject ) {
        if (jsonObject.length() > 0) {
            try {
                txtName.setText(jsonObject.getString("name"));
                txtDoB.setText(jsonObject.getString("dob"));
                txtHomeTown.setText(jsonObject.getString("hometown"));
                txtAddress.setText(jsonObject.getString("address"));
                txtSeat.setText(usercode.substring(2, 4));
                txtGender.setText(jsonObject.getString("sex").equals("false") ? "Nam" : "Nữ");
                txtDepartment.setText(jsonObject.getString("department"));
                Picasso.get().load(jsonObject.getString("avatar")).into(imgAvatar);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            setTextContent("Đang tải dữ liệu...");
        }
    }

    private void confirmAttendant() {
        if (jsonReturn.length() > 0) {
            /* Tạo hộp thoại xác nhận*/
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Thông báo");
            try {
                builder.setMessage("Xác nhận đại biểu " + jsonReturn.getString("name") + " đã có mặt tại đại hội?");
            } catch (Exception e) {
                e.printStackTrace();
            }
            builder.setCancelable(false);
            builder.setPositiveButton("Hủy bỏ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick( DialogInterface dialogInterface, int i ) {
                    dialogInterface.dismiss();
//                    Toast.makeText(ViewInfoGuestActivity.this, "Không thoát được", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Xác nhận", new DialogInterface.OnClickListener() {
                @Override
                public void onClick( DialogInterface dialogInterface, int i ) {
                    dialogInterface.dismiss();
                    sendConfirmRequest();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
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
                    while (progress <= 250) {
                        Thread.sleep(50);
                        handlerIncreaseProgress.sendMessage(handlerIncreaseProgress.obtainMessage());
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
        client.get(DEFAULT_URL + "users/" + usercode.toUpperCase(), new JsonHttpResponseHandler() {
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
                Log.e("GET DATA", jsonReturn.toString());
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

    private void sendConfirmRequest() {
        beginTime = System.currentTimeMillis();
        receivedDataFromServer = false;
        progressDialog = ProgressDialog.show(ViewInfoGuestActivity.this, "",
                "Đang điểm danh đại biểu...", true);
        progress = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (progress <= 250) {
                        Thread.sleep(50);
                        handlerIncreaseProgress.sendMessage(handlerIncreaseProgress.obtainMessage());
                        if (progress == 100 && !receivedDataFromServer) {
                            setTextContent("Có sự cố, xin hãy thử lại.");
                            progressDialog.dismiss();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(DEFAULT_URL + "users/" + usercode.toUpperCase(), new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                Log.e("callAPI", "begin");
            }

            @Override
            public void onSuccess( int statusCode, Header[] headers, JSONObject response ) {
                progressDialog.dismiss();
                receivedDataFromServer = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(thisContext);
                try {
                    builder.setMessage(response.getString("status").equals("true") ?
                            "Chào mừng đại biểu " + jsonReturn.getString("name") + " đã có mặt tại đại hội!" :
                            "Có lỗi xảy ra! Hãy thử lại.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialogInterface, int i ) {
                        dialogInterface.dismiss();
//                        Toast.makeText(ViewInfoGuestActivity.this, "Không thoát được", Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }

            @Override
            public void onFailure( int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse ) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                setTextContent("Có sự cố, xin hãy thử lại.");
                progressDialog.dismiss();
            }
        });
    }

    Handler handlerIncreaseProgress = new Handler() {
        @Override
        public void handleMessage( Message msg ) {
            super.handleMessage(msg);
            Log.e("seeeeee", String.valueOf(progress));
            progress++;
        }
    };
}

