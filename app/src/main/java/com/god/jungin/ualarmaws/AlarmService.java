package com.god.jungin.ualarmaws;

/*
* 주기적으로 외부 DB와 통신,
* 내부 DB 갱신,
* 알람 갱신
*
*
* 알람매니저 구분
* _id
*
*
* */


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.god.jungin.ualarmaws.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import static android.app.PendingIntent.getActivity;

public class AlarmService extends Service {

    //boolean quit = false;
//    TimerTask mTask;
//    Timer mTimer;

    public static AlarmManager am=null;
    public static PendingIntent pi=null;

    final static int REQUEST_CODE=1000;
    String dbName="test.db";
    int dbVersion=1;
    DBManager dbManager;
    Thread mThread;
    Context mContext;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext=this;
        am=(AlarmManager) getSystemService(ALARM_SERVICE);
        /*
        DB
        * */
        dbManager=new DBManager(this,dbName,null,dbVersion);
        ArrayList<AlarmItem> data=dbManager.getAllData();

        /*
        * 알람 매니저에 펜딩인텐트 등록
        * requestCode 이용하여 다르게 식별
        *
        * */
        for(AlarmItem temp:data){
            /*stat = 0 아직 실행 안된 알람*/
            if(temp.getStat()==0) {
                Intent i = new Intent(getApplicationContext(), AlarmReceiver.class);
                i.putExtra("requestCode", temp.getId());

                Log.e("AlarmServiceCode", String.valueOf(temp.getId()));

                pi = PendingIntent.getBroadcast(this, temp.getId(), i, 0);
                SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date t;
                try {
                    t = dt.parse(temp.getDate_time());
                    am.set(AlarmManager.RTC_WAKEUP, t.getTime(), pi); //t에 저장된 시간으로 등록

                    Log.e("AlarmCreate", t.toString());
                } catch (Exception e) {
                    Log.e("AlarmManager", "string->date error");
                }
            }
        }




//        mTask=new TimerTask() {
//            @Override
//            public void run() {
//                Log.e("service","알람 서비스 실행 중");
//            }
//        };
//        mTimer=new Timer();
//        mTimer.schedule(mTask,0,2000);//2초마다 task 실행
    }


    //주기적으로 서비스 재시작함.
    //db읽어와서 stat과 알람 비교.
    //알람 삭제.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        //foreground service--> notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel("UAlarm", "UAlarm", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
//            PendingIntent notiClickIntent = getActivity(this,REQUEST_CODE ,new Intent(this, AlarmServerList.class),FLAG_CANCEL_CURRENT);

            Notification noti = new NotificationCompat.Builder(this)
                    .setContentTitle("UAlarm")
                    .setContentText("Server connected")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setChannelId("UAlarm")


//                    .setContentIntent(notiClickIntent)
                    .build();
            startForeground(2000, noti);
        }else{
//            PendingIntent notiClickIntent = getActivity(this,REQUEST_CODE ,new Intent(this, AlarmServerList.class),FLAG_CANCEL_CURRENT);
            Notification noti = new NotificationCompat.Builder(this)
                    .setContentTitle("UAlarm")
                    .setContentText("Server connected...")
                    .setSmallIcon(R.mipmap.ic_launcher)

//                    .setContentIntent(notiClickIntent)
                    .build();
            startForeground(2000, noti);
        }


        am=(AlarmManager) getSystemService(ALARM_SERVICE);
        /*
        DB
        * */
        dbManager=new DBManager(this,dbName,null,dbVersion);

        mThread=new Thread(new Runnable() {
            @Override
            public void run() {

                while(!mThread.isInterrupted()) {

                    try {
                        Log.e("AlarmService","running..");
                        ArrayList<AlarmItem> data = dbManager.getMyData();


                        for (AlarmItem temp : data) {
                            // if__삭제해야 하는 알람 구별하여 삭제
                            // else__stat = 0 이면 알람 등록
                            if (temp.getStat() == 1) {

                                Intent i = new Intent(getApplicationContext(), AlarmReceiver.class);
                                pi = PendingIntent.getBroadcast(mContext, temp.getId(), i, PendingIntent.FLAG_NO_CREATE);
                                if (pi == null) {
                                    Log.e("AlarmService","PendingIntent is null "+temp.getId());
                                } else {
                                    Log.e("AlarmService","alarm cancel");
                                    am.cancel(pi);
                                    pi.cancel();
                                }
                            } else {
                                //알람이 이미 있다면, 다시 만들지 않음( PendingIntent.getBroadcast(..., FLAG_UPDATE_CURRENT);)
                                Intent i = new Intent(getApplicationContext(), AlarmReceiver.class);
                                i.putExtra("requestCode", temp.getId());

                                Log.e("AlarmServiceCode", String.valueOf(temp.getId()));

                                pi = PendingIntent.getBroadcast(mContext, temp.getId(), i, PendingIntent.FLAG_UPDATE_CURRENT);
                                SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                Date t;
                                try {
                                    t = dt.parse(temp.getDate_time());

                                    //현재 시간과 비교, 이미 지난 시간의 알람이면 알람 설정 안함, DB의 stat 수정
                                    if(t.getTime()>=System.currentTimeMillis()) {
                                        am.set(AlarmManager.RTC_WAKEUP, t.getTime(), pi); //t에 저장된 시간으로 등록
                                        Log.e("AlarmCreate", t.toString());
                                    }
                                    else {
                                        dbManager.setAlarmStat(String.valueOf(temp.getId()),"1");
                                        Log.e("AlarmService", "is past Alarm");
                                    }



                                } catch (Exception e) {
                                    Log.e("AlarmManager", "string->date error");
                                }
                            }


                        }
                        Thread.sleep(10000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                        mThread.interrupt();
                        break;

                    }
                }
            }
        });
        mThread.start();



        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        if(am!=null && pi!=null){
            am.cancel(pi);
            Log.e("AlarmService","알람 서비스 종료(AlarmManager cancel)");
        }else{
            Log.e("AlarmService","알람 서비스 종료 에러(AlarmManager cancel)");
        }
        if(pi!=null){
            pi.cancel();
            Log.e("AlarmService","알람 서비스 종료(PendingIntent cancel)");
        }else{
            Log.e("AlarmService","알람 서비스 종료 에러(PendingIntent cancel)");
        }

        if(mThread!=null){
            mThread.interrupt();

        }

        //quit=true;
        //mTimer.cancel();

    }
}
