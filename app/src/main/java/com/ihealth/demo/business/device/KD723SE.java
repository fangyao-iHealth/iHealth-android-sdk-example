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
import com.ec.easylibrary.utils.Utils;
import com.ihealth.communication.control.BpProfile;
import com.ihealth.communication.control.Kd723seControl;
import com.ihealth.communication.control.NewOtaController;
import com.ihealth.communication.control.OtaCallback;
import com.ihealth.communication.control.OtaDeviceAdapter;
import com.ihealth.communication.control.OtaFileInfo;
import com.ihealth.communication.control.OtaProfile;
import com.ihealth.communication.control.UpgradeControl;
import com.ihealth.communication.control.UpgradeProfile;
import com.ihealth.communication.manager.iHealthDevicesCallback;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.ihealth.communication.utils.Log;
import com.ihealth.demo.R;
import com.ihealth.demo.business.FunctionFoldActivity;
import com.ihealth.demo.databinding.ActivityKd723seBinding;
import com.ihealth.demo.databinding.LayoutNewOtaBinding;
import com.ihealth.sdk.constants.MemoryDataGroupNumberKd723se;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class KD723SE extends FunctionFoldActivity implements View.OnClickListener {
    private Context mContext;
    private static final String TAG = KD723SE.class.getSimpleName();
    private Kd723seControl mKd723seControl;
    private int mClientCallbackId;

    private NewOtaController otaController;
    private LayoutNewOtaBinding otaBinding;

    private String otaModelNumber = "";
    private String otaHardwareVersion = "";
    private String otaFirmwareVersion = "";
    private String otaFirmwareVersionCloud = "";
    private String otaFileCode = "";

    @Override
    public int contentViewID() {
        return R.layout.activity_kd723se;
    }

    @Override
    public void initView() {
        ActivityKd723seBinding kd723seBinding = (ActivityKd723seBinding) binding;
        kd723seBinding.btnDisconnect.setOnClickListener(this);
        kd723seBinding.btnGetFunctionInfo.setOnClickListener(this);
        kd723seBinding.btnGetData.setOnClickListener(this);
        kd723seBinding.btnSetTime.setOnClickListener(this);
        kd723seBinding.btnGetDataCount.setOnClickListener(this);
        kd723seBinding.btnDeleteData.setOnClickListener(this);
        kd723seBinding.btnGetLatestData.setOnClickListener(this);

        otaBinding = kd723seBinding.layoutNewOta;
        otaBinding.btnOtaQueryInfo.setOnClickListener(this);
        otaBinding.btnOtaCheckCloud.setOnClickListener(this);
        otaBinding.btnOtaDownload.setOnClickListener(this);
        otaBinding.btnOtaLoadLocal.setOnClickListener(this);
        otaBinding.btnOtaStart.setOnClickListener(this);
        otaBinding.btnOtaQueryStatus.setOnClickListener(this);
        otaBinding.btnOtaCancel.setOnClickListener(this);

        mContext = this;
        Intent intent = getIntent();
        mDeviceMac = intent.getStringExtra("mac");
        mDeviceName = intent.getStringExtra("type");
        try {
            mClientCallbackId = iHealthDevicesManager.getInstance().registerClientCallback(miHealthDevicesCallback);
            iHealthDevicesManager.getInstance().addCallbackFilterForDeviceType(mClientCallbackId, iHealthDevicesManager.TYPE_KD723SE);
            mKd723seControl = iHealthDevicesManager.getInstance().getKd723seControl(mDeviceMac);
        } catch (Exception e) {
            Log.e(TAG, "SDK init failed, device may not be connected: " + e.getMessage());
        }

        initOtaController();
    }

    @Override
    protected ViewBinding inflateBinding(LayoutInflater inflater) {
        return ActivityKd723seBinding.inflate(inflater);
    }

    // ==================== 新OTA控制器初始化 ====================

    /**
     * 初始化新OTA控制器。
     * 新增设备接入新OTA协议时，只需:
     * 1. 在布局中 include layout_new_ota
     * 2. 复制此方法，将 adapter 中的 Control 调用替换为对应设备的 Control
     * 3. 在 onDeviceNotify 中调用 otaController.handleAction(action, message)
     * 4. 处理 UpgradeProfile 的云端查询/下载回调
     */
    private void initOtaController() {
        OtaDeviceAdapter adapter = new OtaDeviceAdapter() {
            @Override
            public void queryOtaInfo() {
                if (mKd723seControl != null) mKd723seControl.queryOtaInfo();
            }

            @Override
            public void requestOta() {
                if (mKd723seControl != null) mKd723seControl.requestOta();
            }

            @Override
            public void respondFileData(int result, int startAddress, int length, byte[] data) {
                if (mKd723seControl != null) mKd723seControl.respondOtaFileData(result, startAddress, length, data);
            }

            @Override
            public void queryOtaStatus() {
                if (mKd723seControl != null) mKd723seControl.queryOtaStatus();
            }

            @Override
            public void ackOtaStatus() {
                if (mKd723seControl != null) mKd723seControl.ackOtaStatusNotify();
            }

            @Override
            public void sendOtaControl(int instruction, int reason) {
                if (mKd723seControl != null) mKd723seControl.sendOtaControl(instruction, reason);
            }

            @Override
            public void ackOtaControl() {
                if (mKd723seControl != null) mKd723seControl.ackOtaControl();
            }
        };

        OtaCallback otaCallback = new OtaCallback() {
            @Override
            public void onDeviceOtaInfo(String model, String hwVersion, String swVersion) {
                otaModelNumber = model;
                otaHardwareVersion = hwVersion;
                otaFirmwareVersion = swVersion;
                runOnUiThread(() -> {
                    addLogInfo("设备OTA信息 → 型号: " + model
                            + " | HW: " + hwVersion
                            + " | 当前SW: " + swVersion);
                    otaBinding.btnOtaCheckCloud.setEnabled(true);
                });
            }

            @Override
            public void onFileLoaded(OtaFileInfo fileInfo) {
                runOnUiThread(() -> {
                    addLogInfo("固件文件已就绪 → 型号: " + fileInfo.getModel()
                            + " | HW范围: " + fileInfo.getHardwareVersionRange()
                            + " | SW: " + fileInfo.getSoftwareVersion()
                            + " | 总大小: " + fileInfo.getTotalFileSize() + " bytes");
                    otaBinding.btnOtaStart.setEnabled(true);
                });
            }

            @Override
            public void onOtaRequestResult(boolean allowed, int reason) {
                runOnUiThread(() -> {
                    if (allowed) {
                        addLogInfo("设备同意OTA升级，等待固件数据请求...");
                        otaBinding.btnOtaStart.setEnabled(false);
                        otaBinding.btnOtaLoadLocal.setEnabled(false);
                        otaBinding.btnOtaQueryStatus.setEnabled(true);
                        otaBinding.btnOtaCancel.setEnabled(true);
                    } else {
                        String reasonDesc;
                        switch (reason) {
                            case OtaProfile.REASON_LOW_BATTERY: reasonDesc = "电量低"; break;
                            case 0x02: reasonDesc = "设备忙"; break;
                            default: reasonDesc = "未知原因(reason=" + reason + ")"; break;
                        }
                        addLogInfo("设备拒绝OTA升级: " + reasonDesc);
                    }
                });
            }

            @Override
            public void onTransferProgress(int bytesTransferred, int totalBytes) {
                int pct = (totalBytes > 0) ? (int) Math.min(99, (bytesTransferred * 100L / totalBytes)) : 0;
                runOnUiThread(() -> addLogInfo("OTA传输进度: " + pct + "% (" + bytesTransferred + "/" + totalBytes + " bytes)"));
            }

            @Override
            public void onOtaStatusChanged(int status) {
                String desc;
                switch (status) {
                    case OtaProfile.STATUS_TRANSFERRING: desc = "传输中(0x01)"; break;
                    case OtaProfile.STATUS_VERIFYING:   desc = "校验中(0x02)"; break;
                    case OtaProfile.STATUS_COMPLETED:   desc = "完成(0x03)";   break;
                    default: desc = "未知(0x" + Integer.toHexString(status) + ")"; break;
                }
                runOnUiThread(() -> addLogInfo("OTA状态变化: " + desc));
            }

            @Override
            public void onOtaCompleted() {
                runOnUiThread(() -> {
                    resetOtaButtons();
                    addLogInfo("===== OTA升级成功完成 =====");
                });
            }

            @Override
            public void onOtaCancelled(int reason) {
                runOnUiThread(() -> {
                    resetOtaButtons();
                    addLogInfo("OTA已取消, reason=" + reason);
                });
            }

            @Override
            public void onOtaError(String errorMessage) {
                runOnUiThread(() -> {
                    resetOtaButtons();
                    addLogInfo("OTA错误: " + errorMessage);
                });
            }

            @Override
            public void onOtaLog(String log) {
                runOnUiThread(() -> addLogInfo(log));
            }
        };

        otaController = new NewOtaController(adapter, otaCallback);
    }

    private void resetOtaButtons() {
        otaBinding.btnOtaQueryInfo.setEnabled(true);
        otaBinding.btnOtaCheckCloud.setEnabled(false);
        otaBinding.btnOtaDownload.setEnabled(false);
        otaBinding.btnOtaLoadLocal.setEnabled(true);
        otaBinding.btnOtaStart.setEnabled(false);
        otaBinding.btnOtaQueryStatus.setEnabled(false);
        otaBinding.btnOtaCancel.setEnabled(false);
    }

    // ==================== SDK回调 ====================

    private iHealthDevicesCallback miHealthDevicesCallback = new iHealthDevicesCallback() {

        @Override
        public void onDeviceConnectionStateChange(String mac, String deviceType, int status, int errorID) {
            if (status == iHealthDevicesManager.DEVICE_STATE_DISCONNECTED) {
                // 参考iOS逻辑：校验阶段（Verifying）设备重启断连视为升级成功
                if (otaController != null
                        && otaController.getState() == OtaProfile.STATE_VERIFYING) {
                    Log.i(TAG, "OTA: 校验后设备断连，视为升级成功（设备正在重启）");
                    runOnUiThread(() -> {
                        resetOtaButtons();
                        addLogInfo("===== OTA升级成功（设备重启断连）=====");
                    });
                    otaController.reset();
                }
                finish();
            }
        }

        @Override
        public void onUserStatus(String username, int userStatus) { }

        @Override
        public void onDeviceNotify(String mac, String deviceType, String action, String message) {
            Log.i(TAG, "action: " + action);
            Log.json(TAG, "message: " + message);

            // 新OTA协议事件 (0xD0-0xD5)
            if (otaController != null && otaController.handleAction(action, message)) {
                return;
            }

            // 云端版本查询结果
            if (UpgradeProfile.ACTION_DEVICE_CLOUD_FIRMWARE_VERSION.equals(action)) {
                handleCloudVersionResult(message);
                return;
            }

            // 云端固件下载进度
            if (UpgradeProfile.ACTION_DEVICE_UP_DOWNLOADING.equals(action)) {
                try {
                    JSONObject json = new JSONObject(message);
                    int progress = json.optInt(UpgradeProfile.DOWNLOAD_PROGRESS_VALUE, 0);
                    Message msg = new Message();
                    msg.what = HANDLER_MESSAGE;
                    msg.obj = "下载进度: " + progress + "%";
                    myHandler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return;
            }

            // 云端固件下载完成
            if (UpgradeProfile.ACTION_DEVICE_UP_DOWNLOAD_COMPLETED.equals(action)) {
                handleDownloadCompleted(message);
                return;
            }

            // 升级错误
            if (UpgradeProfile.ACTION_DEVICE_ERROR.equals(action)) {
                Message msg = new Message();
                msg.what = HANDLER_MESSAGE;
                msg.obj = "升级错误: " + message;
                myHandler.sendMessage(msg);
                return;
            }

            // 常规设备数据
            if (BpProfile.ACTION_HISTORY_DATA_CBP.equals(action)) {
                try {
                    JSONObject info = new JSONObject(message);
                    if (info.has(BpProfile.HISTORY_DATA_CBP)) {
                        JSONArray array = info.getJSONArray(BpProfile.HISTORY_DATA_CBP);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String str = "date:" + obj.getString(BpProfile.MEASUREMENT_DATE_BP)
                                    + " sys:" + obj.getString(BpProfile.HIGH_BLOOD_PRESSURE_BP)
                                    + " dia:" + obj.getString(BpProfile.LOW_BLOOD_PRESSURE_BP)
                                    + " pulse:" + obj.getString(BpProfile.PULSE_BP)
                                    + " ahr:" + obj.getString(BpProfile.IRREGULAR);
                            Message msg = new Message();
                            msg.what = HANDLER_MESSAGE;
                            msg.obj = str;
                            myHandler.sendMessage(msg);
                        }
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

    private void handleCloudVersionResult(String message) {
        try {
            JSONObject object = new JSONObject(message);
            otaFirmwareVersionCloud = object.optString(UpgradeProfile.DEVICE_CLOUD_FIRMWARE_VERSION, "");
            Message msg = new Message();
            msg.what = HANDLER_MESSAGE;
            if (Utils.compareVersion(otaFirmwareVersion, otaFirmwareVersionCloud) < 0) {
                otaBinding.btnOtaDownload.setEnabled(true);
                msg.obj = "云端有新版本: " + otaFirmwareVersionCloud + " (当前: " + otaFirmwareVersion + ")";
            } else {
                otaBinding.btnOtaDownload.setEnabled(false);
                msg.obj = "固件已是最新版本 (当前: " + otaFirmwareVersion + ", 云端: " + otaFirmwareVersionCloud + ")";
            }
            myHandler.sendMessage(msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleDownloadCompleted(String message) {
        try {
            JSONObject json = new JSONObject(message);
            otaFileCode = json.optString(UpgradeProfile.DOWNLOAD_FILE_CODE, "");
        } catch (JSONException e) {
            otaFileCode = otaModelNumber + otaHardwareVersion + otaFirmwareVersionCloud;
        }
        addLogInfo("固件下载完成, fileCode: " + otaFileCode);

        new Thread(() -> {
            byte[] firmwareData = UpgradeControl.getInstance().readDownloadedFirmwareFile(otaFileCode);
            runOnUiThread(() -> {
                if (firmwareData != null && firmwareData.length > 0) {
                    otaController.loadOtaFile(firmwareData);
                } else {
                    addLogInfo("读取已下载的固件文件失败");
                }
            });
        }).start();
    }

    /**
     * 从 assets 加载本地固件文件（对应 iOS 的本地文件测试流程）。
     * 文件格式与云端下载文件完全相同：12字节Header + 信息区 + 数据区 + 64字节Ed25519签名。
     * OtaFileParser 解析后可取到型号、硬件版本范围、软件版本等元数据。
     */
    private static final String LOCAL_FIRMWARE_ASSET = "OTA_File_For_Test_With_Signature.bin";

    private void loadLocalFirmwareFromAssets() {
        addLogInfo("正在从本地Assets加载固件: " + LOCAL_FIRMWARE_ASSET);
        new Thread(() -> {
            byte[] firmwareData = readAssetFile(LOCAL_FIRMWARE_ASSET);
            runOnUiThread(() -> {
                if (firmwareData == null || firmwareData.length == 0) {
                    addLogInfo("读取本地固件失败，请确认文件存在于 assets/ 目录");
                    return;
                }
                addLogInfo("本地固件加载成功，大小: " + firmwareData.length + " bytes");
                otaController.loadOtaFile(firmwareData);

                // 解析并打印固件元数据（与iOS parseFirmwareInfo对应）
                OtaFileInfo info = otaController.getOtaFileInfo();
                if (info != null) {
                    addLogInfo("固件元数据 → 型号: " + info.getModel()
                            + " | HW范围: " + info.getHardwareVersionRange()
                            + " | SW版本: " + info.getSoftwareVersion()
                            + " | 数据大小: " + info.getDataLength() + " bytes"
                            + " | 签名长度: " + info.getSignature().length + " bytes");
                }
            });
        }).start();
    }

    private byte[] readAssetFile(String assetName) {
        try (InputStream is = getAssets().open(assetName);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "readAssetFile failed: " + e.getMessage());
            return null;
        }
    }

    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == HANDLER_MESSAGE) {
                addLogInfo((String) msg.obj);
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onDestroy() {
        try {
            iHealthDevicesManager.getInstance().disconnectDevice(mDeviceMac, iHealthDevicesManager.TYPE_KD723SE);
            iHealthDevicesManager.getInstance().unRegisterClientCallback(mClientCallbackId);
        } catch (Exception e) {
            Log.e(TAG, "SDK cleanup failed: " + e.getMessage());
        }
        clearLogInfo();
        super.onDestroy();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            if (isShowingLogLayout()) {
                hideLogLayout();
            } else {
                showConfirmDialog(mContext, mContext.getString(R.string.confirm_tip_function_title),
                        mContext.getString(R.string.confirm_tip_function_message, mDeviceName, mDeviceMac),
                        new ConfirmDialog.OnClickLisenter() {
                            @Override
                            public void positiveOnClick() { finish(); }
                            @Override
                            public void nagetiveOnClick() { }
                        });
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        showLogLayout();
        int id = view.getId();

        if (id == R.id.btnDisconnect) {
            iHealthDevicesManager.getInstance().disconnectDevice(mDeviceMac, iHealthDevicesManager.TYPE_KD723SE);
            addLogInfo("disconnect()");
        } else if (id == R.id.btn_getFunctionInfo) {
            if (mKd723seControl != null) { mKd723seControl.getFunctionInfo(); addLogInfo("getFunctionInfo()"); }
        } else if (id == R.id.btnSetTime) {
            if (mKd723seControl != null) { mKd723seControl.setTime(DateFormat.is24HourFormat(mContext)); addLogInfo("setTime()"); }
        } else if (id == R.id.btnGetDataCount) {
            if (mKd723seControl != null) { mKd723seControl.getMemoryCount(MemoryDataGroupNumberKd723se.GROUP_1); addLogInfo("getMemoryCount()"); }
        } else if (id == R.id.btnGetLatestData) {
            if (mKd723seControl != null) { mKd723seControl.getLatestMemoryData(); addLogInfo("getLatestMemoryData()"); }
        } else if (id == R.id.btnGetData) {
            if (mKd723seControl != null) { mKd723seControl.getMemoryData(MemoryDataGroupNumberKd723se.GROUP_1); addLogInfo("getMemoryData()"); }
        } else if (id == R.id.btnDeleteData) {
            if (mKd723seControl != null) { mKd723seControl.deleteMemoryData(MemoryDataGroupNumberKd723se.GROUP_1); addLogInfo("deleteMemoryData()"); }
        }
        // 新OTA协议
        else if (id == R.id.btnOtaQueryInfo) {
            otaController.queryDeviceOtaInfo();
        } else if (id == R.id.btnOtaCheckCloud) {
            UpgradeControl.getInstance().queryDeviceCloudInfo(
                    iHealthDevicesManager.TYPE_KD723SE, otaModelNumber, otaHardwareVersion, otaFirmwareVersion);
            addLogInfo("queryDeviceCloudInfo() model:" + otaModelNumber
                    + " hw:" + otaHardwareVersion + " fw:" + otaFirmwareVersion);
        } else if (id == R.id.btnOtaDownload) {
            UpgradeControl.getInstance().downloadFirmwareFile(
                    iHealthDevicesManager.TYPE_KD723SE, otaModelNumber, otaHardwareVersion, otaFirmwareVersionCloud);
            addLogInfo("downloadFirmwareFile() cloud version:" + otaFirmwareVersionCloud);
        } else if (id == R.id.btnOtaLoadLocal) {
            loadLocalFirmwareFromAssets();
        } else if (id == R.id.btnOtaStart) {
            otaController.requestOta();
        } else if (id == R.id.btnOtaQueryStatus) {
            otaController.queryOtaStatus();
        } else if (id == R.id.btnOtaCancel) {
            otaController.cancelOta(OtaProfile.CANCEL_UNKNOWN);
        }
    }
}
