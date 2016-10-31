package com.thinkware.florida.scenario;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thinkware.florida.network.manager.NetworkManager;
import com.thinkware.florida.network.packets.server2mdt.ServiceConfigPacket;
import com.thinkware.florida.ui.MainApplication;
import com.thinkware.florida.utility.log.LogHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by zic325 on 2016. 9. 13..
 */
public class ConfigurationLoader {
    private static final ConfigurationLoader instance = new ConfigurationLoader();

    private Config configuration;
    private Context context;

    class Config {
        // SD 카드로 부터 읽어오는 환경설정 파일의 json과 필드명이 동일하므로 변수를 바꾸면 안된다.

        protected boolean initiated;
        protected boolean isLoaded;
        protected int serviceNumber; // 서비스 번호
        protected boolean isCorporation; // 개인/법인 여부
        protected int corporationCode; // 법인 코드
        protected int carId; // Car ID
        protected String driverPhoneNumber; // 운전자 번호(직접입력하는 번호)
        protected String modemNumber; // 모뎀 번호(단말 전화번호)
        protected int programVersion; // 프로그램 버전
        protected int configurationVersion; // 환경설정 버전
        protected String callServerIp; // 콜 서버 IP
        protected int callServerPort; // 콜 서버 PORT
        protected String updateServerIp; // 업데이트 서버 IP
        protected int updateServerPort; // 업데이트 서버 PORT
        protected int pst; // 주기 전송 시간
        protected int psd; // 주기 전송 거리 (
        protected int rc; // 모바일콜 배차 후 승차신호 올라올 때까지 8초 주기로 주기 전송.
        protected int rt; // 재시도 시간 (Live 패킷 전송 주기)
        protected int cvt; // 콜 방송 표기 시간
        protected boolean ls; // 로그 저장 여부
        protected boolean isVacancyLight; // 승빈차 신호를 빈차등으로 할지 미터기로 할지 true:빈차등, false:미터기
        protected String meterDeviceType; // 승빈차 신호를 미터기로 했을 경우 미터기 타입(금호, 한국MTS, 광신, EB통합미터)
        protected String password;
        protected int emergencyPeriodTime; // Emergency 주기 시간

        @Override
        public String toString() {
            return "Config{" +
                    "initiated=" + initiated +
                    ", isLoaded=" + isLoaded +
                    ", serviceNumber=" + serviceNumber +
                    ", isCorporation=" + isCorporation +
                    ", corporationCode=" + corporationCode +
                    ", carId=" + carId +
                    ", driverPhoneNumber='" + driverPhoneNumber + '\'' +
                    ", modemNumber='" + modemNumber + '\'' +
                    ", programVersion=" + programVersion +
                    ", configurationVersion=" + configurationVersion +
                    ", callServerIp='" + callServerIp + '\'' +
                    ", callServerPort=" + callServerPort +
                    ", updateServerIp='" + updateServerIp + '\'' +
                    ", updateServerPort=" + updateServerPort +
                    ", pst=" + pst +
                    ", psd=" + psd +
                    ", rc=" + rc +
                    ", rt=" + rt +
                    ", cvt=" + cvt +
                    ", ls=" + ls +
                    ", isVacancyLight=" + isVacancyLight +
                    ", meterDeviceType='" + meterDeviceType + '\'' +
                    ", password='" + password + '\'' +
                    ", emergencyPeriodTime=" + emergencyPeriodTime +
                    '}';
        }
    }

    private ConfigurationLoader() {
        // Activity와 서비스 각각 객체 생성시 환경 설정 정보 싱크가 맞지 않는 이슈가 있어
        // singletone으로 정의 한다.
    }

    public static ConfigurationLoader getInstance() {
        return instance;
    }

    public void initialize(Context context) {
        this.context = context;
        load();
    }

    public boolean hasConfiguration() {
        return configuration.initiated;
    }

    public void write(ServiceConfigPacket response) {
        configuration.carId = response.getCarId();
        configuration.configurationVersion = response.getVersion();
        configuration.pst = response.getPeriodSendingTime();
        configuration.psd = response.getPeriodSendingRange();
        configuration.rc = response.getRetryNumber();
        configuration.rt = response.getRetryTime();
        configuration.cvt = response.getCallAcceptanceTime();
        configuration.emergencyPeriodTime = response.getPeriodEmergency();
        boolean isLogging = configuration.ls;
        configuration.ls = response.isLogging();
        configuration.callServerIp = response.getCallServerIp();
        configuration.callServerPort = response.getCallServerPort();
        configuration.updateServerIp = response.getUpdateServerIp();
        configuration.updateServerPort = response.getUpdateServerPort();
        configuration.password = response.getPassword();
        save();

        if (isLogging != configuration.ls) {
            if (configuration.ls) {
                String rootPath = ((MainApplication) context.getApplicationContext()).getInternalDir();
                File directory = new File(rootPath);
                if (directory == null || !directory.exists()) {
                    directory.mkdirs();
                }
                LogHelper.enableWriteLogFile(directory.getAbsolutePath());
            } else {
                LogHelper.disableWriteLogFile();
            }
        }
    }

