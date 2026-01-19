package com.ihealth.demo.business.device;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ec.easylibrary.dialog.confirm.ConfirmDialog;
import com.ec.easylibrary.dialog.loadingdialog.LoadingDialog;
import com.ihealth.communication.control.HS6Control;
import com.ihealth.communication.manager.iHealthDeviceHs6Callback;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.ihealth.demo.R;
import com.ihealth.demo.business.FunctionFoldActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;


public class HS6 extends FunctionFoldActivity {
    EditText mEtUserName;
    Button mBtnCreate;
    Button mBtnAuthorization;
    EditText mEtSsid;
    EditText mEtPassword;
    EditText mEtDeviceKey;
    EditText mEtSetUnit;
    Button mBtnSetWifi;
    EditText mEtBirthday;
    EditText mEtSerial;
    EditText mEtHeight;
    EditText mEtWeight;
    EditText mEtGender;
    EditText mEtSport;
    Button mBtnBind;
    Button mBtnUnBind;

    Button mBtnGetData;
    EditText mEtClientId;
    EditText mEtClientSecret;
    EditText mEtClientRandom;
    Button mBtnGetToken;
    private Context mContext;
    private HS6Control mHS6control;
    private final String TAG = "HS6";
    private LoadingDialog mLoadingDialog;

    @Override
    public int contentViewID() {
        return R.layout.activity_hs6;
    }

