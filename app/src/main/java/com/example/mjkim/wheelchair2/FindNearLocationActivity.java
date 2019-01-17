package com.example.mjkim.wheelchair2;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mjkim.wheelchair2.NaverSearch.NaverLocationList;
import com.example.mjkim.wheelchair2.Review.ReviewList;
import com.example.mjkim.wheelchair2.NaverMap.NMapCalloutCustomOldOverlay;
import com.example.mjkim.wheelchair2.NaverMap.NMapPOIflagType;
import com.example.mjkim.wheelchair2.NaverMap.NMapViewerResourceProvider;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapCompassManager;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapCalloutCustomOverlay;
import com.nhn.android.mapviewer.overlay.NMapCalloutOverlay;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;
import com.nhn.android.mapviewer.overlay.NMapResourceProvider;

import java.util.ArrayList;

/*
 - 앱 띄우는 데에는 NMapActivity를 상속받아 쓰면 된다.
 - 좌표지정과 마커사용에는 샘플파일 NMapCalloutCustomOldOverlay(밑에거 사용하는 클래스),
   NMapPOIflagType(마커관리, NMapViewerResourceProvider(아이콘, 사진 등 관리)
   들이 이용된다.
 */

public class FindNearLocationActivity extends NMapActivity{
    // gps관련
    double mLatitude, mLongitude;
    LocationManager locationManager;
    TextView locationNameTextView, addressNameTextView, telephoneNumberTextView;
    String phone;

    FrameLayout fl;

    ArrayList<ReviewList> reviewLists;

    NMapPOIdataOverlay poiDataOverlay;
    NMapPOIdataOverlay myPoiDataOverlay;

    // 상단 버튼들
    Button back_button;
    Button menu_button;
    Button callButton;
    Button watchReviewButton;
    Button writeReviewButton;

    String phoneNumber;

    ArrayList<NaverLocationList> mNaverLocationList;

    private final String TAG = "지도검색";

    private ViewGroup mapNearLayout;

    // 맵 컨트롤러
    private NMapController mMapController;

    // 네이버 지도 객체
    private NMapView mMapView;

    private NMapResourceProvider mMapViewerResourceProvider;

    //현재위치
    private NMapOverlayManager mOverlayManager;
    private MapContainerView mMapContainerView;

    private NMapMyLocationOverlay mMyLocationOverlay;
    private NMapLocationManager mMapLocationManager;
    private NMapCompassManager mMapCompassManager;
    private static final int REQUEST_CODE_LOCATION = 2;

    private NGeoPoint mGeoPoint;









    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_near_location);

        back_button = (Button)findViewById(R.id.back_b);
        menu_button = (Button)findViewById(R.id.menu_b);
        callButton = (Button)findViewById(R.id.telephone_icon);
        watchReviewButton = (Button)findViewById(R.id.watch_review_icon);
        writeReviewButton = (Button)findViewById(R.id.write_review_icon);

        locationNameTextView = (TextView)findViewById(R.id.location_name);
        addressNameTextView = (TextView)findViewById(R.id.address_name);
        telephoneNumberTextView = (TextView)findViewById(R.id.telephone_number);
        fl = (FrameLayout)findViewById(R.id.fragmentLayout);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        menu_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                openMenuTab();
            }
        });

        mapNearLayout = findViewById(R.id.mapNearLayout);


        //네이버지도 객체 생성
        mMapView = new NMapView(this);
        //객체에 키값 지정
        mMapView.setClientId(getResources().getString(R.string.NAVER_CLIENT_ID)); // 클라이언트 아이디 값 설정
        //지도 터치가능
        mMapView.setClickable(true);
        mMapView.setEnabled(true);
        mMapView.setFocusable(true);
        mMapView.setFocusableInTouchMode(true);
        mMapView.setAutoRotateEnabled(true, false);
        //안하면 줌 한 다음 축소 시켜도 작게보임
        mMapView.setScalingFactor(1.7f);
        mMapView.requestFocus();

        mMapView.setOnMapStateChangeListener(changeListener);
        mMapView.setOnMapViewTouchEventListener(mapListener);
        mapNearLayout.addView(mMapView);

        //지도 객체로부터 컨트롤러 추출
        mMapController = mMapView.getMapController();

        mMapViewerResourceProvider = new NMapViewerResourceProvider(this);
        mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);





        // 기본 위치(포항시청) + 기다리는 멘트
