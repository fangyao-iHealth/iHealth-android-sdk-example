package com.ihealth.demo.business.device;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.viewbinding.ViewBinding;

import com.ec.easylibrary.dialog.confirm.ConfirmDialog;
import com.ec.easylibrary.utils.ToastUtils;
import com.ihealth.communication.control.Kd5811btControl;
import com.ihealth.communication.control.BpProfile;
import com.ihealth.communication.control.Kd5811btProfile;
import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.ihealth.communication.utils.Log;
import com.ihealth.demo.R;
import com.ihealth.demo.business.FunctionFoldActivity;
import com.ihealth.demo.databinding.ActivityKd5811Binding;
import com.ihealth.sdk.constants.MemoryDataGroupNumber;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class KD5811BT extends FunctionFoldActivity implements View.OnClickListener {
    private Context mContext;
    private static final String TAG = KD5811BT.class.getSimpleName();
    private Kd5811btControl mKd5811BtControl;
    private int mClientCallbackId;

    @Override
    public int contentViewID() {
        return R.layout.activity_kd5811;
    }

    @Override
    public void initView() {
        ActivityKd5811Binding kd5811Binding = (ActivityKd5811Binding)binding;
        kd5811Binding.btnDisconnect.setOnClickListener(this);
        kd5811Binding.btnGetFunctionInfo.setOnClickListener(this);
        kd5811Binding.btnSetTime.setOnClickListener(this);
        kd5811Binding.btnGetDataCount1.setOnClickListener(this);
        kd5811Binding.btnGetDataCount2.setOnClickListener(this);
        kd5811Binding.btnGetData1.setOnClickListener(this);
        kd5811Binding.btnGetData2.setOnClickListener(this);
        kd5811Binding.btnDeleteData1.setOnClickListener(this);
        kd5811Binding.btnDeleteData2.setOnClickListener(this);
        kd5811Binding.btnGetDataAll.setOnClickListener(this);
        kd5811Binding.btnGetDataCountAll.setOnClickListener(this);
        kd5811Binding.btnDeleteDataAll.setOnClickListener(this);


        mContext = this;
        Intent intent = getIntent();
        mDeviceMac = intent.getStringExtra("mac");
        mDeviceName = intent.getStringExtra("type");
        /* register ihealthDevicesCallback id */
        mClientCallbackId = iHealthDevicesManager.getInstance().registerClientCallback(miHealthDevicesCallback);
        /* Limited wants to receive notification specified device */
        iHealthDevicesManager.getInstance().addCallbackFilterForDeviceType(mClientCallbackId, iHealthDevicesManager.TYPE_KD5811BT);
        /* Get bp550bt controller */
        mKd5811BtControl = iHealthDevicesManager.getInstance().getKd5811BtControl(mDeviceMac);
//        setDeviceInfo(mDeviceName, mDeviceMac);
    }

    @Override
    protected ViewBinding inflateBinding(LayoutInflater inflater) {
        return ActivityKd5811Binding.inflate(inflater);
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
            Log.json(TAG, "message: " + message);

            if (BpProfile.ACTION_BATTERY_CBP.equals(action)) {
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


            } else if (Kd5811btProfile.ACTION_GET_MEMORY_DATA.equals(action)) {
                String str = "{}";
                try {
                    JSONObject info = new JSONObject(message);
                    if (info.has(Kd5811btProfile.ACTION_GET_MEMORY_DATA)) {
                        JSONArray array = info.getJSONArray(BpProfile.MEMORY_DATA);
                        Log.d(TAG, "--Kd5811btProfile.ACTION_GET_MEMORY_DATA--data len:"+array.length());
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String date = obj.getString(BpProfile.MEASUREMENT_DATE_BP);
                            String sys = obj.getString(BpProfile.HIGH_BLOOD_PRESSURE_BP);
                            String dia = obj.getString(BpProfile.LOW_BLOOD_PRESSURE_BP);
                            String pulseWave = obj.getString(BpProfile.PULSE_BP);
                            String ahr = obj.getString(BpProfile.IRREGULAR);
                            str = "date:" + date
                                    + "sys:" + sys + "\n"
                                    + "dia:" + dia + "\n"
                                    + "pulseWave" + pulseWave + "\n"
                                    + "ahr:" + ahr + "\n";
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
        if (mKd5811BtControl == null) {
            addLogInfo("mBp926Control == null");
            return;
        }
        showLogLayout();
        int id = view.getId();
        if (id == R.id.btnDisconnect) {
            iHealthDevicesManager.getInstance().disconnectDevice(mDeviceMac, iHealthDevicesManager.TYPE_KD723);
            addLogInfo("disconnect()");
        } else if (id == R.id.btn_getFunctionInfo) {
            mKd5811BtControl.getFunctionInfo();
            addLogInfo("getFunctionInformation()");
        } else if (id == R.id.btnSetTime) {
            mKd5811BtControl.setTime(DateFormat.is24HourFormat(mContext));
            addLogInfo("setTime()");
        } else if (id == R.id.btnGetDataCount1) {
            mKd5811BtControl.getMemoryCount(MemoryDataGroupNumber.GROUP_1);
            addLogInfo("User1 getMemoryCountWithUserID()");
        } else if (id == R.id.btnGetData1) {
            mKd5811BtControl.getMemoryData(MemoryDataGroupNumber.GROUP_1);
            addLogInfo("User1 getData() ");
        } else if (id == R.id.btnDeleteData1) {
            mKd5811BtControl.deleteMemoryData(MemoryDataGroupNumber.GROUP_1);
            addLogInfo("User1 deleteHistoryData()");
        } else if (id == R.id.btnGetDataCount2) {
            mKd5811BtControl.getMemoryCount(MemoryDataGroupNumber.GROUP_2);
            addLogInfo("User2 getMemoryCountWithUserID()");
        } else if (id == R.id.btnGetData2) {
            mKd5811BtControl.getMemoryData(MemoryDataGroupNumber.GROUP_2);
            addLogInfo("User2 getData() ");
        } else if (id == R.id.btnDeleteData2) {
            mKd5811BtControl.deleteMemoryData(MemoryDataGroupNumber.GROUP_2);
            addLogInfo("User2 deleteHistoryData()");
        } else if (id == R.id.btnGetDataCountAll) {
            mKd5811BtControl.getMemoryCount(MemoryDataGroupNumber.GROUP_ALL);
            addLogInfo("All getMemoryCountWithUserID()");
        } else if (id == R.id.btnGetDataAll) {
            mKd5811BtControl.getMemoryData(MemoryDataGroupNumber.GROUP_ALL);
            addLogInfo("All getData() ");
        } else if (id == R.id.btnDeleteDataAll) {
            mKd5811BtControl.deleteMemoryData(MemoryDataGroupNumber.GROUP_ALL);
            addLogInfo("All deleteHistoryData()");
        }
    }
}
