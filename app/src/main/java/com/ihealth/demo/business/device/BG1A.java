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
import com.ihealth.communication.control.Bg1aControl;
import com.ihealth.communication.control.Bg1aProfile;
import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.ihealth.demo.R;
import com.ihealth.demo.business.FunctionFoldActivity;

import java.io.IOException;
import java.io.InputStream;



public class BG1A extends FunctionFoldActivity {

    private Context mContext;
    private static final String TAG = "BG1S";
    private Bg1aControl mBg1aControl;
    private int mClientCallbackId;
    private byte[] codeByte;

    @Override
    public int contentViewID() {
        return R.layout.activity_bg1a;
    }

    @Override
    public void initView() {
        mContext = this;
        Intent intent = getIntent();
        mDeviceMac = intent.getStringExtra("mac");
        mDeviceName = intent.getStringExtra("type");
        mClientCallbackId = iHealthDevicesManager.getInstance().registerClientCallback(miHealthDevicesCallback);
        /* Limited wants to receive notification specified device */
        iHealthDevicesManager.getInstance().addCallbackFilterForDeviceType(mClientCallbackId, iHealthDevicesManager.TYPE_BG1A);
        /* Get bg1s controller */
        mBg1aControl = iHealthDevicesManager.getInstance().getBg1aControl(mDeviceMac);

        // 设置点击监听器
        findViewById(R.id.btnGetDeviceInfo).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnSetTime).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnSetMeasureMode).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnDisconnect).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnSetMeasureMode1).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnGetHistoryData).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnDeleteHistoryData).setOnClickListener(this::onViewClicked);
    }

    private iHealthDevicesCallback miHealthDevicesCallback = new iHealthDevicesCallback() {

        @Override
        public void onDeviceConnectionStateChange(String mac,
                                                  String deviceType, int status, int errorID) {
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
        public void onDeviceNotify(String mac, String deviceType,
                                   String action, String message) {
            Log.i(TAG, "mac: " + mac);
            Log.i(TAG, "deviceType: " + deviceType);
            Log.i(TAG, "action: " + action);
            Log.i(TAG, "message: " + message);

            Message msg = new Message();
            msg.what = HANDLER_MESSAGE;
            msg.obj = message;
            myHandler.sendMessage(msg);

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
        if (mBg1aControl != null) {
            mBg1aControl.disconnect();
        }
        iHealthDevicesManager.getInstance().unRegisterClientCallback(mClientCallbackId);
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
        if (mBg1aControl == null) {
            addLogInfo("mBg1aControl == null");
            return;
        }
        showLogLayout();
        int id = view.getId();
        if (id == R.id.btnGetDeviceInfo) {
            mBg1aControl.getDeviceInfo();
            addLogInfo("getDeviceInfo()");
        } else if (id == R.id.btnSetTime) {
            mBg1aControl.setDeviceTime(System.currentTimeMillis());
            addLogInfo("setDeviceTime()");
        } else if (id == R.id.btnSetMeasureMode) {
            mBg1aControl.setMeasureMode(Bg1aProfile.Bg1aModeType.BloodMode);
            addLogInfo("setMeasureMode() mode 0 ");
        } else if (id == R.id.btnSetMeasureMode1) {
            mBg1aControl.setMeasureMode(Bg1aProfile.Bg1aModeType.CTLMode);
            addLogInfo("setMeasureMode() mode 1");
        } else if (id == R.id.btnGetHistoryData) {
            mBg1aControl.getHistoryData();
            addLogInfo("getHistoryData()");
        } else if (id == R.id.btnDeleteHistoryData) {
            mBg1aControl.deleteHistoryData();
            addLogInfo("deleteHistoryData()");
        } else if (id == R.id.btnDisconnect) {
            mBg1aControl.disconnect();
            addLogInfo("disconnect()");
        }

    }
}
