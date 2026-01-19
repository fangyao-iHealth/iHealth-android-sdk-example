package com.ihealth.demo.business.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.ec.easylibrary.dialog.confirm.ConfirmDialog;
import com.ec.easylibrary.utils.ToastUtils;
import com.ec.easylibrary.utils.Utils;
import com.ihealth.communication.control.Hs2sControl;
import com.ihealth.communication.control.Hs2sProfile;
import com.ihealth.communication.control.UpgradeControl;
import com.ihealth.communication.control.UpgradeProfile;
import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesIDPS;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.ihealth.demo.R;
import com.ihealth.demo.business.FunctionFoldActivity;

import org.json.JSONException;
import org.json.JSONObject;


public class HS2S extends FunctionFoldActivity {

    Button mBtnStopUpgrade;
    Button mBtnCheckCloud;
    Button mBtnCheckDevice;
    Button mBtnDownload;
    Button mBtnUpgrade;
    private Context mContext;
    private static final String TAG = "HS2S";
    private Hs2sControl mHs2sControl;
    private int mClientCallbackId;
    private String deviceTimeString = "";

    private String firmwareVersion = "";
    private String hardwareVersion = "";
    private String bleFirmwareVersion;
    private String modelNumber = "";
    private String firmwareVersionCloud = "";

    private String upgradeFile;