//        mMapController.setMapCenter(129.343422,36.019178, 11);
        Toast.makeText(FindNearLocationActivity.this, "현재 위치를 탐색중입니다. 잠시만 기다려주세요.", Toast.LENGTH_LONG).show();

        //내 주변으로 검색
        mGeoPoint = findNearLocation();
        // 내위치 띄우기
        int markerMyId = NMapPOIflagType.PIN;

        NMapPOIdata myPoiData = new NMapPOIdata(1, mMapViewerResourceProvider);
        myPoiData.beginPOIdata(0);
//        myPoiData.addPOIitem(mGeoPoint, "내 위치", markerMyId, 0);
        myPoiData.addPOIitem(129.343422,36.019178, "포항시청", markerMyId, 0);
        myPoiData.endPOIdata();
        // poi 데이터 띄우기
        myPoiDataOverlay = mOverlayManager.createPOIdataOverlay(myPoiData, null);
        myPoiDataOverlay.showAllPOIdata(11);
        myPoiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);  //좌표 클릭시 말풍선 리스너


//        // 전화마크 누르면 전화걸림
//        callButton.findViewById(R.id.telephone_icon);
//        callButton.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view){
//                Intent intent1 = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
//                startActivity(intent1);
//            }
//        });
//
//        final int a = 0;
//        // 리뷰보기 버튼
//        watchReviewButton.findViewById(R.id.watch_review_icon);
//        watchReviewButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view){
//                Intent intent;
//                intent = new Intent(FindNearLocationActivity.this, ReviewDetail.class);
//
////putExtra 로 선택한 아이템의 정보를 인텐트로 넘겨 줄 수 있다.
//                intent.putExtra("RATING", reviewLists.get(a).getRating());
//                intent.putExtra("TAG1", reviewLists.get(a).getTag1());
//                intent.putExtra("TAG2", reviewLists.get(a).getTag2());
//                intent.putExtra("TAG3", reviewLists.get(a).getTag3());
//                intent.putExtra("TAG4", reviewLists.get(a).getTag4());
//                intent.putExtra("TAG5", reviewLists.get(a).getTag5());
//                intent.putExtra("TAG6", reviewLists.get(a).getTag6());
//                intent.putExtra("REVIEW", reviewLists.get(a).getReview());
//                intent.putExtra("NAME", reviewLists.get(a).getName());
//                intent.putExtra("EMAIL", reviewLists.get(a).getEmail());
//                intent.putExtra("MAPX", reviewLists.get(a).getMapx());
//                intent.putExtra("MAPY", reviewLists.get(a).getMapy());
//                intent.putExtra("IMAGEURL1", reviewLists.get(a).getImageUrl1());
//                intent.putExtra("IMAGEURL2", reviewLists.get(a).getImageUrl2());
//                intent.putExtra("IMAGEURL3", reviewLists.get(a).getImageUrl3());
//
//                startActivity(intent);
//            }
//        });
//
//
//        // 리뷰작성 버튼
//        writeReviewButton.findViewById(R.id.write_review_text);
//        writeReviewButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view){
//                Intent intent;
//                intent = new Intent(FindNearLocationActivity.this, ReviewScreen.class);
//
////putExtra 로 선택한 아이템의 정보를 인텐트로 넘겨 줄 수 있다.
//                intent.putExtra("RATING", reviewLists.get(a).getRating());
//                intent.putExtra("TAG1", reviewLists.get(a).getTag1());
//                intent.putExtra("TAG2", reviewLists.get(a).getTag2());
//                intent.putExtra("TAG3", reviewLists.get(a).getTag3());
//                intent.putExtra("TAG4", reviewLists.get(a).getTag4());
//                intent.putExtra("TAG5", reviewLists.get(a).getTag5());
//                intent.putExtra("TAG6", reviewLists.get(a).getTag6());
//                intent.putExtra("REVIEW", reviewLists.get(a).getReview());
//                intent.putExtra("NAME", reviewLists.get(a).getName());
//                intent.putExtra("EMAIL", reviewLists.get(a).getEmail());
//                intent.putExtra("MAPX", reviewLists.get(a).getMapx());
//                intent.putExtra("MAPY", reviewLists.get(a).getMapy());
//                intent.putExtra("IMAGEURL1", reviewLists.get(a).getImageUrl1());
//                intent.putExtra("IMAGEURL2", reviewLists.get(a).getImageUrl2());
//                intent.putExtra("IMAGEURL3", reviewLists.get(a).getImageUrl3());
//
//
//                startActivity(intent);
//            }
//        });



//        startMyLocation();
//        mySetMarker();

        // 기본위치
