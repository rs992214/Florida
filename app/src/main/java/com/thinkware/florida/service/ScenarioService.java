package com.thinkware.florida.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.thinkware.florida.BuildConfig;
import com.thinkware.florida.R;
import com.thinkware.florida.external.service.EmergencyService;
import com.thinkware.florida.external.service.TachoMeterService;
import com.thinkware.florida.external.service.VacancyLightService;
import com.thinkware.florida.external.service.data.ServiceStatus;
import com.thinkware.florida.media.WavResourcePlayer;
import com.thinkware.florida.network.manager.NetworkListener;
import com.thinkware.florida.network.manager.NetworkManager;
import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.RequestPacket;
import com.thinkware.florida.network.packets.ResponsePacket;
import com.thinkware.florida.network.packets.mdt2server.AckPacket;
import com.thinkware.florida.network.packets.mdt2server.LivePacket;
import com.thinkware.florida.network.packets.mdt2server.PeriodSendingPacket;
import com.thinkware.florida.network.packets.mdt2server.RequestAccountPacket;
import com.thinkware.florida.network.packets.mdt2server.RequestCallerInfoPacket;
import com.thinkware.florida.network.packets.mdt2server.RequestConfigPacket;
import com.thinkware.florida.network.packets.mdt2server.RequestEmergencyPacket;
import com.thinkware.florida.network.packets.mdt2server.RequestMessagePacket;
import com.thinkware.florida.network.packets.mdt2server.RequestNoticePacket;
import com.thinkware.florida.network.packets.mdt2server.RequestOrderRealtimePacket;
import com.thinkware.florida.network.packets.mdt2server.RequestRestPacket;
import com.thinkware.florida.network.packets.mdt2server.RequestServicePacket;
import com.thinkware.florida.network.packets.mdt2server.RequestWaitAreaPacket;
import com.thinkware.florida.network.packets.mdt2server.ServiceReportPacket;
import com.thinkware.florida.network.packets.mdt2server.WaitCancelPacket;
import com.thinkware.florida.network.packets.mdt2server.WaitDecisionPacket;
import com.thinkware.florida.network.packets.server2mdt.CallerInfoResendPacket;
import com.thinkware.florida.network.packets.server2mdt.NoticesPacket;
import com.thinkware.florida.network.packets.server2mdt.OrderInfoPacket;
import com.thinkware.florida.network.packets.server2mdt.OrderInfoProcPacket;
import com.thinkware.florida.network.packets.server2mdt.ResponseAccountPacket;
import com.thinkware.florida.network.packets.server2mdt.ResponseMessagePacket;
import com.thinkware.florida.network.packets.server2mdt.ResponsePeriodSendingPacket;
import com.thinkware.florida.network.packets.server2mdt.ResponseRestPacket;
import com.thinkware.florida.network.packets.server2mdt.ResponseServiceReportPacket;
import com.thinkware.florida.network.packets.server2mdt.ResponseWaitDecisionPacket;
import com.thinkware.florida.network.packets.server2mdt.ServiceConfigPacket;
import com.thinkware.florida.network.packets.server2mdt.ServiceRequestResultPacket;
import com.thinkware.florida.network.packets.server2mdt.WaitOrderInfoPacket;
import com.thinkware.florida.network.packets.server2mdt.WaitPlaceInfoPacket;
import com.thinkware.florida.scenario.ConfigurationLoader;
import com.thinkware.florida.scenario.DebugWindow;
import com.thinkware.florida.scenario.GpsHelper;
import com.thinkware.florida.scenario.INaviExecutor;
import com.thinkware.florida.scenario.PreferenceUtil;
import com.thinkware.florida.ui.MainApplication;
import com.thinkware.florida.ui.MessagePopupActivity;
import com.thinkware.florida.ui.NoticePopupActivity;
import com.thinkware.florida.ui.PassengerInfoPopupActivity;
import com.thinkware.florida.ui.PopupActivity;
import com.thinkware.florida.ui.RequestOrderPopupActivity;
import com.thinkware.florida.ui.TestActivity;
import com.thinkware.florida.ui.fragment.FragmentUtil;
import com.thinkware.florida.ui.fragment.MessageListFragment;
import com.thinkware.florida.ui.fragment.PassengerInfoFragment;
import com.thinkware.florida.ui.fragment.QueryCallDetailFragment;
import com.thinkware.florida.ui.fragment.ServiceStatusFragment;
import com.thinkware.florida.ui.fragment.WaitCallFragment;
import com.thinkware.florida.utility.log.LogHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by zic325 on 2016. 9. 21..
 */

public class ScenarioService extends Service {

    //----------------------------------------------------------------------------------------
    // fields
    //----------------------------------------------------------------------------------------
    public static final int MSG_PERIOD = 1;
    public static final int MSG_LIVE = 2;
    public static final int MSG_EMERGENCY = 3;
    public static final int MSG_AREA_CHECK = 4;
    public static final int MSG_REPORT = 5;
    public static final int MSG_ACK = 6;
    public static final int MSG_SERVICE_ACK = 7;
    public static final int MSG_DEVICE_WATCH = 8;

    private final IBinder binder = new ScenarioService.LocalBinder();
    private Context context;
    private NetworkManager networkManager;
    private ConfigurationLoader cfgLoader;
    private GpsHelper gpsHelper;
    private Packets.BoardType boardType; // 승차 상태
    private Packets.RestType restType; // 휴식 상태
    private Packets.EmergencyType emergencyType; // 긴급상황 상태
    private boolean hasCertification; // 서비스 인증 성공 여부
    private boolean isAvailableNetwork, isValidPort; // DebugWindow에 네트워크 상태, Port 상태를 표시하기 위함
    private int reportRetryCount, ackRetryCount;
    // 전체 화면 Activity 팝업(공지사항, 메시지 등)이 보여질 때 이전 상태가 Background 였는지 저장 한다.
    // 이전 상태가 Background 였다면 MainActivity가 보여지지 않도록 아이나비를 한번 더 호출해 준다.
    private boolean isPrevStatusBackground;
    private String modemNumber;
    // 모바일 배차를 받고 승차보고가 올라가기 전까지는 주기 전송 시간을 cfgLoader.getRc()로 한다.
    // 모바일 배차 승차보고 후 주기 시간을 정상적으로 되돌리기 위해 사용 한다
    private int periodTerm;

    private FragmentManager supportFragmentManager;
    private TachoMeterService serviceTachoMeter;
    private VacancyLightService serviceVacancyLight;
    private EmergencyService serviceEmergency;
    private boolean isDestroyed;

    //----------------------------------------------------------------------------------------
    // life-cycle
    //----------------------------------------------------------------------------------------
    @Override
    public void onCreate() {
        super.onCreate();
        LogHelper.d(">> onCreate()");

        boardType = Packets.BoardType.Empty;
        restType = Packets.RestType.Working;
        emergencyType = Packets.EmergencyType.End;
        hasCertification = false;
        reportRetryCount = 0;
        ackRetryCount = 0;

        this.context = this;

        cfgLoader = ConfigurationLoader.getInstance();
        periodTerm = cfgLoader.getPst();

        if (networkManager == null) {
            networkManager = NetworkManager.getInstance();
            networkManager.addNetworkListener(networkListener);
            //networkManager.connect(cfgLoader.getCallServerIp(), cfgLoader.getCallServerPort());
        }

        if (gpsHelper == null) {
            gpsHelper = new GpsHelper(context);
        }

        isAvailableNetwork = !networkManager.isAvailableNetwork(context);
        isValidPort = !isValidPort();

        isDestroyed = false;
        pollingHandler.sendEmptyMessage(MSG_DEVICE_WATCH);
    }

    public class LocalBinder extends Binder {
        public ScenarioService getService() {
            return ScenarioService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogHelper.d(">> onBind()");
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogHelper.d(">> onDestroy()");
        destroy();
    }

    //----------------------------------------------------------------------------------------
    // getter / setter
    //----------------------------------------------------------------------------------------
    public void setSupportFragmentManager(FragmentManager fm) {
        supportFragmentManager = fm;
    }

    public TachoMeterService getServiceTachoMeter() {
        return serviceTachoMeter;
    }

    public void setServiceTachoMeter(TachoMeterService svc) {
        serviceTachoMeter = svc;
    }

    public VacancyLightService getServiceVacancyLight() {
        return serviceVacancyLight;
    }

