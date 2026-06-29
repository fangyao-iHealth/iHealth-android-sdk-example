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
import com.ihealth.communication.control.Bp300ClControl;
import com.ihealth.communication.control.BpProfile;
import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.ihealth.communication.utils.Log;
import com.ihealth.demo.R;
import com.ihealth.demo.business.FunctionFoldActivity;
import com.ihealth.demo.databinding.ActivityBp300clBinding;
import com.ihealth.sdk.command.bp300cl.Bp300ClMessageKey;
import com.ihealth.sdk.command.bp300cv.Bp300CvMessageKey;
import com.ihealth.sdk.constants.MemoryDataGroupNumberBp300Cv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class BP300CL extends FunctionFoldActivity implements View.OnClickListener {
    private Context mContext;
    private static final String TAG = BP300CL.class.getSimpleName();
    private Bp300ClControl mBp300ClControl;
    private int mClientCallbackId;

    @Override
    public int contentViewID() {
        return R.layout.activity_bp300cl;
    }

    @Override
    public void initView() {
        ActivityBp300clBinding bp300ClBinding = (ActivityBp300clBinding)binding;
        bp300ClBinding.btnDisconnect.setOnClickListener(this);
        bp300ClBinding.btnGetFunctionInfo.setOnClickListener(this);
        bp300ClBinding.btnGetIdps.setOnClickListener(this);
        bp300ClBinding.btnGetDataCount1.setOnClickListener(this);
        bp300ClBinding.btnGetDataCount2.setOnClickListener(this);
        bp300ClBinding.btnGetData1.setOnClickListener(this);
        bp300ClBinding.btnGetData2.setOnClickListener(this);
        bp300ClBinding.btnGetBattery.setOnClickListener(this);
        bp300ClBinding.btnDeleteData1.setOnClickListener(this);
        bp300ClBinding.btnDeleteData2.setOnClickListener(this);
        bp300ClBinding.btnDeleteDataAll.setOnClickListener(this);
        bp300ClBinding.btnGetLatestData.setOnClickListener(this);


        mContext = this;
        Intent intent = getIntent();
        mDeviceMac = intent.getStringExtra("mac");
        mDeviceName = intent.getStringExtra("type");
        /* register ihealthDevicesCallback id */
        mClientCallbackId = iHealthDevicesManager.getInstance().registerClientCallback(miHealthDevicesCallback);
        /* Limited wants to receive notification specified device */
        iHealthDevicesManager.getInstance().addCallbackFilterForDeviceType(mClientCallbackId, iHealthDevicesManager.TYPE_BP300CL);
        /* Get bp550bt controller */
        mBp300ClControl = iHealthDevicesManager.getInstance().getBp300ClControl(mDeviceMac);
//        setDeviceInfo(mDeviceName, mDeviceMac);
    }

    @Override
    protected ViewBinding inflateBinding(LayoutInflater inflater) {
        return ActivityBp300clBinding.inflate(inflater);
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

            if (BpProfile.ACTION_BATTERY_CBP.equals(action)) {
                try {
                    JSONObject info = new JSONObject(message);
                    int battery = info.getInt(BpProfile.BATTERY_CBP);
                    int batteryStatus = info.getInt(BpProfile.BATTERY_STATUS);
                    Message msg = new Message();
                    msg.what = HANDLER_MESSAGE;
                    msg.obj = "battery: " + battery+ "\n"
                    +"batteryStatus: " + batteryStatus;
                    myHandler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else if (Bp300ClMessageKey.ACTION_GET_MEMORY_DATA.equals(action)) {
                String str = "{}";
                try {
                    JSONObject info = new JSONObject(message);
                    if (info.has(Bp300ClMessageKey.MEMORY_DATA)) {
                        JSONArray array = info.getJSONArray(BpProfile.MEMORY_DATA);
                        Log.d(TAG, "--Bp300ClMessageKey.ACTION_GET_MEMORY_DATA--data len:"+array.length());
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String date = obj.getString(BpProfile.MEASUREMENT_DATE_BP);
                            String sys = obj.getString(BpProfile.HIGH_BLOOD_PRESSURE_BP);
                            String dia = obj.getString(BpProfile.LOW_BLOOD_PRESSURE_BP);
                            String pulseWave = obj.getString(BpProfile.PULSE_BP);
                            String cuff = obj.getString(BpProfile.CUF_LOOSE);
                            String bodyMovement = obj.getString(BpProfile.BODY_MOVEMENT);
                            String timeFlag = obj.getString(Bp300ClMessageKey.TIME_FLAG);
                            String measureFlag = obj.getString(Bp300ClMessageKey.MEASURE_FLAG);
                            str = "date:" + date+ "\n"
                                    + "sys:" + sys + "\n"
                                    + "dia:" + dia + "\n"
                                    + "pulseWave:" + pulseWave + "\n"
                                    + "cuff:" + cuff + "\n"
                                    + "timeFlag:" + timeFlag + "\n"
                                    + "measureFlag:" + measureFlag + "\n"
                                    + "bodyMovement:" + bodyMovement + "\n";
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
        if (mBp300ClControl == null) {
            addLogInfo("mBp926Control == null");
            return;
        }
        showLogLayout();
        int id = view.getId();
        if (id == R.id.btnDisconnect) {
            iHealthDevicesManager.getInstance().disconnectDevice(mDeviceMac, iHealthDevicesManager.TYPE_KD723);
            addLogInfo("disconnect()");
        } else if (id == R.id.btnGetBattery) {
            mBp300ClControl.getBattery();
            addLogInfo("btnGetBattery()");
        } else if (id == R.id.btn_getFunctionInfo) {
            mBp300ClControl.getFunctionInfo();
            addLogInfo("getFunctionInformation()");
        } else if (id == R.id.btnGetIdps) {
            mBp300ClControl.getIdps();
            addLogInfo("getIdps()");
        } else if (id == R.id.btnGetDataCount1) {
            mBp300ClControl.getMemoryCount(MemoryDataGroupNumberBp300Cv.GROUP_1);
            addLogInfo("User1 getMemoryCountWithUserID()");
        } else if (id == R.id.btnGetData1) {
            mBp300ClControl.getMemoryData(MemoryDataGroupNumberBp300Cv.GROUP_1);
            addLogInfo("User1 getData() ");
        }  else if (id == R.id.btnGetData2) {
            mBp300ClControl.getMemoryData(MemoryDataGroupNumberBp300Cv.GROUP_2);
            addLogInfo("User2 getData() ");
        } else if (id == R.id.btnGetLatestData) {
            mBp300ClControl.getLatestMemoryData();
            addLogInfo("getLatestMemoryData()");
        } else if (id == R.id.btnDeleteData1) {
            mBp300ClControl.finishGetMemoryData(MemoryDataGroupNumberBp300Cv.GROUP_1);
            addLogInfo("User1 deleteHistoryData()");
        } else if (id == R.id.btnGetDataCount2) {
            mBp300ClControl.getMemoryCount(MemoryDataGroupNumberBp300Cv.GROUP_2);
            addLogInfo("User2 getMemoryCountWithUserID()");
        } else if (id == R.id.btnDeleteData2) {
            mBp300ClControl.finishGetMemoryData(MemoryDataGroupNumberBp300Cv.GROUP_2);
            addLogInfo("User2 deleteHistoryData()");
        } else if (id == R.id.btnDeleteDataAll) {
            mBp300ClControl.finishGetMemoryData(MemoryDataGroupNumberBp300Cv.GROUP_ALL);
            addLogInfo("All deleteHistoryData()");
        }
    }
}