//        mGeoPoint = new NGeoPoint(129.343701, 36.019386);
//        mMapController.animateTo(mGeoPoint);
//
//
//        int markerMyId = NMapPOIflagType.PIN;
//        int markerLocationId = NMapPOIflagType.SPOT;
//
//        // 다른 마커 띄우기
//        NMapPOIdata poiData = new NMapPOIdata(2, mMapViewerResourceProvider);
//        poiData.beginPOIdata(0);
//        // poiData.addPOIitem(mGeoPoint, "내 위치", markerMyId, 0);
//        poiData.addPOIitem(129.398522, 36.082113, "하나로클럽 포항점", markerLocationId, 0);
//        poiData.addPOIitem(129.397848, 36.081324, "바벤", markerLocationId, 1);
//        poiData.endPOIdata();
//        // create POI data overlay
//        poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
//        poiDataOverlay.showAllPOIdata(0);
//        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);  //좌표 클릭시 말풍선 리스너
//        mMapController.setZoomLevel(10);
//
//        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
//
//        //GPS가 켜져있는지 체크
//        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            intent1.addCategory(Intent.CATEGORY_DEFAULT);
//            startActivity(intent1);
//            finish();
//        }
//        //마시멜로 이상이면 권한 요청하기
//        if(Build.VERSION.SDK_INT >= 23){
//            //권한이 없는 경우
//            if(ContextCompat.checkSelfPermission(FindNearLocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
//                    ContextCompat.checkSelfPermission(FindNearLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//                ActivityCompat.requestPermissions(FindNearLocationActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION , Manifest.permission.ACCESS_FINE_LOCATION} , 1);
//            }
//            //권한이 있는 경우
//            else{
//                requestMyLocation();
//            }
//        }
//        //마시멜로 아래
//        else{
//            requestMyLocation();
//        }
    }


    //설정메뉴로 가기
    public void openMenuTab(){
        Intent intent2 = new Intent(this, MenuScreen.class);
        startActivity(intent2);
    }



    //내 주변으로 검색 기능 메소드
    //1. 내 위치 찾는다 - 되지만 반복해서 내 위치로 돌아감, 갱신을 없애야 함
    //2. 내 위치 주변 리뷰 쓰인 곳을 데이터베이스에서 NaverLocationList 형태로 가져와 어레이리스트에 담는다. - 해야함
    //3. 어레이리스트 안에 있는 장소들을 모두 마커를 달고 띄운다. - 아마 될 듯
//    @RequiresApi(api = Build.VERSION_CODES.M)
    private NGeoPoint findNearLocation() {
        // create overlay manager
        mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);
        // register callout overlay listener to customize it.
        mOverlayManager.setOnCalloutOverlayListener(onCalloutOverlayListener);
        // register callout overlay view listener to customize it.
        mOverlayManager.setOnCalloutOverlayViewListener(onCalloutOverlayViewListener);

        // location manager
        mMapLocationManager = new NMapLocationManager(this);
        mMapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);

        // compass manager
        mMapCompassManager = new NMapCompassManager(this);

        // create my location overlay
        mMyLocationOverlay = mOverlayManager.createMyLocationOverlay(mMapLocationManager, mMapCompassManager);

        mMapContainerView = new MapContainerView(this);


        if (mMyLocationOverlay != null) {
            if (!mOverlayManager.hasOverlay(mMyLocationOverlay)) {
                mOverlayManager.addOverlay(mMyLocationOverlay);
            }
            //gps 권한있으면 실행
            if (mMapLocationManager.isMyLocationEnabled()) {

                if (!mMapView.isAutoRotateEnabled()) {
                    mMyLocationOverlay.setCompassHeadingVisible(true);

                    mMapCompassManager.enableCompass();

                    mMapView.setAutoRotateEnabled(true, false);

                    mMapContainerView.requestLayout();
                } else {
                    stopMyLocation();
                }
                mMapView.postInvalidate();

            //권한없으면 환경설정 들어감
            } else {


                int permssionCheck = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
                if (permssionCheck!= PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this,"gps 권한 승인이 필요합니다",Toast.LENGTH_LONG).show();

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Toast.makeText(this,"gps 사용을 위해 위치권한이 필요합니다",Toast.LENGTH_LONG).show();
                    } else {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_CODE_LOCATION);
                        Toast.makeText(this,"gps 사용을 위해 위치권한이 필요합니다",Toast.LENGTH_LONG).show();

                    }
                }



