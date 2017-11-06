package com.example.user.youngclient;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class SettingFragment extends Fragment {

    SharedPreferences setting;
    SharedPreferences.Editor editor;
    String duration;
    LinearLayout settingHomeLocation;
    LinearLayout settingVersion;
    LinearLayout settingDevelopers;

    public SettingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v("aaa", "SettingonCreate");
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_setting, container, false);
        LinearLayout settingHomeLocation = (LinearLayout) layout.findViewById(R.id.settingHome);
        LinearLayout settingVersion = (LinearLayout) layout.findViewById(R.id.settingVersion);
        LinearLayout settingDevelopers = (LinearLayout) layout.findViewById(R.id.settingDevelopers);

        setting = this.getActivity().getSharedPreferences("setting", 0);
        editor = setting.edit();

        //집 위치 설정
        settingHomeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SettingHomeLocation.class);
                startActivity(intent);
            }
        });

        //버전
        settingVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), VersionActivity.class);
                startActivity(intent);
            }
        });


        //개발자 소개
        settingDevelopers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DevelopersActivity.class);
                startActivity(intent);
            }
        });

        return layout;
    }
}