package com.thinkware.florida.ui.fragment;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thinkware.florida.R;
import com.thinkware.florida.media.WavResourcePlayer;
import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.server2mdt.OrderInfoPacket;
import com.thinkware.florida.scenario.PreferenceUtil;
import com.thinkware.florida.ui.RequestOrderPopupActivity;
import com.thinkware.florida.ui.view.CropImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestOrderFragment extends BaseFragment {
    View root, btnRequest, btnRefuse;
    TextView txtLocation, txtLocationDetail;
    CropImageView countDownImg;
    CountDownTimer countDownTimer;
    int countTotal;
    private static final int COUNT_DOWN_INTERVAL = 1000;
    private OrderInfoPacket tempPacket;

    public RequestOrderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_request_order, container, false);

        countTotal = getCfgLoader().getCvt();

        btnRequest = root.findViewById(R.id.btn_request);
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer = null;
                }
                if (tempPacket != null) {
                    getScenarioService().requestOrderRealtime(Packets.OrderDecisionType.Request, tempPacket);
                }
                WavResourcePlayer.getInstance(getActivity()).play(R.raw.voice_124);
                if (getActivity() instanceof RequestOrderPopupActivity) {
                    ((RequestOrderPopupActivity)getActivity()).finishWithINavi();
                }
            }
        });

        btnRefuse = root.findViewById(R.id.btn_refuse);
        btnRefuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferenceUtil.clearTempCallInfo(getActivity());
                if (tempPacket != null) {
                    getScenarioService().requestOrderRealtime(Packets.OrderDecisionType.Reject, tempPacket);
                }
                if (getActivity() instanceof RequestOrderPopupActivity) {
                    ((RequestOrderPopupActivity)getActivity()).finishWithINavi();
                }
            }
        });

        txtLocation = (TextView) root.findViewById(R.id.txt_location);
        txtLocationDetail = (TextView) root.findViewById(R.id.txt_location_detail);

        countDownImg = (CropImageView) root.findViewById(R.id.progress_count);
        countDownImg.setPosition(countTotal);
        countDownTimer = new CountDownTimer((countTotal + 1) * COUNT_DOWN_INTERVAL, COUNT_DOWN_INTERVAL - 5) {
            @Override
            public void onTick(long l) {
                countDownImg.setPosition(countTotal--);
            }

            @Override
            public void onFinish() {
                if (tempPacket != null) {
                    getScenarioService().requestOrderRealtime(Packets.OrderDecisionType.Reject, tempPacket);
                }
                if (getActivity() instanceof RequestOrderPopupActivity) {
                    ((RequestOrderPopupActivity)getActivity()).finishWithINavi();
                }
            }
        };
        countDownTimer.start();

        initialize();

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            countDownTimer.cancel();
        } catch (Exception e) {
        }

        countDownTimer = null;
    }

    @Override
    public boolean onBackPressed() {
        if (tempPacket != null) {
            getScenarioService().requestOrderRealtime(Packets.OrderDecisionType.Reject, tempPacket);
        }
        if (getActivity() instanceof RequestOrderPopupActivity) {
            ((RequestOrderPopupActivity)getActivity()).finishWithINavi();
        }
        return super.onBackPressed();
    }

    private void initialize() {
        tempPacket = PreferenceUtil.getTempCallInfo(getActivity());

        if (tempPacket != null) {
            txtLocation.setText(tempPacket.getPlace());

            SpannableStringBuilder ssb = new SpannableStringBuilder();
            String distance = getString(R.string.distance) + " : "
                    + (int) getScenarioService().getDistance(tempPacket.getLatitude(), tempPacket.getLongitude()) + "m";
            if (tempPacket.getOrderKind() == Packets.OrderKind.Mobile) {
                ForegroundColorSpan span = new ForegroundColorSpan(0xffbaff00);
                String yesCall = getString(R.string.yes_call) + " ";
                ssb.append(yesCall);
                ssb.setSpan(span, 0, yesCall.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            ssb.append(distance).append("\n");
            ssb.append(tempPacket.getPlaceExplanation());

            txtLocationDetail.setText(ssb);
        }
    }
}
