package com.example.user.youngclient;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.InputType.*;

public class SignInActivity extends AppCompatActivity {
    ImageButton joinButton;
    private String return_msg;
    private String sendMessage;
    private String email;
    private String phoneNumber;
    private String password;
    private String confirmPassWord;
    String myPhoneNumber;
    SharedPreferences setting;
    EditText emailEditText;
    EditText pwEditText;
    EditText confirmpwEditText;
    RelativeLayout rl;
    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        rl=(RelativeLayout)findViewById(R.id.signRelative);
        joinButton = (ImageButton) findViewById(R.id.joinButton);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        pwEditText = (EditText) findViewById(R.id.pwEditText);
        confirmpwEditText = (EditText) findViewById(R.id.confirmpwEditText);

        pwEditText.setPrivateImeOptions("defaultInputmode=english;");
        confirmpwEditText.setPrivateImeOptions("defaultInputmode=english;");

        emailEditText.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        pwEditText.setInputType(TYPE_TEXT_VARIATION_PASSWORD);
        pwEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());

        confirmpwEditText.setInputType(TYPE_TEXT_VARIATION_PASSWORD);
        confirmpwEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());

        //emailEditText.setPrivateImeOptions("defaultInputmode=english;");
        //pwEditText.setPrivateImeOptions("defaultInputmode=english;");
        //confirmpwEditText.setPrivateImeOptions("defaultInputmode=english;");
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


        setting = getSharedPreferences("setting", 0);
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imm.hideSoftInputFromWindow(emailEditText.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(pwEditText.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(confirmpwEditText.getWindowToken(), 0);
            }
        });
        joinButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                confirmPassWord = confirmpwEditText.getText().toString();
                email = emailEditText.getText().toString();
                password = pwEditText.getText().toString();
                if (!confirmPassWord.equals(password)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                    builder.setTitle("알림");
                    builder.setMessage("비밀번호와 비밀번호 확인의 값이 다릅니다.\n" +
                            "다시 입력해주세요");
                    builder.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.show();
                } else if (!checkEmail(email)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                    builder.setTitle("알림");
                    builder.setMessage("이메일 형식이 아닙니다.\n" +
                            "다시 입력해주세요");
                    builder.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.show();
                } else {
                    Log.d("TCP", "ONE");
                    myPhoneNumber = setting.getString("myPhoneNumber", "null");
                    Log.d("TCP", "TWO");

                    phoneNumber = myPhoneNumber;


                    // 서버에 보낼 형식에 맞춰 만드는 부분
                    int nullSize = 50 - (email.length());
                    for (int i = 0; i < nullSize; i++)
                        email += " ";

                    nullSize = 11 - (phoneNumber.length());
                    for (int i = 0; i < nullSize; i++)
                        phoneNumber += " ";

                    nullSize = 12 - (password.length());
                    for (int i = 0; i < nullSize; i++)
                        password += " ";

                    signIn();
                }
            }
        });
    }


    private boolean checkEmail(String email) {
        String mail = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Pattern p = Pattern.compile(mail);
        Matcher m = p.matcher(email);
        Log.v("TCP", String.valueOf(m.matches()));
        return m.matches();
    }

    private void signIn() {
        // 서버에 보낼 메세지
        sendMessage = myPhoneNumber + "11" + email + password + phoneNumber;

        // TCP 쓰레드 생성
        TCPClient tcpThread = new TCPClient(sendMessage, 0, getApplicationContext());
        Thread thread = new Thread(tcpThread);
        thread.start();

        try {
            thread.join();
            Log.d("TCP", "try");
        } catch (Exception e) {
            Log.d("TCP", "error");
        }
        return_msg = tcpThread.getReturnMessage();
        Log.d("TCP", "ret" + tcpThread.getReturnMessage());

        //회원가입 성공 시 연동 화면으로 넘어감
        if ("11SUCCESS".equals(return_msg)) {
            Intent intent = new Intent(SignInActivity.this, LinkActivity.class);
            startActivity(intent);
            finish();
        }
        //회원가입 실패
        else if("11FAILURE".equals(tcpThread.getReturnMessage())){
            Log.d("TCP", "failure" + return_msg);

            //연동 할 것이라고 알려주는 다이얼로그
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    SignInActivity.this);

            // 제목셋팅
            alertDialogBuilder.setTitle("회원가입 실패");

            // AlertDialog 셋팅
            alertDialogBuilder
                    .setMessage("정보를 입력해주세요.")
                    .setCancelable(false)
                    .setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // 다이얼로그 생성
            AlertDialog alertDialog = alertDialogBuilder.create();

            // 다이얼로그 보여주기
            alertDialog.show();

        }
        else
        {
//            //연동 할 것이라고 알려주는 다이얼로그
//            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
//                    SignInActivity.this);
//
//            // 제목셋팅
//            alertDialogBuilder.setTitle("서버가 꺼져있음");
//
//            // AlertDialog 셋팅
//            alertDialogBuilder
//                    .setMessage("서버가 꺼져있습니다.\n문의 : 010-9350-0510")
//                    .setCancelable(false)
//                    .setPositiveButton("확인",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(
//                                        DialogInterface dialog, int id) {
//                                    dialog.cancel();
//                                }
//                            });
//
//            // 다이얼로그 생성
//            AlertDialog alertDialog = alertDialogBuilder.create();
//
//            // 다이얼로그 보여주기
//            alertDialog.show();
        }
    }
}