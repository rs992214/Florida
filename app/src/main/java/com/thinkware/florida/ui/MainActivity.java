package com.thinkware.florida.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;

import com.thinkware.florida.BuildConfig;
import com.thinkware.florida.R;
import com.thinkware.florida.external.TachoMeterType;
import com.thinkware.florida.external.service.EmergencyService;
import com.thinkware.florida.external.service.IEmergency;
import com.thinkware.florida.external.service.IEmergencyCallback;
import com.thinkware.florida.external.service.ITachoMeter;
import com.thinkware.florida.external.service.ITachoMeterCallback;
import com.thinkware.florida.external.service.IVacancyLight;
import com.thinkware.florida.external.service.IVacancyLightCallback;
import com.thinkware.florida.external.service.TachoMeterService;
import com.thinkware.florida.external.service.VacancyLightService;
import com.thinkware.florida.external.service.data.EmergencyData;
import com.thinkware.florida.external.service.data.ServiceStatus;
import com.thinkware.florida.external.service.data.TachoMeterData;
import com.thinkware.florida.external.service.data.VacancyLightData;
import com.thinkware.florida.media.WavResourcePlayer;
import com.thinkware.florida.network.manager.ATCommandManager;
import com.thinkware.florida.network.manager.NetworkManager;
import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.scenario.ConfigurationLoader;
import com.thinkware.florida.scenario.INaviExecutor;
import com.thinkware.florida.scenario.PreferenceUtil;
import com.thinkware.florida.service.ScenarioService;
import com.thinkware.florida.ui.dialog.SingleLineDialog;
import com.thinkware.florida.ui.fragment.FragmentUtil;
import com.thinkware.florida.ui.fragment.MessageListFragment;
import com.thinkware.florida.ui.fragment.NoticeFragment;
import com.thinkware.florida.ui.fragment.PassengerInfoFragment;
import com.thinkware.florida.ui.fragment.QueryCallNumFragment;
import com.thinkware.florida.ui.fragment.ServiceManagementFragment;
import com.thinkware.florida.ui.fragment.ServiceStatusFragment;
import com.thinkware.florida.ui.fragment.WaitCallFragment;
import com.thinkware.florida.utility.log.LogHelper;

