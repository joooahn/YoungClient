package com.example.user.youngclient;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginDefine;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import xdroid.toaster.Toaster;

import static java.lang.String.join;
import static java.lang.String.valueOf;

public class LoginActivity extends AppCompatActivity {

    private ImageButton loginButton;
    private ImageButton joinButton;
    private String return_msg;
    private String sendMessage;
    private String myPhoneNumber;
    SharedPreferences setting;
    SharedPreferences.Editor editor;
    int connectCount = 0;

    private static final int MY_PERMISSIONS_REQUEST_SEND_LOCATION = 0;
    private static final int MY_PERMISSIONS_READ_PHONE_STATE = 1;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 2;

    /**
     * UI 요소들
     */
    private TextView mApiResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = (ImageButton) findViewById(R.id.loginButton);
        joinButton = (ImageButton) findViewById(R.id.joinButton);

        //setting에 내 번호 저장
        setting = getSharedPreferences("setting", 0);
        editor= setting.edit();

        //전화번호 얻기 관련 권한 얻기
        int permissionCheck = ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            // 권한 없음
            Log.d("TCP", "no");
            ActivityCompat.requestPermissions(LoginActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_READ_PHONE_STATE);
        } else {
            // 권한 있음
            Log.d("TCP", "yes");
        }

        //위치 권한 얻기
        permissionCheck = ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            // 권한 없음
            Log.v("aaa", "ok");
            ActivityCompat.requestPermissions(LoginActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_SEND_LOCATION);
        } else {
            // 권한 있음
            Log.v("aaa", "notOK");
        }


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //내 전화번호 받아오기
                TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                myPhoneNumber = telManager.getLine1Number();
                if (myPhoneNumber.startsWith("+82")) {
                    myPhoneNumber = myPhoneNumber.replace("+82", "0");
                }
                Log.d("TCP", "in LoginActivity");
                editor.putString("myPhoneNumber", myPhoneNumber);
                editor.commit();

                logIn();
            }
        });

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void logIn() {
        // 서버에 보낼 메세지
        sendMessage = myPhoneNumber + "00";

        // TCP 쓰레드 생성
        TCPClient tcpThread = new TCPClient(sendMessage, 0, LoginActivity.this);
        Thread thread = new Thread(tcpThread);
        thread.start();

        try {
            thread.join();
            Log.d("TCP", "try in LoginActivity");
        } catch (Exception e) {
            Log.d("TCP", "error");
        }
        return_msg = tcpThread.getReturnMessage();
        Log.d("TCP", "Login retrun : " + return_msg);


        //로그인 성공
        if ("00SUCCESS".equals(return_msg)) {
            connectCount = 0;
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);

//            //백그라운드에서 서버에 GrandClient의 위치를 요청하는 서비스 실행
//            Intent serviceIntent = new Intent(
//                    getApplicationContext(),//현재제어권자
//                    MyService.class); // 이동할 컴포넌트
//            startService(serviceIntent); // 서비스 시작

            finish();
        }
        //회원가입 실행
        else if ("00FAILURE".equals(tcpThread.getReturnMessage())) {
            Log.d("TCP", "failure" + return_msg);

            Intent intent = new Intent(LoginActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        } else if ("01FAILURE".equals(tcpThread.getReturnMessage())) {
//연동 할 것이라고 알려주는 다이얼로그
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    LoginActivity.this);

            // 제목셋팅
            alertDialogBuilder.setTitle("연동되지 않음");

            // AlertDialog 셋팅
            alertDialogBuilder
                    .setMessage("회원가입은 되어있으나 연동이 필요합니다.\n확인버튼을 누르면 연동화면으로 넘어갑니다.")
                    .setCancelable(false)
                    .setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog, int id) {
                                    Intent intent = new Intent(LoginActivity.this, LinkActivity.class);
                                    startActivity(intent);
                                    finish();
                                    dialog.cancel();
                                }
                            });

            // 다이얼로그 생성
            AlertDialog alertDialog = alertDialogBuilder.create();

            // 다이얼로그 보여주기
            alertDialog.show();
        } else {
            //서버가 꺼져있음
//            if (connectCount < 3) {
//
//                connectCount++;
//
//                Toaster.toast("서버에 연결 시도 중입니다. ("+String.valueOf(connectCount) + "/" + "3)");
//                Handler handler=new Handler(Looper.getMainLooper());
//                Runnable r=new Runnable() {
//                    public void run() {
//                        logIn();
//                    }
//                };
//                handler.postDelayed(r, 5000);
//            }
//            else
//            {
//                Toaster.toast("서버가 꺼져있습니다. 문의 : 010-5092-3233");
//                finish();
//            }

        }
    }
}
