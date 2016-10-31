package com.thinkware.florida.ui.fragment;

import android.support.v4.app.Fragment;

import com.thinkware.florida.scenario.ConfigurationLoader;
import com.thinkware.florida.service.ScenarioService;
import com.thinkware.florida.ui.MainApplication;

/**
 * Created by zic325 on 2016. 9. 13..
 */
public class BaseFragment extends Fragment {

    public ScenarioService getScenarioService() {
        return ((MainApplication) getActivity().getApplication()).getScenarioService();
    }

    public ConfigurationLoader getCfgLoader() {
        return ConfigurationLoader.getInstance();
    }

    public boolean onBackPressed() {
        return false;
    }

}
