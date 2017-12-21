package com.thinkware.florida.ui.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.thinkware.florida.ui.dialog.CorporationDialog;
import com.thinkware.florida.utility.StringUtil;
import com.thinkware.florida.utility.log.LogHelper;
import com.thinkware.florida.R;
import com.thinkware.florida.external.TachoMeterType;
import com.thinkware.florida.scenario.ConfigurationLoader;
import com.thinkware.florida.ui.BaseActivity;
import com.thinkware.florida.ui.MainApplication;
import com.thinkware.florida.ui.TestActivity;
import com.thinkware.florida.ui.view.NumberPadView;

import java.io.File;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigFragment extends BaseFragment {
    View root, modify, close, test;
    TextView txtServiceCode, txtCorpCode, txtCarNumber;
    TextView txtPhone01, txtPhone02, txtPhone03;
    TextView txtAppVer, txtOtaVer, txtOtherVer;
    TextView txtAppServerIP1, txtAppServerIP2, txtAppServerIP3, txtAppServerIP4, txtAppServerPort;
    TextView txtUpdateServerIP1, txtUpdateServerIP2, txtUpdateServerIP3, txtUpdateServerIP4, txtUpdateServerPort;
    TextView txtPST, txtPSD, txtRC, txtRT, txtCVT;
    ToggleButton toggleIsCorp, toggleSaveLog;
    RadioGroup signalGroup;
    NumberPadView numberPad;
    String selectedMeterDevice;

    View.OnClickListener onTextFieldClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view instanceof TextView) {
                view.requestFocus();
                numberPad.setFocusedTextView((TextView) view);
            }
        }
    };

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            if (b && view instanceof TextView) {
                numberPad.setFocusedTextView((TextView) view);
            }
        }
    };


    public ConfigFragment() {
        // Required empty public constructor
    }

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_config, container, false);

        initViews();
        loadConfig();
        loadApkVersion();

        return root;
    }

    private void initViews() {
        modify = root.findViewById(R.id.btn_modify);
        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveConfig();
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }
        });

        close = root.findViewById(R.id.btn_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        });

        root.findViewById(R.id.btn_corporation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CorporationDialog dialog = new CorporationDialog(getActivity(), new CorporationDialog.ICorporationDialogListener() {
                    @Override
                    public void onSelectedType(int type) {
	                    switch (type) {
		                    case 1:  // 개인
			                    toggleIsCorp.setChecked(false);
			                    txtCorpCode.setText("1");
			                    txtAppServerIP1.setText("58");
			                    txtAppServerIP2.setText("180");
			                    txtAppServerIP3.setText("28");
			                    txtAppServerIP4.setText("213");
			                    txtAppServerPort.setText("3000");
			                    txtUpdateServerIP1.setText("58");
			                    txtUpdateServerIP2.setText("180");
			                    txtUpdateServerIP3.setText("28");
			                    txtUpdateServerIP4.setText("213");
			                    txtUpdateServerPort.setText("3060");
			                    break;
		                    case 2:  // 법인
			                    toggleIsCorp.setChecked(true);
			                    txtCorpCode.setText("11");
			                    txtAppServerIP1.setText("58");
			                    txtAppServerIP2.setText("180");
			                    txtAppServerIP3.setText("28");
			                    txtAppServerIP4.setText("207");
			                    txtAppServerPort.setText("3000");
			                    txtUpdateServerIP1.setText("58");
			                    txtUpdateServerIP2.setText("180");
			                    txtUpdateServerIP3.setText("28");
			                    txtUpdateServerIP4.setText("207");
			                    txtUpdateServerPort.setText("3060");
			                    break;
		                    case 3:  // 순천
			                    toggleIsCorp.setChecked(false);
			                    txtServiceCode.setText("7");
			                    txtCorpCode.setText("1");
			                    txtAppServerIP1.setText("218");
			                    txtAppServerIP2.setText("149");
			                    txtAppServerIP3.setText("86");
			                    txtAppServerIP4.setText("162");
			                    txtAppServerPort.setText("3000");
			                    txtUpdateServerIP1.setText("218");
			                    txtUpdateServerIP2.setText("149");
			                    txtUpdateServerIP3.setText("86");
			                    txtUpdateServerIP4.setText("162");
			                    txtUpdateServerPort.setText("3060");
			                    break;
		                    case 4:  // 하남 개인
			                    toggleIsCorp.setChecked(false);
			                    txtServiceCode.setText("11");
			                    txtCorpCode.setText("1");
			                    txtAppServerIP1.setText("58");
			                    txtAppServerIP2.setText("180");
			                    txtAppServerIP3.setText("28");
			                    txtAppServerIP4.setText("208");
			                    txtAppServerPort.setText("3000");
			                    txtUpdateServerIP1.setText("58");
			                    txtUpdateServerIP2.setText("180");
			                    txtUpdateServerIP3.setText("28");
			                    txtUpdateServerIP4.setText("208");
			                    txtUpdateServerPort.setText("3060");
			                    break;
		                    case 5:  // 하남 법인
			                    toggleIsCorp.setChecked(true);
			                    txtServiceCode.setText("12");
			                    txtCorpCode.setText("11");
			                    txtAppServerIP1.setText("58");
			                    txtAppServerIP2.setText("180");
			                    txtAppServerIP3.setText("28");
			                    txtAppServerIP4.setText("215");
			                    txtAppServerPort.setText("3000");
			                    txtUpdateServerIP1.setText("58");
			                    txtUpdateServerIP2.setText("180");
			                    txtUpdateServerIP3.setText("28");
			                    txtUpdateServerIP4.setText("215");
			                    txtUpdateServerPort.setText("3060");
			                    break;
	                    }
                    }
                });
                dialog.show();
            }
        });

        test = root.findViewById(R.id.btn_test);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), TestActivity.class));
            }
        });

        txtPhone01 = (TextView) root.findViewById(R.id.txt_phone_01);
        txtPhone02 = (TextView) root.findViewById(R.id.txt_phone_02);
        txtPhone03 = (TextView) root.findViewById(R.id.txt_phone_03);
        txtPhone01.setOnClickListener(onTextFieldClick);
        txtPhone02.setOnClickListener(onTextFieldClick);
        txtPhone03.setOnClickListener(onTextFieldClick);
        txtPhone01.setOnFocusChangeListener(onFocusChangeListener);
        txtPhone02.setOnFocusChangeListener(onFocusChangeListener);
        txtPhone03.setOnFocusChangeListener(onFocusChangeListener);

        txtServiceCode = (TextView) root.findViewById(R.id.txt_service_code);
        txtCorpCode = (TextView) root.findViewById(R.id.txt_corp_code);
        txtCarNumber = (TextView) root.findViewById(R.id.txt_car_number);
        txtServiceCode.setOnClickListener(onTextFieldClick);
        txtCorpCode.setOnClickListener(onTextFieldClick);
        txtCarNumber.setOnClickListener(onTextFieldClick);
        txtServiceCode.setOnFocusChangeListener(onFocusChangeListener);
        txtCorpCode.setOnFocusChangeListener(onFocusChangeListener);
        txtCarNumber.setOnFocusChangeListener(onFocusChangeListener);

        txtAppVer = (TextView) root.findViewById(R.id.txt_app_ver);
        txtOtaVer = (TextView) root.findViewById(R.id.txt_ota_ver);
        txtOtherVer = (TextView) root.findViewById(R.id.txt_other_ver);

        txtAppServerIP1 = (TextView) root.findViewById(R.id.txt_appip_1);
        txtAppServerIP2 = (TextView) root.findViewById(R.id.txt_appip_2);
        txtAppServerIP3 = (TextView) root.findViewById(R.id.txt_appip_3);
        txtAppServerIP4 = (TextView) root.findViewById(R.id.txt_appip_4);
        txtAppServerPort = (TextView) root.findViewById(R.id.txt_app_port);
        txtAppServerIP1.setOnClickListener(onTextFieldClick);
        txtAppServerIP2.setOnClickListener(onTextFieldClick);
        txtAppServerIP3.setOnClickListener(onTextFieldClick);
        txtAppServerIP4.setOnClickListener(onTextFieldClick);
        txtAppServerPort.setOnClickListener(onTextFieldClick);
        txtAppServerIP1.setOnFocusChangeListener(onFocusChangeListener);
        txtAppServerIP2.setOnFocusChangeListener(onFocusChangeListener);
        txtAppServerIP3.setOnFocusChangeListener(onFocusChangeListener);
        txtAppServerIP4.setOnFocusChangeListener(onFocusChangeListener);
        txtAppServerPort.setOnFocusChangeListener(onFocusChangeListener);

        txtUpdateServerIP1 = (TextView) root.findViewById(R.id.txt_updateip_1);
        txtUpdateServerIP2 = (TextView) root.findViewById(R.id.txt_updateip_2);
        txtUpdateServerIP3 = (TextView) root.findViewById(R.id.txt_updateip_3);
        txtUpdateServerIP4 = (TextView) root.findViewById(R.id.txt_updateip_4);
        txtUpdateServerPort = (TextView) root.findViewById(R.id.txt_update_port);
        txtUpdateServerIP1.setOnClickListener(onTextFieldClick);
        txtUpdateServerIP2.setOnClickListener(onTextFieldClick);
        txtUpdateServerIP3.setOnClickListener(onTextFieldClick);
        txtUpdateServerIP4.setOnClickListener(onTextFieldClick);
        txtUpdateServerPort.setOnClickListener(onTextFieldClick);
        txtUpdateServerIP1.setOnFocusChangeListener(onFocusChangeListener);
        txtUpdateServerIP2.setOnFocusChangeListener(onFocusChangeListener);
        txtUpdateServerIP3.setOnFocusChangeListener(onFocusChangeListener);
        txtUpdateServerIP4.setOnFocusChangeListener(onFocusChangeListener);
        txtUpdateServerPort.setOnFocusChangeListener(onFocusChangeListener);

        txtPST = (TextView) root.findViewById(R.id.txt_pst);
        txtPSD = (TextView) root.findViewById(R.id.txt_psd);
        txtRC = (TextView) root.findViewById(R.id.txt_rc);
        txtRT = (TextView) root.findViewById(R.id.txt_rt);
        txtCVT = (TextView) root.findViewById(R.id.txt_cvt);
        txtPST.setOnClickListener(onTextFieldClick);
        txtPSD.setOnClickListener(onTextFieldClick);
        txtRC.setOnClickListener(onTextFieldClick);
        txtRT.setOnClickListener(onTextFieldClick);
        txtCVT.setOnClickListener(onTextFieldClick);
        txtPST.setOnFocusChangeListener(onFocusChangeListener);
        txtPSD.setOnFocusChangeListener(onFocusChangeListener);
        txtRC.setOnFocusChangeListener(onFocusChangeListener);
        txtRT.setOnFocusChangeListener(onFocusChangeListener);
        txtCVT.setOnFocusChangeListener(onFocusChangeListener);

        toggleIsCorp = (ToggleButton) root.findViewById(R.id.btn_iscorp);
        toggleSaveLog = (ToggleButton) root.findViewById(R.id.btn_savelog);

        signalGroup = (RadioGroup) root.findViewById(R.id.radiogroup_signal);

        View radioMeter = root.findViewById(R.id.radio_meter);
        radioMeter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (signalGroup.getCheckedRadioButtonId() == R.id.radio_meter) {
                    showMeterListDialog();
                }
            }
        });

        numberPad = (NumberPadView) root.findViewById(R.id.numberpad);
    }

    private void loadConfig() {
        ConfigurationLoader cfgLoader = getCfgLoader();
        txtServiceCode.setText(cfgLoader.getServiceNumber() + "");
        txtCorpCode.setText(cfgLoader.getCorportaionCode() + "");
        txtCarNumber.setText(cfgLoader.getCarId() + "");

        String number = cfgLoader.getDriverPhoneNumber();
        if (!cfgLoader.isCorporation()) {
            if (number != null) {
                String[] phoneNumbers = StringUtil.getDividedPhonenumber(cfgLoader.getDriverPhoneNumber());
                txtPhone01.setText(phoneNumbers[0]);
                txtPhone02.setText(phoneNumbers[1]);
                txtPhone03.setText(phoneNumbers[2]);
            }
        }


        String callServer = cfgLoader.getCallServerIp();
        if (callServer != null) {
            String[] callServerIp = StringUtil.getDividedIP(cfgLoader.getCallServerIp());
            if (callServerIp.length == 4) {
                txtAppServerIP1.setText(callServerIp[0]);
                txtAppServerIP2.setText(callServerIp[1]);
                txtAppServerIP3.setText(callServerIp[2]);
                txtAppServerIP4.setText(callServerIp[3]);
            }
        }
        txtAppServerPort.setText(cfgLoader.getCallServerPort() + "");

        String updateServer = cfgLoader.getUpdateServerIp();
        if (updateServer != null) {
            String[] updateServerIp = StringUtil.getDividedIP(cfgLoader.getUpdateServerIp());
            if (updateServerIp.length == 4) {
                txtUpdateServerIP1.setText(updateServerIp[0]);
                txtUpdateServerIP2.setText(updateServerIp[1]);
                txtUpdateServerIP3.setText(updateServerIp[2]);
                txtUpdateServerIP4.setText(updateServerIp[3]);
            }
        }
        txtUpdateServerPort.setText(cfgLoader.getUpdateServerPort() + "");

        txtPST.setText(cfgLoader.getPst() + "");
        txtPSD.setText(cfgLoader.getPsd() + "");
        txtRC.setText(cfgLoader.getRc() + "");
        txtRT.setText(cfgLoader.getRt() + "");
        txtCVT.setText(cfgLoader.getCvt() + "");

        toggleIsCorp.setChecked(cfgLoader.isCorporation());
        toggleSaveLog.setChecked(cfgLoader.isLs());

        if (cfgLoader.isVacancyLight()) {
            signalGroup.check(R.id.radio_light);
        } else {
            signalGroup.check(R.id.radio_meter);
        }

        selectedMeterDevice = cfgLoader.getMeterDeviceType();

    }

    private void showMeterListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Holo_Dialog_NoActionBar_MinWidth);

        final List<String> tachoMeterList = TachoMeterType.getTachoMeterList();
        String[] meterList = tachoMeterList.toArray(new String[tachoMeterList.size()]);
        int index = 0;
        if (selectedMeterDevice != null) {
            for (String meter : tachoMeterList) {
                if (selectedMeterDevice.equals(meter)) {
                    index = tachoMeterList.indexOf(selectedMeterDevice);
                }
            }
        }
        builder.setSingleChoiceItems(meterList, index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((BaseActivity) getActivity()).removeStatusBar();
                selectedMeterDevice = tachoMeterList.get(i);
                dialogInterface.dismiss();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


    private void loadApkVersion() {
        String appVer = null, otaVer = null, otherVer = null;

        try {
            PackageInfo i = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            appVer = i.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        try {
            PackageInfo i = getActivity().getPackageManager().getPackageInfo("com.thinkware.florida.otaupdater", 0);
            otaVer = i.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        try {
            PackageInfo i = getActivity().getPackageManager().getPackageInfo("com.ntis.dongbunts.bstraffic", 0);
            otherVer = i.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }

        txtAppVer.setText(getString(R.string.app_version, appVer));
        txtOtaVer.setText(getString(R.string.ota_version, otaVer));
        txtOtherVer.setText(getString(R.string.other_version, otherVer));
    }

    private void saveConfig() {
        ConfigurationLoader cfgLoader = getCfgLoader();
        cfgLoader.setServiceNumber(Integer.parseInt(txtServiceCode.getText().toString()));
        cfgLoader.setCorportaionCode(Integer.parseInt(txtCorpCode.getText().toString()));
        cfgLoader.setCarId(Integer.parseInt(txtCarNumber.getText().toString()));

        String phoneNumber = txtPhone01.getText().toString() + txtPhone02.getText() + txtPhone03.getText();
        cfgLoader.setDriverPhoneNumber(phoneNumber);

        String callServerIp = txtAppServerIP1.getText() + "." + txtAppServerIP2.getText() + "." + txtAppServerIP3.getText() + "." + txtAppServerIP4.getText();
        cfgLoader.setCallServerIp(callServerIp);
        cfgLoader.setCallServerPort(Integer.parseInt(txtAppServerPort.getText().toString()));

        String updateServerIp = txtUpdateServerIP1.getText() + "." + txtUpdateServerIP2.getText() + "." + txtUpdateServerIP3.getText() + "." + txtUpdateServerIP4.getText();
        cfgLoader.setUpdateServerIp(updateServerIp);
        cfgLoader.setUpdateServerPort(Integer.parseInt(txtUpdateServerPort.getText().toString()));

        cfgLoader.setPst(Integer.parseInt(txtPST.getText().toString()));
        cfgLoader.setPsd(Integer.parseInt(txtPSD.getText().toString()));
        cfgLoader.setRc(Integer.parseInt(txtRC.getText().toString()));
        cfgLoader.setRt(Integer.parseInt(txtRT.getText().toString()));
        cfgLoader.setCvt(Integer.parseInt(txtCVT.getText().toString()));

        cfgLoader.setCorporation(toggleIsCorp.isChecked());
        boolean isLogging = cfgLoader.isLs();
        cfgLoader.setLs(toggleSaveLog.isChecked());

        int resId = signalGroup.getCheckedRadioButtonId();
        if (resId == R.id.radio_light) {
            cfgLoader.setVacancyLight(true);
            cfgLoader.setMeterDeviceType(null);
        } else {
            cfgLoader.setVacancyLight(false);
            cfgLoader.setMeterDeviceType(selectedMeterDevice);
        }

        cfgLoader.save();

        if (isLogging != cfgLoader.isLs()) {
            if (cfgLoader.isLs()) {
                String rootPath = ((MainApplication) getActivity().getApplication()).getInternalDir();
                File directory = new File(rootPath);
                if (directory == null || !directory.exists()) {
                    directory.mkdirs();
                }
                LogHelper.enableWriteLogFile(directory.getAbsolutePath());
            } else {
                LogHelper.disableWriteLogFile();
            }
        }
    }
}
