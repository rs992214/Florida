package com.thinkware.florida.media;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;

/**
 * Created by Mihoe on 2016-09-09.
 *
 101.wav 인증 요청 중입니다
 102.wav 인증되었습니다
 103.wav 차량 인증에 실패했습니다
 104.wav 등록되지 않은 전화번호입니다
 105.wav 등록되지 않은 차량입니다.
 106.wav 통신요금 미납으로 일시 정지중입니다.
 107.wav Car ID인증에 실패했습니다.
 108.wav 연락처인증에 실패했습니다.
 109.wav 일시정지 기간입니다
 110.wav 휴무 조입니다.
 111.wav 휴식 요청중입니다
 112.wav 휴식 요청이 처리되었습니다
 113.wav 운행요청중입니다
 114.wav 운행요청이 처리되었습니다.
 115.wav 공지사항입니다
 116.wav 콜 요청이 수신되었습니다
 117.wav 콜 요청을 수신했습니다
 118.wav 배차중인 콜이 있습니다
 119.wav 진행중인 콜이 있습니다. 배차 참여하세요
 120.wav 배차되었습니다. 고객정보를 확인하세요
 121.wav 배차가 되었습니다. 고객정보를 확인하세요
 122.wav 배차 실패되었습니다.
 123.wav 배차가 실패되었습니다.
 124.wav 배차 요청했습니다.
 125.wav 배차 요청하였습니다.
 126.wav 배차 거부했습니다
 127.wav 배차 거부하였습니다
 128.wav 길 안내를 시작합니다
 129.wav 비상 요청이 해제되었습니다
 130.wav 대기 요청 중입니다
 131.wav 대기 지역이 아닙니다
 132.wav 대기 배차되었습니다. 고객정보를 확인하세요.
 133.wav 대기 취소되었습니다
 134.wav 대기지역을 벗어나 대기 취소되었습니다.
 135.wav 대기 요청에 성공했습니다
 136.wav 대기 요청에 성공하였습니다
 137.wav 대기 요청에 실패했습니다
 138.wav 대기 요청에 실패하였습니다
 139.wav 대기 취소했습니다.
 140.wav 대기 취소하였습니다.
 141.wav 대기 순번이 갱신되었습니다.
 142.wav 메시지가 도착했습니다.
 143.wav 메시지가 도착하였습니다.
 144.wav 음영지역에 진입했습니다
 145.wav 음영지역에 진입하였습니다.
 146.wav 음영지역을 벗어났습니다.
 147.wav 영업종료 요청을 하였습니다.
 148.wav 영업종료 요청 중입니다.
 149.wav 영업종료가 정상 처리되었습니다.
 150.wav 콜 장비에 문제가 발생하였습니다. 가까운 AS센터를 방문해주십시오
 151.wav 탑승실패가 정상처리되었습니다
 160.wav 띵똥!

 */

public class WavResourcePlayer implements SoundPool.OnLoadCompleteListener {
    private Context context;
    private static WavResourcePlayer instance = null;
    private SoundPool soundPool;
    private HashMap<Integer, Integer> soundMap;

    private WavResourcePlayer(Context context) {
        this.context = context.getApplicationContext();
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(this);
        soundMap = new HashMap<Integer, Integer>();
    }

    public static WavResourcePlayer getInstance(Context context) {
        if (instance == null) {
            instance = new WavResourcePlayer(context);
        }
        return instance;
    }

    public void play(int resId) {
        if (soundMap.get(resId) == null) {
            int soundId = soundPool.load(context, resId, 1);
            soundMap.put(resId, soundId);
        } else {
            int soundId = soundMap.get(resId);
            soundPool.play(soundId, 1, 1, 0, 0, 1);
        }
    }

    public void release() {
        soundPool.release();
        soundPool = null;
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int soundId, int status) {
        if (status == 0) {
            soundPool.play(soundId, 1, 1, 0, 0, 1);
        }
    }
}
