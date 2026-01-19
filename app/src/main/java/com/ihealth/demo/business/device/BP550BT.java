package com.ihealth.demo.business.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.ec.easylibrary.dialog.confirm.ConfirmDialog;
import com.ec.easylibrary.utils.ToastUtils;
import com.ihealth.communication.control.Bp550BTControl;
import com.ihealth.communication.control.BpProfile;
import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.ihealth.demo.R;
import com.ihealth.demo.base.BaseActivity;
import com.ihealth.demo.business.FunctionFoldActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class BP550BT extends FunctionFoldActivity {
    private Context mContext;
    private static final String TAG = "BP550BT";
    private Bp550BTControl mBp550btControl;
    private int mClientCallbackId;

    @Override
    public int contentViewID() {
        return R.layout.activity_bp550bt;
    }

    @Override
    public void initView() {
        mContext = this;
        Intent intent = getIntent();
        mDeviceMac = intent.getStringExtra("mac");
        mDeviceName = intent.getStringExtra("type");
        /* register ihealthDevicesCallback id */
        mClientCallbackId = iHealthDevicesManager.getInstance().registerClientCallback(miHealthDevicesCallback);
        /* Limited wants to receive notification specified device */
        iHealthDevicesManager.getInstance().addCallbackFilterForDeviceType(mClientCallbackId, iHealthDevicesManager.TYPE_550BT);
        /* Get bp550bt controller */
        mBp550btControl = iHealthDevicesManager.getInstance().getBp550BTControl(mDeviceMac);
//        setDeviceInfo(mDeviceName, mDeviceMac);

        // 设置点击监听器
        findViewById(R.id.btnDisconnect).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnIDPS).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnBattery).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnFunction).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnGetStatus).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnSetBackLight).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnSetLocking).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnDataNum).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnGetData).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnGetTime).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnTransferFinished).setOnClickListener(this::onViewClicked);
    }

    private iHealthDevicesCallback miHealthDevicesCallback = new iHealthDevicesCallback() {

        @Override
        public void onDeviceConnectionStateChange(String mac, String deviceType, int status, int errorID) {
            Log.i(TAG, "mac: " + mac);
            Log.i(TAG, "deviceType: " + deviceType);
            Log.i(TAG, "status: " + status);
            if (status == iHealthDevicesManager.DEVICE_STATE_DISCONNECTED) {
                addLogInfo(mContext.getString(R.string.connect_main_tip_disconnect));
                ToastUtils.showToast(mContext, mContext.getString(R.string.connect_main_tip_disconnect));
                finish();
            }
        }

        @Override
        public void onUserStatus(String username, int userStatus) {
            Log.i(TAG, "username: " + username);
            Log.i(TAG, "userState: " + userStatus);
        }

        @Override
        public void onDeviceNotify(String mac, String deviceType, String action, String message) {
            Log.i(TAG, "mac: " + mac);
            Log.i(TAG, "deviceType: " + deviceType);
            Log.i(TAG, "action: " + action);
            Log.i(TAG, "message: " + message);


            if (BpProfile.ACTION_BATTERY_BP.equals(action)) {
                try {
                    JSONObject info = new JSONObject(message);
                    String battery = info.getString(BpProfile.BATTERY_BP);
                    Message msg = new Message();
                    msg.what = HANDLER_MESSAGE;
                    msg.obj = "battery: " + battery;
                    myHandler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else if (BpProfile.ACTION_ERROR_BP.equals(action)) {
                try {
                    JSONObject info = new JSONObject(message);
                    String num = info.getString(BpProfile.ERROR_NUM_BP);
                    Message msg = new Message();
                    msg.what = HANDLER_MESSAGE;
                    msg.obj = "error num: " + num;
                    myHandler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if (BpProfile.ACTION_HISTORICAL_DATA_BP.equals(action)) {
                String str = "{}";
                try {
                    JSONObject info = new JSONObject(message);
                    if (info.has(BpProfile.HISTORICAL_DATA_BP)) {
                        JSONArray array = info.getJSONArray(BpProfile.HISTORICAL_DATA_BP);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String date = obj.getString(BpProfile.MEASUREMENT_DATE_BP);
                            String hightPressure = obj.getString(BpProfile.HIGH_BLOOD_PRESSURE_BP);
                            String lowPressure = obj.getString(BpProfile.LOW_BLOOD_PRESSURE_BP);
                            String pulseWave = obj.getString(BpProfile.PULSE_BP);
                            String ahr = obj.getString(BpProfile.MEASUREMENT_AHR_BP);
                            str = "date:" + date
                                    + "hightPressure:" + hightPressure + "\n"
                                    + "lowPressure:" + lowPressure + "\n"
                                    + "pulseWave" + pulseWave + "\n"
                                    + "ahr:" + ahr + "\n";
                            Message msg = new Message();
                            msg.what = HANDLER_MESSAGE;
                            msg.obj = str;
                            myHandler.sendMessage(msg);
                        }

                    }else {
                        Message msg = new Message();
                        msg.what = HANDLER_MESSAGE;
                        msg.obj = str;
                        myHandler.sendMessage(msg);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if (BpProfile.ACTION_HISTORICAL_NUM_BP.equals(action)) {
                try {
                    JSONObject info = new JSONObject(message);
                    String num = info.getString(BpProfile.HISTORICAL_NUM_BP);
                    Message msg = new Message();
                    msg.what = HANDLER_MESSAGE;
                    msg.obj = "num: " + num;
                    myHandler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if (BpProfile.ACTION_SET_UNIT_SUCCESS_BP.equals(action)) {
                Message msg = new Message();
                msg.what = HANDLER_MESSAGE;
                msg.obj = "set unit success";
                myHandler.sendMessage(msg);
            } else if (BpProfile.ACTION_FUNCTION_INFORMATION_BP.equals(action)) {
                Message msg = new Message();
                msg.what = HANDLER_MESSAGE;
                msg.obj = message;
                myHandler.sendMessage(msg);
            } else if (BpProfile.ACTION_SET_STATUS_DISPLAY_SUCCESS.equals(action)) {
                Message msg = new Message();
                msg.what = HANDLER_MESSAGE;
                msg.obj = "set display success";
                myHandler.sendMessage(msg);
            } else {
                Message msg = new Message();
                msg.what = HANDLER_MESSAGE;
                msg.obj = "message: " + message;
                myHandler.sendMessage(msg);
            }
        }
    };


    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_MESSAGE:
                    addLogInfo((String) msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };


    @Override
    protected void onDestroy() {
        if(mBp550btControl!=null){
            mBp550btControl.disconnect();
        }
        iHealthDevicesManager.getInstance().unRegisterClientCallback(mClientCallbackId);
        clearLogInfo();
        super.onDestroy();

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

    public void onViewClicked(View view) {
        if (mBp550btControl == null) {
            addLogInfo("mBp550btControl == null");
            return;
        }
        showLogLayout();
        int id = view.getId();
        if (id == R.id.btnDisconnect) {
            mBp550btControl.disconnect();
            addLogInfo("disconnect()");
        } else if (id == R.id.btnIDPS) {
            mBp550btControl.getIdps();
            addLogInfo("getIdps() -->" + mBp550btControl.getIdps());
        } else if (id == R.id.btnBattery) {
            mBp550btControl.getBattery();
            addLogInfo("getBattery()");
        } else if (id == R.id.btnFunction) {
            mBp550btControl.getFunctionInfo();
            addLogInfo("getFunctionInfo()");
        } else if (id == R.id.btnGetStatus) {
            mBp550btControl.getStatusOfDisplay();
            addLogInfo("getStatusOfDisplay()");
        } else if (id == R.id.btnSetBackLight) {
            mBp550btControl.setStatusOfDisplay(true, false);
            addLogInfo("setStatusOfDisplay()");
        } else if (id == R.id.btnSetLocking) {
            mBp550btControl.setStatusOfDisplay(false, true);
            addLogInfo("setStatusOfDisplay()");
        } else if (id == R.id.btnDataNum) {
            mBp550btControl.getOfflineNum();
            addLogInfo("getOfflineNum()");
        } else if (id == R.id.btnGetData) {
            mBp550btControl.getOfflineData();
            addLogInfo("getOfflineData()");
        } else if (id == R.id.btnGetTime) {
            mBp550btControl.getTime();
            addLogInfo("getTime()");
        } else if (id == R.id.btnTransferFinished) {
            mBp550btControl.transferFinished();
            addLogInfo("transferFinished()");
        }
    }
}
