package com.thinkware.florida.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thinkware.florida.R;
import com.thinkware.florida.scenario.ConfigurationLoader;
import com.thinkware.florida.ui.view.NumberPadView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigPasswordFragment extends BaseFragment {
    View confirm, cancel;
    TextView txtPassword;
    NumberPadView numberPad;

    public ConfigPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_config_password, container, false);

        confirm = root.findViewById(R.id.btn_confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConfigurationLoader cfgLoader = getCfgLoader();
                if (cfgLoader.getPassword().equals(txtPassword.getText().toString())) {
                    FragmentUtil.replace(getFragmentManager(), new ConfigFragment());
                } else {
                    txtPassword.setText("");
                }
            }
        });
        cancel = root.findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        txtPassword = (TextView)root.findViewById(R.id.txt_password);

        numberPad = (NumberPadView)root.findViewById(R.id.numberpad);
        numberPad.setFocusedTextView(txtPassword);

        return root;
    }
}
