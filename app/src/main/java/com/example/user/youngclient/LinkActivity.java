package com.example.user.youngclient;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class LinkActivity extends Activity {

    private EditText partnerNumberEditText;
    private ImageView notice;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    private static final int MY_PERMISSIONS_READ_PHONE_STATE = 2;
    private int i = 0;
    RelativeLayout rl;
    InputMethodManager imm;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link);
        partnerNumberEditText = (EditText) findViewById(R.id.partnerNumberEditText);
        notice = (ImageView)findViewById(R.id.notice);
        rl=(RelativeLayout)findViewById(R.id.linkRelative);

        //내 번호 알아오기 권한 얻기
        int permissionCheck = ContextCompat.checkSelfPermission(LinkActivity.this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            // 권한 없음

            Log.d("TCP", "no");

            ActivityCompat.requestPermissions(LinkActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_READ_PHONE_STATE);
        } else {
            // 권한 있음
            Log.d("TCP", "yes");
        }

        //문자 보내는 권한 받기
        permissionCheck = ContextCompat.checkSelfPermission(LinkActivity.this, Manifest.permission.SEND_SMS);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            // 권한 없음

            Log.d("TCP", "no");

            ActivityCompat.requestPermissions(LinkActivity.this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        } else {
            // 권한 있음
            Log.d("TCP", "yes");
        }

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imm.hideSoftInputFromWindow(partnerNumberEditText.getWindowToken(), 0);
            }
        });


        final ImageButton sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                //내 전화번호 받아오기
                TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                String myPhoneNumber = telManager.getLine1Number();
                if (myPhoneNumber.startsWith("+82")) {
                    myPhoneNumber = myPhoneNumber.replace("+82", "0");
                }

                final String myNumber = myPhoneNumber;
                final String partnerNumber = partnerNumberEditText.getText().toString();
                Log.d("TCP", "111111"+String.valueOf(partnerNumber.length()));

                if(partnerNumber.length() == 0)
                {
                    Log.d("TCP", "22222"+partnerNumber);
                    //연동 할 것이라고 알려주는 다이얼로그
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            LinkActivity.this);

                    // 제목셋팅
                    alertDialogBuilder.setTitle("연동 실패");

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
                   notice.setImageResource(R.drawable.notice_link_check);

                    sendSMS(partnerNumber, "링크를 클릭하시면 꽃길 어플리케이션이 다운로드 됩니다.\n" +
                            "http://210.89.178.111/download.php");
                }
            }
        });
    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case  MY_PERMISSIONS_REQUEST_SEND_SMS:
//
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // 권한 허가
//// 해당 권한을 사용해서 작업을 진행할 수 있습니다
//                } else {
//                    // 권한 거부
//// 사용자가 해당권한을 거부했을때 해주어야 할 동작을 수행합니다
//                }
//                return;
//        }
//    }

    /**
     * 문자를 보내는 메소드
     *
     * @param phoneNumber
     * @param message
     */
    private void sendSMS(String phoneNumber, String message) {

        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        // 문자 보내는 상태를 감지하는 PendingIntent
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        // 문자 받은 상태를 감지하는 PendingIntent
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        // 문자 보내는 상태를 감지하는 BroadcastReceiver를 등록한다.
        registerReceiver(new BroadcastReceiver() {

            // 문자를 수신하면, 발생.
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("TCP", "asdasd : " + getResultCode());
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        // 문자를 받는 상태를 확인하는 BroadcastReceiver를 등록.
        registerReceiver(new BroadcastReceiver() {


            // 문자를 받게 되면, 불린다.
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        // SmsManager를 가져온다.
        SmsManager sms = SmsManager.getDefault();
        // sms를 보낸다.
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }
}