//                boolean isMyLocationEnabled = mMapLocationManager.enableMyLocation(true);
//                if (!isMyLocationEnabled) {
//                    Toast.makeText(FindNearLocationActivity.this, "환경설정에서 위치 정보 권한을 앱에 부여해주세요.",
//                            Toast.LENGTH_LONG).show();
//
//                    Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    startActivity(goToSettings);
//
//                    return null;
//                }
            }
        }

//        // 다른 마커 띄우기
//        int markerLocationId = NMapPOIflagType.SPOT;
//        NMapPOIdata poiData = new NMapPOIdata(2, mMapViewerResourceProvider);
//        poiData.beginPOIdata(0);
//        poiData.addPOIitem(129.398522, 36.082113, "하나로클럽 포항점", markerLocationId, 0);
//        poiData.addPOIitem(129.397848, 36.081324, "바벤", markerLocationId, 1);
//        poiData.endPOIdata();
//        // create POI data overlay
//        poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
//        poiDataOverlay.showAllPOIdata(11);
//        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);  //좌표 클릭시 말풍선 리스너

        int markerId = NMapPOIflagType.PIN;
        int i = 3;
        // set POI data
        NMapPOIdata poiData = new NMapPOIdata(i, mMapViewerResourceProvider);
        poiData.beginPOIdata(0);
        poiData.addPOIitem(129.397848, 36.081324, "바벤", markerId, 0);
        poiData.addPOIitem(129.398522, 36.082113, "하나로클럽 포항점", markerId, 1);
        poiData.addPOIitem(129.403934, 36.079803, "양덕초등학교", markerId, 2);
        poiData.endPOIdata();

        NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
        poiDataOverlay.showAllPOIdata(11);
        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);  //좌표 클릭시 말풍선 리스너

        System.out.println("myLocation : " + mMapLocationManager.getMyLocation());
        return mMapLocationManager.getMyLocation();


//        // 리뷰단 장소들의 NaverLocationList 어레이리스트로 사용, db에서 받아와야함
//        mNaverLocationList = new ArrayList<NaverLocationList>();
//         어레이리스트에 있는 정보로 마커 띄우기
//        NMapPOIdata poiData = new NMapPOIdata(mNaverLocationList.size(), mMapViewerResourceProvider);
//        poiData.beginPOIdata(mNaverLocationList.size());
//        for (int i = 0; i < mNaverLocationList.size(); i++) {
//            poiData.addPOIitem(mNaverLocationList.get(i).getMapx(), mNaverLocationList.get(i).getMapy(), mNaverLocationList.get(i).getName(), markerId, 0);
//        }
//        poiData.endPOIdata();
//        // create POI data overlay
//        NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
//        poiDataOverlay.showAllPOIdata(0);
//        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);  //좌표 클릭시 말풍선 리스너
//        mMapController.setMapCenter(mMapLocationManager.getMyLocation(), 12);

//        mGeoPoint = new NGeoPoint(mMapLocationManager.getMyLocation().getLongitude(), mMapLocationManager.getMyLocation().getLatitude());
    }


    //내 위치 기반으로 (150m, 구현해야함) 미만의 리뷰가 등록된 곳에 마커가 뜬다.
    private void mySetMarker(){
        int markerId = NMapPOIflagType.PIN;
//        ArrayList<NMapPOIdata> locations = new ArrayList<NMapPOIdata>();

        // NaverLocationList 어레이리스트로 사용, db에서 받아와야함
        mNaverLocationList = new ArrayList<NaverLocationList>();

        // set current POI data
//        NMapPOIdata currentPoiData = new NMapPOIdata(1, mMapViewerResourceProvider);
//        currentPoiData.beginPOIdata(1);
//        currentPoiData.addPOIitem()


//         어레이리스트에 있는 정보로 마커 띄우기
//        NMapPOIdata poiData = new NMapPOIdata(mNaverLocationList.size(), mMapViewerResourceProvider);
//        poiData.beginPOIdata(mNaverLocationList.size());
//        for (int i = 0; i < mNaverLocationList.size(); i++) {
//            poiData.addPOIitem(mNaverLocationList.get(i).getMapx(), mNaverLocationList.get(i).getMapy(), mNaverLocationList.get(i).getName(), markerId, 0);
//        }
//        poiData.endPOIdata();
//        // create POI data overlay
//        NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
//        poiDataOverlay.showAllPOIdata(0);
//        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);  //좌표 클릭시 말풍선 리스너


        // set POI data
        NMapPOIdata poiData = new NMapPOIdata(2, mMapViewerResourceProvider);
        poiData.beginPOIdata(0);
        poiData.addPOIitem(129.398522, 36.082113, "하나로클럽 포항점", markerId, 0);
        poiData.addPOIitem(129.397848, 36.081324, "바벤", markerId, 1);
        poiData.endPOIdata();

//        NGeoPoint nGeoPoint = new NGeoPoint((36.082113 + 36.081324) / 2, (129.398522 + 129.397848) / 2);

        // create POI data overlay
        NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
        poiDataOverlay.showAllPOIdata(11);
        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);  //좌표 클릭시 말풍선 리스너