    @Override
    public int contentViewID() {
        return R.layout.activity_hs2s;
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
        iHealthDevicesManager.getInstance().addCallbackFilterForDeviceType(mClientCallbackId, iHealthDevicesManager.TYPE_HS2S, iHealthDevicesManager.TYPE_HS2SPRO);
        /* Get hs2s controller */
        mHs2sControl = iHealthDevicesManager.getInstance().getHs2sControl(mDeviceMac);

        // 初始化视图
        mBtnStopUpgrade = findViewById(R.id.btnStopUpgrade);
        mBtnCheckCloud = findViewById(R.id.btnCheckCloud);
        mBtnCheckDevice = findViewById(R.id.btnCheckDevice);
        mBtnDownload = findViewById(R.id.btnDownload);
        mBtnUpgrade = findViewById(R.id.btnUpgrade);
        
        // 设置点击监听器
        findViewById(R.id.btnDisconnect).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnIDPS).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnGetDeviceInfo).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnBattery).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnRestore).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnSetUnit).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnSetUnit2).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnSetUnit3).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnSetUserInfo).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnGetUserInfo).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnDeleteUserInfo).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnSpecifyUserOnline).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnSpecifyUserTourist).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnGetOfflineDataNum).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnGetOfflineData).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnDeleteOfflineData).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnStartHeartRateMode).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnStopHeartRateMode).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnGetAnonymousDataNum).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnGetAnonymousData).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnDeleteAnonymousData).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnCheckDevice).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnCheckCloud).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnDownload).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnUpgrade).setOnClickListener(this::onViewClicked);
        findViewById(R.id.imgStopUpGrade).setOnClickListener(this::onViewClicked);
        findViewById(R.id.btnStopUpgrade).setOnClickListener(this::onViewClicked);
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
            if (UpgradeProfile.ACTION_DEVICE_CLOUD_FIRMWARE_VERSION.equals(action)) {
                msg.obj = "result: " + message;
                try {
                    JSONObject object = new JSONObject(message);
                    firmwareVersionCloud = object.optString(UpgradeProfile.DEVICE_CLOUD_FIRMWARE_VERSION);
                    if (Utils.compareVersion(firmwareVersion, firmwareVersionCloud) < 0) {
                        mBtnDownload.setEnabled(true);
                        addLogInfo("Need to upgrade");
                    } else {
                        mBtnDownload.setEnabled(false);
                        mBtnUpgrade.setEnabled(false);
                        addLogInfo("No need to upgrade");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (UpgradeProfile.ACTION_DEVICE_UP_DOWNLOAD_COMPLETED.equals(action)) {
                msg.obj = "download success";
                mBtnUpgrade.setEnabled(true);
            } else {
                msg.obj = action + "      " + message;
            }
            mHandler.sendMessage(msg);
        }
    };


    Handler mHandler = new Handler() {
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
        if (mHs2sControl != null) {
            mHs2sControl.disconnect();
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
        showLogLayout();
        int id = view.getId();
        if (id == R.id.btnDisconnect) {
            if (mHs2sControl != null) {
                mHs2sControl.disconnect();
                addLogInfo("disconnect()");
            }
        } else if (id == R.id.btnIDPS) {
            if (mHs2sControl != null) {
                String idps = mHs2sControl.getIDPS();
                try {
                    JSONObject idpsObj = new JSONObject(idps);
                    firmwareVersion = idpsObj.optString(iHealthDevicesIDPS.FIRMWAREVERSION);
                    hardwareVersion = idpsObj.optString(iHealthDevicesIDPS.HARDWAREVERSION);
                    bleFirmwareVersion = idpsObj.optString(iHealthDevicesIDPS.BLEFIRMWAREVERSION);
                    modelNumber = idpsObj.optString(iHealthDevicesIDPS.MODENUMBER);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                addLogInfo("getIDPS() -->firmwareVersion:" + firmwareVersion
                        + " hardwareVersion:" + hardwareVersion + " modelNumber:" + modelNumber);
            }
        } else if (id == R.id.btnGetDeviceInfo) {
            if (mHs2sControl != null) {
                mHs2sControl.getDeviceInfo();
                addLogInfo("getDeviceInfo()");
            }

            if (mHs2sControl != null) {
                String idps = mHs2sControl.getIDPS();
                try {
                    JSONObject idpsObj = new JSONObject(idps);
                    firmwareVersion = idpsObj.optString(iHealthDevicesIDPS.FIRMWAREVERSION);
                    hardwareVersion = idpsObj.optString(iHealthDevicesIDPS.HARDWAREVERSION);
                    bleFirmwareVersion = idpsObj.optString(iHealthDevicesIDPS.BLEFIRMWAREVERSION);
                    modelNumber = idpsObj.optString(iHealthDevicesIDPS.MODENUMBER);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                    addLogInfo("getIDPS() -->firmwareVersion:" + firmwareVersion
//                            + " hardwareVersion:" + hardwareVersion + " modelNumber:" + modelNumber);
            }
        } else if (id == R.id.btnBattery) {
            if (mHs2sControl != null) {
                mHs2sControl.getBattery();
                addLogInfo("getBattery()");
            }
        } else if (id == R.id.btnRestore) {
            if (mHs2sControl != null) {
                mHs2sControl.restoreFactorySettings();
                addLogInfo("restoreFactorySettings()");
            }
        } else if (id == R.id.btnSetUnit) {
            if (mHs2sControl != null) {
                mHs2sControl.setUnit(Hs2sProfile.UNIT_KG);
                addLogInfo("setUnit()  UNIT_KG");
            }
        } else if (id == R.id.btnSetUnit2) {
            if (mHs2sControl != null) {
                mHs2sControl.setUnit(Hs2sProfile.UNIT_LB);
                addLogInfo("setUnit()  UNIT_LB");
            }
        } else if (id == R.id.btnSetUnit3) {
            if (mHs2sControl != null) {
                mHs2sControl.setUnit(Hs2sProfile.UNIT_ST);
                addLogInfo("Hs2sProfile()  UNIT_ST");
            }
        } else if (id == R.id.btnSetUserInfo) {
            if (mHs2sControl != null) {
                mHs2sControl.createOrUpdateUserInfo("abcdef1234567890", (float) 71, 1, 28, 176, 1, 0);
                addLogInfo("createOrUpdateUserInfo()");
            }
        } else if (id == R.id.btnGetUserInfo) {
            if (mHs2sControl != null) {
                mHs2sControl.getUserInfo();
                addLogInfo("getUserInfo()");
            }
        } else if (id == R.id.btnDeleteUserInfo) {
            if (mHs2sControl != null) {
                mHs2sControl.deleteUserInfo("abcdef1234567890");
                addLogInfo("deleteUserInfo()");
            }
        } else if (id == R.id.btnSpecifyUserOnline) {
            if (mHs2sControl != null) {
                mHs2sControl.specifyOnlineUsers("abcdef1234567890", (float) 71, 1, 28, 176, 1, 0);
                addLogInfo("specifyOnlineUsers()");
            }
        } else if (id == R.id.btnSpecifyUserTourist) {
            if (mHs2sControl != null) {
                mHs2sControl.specifyTouristUsers();
                addLogInfo("specifyTouristUsers()");
            }
        } else if (id == R.id.btnGetOfflineDataNum) {
            if (mHs2sControl != null) {
                mHs2sControl.getOfflineDataCount("abcdef1234567890");
                addLogInfo("getOfflineDataCount()");
            }
        } else if (id == R.id.btnGetOfflineData) {
            if (mHs2sControl != null) {
                mHs2sControl.getOfflineData("abcdef1234567890");
                addLogInfo("getOfflineData()");
            }
        } else if (id == R.id.btnDeleteOfflineData) {
            if (mHs2sControl != null) {
                mHs2sControl.deleteOfflineData("abcdef1234567890");
                addLogInfo("deleteOfflineData()");
            }
        } else if (id == R.id.btnGetAnonymousDataNum) {
            if (mHs2sControl != null) {
                mHs2sControl.getAnonymousDataCount();
                addLogInfo("getAnonymousDataCount()");
            }
        } else if (id == R.id.btnGetAnonymousData) {
            if (mHs2sControl != null) {
                mHs2sControl.getAnonymousData();
                addLogInfo("getAnonymousData()");
            }
        } else if (id == R.id.btnDeleteAnonymousData) {
            if (mHs2sControl != null) {
                mHs2sControl.deleteAnonymousData();
                addLogInfo("deleteAnonymousData()");
            }
        } else if (id == R.id.btnCheckDevice) {
            String idps = iHealthDevicesManager.getInstance().getDevicesIDPS(mDeviceMac);

            try {
                JSONObject idpsObj = new JSONObject(idps);
                firmwareVersion = idpsObj.getString(iHealthDevicesIDPS.FIRMWAREVERSION);
                hardwareVersion = idpsObj.getString(iHealthDevicesIDPS.HARDWAREVERSION);
                bleFirmwareVersion = idpsObj.getString(iHealthDevicesIDPS.BLEFIRMWAREVERSION);
                modelNumber = idpsObj.getString(iHealthDevicesIDPS.MODENUMBER);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            addLogInfo("queryDeviceFirmwareInfo() -->firmwareVersion:" + firmwareVersion
                    + " hardwareVersion:" + hardwareVersion + " modelNumber:" + modelNumber);
            mBtnCheckCloud.setEnabled(true);
        } else if (id == R.id.btnCheckCloud) {
            UpgradeControl.getInstance().queryDeviceCloudInfo(iHealthDevicesManager.TYPE_HS2S, modelNumber, hardwareVersion, firmwareVersion);
            addLogInfo("queryDeviceCloudInfo() -->firmwareVersion:" + firmwareVersion
                    + " hardwareVersion:" + hardwareVersion + " modelNumber:" + modelNumber);
        } else if (id == R.id.btnDownload) {
            UpgradeControl.getInstance().downloadFirmwareFile(iHealthDevicesManager.TYPE_HS2S, modelNumber, hardwareVersion, firmwareVersionCloud);
            addLogInfo("downloadFirmwareFile() -->firmwareVersionCloud:" + firmwareVersionCloud);
        } else if (id == R.id.btnUpgrade) {
            UpgradeControl.getInstance().startUpgrade(mDeviceMac, iHealthDevicesManager.TYPE_HS2S, modelNumber, hardwareVersion,
                    firmwareVersionCloud, modelNumber + hardwareVersion + firmwareVersionCloud);
            addLogInfo("startUpgrade() -->firmwareVersion:" + firmwareVersion
                    + " hardwareVersion:" + hardwareVersion + " modelNumber:" + modelNumber + " firmwareVersionCloud:" + firmwareVersionCloud);
            mBtnStopUpgrade.setEnabled(true);
        } else if (id == R.id.btnStopUpgrade) {
            UpgradeControl.getInstance().stopUpgrade(mDeviceMac, iHealthDevicesManager.TYPE_HS2S);
            addLogInfo("stopUpgrade() ");
        } else if (id == R.id.btnStartHeartRateMode) {
            if (mHs2sControl != null) {
                mHs2sControl.startHeartRateMode();
                addLogInfo("startHeartRateMode() ");
            }
        } else if (id == R.id.btnStopHeartRateMode) {
            if (mHs2sControl != null) {
                mHs2sControl.setBleLight();
                addLogInfo("setBleLight() ");
            }
        }
    }
}

