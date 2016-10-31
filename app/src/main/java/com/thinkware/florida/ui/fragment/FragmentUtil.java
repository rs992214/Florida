package com.thinkware.florida.ui.fragment;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.thinkware.florida.R;

/**
 * Created by Mihoe on 2016-09-09.
 */
public class FragmentUtil {


    public static void add(FragmentManager fragmentManager, Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.container, fragment, fragment.getClass().getSimpleName());
        transaction.addToBackStack(fragment.getClass().getSimpleName());
        transaction.commitAllowingStateLoss();
    }

    public static void replace(FragmentManager fragmentManager, Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment, fragment.getClass().getSimpleName());
        transaction.addToBackStack(fragment.getClass().getSimpleName());
        transaction.commitAllowingStateLoss();
    }

    public static void remove(FragmentManager fragmentManager, Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragment);
        transaction.commitAllowingStateLoss();
    }

    public static Fragment getTopFragment(FragmentManager fm) {
        if (fm == null) {
            return null;
        }

        int nSize = fm.getBackStackEntryCount();

        if (nSize <= 0) {
            return null;
        }

        FragmentManager.BackStackEntry tt = fm.getBackStackEntryAt(nSize - 1);
        String str = tt.getName();
        Fragment fragment = fm.findFragmentByTag(str);

        return fragment;

    }
}
