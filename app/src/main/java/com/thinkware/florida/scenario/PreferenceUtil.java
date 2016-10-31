package com.thinkware.florida.scenario;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thinkware.florida.utility.log.LogHelper;
import com.thinkware.florida.network.packets.server2mdt.NoticesPacket;
import com.thinkware.florida.network.packets.server2mdt.OrderInfoPacket;
import com.thinkware.florida.network.packets.server2mdt.ResponseWaitDecisionPacket;
import com.thinkware.florida.network.packets.server2mdt.WaitOrderInfoPacket;

import java.util.List;

/**
 * Created by zic325 on 2016. 9. 13..
 */
public class PreferenceUtil {

    //------------------------------------------------------------------------------------
    // fields
    //------------------------------------------------------------------------------------
    private static SharedPreferences preferences;

    //------------------------------------------------------------------------------------
    // public
    //------------------------------------------------------------------------------------
    /**
     * 공지사항 저장
     */
    public static void setNotice(Context context, NoticesPacket packet) {
        Gson gson = new Gson();
        String json = gson.toJson(packet);
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("NoticePacket", json);
        editor.commit();
    }

    /**
     * @return 공지사항
     */
    public static NoticesPacket getNotice(Context context) {
        Gson gson = new Gson();
        String json = getString(context, "NoticePacket");
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        return gson.fromJson(json, NoticesPacket.class);
    }

    /**
     * 메시지 목록 저장
     */
    public static void setMessageList(Context context, List<String> messages) {
        Gson gson = new Gson();
        String json = gson.toJson(messages);
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("MessageList", json);
        editor.commit();
    }

    /**
     * @return 메시지 목록
     */
    public static List<String> getMessageList(Context context) {
        Gson gson = new Gson();
        String json = getString(context, "MessageList");
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        return gson.fromJson(json, new TypeToken<List<String>>() {
        }.getType());
    }

    /**
     * 대기상태 저장
     */
    public static void setWaitArea(Context context, ResponseWaitDecisionPacket packet) {
        LogHelper.write("==> [대기상태 저장] : " + packet);
        Gson gson = new Gson();
        String json = gson.toJson(packet);
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("WaitAreaInfo", json);
        editor.commit();
    }

    /**
     * @return 대기상태
     */
    public static ResponseWaitDecisionPacket getWaitArea(Context context) {
        Gson gson = new Gson();
        String json = getString(context, "WaitAreaInfo");
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        return gson.fromJson(json, ResponseWaitDecisionPacket.class);
    }

    /**
     * 대기상태 삭제
     */
    public static void clearWaitArea(Context context) {
        LogHelper.write("==> [대기상태 삭제]");
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("WaitAreaInfo");
        editor.commit();
    }

    /**
     * 대기배차 정보 저장
     */
    public static void setWaitOrderInfo(Context context, WaitOrderInfoPacket packet) {
        LogHelper.write("==> [대기배차 정보 저장] : " + packet);
        Gson gson = new Gson();
        String json = gson.toJson(packet);
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("WaitOrderInfo", json);
        editor.commit();
    }

    /**
     * @return 대기배차 정보
     */
    public static WaitOrderInfoPacket getWaitOrderInfo(Context context) {
        Gson gson = new Gson();
        String json = getString(context, "WaitOrderInfo");
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        return gson.fromJson(json, WaitOrderInfoPacket.class);
    }

    /**
     * 대기배차 정보 삭제
     */
    public static void clearWaitOrderInfo(Context context) {
        LogHelper.write("==> [대기배차 정보 삭제]");
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("WaitOrderInfo");
        editor.commit();
    }

    /**
     * 임시배차 정보 저장
     */
    public static void setTempCallInfo(Context context, OrderInfoPacket packet) {
        if (packet == null) {
            return;
        }
        LogHelper.write("==> [임시배차 정보 저장] : " + packet);
        Gson gson = new Gson();
        String json = gson.toJson(packet);
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("TempCallInfo", json);
        editor.commit();
    }

    /**
     * @return 임시배차 정보
     */
    public static OrderInfoPacket getTempCallInfo(Context context) {
        Gson gson = new Gson();
        String json = getString(context, "TempCallInfo");
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        return gson.fromJson(json, OrderInfoPacket.class);
    }

    /**
     * 임시배차 정보 삭제
     */
    public static void clearTempCallInfo(Context context) {
        LogHelper.write("==> [임시배차 정보 삭제]");
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("TempCallInfo");
        editor.commit();
    }

    /**
     * 일반배차 정보 저장
     */
    public static void setNormalCallInfo(Context context, OrderInfoPacket packet) {
        if (packet == null) {
            return;
        }
        LogHelper.write("==> [배차1 정보 저장] : " + packet);
        Gson gson = new Gson();
        String json = gson.toJson(packet);
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("NormalCallInfo", json);
        editor.commit();
    }

    /**
     * @return 일반배차 정보
     */
    public static OrderInfoPacket getNormalCallInfo(Context context) {
        Gson gson = new Gson();
        String json = getString(context, "NormalCallInfo");
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        return gson.fromJson(json, OrderInfoPacket.class);
    }

    /**
     * 일반배차 정보 삭제
     */
    public static void clearNormalCallInfo(Context context) {
        LogHelper.write("==> [배차1 정보 삭제]");
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("NormalCallInfo");
        editor.commit();
    }

    /**
     * 승차중배차 정보 저장
     */
    public static void setGetOnCallInfo(Context context, OrderInfoPacket packet) {
        if (packet == null) {
            return;
        }
        LogHelper.write("==> [배차2 정보 저장] : " + packet);
        Gson gson = new Gson();
        String json = gson.toJson(packet);
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("GetOnCallInfo", json);
        editor.commit();
    }

    /**
     * @return 저장된 승차중배차 정보
     */
    public static OrderInfoPacket getGetOnCallInfo(Context context) {
        Gson gson = new Gson();
        String json = getString(context, "GetOnCallInfo");
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        return gson.fromJson(json, OrderInfoPacket.class);
    }

    /**
     * 승차중배차 정보 삭제
     */
    public static void clearGetOnCallInfo(Context context) {
        LogHelper.write("==> [배차2 정보 삭제]");
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("GetOnCallInfo");
        editor.commit();
    }

    /**
     * 설정 저장
     */
    public static void setConfiguration(Context context, String config) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Configuration", config);
        editor.commit();
    }

    /**
     * 설정 가져오기
     */
    public static String getConfiguration(Context context) {
        return getString(context, "Configuration");
    }

    /**
     * 마지막으로 OTA Updater를 호출한 날짜 저장
     * @param context
     * @param date 저장할 날짜 yyyyMMdd
     */
    public static void setLastCalledOTA(Context context, String date) {
        putString(context, "LastCalledOTA", date);
    }

    /**
     * 마지막으로 OTA Updater를 호출한 날짜
     * @param context
     * @return 날짜 yyyyMMdd
     */
    public static String getLastCalledOTA(Context context) {
        return getString(context, "LastCalledOTA");
    }


    //------------------------------------------------------------------------------------
    // private
    //------------------------------------------------------------------------------------
    private static void putInt(Context context, String key, int value) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private static int getInt(Context context, String key) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getInt(key, 0);
    }

    private static void putFloat(Context context, String key, float value) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    private static float getFloat(Context context, String key) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getFloat(key, 0);
    }

    private static void putString(Context context, String key, String value) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static String getString(Context context, String key) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getString(key, "");
    }

    private static SharedPreferences getPreferences(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences("FloridaPreference", Context.MODE_PRIVATE);
        }
        return preferences;
    }
}