    public void setServiceVacancyLight(VacancyLightService svc) {
        serviceVacancyLight = svc;
    }

    public EmergencyService getServiceEmergency() {
        return serviceEmergency;
    }

    public void setServiceEmergency(EmergencyService svc) {
        serviceEmergency = svc;
    }

    public GpsHelper getGpsHelper() {
        return gpsHelper;
    }

    public boolean hasCertification() {
        return hasCertification;
    }

    public Packets.BoardType getBoardType() {
        return boardType;
    }

    // 시나리오 서비스가 바인드 되기 전에 빈차등/미터기에서 값이 올라오는 경우가 있으므로 Setter를 둔다.
    public void setBoardType(Packets.BoardType boardType) {
        this.boardType = boardType;
    }

    public Packets.RestType getRestType() {
        return restType;
    }

    public Packets.EmergencyType getEmergencyType() {
        return emergencyType;
    }

    public boolean isPrevStatusBackground() {
        return isPrevStatusBackground;
    }

    public void setPrevStatusBackground(boolean status) {
        isPrevStatusBackground = status;
    }

    public String getModemNumber() {
        return modemNumber;
    }

    public void setModemNumber(String modemNumber) {
        this.modemNumber = modemNumber;
    }

    //----------------------------------------------------------------------------------------
    // method
    //----------------------------------------------------------------------------------------
    public void reset() {
        pollingHandler.removeMessages(MSG_PERIOD);
        pollingHandler.removeMessages(MSG_LIVE);
        pollingHandler.removeMessages(MSG_EMERGENCY);

        pollingCheckWaitRange(false);

        networkManager.disconnect();
        hasCertification = false;
    }

    /**
     * 빈차등/미터기의 승차 신호를 처리 한다.
     *
     * @param fare    요금 (빈차등일 경우 0)
     * @param mileage 거리 (빈차등일 경우 0)
     */
    public void applyVacancy(int fare, int mileage) {
        if (boardType == Packets.BoardType.Empty) {
            LogHelper.d(">> Skip empty signal. already empty.");
            return;
        }

        boardType = Packets.BoardType.Empty;
        // 인증 전에는 빈차 신호를 무시 한다.
        if (hasCertification) {
            requestBoardState(Packets.ReportKind.GetOff, fare, mileage);
        }
    }

    /**
     * 빈차등/미터기의 승차 신호를 처리 한다.
     *
     * @param fare    요금 (빈차등일 경우 0)
     * @param mileage 거리 (빈차등일 경우 0)
     */
    public void applyDriving(int fare, int mileage) {
        if (boardType == Packets.BoardType.Boarding) {
            LogHelper.d(">> Skip driving signal. already driving.");
            return;
        }

        boardType = Packets.BoardType.Boarding;
        // 인증 전에는 승차 신호를 무시 한다.
        if (hasCertification) {
            requestBoardState(Packets.ReportKind.GetOn, fare, mileage);

            // 모바일 배차 수신 후 승차 신호가 올라갈 때 주기를 다시 변경 한다.
            // 배차가 하나도 없을 경우에 모바일 배차가 수신 되므로 orderkind 구분 하지 않아도 된다.
            if (periodTerm != cfgLoader.getPst()) {
                periodTerm = cfgLoader.getPst();
                pollingPeriod(periodTerm);
            }

            // 대기 상태이면서 대기 고객 정보가 없을 때 주행이 올라오면 대기 취소를 한다.
            ResponseWaitDecisionPacket p = PreferenceUtil.getWaitArea(context);
            if (p != null && PreferenceUtil.getWaitOrderInfo(context) == null) {
                requestWaitCancel(p.getWaitPlaceCode());
            }

            // 승차 신호가 들어올 경우 고객 정보 창을 닫도록 한다.
            Activity act = ((MainApplication) getApplication()).getActivity(PassengerInfoPopupActivity.class);
            if (act != null) {
                act.finish();
            }

            // 기기 테스트 화면에서 승/빈차 신호시 화면 종료되는 것에 대한 예외처리를 추가
            if (((MainApplication) getApplication()).getActivity(TestActivity.class) == null) {
                INaviExecutor.cancelNavigation(context);
            }
        }
    }

    public void enabledEmergency(boolean enabled) {
        if (enabled) {
            emergencyType = Packets.EmergencyType.Begin;
            pollingEmergency();
        } else {
            // 실제 Emergency Off는 서버에서 받아서 처리하도록 한다.
            // 디버깅 페이지에서 Emergency를 Off하는 경우를 위해 구현해 둔다.
            emergencyType = Packets.EmergencyType.End;
            pollingHandler.removeMessages(MSG_EMERGENCY);

            // 응급 상황 해제 후에 주기 전송을 재시작 한다.
            periodTerm = cfgLoader.getPst();
            WaitOrderInfoPacket wait = PreferenceUtil.getWaitOrderInfo(context);
            OrderInfoPacket normal = PreferenceUtil.getNormalCallInfo(context);
            if (wait != null
                    && wait.getOrderKind() == Packets.OrderKind.Mobile
                    && !wait.isReported()) {
                periodTerm = cfgLoader.getRc();
            } else if (normal != null
                    && normal.getOrderKind() == Packets.OrderKind.Mobile
                    && !normal.isReported()) {
                periodTerm = cfgLoader.getRc();
            }
            pollingPeriod(periodTerm);
        }
    }

    public void destroy() {
        if (networkManager != null) {
            networkManager.removeNetworkListener(networkListener);
            networkManager.disconnect();
        }
        if (gpsHelper != null) {
            gpsHelper.destroy();
        }

        isDestroyed = true;
        if (pollingHandler != null) {
            pollingHandler.removeMessages(MSG_PERIOD);
            pollingHandler.removeMessages(MSG_LIVE);
            pollingHandler.removeMessages(MSG_EMERGENCY);
            pollingHandler.removeMessages(MSG_AREA_CHECK);
            pollingHandler.removeMessages(MSG_REPORT);
            pollingHandler.removeMessages(MSG_ACK);
            pollingHandler.removeMessages(MSG_SERVICE_ACK);
            pollingHandler.removeMessages(MSG_DEVICE_WATCH);
        }
    }

    public void showDebugWindow(boolean moveLogList) {
        DebugWindow window = new DebugWindow(this, cfgLoader);
        window.show(moveLogList);
    }

    public String getCurrentTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
        return format.format(new Date(System.currentTimeMillis()));
    }

    // 거리를 계산하여 반환 (단위 : m)
    public float getDistance(float fLatitude, float fLongitude) {
        float fCarLatitude = gpsHelper.getLatitude();
        float fCarLongitude = gpsHelper.getLongitude();
        // 거리계산
        return (float) Math.sqrt(Math.pow(((fLongitude - fCarLongitude) * 88359.03358448), 2) +
                Math.pow(((fLatitude - fCarLatitude) * 110989.086779), 2));
    }

    /**
     * 배차 데이터를 아래의 순서로 업데이트 한다.
     * 1. 전달 받은 콜넘버와 저장 받은 배차데이터를 비교하여
     * 해당 사항이 있는 경우 배차데이터를 삭제 한다.
     * 2. 배차1이 없고 배차2가 존재한다면 배차2 -> 배차1로 이동 후
     * 배차2를 삭제 한다.
     *
     * @param callNo 콜번호
     */
    private void refreshSavedPassengerInfo(int callNo) {
        LogHelper.d(">> callNo : " + callNo);
        WaitOrderInfoPacket wait = PreferenceUtil.getWaitOrderInfo(context);
        if (wait != null && wait.getCallNumber() == callNo) {
            PreferenceUtil.clearWaitOrderInfo(context);
            PreferenceUtil.clearWaitArea(context);
        }

        OrderInfoPacket getOn = PreferenceUtil.getGetOnCallInfo(context);
        if (getOn != null && getOn.getCallNumber() == callNo) {
            PreferenceUtil.clearGetOnCallInfo(context);
        }

        OrderInfoPacket normal = PreferenceUtil.getNormalCallInfo(context);
        if (normal != null && normal.getCallNumber() == callNo) {
            PreferenceUtil.clearNormalCallInfo(context);
            PreferenceUtil.setNormalCallInfo(context, PreferenceUtil.getGetOnCallInfo(context));
            PreferenceUtil.clearGetOnCallInfo(context);
        }

        if (getOn != null && normal == null) {
            PreferenceUtil.clearNormalCallInfo(context);
            PreferenceUtil.setNormalCallInfo(context, PreferenceUtil.getGetOnCallInfo(context));
            PreferenceUtil.clearGetOnCallInfo(context);
        }
    }