//        mMapController.setMapCenter(129.398185,36.0817185, 12);
    }

    private NMapPOIdataOverlay.OnStateChangeListener onPOIdataStateChangeListener = new NMapPOIdataOverlay.OnStateChangeListener() {
        //마커 누르면 작동
        @Override
        public void onFocusChanged(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {
            if (nMapPOIitem != null) {
                Log.e(TAG, "onFocusChanged: " + nMapPOIitem.toString());

                locationNameTextView.setText(nMapPOIitem.getTitle());
                addressNameTextView.setText(nMapPOIitem.toString());
                //전화번호 받아오기
//                telephoneNumberTextView.setText();
//                phone = ~~

                //누르면 프래그먼트 뜨기
                fl.setVisibility(View.VISIBLE);
                FragmentManager fm = getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.commit();

//                //전화번호 누르면 전화켜짐
                callButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
//                        Intent intent1 = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                        Intent intent1 = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "010"));
                        startActivity(intent1);
                    }
                });

//                // 리뷰보기 틀, 리뷰들 데이터를 받아야 띄울 수 있음
//                watchReviewButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent intent2 = new Intent(FindNearLocationActivity.this, BlogSearch.class);
//                        intent2.putExtra("BLOGNAME", name);
//                        startActivity(intent2);
//                    }
//                });
//
//                // 리뷰작성 틀, 데이터가 있어야 테스트 할 수 있음
//                writeReviewButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent intent3 = new Intent(FindNearLocationActivity.this, ReviewScreen.class);
//                        intent3.putExtra("NAME", name);
//                        intent3.putExtra("MAPX", mapx);
//                        intent3.putExtra("MAPY", mapy);
//                        intent3.putExtra("ROAD_ADDRESS", address);
//                        startActivity(intent3);
//                    }
//                });

            } else {
                Log.e(TAG, "onFocusChanged: ");
                fl.setVisibility(View.INVISIBLE);
            }
        }



        //마커에 달린 말풍선 누르면 작동
        @Override
        public void onCalloutClick(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {
//            if (nMapPOIitem != null) {
//                Log.e(TAG, "onFocusChanged: " + nMapPOIitem.toString());
//            } else {
//                Log.e(TAG, "onFocusChanged: ");
//            }
        }
    };


    private NMapView.OnMapStateChangeListener changeListener = new NMapView.OnMapStateChangeListener() {
        @Override
        public void onMapInitHandler(NMapView nMapView, NMapError nMapError) {
            Log.e(TAG, "OnMapStateChangeListener onMapInitHandler : ");
        }

        @Override
        public void onMapCenterChange(NMapView nMapView, NGeoPoint nGeoPoint) {
            Log.e(TAG, "OnMapStateChangeListener onMapCenterChange : " + nGeoPoint.getLatitude() + " ㅡ  " + nGeoPoint.getLongitude());
        }

        @Override
        public void onMapCenterChangeFine(NMapView nMapView) {
            Log.e(TAG, "OnMapStateChangeListener onMapCenterChangeFine : ");
        }

        @Override
        public void onZoomLevelChange(NMapView nMapView, int i) {
            Log.e(TAG, "OnMapStateChangeListener onZoomLevelChange : " + i);
        }

        @Override
        public void onAnimationStateChange(NMapView nMapView, int i, int i1) {
            Log.e(TAG, "OnMapStateChangeListener onAnimationStateChange : ");
        }
    };

    private NMapView.OnMapViewTouchEventListener mapListener = new NMapView.OnMapViewTouchEventListener() {
        @Override
        public void onLongPress(NMapView nMapView, MotionEvent motionEvent) {
            Log.e(TAG, "OnMapViewTouchEventListener onLongPress : ");
        }

        @Override
        public void onLongPressCanceled(NMapView nMapView) {
            Log.e(TAG, "OnMapViewTouchEventListener onLongPressCanceled : ");
        }

        @Override
        public void onTouchDown(NMapView nMapView, MotionEvent motionEvent) {
            Log.e(TAG, "OnMapViewTouchEventListener onTouchDown : ");
        }

        @Override
        public void onTouchUp(NMapView nMapView, MotionEvent motionEvent) {
            Log.e(TAG, "OnMapViewTouchEventListener onTouchUp : ");
        }

        @Override
        public void onScroll(NMapView nMapView, MotionEvent motionEvent, MotionEvent motionEvent1) {
            Log.e(TAG, "OnMapViewTouchEventListener onScroll : ");
        }

        @Override
        public void onSingleTapUp(NMapView nMapView, MotionEvent motionEvent) {
            Log.e(TAG, "OnMapViewTouchEventListener onSingleTapUp : ");
        }
    };

    private void startMyLocation() {

        // create overlay manager
        mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);
        // register callout overlay listener to customize it.
        mOverlayManager.setOnCalloutOverlayListener(onCalloutOverlayListener);
        // register callout overlay view listener to customize it.
        mOverlayManager.setOnCalloutOverlayViewListener(onCalloutOverlayViewListener);

        // location manager
        mMapLocationManager = new NMapLocationManager(this);
        mMapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);

        // compass manager
        mMapCompassManager = new NMapCompassManager(this);

        // create my location overlay
        mMyLocationOverlay = mOverlayManager.createMyLocationOverlay(mMapLocationManager, mMapCompassManager);

        mMapContainerView = new MapContainerView(this);


        if (mMyLocationOverlay != null) {
            if (!mOverlayManager.hasOverlay(mMyLocationOverlay)) {
                mOverlayManager.addOverlay(mMyLocationOverlay);
            }
            //gps 권한있으면 실행
            if (mMapLocationManager.isMyLocationEnabled()) {

                if (!mMapView.isAutoRotateEnabled()) {
                    mMyLocationOverlay.setCompassHeadingVisible(true);

                    mMapCompassManager.enableCompass();

                    mMapView.setAutoRotateEnabled(true, false);

                    mMapContainerView.requestLayout();
                } else {
                    stopMyLocation();
                }

                mMapView.postInvalidate();
            //권한없으면 환경설정 들어감
            } else {
                boolean isMyLocationEnabled = mMapLocationManager.enableMyLocation(true);
                if (!isMyLocationEnabled) {
                    Toast.makeText(FindNearLocationActivity.this, "환경설정에서 위치 정보 권한을 앱에 부여해주세요.",
                            Toast.LENGTH_LONG).show();

                    Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(goToSettings);

                    return;
                }
            }
        }
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


    //이미지버튼 누르면 현재위치로 이동하는 이벤트
