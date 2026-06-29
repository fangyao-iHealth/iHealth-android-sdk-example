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
import com.ihealth.communication.control.BpProfile;
import com.ihealth.communication.control.Bp300CvControl;
import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.ihealth.communication.utils.Log;
import com.ihealth.demo.R;
import com.ihealth.demo.business.FunctionFoldActivity;
import com.ihealth.demo.databinding.ActivityBp300cvBinding;
import com.ihealth.sdk.command.bp300cv.Bp300CvMessageKey;
import com.ihealth.sdk.constants.MemoryDataGroupNumber;
import com.ihealth.sdk.constants.MemoryDataGroupNumberBp300Cv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class BP300CV extends FunctionFoldActivity implements View.OnClickListener {
    private Context mContext;
    private static final String TAG = BP300CV.class.getSimpleName();
    private Bp300CvControl mBp300CvControl;
    private int mClientCallbackId;

    @Override
    public int contentViewID() {
        return R.layout.activity_bp300cv;
    }

    @Override
    public void initView() {
        ActivityBp300cvBinding bp300cvBinding = (ActivityBp300cvBinding)binding;
        bp300cvBinding.btnDisconnect.setOnClickListener(this);
        bp300cvBinding.btnGetFunctionInfo.setOnClickListener(this);
        bp300cvBinding.btnGetData1.setOnClickListener(this);
        bp300cvBinding.btnDeleteData1.setOnClickListener(this);
        bp300cvBinding.btnGetDataCount2.setOnClickListener(this);
        bp300cvBinding.btnGetData2.setOnClickListener(this);
        bp300cvBinding.btnDeleteData2.setOnClickListener(this);
        bp300cvBinding.btnGetBattery.setOnClickListener(this);
        bp300cvBinding.btnGetThreeMeasureInterval.setOnClickListener(this);
        bp300cvBinding.btnGetLanAndVol.setOnClickListener(this);
        bp300cvBinding.btnGSetLanAndVol.setOnClickListener(this);
        bp300cvBinding.btnDeleteDataAll.setOnClickListener(this);
        bp300cvBinding.btnGetIdps.setOnClickListener(this);

        mContext = this;
        Intent intent = getIntent();
        mDeviceMac = intent.getStringExtra("mac");
        mDeviceName = intent.getStringExtra("type");
        /* register ihealthDevicesCallback id */
        mClientCallbackId = iHealthDevicesManager.getInstance().registerClientCallback(miHealthDevicesCallback);
        /* Limited wants to receive notification specified device */
        iHealthDevicesManager.getInstance().addCallbackFilterForDeviceType(mClientCallbackId, iHealthDevicesManager.TYPE_BP300CV);
        /* Get bp550bt controller */
        mBp300CvControl = iHealthDevicesManager.getInstance().getBp300CvControl(mDeviceMac);
//        setDeviceInfo(mDeviceName, mDeviceMac);
    }

    @Override
    protected ViewBinding inflateBinding(LayoutInflater inflater) {
        return ActivityBp300cvBinding.inflate(inflater);
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


            } else if (Bp300CvMessageKey.ACTION_GET_MEMORY_DATA.equals(action)) {
                String str = "{}";
                try {
                    JSONObject info = new JSONObject(message);
                    if (info.has(BpProfile.MEMORY_DATA)) {
                        JSONArray array = info.getJSONArray(BpProfile.MEMORY_DATA);
                        Log.d(TAG, "--Bp300CvMessageKey.ACTION_GET_MEMORY_DATA--data len:"+array.length());
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String date = obj.getString(Bp300CvMessageKey.MEASUREMENT_DATE_BP);
                            String sys = obj.getString(Bp300CvMessageKey.HIGH_BLOOD_PRESSURE_BP);
                            String dia = obj.getString(Bp300CvMessageKey.LOW_BLOOD_PRESSURE_BP);
                            String pulseWave = obj.getString(Bp300CvMessageKey.PULSE_BP);
                            String ahr = obj.getString(Bp300CvMessageKey.HAVE_IHB);
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
        if (mBp300CvControl == null) {
            addLogInfo("mBp300CvControl == null");
            return;
        }
        showLogLayout();
        int id = view.getId();
        if (id == R.id.btnDisconnect) {
            iHealthDevicesManager.getInstance().disconnectDevice(mDeviceMac, iHealthDevicesManager.TYPE_BP300CV);
            addLogInfo("disconnect()");
        } else if (id == R.id.btnGetIdps) {
            mBp300CvControl.getIdps();
            addLogInfo("disconnect()");
        } else if (id == R.id.btn_getFunctionInfo) {
            mBp300CvControl.getFunctionInfo();
            addLogInfo("getFunctionInformation()");
        } else if (id == R.id.btn_getBattery) {
            mBp300CvControl.getBattery();
            addLogInfo("getBattery()");
        } else if (id == R.id.btnGetThreeMeasureInterval) {
            mBp300CvControl.getThreeMeasurementInterval();
            addLogInfo("getThreeMeasurementInterval()");
        } else if (id == R.id.btnSetThreeMeasureInterval) {
            mBp300CvControl.setThreeMeasurementInterval(60);
            addLogInfo("setThreeMeasurementInterval()");
        } else if (id == R.id.btnGetLanAndVol) {
            mBp300CvControl.getLanAndVolume();
            addLogInfo("getLanAndVolume()");
        } else if (id == R.id.btnGSetLanAndVol) {
            mBp300CvControl.setLanAndVolume(1, 30);
            addLogInfo("setThreeMeasurementInterval()");
        } else if (id == R.id.btnGetDataCount1) {
            mBp300CvControl.getMemoryCount(MemoryDataGroupNumberBp300Cv.GROUP_1);
            addLogInfo("User1 getMemoryCountWithUserID()");
        } else if (id == R.id.btnGetData1) {
            mBp300CvControl.getMemoryData(MemoryDataGroupNumberBp300Cv.GROUP_1);
            addLogInfo("User1 getData() ");
        } else if (id == R.id.btnDeleteData1) {
            mBp300CvControl.deleteMemoryData(MemoryDataGroupNumberBp300Cv.GROUP_1);
            addLogInfo("User1 deleteHistoryData()");
        } else if (id == R.id.btnGetDataCount2) {
            mBp300CvControl.getMemoryCount(MemoryDataGroupNumberBp300Cv.GROUP_2);
            addLogInfo("User2 getMemoryCountWithUserID()");
        } else if (id == R.id.btnGetData2) {
            mBp300CvControl.getMemoryData(MemoryDataGroupNumberBp300Cv.GROUP_2);
            addLogInfo("User2 getData() ");
        } else if (id == R.id.btnDeleteData2) {
            mBp300CvControl.deleteMemoryData(MemoryDataGroupNumberBp300Cv.GROUP_2);
            addLogInfo("User2 deleteHistoryData()");

//            case R.id.btnGetDataCountAll:
//                mBp300CvControl.getMemoryCount(MemoryDataGroupNumberBp300Cv.GROUP_ALL);
//                addLogInfo("All getMemoryCountWithUserID()");
//                break;
//
//            case R.id.btnGetDataAll:
//                mBp300CvControl.getMemoryData(MemoryDataGroupNumberBp300Cv.GROUP_ALL);
//                addLogInfo("All getData() ");
//                break;
        } else if (id == R.id.btnDeleteDataAll) {
            mBp300CvControl.deleteMemoryData(MemoryDataGroupNumberBp300Cv.GROUP_ALL);
            addLogInfo("All deleteHistoryData()");
        }
    }
}
