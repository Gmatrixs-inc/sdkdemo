package com.gms.sdkdemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import com.gms.sdk.GmsManager;
import com.gms.sdk.ILoginCallBack;
import com.gms.sdk.IPayCallback;
import com.gms.sdk.PermissionListener;
import com.gms.sdk.bean.Config;
import com.gms.sdk.utils.FloatGravity;
import com.gms.sdk.utils.PermissionUtils;
import com.gms.sdk.utils.ToastFactory;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
public class MainActivity extends Activity implements View.OnClickListener{
    private Button btn_center, btn_login, btn_pay;
    private EditText edit;
    private String[] perms;
    private String appId = "you app id";
    private String comment = "Game currency recharge!";
    //your Beneficiary Address;
    private String address="0x***********";

    public Config config;
    private String orderId = null;
    private PermissionUtils permissionUtils;
    private static final int REQUEST_CODE_SDK_RESULT_PERMISSIONS = 102;
    private boolean showRequestPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edit = findViewById(R.id.edit);
        btn_center = (Button) findViewById(R.id.btn_center);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_pay = (Button) findViewById(R.id.btn_pay);
        btn_center.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        btn_pay.setOnClickListener(this);

        getConfig();
        perms = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
        };
        //Permission Request
        permissionUtils = new PermissionUtils(this);
        setPermission();
        GmsManager.setSDKLoginCallback(new ILoginCallBack() {
            @Override
            public void OnSuccess(String message) {
                try {
                    JSONObject object = new JSONObject(message);
                    //Login validation callback
                    String signdata = object.getString("signdata");
                    String uname = object.getString("uname");
                    String address = object.getString("address");

                    ToastFactory.showToast(MainActivity.this, signdata);
                    Log.e("++++signdata++++++", signdata);
                    //Display suspension ball
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void OnFailed(String msg) {
                ToastFactory.showToast(MainActivity.this, msg);
            }
        });
    }



    private void setPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            permissionUtils.requestRunPermisssion(perms, new PermissionListener() {
                @Override
                public void onGranted() {
                    //he instructions are all authorized
                    GmsManager.init(MainActivity.this, config);
                }

                @Override
                public void onDenied(List<String> deniedPermission) {
                    for (int i = 0; i < deniedPermission.size(); i++) {
                        showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, deniedPermission.get(i));
                    }
                    if (showRequestPermission) {
                        setPermission();
                    } else {
                        showMissingPermissionDialog();
                    }
                }
            });
        } else {
            GmsManager.init(MainActivity.this, config);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //Need to manually open the missing permissions dialog
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("prompt");
        builder.setMessage("For your normal use of SDK, please open the permissions!");
        builder.setPositiveButton("Go Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    //Start application settings to manually open permissions
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE_SDK_RESULT_PERMISSIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SDK_RESULT_PERMISSIONS) {
            setPermission();
        }
    }

    public void getConfig() {
        config = new Config();
        //sdk appId
        config.setAppId(appId);
        //Suspension ball initialization display position
        config.setFloatGravity(FloatGravity.TOP_CENTER);
        //Screen vertical screen setting,Horizontal screen is true
        config.setLandscape(true);
        //1 ETH 2HECO
        config.setPlat(1);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_login:
                login();
                break;
            case R.id.btn_center:
                openUserCenter();
                break;
            case R.id.btn_pay:
                pay();
                break;
            default:
                break;
        }

    }
    public void login() {
        GmsManager.login(MainActivity.this);
    }

    public void openUserCenter() {
        if (GmsManager.isLogin() == Config.LOGIN) {
            GmsManager.openUserCenter();
        } else {
            login();
        }
    }

    public void pay() {
        if (GmsManager.isLogin() == Config.LOGIN) {
            orderId = String.valueOf(Math.random() * 1000000000);
            String amount = edit.getText().toString().trim();
            if (amount == null || amount.equals("")) {
                ToastFactory.showToast(this, "Please enter the amount of recharge!");
            } else {
                GmsManager.setSDKPayCallback(new IPayCallback() {
                    @Override
                    public void onPaySuccess(String jsonStr) {
                        try {
                            JSONObject object = new JSONObject(jsonStr);
                            //Payment order number
                            String payOrderId = object.getString("payOrderId");
                            //Transaction Number
                            String txid = object.getString("txid");

                            ToastFactory.showToast(MainActivity.this, jsonStr.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onPayFail(String error) {
                        ToastFactory.showToast(MainActivity.this, error);
                    }
                });
                GmsManager.pay(MainActivity.this, orderId,address, amount, comment + System.currentTimeMillis());
            }
        } else {
            login();
        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        GmsManager.onDestory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GmsManager.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        GmsManager.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GmsManager.hideFloatingView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        GmsManager.onStop();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Are you sure you want to quit the game?")
                .setPositiveButton("sure", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("cancel", null)
                .show();
    }}