//    @RequiresApi(api = Build.VERSION_CODES.M)
    public void gpsClick(View v) {
//        if (mMapController != null) {
//            mMapController.animateTo(myLocation);
//        }


        //다시검색하면 이미 떠있던건 지워버리기
//        poiDataOverlay.removeAllPOIdata();
        //다시검색
//        requestMyLocation();
        // 멘트
        Toast.makeText(FindNearLocationActivity.this, "현재 위치를 다시 탐색중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show();
        mGeoPoint = findNearLocation();
        // 내위치 띄우기
        int markerMyId = NMapPOIflagType.PIN;

        NMapPOIdata myPoiData = new NMapPOIdata(1, mMapViewerResourceProvider);
        myPoiData.beginPOIdata(0);
        myPoiData.addPOIitem(mGeoPoint, "내 위치", markerMyId, 0);
        myPoiData.endPOIdata();
        // poi 데이터 띄우기
        myPoiDataOverlay = mOverlayManager.createPOIdataOverlay(myPoiData, null);
        myPoiDataOverlay.showAllPOIdata(11);
        myPoiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);  //좌표 클릭시 말풍선 리스너
//        // create overlay manager
//        mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);
//        // register callout overlay listener to customize it.
//        mOverlayManager.setOnCalloutOverlayListener(onCalloutOverlayListener);
//        // register callout overlay view listener to customize it.
//        mOverlayManager.setOnCalloutOverlayViewListener(onCalloutOverlayViewListener);
//
//        // location manager
//        mMapLocationManager = new NMapLocationManager(this);
//        mMapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);
//
//        // get geo point of current position
//        mGeoPoint = new NGeoPoint();
//        mGeoPoint.set(mMapLocationManager.getMyLocation());
//
//        // compass manager
//        mMapCompassManager = new NMapCompassManager(this);
//
//        // create my location overlay
//        mMyLocationOverlay = mOverlayManager.createMyLocationOverlay(mMapLocationManager, mMapCompassManager);
//
//
//        if (mMyLocationOverlay != null) {
//            if (!mOverlayManager.hasOverlay(mMyLocationOverlay)) {
//                mOverlayManager.addOverlay(mMyLocationOverlay);
//            }
//
//            if (mMapLocationManager.isMyLocationEnabled()) {
//
//                if (!mMapView.isAutoRotateEnabled()) {
//                    mMyLocationOverlay.setCompassHeadingVisible(true);
//
//                    mMapCompassManager.enableCompass();
//
//                    mMapView.setAutoRotateEnabled(true, false);
//
//                    mMapContainerView.requestLayout();
//                } else {
//                    stopMyLocation();
//                }
//
//                mMapView.postInvalidate();
//            } else {
//                boolean isMyLocationEnabled = mMapLocationManager.enableMyLocation(true);
//                if (!isMyLocationEnabled) {
//                    Toast.makeText(NaverMapActivity.this, "환경설정에서 위치 정보 권한을 앱에 부여해주세요.",
//                            Toast.LENGTH_LONG).show();
//
//                    Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    startActivity(goToSettings);
//
//                    return;
//                }
//            }
//        }

    }

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
                        int diag = (((int)(Math.sqrt(w * w + h * h)) + 1) / 2 * 2);
                        sizeSpecWidth = MeasureSpec.makeMeasureSpec(diag, MeasureSpec.EXACTLY);
                        sizeSpecHeight = sizeSpecWidth;
                    }
                }

                view.measure(sizeSpecWidth, sizeSpecHeight);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }


    private final NMapOverlayManager.OnCalloutOverlayListener onCalloutOverlayListener = new NMapOverlayManager.OnCalloutOverlayListener() {

        @Override
        public NMapCalloutOverlay onCreateCalloutOverlay(NMapOverlay itemOverlay, NMapOverlayItem overlayItem,
                                                         Rect itemBounds) {

            // handle overlapped items
            if (itemOverlay instanceof NMapPOIdataOverlay) {
                NMapPOIdataOverlay poiDataOverlay = (NMapPOIdataOverlay)itemOverlay;

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

//                        // check if overlapped or not
//                        if (Rect.intersects(poiItem.getBoundsInScreen(), overlayItem.getBoundsInScreen())) {
//                            countOfOverlappedItems++;
//                        }
                    }

                    if (countOfOverlappedItems > 1) {
                        String text = countOfOverlappedItems + " overlapped items for " + overlayItem.getTitle();
                        Toast.makeText(FindNearLocationActivity.this, text, Toast.LENGTH_LONG).show();
                        return null;
                    }
                }
            }

            // use custom old callout overlay
            if (overlayItem instanceof NMapPOIitem) {
                NMapPOIitem poiItem = (NMapPOIitem)overlayItem;

                if (poiItem.showRightButton()) {
                    return new NMapCalloutCustomOldOverlay(itemOverlay, overlayItem, itemBounds,
                            (NMapCalloutCustomOldOverlay.ResourceProvider) mMapViewerResourceProvider);
                }
            }

            // use custom callout overlay
            return new NMapCalloutCustomOverlay(itemOverlay, overlayItem, itemBounds, mMapViewerResourceProvider);

