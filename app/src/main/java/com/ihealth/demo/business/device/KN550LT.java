package com.ihealth.demo.business.device;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.viewbinding.ViewBinding;

import com.ec.easylibrary.dialog.confirm.ConfirmDialog;
import com.ihealth.communication.control.BpProfile;
import com.ihealth.communication.control.Kn550LtControl;
import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.ihealth.communication.utils.Log;
import com.ihealth.demo.R;
import com.ihealth.demo.business.FunctionFoldActivity;
import com.ihealth.demo.databinding.ActivityKn550ltBinding;
import com.ihealth.sdk.command.kn550lt.Kn550LtMessageKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class KN550LT extends FunctionFoldActivity implements View.OnClickListener {
    private Context mContext;
    private static final String TAG = KN550LT.class.getSimpleName();
    private Kn550LtControl mKn550LtControl;
    private int mClientCallbackId;

    @Override
    public int contentViewID() {
        return R.layout.activity_kn550lt;
    }

    @Override
    public void initView() {
        ActivityKn550ltBinding kn550ltBinding = (ActivityKn550ltBinding)binding;
        kn550ltBinding.btnDisconnect.setOnClickListener(this);
        kn550ltBinding.btnGetFunctionInfo.setOnClickListener(this);
        kn550ltBinding.btnGetDataCount.setOnClickListener(this);
        kn550ltBinding.btnGetData.setOnClickListener(this);
        kn550ltBinding.btnDeleteData.setOnClickListener(this);
        kn550ltBinding.btnGetBattery.setOnClickListener(this);
        kn550ltBinding.btnGetIdps.setOnClickListener(this);
        kn550ltBinding.btnGetTime.setOnClickListener(this);


        mContext = this;
        Intent intent = getIntent();
        mDeviceMac = intent.getStringExtra("mac");
        mDeviceName = intent.getStringExtra("type");
        /* register ihealthDevicesCallback id */
        mClientCallbackId = iHealthDevicesManager.getInstance().registerClientCallback(miHealthDevicesCallback);
        /* Limited wants to receive notification specified device */
        iHealthDevicesManager.getInstance().addCallbackFilterForDeviceType(mClientCallbackId, iHealthDevicesManager.TYPE_KN550LT);
        /* Get bp550bt controller */
        mKn550LtControl = iHealthDevicesManager.getInstance().getKn550LtControl(mDeviceMac);
//        setDeviceInfo(mDeviceName, mDeviceMac);
    }

    @Override
    protected ViewBinding inflateBinding(LayoutInflater inflater) {
        return ActivityKn550ltBinding.inflate(inflater);
    }

    private iHealthDevicesCallback miHealthDevicesCallback = new iHealthDevicesCallback() {

        @Override
        public void onDeviceConnectionStateChange(String mac, String deviceType, int status, int errorID) {
            Log.i(TAG, "mac: " + mac);
            Log.i(TAG, "deviceType: " + deviceType);
            Log.i(TAG, "status: " + status);
            if (status == iHealthDevicesManager.DEVICE_STATE_DISCONNECTED) {
//                addLogInfo(mContext.getString(R.string.connect_main_tip_disconnect));
//                ToastUtils.showToast(mContext, mContext.getString(R.string.connect_main_tip_disconnect));
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
            Log.json(TAG, "message: " + message);

            if (BpProfile.ACTION_BATTERY_BP.equals(action)) {
                try {
                    JSONObject info = new JSONObject(message);
                    int battery = info.getInt(BpProfile.BATTERY_CBP);
                    Message msg = new Message();
                    msg.what = HANDLER_MESSAGE;
                    msg.obj = "battery: " + battery;
                    myHandler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else if (Kn550LtMessageKey.ACTION_GET_MEMORY_DATA.equals(action)) {
                String str = "{}";
                try {
                    JSONObject info = new JSONObject(message);
                    if (info.has(BpProfile.MEMORY_DATA)) {
                        JSONArray array = info.getJSONArray(BpProfile.MEMORY_DATA);
                        Log.d(TAG, "--Kn550LtMessageKey.ACTION_GET_MEMORY_DATA--data len:"+array.length());
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String date = obj.getString(Kn550LtMessageKey.MEASUREMENT_DATE_BP);
                            int sys = obj.getInt(Kn550LtMessageKey.HIGH_BLOOD_PRESSURE_BP);
                            int dia = obj.getInt(Kn550LtMessageKey.LOW_BLOOD_PRESSURE_BP);
                            boolean cuff = obj.getBoolean(Kn550LtMessageKey.CUF_LOOSE);
                            int pulseWave = obj.getInt(Kn550LtMessageKey.PULSE_BP);
                            boolean bodyMovement = obj.getBoolean(Kn550LtMessageKey.BODY_MOVEMENT);
                            boolean ahr = obj.getBoolean(Kn550LtMessageKey.HAVE_IHB);
                            int timeFlag = obj.getInt(Kn550LtMessageKey.TIME_FLAG);
                            str = "date:" + date
                                    + "sys:" + sys + "\n"
                                    + "dia:" + dia + "\n"
                                    + "pulseWave" + pulseWave + "\n"
                                    + "bodyMovement" + bodyMovement + "\n"
                                    + "ahr:" + ahr + "\n"
                                    + "cuffLoose:" + cuff + "\n"
                                    + "timeFlag:" + timeFlag + "\n";
                            Message msg = new Message();
                            msg.what = HANDLER_MESSAGE;
                            msg.obj = str;
                            myHandler.sendMessage(msg);
                        }

                    } else {
                        Message msg = new Message();
                        msg.what = HANDLER_MESSAGE;
                        msg.obj = str;
                        myHandler.sendMessage(msg);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
//        if (mBp723Control != null) {
//            mBp723Control.disconnect();
//        }
        iHealthDevicesManager.getInstance().disconnectDevice(mDeviceMac, iHealthDevicesManager.TYPE_KD723);
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

    @Override
    public void onClick(View view) {
        if (mKn550LtControl == null) {
            addLogInfo("Kn550LtControl == null");
            return;
        }
        showLogLayout();
        int id = view.getId();
        if (id == R.id.btnDisconnect) {
            iHealthDevicesManager.getInstance().disconnectDevice(mDeviceMac, iHealthDevicesManager.TYPE_KN550LT);
            addLogInfo("disconnect()");
        } else if (id == R.id.btnGetIdps) {
            mKn550LtControl.getIdps();
            addLogInfo("getIdps()");
        } else if (id == R.id.btn_getFunctionInfo) {
            mKn550LtControl.getFunctionInfo();
            addLogInfo("getFunctionInformation()");
        } else if (id == R.id.btn_getBattery) {
            mKn550LtControl.getBattery();
            addLogInfo("getBattery()");
        } else if (id == R.id.btn_getTime) {
            mKn550LtControl.getTime();
            addLogInfo("getTime()");
        } else if (id == R.id.btnGetDataCount) {
            mKn550LtControl.getMemoryCount();
            addLogInfo("getMemoryCount");
        } else if (id == R.id.btnGetData) {
            mKn550LtControl.getMemoryData();
            addLogInfo("getData() ");
        } else if (id == R.id.btnDeleteData) {
            mKn550LtControl.deleteMemoryData();
            addLogInfo("deleteHistoryData()");
        }
    }
}
