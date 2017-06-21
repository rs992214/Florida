package com.thinkware.florida.ui;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.thinkware.florida.R;
import com.thinkware.florida.network.packets.server2mdt.ResponseWaitAreaStatePacket;
import com.thinkware.florida.service.ScenarioService;
import com.thinkware.florida.utility.log.LogHelper;

/**
 * Created by hoonlee on 2017. 5. 30..
 */

public class WaitStateActivity extends BaseActivity {

    View btnCloseX;
    TextView textNames0, textNames1, textNum0, textNum1;
    TextView[] textStates;
    ListView listStates0, listStates1;
    ArrayAdapter<String> listAdapter0, listAdapter1;

    private int mAreaNum;
    private int[] mCarNumInAreas;
    private String[] mAreaNames;

    static WaitStateActivity mInstance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_state);

        btnCloseX = (View) findViewById(R.id.btn_close_x);
        btnCloseX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        listStates0 = (ListView) findViewById(R.id.list_state0);
        listStates1 = (ListView) findViewById(R.id.list_state1);
        initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInstance = null;
    }

    private void initialize() {
        mInstance = this;
        ((MainApplication)getApplication()).getScenarioService().requestWaitAreaState();
    }

    public static WaitStateActivity getInstance()
    {
        return mInstance;
    }

    public void apply(ResponseWaitAreaStatePacket packet) {
        mAreaNames = packet.getWaitAreaNames();
        mCarNumInAreas = packet.getCarNumInWaitAreas();
        mAreaNum = packet.getAreaNum();
        String[] areaName0 = new String[mAreaNum/2];
        String[] areaName1 = new String[mAreaNum/2];
        for(int i = 0; i <  mAreaNum/2; i++) {
            if(mAreaNames[i] == null || mAreaNames[i].length() == 0) {
                areaName0[i] = "" + (i+1) + ". ";
            } else {
                areaName0[i] = "" + (i+1) + ". " + new String(mAreaNames[i])
                        + "      " + mCarNumInAreas[i];
            }

            if(mAreaNames[i+mAreaNum/2] == null || mAreaNames[i+mAreaNum/2].length() == 0) {
                areaName1[i] = "" + (i+mAreaNum/2+1) + ". ";
            } else {
                areaName1[i] = "" + (i+mAreaNum/2+1) + ". " + new String(mAreaNames[i+mAreaNum/2])
                        + "      " + mCarNumInAreas[i+mAreaNum/2];
            }

        }

        listAdapter0 = new ArrayAdapter<String>(getApplicationContext(), R.layout.listview_waitstate_item, areaName0);
        listStates0.setAdapter(listAdapter0);

        listAdapter1 = new ArrayAdapter<String>(getApplicationContext(), R.layout.listview_waitstate_item, areaName1);
        listStates1.setAdapter(listAdapter1);

    }
}