//            // set basic callout overlay
//            return new NMapCalloutBasicOverlay(itemOverlay, overlayItem, itemBounds);
        }

    };

    /* MyLocation Listener */
    private final NMapLocationManager.OnLocationChangeListener onMyLocationChangeListener = new NMapLocationManager.OnLocationChangeListener() {

        @Override
        public boolean onLocationChanged(NMapLocationManager locationManager, NGeoPoint myLocation) {

            if (mMapController != null) {
                mMapController.animateTo(myLocation);
            }

            return false;
        }

        @Override
        public void onLocationUpdateTimeout(NMapLocationManager locationManager) {

             //stop location updating
//            			Runnable runnable = new Runnable() {
//            				public void run() {
//            					stopMyLocation();
//            				}
//            			};
//            			runnable.run();

            Toast.makeText(FindNearLocationActivity.this, "현재 위치를 일시적으로 확인할 수 없습니다.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onLocationUnavailableArea(NMapLocationManager locationManager, NGeoPoint myLocation) {

            Toast.makeText(FindNearLocationActivity.this, "현재 위치가 탐지되지 않는 지역입니다.", Toast.LENGTH_LONG).show();

            stopMyLocation();
        }

    };

    private final NMapOverlayManager.OnCalloutOverlayViewListener onCalloutOverlayViewListener = new NMapOverlayManager.OnCalloutOverlayViewListener() {

        @Override
        public View onCreateCalloutOverlayView(NMapOverlay itemOverlay, NMapOverlayItem overlayItem, Rect itemBounds) {

            if (overlayItem != null) {
                // 있으니까 오류떠서 잠금
//                // [TEST] 말풍선 오버레이를 뷰로 설정함
//                String title = overlayItem.getTitle();
//                if (title != null && title.length() > 5) {
//                    return new NMapCalloutCustomOverlayView(NaverMapActivity.this, itemOverlay, overlayItem, itemBounds);
//                }
            }

            // null을 반환하면 말풍선 오버레이를 표시하지 않음
            return null;
        }

    };





    //권한 요청후 응답 콜백
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //ACCESS_COARSE_LOCATION 권한
        if(requestCode==1){
            //권한받음
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
//                requestMyLocation();
                findNearLocation();
            }
            //권한못받음
            else{
                Toast.makeText(this, "권한없음", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //나의 위치 요청
    public void requestMyLocation(){
        if(ContextCompat.checkSelfPermission(FindNearLocationActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(FindNearLocationActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        //위치 변할때마다 (시간 1초, 10미터단위) 현재위치 갱신
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
    }

    //위치정보 구하기 리스너
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(ContextCompat.checkSelfPermission(FindNearLocationActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(FindNearLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }
            //나의 위치를 한번만 가져오기 위해
            locationManager.removeUpdates(locationListener);

            //위도 경도
            mLatitude = location.getLatitude();   //위도
            mLongitude = location.getLongitude(); //경도
            mGeoPoint = new NGeoPoint(mLongitude, mLatitude);
            mMapController.animateTo(mGeoPoint);


            int markerMyId = NMapPOIflagType.PIN;
            int markerLocationId = NMapPOIflagType.SPOT;


            // 내위치 띄우기
            NMapPOIdata myPoiData = new NMapPOIdata(1, mMapViewerResourceProvider);
            myPoiData.beginPOIdata(1);
            myPoiData.addPOIitem(mGeoPoint, "내 위치", markerMyId, 0);
            myPoiData.endPOIdata();
            // create POI data overlay
            myPoiDataOverlay = mOverlayManager.createPOIdataOverlay(myPoiData, null);
            myPoiDataOverlay.showAllPOIdata(0);
            myPoiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);  //좌표 클릭시 말풍선 리스너



            // 다른 마커 띄우기
            NMapPOIdata poiData = new NMapPOIdata(2, mMapViewerResourceProvider);
            poiData.beginPOIdata(2);
            poiData.addPOIitem(mGeoPoint, "내 위치", markerMyId, 0);
            poiData.addPOIitem(129.398522, 36.082113, "하나로클럽 포항점", markerLocationId, 0);
            poiData.addPOIitem(129.397848, 36.081324, "바벤", markerLocationId, 0);
            poiData.endPOIdata();
            // create POI data overlay
            poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
            poiDataOverlay.showAllPOIdata(0);
            poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);  //좌표 클릭시 말풍선 리스너
            mMapController.setZoomLevel(10);






            //         어레이리스트에 있는 정보로 마커 띄우기, 지우지마
//        NMapPOIdata poiData = new NMapPOIdata(mNaverLocationList.size(), mMapViewerResourceProvider);
//        poiData.beginPOIdata(mNaverLocationList.size());
//        for (int i = 0; i < mNaverLocationList.size(); i++) {
//            poiData.addPOIitem(mNaverLocationList.get(i).getMapx(), mNaverLocationList.get(i).getMapy(), mNaverLocationList.get(i).getName(), markerId, 0);
//        }
//        poiData.endPOIdata();
//        // create POI data overlay
//        NMapPOIdataOverlay poiDataOverlay = mOverlayManager.createPOIdataOverlay(poiData, null);
//        poiDataOverlay.showAllPOIdata(0);
//        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);  //좌표 클릭시 말풍선 리스너
//        mMapController.setMapCenter(mMapLocationManager.getMyLocation(), 12);

//        mGeoPoint = new NGeoPoint();
//        mGeoPoint.set(mMapLocationManager.getMyLocation());

        }


        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { Log.d("gps", "onStatusChanged"); }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onProviderDisabled(String provider) { }
    };
}