    @Override
    public void initView() {
        mContext = this;
        mLoadingDialog = new LoadingDialog(mContext);
        mLoadingDialog.setCancellable(true);
        Intent intent = getIntent();
        mDeviceMac = intent.getStringExtra("mac");
        mDeviceName = intent.getStringExtra("type");

        // 初始化视图
        mEtUserName = findViewById(R.id.etUserName);
        mBtnCreate = findViewById(R.id.btnCreate);
        mBtnAuthorization = findViewById(R.id.btnAuthorization);
        mEtSsid = findViewById(R.id.mEtSsid);
        mEtPassword = findViewById(R.id.etPassword);
        mEtDeviceKey = findViewById(R.id.etDeviceKey);
        mEtSetUnit = findViewById(R.id.etUnit);
        mBtnSetWifi = findViewById(R.id.btnSetWifi);
        mEtBirthday = findViewById(R.id.etBirthday);
        mEtSerial = findViewById(R.id.etSerial);
        mEtHeight = findViewById(R.id.etHeight);
        mEtWeight = findViewById(R.id.etWeight);
        mEtGender = findViewById(R.id.etGender);
        mEtSport = findViewById(R.id.etSport);
        mBtnBind = findViewById(R.id.btnBind);
        mBtnUnBind = findViewById(R.id.btnUnBind);
        mBtnGetData = findViewById(R.id.btnGetData);
        mEtClientId = findViewById(R.id.etClientId);
        mEtClientSecret = findViewById(R.id.etClientSecret);
        mEtClientRandom = findViewById(R.id.etClientRandom);
        mBtnGetToken = findViewById(R.id.btnGetToken);
        
        // 设置点击监听器
        mBtnCreate.setOnClickListener(this::onViewClicked);
        mBtnAuthorization.setOnClickListener(this::onViewClicked);
        mBtnSetWifi.setOnClickListener(this::onViewClicked);
        mBtnBind.setOnClickListener(this::onViewClicked);
        mBtnUnBind.setOnClickListener(this::onViewClicked);
        mBtnGetData.setOnClickListener(this::onViewClicked);
        mBtnGetToken.setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnSetUnit).setOnClickListener(this::onViewClicked);
    }

    iHealthDeviceHs6Callback mIHealthDeviceHs6Callback = new iHealthDeviceHs6Callback() {
        public void setWifiNotify(String Type, String action, String message) {
            Log.e(TAG, "type:" + Type + "--action:" + action + "--message:" + message);
            if (action.equals(HS6Control.ACTION_HS6_SETWIFI)) {
                JSONTokener jsonTokener = new JSONTokener(message);
                JSONObject jsonObject;
                try {
                    jsonObject = (JSONObject) jsonTokener.nextValue();
                    boolean result = jsonObject.getBoolean(HS6Control.SETWIFI_RESULT);
                    Log.d(TAG, "result:" + result);
                    mHandler.sendEmptyMessage(2);
                    Message handMessage = new Message();
                    handMessage.what = HANDLER_MESSAGE;
                    handMessage.obj = "Set Wifi Result:" + result;
                    mHandler.sendMessage(handMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onNotify(String mac, String Type, String action, String message) {
            Log.e(TAG, "mac:" + mac + "--type:" + Type + "--action:" + action + "--mesage:" + message);
            Message handMessage = new Message();
            handMessage.what = HANDLER_MESSAGE;
            JSONTokener jsonTokener = new JSONTokener(message);
            switch (action) {
                case HS6Control.ACTION_HS6_BIND:
                    mHandler.sendEmptyMessage(2);
                    try {
                        JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                        JSONArray jsonArray = jsonObject.getJSONArray(HS6Control.HS6_BIND_EXTRA);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject2 = (JSONObject) jsonArray.get(i);
                            int result = jsonObject2.getInt(HS6Control.BIND_HS6_RESULT);
                            String model = jsonObject2.getString(HS6Control.HS6_MODEL);
                            int position = jsonObject2.getInt(HS6Control.HS6_POSITION);
                            int settedWifi = jsonObject2.getInt(HS6Control.HS6_SETTED_WIFI);
                            if (result == 1) {
                                handMessage.obj = "bind success";
                            } else if (result == 2) {
                                handMessage.obj = "the scale has no empty position";
                            } else {
                                handMessage.obj = "bind fail";
                            }
                            handMessage.obj = "result:" + result + "--model:" + model + "--position:" + position + "--setted:" + settedWifi;
                        }
                        mHandler.sendMessage(handMessage);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    break;
                case HS6Control.ACTION_HS6_ERROR:
                    try {
                        JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                        int error = jsonObject.getInt(HS6Control.HS6_ERROR);
                        handMessage.obj = "error:" + error;
                        mHandler.sendMessage(handMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case HS6Control.ACTION_HS6_UNBIND:
                    mHandler.sendEmptyMessage(2);
                    try {
                        JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                        boolean result = jsonObject.getBoolean(HS6Control.HS6_UNBIND_RESULT);
                        Log.d(TAG, "UnBind result:" + result);
                        handMessage.obj = "UnBind result:" + result;
                        mHandler.sendMessage(handMessage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case HS6Control.ACTION_HS6_GET_TOKEN:
                    mHandler.sendEmptyMessage(2);
                    try {
                        JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                        String result = jsonObject.getString(HS6Control.GET_TOKEN_RESULT);
                        Log.d(TAG, "Get Token result:" + result);
                        handMessage.obj = "Get Token result:" + result;
                        mHandler.sendMessage(handMessage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case HS6Control.ACTION_HS6_SET_UNIT:
                    mHandler.sendEmptyMessage(2);
                    try {
                        JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                        boolean result = jsonObject.getBoolean(HS6Control.SET_UNIT_RESULT);
                        Log.d(TAG, "Set unit result:" + result);
                        handMessage.obj = "Set unit result:" + result;
                        mHandler.sendMessage(handMessage);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case HS6Control.ACTION_HS6_GET_DATA:
                    mHandler.sendEmptyMessage(2);
                    try {
                        JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                        long downloadTS = jsonObject.getLong(HS6Control.DATA_DOWNLOAD_TS);
                        Log.d(TAG, "downloadTS:" + downloadTS);
                        handMessage.obj = "Get Data downloadTS:" + downloadTS + "Get Data:" + message;
                        mHandler.sendMessage(handMessage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }

        public void onUserStatus(String username, int userStatus) {
            Log.e(TAG, "onUserStatus username = " + username + " userStatus = " + userStatus);
            Message handMessage = new Message();
            handMessage.what = HANDLER_MESSAGE;
            handMessage.obj = "onUserStatus username = " + username + " userStatus = " + userStatus;
            mHandler.sendMessage(handMessage);
        }
    };


    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mLoadingDialog.show();
                    break;
                case 2:
                    mLoadingDialog.dismiss();
                    break;
                case HANDLER_MESSAGE:
                    addLogInfo((String) msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };


    public void onViewClicked(View view) {
        showLogLayout();
        int id = view.getId();
        if (id == R.id.btnCreate) {
            final String userName = mEtUserName.getText().toString().trim();
            mHS6control = new HS6Control(userName, this, iHealthDevicesManager.TYPE_HS6, mIHealthDeviceHs6Callback);
            mBtnAuthorization.setEnabled(true);
            addLogInfo("Create HS6Control");
        } else if (id == R.id.btnAuthorization) {
            try {
                InputStream is = getAssets().open("com_demo_sdk_android.pem");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                mHS6control.sdkAuthWithLicense(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mBtnSetWifi.setEnabled(true);
            mBtnBind.setEnabled(true);
            mBtnUnBind.setEnabled(true);
            mBtnGetData.setEnabled(true);
            mBtnGetToken.setEnabled(true);
            addLogInfo("HS6 SdkAuthWithLicense()");
        } else if (id == R.id.btnSetWifi) {
            mLoadingDialog.show();
            String ssid = mEtSsid.getText().toString().trim();
            String password = mEtPassword.getText().toString().trim();
            String deviceKey = mEtDeviceKey.getText().toString().trim();
            if (deviceKey.isEmpty()) {
                mHS6control.setWifi(ssid, password);
            } else {
                mHS6control.setWifi(ssid, password, deviceKey);
            }
            addLogInfo("HS6 setWifi --> ssid:" + ssid + " password:" + password + " deviceKey:" + deviceKey);
        } else if (id == R.id.btnBind) {
            mLoadingDialog.show();
            final String birthday = mEtBirthday.getText().toString().trim();
            final String weight = mEtWeight.getText().toString().trim();
            final String height = mEtHeight.getText().toString().trim();
            final String sport = mEtSport.getText().toString().trim();
            final String gender = mEtGender.getText().toString().trim();
            final String serialNumber = mEtSerial.getText().toString().trim();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mHS6control.bindDeviceHS6(birthday, Float.parseFloat(weight), Integer.parseInt(height),
                            Integer.parseInt(sport), Integer.parseInt(gender), serialNumber);

                }
            }).start();
            addLogInfo("HS6 bindDeviceHS6() --> birthday:" + birthday + " weight:" + weight + " height:" + height
                    + " sport:" + sport + " gender:" + gender + " serialNumber:" + serialNumber);
        } else if (id == R.id.btnUnBind) {
            mLoadingDialog.show();
            final String unSerialNumber = mEtSerial.getText().toString().trim();
            new Thread(new Runnable() {

                @Override
                public void run() {
                    mHS6control.unBindDeviceHS6(unSerialNumber);
                }
            }).start();
            addLogInfo("HS6 unBindDeviceHS6() --> serialNumber:" + unSerialNumber);
        } else if (id == R.id.btnSetUnit) {
            mLoadingDialog.show();
            final String unit = mEtSetUnit.getText().toString().trim();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mHS6control.setUnit(mEtUserName.getText().toString().trim(), Integer.parseInt(unit));
                }
            }).start();
            addLogInfo("HS6 btnSetUnit() --> unit:" + unit);
        } else if (id == R.id.btnGetData) {
            mLoadingDialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mHS6control.getDataByMeasuretimeFromCloud(0, 5);
                }
            }).start();
            addLogInfo("HS6 getDataByMeasuretimeFromCloud()");
        } else if (id == R.id.btnGetToken) {
            mLoadingDialog.show();
            final String clientId = mEtClientId.getText().toString().trim();
            final String clientSecret = mEtClientSecret.getText().toString().trim();
            final String clientPara = mEtClientRandom.getText().toString().trim();
            final String username = mEtUserName.getText().toString().trim();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mHS6control.getToken(clientId, clientSecret, clientPara, username);
                }
            }).start();
            addLogInfo("HS6 getToken() --> clientId:" + clientId + " clientSecret:" + clientSecret + " clientPara:" + clientPara
                    + " username:" + username);
        }
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            //如果当前在认证错误的页面 则直接返回 最开始的页面重新取认证
            if (isShowingLogLayout()) {
                hideLogLayout();
            } else {
                showConfirmDialog(mContext, mContext.getString(R.string.confirm_tip_function_title),
                        mContext.getString(R.string.confirm_tip_function_message, mDeviceName, mDeviceMac), new ConfirmDialog.OnClickLisenter() {
                            @Override
                            public void positiveOnClick() {
                                finish();
                            }

                            @Override
                            public void nagetiveOnClick() {

                            }
                        });
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
    
}
