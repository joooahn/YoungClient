package com.example.user.youngclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class HelpFragment extends Fragment {

    LinearLayout helpHome;
    LinearLayout helpMap;
    LinearLayout helpSetting;

    public HelpFragment()
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
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_help, container, false);
        helpHome = (LinearLayout) layout.findViewById(R.id.askHomeButton);
        helpMap = (LinearLayout) layout.findViewById(R.id.askMapButton);
        helpSetting = (LinearLayout) layout.findViewById(R.id.askSettingButton);

        helpHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), HelpHome.class);
                startActivity(intent);
            }
        });

        helpMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), HelpMapActivity.class);
                startActivity(intent);
            }
        });

        helpSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), HelpSetting.class);
                startActivity(intent);
            }
        });

        return layout;
    }
}