import static com.thinkware.florida.external.service.data.TachoMeterData.STATUS_CALL;
import static com.thinkware.florida.external.service.data.TachoMeterData.STATUS_DRIVING;
import static com.thinkware.florida.external.service.data.TachoMeterData.STATUS_EXTRA_CHARGE;
import static com.thinkware.florida.external.service.data.TachoMeterData.STATUS_PAYMENT;
import static com.thinkware.florida.external.service.data.TachoMeterData.STATUS_VACANCY;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    public static final int REQUEST_CODE_CONFIG = 1000;

    View menuService, menuQueryCall, menuReqWait, menuCallerInfo, menuNotice, menuMessage, menuConfig, menuExit;
    TextView txtCorpInfo, txtVersionInfo;

    private ConfigurationLoader cfgLoader;
    private ScenarioService scenarioService;
    private String modemNumber;
    private Packets.BoardType boardType = Packets.BoardType.Empty;
    private int debugCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogHelper.d("++ MainActivity.onCreate(savedInstanceState)");
        super.onCreate(savedInstanceState);

        LogHelper.write("#### Application 시작.");
        setContentView(R.layout.activity_main);

        menuService = findViewById(R.id.menu_servicemanage);
        menuQueryCall = findViewById(R.id.menu_querycallnum);
        menuReqWait = findViewById(R.id.menu_reqwait);
        menuCallerInfo = findViewById(R.id.menu_callerinfo);
        menuNotice = findViewById(R.id.menu_notice);
        menuMessage = findViewById(R.id.menu_message);
        menuConfig = findViewById(R.id.menu_config);
        menuExit = findViewById(R.id.menu_exit);

        menuService.setOnClickListener(this);
        menuQueryCall.setOnClickListener(this);
        menuReqWait.setOnClickListener(this);
        menuCallerInfo.setOnClickListener(this);
        menuNotice.setOnClickListener(this);
        menuMessage.setOnClickListener(this);
        menuConfig.setOnClickListener(this);
        menuExit.setOnClickListener(this);

        cfgLoader = ConfigurationLoader.getInstance();
        txtCorpInfo = (TextView) findViewById(R.id.corporation_type);
        txtVersionInfo = (TextView) findViewById(R.id.version_info);
        showVersionInfo();

        FragmentUtil.replace(getSupportFragmentManager(), new ServiceManagementFragment());

        callTrafficReport();
        callOTAUpgrade();

        if (NetworkManager.getInstance().isAvailableNetwork(this)) {
            requestModemNumber();
        }

        if (!hasConfiguration()) {
            return;
        }

        initialize();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindLocalServices();
        unbindCallbackServices();
        WavResourcePlayer.getInstance(this).release();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (scenarioService != null && getApplication() instanceof MainApplication) {
            // Background에서 Foreground로 돌아 올 때는 항상 인증관리가 먼저 보여져야 한다.
            if (((MainApplication) getApplication()).isReturnedForground()) {
                if (scenarioService.hasCertification()) {
                    FragmentUtil.replace(getSupportFragmentManager(), new ServiceStatusFragment());
                } else {
                    FragmentUtil.replace(getSupportFragmentManager(), new ServiceManagementFragment());
                }
            }
        }

    }

    @Override
    public void onClick(View view) {
        int resId = view.getId();

        debugCount = 0;

        if (scenarioService == null || !scenarioService.hasCertification()) {
            if (resId == R.id.menu_servicemanage
                    || resId == R.id.menu_config) {
                // 인증관리와 환경 설정은 인증 전에도 선택 되어야 한다.
                if (scenarioService == null && resId == R.id.menu_servicemanage) {
                    return;
                }
            } else {
                SingleLineDialog dialog = new SingleLineDialog(this,
                        getString(R.string.done),
                        getString(R.string.need_certify));
                dialog.show();
                return;
            }
        }
        switch (resId) {
            case R.id.menu_servicemanage: {
                if (scenarioService.hasCertification()) {
                    FragmentUtil.replace(getSupportFragmentManager(), new ServiceStatusFragment());
                } else {
                    FragmentUtil.replace(getSupportFragmentManager(), new ServiceManagementFragment());
                }
                break;
            }
            case R.id.menu_querycallnum: {
                FragmentUtil.replace(getSupportFragmentManager(), new QueryCallNumFragment());
                break;
            }
            case R.id.menu_reqwait: {
                // 승차중 또는 저장된 배차 정보가 있을 경우 대기 요청을 할 수 없다.
                if (scenarioService.getBoardType() == Packets.BoardType.Boarding
                        || PreferenceUtil.getWaitOrderInfo(MainActivity.this) != null
                        || PreferenceUtil.getNormalCallInfo(MainActivity.this) != null) {
                    SingleLineDialog dialog = new SingleLineDialog(this,
                            getString(R.string.done),
                            getString(R.string.cannot_query_wait));
                    dialog.show();
                } else {
                    FragmentUtil.replace(getSupportFragmentManager(), new WaitCallFragment());
                }
                break;
            }
            case R.id.menu_callerinfo: {
                FragmentUtil.replace(getSupportFragmentManager(), new PassengerInfoFragment());
                break;
            }
            case R.id.menu_notice: {
                FragmentUtil.replace(getSupportFragmentManager(), new NoticeFragment());
                break;
            }
            case R.id.menu_message: {
                FragmentUtil.replace(getSupportFragmentManager(), new MessageListFragment());
                break;
            }
            case R.id.menu_config: {
                startActivityForResult(new Intent(this, ConfigActivity.class), REQUEST_CODE_CONFIG);
                break;
            }
            case R.id.menu_exit: {
                INaviExecutor.run(this);
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogHelper.d(">> requestCode : " + requestCode + ", resultCode : " + resultCode + ", data : " + data);
        if (requestCode == REQUEST_CODE_CONFIG && resultCode == RESULT_OK) {
            reset();
            initialize();
        }
    }

    public void initialize() {
        bindLocalServices();
        bindCallbackServices();
        initScenarioService();

        showCorpInfo();
    }

    /**
     * 환경 설정 파일이 존재하는지 체크 한다.
     * 파일이 없다면 환경 설정 Activity를 실행 한다.
     *
     * @return 환경 설정 파일이 있으면 true, 없으면 false
     */
    public boolean hasConfiguration() {
        if (!cfgLoader.hasConfiguration()) {
            startActivityForResult(new Intent(this, ConfigActivity.class), REQUEST_CODE_CONFIG);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 모뎀 전화번호를 가져온다.
     */
    public void requestModemNumber() {
        modemNumber = "";
        ConfigurationLoader.getInstance().setModemNumber(modemNumber);
        if (scenarioService != null) {
            scenarioService.setModemNumber(modemNumber);
        }

        ATCommandManager.getInstance().request(
                ATCommandManager.CMD_MODEM_NO,
                new ATCommandManager.IModemListener() {
                    @Override
                    public void onModemResult(String result) {
                        if (result != null && !result.contains(ATCommandManager.CMD_MODEM_NO)) {
                            modemNumber = ATCommandManager.getInstance().parseModemNumber(result);
                            LogHelper.write("#### Get modem number(" + modemNumber + ")");
                            ConfigurationLoader.getInstance().setModemNumber(modemNumber);

                            if (scenarioService != null) {
                                scenarioService.setModemNumber(modemNumber);
                            }
                        }
                    }
                });
    }

    public String getModemNumber() {
        return modemNumber;
    }

    /**
     * 시나리오 서비스를 초기화 한다.
     * 빈차등, 미터기, 응급 서비스의 정확한 바인드 시점을 알 수 없으므로 Null 체크를 반드시 해야 한다.
     */
    private void initScenarioService() {
        if (scenarioService != null) {
            ((MainApplication) getApplication()).setScenarioService(scenarioService);

            scenarioService.setSupportFragmentManager(getSupportFragmentManager());
            scenarioService.setBoardType(boardType);
            if (localTachoMeterService != null) {
                scenarioService.setServiceTachoMeter(localTachoMeterService);
            }
            if (localVacancyLightService != null) {
                scenarioService.setServiceVacancyLight(localVacancyLightService);
            }
            if (localEmergencyService != null) {
                scenarioService.setServiceEmergency(localEmergencyService);
            }
            scenarioService.setModemNumber(modemNumber);
        }
    }

    /**
     * App을 초기 실행 상태로 변경 한다.
     * (환경 설정페이지에서 "수정" 버튼을 통해 환경설정이 바뀌었을 경우 사용)
     */
    private void reset() {
        LogHelper.d(">> Configuration changed. reset!");

        int type = TachoMeterType.getTachoMeterKey(cfgLoader.getMeterDeviceType());
        if (localTachoMeterService != null && type != -1) {
            localTachoMeterService.changeTachoMeterType(type);
        }

        if (localVacancyLightService != null) {
            localVacancyLightService.launchService();
        }

        if (scenarioService != null) {
            scenarioService.reset();
        }

        FragmentUtil.replace(getSupportFragmentManager(), new ServiceManagementFragment());
    }

    /**
     * 교통 정보 Service를 실행 한다.
     */
    private void callTrafficReport() {
        LogHelper.d(">> Call traffic report.");
        try {
            Intent i = new Intent();
            String pkg = "com.ntis.dongbunts.bstraffic";
            String cls = ".manager.BSService";
            i.setComponent(new ComponentName(pkg, pkg + cls));
            startService(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * OTA Updater 서비스를 실행 한다.
     */
    private void callOTAUpgrade() {
        LogHelper.write("#### Call OTA upgrade.");
        startService(new Intent("com.thinkware.florida.otaupdater.Updater"));
        
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//        String today = sdf.format(Calendar.getInstance().getTime());
//        String lastCalled = PreferenceUtil.getLastCalledOTA(this);
//        LogHelper.d(">> today = " + today + ", lastCalled = " + lastCalled);
//        if (TextUtils.isEmpty(lastCalled) || !today.equals(lastCalled)) {
//            LogHelper.d(">> Call OTA upgrade.");
//            startService(new Intent("com.thinkware.florida.otaupdater.Updater"));
//        } else {
//            LogHelper.d(">> Skip call OTA upgrade.");
//        }
//        PreferenceUtil.setLastCalledOTA(this, today);
    }

    private ServiceConnection scenarioConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            ScenarioService.LocalBinder binder = (ScenarioService.LocalBinder) service;
            scenarioService = binder.getService();
            initScenarioService();
        }

        public void onServiceDisconnected(ComponentName className) {
            scenarioService = null;
        }
    };

    //----------------------------------------------------------------------------------------------
    // 외부 기기 Service Bind - Begin
    //----------------------------------------------------------------------------------------------

    // tachometer local service
    private TachoMeterService localTachoMeterService;
    private ServiceConnection localTachoMeterConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            TachoMeterService.LocalBinder binder = (TachoMeterService.LocalBinder) service;
            localTachoMeterService = binder.getService();
            int type = TachoMeterType.getTachoMeterKey(cfgLoader.getMeterDeviceType());
            if (type != -1) {
                localTachoMeterService.setTachoMeterType(type);
                localTachoMeterService.launchService();
                if (localTachoMeterService != null) {
                    scenarioService.setServiceTachoMeter(localTachoMeterService);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            localTachoMeterService = null;
        }
    };

    // vacancy light local service
    private VacancyLightService localVacancyLightService;
    private ServiceConnection localVacancyLightConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            VacancyLightService.LocalBinder binder = (VacancyLightService.LocalBinder) service;
            localVacancyLightService = binder.getService();
            localVacancyLightService.launchService();
            if (scenarioService != null) {
                scenarioService.setServiceVacancyLight(localVacancyLightService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            localVacancyLightService = null;
        }
    };

    // emergency local service
    private EmergencyService localEmergencyService;
    private ServiceConnection localEmergencyConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            EmergencyService.LocalBinder binder = (EmergencyService.LocalBinder) service;
            localEmergencyService = binder.getService();
            localEmergencyService.launchService();
            if (scenarioService != null) {
                scenarioService.setServiceEmergency(localEmergencyService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            localEmergencyService = null;
        }
    };

    // tachometer callback service
    private ITachoMeter tachoMeterService;
    private ServiceConnection tachoMeterConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service != null) {
                tachoMeterService = ITachoMeter.Stub.asInterface(service);
                if (tachoMeterService != null) {
                    try {
                        tachoMeterService.registerCallback(tachoMeterCallback);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (tachoMeterService != null) {
                try {
                    tachoMeterService.unregisterCallback(tachoMeterCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                tachoMeterService = null;
            }
        }
    };

    private ITachoMeterCallback tachoMeterCallback = new ITachoMeterCallback.Stub() {

        @Override
        public void onReceive(TachoMeterData data) throws RemoteException {
            LogHelper.d("미터기 명령: 0x%x, 상태: %d, 요금: %d, 거리: %d",
                    data.getCommand(), data.getStatus(), data.getFare(), data.getMileage());

            int status = data.getStatus();
            if (!cfgLoader.isVacancyLight()
                    && scenarioService != null && scenarioService.hasCertification()) {
                if ((status & STATUS_VACANCY) > 0) { // 빈차
                    LogHelper.write("@@ 미터기 : 빈차 (STATUS_VACANCY)");
                } else if ((status & STATUS_DRIVING) > 0) { // 주행
                    LogHelper.write("@@ 미터기 : 주행 (STATUS_DRIVING)");
                } else if ((status & STATUS_EXTRA_CHARGE) > 0) { // 할증
                    LogHelper.write("@@ 미터기 : 할증 (STATUS_EXTRA_CHARGE)");
                } else if ((status & STATUS_PAYMENT) > 0) { // 지불
                    LogHelper.write("@@ 미터기 : 지불 (STATUS_PAYMENT)");
                } else if ((status & STATUS_CALL) > 0) { // 호출
                    LogHelper.write("@@ 미터기 : 호출 (STATUS_CALL)");
                }

                if ((status & STATUS_VACANCY) > 0) { // 빈차
                    boardType = Packets.BoardType.Empty;
                } else {
                    boardType = Packets.BoardType.Boarding;
                }

                if (boardType == Packets.BoardType.Boarding) {
                    scenarioService.applyDriving(data.getFare(), data.getMileage());
                } else if (boardType == Packets.BoardType.Empty) {
                    scenarioService.applyVacancy(data.getFare(), data.getMileage());
                }
            }

        }

        @Override
        public void onServiceStatus(int status) throws RemoteException {
            LogHelper.d(">> 미터기 서비스 상태: " + status);
            if (scenarioService != null) {
                LogHelper.write("#### 미터기 -> " + scenarioService.getServiceStatus(status));
            }
            if (status != ServiceStatus.NO_ERROR
                    && status != ServiceStatus.SERVICE_NOT_LAUNCHED
                    && status != ServiceStatus.SERVICE_LAUNCHED) {
                // 미터기 오류 발생시 휴식 패킷을 통해 오류를 리포트 한다.
                if (scenarioService != null) {
                    scenarioService.requestRest(Packets.RestType.TachoMeterError);
                }
                if (BuildConfig.DEBUG) {
                    Intent intent = new Intent(MainActivity.this, PopupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("MSG", getString(R.string.check_device_tachometer));
                    startActivity(intent);
                }
            }
        }
    };

    // Vacancy Light callback  service
    private IVacancyLight vacancyLightService;
    private ServiceConnection vacancyLightConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service != null) {
                vacancyLightService = IVacancyLight.Stub.asInterface(service);
                try {
                    vacancyLightService.registerCallback(vacancyLightCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (vacancyLightService != null) {
                try {
                    vacancyLightService.unregisterCallback(vacancyLightCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                vacancyLightService = null;
            }
        }
    };

    private IVacancyLightCallback vacancyLightCallback = new IVacancyLightCallback.Stub() {
        @Override
        public void onReceive(VacancyLightData data) throws RemoteException {
            LogHelper.d("빈차등 상태: %d", data.getStatus());

            // 광신 미터기에서 초기 시작시에 현재 상태를 올려주지 못한다.
            // 때문에 인증 전에는 빈차등에서 초기값(빈차/승차)를 읽어와서 세팅하도록 한다.
            int status = data.getStatus();
            if (cfgLoader.isVacancyLight()
                    || scenarioService == null || !scenarioService.hasCertification()) {
                switch (status) {
                    case VacancyLightData.VACANCY: // 빈차
                        LogHelper.write("@@ 빈차등 : 빈차 (VACANCY)");
                        boardType = Packets.BoardType.Empty;
                        break;
                    case VacancyLightData.RIDDEN: // 승차
                        LogHelper.write("@@ 빈차등 : 승차 (RIDDEN)");
                        boardType = Packets.BoardType.Boarding;
                        break;
                    case VacancyLightData.RESERVATION: // 예약
                        LogHelper.write("@@ 빈차등 : 예약 (RESERVATION)");
                        break;
                    case VacancyLightData.DAY_OFF: // 휴무
                        LogHelper.write("@@ 빈차등 : 휴무 (DAY_OFF)");
                        break;
                    default:
                        break;
                }

                if (scenarioService != null) {
                    if (boardType == Packets.BoardType.Boarding) {
                        scenarioService.applyDriving(0, 0);
                    } else if (boardType == Packets.BoardType.Empty) {
                        scenarioService.applyVacancy(0, 0);
                    }
                }
            }
        }

        @Override
        public void onServiceStatus(int status) throws RemoteException {
            LogHelper.d(">> 빈차등 서비스 상태: " + status);
            if (scenarioService != null) {
                LogHelper.write("#### 빈차등 -> " + scenarioService.getServiceStatus(status));
            }
            if (status != ServiceStatus.NO_ERROR
                    && status != ServiceStatus.SERVICE_NOT_LAUNCHED
                    && status != ServiceStatus.SERVICE_LAUNCHED) {
                // 빈차등 오류 발생시 휴식 패킷을 통해 오류를 리포트 한다.
                if (scenarioService != null) {
                    scenarioService.requestRest(Packets.RestType.VacancyError);
                }

                if (BuildConfig.DEBUG) {
                    Intent intent = new Intent(MainActivity.this, PopupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("MSG", getString(R.string.check_device_vacancylight));
                    startActivity(intent);
                }
            }
        }
    };


    // Emergency callback service
    private IEmergency emergencyService;
    private ServiceConnection emergencyConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service != null) {
                emergencyService = IEmergency.Stub.asInterface(service);
                try {
                    emergencyService.registerCallback(emergencyCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (emergencyService != null) {
                try {
                    emergencyService.unregisterCallback(emergencyCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                emergencyService = null;
            }
        }
    };

    private IEmergencyCallback emergencyCallback = new IEmergencyCallback.Stub() {

        @Override
        public void onReceive(EmergencyData data) throws RemoteException {
            LogHelper.d("emergencyCallback: %d", data.getStatus());
            if (data.getStatus() == EmergencyData.EMERGENCY_ON) {
                // 긴급요청을 눌렀을 때
                LogHelper.write("@@ Emergency : ON");
                if (scenarioService != null) {
                    scenarioService.enabledEmergency(true);
                }
            } else {
                LogHelper.write("@@ Emergency : OFF");
                if (scenarioService != null) {
                    scenarioService.enabledEmergency(false);
                }
            }
        }

        @Override
        public void onServiceStatus(int status) throws RemoteException {
            LogHelper.d("긴급 서비스 상태: %s", status);
        }
    };

    private void bindLocalServices() {
        if (scenarioService == null) {
            Intent intent = new Intent(getApplicationContext(), ScenarioService.class);
            bindService(intent, scenarioConnection, Context.BIND_AUTO_CREATE);
        }

        if (localTachoMeterService == null) {
            Intent intent = new Intent(getApplicationContext(), TachoMeterService.class);
            intent.putExtra("local.bind", 1);
            bindService(intent, localTachoMeterConnection, Context.BIND_AUTO_CREATE);
        }

        if (localVacancyLightService == null) {
            Intent intent = new Intent(getApplicationContext(), VacancyLightService.class);
            intent.putExtra("local.bind", 1);
            bindService(intent, localVacancyLightConnection, Context.BIND_AUTO_CREATE);
        }

        if (localEmergencyService == null) {
            Intent intent = new Intent(getApplicationContext(), EmergencyService.class);
            intent.putExtra("local.bind", 1);
            bindService(intent, localEmergencyConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void bindCallbackServices() {
        if (tachoMeterService == null) {
            Intent intent = new Intent(TachoMeterService.INTENT_ACTION);
            bindService(intent, tachoMeterConnection, Context.BIND_AUTO_CREATE);
        }

        if (vacancyLightService == null) {
            Intent intent = new Intent(VacancyLightService.INTENT_ACTION);
            bindService(intent, vacancyLightConnection, Context.BIND_AUTO_CREATE);
        }

        if (emergencyService == null) {
            Intent intent = new Intent(EmergencyService.INTENT_ACTION);
            bindService(intent, emergencyConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void unbindLocalServices() {
        if (scenarioService != null) {
            unbindService(scenarioConnection);
            scenarioService = null;
        }

        if (localTachoMeterService != null) {
            unbindService(localTachoMeterConnection);
            localTachoMeterService = null;
        }

        if (localVacancyLightService != null) {
            unbindService(localVacancyLightConnection);
            localVacancyLightService = null;
        }

        if (localEmergencyService != null) {
            unbindService(localEmergencyConnection);
            localEmergencyService = null;
        }
    }

    private void unbindCallbackServices() {
        if (tachoMeterService != null) {
            unbindService(tachoMeterConnection);
            tachoMeterService = null;
        }

        if (vacancyLightService != null) {
            unbindService(vacancyLightConnection);
            vacancyLightService = null;
        }

        if (emergencyService != null) {
            unbindService(emergencyConnection);
            emergencyService = null;
        }
    }

    //----------------------------------------------------------------------------------------------
    // 외부 기기 Service Bind - End
    //----------------------------------------------------------------------------------------------

    /**
     * 화면의 왼쪽 상단을 5회 클릭하면 Debug Window를 보여준다.
     */
    public void processDebug(View v) {
        if (!cfgLoader.hasConfiguration()) {
            startActivityForResult(new Intent(this, ConfigActivity.class), REQUEST_CODE_CONFIG);
            return;
        }

        debugCount++;
        if (debugCount > 5 && scenarioService != null) {
            debugCount = 0;
            scenarioService.showDebugWindow(false);
        }
    }

    /**
     * 화면의 오른쪽 상단에 개인/법인 구분을 표기한다.
     */
    private void showCorpInfo() {
        if (cfgLoader.isCorporation()) {
            txtCorpInfo.setText(getString(R.string.corporation));
        } else {
            txtCorpInfo.setText(getString(R.string.indivisual));
        }
    }

    /**
     * 화면의 오른쪽 하단에 버전 정보를 표기한다.
     */
    private void showVersionInfo() {
        if (txtVersionInfo != null) {
            try {
                PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
                txtVersionInfo.setText(getString(R.string.version) + " " + pi.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
