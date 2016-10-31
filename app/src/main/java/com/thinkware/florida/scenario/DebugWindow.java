package com.thinkware.florida.scenario;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.thinkware.florida.BuildConfig;
import com.thinkware.florida.R;
import com.thinkware.florida.network.manager.ATCommandManager;
import com.thinkware.florida.network.manager.NetworkManager;
import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.server2mdt.OrderInfoPacket;
import com.thinkware.florida.network.packets.server2mdt.ResponseWaitDecisionPacket;
import com.thinkware.florida.network.packets.server2mdt.WaitOrderInfoPacket;
import com.thinkware.florida.service.ScenarioService;
import com.thinkware.florida.ui.LogFileActivity;
import com.thinkware.florida.ui.MainActivity;
import com.thinkware.florida.ui.MainApplication;
import com.thinkware.florida.utility.log.LogHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by zic325 on 2016. 9. 26..
 */

public class DebugWindow implements View.OnClickListener {

    private Context context;
    private ScenarioService service;
    private ConfigurationLoader cfgLoader;
    private WindowManager windowManager;
    private WindowManager.LayoutParams windowViewLayoutParams;
    private View rootView;
    private ScrollView scrollView;
    private TextView txtStatus;
    private View logFileListCont;
    private TextView titleView;
    private ListView listView;
    private TextView emptyView;
    private int appendCount;
    private float prevX, prevY;
    private File[] originFiles;
    private ArrayList<File> fileList;

    public DebugWindow(ScenarioService svc, ConfigurationLoader loader) {
        context = svc.getApplicationContext();
        service = svc;
        cfgLoader = loader;
    }

