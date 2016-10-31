package com.thinkware.florida.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thinkware.florida.R;
import com.thinkware.florida.network.manager.NetworkManager;
import com.thinkware.florida.ui.MainActivity;
import com.thinkware.florida.ui.view.NumberPadView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ServiceManagementFragment extends BaseFragment {
    View root, btnLogin;
    NumberPadView numberPad;
    TextView txtPhoneNumber;

    public ServiceManagementFragment() {
        // Required empty public constructor
    }

    public static ServiceManagementFragment newInstance(String phoneNumber) {
        Bundle b = new Bundle();
        b.putString("PhoneNumber", phoneNumber);
        ServiceManagementFragment f = new ServiceManagementFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_service_management, container, false);

        btnLogin = root.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).initialize();

                // 1. 환경 설정 여부 판단
                if (!((MainActivity) getActivity()).hasConfiguration()) {
                    return;
                }

//                // 2. 네트워크 연결 여부 판단.
//                if (!NetworkManager.getInstance().isAvailableNetwork(getActivity())) {
//                    WavResourcePlayer.getInstance(getActivity()).play(R.raw.voice_103);
//                    FragmentUtil.replace(getFragmentManager(), ServiceStatusFragment.newInstance(getString(R.string.not_available_network)));
//                    return ;
//                }

                // 미터기, 빈차등, Emergency의 경우 초기화 과정에서 오류가 발생하더라도 별도로 체크하지 않고 SKIP 한다.
                // bind가 정상적으로 이루어 지지 않았을 경우 혹은 늦게 이루어질 경우
                // "인증" 버튼을 통해 재시도가 가능하므로 구동에 이슈가 되지 않는다.
//                // 3. 서비스 바인드 상태 판단
//                if (!((MainActivity)getActivity()).isBindedLocalService()) {
//                    LogHelper.d(">> Not yet bind service.");
//                    return ;
//                }

                if (NetworkManager.getInstance().isAvailableNetwork(getActivity())) {
                    FragmentUtil.replace(getFragmentManager(),
                            ServiceStatusFragment.newInstance(
                                    ServiceStatusFragment.STATUS_CERTIFICATION,
                                    txtPhoneNumber.getText().toString(),
                                    getString(R.string.request_certify)));
                } else {
                    FragmentUtil.replace(getFragmentManager(),
                            ServiceStatusFragment.newInstance(
                                    ServiceStatusFragment.STATUS_CERTIFICATION_AFTER_MODEM_INIT,
                                    txtPhoneNumber.getText().toString(),
                                    getString(R.string.initialized_modem)));
                }
            }
        });

        txtPhoneNumber = (TextView) root.findViewById(R.id.txt_phonenumber);
        if (getArguments() != null && !TextUtils.isEmpty(getArguments().getString("PhoneNumber"))) {
            txtPhoneNumber.setText(getArguments().getString("PhoneNumber"));
        } else {
            if (!getCfgLoader().isCorporation()) {
                txtPhoneNumber.setText(getCfgLoader().getDriverPhoneNumber());
            }
        }

        numberPad = (NumberPadView) root.findViewById(R.id.numberpad);
        numberPad.setFocusedTextView(txtPhoneNumber);

        return root;
    }
}
