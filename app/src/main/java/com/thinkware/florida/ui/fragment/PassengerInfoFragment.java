package com.thinkware.florida.ui.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thinkware.florida.R;
import com.thinkware.florida.media.WavResourcePlayer;
import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.server2mdt.OrderInfoPacket;
import com.thinkware.florida.network.packets.server2mdt.WaitOrderInfoPacket;
import com.thinkware.florida.scenario.ConfigurationLoader;
import com.thinkware.florida.scenario.INaviExecutor;
import com.thinkware.florida.scenario.PreferenceUtil;
import com.thinkware.florida.scenario.ServiceNumber;
import com.thinkware.florida.ui.MainApplication;
import com.thinkware.florida.ui.NoticePopupActivity;
import com.thinkware.florida.ui.PassengerInfoPopupActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class PassengerInfoFragment extends BaseFragment {

	TextView txtStatus, txtCallNum, txtPhoneNum, txtLocation, txtLocationDetail;
	TextView txtPassengerName, txtDisabledGrade, txtWheelchairYn, txtDestination; //복지콜

	View guide, fail, call;
	boolean isFullScreen = false;
	boolean isCallForDisabledPerson = false;

	public PassengerInfoFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arg = getArguments();
		if (arg != null) {
			isFullScreen = getArguments().getBoolean("full_screen", false);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		ConfigurationLoader cfgLoader = ConfigurationLoader.getInstance();
		int serviceNumber = cfgLoader.getServiceNumber();
		isCallForDisabledPerson = (serviceNumber == ServiceNumber.AREA_SUNGNAM_BOKJI);
//		LogHelper.e("serviceNumber : " + serviceNumber);
//		LogHelper.e("isCallForDisabledPerson : " + isCallForDisabledPerson);

		//서비스 번호로 받아올 경우
		View root;
		if (isFullScreen) {
			if (isCallForDisabledPerson) {
				root = inflater.inflate(R.layout.fragment_passenger_info2_dp, container, false);
			} else {
				root = inflater.inflate(R.layout.fragment_passenger_info2, container, false);
			}
		} else {
			if (isCallForDisabledPerson) {
				root = inflater.inflate(R.layout.fragment_passenger_info_dp, container, false);
			} else {
				root = inflater.inflate(R.layout.fragment_passenger_info, container, false);
			}
		}

		guide = root.findViewById(R.id.btn_guide);
		guide.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TextUtils.isEmpty(txtCallNum.getText().toString())) {
					return;
				}

				double latitude = -1;
				double longitude = -1;
				String destination = "";

				WaitOrderInfoPacket wait = PreferenceUtil.getWaitOrderInfo(getActivity());
				if (wait != null) {
					latitude = wait.getLatitude();
					longitude = wait.getLongitude();
					destination = wait.getPlace();
				} else {
					OrderInfoPacket normal = PreferenceUtil.getNormalCallInfo(getActivity());
					if (normal != null) {
						latitude = normal.getLatitude();
						longitude = normal.getLongitude();
						destination = normal.getPlace();
					}
				}

				if (latitude != -1 && longitude != -1) {
					INaviExecutor.startNavigationNow(getContext(), destination, latitude, longitude);
				}

				quit();
			}
		});

		fail = root.findViewById(R.id.btn_fail);
		fail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TextUtils.isEmpty(txtCallNum.getText().toString())) {
					return;
				}

				WaitOrderInfoPacket wait = PreferenceUtil.getWaitOrderInfo(getActivity());
				OrderInfoPacket normal = PreferenceUtil.getNormalCallInfo(getActivity());
				if (wait != null || normal != null) {
					if (!((MainApplication) getActivity().getApplication())
							.isLaunchedActivity(NoticePopupActivity.class)) {
						INaviExecutor.cancelNavigation(getContext());
					}
					cancel();
				}
			}
		});

		txtStatus = (TextView) root.findViewById(R.id.textView_status);
		txtCallNum = (TextView) root.findViewById(R.id.txt_callnum);
		txtPhoneNum = (TextView) root.findViewById(R.id.txt_phonenumber);
		txtLocation = (TextView) root.findViewById(R.id.txt_location);
		txtLocationDetail = (TextView) root.findViewById(R.id.txt_location_detail);

		if(isCallForDisabledPerson){
			call = root.findViewById(R.id.btn_call);
			call.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					String callNum = txtPhoneNum.getText().toString();
					if (!callNum.equals("")){
						callToCustomer(callNum);
					}
				}
			});
			//복지콜
			txtPassengerName = (TextView) root.findViewById(R.id.txt_passenger_name);
			txtDisabledGrade = (TextView) root.findViewById(R.id.txt_disabled_grade);
			txtWheelchairYn = (TextView) root.findViewById(R.id.txt_wheelchair_yn);
			txtDestination = (TextView) root.findViewById(R.id.txt_destination);
		}

		initialize();
		return root;
	}

	@Override
	public boolean onBackPressed() {
		cancel();
		return super.onBackPressed();
	}

	private void initialize() {
		// 운행 중에는 손님이 타고 있으므로 아무 정보도 표시하지 않는다.
		Packets.BoardType boardType = getScenarioService().getBoardType();
		if (boardType == Packets.BoardType.Boarding) {
			return;
		}

		if (isFullScreen) {
			WavResourcePlayer.getInstance(getActivity()).play(R.raw.voice_120);
		}

		WaitOrderInfoPacket waitOrderInfoPacket = PreferenceUtil.getWaitOrderInfo(getActivity());
		if (waitOrderInfoPacket != null && boardType != Packets.BoardType.Boarding) {
			// 1. 미리 저장되어 있는 대기요청고객정보가 있다면 해당 정보를 보여준다.
			txtCallNum.setText(String.valueOf(waitOrderInfoPacket.getCallNumber()));
			txtPhoneNum.setText(waitOrderInfoPacket.getCallerPhone());
			txtLocation.setText(waitOrderInfoPacket.getPlace());
			txtLocationDetail.setText(waitOrderInfoPacket.getPlaceExplanation());
		} else if (PreferenceUtil.getWaitArea(getActivity()) != null) {
			if (txtStatus != null && boardType != Packets.BoardType.Boarding) {
				txtStatus.setText(": " + getString(R.string.request_caller_info));
			}
			// 2. 저장 되어 있는 대기요청고객정보가 없지만 대기요청 상태의 경우 0x1517 대기배차고객정보 요청해서 정보를 가져온다.
			getScenarioService().requestWaitPassengerInfo();
		} else {
			// 3. 실시간 배차 저장된 정보를 찾아 본다.
			findOrderInfoFromFile();
		}
	}

	private void findOrderInfoFromFile() {
		// 대기 상태가 아닐 경우 0x1312 배차데이터에서 저장해 둔 고객 정보를 보여 준다.
		OrderInfoPacket normal = PreferenceUtil.getNormalCallInfo(getActivity());
		if (normal != null) {
			txtCallNum.setText(String.valueOf(normal.getCallNumber()));
			txtPhoneNum.setText(normal.getCallerPhone());
			txtLocation.setText(normal.getPlace());
			txtLocationDetail.setText(normal.getPlaceExplanation());

			//복지콜
			if(isCallForDisabledPerson) {
				txtPassengerName.setText(normal.getCallerName());
				txtDisabledGrade.setText(normal.getErrorCode());
				txtWheelchairYn.setText(normal.isWheelChair());
				txtDestination.setText(normal.getDestination());
			}
		} else {
			OrderInfoPacket getOn = PreferenceUtil.getGetOnCallInfo(getActivity());
			if (getOn != null) {
				// 일반배차가 비어 있는데 승차 중 배차가 있다는 것은 오류가 발생했다는 의미다.
				// 예외 처리를 추가해서 운행 보고가 정상적으로 이루어지도록 한다.
				PreferenceUtil.setNormalCallInfo(getActivity(), getOn);
				PreferenceUtil.clearGetOnCallInfo(getActivity());

				txtCallNum.setText(String.valueOf(getOn.getCallNumber()));
				txtPhoneNum.setText(getOn.getCallerPhone());
				txtLocation.setText(getOn.getPlace());
				txtLocationDetail.setText(getOn.getPlaceExplanation());

				//복지콜
				if(isCallForDisabledPerson) {
					txtPassengerName.setText(getOn.getCallerName());
					txtPassengerName.setText(getOn.getCallerName());
					txtDisabledGrade.setText(getOn.getErrorCode());
					txtWheelchairYn.setText(getOn.isWheelChair());
					txtDestination.setText(getOn.getDestination());
				}
			}
		}
	}

	private void callToCustomer(String telNum){
		Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telNum));
		callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getActivity().startActivity(callIntent);
	}

	private void quit() {
		if (getActivity() instanceof PassengerInfoPopupActivity) {
			((PassengerInfoPopupActivity)getActivity()).finishWithINavi();
		} else {
			FragmentUtil.replace(getFragmentManager(), PassengerInfoFragment.this);
		}
	}

	private void cancel() {
		if (TextUtils.isEmpty(txtCallNum.getText().toString())) {
			quit();
		} else {
			WaitOrderInfoPacket wait = PreferenceUtil.getWaitOrderInfo(getActivity());
			if (wait != null) {
				getScenarioService().requestReport(
						wait.getCallNumber(), wait.getOrderCount(),
						wait.getOrderKind(), wait.getCallReceiptDate(),
						Packets.ReportKind.Failed, 0, 0);
			} else {
				OrderInfoPacket normal = PreferenceUtil.getNormalCallInfo(getActivity());
				if (normal != null) {
					getScenarioService().requestReport(
							normal.getCallNumber(), normal.getOrderCount(),
							normal.getOrderKind(), normal.getCallReceiptDate(),
							Packets.ReportKind.Failed, 0, 0);
				}
			}
		}
	}

	public void applySuccessOrder() {
		if (txtStatus != null) {
			txtStatus.setText("");
		}
		txtCallNum.setText("");
		txtPhoneNum.setText("");
		txtLocation.setText("");
		txtLocationDetail.setText("");
	}

	public void applyFailOrder(Packets.BoardType boardType) {
		if (txtStatus != null && boardType != Packets.BoardType.Boarding) {
			txtStatus.setText(": " + getString(R.string.empty_caller));
		}
	}

	public void applyCancelOrder() {
		quit();
	}
}
