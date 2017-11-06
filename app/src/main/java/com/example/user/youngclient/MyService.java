package com.example.user.youngclient;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

// 서비스 클래스를 구현하려면, Service 를 상속받는다
public class MyService extends Service {
    //MediaPlayer mp; // 음악 재생을 위한 객체
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    long startTime;
    String returnMessage;
    boolean firstCount = true;

    //public static double latitude;
    //public static double longitude;


    @Override
    public IBinder onBind(Intent intent) {
        // Service 객체와 (화면단 Activity 사이에서)
        // 통신(데이터를 주고받을) 때 사용하는 메서드
        // 데이터를 전달할 필요가 없으면 return null;
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        // 서비스에서 가장 먼저 호출됨(최초에 한번만)
        Log.d("test", "서비스의 onCreate");
        //mp = MediaPlayer.create(this, R.raw.chacha);
        //mp.setLooping(false); // 반복재생
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 호출될 때마다 실행
        Log.d("test", "서비스의 onStartCommand");
        //mp.start(); // 노래 시작

//        //서버에 GrandClient의 위치를 요청
//        String sendMessage = LoginActivity.myPhoneNumber + "60";
//        Log.d("test", sendMessage);
//        TCPClient tcpThread = new TCPClient(sendMessage);
//        Thread thread = new Thread(tcpThread);
//        thread.start();
//
//        try {
//            thread.join();
//            Log.d("TCP", "try in myService");
//        } catch (Exception e) {
//            Log.d("TCP", "error in myService");
//        }
//
//        //서버에서 보낸 Message를 해독
//        returnMessage = tcpThread.getReturnMessage();
//        Log.d("test", "return in myservice : " + returnMessage);
//
//        if(returnMessage.compareToIgnoreCase("null60") == 0)
//        {
//            //Toast.makeText(getApplicationContext(), "서버에 문제가 있습니다. 문의 : 010-9350-0510", Toast.LENGTH_LONG).show();
//        }
//        else
//        {
//
//            returnMessage = returnMessage.substring(2, 22);
//            Log.d("test", returnMessage);
//            String tempLatitude = returnMessage.substring(0, 10);
//            String tempLongitude = returnMessage.substring(10, 20);
//
//            Log.d("test", tempLatitude);
//            Log.d("test", tempLongitude);
//
//            latitude = Double.parseDouble(tempLatitude);
//            longitude = Double.parseDouble(tempLongitude);
//
//            Log.d("test", String.valueOf(latitude));
//            Log.d("test", String.valueOf(longitude));
//        }
        //returnMessage = "60126.99398937.2816537"; // 임시

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 실행
        //mp.stop(); // 음악 종료
        Log.d("test", "서비스의 onDestroy");
    }
}