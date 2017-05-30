package com.thinkware.florida.ui.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thinkware.florida.R;
import com.thinkware.florida.media.WavResourcePlayer;
import com.thinkware.florida.network.manager.NetworkManager;
import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.scenario.INaviExecutor;
import com.thinkware.florida.scenario.PreferenceUtil;
import com.thinkware.florida.ui.MainActivity;
import com.thinkware.florida.ui.dialog.SingleLineDialog;
import com.thinkware.florida.utility.log.LogHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class ServiceStatusFragment extends BaseFragment {

    //---------------------------------------------------------------------------------------------
    // fields
    //---------------------------------------------------------------------------------------------
    public static final int STATUS_NONE = 0;
    public static final int STATUS_ERROR_MESSAGE = 1;
    public static final int STATUS_CERTIFICATION = 2;
    public static final int STATUS_CERTIFICATION_AFTER_MODEM_INIT = 3;

    public static final int MSG_WAITING_INIT_MODEM = 1;
    public static final int MSG_WAITING_GET_TO_MODEM_NUMBER = 2;
    public static final int MSG_REQUEST_CERTIFICATION = 3;
    public static final int MSG_BACK_TO_MANAGEMENT = 100;

    View root;
    TextView txtStatus, txtPhoneNumber, btnRest;
    int modemInitializeCount;

    //---------------------------------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------------------------------
    public ServiceStatusFragment() {
        // Required empty public constructor
    }

    public static ServiceStatusFragment newInstance(String message) {
        return newInstance(STATUS_ERROR_MESSAGE, null, message);
    }

    public static ServiceStatusFragment newInstance(int status, String phoneNumber, String message) {
        Bundle b = new Bundle();
        b.putInt("Status", status);
        if (!TextUtils.isEmpty(phoneNumber)) {
            b.putString("PhoneNumber", phoneNumber);
        }
        b.putString("Message", message);
        ServiceStatusFragment f = new ServiceStatusFragment();
        f.setArguments(b);
        return f;
    }

    //---------------------------------------------------------------------------------------------
    // life-cycle
    //---------------------------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_service_status, container, false);

        btnRest = (TextView) root.findViewById(R.id.btn_rest);
        btnRest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getScenarioService().getBoardType() == Packets.BoardType.Boarding
                        || PreferenceUtil.getWaitOrderInfo(getActivity()) != null
                        || PreferenceUtil.getNormalCallInfo(getActivity()) != null) {
                    SingleLineDialog dialog = new SingleLineDialog(
                            getActivity(),
                            getString(R.string.done),
                            getString(R.string.boarding_or_reservation));
                    dialog.show();
                } else {
                    toggleRest();
                }
            }
        });

        txtPhoneNumber = (TextView) root.findViewById(R.id.txt_phonenumber);
        txtStatus = (TextView) root.findViewById(R.id.txt_status);

        initialize();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        delayHandler.removeMessages(MSG_WAITING_INIT_MODEM);
        delayHandler.removeMessages(MSG_WAITING_GET_TO_MODEM_NUMBER);
        delayHandler.removeMessages(MSG_REQUEST_CERTIFICATION);
        delayHandler.removeMessages(MSG_BACK_TO_MANAGEMENT);
    }

    //---------------------------------------------------------------------------------------------
    // private
    //---------------------------------------------------------------------------------------------
    private void initialize() {
        int status = STATUS_NONE;
        String message = "";
        String phoneNumber = "";
        if (getArguments() != null) {
            status = getArguments().getInt("Status");
            message = getArguments().getString("Message");
            phoneNumber = getArguments().getString("PhoneNumber");
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            txtPhoneNumber.setText(getCfgLoader().getDriverPhoneNumber());
        } else {
            txtPhoneNumber.setText(phoneNumber);
        }

        if (status != STATUS_NONE) {
            btnRest.setVisibility(View.INVISIBLE);

            if (status == STATUS_ERROR_MESSAGE) { // 에러 메시지
                showErrorMessage(message);
            } else if (status == STATUS_CERTIFICATION) { // 사용자 인증 요청
                WavResourcePlayer.getInstance(getActivity()).play(R.raw.voice_101);
                // 네트워크가 연결된 상태이지만 모뎀 전화번호를 못 가져오는 케이스가 종종 있다.
                // 전화번호가 없는 경우 한번더 모뎀에 전화번호를 요청해 보고 서비스 인증 요청을 한다.
                if (TextUtils.isEmpty(((MainActivity) getActivity()).getModemNumber())) {
                    ((MainActivity) getActivity()).requestModemNumber();
                    txtStatus.setText(getString(R.string.request_certify) + ".. (0)");
                    // 모뎀 전화번호 가져오는 시간을 고려하여 delay를 준다.
                    delayHandler.sendEmptyMessageDelayed(MSG_WAITING_GET_TO_MODEM_NUMBER, 1000);
                } else {
                    txtStatus.setText(message);
                    getScenarioService().requestServicePacket(txtPhoneNumber.getText().toString(), true);
                }
            } else if (status == STATUS_CERTIFICATION_AFTER_MODEM_INIT) { // 모뎀 초기화 후 인증 요청
                modemInitializeCount = 0;
                LogHelper.write("#### 네트워크 연결 안됨 -> Waiting : " + modemInitializeCount);
                txtStatus.setText(getString(R.string.initialized_modem) + " (" + modemInitializeCount + ")");
                WavResourcePlayer.getInstance(getActivity()).play(R.raw.voice_101);
                delayHandler.sendEmptyMessageDelayed(MSG_WAITING_INIT_MODEM, 1000);
            }
        } else {
            if (getScenarioService() != null
                    && getScenarioService().getRestType() == Packets.RestType.Rest) {
                txtStatus.setText(R.string.success_rest);
                btnRest.setText(R.string.drive);
            } else {
                txtStatus.setText(R.string.certify_completed);
                btnRest.setText(R.string.rest);
            }
        }
    }

    private void toggleRest() {
        if (getScenarioService().getRestType() == Packets.RestType.Rest) {
            // 운행 요청
            WavResourcePlayer.getInstance(getContext()).play(R.raw.voice_113);
            txtStatus.setText(R.string.request_drive);
            getScenarioService().requestRest(Packets.RestType.Working);
        } else {
            // 휴식 요청
            WavResourcePlayer.getInstance(getContext()).play(R.raw.voice_111);
            txtStatus.setText(R.string.request_rest);
            getScenarioService().requestRest(Packets.RestType.Rest);
        }
    }

    private Handler delayHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WAITING_INIT_MODEM:
                    if (modemInitializeCount >= 5) {
                        WavResourcePlayer.getInstance(getActivity()).play(R.raw.voice_103);
                        showErrorMessage(getString(R.string.not_available_network), 1500);
                    } else {
                        if (NetworkManager.getInstance().isAvailableNetwork(getActivity())) {
                            ((MainActivity) getActivity()).requestModemNumber();
                            txtStatus.setText(getString(R.string.request_certify) + ".. (0)");
                            // 모뎀 전화번호 가져오는 시간을 고려하여 delay를 준다.
                            sendEmptyMessageDelayed(MSG_WAITING_GET_TO_MODEM_NUMBER, 1000);
                        } else {
                            modemInitializeCount++;
                            LogHelper.write("#### 네트워크 연결 안됨 -> Waiting : " + modemInitializeCount);
                            txtStatus.setText(getString(R.string.initialized_modem)
                                    + " (" + modemInitializeCount + ")");
                            sendEmptyMessageDelayed(MSG_WAITING_INIT_MODEM, 1000);
                        }
                    }
                    break;
                case MSG_WAITING_GET_TO_MODEM_NUMBER:
                    if (txtStatus != null) {
                        txtStatus.setText(getString(R.string.request_certify) + ".. (1)");
                    }
                    sendEmptyMessageDelayed(MSG_REQUEST_CERTIFICATION, 500);
                    break;
                case MSG_REQUEST_CERTIFICATION:
                    getScenarioService().requestServicePacket(txtPhoneNumber.getText().toString(), false);
                    break;
                case MSG_BACK_TO_MANAGEMENT:
                    FragmentUtil.replace(getFragmentManager(),
                            ServiceManagementFragment.newInstance(txtPhoneNumber.getText().toString()));
                    break;
                default:
                    break;
            }
        }
    };

    //---------------------------------------------------------------------------------------------
    // public
    //---------------------------------------------------------------------------------------------
    public void applyCertificationResult(Packets.CertificationResult result, int certCode) {
        LogHelper.d(">> Cert Result = " + result + ", " + certCode);
        if (result != Packets.CertificationResult.Success) {
            String message = getString(R.string.fail_cert);
            if (result == Packets.CertificationResult.InvalidCar) {
                message = getString(R.string.fail_car) + " (0x" + Integer.toHexString(result.value) + ")";
                WavResourcePlayer.getInstance(getActivity()).play(R.raw.voice_105);
            } else if (result == Packets.CertificationResult.InvalidContact) {
                message = getString(R.string.fail_phone_number) + " (0x" + Integer.toHexString(result.value) + ")";
                WavResourcePlayer.getInstance(getActivity()).play(R.raw.voice_104);
            } else if (result == Packets.CertificationResult.DriverPenalty) {
                message = getString(R.string.fail_panelty) + " (0x" + Integer.toHexString(result.value) + ")";
                WavResourcePlayer.getInstance(getActivity()).play(R.raw.voice_103);
            } else if (result == Packets.CertificationResult.InvalidHoliday) {
                message = getString(R.string.fail_vacation) + " (0x" + Integer.toHexString(result.value) + ")";
                WavResourcePlayer.getInstance(getActivity()).play(R.raw.voice_103);
            } else {
                message += "(0x" + Integer.toHexString(certCode) + ")";
                WavResourcePlayer.getInstance(getActivity()).play(R.raw.voice_103);
            }
            showErrorMessage(message);
        } else {
            // 인증 완료 되면 전화번호를 저장 한다.
            String number = getCfgLoader().getDriverPhoneNumber();
            if (number == null || !number.equals(txtPhoneNumber.getText().toString())) {
                getCfgLoader().setDriverPhoneNumber(txtPhoneNumber.getText().toString());
                getCfgLoader().save();
            }
            WavResourcePlayer.getInstance(getActivity()).play(R.raw.voice_102);
            FragmentUtil.replace(getFragmentManager(), new ServiceStatusFragment());
        }
    }

    public void showErrorMessage(String message) {
        showErrorMessage(message, 3000);
    }

    public void showErrorMessage(String message, long delay) {
        txtStatus.setText(message);
        delayHandler.sendEmptyMessageDelayed(MSG_BACK_TO_MANAGEMENT, delay);
    }

    public void applyRestResult(Packets.RestType restType) {
        if (restType == Packets.RestType.Rest) {
            WavResourcePlayer.getInstance(getActivity()).play(R.raw.voice_112);
            txtStatus.setText(R.string.success_rest);
            btnRest.setText(R.string.drive);
        } else {
            WavResourcePlayer.getInstance(getActivity()).play(R.raw.voice_114);
            txtStatus.setText(R.string.success_drive);
            btnRest.setText(R.string.rest);
        }
        INaviExecutor.run(getActivity());
    }
}
