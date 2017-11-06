package com.example.user.youngclient;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapCompassManager;
import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.nmapmodel.NMapPlacemark;
import com.nhn.android.maps.overlay.NMapCircleData;
import com.nhn.android.maps.overlay.NMapCircleStyle;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.maps.overlay.NMapPathData;
import com.nhn.android.maps.overlay.NMapPathLineStyle;
import com.nhn.android.mapviewer.overlay.NMapCalloutCustomOverlay;
import com.nhn.android.mapviewer.overlay.NMapCalloutOverlay;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;
import com.nhn.android.mapviewer.overlay.NMapPathDataOverlay;

import static android.content.Context.MODE_PRIVATE;

public class MapFragment extends Fragment {

    private double lng = 0;
    private double lat = 0;

    SharedPreferences setting;
    SharedPreferences.Editor editor;

    private static final String CLIENT_ID = "lY27fNrZb3AcxOTRdiNo";
    private static final int MY_PERMISSIONS_REQUEST_SEND_LOCATION = 0;

    private NMapContext mMapContext;

    private static final String LOG_TAG = "NMapViewer";
    private static final boolean DEBUG = false;
    private NMapActivity nMapActivity;
    // set your Client ID which is registered for NMapViewer library.

    private MapContainerView mMapContainerView;

    private NMapView mMapView;
    private NMapController mMapController;
    //126.97838461534991,37.56661093444756 서울 시청
    private static final NGeoPoint NMAP_LOCATION_DEFAULT = new NGeoPoint(126.97838461534991, 37.56661093444756);

    private static final int NMAP_ZOOMLEVEL_DEFAULT = 14;
    private static final int NMAP_VIEW_MODE_DEFAULT = NMapView.VIEW_MODE_VECTOR;
    private static final boolean NMAP_TRAFFIC_MODE_DEFAULT = false;
    private static final boolean NMAP_BICYCLE_MODE_DEFAULT = false;

    private static final String KEY_ZOOM_LEVEL = "NMapViewer.zoomLevel";
    private static final String KEY_CENTER_LONGITUDE = "NMapViewer.centerLongitudeE6";
    private static final String KEY_CENTER_LATITUDE = "NMapViewer.centerLatitudeE6";
    private static final String KEY_VIEW_MODE = "NMapViewer.viewMode";
    private static final String KEY_TRAFFIC_MODE = "NMapViewer.trafficMode";
    private static final String KEY_BICYCLE_MODE = "NMapViewer.bicycleMode";

    private SharedPreferences mPreferences;

    private NMapOverlayManager mOverlayManager;

    private NMapMyLocationOverlay mMyLocationOverlay;
    private NMapLocationManager mMapLocationManager;
    private NMapCompassManager mMapCompassManager;

    private NMapViewerResourceProvider mMapViewerResourceProvider;

    private NMapPOIdataOverlay mFloatingPOIdataOverlay;
    private NMapPOIitem mFloatingPOIitem;
    private String return_msg;
    private NGeoPoint ng;
    String duration = "7";
    private int zoomLevel = 14;

    /*
     * Fragment에 포함된 NMapView 객체를 반환함
     */
    private NMapView findMapView(View v) {

        if (!(v instanceof ViewGroup)) {
            return null;
        }

        ViewGroup vg = (ViewGroup) v;
        if (vg instanceof NMapView) {
            return (NMapView) vg;
        }

        for (int i = 0; i < vg.getChildCount(); i++) {

            View child = vg.getChildAt(i);
            if (!(child instanceof ViewGroup)) {
                continue;
            }

            mMapView = findMapView(child);
            if (mMapView != null) {
                return mMapView;
            }
        }
        return null;
    }

    /* Fragment 라이프사이클에 따라서 NMapContext의 해당 API를 호출함 */
    private AlertDialog.Builder builder;
    private int i=0;
    private int j=0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        builder = new AlertDialog.Builder(getActivity());
        /*
        builder.setTitle("상대방의 위치 정보 없음");
        builder.setMessage("상대방의 위치 정보가 저장되지 않았습니다.\n" +
                "조금만 기다려 주세요!");
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
*/
        setting = this.getActivity().getSharedPreferences("setting", 0);

