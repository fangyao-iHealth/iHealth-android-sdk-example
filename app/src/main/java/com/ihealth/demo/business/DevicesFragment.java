package com.ihealth.demo.business;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ec.easylibrary.dialog.IOSActionSheetDialog;
import com.ihealth.communication.manager.iHealthDevicesManager;
import com.ihealth.demo.R;
import android.view.View;

import com.ihealth.demo.base.BaseFragment;
import com.ihealth.demo.business.device.BG1;
import com.ihealth.demo.business.device.BPM1;
import com.ihealth.demo.business.device.HS6;
import com.ihealth.demo.databinding.FragmentDevicesBinding;

/**
 * <li>DevicesFragment</li>
 * <li>This Fragment shows all the supported devices,and guide subsequent operations</li>
 * <p>
 * Created by wj on 2018/11/20
 */
public class DevicesFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private FragmentDevicesBinding binding;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Context mContext;
    MainActivity mMainActivity;

    public DevicesFragment() { }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DevicesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DevicesFragment newInstance(String param1, String param2) {
        DevicesFragment fragment = new DevicesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public int contentViewID() {
        return R.layout.fragment_devices;
    }

    @Override
    public void initView() {
        mContext = getActivity();
        mMainActivity = (MainActivity) mContext;
        binding = FragmentDevicesBinding.bind(mRootView);
        
        // 设置所有点击监听器
        binding.llBP5.setOnClickListener(this::onViewClicked);
        binding.llBP5S.setOnClickListener(this::onViewClicked);
        binding.ll550BT.setOnClickListener(this::onViewClicked);
        binding.llBP7S.setOnClickListener(this::onViewClicked);
        binding.llBP3L.setOnClickListener(this::onViewClicked);
        binding.llKD723.setOnClickListener(this::onViewClicked);
        binding.llKD926.setOnClickListener(this::onViewClicked);
        binding.llBPM1.setOnClickListener(this::onViewClicked);
        binding.llBG1.setOnClickListener(this::onViewClicked);
        binding.llBG1S.setOnClickListener(this::onViewClicked);
        binding.llBG5.setOnClickListener(this::onViewClicked);
        binding.llBG1A.setOnClickListener(this::onViewClicked);
        binding.llBG5A.setOnClickListener(this::onViewClicked);
        binding.llBG5S.setOnClickListener(this::onViewClicked);
        binding.llHS2.setOnClickListener(this::onViewClicked);
        binding.llHS2S.setOnClickListener(this::onViewClicked);
        binding.llHS4.setOnClickListener(this::onViewClicked);
        binding.llHS6.setOnClickListener(this::onViewClicked);
        binding.llHS2SPRO.setOnClickListener(this::onViewClicked);
        binding.llAM3.setOnClickListener(this::onViewClicked);
        binding.llAM3S.setOnClickListener(this::onViewClicked);
        binding.llAM4.setOnClickListener(this::onViewClicked);
        binding.llAM6.setOnClickListener(this::onViewClicked);
        binding.llTS28B.setOnClickListener(this::onViewClicked);
        binding.llBTM.setOnClickListener(this::onViewClicked);
        binding.llNt13b.setOnClickListener(this::onViewClicked);
        binding.llPt3sbt.setOnClickListener(this::onViewClicked);
        binding.llPO3M.setOnClickListener(this::onViewClicked);
        binding.llPO1.setOnClickListener(this::onViewClicked);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onViewClicked(View view) {
        String deviceName = "";
        int id = view.getId();
        if (id == R.id.llBP5) {
            deviceName = iHealthDevicesManager.TYPE_BP5;
        } else if (id == R.id.llBP5S) {
            deviceName = iHealthDevicesManager.TYPE_BP5S;
        } else if (id == R.id.ll550BT) {
            deviceName = "KN550BT";
        } else if (id == R.id.llBP7S) {
            deviceName = iHealthDevicesManager.TYPE_BP7S;
        } else if (id == R.id.llBP3L) {
            deviceName = iHealthDevicesManager.TYPE_BP3L;
        } else if (id == R.id.llKD723) {
            deviceName = iHealthDevicesManager.TYPE_KD723;
        } else if (id == R.id.llKD926) {
            deviceName = iHealthDevicesManager.TYPE_KD926;
        } else if (id == R.id.llBPM1) {
            Intent intent3 = new Intent();
            intent3.putExtra("mac", "");
            intent3.putExtra("type", "BPM1");
            intent3.setClass(mContext, BPM1.class);
            startActivity(intent3);
        } else if (id == R.id.llBG1) {
            Intent intent = new Intent();
            intent.putExtra("mac", "");
            intent.putExtra("type", "BG1");
            intent.putExtra("username", "test@com.cn");
            intent.setClass(mContext, BG1.class);
            startActivity(intent);
            return;
        } else if (id == R.id.llBG1S) {
            deviceName = iHealthDevicesManager.TYPE_BG1S;
        } else if (id == R.id.llBG1A) {
            deviceName = iHealthDevicesManager.TYPE_BG1A;
        } else if (id == R.id.llBG5A) {
            deviceName = iHealthDevicesManager.TYPE_BG5A;
        } else if (id == R.id.llBG5) {
            deviceName = iHealthDevicesManager.TYPE_BG5;
        } else if (id == R.id.llBG5S) {
            deviceName = iHealthDevicesManager.TYPE_BG5S;
        } else if (id == R.id.llHS2) {
            deviceName = iHealthDevicesManager.TYPE_HS2;
        } else if (id == R.id.llHS3) {
            deviceName = iHealthDevicesManager.TYPE_HS3;
        } else if (id == R.id.llHS4) {
            deviceName = iHealthDevicesManager.TYPE_HS4;
        } else if (id == R.id.llHS2S) {
            deviceName = iHealthDevicesManager.TYPE_HS2S;
        } else if (id == R.id.llHS2SPRO) {
            deviceName = iHealthDevicesManager.TYPE_HS2SPRO;
        } else if (id == R.id.llHS6) {
            Intent intent2 = new Intent();
            intent2.putExtra("mac", "");
            intent2.putExtra("type", "HS6");
            intent2.setClass(mContext, HS6.class);
            startActivity(intent2);
            return;
        } else if (id == R.id.llAM3) {
            deviceName = iHealthDevicesManager.TYPE_AM3;
        } else if (id == R.id.llAM3S) {
            deviceName = iHealthDevicesManager.TYPE_AM3S;
        } else if (id == R.id.llAM4) {
            deviceName = iHealthDevicesManager.TYPE_AM4;
        } else if (id == R.id.llAM6) {
            deviceName = iHealthDevicesManager.TYPE_AM6;
        } else if (id == R.id.llTS28B) {
            deviceName = iHealthDevicesManager.TYPE_TS28B;
        } else if (id == R.id.llBTM) {
            deviceName = "FDIR-V3";
        } else if (id == R.id.ll_nt13b) {
            deviceName = iHealthDevicesManager.TYPE_NT13B;
        } else if (id == R.id.llPO3M) {
            deviceName = "PO3/PO3M";
        } else if (id == R.id.llPO1) {
            deviceName = "PO1";
        } else if (id == R.id.ll_pt3sbt) {
            deviceName = "PT3SBT";
        }
        mMainActivity.showScanFragment(deviceName, null);
    }

}