    /**
     * 저장되어 있는 배차가 있다면 고객 정보팝업을 보여준다.
     */
    private void checkReservation() {
        LogHelper.d(">> Check reserved call info.");
        OrderInfoPacket normal = PreferenceUtil.getNormalCallInfo(this);
        if (normal != null) {
            LogHelper.d(">> has passenger info.");
            if (serviceVacancyLight != null) {
                serviceVacancyLight.setReservation();
            }
            showPassengerPopupActivity();
        }
    }

    private void launchActivity(Class<?> cls) {
        // 인증 완료 후 지도 화면 -> 메시지(공지사항, 콜 등) 수신 -> 메시지(공지사항, 콜 등) 창을 닫을 때 지도 화면이 보여져야 한다.
        // Activity 실행시 FLAG_ACTIVITY_NEW_TASK를 사용하더라도 taskAffinity가 MainActivity와 같아서
        // 동일 Task 내에서 Activity가 실행된다. (FLAG_ACTIVITY_MULTIPLE_TASK를 같이 사용하면 무조건 다른 Task로
        // Activity를 실행 가능하나 Task가 여러개 생성됨에 따라 발생되는 이슈들에 대한 검증이 현재 어려우므로 사용이 어렵다.)

        // 위의 이슈로 메시지 창을 닫았을 때 MainActivity가 onResume 된다.
        // 콜 메인 화면 대신 지도 화면을 보여주기 위해서 현재 최상위 Activity가 MainActivity일 경우에만
        // background/foreground FLAG를 저장해 두었다가 BaseActivity.finishWithINavi()에서 이를 보고 지도를 실행하도록 한다.
        Activity topAct = ((MainApplication) getApplication()).getTopActivity();
        if (topAct != null && topAct.getClass().getSimpleName().contains("MainActivity")) {
            isPrevStatusBackground = !((MainApplication) getApplication()).isForegroundActivity(getPackageName());
            LogHelper.write("#### isPrevStatusBackground = " + isPrevStatusBackground);
        } else {
            LogHelper.write("#### isPrevStatusBackground = " + (topAct == null ? "null" : topAct.getClass().getName()));
        }
        Intent intent = new Intent(ScenarioService.this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showNoticePopupActivity() {
        LogHelper.write("#### showNoticePopupActivity()");
        WavResourcePlayer.getInstance(context).play(R.raw.voice_115);
        launchActivity(NoticePopupActivity.class);
    }

    private void showPassengerPopupActivity() {
        LogHelper.write("#### showPassengerPopupActivity()");
        launchActivity(PassengerInfoPopupActivity.class);
    }

    private void showMessagePopupActivity() {
        LogHelper.write("#### showMessagePopupActivity()");
        launchActivity(MessagePopupActivity.class);
    }

    private void showRequestOrderPopupActivity() {
        LogHelper.write("#### showRequestOrderPopupActivity()");
        launchActivity(RequestOrderPopupActivity.class);
    }

    //----------------------------------------------------------------------------------------
    // Network & USB Port Watcher
    //----------------------------------------------------------------------------------------
    public void watchDevice() {
        if (!networkManager.isAvailableNetwork(context)) {
            if (isAvailableNetwork) {
                LogHelper.write("#### 네트워크 연결 끊김");
                if (BuildConfig.DEBUG) {
                    Intent intent = new Intent(context, PopupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("MSG", "통신 연결이 끊어졌습니다. 모뎀 상태를 확인해주세요.");
                    context.startActivity(intent);
                }
            }
            isAvailableNetwork = false;
        } else {
            if (!isAvailableNetwork) {
                LogHelper.write("#### 네트워크에 연결됨");
                ScenarioService svc = ((MainApplication) context.getApplicationContext()).getScenarioService();
                if (svc != null && svc.hasCertification()) {
                    // 네트워크 연결이 끊어진 후 재접속시 휴식 패킷에 오류 보고를 한다.
                    requestRest(Packets.RestType.ModemError);

                    //saveModemLog(context);
                }
            }
            isAvailableNetwork = true;
        }

        if (!isValidPort()) {
            if (isValidPort) {
                LogHelper.write("#### 포트 목록 : " + getPortList());
            }
            isValidPort = false;
        } else {
            if (!isValidPort) {
                LogHelper.write("#### 포트 목록 : " + getPortList());
            }
            isValidPort = true;
        }
    }

    private boolean isValidPort() {
        int successCount = 4;
        File dev = new File(File.separator + "dev");
        if (dev != null && dev.exists()) {
            File[] files = dev.listFiles();
            if (files != null) {
                for (File f : files) {
                    String name = f.getName();
                    if ("ttyUSB0".equals(name)) {
                        successCount--;
                    } else if ("ttyUSB1".equals(name)) {
                        successCount--;
                    } else if ("ttyUSB2".equals(name)) {
                        successCount--;
                    } else if ("ttyUSB3".equals(name)) {
                        successCount--;
                    }
                }
            }
        }

        return successCount == 0;
    }

    private String getPortList() {
        String list = "";
        File dev = new File(File.separator + "dev");
        if (dev != null && dev.exists()) {
            File[] files = dev.listFiles();
            if (files != null) {
                for (File f : files) {
                    String name = f.getName();
                    if (name != null && name.startsWith("ttyUSB")) {
                        list += name + " / ";
                    }
                }
            }
        }
        int lastIndex = list.lastIndexOf("/");
        if (lastIndex == -1) {
            return "연결된 포트 없음";
        } else {
            return list.substring(0, lastIndex);
        }
    }

    public String getServiceStatus(int status) {
        String ret = "알 수 없음";
        switch (status) {
            case ServiceStatus.UNKNOWN_ERROR:
                ret = "알 수 없는 오류";
                break;
            case ServiceStatus.SERVICE_NOT_LAUNCHED:
                ret = "서비스 기능이 시작되지 않음(초기상태)";
                break;
            case ServiceStatus.SERVICE_LAUNCHED:
                ret = "서비스 기능이 시작됨";
                break;
            case ServiceStatus.FAILED_SET_HANDLER:
                ret = "서비스에서 사용하는 handler 생성 오류";
                break;
            case ServiceStatus.FAILED_PORT_OPENED:
                ret = "외부 포트 열기 실패";
                break;
            case ServiceStatus.FAILED_SET_PARSER:
                ret = "내부 파서 생성 실패";
                break;
            case ServiceStatus.FAILED_SET_THREAD:
                ret = "Thread 생성 실패";
                break;
            case ServiceStatus.FAILED_SET_QUEUE:
                ret = "Queue 생성 실패";
                break;
            case ServiceStatus.FAILED_READ_DATA:
                ret = "외부기기에서 데이터 읽기 실패";
                break;
            case ServiceStatus.FAILED_WRITE_DATA:
                ret = "외부기기에 데이터 쓰기 실패";
                break;
            case ServiceStatus.FAILED_USE_NOT_CONNECTED:
                ret = "포트가 물리적으로 연결되어 있지 않음";
                break;
        }
        return ret;
    }

    //----------------------------------------------------------------------------------------
    // request
    //----------------------------------------------------------------------------------------

    /**
     * 패킷을 요청한다.
     * Socket 접속이 끊어지는 경우를 방지하기 위해
     * 마지막 요청 이후 일정 시간 이내에 요청이 없을 경우
     * Live 패킷을 전송하여 Connection을 유지한다.
     */
    public void request(RequestPacket packet) {
        networkManager.request(context, packet);

        // 마지막으로 요청 된 리퀘스트 이후 일정 시간 부터 라이브 전송 한다.
        pollingHandler.removeMessages(MSG_LIVE);
        pollingHandler.sendEmptyMessageDelayed(MSG_LIVE, cfgLoader.getRt() * 1000);
    }

    /**
     * Ack 패킷을 요청 한다. Retry로직이 포함되어 있다.(최대 3회 5초간격)
     */
    public void requestAck(final int messageType, final int serviceNo, final int callNo) {
        ackRetryCount = 0;

        AckPacket packet = new AckPacket();
        packet.setServiceNumber(serviceNo);
        packet.setCorporationCode(cfgLoader.getCorportaionCode());
        packet.setCarId(cfgLoader.getCarId());
        packet.setAckMessage(messageType);
        packet.setParameter(callNo);

        Message msg = pollingHandler.obtainMessage();
        msg.what = MSG_ACK;
        msg.obj = packet;

        pollingHandler.removeMessages(MSG_ACK);
        pollingHandler.sendMessage(msg);
    }

    /**
     * 서비스요청 패킷을 요청 한다.
     * 요청 후 3초 이내에 응답이 없을 경우 Error를 보여준다.
     */
    public void requestServicePacket(String driverNumber, boolean withTimer) {
        RequestServicePacket packet = new RequestServicePacket();
        packet.setServiceNumber(cfgLoader.getServiceNumber());
        packet.setCorporationCode(cfgLoader.getCorportaionCode());
        packet.setCarId(cfgLoader.getCarId());
        packet.setPhoneNumber(driverNumber); // 전화번호는 UI에서 세팅해준다.
        packet.setCorporationType(cfgLoader.isCorporation() ? Packets.CorporationType.Corporation : Packets.CorporationType.Indivisual);
        packet.setProgramVersion(cfgLoader.getProgramVersion());
        packet.setModemNumber(TextUtils.isEmpty(modemNumber) ? "" : modemNumber); // 단말의 실제 전화 번호
        request(packet);

        pollingHandler.removeMessages(MSG_SERVICE_ACK);
        if (withTimer) {
            pollingHandler.sendEmptyMessageDelayed(MSG_SERVICE_ACK, 3000);
        }
    }

    /**
     * 환경설정요청 패킷을 요청한다.
     */
    private void requestConfig(int cfgVersion) {
        RequestConfigPacket packet = new RequestConfigPacket();
        packet.setServiceNumber(cfgLoader.getServiceNumber());
        packet.setCorporationCode(cfgLoader.getCorportaionCode());
        packet.setCarId(cfgLoader.getCarId());
        packet.setConfigurationCode(cfgVersion);
        request(packet);
    }

    /**
     * 공지사항요청 패킷을 요청한다.
     */
    private void requestNotice() {
        RequestNoticePacket packet = new RequestNoticePacket();
        packet.setServiceNumber(cfgLoader.getServiceNumber());
        packet.setCorporationCode(cfgLoader.getCorportaionCode());
        packet.setCarId(cfgLoader.getCarId());
        request(packet);
    }

    /**
     * 메시지 요청 패킷을 요청한다.
     */
    private void requestMessage() {
        RequestMessagePacket packetMsg = new RequestMessagePacket();
        packetMsg.setServiceNumber(cfgLoader.getServiceNumber());
        packetMsg.setCorporationCode(cfgLoader.getCorportaionCode());
        packetMsg.setCarId(cfgLoader.getCarId());
        request(packetMsg);
    }

    /**
     * 주기전송 패킷을 요청한다.
     */
    private void requestPeriod() {
        PeriodSendingPacket packet = new PeriodSendingPacket();
        packet.setServiceNumber(cfgLoader.getServiceNumber());
        packet.setCorporationCode(cfgLoader.getCorportaionCode());
        packet.setCarId(cfgLoader.getCarId());
        packet.setSendingTime(getCurrentTime());
        packet.setGpsTime(gpsHelper.getTime());
        packet.setDirection(gpsHelper.getBearing());
        packet.setLongitude(gpsHelper.getLongitude());
        packet.setLatitude(gpsHelper.getLatitude());
        packet.setSpeed(gpsHelper.getSpeed());
        packet.setBoardState(boardType);
        packet.setRestState(restType);
        request(packet);
    }

    /**
     * 라이브 패킷을 요청한다.
     */
    private void requestLive() {
        LivePacket packet = new LivePacket();
        packet.setCarId(cfgLoader.getCarId());
        request(packet);
    }

    /**
     * Emergency 요청 패킷을 요청한다.
     */
    private void requestEmergency() {
        RequestEmergencyPacket packet = new RequestEmergencyPacket();
        packet.setServiceNumber(cfgLoader.getServiceNumber());
        packet.setCorporationCode(cfgLoader.getCorportaionCode());
        packet.setCarId(cfgLoader.getCarId());
        packet.setEmergencyType(emergencyType);
        packet.setGpsTime(gpsHelper.getTime());
        packet.setDirection(gpsHelper.getBearing());
        packet.setLongitude(gpsHelper.getLongitude());
        packet.setLatitude(gpsHelper.getLatitude());
        packet.setSpeed(gpsHelper.getSpeed());
        packet.setTaxiState(boardType);
        request(packet);
    }

    /**
     * 휴식/운행재개 패킷을 요청한다.
     */
    public void requestRest(Packets.RestType restType) {
        RequestRestPacket packet = new RequestRestPacket();
        packet.setCorporationCode(cfgLoader.getCorportaionCode());
        packet.setCarId(cfgLoader.getCarId());
        packet.setRestType(restType);
        request(packet);
    }

    /**
     * 콜정산 요청 패킷을 요청한다.
     */
    public void requestAccount(String begin, String end) {
        RequestAccountPacket packet = new RequestAccountPacket();
        packet.setServiceNumber(cfgLoader.getServiceNumber());
        packet.setCorporationCode(cfgLoader.getCorportaionCode());
        packet.setCarId(cfgLoader.getCarId());
        packet.setPhoneNumber(cfgLoader.getDriverPhoneNumber());
        packet.setAccountType(Packets.AccountType.Period);
        packet.setBeginDate(begin);
        packet.setEndDate(end);
        request(packet);
    }

    /**
     * 운행보고 패킷을 요청 한다. Retry 로직이 포함되어 있다. (최대 3회 5초 간격)
     */
    public void requestReport(final int callNo, final int orderCount, final Packets.OrderKind orderKind,
                              final String callDate, final Packets.ReportKind kind,
                              final int fare, final int mileage) {
        reportRetryCount = 0;

        ServiceReportPacket packet = new ServiceReportPacket();
        packet.setServiceNumber(cfgLoader.getServiceNumber());
        packet.setCorporationCode(cfgLoader.getCorportaionCode());
        packet.setCarId(cfgLoader.getCarId());
        packet.setPhoneNumber(cfgLoader.getDriverPhoneNumber());
        packet.setCallNumber(callNo);
        packet.setOrderCount(orderCount);
        packet.setOrderKind(orderKind);
        packet.setCallReceiptDate(callDate);
        packet.setReportKind(kind);
        packet.setGpsTime(gpsHelper.getTime());
        packet.setDirection(gpsHelper.getBearing());
        packet.setLongitude(gpsHelper.getLongitude());
        packet.setLatitude(gpsHelper.getLatitude());
        packet.setSpeed(gpsHelper.getSpeed());
        packet.setTaxiState(boardType);
        packet.setFare(fare);
        packet.setDistance(mileage);

        Message msg = pollingHandler.obtainMessage();
        msg.what = MSG_REPORT;
        msg.obj = packet;

        pollingHandler.removeMessages(MSG_REPORT);
        pollingHandler.sendMessage(msg);
    }

    /**
     * 승/빈차 신호를 서버에 전송 한다.
     * 저장된 배차 정보가 없을 경우 주기 전송 패킷을 요청한다.
     * 저장된 배차 정보가 있을 경우 운행 보고 패킷을 요청한다.
     */
    public void requestBoardState(Packets.ReportKind kind, int fare, int mileage) {
        WaitOrderInfoPacket wait = PreferenceUtil.getWaitOrderInfo(context);
        OrderInfoPacket normal = PreferenceUtil.getNormalCallInfo(context);
        if (wait == null && normal == null) {
            LogHelper.d(">> Not exist saved passenger info. request period sending");
            requestPeriod();

            if (kind == Packets.ReportKind.GetOn) {
                LogHelper.write("#### 승차 주기 보고");
            } else {
                LogHelper.write("#### 하차 주기 보고");
            }

            // 길에서 손님 태운 후 승차 상태에서 승차 중 배차를 받음 -> 하차 버튼 -> 고객 정보 창이 보여져야 함
            if (kind == Packets.ReportKind.GetOff) {
                refreshSavedPassengerInfo(0);
                checkReservation();
            }
        } else {
            if (wait != null) {
                wait.setReported(true);
                PreferenceUtil.setWaitOrderInfo(context, wait);
                requestReport(
                        wait.getCallNumber(), wait.getOrderCount(),
                        wait.getOrderKind(), wait.getCallReceiptDate(),
                        kind, fare, mileage);
            } else if (normal != null) {
                normal.setReported(true);
                PreferenceUtil.setNormalCallInfo(context, normal);
                requestReport(
                        normal.getCallNumber(), normal.getOrderCount(),
                        normal.getOrderKind(), normal.getCallReceiptDate(),
                        kind, fare, mileage);
            }
        }
    }

    /**
     * 대기지역요청 패킷을 요청한다.
     */
    public void requestWaitAreas() {
        RequestWaitAreaPacket packet = new RequestWaitAreaPacket();
        packet.setServiceNumber(cfgLoader.getServiceNumber());
        packet.setCorporationCode(cfgLoader.getCorportaionCode());
        packet.setCarId(cfgLoader.getCarId());
        packet.setGpsTime(gpsHelper.getTime());
        packet.setLongitude(gpsHelper.getLongitude());
        packet.setLatitude(gpsHelper.getLatitude());
        packet.setTaxiState(getBoardType());
        request(packet);
    }

    /**
     * 대기결정 패킷을 요청한다.
     */
    public void requestWait(String waitPlaceCode) {
        WaitDecisionPacket packet = new WaitDecisionPacket();
        packet.setServiceNumber(cfgLoader.getServiceNumber());
        packet.setCorporationCode(cfgLoader.getCorportaionCode());
        packet.setCarId(cfgLoader.getCarId());
        packet.setDriverNumber(cfgLoader.getDriverPhoneNumber());
        packet.setGpsTime(gpsHelper.getTime());
        packet.setLongitude(gpsHelper.getLongitude());
        packet.setLatitude(gpsHelper.getLatitude());
        packet.setDecisionAreaCode(waitPlaceCode);
        request(packet);
    }

    /**
     * 대기취소 패킷을 요청한다.
     */
    public void requestWaitCancel(String waitPlaceCode) {
        // 저장된 대기지역이 있는데 대기지역을 다시 요청하는 경우는 취소의 케이스로 간주한다.
        WaitOrderInfoPacket wait = PreferenceUtil.getWaitOrderInfo(context);
        if (wait != null) {
            requestReport(
                    wait.getCallNumber(),
                    wait.getOrderCount(),
                    wait.getOrderKind(),
                    wait.getCallReceiptDate(),
                    Packets.ReportKind.Failed, 0, 0);
        }
        WaitCancelPacket packet = new WaitCancelPacket();
        packet.setServiceNumber(cfgLoader.getServiceNumber());
        packet.setCorporationCode(cfgLoader.getCorportaionCode());
        packet.setCarId(cfgLoader.getCarId());
        packet.setPhoneNumber(cfgLoader.getDriverPhoneNumber());
        packet.setAreaCode(waitPlaceCode);
        request(packet);
    }

    /**
     * 대기배차고객정보 요청 패킷을 요청한다.
     */
    public void requestWaitPassengerInfo() {
        RequestCallerInfoPacket packet = new RequestCallerInfoPacket();
        packet.setServiceNumber(cfgLoader.getServiceNumber());
        packet.setCorporationCode(cfgLoader.getCorportaionCode());
        packet.setCarId(cfgLoader.getCarId());
        request(packet);
    }

    /**
     * 실시간 위치 및 배차결정 패킷을 요청한다.
     */
    public void requestOrderRealtime(Packets.OrderDecisionType type, OrderInfoPacket info) {
        RequestOrderRealtimePacket packet = new RequestOrderRealtimePacket();
        packet.setServiceNumber(cfgLoader.getServiceNumber());
        packet.setCorporationCode(cfgLoader.getCorportaionCode());
        packet.setCarId(cfgLoader.getCarId());
        packet.setPhoneNumber(cfgLoader.getDriverPhoneNumber());
        packet.setCallNumber(info.getCallNumber());
        packet.setCallReceiptDate(info.getCallReceiptDate());
        packet.setDecisionType(type);
        packet.setSendTime(getCurrentTime());
        packet.setGpsTime(gpsHelper.getTime());
        packet.setDirection(gpsHelper.getBearing());
        packet.setLongitude(gpsHelper.getLongitude());
        packet.setLatitude(gpsHelper.getLatitude());
        packet.setSpeed(gpsHelper.getSpeed());
        packet.setDistance(getDistance(info.getLatitude(), info.getLongitude()));
        packet.setOrderCount(info.getOrderCount());
        request(packet);
    }

    //----------------------------------------------------------------------------------------
    // polling & timer
    //----------------------------------------------------------------------------------------
    private void cancelTimer(Timer t) {
        if (t != null) {
            t.cancel();
            t = null;
        }
    }

    /**
     * 주기 전송 패킷을 일정 간격마다 요청 한다.
     */
    private void pollingPeriod(int period) {
        LogHelper.d(">> Polling period : " + period + " sec");
        pollingHandler.removeMessages(MSG_PERIOD);

        Message msg = pollingHandler.obtainMessage();
        msg.what = MSG_PERIOD;
        msg.arg1 = period;

        pollingHandler.sendMessage(msg);
    }

    /**
     * Emergency 요청 패킷을 일정 간격마다 요청 한다.
     */
    public void pollingEmergency() {
        pollingHandler.removeMessages(MSG_EMERGENCY);
        pollingHandler.sendEmptyMessage(MSG_EMERGENCY);

        // 응급 상황에서는 주기를 올리지 않아야 한다.
        pollingHandler.removeMessages(MSG_PERIOD);
    }

    /**
     * 지정된 거리를 벗어 날 경우 대기취소 패킷을 요청 한다.
     */
    private void pollingCheckWaitRange(boolean start) {
        // 범위를 벗어 난 상태에서 speed 5 이상이면 대기 취소를 요청 할 것 (5초 주기로 체크)
        pollingHandler.removeMessages(MSG_AREA_CHECK);
        if (start) {
            pollingHandler.sendEmptyMessage(MSG_AREA_CHECK);
        }
    }

    //----------------------------------------------------------------------------------------
    // NetworkManager Callback
    //----------------------------------------------------------------------------------------
    private NetworkListener networkListener = new NetworkListener() {

        @Override
        public void onConnectedServer() {
            LogHelper.write("#### Socket Connected");
            if (hasCertification) {
                // 소켓 연결이 끊어진 후 재접속시 주기 전송을 한번 한다.
                requestPeriod();
            }
        }

        @Override
        public void onDisconnectedServer(ErrorCode code) {
            LogHelper.write("#### Connect Error : " + code);
        }

        @Override
        public void onReceivedPacket(@NonNull ResponsePacket response) {
            LogHelper.write(">> RES " + response);
            int messageType = response.getMessageType();

            if (messageType != Packets.SERVICE_REQUEST_RESULT
                    && !hasCertification) {
                LogHelper.d(">> Skip received packet. Invalid service certification.");
                return;
            }
            switch (messageType) {
                case Packets.SERVICE_REQUEST_RESULT: { // 서비스 요청 응답
                    pollingHandler.removeMessages(MSG_SERVICE_ACK);

                    ServiceRequestResultPacket p = (ServiceRequestResultPacket) response;

                    Fragment f = FragmentUtil.getTopFragment(supportFragmentManager);
                    if (f != null && f instanceof ServiceStatusFragment) {
                        ((ServiceStatusFragment) f).applyCertificationResult(
                                p.getCertificationResult(), p.getCertCode());
                    }

                    if (p.getCertificationResult() == Packets.CertificationResult.Success) {

                        //saveModemLog(context);

                        hasCertification = true;
                        //requestPeriod();

                        INaviExecutor.run(context);

                        if (p.getProgramVersion() > cfgLoader.getProgramVersion()) {
                            startService(new Intent("com.thinkware.florida.otaupdater.Updater"));
                        }

                        periodTerm = cfgLoader.getPst();
                        WaitOrderInfoPacket wait = PreferenceUtil.getWaitOrderInfo(context);
                        OrderInfoPacket normal = PreferenceUtil.getNormalCallInfo(context);
                        if (wait != null
                                && wait.getOrderKind() == Packets.OrderKind.Mobile
                                && !wait.isReported()) {
                            periodTerm = cfgLoader.getRc();
                        } else if (normal != null
                                && normal.getOrderKind() == Packets.OrderKind.Mobile
                                && !normal.isReported()) {
                            periodTerm = cfgLoader.getRc();
                        }

                        pollingPeriod(periodTerm);

                        if (p.getConfigurationVersion() > cfgLoader.getConfigurationVersion()) {
                            // 환경 설정 버전이 높을 경우 환경 설정을 요청한다
                            // 응답 후 파일로 저장하고 IP를 변경하도록 한다.
                            requestConfig(cfgLoader.getConfigurationVersion());
                        }

                        final int noticeCode = p.getNoticeCode();
                        if (noticeCode > 0) {
                            Timer timerNotice = new Timer();
                            timerNotice.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    NoticesPacket noticesPacket = PreferenceUtil.getNotice(context);
                                    if (noticesPacket != null && noticesPacket.getNoticeCode() == noticeCode) {
                                        // 기존 공지사항과 같은 공지사항이므로 서버에 요청하지 않는다.
                                        showNoticePopupActivity();
                                    } else {
                                        // 새로운 공지사항이 있는 경우이므로 공지사항 페이지를 보여준다.
                                        requestNotice();
                                    }
                                }
                            }, 1500);
                        }

                        if (PreferenceUtil.getWaitArea(context) != null) {
                            pollingCheckWaitRange(true);
                        }

                        if (p.isWaiting()
                                || (wait != null && !wait.isReported())
                                || (normal != null && !normal.isReported())) {
                            // 대기 배차가 있는 경우 대기 배차 고객 정보 페이지를 보여 준다.
                            // 일반 배차가 있는 경우에도 고객 정보 페이지를 보여 준다.
                            Timer timerWaiting = new Timer();
                            timerWaiting.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (PreferenceUtil.getWaitOrderInfo(context) != null) {
                                        WavResourcePlayer.getInstance(context).play(R.raw.voice_132);
                                    } else {
                                        WavResourcePlayer.getInstance(context).play(R.raw.voice_120);
                                    }
                                    if (serviceVacancyLight != null) {
                                        serviceVacancyLight.setReservation();
                                    }
                                    showPassengerPopupActivity();
                                }
                            }, 4000);
                        }

                        if (cfgLoader.isVacancyLight()) {
                            if (serviceVacancyLight != null) {
                                if (!serviceVacancyLight.isLaunched()) {
                                    requestRest(Packets.RestType.VacancyError);
                                    return;
                                }
                            }
                            requestRest(Packets.RestType.Vacancy);
                        } else {
                            if (serviceTachoMeter != null) {
                                if (!serviceTachoMeter.isLaunched()) {
                                    requestRest(Packets.RestType.TachoMeterError);
                                    return;
                                }
                            }
                            String deviceType = cfgLoader.getMeterDeviceType();
                            if ("금호".equals(deviceType)) {
                                requestRest(Packets.RestType.KumHo);
                            } else if ("한국".equals(deviceType)) {
                                requestRest(Packets.RestType.Hankook);
                            } else if ("광신".equals(deviceType)) {
                                requestRest(Packets.RestType.Kwangshin);
                            }
                        }
                    }
                }
                break;
                case Packets.SERVICE_CONFIG: { // 환경 설정 응답
                    cfgLoader.write((ServiceConfigPacket) response);

                    // 환경 설정 아이피가 변경 되었으므로 변경된 아이피로 접속 한다.
                    if (!networkManager.getIp().equals(cfgLoader.getCallServerIp())) {
                        networkManager.disconnect();
                        networkManager.connect(cfgLoader.getCallServerIp(), cfgLoader.getCallServerPort());
                    }
                }
                break;
                case Packets.NOTICES: { // 공지사항
                    NoticesPacket packet = (NoticesPacket) response;
                    PreferenceUtil.setNotice(context, packet);
                    showNoticePopupActivity();
                }
                break;
                case Packets.RESPONSE_PERIOD_SENDING: { // 주기 전송 응답
                    ResponsePeriodSendingPacket packet = (ResponsePeriodSendingPacket) response;
                    // 메시지 존재하는 경우
                    if (packet.hasMessage()) {
                        requestMessage();
                    }
                }
                break;
                case Packets.RESPONSE_REST: // 휴식/운행재개
                    Packets.RestType restType = ((ResponseRestPacket) response).getRestType();
                    if (restType != null && (restType == Packets.RestType.Rest
                            || restType == Packets.RestType.Working)) {
                        Fragment f = FragmentUtil.getTopFragment(supportFragmentManager);
                        if (f != null && f instanceof ServiceStatusFragment) {
                            ((ServiceStatusFragment) f).applyRestResult(restType);
                        }
                        ScenarioService.this.restType = restType;
                    }
                    break;
                case Packets.WAIT_PLACE_INFO: { // 대기지역 정보
                    WaitPlaceInfoPacket packet = (WaitPlaceInfoPacket) response;
                    Fragment f = FragmentUtil.getTopFragment(supportFragmentManager);
                    if (f != null && f instanceof WaitCallFragment) {
                        ((WaitCallFragment) f).apply(packet);
                    }
                }
                break;
                case Packets.RESPONSE_WAIT_DECISION: { // 대기결정응답
                    ResponseWaitDecisionPacket packet = (ResponseWaitDecisionPacket) response;
                    if (packet.getWaitProcType() == Packets.WaitProcType.Success) {
                        PreferenceUtil.setWaitArea(context, packet);
                        pollingCheckWaitRange(true);

                        Fragment f = FragmentUtil.getTopFragment(supportFragmentManager);
                        if (f != null && f instanceof WaitCallFragment) {
                            ((WaitCallFragment) f).successWait();
                        }
                    } else {
                        Fragment f = FragmentUtil.getTopFragment(supportFragmentManager);
                        if (f != null && f instanceof WaitCallFragment) {
                            ((WaitCallFragment) f).failWait();
                        }
                    }
                }
                break;
                case Packets.RESPONSE_WAIT_CANCEL: { // 대기취소응답
                    WavResourcePlayer.getInstance(context).play(R.raw.voice_140);
                    PreferenceUtil.clearWaitArea(context);
                    pollingCheckWaitRange(false);

                    Fragment f = FragmentUtil.getTopFragment(supportFragmentManager);
                    if (f != null && f instanceof WaitCallFragment) {
                        ((WaitCallFragment) f).cancelWait();
                    }
                }
                break;
                case Packets.WAIT_ORDER_INFO: { // 대기배차고객정보 응답
                    Fragment f = FragmentUtil.getTopFragment(supportFragmentManager);
                    WaitOrderInfoPacket resp = (WaitOrderInfoPacket) response;
                    if (resp != null && resp.getCallNumber() > 0) {
                        WavResourcePlayer.getInstance(context).play(R.raw.voice_132);
                        if (serviceVacancyLight != null) {
                            serviceVacancyLight.setReservation();
                        }
                        // 서버에 Packet을 한번 요청하면 데이터가 초기화 되기 때문에 콜번호가 유효한 경우에만 저장을 한다.
                        PreferenceUtil.setWaitOrderInfo(context, resp);

                        // 대기 배차 완료시 서버의 대기목록에서 빠지므로 로컬 파일 지우도록 한다.
                        PreferenceUtil.clearWaitArea(context);

                        requestAck(resp.getMessageType(), cfgLoader.getServiceNumber(), resp.getCallNumber());

                        if (f != null && f instanceof PassengerInfoFragment) {
                            ((PassengerInfoFragment) f).applySuccessOrder();
                        }
                        showPassengerPopupActivity();

                        LogHelper.write("#### 콜 수락(대기) -> callNo : " + resp.getCallNumber());
                    } else {
                        if (f != null && f instanceof PassengerInfoFragment) {
                            ((PassengerInfoFragment) f).applyFailOrder(boardType);
                        }
                    }
                }
                break;
                case Packets.RESPONSE_SERVICE_REPORT: { // 운행보고 응답
                    pollingHandler.removeMessages(MSG_REPORT);

                    ResponseServiceReportPacket packet = (ResponseServiceReportPacket) response;
                    Packets.ReportKind reportKind = packet.getReportKind();
                    if (reportKind == Packets.ReportKind.GetOff
                            || reportKind == Packets.ReportKind.Failed) {
                        if (serviceVacancyLight != null) {
                            // 빈차등이 회사별로 다르기 때문에 예약과 빈차를 같이 보내야 정상 처리 된다.
                            if (packet.getReportKind() == Packets.ReportKind.Failed) {
                                serviceVacancyLight.setReservation();
                                WavResourcePlayer.getInstance(context).play(R.raw.voice_151);
                            }
                            serviceVacancyLight.setVacancy();
                        }

                        Activity act = ((MainApplication) getApplication()).getActivity(PassengerInfoPopupActivity.class);
                        if (act != null) {
                            act.finish();
                        }
                        Fragment f = FragmentUtil.getTopFragment(supportFragmentManager);
                        if (f != null && f instanceof PassengerInfoFragment) {
                            ((PassengerInfoFragment) f).applyCancelOrder();
                        }

                        refreshSavedPassengerInfo(packet.getCallNumber());
                        checkReservation();
                    }

                    if (reportKind == Packets.ReportKind.GetOn) {
                        LogHelper.write("#### 승차 보고 -> callNo : " + packet.getCallNumber());
                    } else if (reportKind == Packets.ReportKind.GetOff) {
                        LogHelper.write("#### 하차 보고 -> callNo : " + packet.getCallNumber());
                    } else if (reportKind == Packets.ReportKind.Failed) {
                        LogHelper.write("#### 탑승 실패 보고 -> callNo : " + packet.getCallNumber());
                    }
                }
                break;
                case Packets.RESPONSE_MESSAGE: { // 메시지 응답
                    WavResourcePlayer.getInstance(context).play(R.raw.voice_142);

                    ResponseMessagePacket packet = (ResponseMessagePacket) response;
                    List<String> messages = PreferenceUtil.getMessageList(context);
                    if (messages == null) {
                        messages = new ArrayList<>();
                    }

                    if (!TextUtils.isEmpty(packet.getMessage())) {
                        messages.add(0, packet.getMessage());
                    }
                    if (messages.size() > 5) {
                        PreferenceUtil.setMessageList(context, messages.subList(0, 5));
                    } else {
                        PreferenceUtil.setMessageList(context, messages);
                    }

                    Fragment f = FragmentUtil.getTopFragment(supportFragmentManager);
                    if (f != null && f instanceof MessageListFragment) {
                        ((MessageListFragment) f).refresh();
                    }

                    showMessagePopupActivity();
                }
                break;
                case Packets.ORDER_INFO: { // 배차데이터
                    if (emergencyType == Packets.EmergencyType.Begin) {
                        // 응급 상황 중이면 콜 수신을 무시한다.
                        LogHelper.d(">> Skip receive call broadcast.");
                        return;
                    }

                    OrderInfoPacket packet = (OrderInfoPacket) response;

                    if ((PreferenceUtil.getWaitOrderInfo(context) != null
                            || PreferenceUtil.getNormalCallInfo(context) != null)
                            && PreferenceUtil.getGetOnCallInfo(context) != null) {
                        // - 0x14 : 배차가 2개 이상인지 (일반배차가 있는 상태에서 또 일반배차가 내려 올 경우)
                        requestOrderRealtime(Packets.OrderDecisionType.MultipleOrder, packet);
                    } else if ((PreferenceUtil.getNormalCallInfo(context) != null
                            || PreferenceUtil.getWaitOrderInfo(context) != null)
                            && boardType == Packets.BoardType.Empty) {
                        // - 0x0D : 배차가 1개 일때 - 현재상태가 빈차일 경우 (운행보고 안함 : 콜받고 운행보고 안된상태에서 콜수신된경우)
                        requestOrderRealtime(Packets.OrderDecisionType.AlreadyOrderd, packet);
                    } else if (boardType == Packets.BoardType.Boarding
                            && packet.getOrderKind() == Packets.OrderKind.Normal) {
                        // - 0x0A : 주행중 일반콜 수신될 경우
                        requestOrderRealtime(Packets.OrderDecisionType.Driving, packet);
                    } else if (PreferenceUtil.getWaitArea(context) != null
                            && packet.getOrderKind() == Packets.OrderKind.Normal) {
                        // - 0x0C : 대기배차 상태인데 일반콜 수신될 경우
                        requestOrderRealtime(Packets.OrderDecisionType.Waiting, packet);
                    } else {
                        WavResourcePlayer.getInstance(context).play(R.raw.voice_160_116);
                        PreferenceUtil.setTempCallInfo(context, packet);
                        showRequestOrderPopupActivity();
                    }
                }
                break;
                case Packets.ORDER_INFO_PROC: { // 배차데이터 처리
                    OrderInfoPacket tempPacket = PreferenceUtil.getTempCallInfo(context);
                    OrderInfoProcPacket p = (OrderInfoProcPacket) response;
                    boolean isFailed;
                    if (tempPacket == null || p.getOrderProcType() != Packets.OrderProcType.Display) {
                        isFailed = true;
                    } else {
                        if (tempPacket.getCallNumber() != p.getCallNumber()) {
                            // 실시간 위치 및 배차요청으로 올린 콜넘버와 응답의 콜넘버가 다른 겨우 서비스 넘버 97로 ACK
                            requestAck(Packets.ORDER_INFO_PROC, 97, p.getCallNumber());
                            isFailed = true;
                        } else if (tempPacket.getCarId() != p.getCarId()) {
                            // 실시간 위치 및 배차요청으로 올린 콜ID와 응답의 콜ID가 다른 겨우 서비스 넘버 98로 ACK
                            requestAck(Packets.ORDER_INFO_PROC, 98, p.getCallNumber());
                            isFailed = true;
                        } else if (tempPacket.getOrderKind() != Packets.OrderKind.GetOnOrder
                                && boardType == Packets.BoardType.Boarding) {
                            // 승차 중 인데 승차 중 배차가 아니면
                            requestAck(Packets.ORDER_INFO_PROC, 99, p.getCallNumber());
                            isFailed = true;
                        } else {
                            isFailed = false;
                        }
                    }

                    if (isFailed) {
                        WavResourcePlayer.getInstance(context).play(R.raw.voice_122);
                        PreferenceUtil.clearTempCallInfo(context);
                    } else {
                        if (tempPacket.getOrderKind() == Packets.OrderKind.GetOnOrder) {
                            PreferenceUtil.setGetOnCallInfo(context, tempPacket);
                            LogHelper.write("#### 콜 수락(승차중배차) -> callNo : " + tempPacket.getCallNumber());
                        } else {
                            PreferenceUtil.setNormalCallInfo(context, tempPacket);
                            LogHelper.write("#### 콜 수락(일반) -> callNo : " + tempPacket.getCallNumber());
                        }

                        // 모바일 배차 완료시 주기전송 간격을 8초로 변경 한다.
                        // 승차신호가 올라오면 정상 주기로 다시 변경 한다.
                        if (tempPacket.getOrderKind() == Packets.OrderKind.Mobile) {
                            periodTerm = cfgLoader.getRc();
                            pollingPeriod(periodTerm);
                        }

                        if (boardType != Packets.BoardType.Boarding) {
                            if (serviceVacancyLight != null) {
                                serviceVacancyLight.setReservation();
                            }
                            // 주행 중이 아닐 때는 고객 정보 창에서 음성이 나온다.
                            showPassengerPopupActivity();
                        } else {
                            WavResourcePlayer.getInstance(context).play(R.raw.voice_120);
                        }
                        requestAck(Packets.ORDER_INFO_PROC, cfgLoader.getServiceNumber(), p.getCallNumber());

                        PreferenceUtil.clearTempCallInfo(context);
                    }
                }
                break;
                case Packets.RESPONSE_ACK: // 접속종료 (ACK의 응답)
                    pollingHandler.removeMessages(MSG_ACK);
                    break;
                case Packets.CALLER_INFO_RESEND: { // 고객정보재전송 응답
                    CallerInfoResendPacket p = (CallerInfoResendPacket) response;

                    OrderInfoPacket tempPacket = PreferenceUtil.getTempCallInfo(context);
                    // 임시 저장 패킷이 없는 경우 배차데이터 처리(1314)까지는 완료가 되었다는 의미이므로
                    // 고객 정보가 있는 경우 고객 정보 팝업을 보여준다.
                    if (tempPacket == null) {
                        requestAck(Packets.CALLER_INFO_RESEND, cfgLoader.getServiceNumber(), p.getCallNumber());

                        if (p.getOrderKind() == Packets.OrderKind.GetOnOrder) {
                            OrderInfoPacket getOn = PreferenceUtil.getGetOnCallInfo(context);
                            if (getOn != null && getOn.getCallNumber() == p.getCallNumber()) {
                                showPassengerPopupActivity();
                            } else {
                                OrderInfoPacket orderInfo = new OrderInfoPacket(p);
                                PreferenceUtil.setGetOnCallInfo(context, orderInfo);
                                showPassengerPopupActivity();
                            }
                        } else {
                            OrderInfoPacket normal = PreferenceUtil.getNormalCallInfo(context);
                            if (normal != null && normal.getCallNumber() == p.getCallNumber()) {
                                showPassengerPopupActivity();
                            } else {
                                OrderInfoPacket orderInfo = new OrderInfoPacket(p);
                                PreferenceUtil.setNormalCallInfo(context, orderInfo);
                                showPassengerPopupActivity();
                            }
                        }
                    } else {
                        // 임시 저장 패킷이 남아 있는 경우 배차데이터 처리(1314)를 받지 못했다는 뜻이므로
                        // 1314 수신과 동일하게 처리 한다. (Packets.ORDER_INFO_PROC)

                        boolean isFailed;
                        if (tempPacket.getCallNumber() != p.getCallNumber()) {
                            // 실시간 위치 및 배차요청으로 올린 콜넘버와 응답의 콜넘버가 다른 겨우 서비스 넘버 97로 ACK
                            requestAck(Packets.CALLER_INFO_RESEND, 97, p.getCallNumber());
                            isFailed = true;
                        } else if (tempPacket.getCarId() != p.getCarId()) {
                            // 실시간 위치 및 배차요청으로 올린 콜ID와 응답의 콜ID가 다른 겨우 서비스 넘버 98로 ACK
                            requestAck(Packets.CALLER_INFO_RESEND, 98, p.getCallNumber());
                            isFailed = true;
                        } else if (tempPacket.getOrderKind() != Packets.OrderKind.GetOnOrder
                                && boardType == Packets.BoardType.Boarding) {
                            // 승차 중 인데 승차 중 배차가 아니면
                            requestAck(Packets.CALLER_INFO_RESEND, 99, p.getCallNumber());
                            isFailed = true;
                        } else {
                            isFailed = false;
                        }

                        if (isFailed) {
                            WavResourcePlayer.getInstance(context).play(R.raw.voice_122);
                            PreferenceUtil.clearTempCallInfo(context);
                        } else {
                            if (tempPacket.getOrderKind() == Packets.OrderKind.GetOnOrder) {
                                PreferenceUtil.setGetOnCallInfo(context, tempPacket);
                            } else {
                                PreferenceUtil.setNormalCallInfo(context, tempPacket);
                            }

                            // 모바일 배차 완료시 주기전송 간격을 8초로 변경 한다.
                            // 승차신호가 올라오면 정상 주기로 다시 변경 한다.
                            if (tempPacket.getOrderKind() == Packets.OrderKind.Mobile) {
                                periodTerm = cfgLoader.getRc();
                                pollingPeriod(periodTerm);
                            }

                            if (boardType != Packets.BoardType.Boarding) {
                                if (serviceVacancyLight != null) {
                                    serviceVacancyLight.setReservation();
                                }
                                // 주행 중이 아닐 때는 고객 정보 창에서 음성이 나온다.
                                showPassengerPopupActivity();
                            } else {
                                WavResourcePlayer.getInstance(context).play(R.raw.voice_120);
                            }

                            requestAck(Packets.CALLER_INFO_RESEND, cfgLoader.getServiceNumber(), p.getCallNumber());

                            PreferenceUtil.clearTempCallInfo(context);
                        }
                    }
                }
                break;
                case Packets.CANCEL_EMERGENCY: { // Emergency 응답
                    emergencyType = Packets.EmergencyType.End;
                    pollingHandler.removeMessages(MSG_EMERGENCY);

                    if (serviceEmergency != null) {
                        serviceEmergency.setEmergencyOff();
                    }
                    // 응급 상황 해제 후에 주기 전송을 재시작 한다.
                    periodTerm = cfgLoader.getPst();
                    WaitOrderInfoPacket wait = PreferenceUtil.getWaitOrderInfo(context);
                    OrderInfoPacket normal = PreferenceUtil.getNormalCallInfo(context);
                    if (wait != null
                            && wait.getOrderKind() == Packets.OrderKind.Mobile
                            && !wait.isReported()) {
                        periodTerm = cfgLoader.getRc();
                    } else if (normal != null
                            && normal.getOrderKind() == Packets.OrderKind.Mobile
                            && !normal.isReported()) {
                        periodTerm = cfgLoader.getRc();
                    }
                    pollingPeriod(periodTerm);
                }
                break;
                case Packets.RESPONSE_ACCOUNT: { // 콜정산정보 응답
                    ResponseAccountPacket p = (ResponseAccountPacket) response;
                    Fragment f = FragmentUtil.getTopFragment(supportFragmentManager);
                    if (f != null && f instanceof QueryCallDetailFragment) {
                        ((QueryCallDetailFragment) f).apply(p);
                    }
                }
                break;
            }
        }
    };

    //----------------------------------------------------------------------------------------
    // polling & timer
    //----------------------------------------------------------------------------------------
    private Handler pollingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (isDestroyed) {
                return;
            }

            switch (msg.what) {
                case MSG_PERIOD:
                    requestPeriod();
                    int period = msg.arg1;

                    Message msgNew = obtainMessage();
                    msgNew.what = MSG_PERIOD;
                    msgNew.arg1 = period;
                    sendMessageDelayed(msgNew, period * 1000);
                    break;
                case MSG_LIVE:
                    requestLive();
                    pollingHandler.sendEmptyMessageDelayed(MSG_LIVE, cfgLoader.getRt() * 1000);
                    break;
                case MSG_EMERGENCY:
                    requestEmergency();
                    sendEmptyMessageDelayed(MSG_EMERGENCY, cfgLoader.getEmergencyPeriodTime() * 1000);
                    break;
                case MSG_AREA_CHECK:
                    LogHelper.d(">> Wait Area : Search");
                    ResponseWaitDecisionPacket p = PreferenceUtil.getWaitArea(context);
                    LogHelper.d(">> Wait Area : Speed -> " + gpsHelper.getSpeed());
                    if (p != null && gpsHelper.getSpeed() > 5) {
                        float distance = getDistance(p.getLatitude(), p.getLongitude());
                        LogHelper.d(">> Wait Area : distance -> " + distance + ". range -> " + p.getWaitRange());
                        if (distance > p.getWaitRange()) {
                            LogHelper.d(">> Wait Area : Out of area");
                            removeMessages(MSG_AREA_CHECK);
                            requestWaitCancel(p.getWaitPlaceCode());
                            return;
                        }
                    }
                    sendEmptyMessageDelayed(MSG_AREA_CHECK, 5000);
                    break;
                case MSG_REPORT:
                    ServiceReportPacket sp = (ServiceReportPacket) msg.obj;
                    if (reportRetryCount >= 3) {
                        if (sp.getReportKind() == Packets.ReportKind.Failed) {
                            refreshSavedPassengerInfo(sp.getCallNumber());
                        }
                        removeMessages(MSG_REPORT);
                    } else {
                        request(sp);
                        reportRetryCount++;

                        Message newMsg = obtainMessage();
                        newMsg.what = MSG_REPORT;
                        newMsg.obj = sp;
                        sendMessageDelayed(newMsg, 5000);
                    }
                    break;
                case MSG_ACK:
                    if (ackRetryCount >= 3) {
                        removeMessages(MSG_ACK);
                    } else {
                        AckPacket packet = (AckPacket) msg.obj;
                        request(packet);
                        ackRetryCount++;

                        Message newMsg = obtainMessage();
                        newMsg.what = MSG_ACK;
                        newMsg.obj = packet;
                        sendMessageDelayed(newMsg, 5000);
                    }
                    break;
                case MSG_SERVICE_ACK:
                    LogHelper.d(">> Failed to certify in 3 sec.");
                    WavResourcePlayer.getInstance(context).play(R.raw.voice_103);
                    Fragment f = FragmentUtil.getTopFragment(supportFragmentManager);
                    if (f != null && f instanceof ServiceStatusFragment) {
                        ((ServiceStatusFragment) f).showErrorMessage(
                                context.getString(R.string.fail_connect_server));
                    }
                    break;
                case MSG_DEVICE_WATCH:
                    watchDevice();
                    sendEmptyMessageDelayed(MSG_DEVICE_WATCH, 1500);
                    break;
            }
        }
    };

}