        String myPhoneNumber = setting.getString("myPhoneNumber", null);
        //    Log.v("TCP",myPhoneNumber);
        String sendMessage = myPhoneNumber + "60";
        TCPClient tcpThread = new TCPClient(sendMessage, 0, getContext());
        Thread thread = new Thread(tcpThread);
        thread.start();
        try {
            thread.join();
            Log.d("TCP", "try");

        } catch (Exception e) {
            Log.d("TCP", "error");
        }
        return_msg = tcpThread.getReturnMessage();

        if(return_msg == null)
        {

        }
        else if(return_msg.equals("60FAILURE"))
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
            String latStr = return_msg.substring(2, 12);
            Log.v("TCP", latStr);
            lat = Double.parseDouble(latStr);
            String lngStr = return_msg.substring(12, 22);
            Log.v("TCP", lngStr);
            lng = Double.parseDouble(lngStr);
        }

        //grandClient의 위치가 저장이 안 되었을 때
        if (lng == 0 || lat == 0) {
            lng = 126.97838461534991;
            lat = 37.56661093444756;

            builder.show();
        }


        mMapContext = new NMapContext(super.getActivity());

        mMapContext.onCreate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        throw new IllegalArgumentException("onCreateView should be implemented in the subclass of NMapFragment.");

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Fragment에 포함된 NMapView 객체 찾기
        // NMapView mMapView = findMapView(super.getView());
        //Switch changeSwitch = (Switch)getView().findViewById(R.id.top3static);
        final ImageButton top3Static = (ImageButton) getView().findViewById(R.id.top3static);
        final ImageButton refreshButton = (ImageButton) getView().findViewById(R.id.refreshButton);
        final String items[] = {"7일", "15일", "1달"};





        top3Static.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i=1-i;
                String myPhoneNumber = setting.getString("myPhoneNumber", null);
                String sendMessage = myPhoneNumber + "70" + duration;
                Log.d("TCP", sendMessage);
                final TCPClient tcpThread = new TCPClient(sendMessage, 0, getContext());
                Thread thread = new Thread(tcpThread);
                thread.start();

                try {
                    thread.join();
                    Log.d("TCP", "try in ViewTop3");
                } catch (Exception e) {
                    Log.d("TCP", "error in ViewTop3");
                }
                String returnMessage = tcpThread.getReturnMessage();
                if(returnMessage == null){

                }
                else
                {
                    boolean allZero =returnMessage.contains("."); // '.'이있으면 true임

                    //double allZero = Double.parseDouble(returnMessage.substring(2, 62));

                    if (i==1 && allZero) {
                        //on할 때
                        top3Static.setBackgroundDrawable(
                                getResources().
                                        getDrawable(R.drawable.map_visit_btn)
                        );
                        refreshButton.setBackgroundDrawable(
                                getResources().
                                        getDrawable(R.drawable.map_now_btn_gray)
                        );

                        AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
                        ab.setTitle("기간 설정");
                        ab.setSingleChoiceItems(items, -1,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        switch (whichButton) {
                                            case 0:
                                                duration = "7";
                                                break;
                                            case 1:
                                                duration = "15";
                                                break;
                                            case 2:
                                                duration = "30";
                                                break;

                                        }
                                    }
                                }).setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // OK 버튼 클릭시 , 여기서 선택한 값을 메인 Activity 로 넘기면 된다.
                                        String returnMessage1 = tcpThread.getReturnMessage();
                                        double lat1 = Double.parseDouble(returnMessage1.substring(2, 12));
                                        double lng1 = Double.parseDouble(returnMessage1.substring(12, 22));
                                        double lat2 = Double.parseDouble(returnMessage1.substring(22, 32));
                                        double lng2 = Double.parseDouble(returnMessage1.substring(32, 42));
                                        double lat3 = Double.parseDouble(returnMessage1.substring(42, 52));
                                        double lng3 = Double.parseDouble(returnMessage1.substring(52, 62));


                                        mOverlayManager.clearOverlays();

                                        //오버레이 아이템
                                        int markerId1 = NMapPOIflagType.SPOT;
                                        int markerId2 = NMapPOIflagType.FROM;
                                        int markerId3 = NMapPOIflagType.TO;

                                        // set POI data
                                        NMapPOIdata poiData = new NMapPOIdata(2, mMapViewerResourceProvider);
                                        poiData.beginPOIdata(0);
                                        if (lat1 != 0 && lng1 != 0) {
                                            poiData.addPOIitem(lng1, lat1, "1st", markerId1, 0);
                                            if (lat2 != 0 && lng2 != 0) {
                                                poiData.addPOIitem(lng2, lat2, "2nd", markerId2, 0);
                                                if (lat3 != 0 && lng3 != 0)
                                                    poiData.addPOIitem(lng3, lat3, "3rd", markerId3, 0);
                                            }
                                        }
                                        poiData.endPOIdata();
                                        // create POI data overlay
                                        NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
                                        // show all POI data
                                        poiDataOverlay.showAllPOIdata(0);
                                        // set event listener to the overlay
                                        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);
                                        mMapView.setScalingFactor(2.0F);

                                    }
                                }).setNegativeButton("취소",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // Cancel 버튼 클릭시
                                    }
                                });
                        ab.show();

                    } else {
                        //off할 때

                        if (!allZero) {
                            builder.setTitle("머물렀던 장소 정보 없음");
                            builder.setMessage("상대방이 머물렀던 기록이 충분히 저장되지 않았습니다.\n" +
                                    "조금만 기다려 주세요!");
                            builder.setPositiveButton("확인",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                            builder.show();
                        }
                        refreshButton.setBackgroundDrawable(
                                getResources().
                                        getDrawable(R.drawable.map_now_btn)
                        );
                        top3Static.setBackgroundDrawable(
                                getResources().
                                        getDrawable(R.drawable.map_visit_btn_gray)
                        );

                        mOverlayManager.clearOverlays();
                        //mMapController.setMapCenter(new NGeoPoint(126.97838461534991, 37.56661093444756), 14); //초기 시작 좌표

                        //오버레이 아이템
                        int markerId = NMapPOIflagType.PIN;

                        // set POI data
                        NMapPOIdata poiData = new NMapPOIdata(0, mMapViewerResourceProvider);
                        poiData.beginPOIdata(0);
                        poiData.addPOIitem(lng, lat, "GrandParents", markerId, 0);
                        poiData.endPOIdata();

                        // create POI data overlay
                        NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
                        // show all POI data
                        poiDataOverlay.showAllPOIdata(zoomLevel);
                        // set event listener to the overlay
                        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);
                        mMapView.setScalingFactor(2.0F);


                    } // end if



                }




            }
        });


        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(i==1)
                {
                    refreshButton.setBackgroundDrawable(
                            getResources().
                                    getDrawable(R.drawable.map_now_btn)
                    );
                    top3Static.setBackgroundDrawable(
                            getResources().
                                    getDrawable(R.drawable.map_visit_btn_gray)
                    );
                }

                String myPhoneNumber = setting.getString("myPhoneNumber", null);
                String sendMessage = myPhoneNumber + "60";
                TCPClient tcpThread = new TCPClient(sendMessage, 0, getContext());
                Thread thread = new Thread(tcpThread);
                thread.start();
                try {
                    thread.join();
                    Log.d("TCP", "try");
                } catch (Exception e) {
                    Log.d("TCP", "error");
                }
                return_msg = tcpThread.getReturnMessage();

                if(return_msg == null)
                {

                }
                else if(return_msg.equals("60FAILURE"))
                {
                    Log.d("TCP", "in 60FAILURE");
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
                    String latStr = return_msg.substring(2, 12);
                    Log.v("TCP", latStr);
                    lat = Double.parseDouble(latStr);
                    String lngStr = return_msg.substring(12, 22);
                    Log.v("TCP", lngStr);
                    lng = Double.parseDouble(lngStr);
                }



                //grandClient의 위치가 저장이 안 되었을 때
                if (lng == 0 || lat == 0) {
                    //서울시청
                    lng = 126.97838461534991;
                    lat = 37.56661093444756;
                    /*
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
*/
                    builder.setTitle("상대방의 위치 정보 없음");
                    builder.setMessage("상대방의 위치 정보가 저장되지 않았습니다.\n" +
                            "조금만 기다려 주세요!");
                    builder.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.show();
                    mOverlayManager.clearOverlays();
                    //mMapController.setMapCenter(new NGeoPoint(126.97838461534991, 37.56661093444756), 14); //초기 시작 좌표

                    //오버레이 아이템
                    int markerId = NMapPOIflagType.PIN;

                    // set POI data
                    NMapPOIdata poiData = new NMapPOIdata(0, mMapViewerResourceProvider);
                    poiData.beginPOIdata(0);
                    poiData.addPOIitem(lng, lat, "GrandParents", markerId, 0);
                    poiData.endPOIdata();

                    // create POI data overlay
                    NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
                    // show all POI data
                    poiDataOverlay.showAllPOIdata(0);
                    // set event listener to the overlay
                    poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);
                    mMapView.setScalingFactor(2.0F);
                    //      restoreInstanceState();

                }
                //grandClient의 위치가 저장되어 있을 때
                else {
                    mOverlayManager.clearOverlays();
                    //mMapController.setMapCenter(new NGeoPoint(126.97838461534991, 37.56661093444756), 14); //초기 시작 좌표

                    //오버레이 아이템
                    int markerId = NMapPOIflagType.PIN;

                    // set POI data
                    NMapPOIdata poiData = new NMapPOIdata(0, mMapViewerResourceProvider);
                    poiData.beginPOIdata(0);
                    poiData.addPOIitem(lng, lat, "GrandParents", markerId, 0);
                    poiData.endPOIdata();

                    // create POI data overlay
                    NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
                    // show all POI data
                    poiDataOverlay.showAllPOIdata(0);
                    // set event listener to the overlay
                    poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);
                    mMapView.setScalingFactor(2.0F);
                    //      restoreInstanceState();
                }


            }
        });


        mMapView = (NMapView) getView().findViewById(R.id.mapView);
        mMapView.setClientId(CLIENT_ID);
        mMapView.setClickable(true);
        mMapView.setEnabled(true);
        mMapView.setFocusable(true);
        mMapView.setFocusableInTouchMode(true);
        mMapView.requestFocus();
        if (mMapView == null) {
            throw new IllegalArgumentException("NMapFragment dose not have an instance of NMapView.");
        }
        // NMapActivity를 상속하지 않는 경우에는 NMapView 객체 생성후 반드시 setupMapView()를 호출해야함.
        mMapContext.setupMapView(mMapView);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.setOnMapStateChangeListener(onMapViewStateChangeListener);
        mMapView.setOnMapViewTouchEventListener(onMapViewTouchEventListener);
        mMapView.setOnMapViewDelegate(onMapViewTouchDelegate);
        // use map controller to zoom in/out, pan and set map center, zoom level etc.
        mMapController = mMapView.getMapController();

        // use built in zoom controls
        NMapView.LayoutParams lp = new NMapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, NMapView.LayoutParams.BOTTOM_RIGHT);
        mMapView.setBuiltInZoomControls(false, lp);

        // create resource provider
        mMapViewerResourceProvider = new NMapViewerResourceProvider(getActivity());

        // set data provider listener
        // super.setMapDataProviderListener(onDataProviderListener);
        nMapActivity = new NMapActivity() {
            @Override
            public void setMapDataProviderListener(OnDataProviderListener onDataProviderListener) {
                super.setMapDataProviderListener(onDataProviderListener);
            }
        };
        // create overlay manager
        mOverlayManager = new NMapOverlayManager(getActivity(), mMapView, mMapViewerResourceProvider);
        // register callout overlay listener to customize it.
        mOverlayManager.setOnCalloutOverlayListener(onCalloutOverlayListener);
        // register callout overlay view listener to customize it.
        mOverlayManager.setOnCalloutOverlayViewListener(onCalloutOverlayViewListener);

        // location manager
        mMapLocationManager = new NMapLocationManager(getActivity());
        mMapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);

        // compass manager
        mMapCompassManager = new NMapCompassManager(getActivity());

        // create my location overlay
        mMyLocationOverlay = mOverlayManager.createMyLocationOverlay(mMapLocationManager, mMapCompassManager);
        //  mOverlayManager.clearOverlays();

        // add POI data overlay
        //   testPOIdataOverlay();


        mMapContext.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        mMapContext.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        mMapContext.onPause();
    }

    @Override
    public void onStop() {
        stopMyLocation();
        mMapContext.onStop();

        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mMapContext.onDestroy();
        saveInstanceState();

        super.onDestroy();
    }


    private void stopMyLocation() {
        if (mMyLocationOverlay != null) {
            mMapLocationManager.disableMyLocation();

            if (mMapView.isAutoRotateEnabled()) {
                mMyLocationOverlay.setCompassHeadingVisible(false);

                mMapCompassManager.disableCompass();

                mMapView.setAutoRotateEnabled(false, false);

                mMapContainerView.requestLayout();
            }
        }
    }

    private void testPathDataOverlay() {

        // set path data points
        NMapPathData pathData = new NMapPathData(9);

        pathData.initPathData();
        pathData.addPathPoint(127.108099, 37.366034, NMapPathLineStyle.TYPE_SOLID);
        pathData.addPathPoint(127.108088, 37.366043, 0);
        pathData.addPathPoint(127.108079, 37.365619, 0);
        pathData.addPathPoint(127.107458, 37.365608, 0);
        pathData.addPathPoint(127.107232, 37.365608, 0);
        pathData.addPathPoint(127.106904, 37.365624, 0);
        pathData.addPathPoint(127.105933, 37.365621, NMapPathLineStyle.TYPE_DASH);
        pathData.addPathPoint(127.105929, 37.366378, 0);
        pathData.addPathPoint(127.106279, 37.366380, 0);
        pathData.endPathData();

        NMapPathDataOverlay pathDataOverlay = mOverlayManager.createPathDataOverlay(pathData);
        if (pathDataOverlay != null) {

            // add path data with polygon type
            NMapPathData pathData2 = new NMapPathData(4);
            pathData2.initPathData();
            pathData2.addPathPoint(127.106, 37.367, NMapPathLineStyle.TYPE_SOLID);
            pathData2.addPathPoint(127.107, 37.367, 0);
            pathData2.addPathPoint(127.107, 37.368, 0);
            pathData2.addPathPoint(127.106, 37.368, 0);
            pathData2.endPathData();
            pathDataOverlay.addPathData(pathData2);
            // set path line style
            NMapPathLineStyle pathLineStyle = new NMapPathLineStyle(mMapView.getContext());
            pathLineStyle.setPataDataType(NMapPathLineStyle.DATA_TYPE_POLYGON);
            pathLineStyle.setLineColor(0xA04DD2, 0xff);
            pathLineStyle.setFillColor(0xFFFFFF, 0x00);
            pathData2.setPathLineStyle(pathLineStyle);

            // add circle data
            NMapCircleData circleData = new NMapCircleData(1);
            circleData.initCircleData();
            circleData.addCirclePoint(127.1075, 37.3675, 50.0F);
            circleData.endCircleData();
            pathDataOverlay.addCircleData(circleData);
            // set circle style
            NMapCircleStyle circleStyle = new NMapCircleStyle(mMapView.getContext());
            circleStyle.setLineType(NMapPathLineStyle.TYPE_DASH);
            circleStyle.setFillColor(0x000000, 0x00);
            circleData.setCircleStyle(circleStyle);

            // show all path data
            pathDataOverlay.showAllPathData(0);
        }
    }

    private void testPathPOIdataOverlay() {

        // set POI data
        NMapPOIdata poiData = new NMapPOIdata(4, mMapViewerResourceProvider, true);
        poiData.beginPOIdata(4);
        poiData.addPOIitem(349652983, 149297368, "Pizza 124-456", NMapPOIflagType.FROM, null);
        poiData.addPOIitem(349652966, 149296906, null, NMapPOIflagType.NUMBER_BASE + 1, null);
        poiData.addPOIitem(349651062, 149296913, null, NMapPOIflagType.NUMBER_BASE + 999, null);
        poiData.addPOIitem(349651376, 149297750, "Pizza 000-999", NMapPOIflagType.TO, null);
        poiData.endPOIdata();

        // create POI data overlay
        NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);

        // set event listener to the overlay
        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);

    }

    private void testPOIdataOverlay() {

        // Markers for POI item
        int markerId = NMapPOIflagType.PIN;

        // set POI data
        NMapPOIdata poiData = new NMapPOIdata(1, mMapViewerResourceProvider);
        poiData.beginPOIdata(1);
        NMapPOIitem item = poiData.addPOIitem(lng, lat, String.valueOf(lat), markerId, 0);
        Log.v("TCP", "dataLat: " + String.valueOf(lat) + " " + String.valueOf(lng));
        item.setRightAccessory(true, NMapPOIflagType.CLICKABLE_ARROW);
        //poiData.addPOIitem(127.061, 37.51, "Pizza 123-456", markerId, 0);
        poiData.endPOIdata();

        // create POI data overlay
        NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);

        // set event listener to the overlay
        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);
        if (mIsMapEnlared) {
            mMapView.setScalingFactor(2.0F);
        } else {
            mMapView.setScalingFactor(1.0F);
        }
        // select an item
        poiDataOverlay.selectPOIitem(0, true);
        restoreInstanceState();
    }


    /* NMapDataProvider Listener */
    private final NMapActivity.OnDataProviderListener onDataProviderListener = new NMapActivity.OnDataProviderListener() {

        @Override
        public void onReverseGeocoderResponse(NMapPlacemark placeMark, NMapError errInfo) {
            Log.v("aaa", "Geocoding");
            if (DEBUG) {
                Log.i(LOG_TAG, "onReverseGeocoderResponse: placeMark="
                        + ((placeMark != null) ? placeMark.toString() : null));
            }

            if (errInfo != null) {
                Log.e(LOG_TAG, "Failed to findPlacemarkAtLocation: error=" + errInfo.toString());

                Toast.makeText(getActivity(), errInfo.toString(), Toast.LENGTH_LONG).show();
                return;
            }

            if (mFloatingPOIitem != null && mFloatingPOIdataOverlay != null) {
                mFloatingPOIdataOverlay.deselectFocusedPOIitem();

                if (placeMark != null) {
                    mFloatingPOIitem.setTitle(placeMark.toString());

                }
                mFloatingPOIdataOverlay.selectPOIitemBy(mFloatingPOIitem.getId(), false);
            }
        }

    };

    /* MyLocation Listener */
    private final NMapLocationManager.OnLocationChangeListener onMyLocationChangeListener = new NMapLocationManager.OnLocationChangeListener() {

        @Override
        public boolean onLocationChanged(NMapLocationManager locationManager, NGeoPoint myLocation) {
            if (mMapController != null) {
                mMapController.animateTo(myLocation);
            }
            return true;
        }

        @Override
        public void onLocationUpdateTimeout(NMapLocationManager locationManager) {

            // stop location updating
            //			Runnable runnable = new Runnable() {
            //				public void run() {
            //					stopMyLocation();
            //				}
            //			};
            //			runnable.run();

            Toast.makeText(getActivity(), "Your current location is temporarily unavailable.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onLocationUnavailableArea(NMapLocationManager locationManager, NGeoPoint myLocation) {

            Toast.makeText(getActivity(), "Your current location is unavailable area.", Toast.LENGTH_LONG).show();

            stopMyLocation();
        }

    };

    /* MapView State Change Listener*/
    private final NMapView.OnMapStateChangeListener onMapViewStateChangeListener = new NMapView.OnMapStateChangeListener() {

        @Override
        public void onMapInitHandler(NMapView mapView, NMapError errorInfo) {
            Log.v("TCP", "onMapInitHandler");
            if (errorInfo == null) { // success
                // restore map view state such as map center position and zoom level.
                mMapController.setMapCenter(new NGeoPoint(126.97838461534991, 37.56661093444756), 14); //초기 시작 좌표

                //오버레이 아이템
                int markerId = NMapPOIflagType.PIN;

                // set POI data
                NMapPOIdata poiData = new NMapPOIdata(0, mMapViewerResourceProvider);
                poiData.beginPOIdata(0);
                poiData.addPOIitem(lng, lat, "GrandParents", markerId, 0);
                Log.v("TCP", "lat: " + String.valueOf(lat) + " " + "lng: " + String.valueOf(lng));
                poiData.endPOIdata();

                // create POI data overlay
                NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
                // show all POI data
                poiDataOverlay.showAllPOIdata(0);
                // set event listener to the overlay
                poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);
                mMapView.setScalingFactor(2.0F);
                //      restoreInstanceState();
            } else { // fail
                Log.e("TCP", "onFailedToInitializeWithError: " + errorInfo.toString());

                Toast.makeText(getActivity(), errorInfo.toString(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onAnimationStateChange(NMapView mapView, int animType, int animState) {
            Log.v("aaa", "onAnimationStateChange");
            if (DEBUG) {
                Log.i("aaa", "onAnimationStateChange: animType=" + animType + ", animState=" + animState);
            }
        }

        @Override
        public void onMapCenterChange(NMapView mapView, NGeoPoint center) {
            //Log.v("TCP", "onMapCenterChange");
            Log.v("TCP", "onMapCenterChange: center=" + center.toString());
            if (DEBUG) {
                Log.i(LOG_TAG, "onMapCenterChange: center=" + center.toString());
            }
        }

        @Override
        public void onZoomLevelChange(NMapView mapView, int level) {
            zoomLevel = level;
            Log.v("TCP", "ZoomLevelChange");
            if (DEBUG) {
                Log.i(LOG_TAG, "onZoomLevelChange: level=" + level);
            }
        }

        @Override
        public void onMapCenterChangeFine(NMapView mapView) {

        }
    };

    private final NMapView.OnMapViewTouchEventListener onMapViewTouchEventListener = new NMapView.OnMapViewTouchEventListener() {

        @Override
        public void onLongPress(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLongPressCanceled(NMapView mapView) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSingleTapUp(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTouchDown(NMapView mapView, MotionEvent ev) {

        }

        @Override
        public void onScroll(NMapView mapView, MotionEvent e1, MotionEvent e2) {
        }

        @Override
        public void onTouchUp(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub

        }

    };

    private final NMapView.OnMapViewDelegate onMapViewTouchDelegate = new NMapView.OnMapViewDelegate() {

        @Override
        public boolean isLocationTracking() {
            if (mMapLocationManager != null) {
                if (mMapLocationManager.isMyLocationEnabled()) {
                    return mMapLocationManager.isMyLocationFixed();
                }
            }
            return false;
        }

    };

    /* POI data State Change Listener*/
    private final NMapPOIdataOverlay.OnStateChangeListener onPOIdataStateChangeListener = new NMapPOIdataOverlay.OnStateChangeListener() {
        @Override
        public void onCalloutClick(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
            Log.d(LOG_TAG, "5");
            if (DEBUG) {
                Log.i(LOG_TAG, "onCalloutClick: title=" + item.getTitle());
            }
            // [[TEMP]] handle a click event of the callout
            Toast.makeText(getActivity(), "onCalloutClick: " + item.getTitle(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFocusChanged(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
            if (DEBUG) {
                if (item != null) {
                    Log.i(LOG_TAG, "onFocusChanged: " + item.toString());
                } else {
                    Log.i(LOG_TAG, "onFocusChanged: ");
                }
            }
        }
    };

    private final NMapPOIdataOverlay.OnFloatingItemChangeListener onPOIdataFloatingItemChangeListener = new NMapPOIdataOverlay.OnFloatingItemChangeListener() {

        @Override
        public void onPointChanged(NMapPOIdataOverlay poiDataOverlay, NMapPOIitem item) {
            NGeoPoint point = item.getPoint();

            if (DEBUG) {
                Log.i(LOG_TAG, "onPointChanged: point=" + point.toString());
            }
            Log.v("aaa", "findPlacemarkAtLocation");
            nMapActivity.findPlacemarkAtLocation(point.longitude, point.latitude);
            item.setTitle(null);
        }
    };

    private final NMapOverlayManager.OnCalloutOverlayListener onCalloutOverlayListener = new NMapOverlayManager.OnCalloutOverlayListener() {

        @Override
        public NMapCalloutOverlay onCreateCalloutOverlay(NMapOverlay itemOverlay, NMapOverlayItem overlayItem,
                                                         Rect itemBounds) {

            // handle overlapped items
            if (itemOverlay instanceof NMapPOIdataOverlay) {
                NMapPOIdataOverlay poiDataOverlay = (NMapPOIdataOverlay) itemOverlay;

                // check if it is selected by touch event
                if (!poiDataOverlay.isFocusedBySelectItem()) {
                    int countOfOverlappedItems = 1;

                    NMapPOIdata poiData = poiDataOverlay.getPOIdata();
                    for (int i = 0; i < poiData.count(); i++) {
                        NMapPOIitem poiItem = poiData.getPOIitem(i);

                        // skip selected item
                        if (poiItem == overlayItem) {
                            continue;
                        }

                        // check if overlapped or not
                        if (Rect.intersects(poiItem.getBoundsInScreen(), overlayItem.getBoundsInScreen())) {
                            countOfOverlappedItems++;
                        }
                    }

                    if (countOfOverlappedItems > 1) {
                        String text = countOfOverlappedItems + " overlapped items for " + overlayItem.getTitle();
                        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
                        return null;
                    }
                }
            }

            // use custom old callout overlay
            if (overlayItem instanceof NMapPOIitem) {
                NMapPOIitem poiItem = (NMapPOIitem) overlayItem;

                if (poiItem.showRightButton()) {
                    return new NMapCalloutCustomOldOverlay(itemOverlay, overlayItem, itemBounds,
                            mMapViewerResourceProvider);
                }
            }

            // use custom callout overlay
            return new NMapCalloutCustomOverlay(itemOverlay, overlayItem, itemBounds, mMapViewerResourceProvider);

            // set basic callout overlay
            //return new NMapCalloutBasicOverlay(itemOverlay, overlayItem, itemBounds);
        }

    };

    private final NMapOverlayManager.OnCalloutOverlayViewListener onCalloutOverlayViewListener = new NMapOverlayManager.OnCalloutOverlayViewListener() {

        @Override
        public View onCreateCalloutOverlayView(NMapOverlay itemOverlay, NMapOverlayItem overlayItem, Rect itemBounds) {

            if (overlayItem != null) {
                // [TEST] 말풍선 오버레이를 뷰로 설정함
                String title = overlayItem.getTitle();
                if (title != null && title.length() > 5) {
                    return new NMapCalloutCustomOverlayView(getActivity(), itemOverlay, overlayItem, itemBounds);
                }
            }

            // null을 반환하면 말풍선 오버레이를 표시하지 않음
            return null;
        }

    };

    /* Local Functions */
    private static boolean mIsMapEnlared = true;

    private void restoreInstanceState() {
        Log.d(LOG_TAG, "restoreInstanceState");
        // mPreferences = getPreferences(MODE_PRIVATE);

        int longitudeE6 = NMAP_LOCATION_DEFAULT.getLongitudeE6();
        int latitudeE6 = NMAP_LOCATION_DEFAULT.getLatitudeE6();
        int level = NMAP_ZOOMLEVEL_DEFAULT;
        int viewMode = NMAP_VIEW_MODE_DEFAULT;
        boolean trafficMode = NMAP_TRAFFIC_MODE_DEFAULT;
        boolean bicycleMode = NMAP_BICYCLE_MODE_DEFAULT;

        mMapController.setMapViewMode(viewMode);
        //mMapController.setMapCenter(new NGeoPoint(longitudeE6, latitudeE6), level);
        if (lat == 0 || lng == 0) {
            mMapController.setMapCenter(126.97838461534991, 37.56661093444756, level);
        } else {
            Log.v("TCP", "mapCenter Lat: " + String.valueOf(lat) + " " + String.valueOf(lng));
            mMapController.setMapCenter(lat, lng, 14);
        }
        Log.d("TCP", "in mapFragment : " + String.valueOf(HomeFragment.latitude) + String.valueOf(HomeFragment.longitude));
        if (mIsMapEnlared) {
            mMapView.setScalingFactor(2.0F);
        } else {
            mMapView.setScalingFactor(1.0F);
        }
    }

    private void saveInstanceState() {
        Log.d("aaa", "here");
        if (mPreferences == null) {
            return;
        }

        NGeoPoint center = mMapController.getMapCenter();
        int level = mMapController.getZoomLevel();
        int viewMode = mMapController.getMapViewMode();
        boolean trafficMode = mMapController.getMapViewTrafficMode();
        boolean bicycleMode = mMapController.getMapViewBicycleMode();

        SharedPreferences.Editor edit = mPreferences.edit();

        edit.putInt(KEY_CENTER_LONGITUDE, center.getLongitudeE6());
        edit.putInt(KEY_CENTER_LATITUDE, center.getLatitudeE6());
        edit.putInt(KEY_ZOOM_LEVEL, level);
        edit.putInt(KEY_VIEW_MODE, viewMode);
        edit.putBoolean(KEY_TRAFFIC_MODE, trafficMode);
        edit.putBoolean(KEY_BICYCLE_MODE, bicycleMode);

        edit.commit();

    }

    /**
     * Invoked during init to give the Activity a chance to set up its Menu.
     *
     * @param menu the Menu to which entries may be added
     * @return true
     */
    /**
     * Container view class to rotate map view.
     */
    private class MapContainerView extends ViewGroup {

        public MapContainerView(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            final int width = getWidth();
            final int height = getHeight();
            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                final View view = getChildAt(i);
                final int childWidth = view.getMeasuredWidth();
                final int childHeight = view.getMeasuredHeight();
                final int childLeft = (width - childWidth) / 2;
                final int childTop = (height - childHeight) / 2;
                view.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            }

            if (changed) {
                mOverlayManager.onSizeChanged(width, height);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            int h = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            int sizeSpecWidth = widthMeasureSpec;
            int sizeSpecHeight = heightMeasureSpec;

            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                final View view = getChildAt(i);

                if (view instanceof NMapView) {
                    if (mMapView.isAutoRotateEnabled()) {
                        int diag = (((int) (Math.sqrt(w * w + h * h)) + 1) / 2 * 2);
                        sizeSpecWidth = MeasureSpec.makeMeasureSpec(diag, MeasureSpec.EXACTLY);
                        sizeSpecHeight = sizeSpecWidth;
                    }
                }

                view.measure(sizeSpecWidth, sizeSpecHeight);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
