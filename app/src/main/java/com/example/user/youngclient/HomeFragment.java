package com.example.user.youngclient;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
        import android.support.annotation.NonNull;
        import android.support.design.widget.BottomNavigationView;
        import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
        import android.widget.TextView;
import android.widget.Toast;

public class HomeFragment extends Fragment {

    Button button;
    String returnMessage;
    public static double latitude;
    public static double longitude;
    String myPhoneNumber;
    SharedPreferences setting;
    ImageView notice;
    ImageView isInHome;
    ImageView isOutHome;

    public HomeFragment()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_home, container, false);

        button = (Button)layout.findViewById(R.id.refreshButton);
        notice = (ImageView) layout.findViewById(R.id.home_notice);
        isInHome = (ImageView) layout.findViewById(R.id.isInHome);
        isOutHome = (ImageView) layout.findViewById(R.id.isOutHome);

        setting = this.getActivity().getSharedPreferences("setting", 0);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //서버에 GrandClient의 현재 위치를 요청
                myPhoneNumber = setting.getString("myPhoneNumber", "null");
                String sendMessage = myPhoneNumber + "60";
                Log.d("test", sendMessage);
                TCPClient tcpThread = new TCPClient(sendMessage, 0, getContext());
                Thread thread = new Thread(tcpThread);
                thread.start();

                try {
                    thread.join();
                    Log.d("TCP", "try in HomeFragment");
                } catch (Exception e) {
                    Log.d("TCP", "error in HomeFragment");
                }
                Log.d("TCP", "HomeFragment11");
                //서버에서 보낸 Message를 해독
                returnMessage = tcpThread.getReturnMessage();

                Log.d("test", "return in myservice : " + returnMessage);

                if(returnMessage == null)
                {
                    //Toast.makeText(getApplicationContext(), "서버에 문제가 있습니다. 문의 : 010-9350-0510", Toast.LENGTH_LONG).show();
                }
                else if(returnMessage.equals("60FAILURE"))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("상대방의 위치 정보 없음");
                    builder.setMessage("상대방의 위치 정보가 저장되지 않았습니다.\n" +
                            "조금만 기다려 주세요!");
                    builder.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.show();
                }
                else
                {

                    returnMessage = returnMessage.substring(2, 22);
                    Log.d("test", returnMessage);
                    String tempLatitude = returnMessage.substring(0, 10);
                    String tempLongitude = returnMessage.substring(10, 20);

                    Log.d("test", tempLatitude);
                    Log.d("test", tempLongitude);

                    latitude = Double.parseDouble(tempLatitude);
                    longitude = Double.parseDouble(tempLongitude);

                    Log.d("test", String.valueOf(latitude));
                    Log.d("test", String.valueOf(longitude));

                    String homeLatitude = setting.getString("homeLatitude", null);
                    String homeLongitude = setting.getString("homeLongitude", null);

                    //위치 계산
                    if (homeLatitude == null || homeLongitude == null)
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("어르신 집 위치 정보 없음");
                        builder.setMessage("어르신 집의 위치 정보가 저장되지 않았습니다.\n" +
                                "설정에서 위치 정보를 넣어 주세요!");
                        builder.setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                        builder.show();
                    }
                    else if(latitude == 0 || longitude == 0)
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("상대방의 위치 정보 없음");
                        builder.setMessage("상대방의 위치 정보가 저장되지 않았습니다.\n" +
                                "조금만 기다려 주세요!");
                        builder.setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                        builder.show();
                    }
                    else
                    {
                        double latitudeDiff = Math.abs(Double.parseDouble(homeLatitude) - latitude);
                        double longitudeDiff = Math.abs(Double.parseDouble(homeLongitude) - longitude);

                        if(latitudeDiff < 0.001 && longitudeDiff < 0.001) // 집 근처에 있을 때
                        {
                            notice.setImageResource(R.drawable.home_notice_2);
                            isInHome.setImageResource(R.drawable.home_home_color);
                            isOutHome.setImageResource(R.drawable.home_out_gray);
                        }

                        else // 집 바깥에 있을 때
                        {
                            notice.setImageResource(R.drawable.home_notice);
                            isInHome.setImageResource(R.drawable.home_near_gray);
                            isOutHome.setImageResource(R.drawable.home_out_color);
                        }

                    }
                }
            }
        });
        return layout;
    }


}