    public void show(boolean moveLogList) {
        windowViewLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                42, -24, // X, Y 좌표
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        windowViewLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;

        rootView = View.inflate(context, R.layout.view_debug, null);
        rootView.findViewById(R.id.moveArea).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: // 처음 위치를 기억해둔다.
                        prevX = event.getRawX();
                        prevY = event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float rawX = event.getRawX(); // 절대 X 좌표 값을 가져온다.
                        float rawY = event.getRawY(); // 절대 Y 좌표값을 가져온다.

                        // 이동한 위치에서 처음 위치를 빼서 이동한 거리를 구한다.
                        float x = rawX - prevX;
                        float y = rawY - prevY;

                        if (windowViewLayoutParams != null) {
                            windowViewLayoutParams.x += (int) x;
                            windowViewLayoutParams.y += (int) y;

                            windowManager.updateViewLayout(rootView, windowViewLayoutParams);
                        }

                        prevX = rawX;
                        prevY = rawY;
                        break;
                }
                return true;
            }
        });
        scrollView = (ScrollView) rootView.findViewById(R.id.scrollView);
        txtStatus = (TextView) rootView.findViewById(R.id.txt_status);
        logFileListCont = rootView.findViewById(R.id.logFileCont);
        titleView = (TextView) rootView.findViewById(R.id.logFileCount);
        listView = (ListView) rootView.findViewById(R.id.logList);
        emptyView = (TextView) rootView.findViewById(R.id.emptyLog);
        rootView.findViewById(R.id.logCopy).setOnClickListener(this);
        rootView.findViewById(R.id.logDelete).setOnClickListener(this);
        rootView.findViewById(R.id.logRefresh).setOnClickListener(this);
        rootView.findViewById(R.id.showInformation).setOnClickListener(this);
        rootView.findViewById(R.id.showLog).setOnClickListener(this);
        rootView.findViewById(R.id.modemInformation).setOnClickListener(this);
        rootView.findViewById(R.id.showGPSStatus).setOnClickListener(this);
        rootView.findViewById(R.id.clearOrders).setOnClickListener(this);
        rootView.findViewById(R.id.clearApplication).setOnClickListener(this);
        rootView.findViewById(R.id.restartApplication).setOnClickListener(this);
        rootView.findViewById(R.id.logFiles).setOnClickListener(this);
        rootView.findViewById(R.id.closeDebug).setOnClickListener(this);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(rootView, windowViewLayoutParams);

        if (moveLogList) {
            logFiles();
        } else {
            showInformation();
        }
    }

    public void destroy() {
        windowManager.removeViewImmediate(rootView);
        cancelTimer();
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        switch (vId) {
            case R.id.showInformation:
                showInformation();
                break;
            case R.id.showLog:
                showLog();
                break;
            case R.id.logFiles:
                saveModemLog();
                logFiles();
                handler.sendEmptyMessageDelayed(10, 1000);
                break;
            case R.id.logCopy:
                String internal = ((MainApplication) context.getApplicationContext()).getInternalDir();
                String external = ((MainApplication) context.getApplicationContext()).getExternalDir();

                if (internal.equals(external)) {
                    return;
                }

                File fExt = new File(external);
                if (!fExt.exists()) {
                    fExt.mkdirs();
                }

                File fInt = new File(internal);
                if (fInt != null && fInt.exists()) {
                    File[] files = fInt.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            try {
                                File output = new File(external + File.separator + f.getName());
                                if (!output.exists()) {
                                    output.createNewFile();
                                }

                                FileInputStream inputStream = new FileInputStream(f);
                                FileOutputStream outputStream = new FileOutputStream(output);
                                FileChannel fis = inputStream.getChannel();
                                FileChannel fos = outputStream.getChannel();

                                long size = fis.size();
                                fis.transferTo(0, size, fos);

                                fos.close();
                                fis.close();
                                outputStream.close();
                                inputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        Toast.makeText(context, "복사가 완료 되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.logDelete:
                if (originFiles != null) {
                    for (File f : originFiles) {
                        f.delete();
                    }
                    fileList.clear();
                    fileList = null;

                    originFiles = null;
                    loadFiles();
                }
                break;
            case R.id.logRefresh:
                loadFiles();
                break;
            case R.id.modemInformation:
                showModemInformation();
                break;
            case R.id.showGPSStatus:
                showGPSStatus();
                break;
            case R.id.clearOrders:
                clearOrders();
                break;
            case R.id.clearApplication:
                clearApplication();
                break;
            case R.id.restartApplication:
                restartApplication();
                break;
            case R.id.closeDebug:
                closeDebug();
                break;
        }
    }

    //---------------------------------------------------------------------------------------------
    // public
    //---------------------------------------------------------------------------------------------
    // 현재 정보 보기
    public void showInformation() {
        cancelTimer();
        txtStatus.setText("###### Application 정보 ######" + "\n");

        String apkVersion = "";
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            apkVersion = "Code " + pi.versionCode + " / Name " + pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        txtStatus.append("APK Version : " + apkVersion + " / " + (BuildConfig.DEBUG ? "DEBUG" : "RELEASE") + "\n");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        txtStatus.append("APK Build time : " + sdf.format(new Date(BuildConfig.TIMESTAMP)) + "\n");
        txtStatus.append("프로그램 Version : " + cfgLoader.getProgramVersion() + "\n");
        txtStatus.append("환경설정 Version : " + cfgLoader.getConfigurationVersion() + "\n");
        txtStatus.append("네트워크 상태 : " + (NetworkManager.getInstance().isAvailableNetwork(context) ? "사용 가능" : "사용 불가")
                + "  /  소켓 상태 : " + (NetworkManager.getInstance().isConnected() ? "연결됨" : "연결 안됨")
                + " (" + NetworkManager.getInstance().getIp() + ":" + NetworkManager.getInstance().getPort()
                + ")\n");
        if (service == null) {
            txtStatus.append("모뎀 번호 : 알 수 없음" + "\n");
            txtStatus.append("미터기 : 알 수 없음" + "\n");
            txtStatus.append("빈차등 : 알 수 없음" + "\n");
            txtStatus.append("Emergency : 알 수 없음" + "\n");
        } else {
            txtStatus.append("모뎀 번호 : " + (service.getModemNumber() == null ? "읽기 실패" : service.getModemNumber()) + "\n");
            String svcTacho = "";
            if (cfgLoader.isVacancyLight()) {
                svcTacho = "사용 안함";
            } else {
                if (service.getServiceTachoMeter() == null) {
                    svcTacho = "에러 (서비스 오류)";
                } else {
                    int error = service.getServiceTachoMeter().getServiceStatus();
                    svcTacho = service.getServiceStatus(error) + " " + (cfgLoader.isVacancyLight() ? "" : "(환경설정 미터기 사용)");
                }
            }
            txtStatus.append("미터기 : " + svcTacho + "\n");

            String svcVacancy = "";
            if (service.getServiceVacancyLight() == null) {
                svcVacancy = "에러 (서비스 오류)";
            } else {
                int error = service.getServiceVacancyLight().getServiceStatus();
                svcVacancy = service.getServiceStatus(error) + " " + (cfgLoader.isVacancyLight() ? "(환경설정 빈차등 사용)" : "");
            }
            txtStatus.append("빈차등 : " + svcVacancy + "\n");

            String svcEmergency = "";
            if (service.getServiceEmergency() == null) {
                svcEmergency = "에러 (서비스 오류)";
            } else {
                int error = service.getServiceEmergency().getServiceStatus();
                svcEmergency = service.getServiceStatus(error);
            }
            txtStatus.append("Emergency : " + svcEmergency + "\n");
        }
        String ports = "";
        File dev = new File(File.separator + "dev");
        if (dev != null && dev.exists()) {
            File[] files = dev.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().startsWith("ttyUSB")) {
                        ports += f.getName() + " / ";
                    }
                }
            }

            if (TextUtils.isEmpty(ports)) {
                ports = "없음";
            } else {
                int index = ports.lastIndexOf("/");
                if (index > 0) {
                    ports = ports.substring(0, index);
                }
            }
        }
        txtStatus.append("Mounted : " + ports + "\n");
        txtStatus.append("\n");

        txtStatus.append("###### 승차/배차 정보 ######" + "\n");
        if (service == null) {
            txtStatus.append("시나리오 서비스 초기화 안됨" + "\n");
        } else {
            txtStatus.append("환경설정 파일 유무 : " + (cfgLoader.hasConfiguration() ? "있음" : "없음") + "\n");
            txtStatus.append("서비스 인증 여부 : " + (service.hasCertification() ? "인증됨" : "미인증") + "\n");
            txtStatus.append("운행/휴식 상태 : " + (service.getRestType() == Packets.RestType.Rest ? "휴식" : "운행") + "\n");
            txtStatus.append("승/빈차 상태 : " + (service.getBoardType() == Packets.BoardType.Boarding ? "승차 중" : "빈차") + "\n");
            txtStatus.append("긴급상황 상태 : " + (service.getEmergencyType() == Packets.EmergencyType.Begin ? "긴급상황 중" : "긴급상황 아님") + "\n");

            String waitArea = "";
            if (PreferenceUtil.getWaitArea(context) == null) {
                waitArea = "대기상태 아님";
            } else {
                ResponseWaitDecisionPacket p = PreferenceUtil.getWaitArea(context);
                waitArea = "대기상태(대기지역코드 : " + p.getWaitPlaceCode() + ")";
            }
            txtStatus.append("대기여부 : " + waitArea + "\n");

            String waitInfo = "";
            if (PreferenceUtil.getWaitOrderInfo(context) == null) {
                waitInfo = "대기 배차 없음";
            } else {
                WaitOrderInfoPacket p = PreferenceUtil.getWaitOrderInfo(context);
                waitInfo = "대기 배차 있음" + "\n"
                        + "----> 콜번호 : " + p.getCallNumber() + "\n"
                        + "----> 고객연락처 : " + p.getCallerPhone() + "\n"
                        + "----> 콜접수일자 : " + p.getCallReceiptDate() + "\n"
                        + "----> 장소 : " + p.getPlace() + "\n"
                        + "----> 배차횟수 : " + p.getOrderCount();
            }
            txtStatus.append("대기 배차 상태 : " + waitInfo + "\n");

            String normalInfo = "";
            if (PreferenceUtil.getNormalCallInfo(context) == null) {
                normalInfo = "배차1 없음";
            } else {
                OrderInfoPacket p = PreferenceUtil.getNormalCallInfo(context);
                normalInfo = "배차1 있음" + "\n"
                        + "----> 콜번호 : " + p.getCallNumber() + "\n"
                        + "----> 고객연락처 : " + p.getCallerPhone() + "\n"
                        + "----> 콜접수일자 : " + p.getCallReceiptDate() + "\n"
                        + "----> 장소 : " + p.getPlace() + "\n"
                        + "----> 배차횟수 : " + p.getOrderCount();
            }
            txtStatus.append("배차1 상태 : " + normalInfo + "\n");

            String getOn = "";
            if (PreferenceUtil.getGetOnCallInfo(context) == null) {
                getOn = "배차2 없음";
            } else {
                OrderInfoPacket p = PreferenceUtil.getGetOnCallInfo(context);
                getOn = "배차2 있음" + "\n"
                        + "----> 콜번호 : " + p.getCallNumber() + "\n"
                        + "----> 고객연락처 : " + p.getCallerPhone() + "\n"
                        + "----> 콜접수일자 : " + p.getCallReceiptDate() + "\n"
                        + "----> 장소 : " + p.getPlace() + "\n"
                        + "----> 배차횟수 : " + p.getOrderCount();
            }
            txtStatus.append("배차2 상태 : " + getOn + "\n");
        }
    }

    // 로그 보기
    public void showLog() {
        cancelTimer();
        txtStatus.setText("");
        LogHelper.applyViewHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (appendCount > 500) {
                    appendCount = 0;
                    txtStatus.setText("");
                }
                txtStatus.append(((String) msg.obj) + "\n");
                //scrollView.fullScroll(View.FOCUS_DOWN);
                appendCount++;
            }
        });
    }

    // 로그 파일 보기
    public void logFiles() {
        cancelTimer();

        logFileListCont.setVisibility(View.VISIBLE);
        loadFiles();
    }

    // GPS 상태 보기
    public void showGPSStatus() {
        cancelTimer();
        txtStatus.setText("");
        if (service != null && service.getGpsHelper() != null) {
            handler.sendEmptyMessage(0);
        }
    }

    // 배차 정보 초기화
    public void clearOrders() {
        cancelTimer();
        PreferenceUtil.clearTempCallInfo(context);
        PreferenceUtil.clearGetOnCallInfo(context);
        PreferenceUtil.clearNormalCallInfo(context);
        PreferenceUtil.clearWaitOrderInfo(context);
        PreferenceUtil.clearWaitArea(context);
        txtStatus.setText("데이터 삭제 완료!");
    }

    // 앱 초기화
    public void clearApplication() {
        cancelTimer();
        File cache = context.getCacheDir();
        try {
            File appDir = new File(cache.getParent());
            if (appDir.exists()) {
                String[] children = appDir.list();
                for (String s : children) {
                    if (!s.equals("lib")) {
                        deleteDir(new File(appDir, s));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        txtStatus.setText("초기화 완료!");
    }

    // 앱 종료 후 재시작
    public void restartApplication() {
        cancelTimer();
        if (service != null) {
            service.reset();
        }

        Activity act = ((MainApplication) context).getActivity(MainActivity.class);
        if (act != null) {
            act.finish();
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC, System.currentTimeMillis() + 2000,
                PendingIntent.getActivity(context, 0, intent, 0));
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    // 창 닫기
    public void closeDebug() {
        cancelTimer();
        destroy();
    }

    //---------------------------------------------------------------------------------------------
    // private
    //---------------------------------------------------------------------------------------------
    private void cancelTimer() {
        appendCount = 0;
        handler.removeMessages(0);
        handler.removeMessages(10);
        LogHelper.applyViewHandler(null);
        logFileListCont.setVisibility(View.GONE);
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    private android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (appendCount > 500) {
                        appendCount = 0;
                        txtStatus.setText("");
                    }
                    GpsHelper helper = service.getGpsHelper();
                    String addr = helper.getAddress();
                    final String info = "위도 : " + helper.getLatitude()
                            + ", 경도 : " + helper.getLongitude()
                            + ", 속도 : " + helper.getSpeed()
                            + ", 방향 : " + helper.getBearing()
                            + ", 시간 : " + helper.getTime()
                            + ", 주소 : " + (TextUtils.isEmpty(addr) ? "확인불가" : addr) + "\n";
                    txtStatus.append(info);
                    scrollView.fullScroll(View.FOCUS_DOWN);
                    sendEmptyMessageDelayed(0, 1000);
                    appendCount++;
                    break;
                case 10:
                    loadFiles();
                    break;
            }
        }
    };

    private void loadFiles() {
        String rootPath = ((MainApplication) context.getApplicationContext()).getInternalDir();
        File root = new File(rootPath);
        if (root == null || !root.exists()) {
            listView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            fileList = new ArrayList<>();
            originFiles = root.listFiles();
            if (originFiles != null) {
                for (File f : originFiles) {
                    if (f.getName().endsWith(".txt")) {
                        fileList.add(f);
                    }
                }
            }
            Collections.sort(fileList, new DebugWindow.ModifiedDate());

            if (fileList == null || fileList.size() == 0) {
                titleView.setText("로그 (0)");
                listView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                titleView.setText("로그 (" + fileList.size() + ")");
                listView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);

                DebugWindow.LogFileAdapter adapter = new DebugWindow.LogFileAdapter(fileList);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(context, LogFileActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("fileName", fileList.get(position).getAbsolutePath());
                        context.startActivity(intent);
                        closeDebug();
                    }
                });
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 모뎀 부팅 로그를 파일로 저장 한다.
     */
    private void saveModemLog() {
        if (NetworkManager.getInstance().isAvailableNetwork(context)) {
            String path = ((MainApplication) context.getApplicationContext()).getInternalDir();
            File file = new File(path + File.separator + "Modem-boot-log.txt");
            if (file.exists()) {
                file.delete();
            }

            ATCommandManager.getInstance().request(
                    ATCommandManager.CMD_BOOT_LOG,
                    new ATCommandManager.IModemListener() {
                        @Override
                        public void onModemResult(String result) {
                            String path = ((MainApplication) context.getApplicationContext()).getInternalDir();
                            File directory = new File(path);
                            if (!directory.exists()) {
                                directory.mkdirs();
                            }

                            try {
                                File file = new File(directory, "Modem-boot-log.txt");
                                if (!file.exists()) {
                                    file.createNewFile();
                                }
                                BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsolutePath(), true));
                                writer.write(result);
                                writer.newLine();
                                writer.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    private void showModemInformation() {
        cancelTimer();
        txtStatus.setText("");

        final ATCommandManager.IModemListener listener = new ATCommandManager.IModemListener() {
            @Override
            public void onModemResult(String result) {
                txtStatus.append(result + "\n");
            }
        };

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1000:
                        ATCommandManager.getInstance().request(
                                ATCommandManager.CMD_MODEM_NO, listener);
                        break;
                    case 1001:
                        ATCommandManager.getInstance().request(
                                ATCommandManager.CMD_VERSION, listener);
                        break;
                    case 1002:
                        ATCommandManager.getInstance().request(
                                ATCommandManager.CMD_USIM_NO, listener);
                        break;
                    case 1003:
                        ATCommandManager.getInstance().request(
                                ATCommandManager.CMD_USIM_STATE, listener);
                        break;
                    case 1004:
                        ATCommandManager.getInstance().request(
                                ATCommandManager.CMD_DEBUG_INFO, listener);
                        break;
//                    case 1005:
//                        ATCommandManager.getInstance().request(
//                                ATCommandManager.CMD_BOOT_LOG, listener);
//                        break;
                    default:
                        break;
                }
            }
        };
        handler.sendEmptyMessage(1000);
        handler.sendEmptyMessageDelayed(1001, 1500);
        handler.sendEmptyMessageDelayed(1002, 3000);
        handler.sendEmptyMessageDelayed(1003, 4500);
        handler.sendEmptyMessageDelayed(1004, 6000);
//        handler.sendEmptyMessageDelayed(1005, 7500);

    }

    private class ModifiedDate implements Comparator<File> {

        public int compare(File f1, File f2) {
            if (f1.lastModified() < f2.lastModified()) {
                return 1;
            } else if (f1.lastModified() == f2.lastModified()) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    private class LogFileAdapter extends BaseAdapter {

        private ArrayList<File> files;
        private SimpleDateFormat formatter;

        public LogFileAdapter(ArrayList<File> files) {
            this.files = files;
            this.formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        }

        @Override
        public int getCount() {
            return files.size();
        }

        @Override
        public Object getItem(int position) {
            return files.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DebugWindow.LogFileAdapter.LogHolder holder;
            if (convertView == null) {
                holder = new DebugWindow.LogFileAdapter.LogHolder();
                convertView = View.inflate(parent.getContext(), R.layout.view_log_list_item, null);
                holder.tvName = (TextView) convertView.findViewById(R.id.fileName);
                holder.tvDate = (TextView) convertView.findViewById(R.id.fileDate);
                holder.tvSize = (TextView) convertView.findViewById(R.id.fileSize);
                convertView.setTag(holder);
            } else {
                holder = (DebugWindow.LogFileAdapter.LogHolder) convertView.getTag();
            }
            File f = files.get(position);
            holder.tvName.setText(f.getName());
            holder.tvDate.setText(formatter.format(new Date(f.lastModified())));
            holder.tvSize.setText(NumberFormat.getInstance().format(f.length()) + " bytes");
            return convertView;
        }

        class LogHolder {
            TextView tvName;
            TextView tvDate;
            TextView tvSize;
        }
    }
}
