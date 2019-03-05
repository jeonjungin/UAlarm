package com.god.jungin.ualarmaws;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.god.jungin.ualarmaws.R;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

//안씀
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Thread alarm_thread=null;
    Boolean service_stat=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionChk();

        /* 맵핑,리스너 */
        Button btn_alarm_set= (Button)findViewById(R.id.main_alarm_set_btn);
        Button btn_alarm_list= (Button)findViewById(R.id.main_alarm_list_btn);
        Button start=(Button)findViewById(R.id.start);
        Button stop=(Button)findViewById(R.id.stop);


        btn_alarm_set.setOnClickListener(this);
        btn_alarm_list.setOnClickListener(this);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);




    }

    /*버튼 리스너*/
    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.main_alarm_set_btn://알람
                Intent set_intent = new Intent(this, AlarmSet.class);
                startActivity(set_intent);

                break;

            case R.id.main_alarm_list_btn://알람 리스트
                Intent list_intent = new Intent(this, AlarmServerList.class);
                startActivity(list_intent);

                break;

            case R.id.start://서비스 시작

                //메인 쓰레드에 부담을 덜 주기위한 서비스 쓰레드
//                alarm_thread= new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        while(!alarm_thread.isInterrupted()){   //stop 버튼에서 interrupt 보내기 전까지 실행
//                            try {
//
//                                Intent start = new Intent(MainActivity.this,AlarmService.class);
//                                startService(start);
//                                alarm_thread.sleep(10000);  //interrupt는 sleep을 만나면 interruptedExcpetion 발생
//                                Log.e("thread","alarm thread 실행중");
//                            }catch (InterruptedException e){
//                                alarm_thread.interrupt();   //interrupt 받으면 다시 한번 확인 하기 위해 보냄
//                                break;                       //loop 빠져나감
//                            }
//
//                        }
//                        Log.e("thread","alarm thread is dead");
//                    }
//                });
//                alarm_thread.start();
                if(!service_stat) {
                    Intent alarmSevStart = new Intent(MainActivity.this, AlarmService.class);
                    startService(alarmSevStart);
                    Intent serverSevStart = new Intent(MainActivity.this, AlarmGetService.class);
                    startService(serverSevStart);
                    service_stat=true;
                }
                break;

            // 서비스 스레드에 interrupt 보냄, 서비스 종료
            case R.id.stop://서비스 종료

//                if( alarm_thread!=null) {
//                    alarm_thread.interrupt();
//                    Log.e("thread", alarm_thread.isInterrupted() ? "die true" : "die false");
//                }

                Intent alarmSevStop = new Intent(this, AlarmService.class);
                stopService(alarmSevStop);
                Intent serverSevStop = new Intent(this, AlarmGetService.class);
                stopService(serverSevStop);
                service_stat=false;

                break;




        }


    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    void permissionChk(){
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(MainActivity.this,"권한 거부",Toast.LENGTH_LONG).show();
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