    private void load() {
        Gson gson = new GsonBuilder().create();

        String config = PreferenceUtil.getConfiguration(context);
        if (config.length() > 0) {
            configuration = gson.fromJson(config, Config.class);
            configuration.isLoaded = true;
            LogHelper.write("==> 환경설정 로드 : " + configuration);
        } else {
            // load default
            configuration = new Config();
            configuration.initiated = false;
            configuration.isLoaded = false;
            configuration.isVacancyLight = true;
            configuration.callServerIp = NetworkManager.IP_DEV;
            configuration.callServerPort = NetworkManager.PORT_DEV;
            configuration.updateServerIp = "183.99.72.173";
            configuration.updateServerPort = 3060;
            configuration.pst = 30;
            configuration.psd = 3000;
            configuration.rc = 7;
            configuration.rt = 7;
            configuration.cvt = 6;
            configuration.password = "0";
            configuration.configurationVersion = 0;
            configuration.emergencyPeriodTime = 10;
            configuration.isCorporation = false;
            configuration.ls = false;

            // SD 카드에 환경설정 파일이 존재한다면 파일에 기록된 정보로 덮어 쓴다.
            File f = new File("/storage/sdcard1/Configuration.txt");
            if (f != null && f.exists()) {
                loadFromSdCard(f, configuration);
            }
        }

        if (configuration.ls) {
            String rootPath = ((MainApplication) context.getApplicationContext()).getInternalDir();
            File directory = new File(rootPath);
            if (directory == null || !directory.exists()) {
                directory.mkdirs();
            }
            LogHelper.enableWriteLogFile(directory.getAbsolutePath());
        } else {
            LogHelper.disableWriteLogFile();
        }

        //내부적으로 사용되는 버전이다.(OTA는 Manifest의 Version Name을 사용한다.)
        // 201
        //  -> 초기 발행 버전
        // 202
        //  -> 홈화면 상태뷰 환경설정의 빈차등/미터기 설정값에 따라 동작하도록 처리
        //  -> 기기테스트 모뎀 번호 나오지 않는 이슈 수정
        //  -> 네비게이션 지도 화면 이외에서는 콜 아이콘 보여지지 않도록 처리
        //  -> 인증 받기 전 네트워크가 연결 되어 있지 않을 경우 네트워크 연결 될 때 까지 delay + retry 로직 추가
        //  -> 앱 실행 후 네트워크가 연결 되었을 때 인증 패킷에 모뎀 번호가 누락되는 이슈 수정
        //  -> 모뎀 전화번호가 없을 경우 빈값("")을 서비스 인증 패킷에 올려보내도록 수정
        //  -> 디버그 화면 모뎀 ATCommand 추가 : AT+ICCID, AT$$DSCREEN?, AT+CPIN?
        //  -> 모뎀 재부팅시 모뎀 부팅 로그 저장하지 않도록 변경
        configuration.programVersion = 202;
    }

    private void loadFromSdCard(File f, Config configuration) {
        StringBuffer sb = new StringBuffer();
        try {
            FileInputStream fis = new FileInputStream(f);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }
            reader.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        String json = sb.toString();
        if (!TextUtils.isEmpty(json)) {
            Config cfg = gson.fromJson(json, Config.class);
            if (cfg != null) {
                LogHelper.d("From SD Card : " + cfg);
                configuration.serviceNumber = cfg.serviceNumber;
                configuration.isCorporation = cfg.isCorporation;
                configuration.corporationCode = cfg.corporationCode;
                configuration.carId = cfg.carId;
                if (!TextUtils.isEmpty(cfg.driverPhoneNumber)) {
                    configuration.driverPhoneNumber = cfg.driverPhoneNumber;
                }
                if (!TextUtils.isEmpty(cfg.callServerIp)) {
                    configuration.callServerIp = cfg.callServerIp;
                }
                configuration.callServerPort = cfg.callServerPort;
                if (!TextUtils.isEmpty(cfg.updateServerIp)) {
                    configuration.updateServerIp = cfg.updateServerIp;
                }
                configuration.updateServerPort = cfg.updateServerPort;
                configuration.pst = cfg.pst;
                configuration.psd = cfg.psd;
                configuration.rc = cfg.rc;
                configuration.rt = cfg.rt;
                configuration.cvt = cfg.cvt;
                configuration.ls = cfg.ls;
                configuration.isVacancyLight = cfg.isVacancyLight;
                if (!TextUtils.isEmpty(cfg.meterDeviceType)) {
                    configuration.meterDeviceType = cfg.meterDeviceType;
                }
            }
        }
    }

