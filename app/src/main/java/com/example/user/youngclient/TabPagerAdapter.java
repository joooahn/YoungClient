package com.example.user.youngclient;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Created by LENOVO on 2017-09-13.
 */
public class TabPagerAdapter extends FragmentStatePagerAdapter {

    // Count number of tabs
    private int tabCount;
    HomeFragment homeFragment = new HomeFragment();
    Fragment1 mapFragment = new Fragment1();
    SettingFragment settingFragment = new SettingFragment();
    HelpFragment helpFragment = new HelpFragment();

    public TabPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {

        // Returning the current tabs
        switch (position) {
            case 0:
                //HomeFragment homeFragment = new HomeFragment();
                //  return homeFragment;
                return new HomeFragment();
            case 1:
                //Fragment1 mapFragment = new Fragment1();
                //return mapFragment;
                return new Fragment1();
            case 2:
                //SettingFragment settingFragment = new SettingFragment();
                //return settingFragment;
                return new SettingFragment();
            case 3:
                //HelpFragment helpFragment = new HelpFragment();
                //return helpFragment;
                return new HelpFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
