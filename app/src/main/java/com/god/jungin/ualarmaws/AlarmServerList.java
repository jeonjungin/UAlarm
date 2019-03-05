package com.god.jungin.ualarmaws;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.god.jungin.ualarmaws.R;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

/*
* 내가 서버에 보낸 알람
*
* */

public class AlarmServerList extends AppCompatActivity{

    String dbName="test.db";
    int dbVersion=1;
    ArrayList<AlarmItem> data;

    //리사이클러뷰
    RecyclerView.LayoutManager mLayoutManager;

    //아래로 당겨서 새로고침... 리사이클러뷰 감싸고있다.
    SwipeRefreshLayout swipeRefreshLayout;

//    Button back_btn;
    //버튼 대신 레이아웃으로 알람 설정 버튼 대신함
    LinearLayout addLayout;
    TextView alarm_empty;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_server_list_layout);

        permissionChk();

        //서비스 실행 체크--> 중지 상태일시 실행
        if(!isServiceRunningCheck()){
            Log.e("AlarmServerList","Service is not running");
            Intent alarmSevStart = new Intent(this, AlarmService.class);
            startService(alarmSevStart);
            Intent serverSevStart = new Intent(this, AlarmGetService.class);
            startService(serverSevStart);
        }

        addLayout=(LinearLayout)findViewById(R.id.alarm_server_set);
        alarm_empty=(TextView)findViewById(R.id.alarm_server_empty);

        mLayoutManager=new LinearLayoutManager(this);

        DBManager dbManager=new DBManager(this, dbName, null, dbVersion);
        data=dbManager.getAllData();

        if(data.isEmpty()){
            alarm_empty.setVisibility(View.VISIBLE);
        }else{
            alarm_empty.setVisibility(View.INVISIBLE);
        }

        //알람 리스트뷰
        RecyclerView lv = (RecyclerView) findViewById(R.id.alarm_server_lv);
        lv.setHasFixedSize(true);

        lv.setLayoutManager(mLayoutManager);

        final AlarmListViewAdapter adapter=new AlarmListViewAdapter(this);
        adapter.setHasStableIds(true);
        lv.setAdapter(adapter);

        adapter.addItem(data);

        //아이템 항목 스와이프를 위한 콜백 메소드 붙임
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeCallback(adapter,this));
        itemTouchHelper.attachToRecyclerView(lv);


        //아래로 당겨서 새로고침
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.alarm_server_refresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        data=null;
                        DBManager dbManager=new DBManager(AlarmServerList.this, dbName, null, dbVersion);
                        data=dbManager.getAllData();

                        if(data.isEmpty()){
                            alarm_empty.setVisibility(View.VISIBLE);
                        }else{
                            alarm_empty.setVisibility(View.INVISIBLE);
                        }

                        adapter.clearItem();
                        adapter.addItem(data);



                        //새로고침 완료..  없으면 평생 새로고침 돌아감
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }

        );




        addLayout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent set_intent = new Intent(AlarmServerList.this, AlarmSet.class);
                        startActivity(set_intent);
                        finish();
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    //서비스 체크
    public boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.e("isServiceRunning",service.service.getClassName());
            if ("AlarmGetService".equals(service.service.getClassName())) {

                return true;
            }
        }
        return false;
    }

    void permissionChk(){
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(AlarmServerList.this,"권한 거부",Toast.LENGTH_LONG).show();
            }
        };
        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("앱을 이용하기 위해선 권한이 필요합니다.")
                .setDeniedMessage("설정>권한에서 허용해주세용")
                .setPermissions(Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_CONTACTS)
                .check();
    }


}