    public void save() {
        Gson gson = new GsonBuilder().create();

        configuration.initiated = true;

        // 법인의 경우 전화번호를 저장하면 안된다.
        String driverNumber = "";
        if (configuration.isCorporation) {
            driverNumber = configuration.driverPhoneNumber;
            configuration.driverPhoneNumber = "";
        }
        String config = gson.toJson(configuration);
        PreferenceUtil.setConfiguration(context, config);
        LogHelper.write("==> 환경설정 저장 : " + configuration);

        if (configuration.isCorporation) {
            configuration.driverPhoneNumber = driverNumber;
        }
    }

    public boolean isLoaded() {
        return configuration.isLoaded;
    }

    public void setLoaded(boolean loaded) {
        configuration.isLoaded = loaded;
    }

    public int getServiceNumber() {
        return configuration.serviceNumber;
    }

    public void setServiceNumber(int serviceNumber) {
        configuration.serviceNumber = serviceNumber;
    }

    public boolean isCorporation() {
        return configuration.isCorporation;
    }

    public void setCorporation(boolean corporation) {
        configuration.isCorporation = corporation;
    }

    public int getCorportaionCode() {
        return configuration.corporationCode;
    }

    public void setCorportaionCode(int corportaionCode) {
        configuration.corporationCode = corportaionCode;
    }

    public int getCarId() {
        return configuration.carId;
    }

    public void setCarId(int carId) {
        configuration.carId = carId;
    }

    public String getDriverPhoneNumber() {
        return configuration.driverPhoneNumber;
    }

    public void setDriverPhoneNumber(String driverPhoneNumber) {
        configuration.driverPhoneNumber = driverPhoneNumber;
    }

    public String getModemNumber() {
        return configuration.modemNumber;
    }

    public void setModemNumber(String modemNumber) {
        configuration.modemNumber = modemNumber;

        // 동부교통앱에 공유할 모뎀번호는 별도의 preference에 저장
        SharedPreferences preferences = context.getSharedPreferences("ShareToOtherApp", Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ModemNumber", modemNumber);
        editor.commit();
    }

    public int getProgramVersion() {
        return configuration.programVersion;
    }

    public void setProgramVersion(int programVersion) {
        configuration.programVersion = programVersion;
    }

    public int getConfigurationVersion() {
        return configuration.configurationVersion;
    }

    public void setConfigurationVersion(int configurationVersion) {
        configuration.configurationVersion = configurationVersion;
    }

    public String getCallServerIp() {
        return configuration.callServerIp;
    }

    public void setCallServerIp(String callServerIp) {
        configuration.callServerIp = callServerIp;
    }

    public int getCallServerPort() {
        return configuration.callServerPort;
    }

    public void setCallServerPort(int callServerPort) {
        configuration.callServerPort = callServerPort;
    }

    public String getUpdateServerIp() {
        return configuration.updateServerIp;
    }

    public void setUpdateServerIp(String updateServerIp) {
        configuration.updateServerIp = updateServerIp;
    }

    public int getUpdateServerPort() {
        return configuration.updateServerPort;
    }

    public void setUpdateServerPort(int updateServerPort) {
        configuration.updateServerPort = updateServerPort;
    }

    public int getPst() {
        return configuration.pst;
    }

    public void setPst(int pst) {
        configuration.pst = pst;
    }

    public int getPsd() {
        return configuration.psd;
    }

    public void setPsd(int psd) {
        configuration.psd = psd;
    }

    public int getRc() {
        return configuration.rc;
    }

    public void setRc(int rc) {
        configuration.rc = rc;
    }

    public int getRt() {
        return configuration.rt;
    }

    public void setRt(int rt) {
        configuration.rt = rt;
    }

    public int getCvt() {
        return configuration.cvt;
    }

    public void setCvt(int cvt) {
        configuration.cvt = cvt;
    }

    public boolean isLs() {
        return configuration.ls;
    }

    public void setLs(boolean ls) {
        configuration.ls = ls;
    }

    public boolean isVacancyLight() {
        return configuration.isVacancyLight;
    }

    public void setVacancyLight(boolean boardingSignal) {
        configuration.isVacancyLight = boardingSignal;
    }

    public String getMeterDeviceType() {
        return configuration.meterDeviceType;
    }

    public void setMeterDeviceType(String meterDeviceType) {
        configuration.meterDeviceType = meterDeviceType;
    }

    public String getPassword() {
        return configuration.password;
    }

    public void setPassword(String password) {
        configuration.password = password;
    }

    public int getEmergencyPeriodTime() {
        return configuration.emergencyPeriodTime;
    }

    public void setEmergencyPeriodTime(int time) {
        configuration.emergencyPeriodTime = time;
    }
}
