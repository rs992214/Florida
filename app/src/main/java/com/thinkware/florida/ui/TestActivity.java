package com.thinkware.florida.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;

import com.thinkware.florida.R;
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
import com.thinkware.florida.network.manager.ATCommandManager;
import com.thinkware.florida.utility.log.LogHelper;

import java.util.Locale;

/**
 * Created by Mihoe on 2016-09-12.
 */
public class TestActivity extends BaseActivity {
    private View exit;
    private TextView txtMeter, txtLight, txtEmergency, txtModem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        bindCallbackServices();
        bindLocalServices();

        exit = findViewById(R.id.btn_exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        txtMeter = (TextView) findViewById(R.id.txt_meter_status);
        txtLight = (TextView) findViewById(R.id.txt_light_status);
        txtEmergency = (TextView) findViewById(R.id.txt_emergengy_status);
        txtModem = (TextView) findViewById(R.id.txt_modem_status);

        findViewById(R.id.btn_retry_tacho).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (localTachoMeterService != null) {
                    localTachoMeterService.launchService(isAttachedUSB(MainActivity.USB_VENDOR_ID, MainActivity.USB_PRODUCT_ID));
                }
            }
        });

        findViewById(R.id.btn_reserve).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (localVacancyLightService != null) {
                    localVacancyLightService.setReservation();
                }
            }
        });

        findViewById(R.id.btn_reserve_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (localVacancyLightService != null) {
                    localVacancyLightService.setVacancy();
                }
            }
        });

        findViewById(R.id.btn_emergency_on).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (localEmergencyService != null) {
                    localEmergencyService.setEmergencyOn();
                }
            }
        });

        findViewById(R.id.btn_emergency_off).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (localEmergencyService != null) {
                    localEmergencyService.setEmergencyOff();
                }
            }
        });

        txtModem.setText("전화 번호 요청 중..");
        errorHandler.removeMessages(1000);
        errorHandler.sendEmptyMessageDelayed(1000, 3000);
        ATCommandManager.getInstance().request(
                ATCommandManager.CMD_MODEM_NO, modemListener);

        findViewById(R.id.btn_retry_modem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtModem.setText("전화 번호 요청 중..");
                errorHandler.removeMessages(1000);
                errorHandler.sendEmptyMessageDelayed(1000, 3000);
                ATCommandManager.getInstance().request(
                        ATCommandManager.CMD_MODEM_NO, modemListener);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindCallbackServices();
        unbindLocalServices();
    }

    private ATCommandManager.IModemListener modemListener = new ATCommandManager.IModemListener() {
        @Override
        public void onModemResult(String result) {
            if (result != null && !result.contains(ATCommandManager.CMD_MODEM_NO)) {
                txtModem.setText(ATCommandManager.getInstance().parseModemNumber(result));
                errorHandler.removeMessages(1000);
            }
        }
    };

    private Handler errorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    if (txtModem != null) {
                        txtModem.setText("전화번호 가져오기 실패");
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void setTachoMeterText(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtMeter.setText(msg);
            }
        });
    }

    private void setVacancyLightText(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtLight.setText(msg);
            }
        });
    }

    private void setEmergencyText(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtEmergency.setText(msg);
            }
        });
    }

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
            String msg = "";
            int serviceStatus = localTachoMeterService.getServiceStatus();
            if (serviceStatus == ServiceStatus.FAILED_SET_HANDLER) {
                msg = "오류: 핸들러 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_PORT_OPENED) {
                msg = "오류: 포트 열기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_PARSER) {
                msg = "오류: 파서 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_THREAD) {
                msg = "오류: 쓰레드 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_READ_DATA) {
                msg = "오류: 데이터 읽기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_WRITE_DATA) {
                msg = "오류: 데이터 쓰기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_QUEUE) {
                msg = "오류: 큐 생성 실패";
            } else if (serviceStatus == ServiceStatus.SERVICE_NOT_LAUNCHED) {
                msg = "서비스 시작 안됨";
            } else if (serviceStatus == ServiceStatus.SERVICE_LAUNCHED) {
                msg = "서비스 시작됨";
            } else {
                msg = "알수 없음";
            }

            setTachoMeterText(msg);
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

            String msg = "";
            int serviceStatus = localVacancyLightService.getServiceStatus();
            if (serviceStatus == ServiceStatus.FAILED_SET_HANDLER) {
                msg = "오류: 핸들러 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_PORT_OPENED) {
                msg = "오류: 포트 열기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_PARSER) {
                msg = "오류: 파서 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_THREAD) {
                msg = "오류: 쓰레드 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_READ_DATA) {
                msg = "오류: 데이터 읽기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_WRITE_DATA) {
                msg = "오류: 데이터 쓰기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_QUEUE) {
                msg = "오류: 큐 생성 실패";
            } else if (serviceStatus == ServiceStatus.SERVICE_NOT_LAUNCHED) {
                msg = "서비스 시작 안됨";
            } else if (serviceStatus == ServiceStatus.SERVICE_LAUNCHED) {
                msg = "서비스 시작됨";
            } else {
                msg = "알수 없음";
            }

            setVacancyLightText(msg);
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

            String msg = "";
            int serviceStatus = localEmergencyService.getServiceStatus();
            if (serviceStatus == ServiceStatus.FAILED_SET_HANDLER) {
                msg = "오류: 핸들러 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_PORT_OPENED) {
                msg = "오류: 포트 열기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_PARSER) {
                msg = "오류: 파서 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_THREAD) {
                msg = "오류: 쓰레드 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_READ_DATA) {
                msg = "오류: 데이터 읽기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_WRITE_DATA) {
                msg = "오류: 데이터 쓰기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_QUEUE) {
                msg = "오류: 큐 생성 실패";
            } else if (serviceStatus == ServiceStatus.SERVICE_NOT_LAUNCHED) {
                msg = "서비스 시작 안됨";
            } else if (serviceStatus == ServiceStatus.SERVICE_LAUNCHED) {
                msg = "서비스 시작됨";
            } else {
                msg = "알수 없음";
            }

            setEmergencyText(msg);
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
        }
    };

    private ITachoMeterCallback tachoMeterCallback = new ITachoMeterCallback.Stub() {

        @Override
        public void onReceive(TachoMeterData data) throws RemoteException {
            String msg = null;
            if (data != null) {
                msg = String.format(Locale.KOREA, "미터기 명령: 0x%x, 상태: %d, 요금: %d, 거리: %d",
                        data.getCommand(), data.getStatus(), data.getFare(), data.getMileage());
            } else {
                msg = "미터기 데이터가 null입니다.";
            }
            setTachoMeterText(msg);
            LogHelper.d(msg);
        }

        @Override
        public void onServiceStatus(int serviceStatus) throws RemoteException {
            LogHelper.d("미터기 서비스 상태: %s", serviceStatus);
            String msg = "";
            if (serviceStatus == ServiceStatus.FAILED_SET_HANDLER) {
                msg = "오류: 핸들러 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_PORT_OPENED) {
                msg = "오류: 포트 열기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_PARSER) {
                msg = "오류: 파서 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_THREAD) {
                msg = "오류: 쓰레드 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_READ_DATA) {
                msg = "오류: 데이터 읽기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_WRITE_DATA) {
                msg = "오류: 데이터 쓰기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_QUEUE) {
                msg = "오류: 큐 생성 실패";
            } else if (serviceStatus == ServiceStatus.SERVICE_NOT_LAUNCHED) {
                msg = "서비스 시작 안됨";
            } else if (serviceStatus == ServiceStatus.SERVICE_LAUNCHED) {
                msg = "서비스 시작됨";
            } else {
                msg = "알수 없음";
            }
            setTachoMeterText(msg);
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
        }
    };

    private IVacancyLightCallback vacancyLightCallback = new IVacancyLightCallback.Stub() {
        @Override
        public void onReceive(VacancyLightData data) throws RemoteException {
            String msg = null;
            if (data != null) {
                msg = String.format(Locale.KOREA, "빈차등 상태: %d", data.getStatus());
            } else {
                msg = "빈차등 상태 데이터가 null입니다.";
            }
            setVacancyLightText(msg);
            LogHelper.d(msg);
        }

        @Override
        public void onServiceStatus(int serviceStatus) throws RemoteException {
            LogHelper.d("빈차등 서비스 상태: %s", serviceStatus);
            String msg = "";
            if (serviceStatus == ServiceStatus.FAILED_SET_HANDLER) {
                msg = "오류: 핸들러 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_PORT_OPENED) {
                msg = "오류: 포트 열기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_PARSER) {
                msg = "오류: 파서 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_THREAD) {
                msg = "오류: 쓰레드 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_READ_DATA) {
                msg = "오류: 데이터 읽기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_WRITE_DATA) {
                msg = "오류: 데이터 쓰기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_QUEUE) {
                msg = "오류: 큐 생성 실패";
            } else if (serviceStatus == ServiceStatus.SERVICE_NOT_LAUNCHED) {
                msg = "서비스 시작 안됨";
            } else if (serviceStatus == ServiceStatus.SERVICE_LAUNCHED) {
                msg = "서비스 시작됨";
            } else {
                msg = "알수 없음";
            }
            setVacancyLightText(msg);
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
        }
    };

    private IEmergencyCallback emergencyCallback = new IEmergencyCallback.Stub() {

        @Override
        public void onReceive(EmergencyData data) throws RemoteException {
            String msg = null;
            if (data != null) {
                msg = String.format(Locale.KOREA, "긴급 상태: %d", data.getStatus());
            } else {
                msg = "긴급 상태 데이터가 null입니다.";
            }
            setEmergencyText(msg);
            LogHelper.d(msg);
        }

        @Override
        public void onServiceStatus(int serviceStatus) throws RemoteException {
            LogHelper.d("긴급 서비스 상태: %s", serviceStatus);
            String msg = "";
            if (serviceStatus == ServiceStatus.FAILED_SET_HANDLER) {
                msg = "오류: 핸들러 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_PORT_OPENED) {
                msg = "오류: 포트 열기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_PARSER) {
                msg = "오류: 파서 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_THREAD) {
                msg = "오류: 쓰레드 생성 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_READ_DATA) {
                msg = "오류: 데이터 읽기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_WRITE_DATA) {
                msg = "오류: 데이터 쓰기 실패";
            } else if (serviceStatus == ServiceStatus.FAILED_SET_QUEUE) {
                msg = "오류: 큐 생성 실패";
            } else if (serviceStatus == ServiceStatus.SERVICE_NOT_LAUNCHED) {
                msg = "서비스 시작 안됨";
            } else if (serviceStatus == ServiceStatus.SERVICE_LAUNCHED) {
                msg = "서비스 시작됨";
            } else {
                msg = "알수 없음";
            }
            setEmergencyText(msg);
        }
    };

    private void bindLocalServices() {
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
        if (localTachoMeterService != null) {
            unbindService(localTachoMeterConnection);
        }

        if (localVacancyLightService != null) {
            unbindService(localVacancyLightConnection);
        }

        if (localEmergencyService != null) {
            unbindService(localEmergencyConnection);
        }
    }

    private void unbindCallbackServices() {
        if (tachoMeterService != null) {
            try {
                tachoMeterService.unregisterCallback(tachoMeterCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            unbindService(tachoMeterConnection);
            tachoMeterService = null;
        }

        if (vacancyLightService != null) {
            try {
                vacancyLightService.unregisterCallback(vacancyLightCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            unbindService(vacancyLightConnection);
            vacancyLightService = null;
        }

        if (emergencyService != null) {
            try {
                emergencyService.unregisterCallback(emergencyCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            unbindService(emergencyConnection);
            emergencyService = null;
        }
    }

    //----------------------------------------------------------------------------------------------
    // 외부 기기 Service Bind - End
    //----------------------------------------------------------------------------------------------
}
