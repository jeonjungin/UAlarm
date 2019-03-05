package com.god.jungin.ualarmaws;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.god.jungin.ualarmaws.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.getActivities;
import static android.app.PendingIntent.getActivity;
import static android.app.PendingIntent.getBroadcast;


public class AlarmGetService extends Service {

    final static int REQUEST_CODE = 1000;

    Thread mThread;
    Context mContext;
    PhoneNum phoneNum;

    RemoteViews rv;
    PendingIntent notiCancelPi;

    public AlarmGetService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        phoneNum=new PhoneNum();
        mContext=this;


        //노티피케이션 취소버튼-->브로드캐스트 리시버로 넘겨서 서비스를 종료시킴.(액티비티를 실행하지 않음)
        notiCancelPi= getBroadcast(this, 2000, new Intent(this, NotiCancelBroadCast.class), PendingIntent.FLAG_UPDATE_CURRENT);
        //커스텀 노티피케이션 레이아웃
        rv= new RemoteViews(getPackageName(), R.layout.notiremote_layout);
        //취소버튼에 리스너 달아줌. 위 페딩인텐트
        rv.setOnClickPendingIntent(R.id.noti_cancel,notiCancelPi);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //포 그라운드 서비스로 돌리기 위한 notification
        //오래오 버전 이상 부터 필요한 notification 채널




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel("UAlarm", "UAlarm", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);



            PendingIntent notiClickIntent = getActivity(this,REQUEST_CODE ,new Intent(this, AlarmServerList.class),FLAG_CANCEL_CURRENT);

            Notification notiEx = new NotificationCompat.Builder(this)
                    .setContentTitle("UAlarm")
                    .setContentText("Server connected")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setChannelId("UAlarm")
                    .setContentIntent(notiClickIntent)  //노티피케이션 클릭시 notiClickIntent-->메인 액티비티 실행
                    .setCustomContentView(rv)           //커스텀 노티피케이션 적용
                    .build();
            startForeground(2000, notiEx);
        }else{    //오래오 이전 버전

            PendingIntent notiClickIntent = getActivity(this,REQUEST_CODE ,new Intent(this, AlarmServerList.class),FLAG_CANCEL_CURRENT);
            Notification notiEx = new NotificationCompat.Builder(this)
                    .setContentTitle("UAlarm")
                    .setContentText("Server connected...")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(notiClickIntent)
                    .setCustomContentView(rv)

                    .build();
            startForeground(2000, notiEx);
        }




        //10초마다 한번씩 서버와 연결해서 값을 받아 DB에 저장.
        mThread=new Thread(new Runnable() {
            @Override
            public void run() {

                String pNumber=new PhoneNum().getPhoneNum(mContext);
                GetAlarm conn;

                while(!mThread.isInterrupted()){
                    try{
                        if(pNumber!=null) {
                            //서버에서 값 받기
                            conn = new GetAlarm(mContext,pNumber);
                            conn.execute("http://ec2-13-125-104-87.ap-northeast-2.compute.amazonaws.com:9000/sendAlarm");
                        }
                        Thread.sleep(10000);
                    }catch (InterruptedException e){
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
        if(mThread!=null){
            Log.e("AlarmGetService","알람 서비스 종료");

            mThread.interrupt();
        }
    }



}

/*
 * 서버에 pNumber만 보냄.
 * 서버에서 내 pNumber가 있는 lows->jsonArray 형식으로 받음
 * DB에 저장
 * */
class GetAlarm extends AsyncTask<String, String, String>{

    String pNumber;     //서버에 나를 특정하기 위한 변수 phoneNumber
    Context mContext;

    public GetAlarm(Context mContext, String pNumber){
        this.mContext=mContext;
        this.pNumber=pNumber;

    }


    @Override
    protected String doInBackground(String... urls) {

        JSONObject sendJSON = new JSONObject();

        HttpURLConnection con=null;
        BufferedReader reader = null;
        BufferedWriter write =null;

        try{
            sendJSON.accumulate("pNumber",pNumber);

            URL url = new URL(urls[0]);
            con=(HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Cache-Control","no-cache"); //캐시 사용안함
            con.setRequestProperty("Content-Type","application/json");  //json형식
            con.setRequestProperty("Accept","text/html");   //html 형식으로 받겟다
            con.setConnectTimeout(5000);                        //타임아웃 5초 설정
            con.setDoOutput(true);                              //OutPutStream 사용한다.
            con.setDoInput(true);                               //InPutStream 사용한다.
            con.connect();                                      //url에 연결

            OutputStream out= con.getOutputStream();
            write = new BufferedWriter(new OutputStreamWriter(out));
            write.write(sendJSON.toString());
            write.flush();
            write.close();

            InputStream in = con.getInputStream();
            reader=new BufferedReader(new InputStreamReader(in));

            StringBuffer buffer = new StringBuffer();
            String line ="";

            while((line=reader.readLine())!=null){
                buffer.append(line);
            }

            return buffer.toString();

        }catch (Exception e){

        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        String dbName="test.db";
        int dbVersion=1;

        DBManager dbManager=new DBManager(mContext,dbName,null,dbVersion);


        try{
            JSONObject getJSON=null;
            JSONArray jsonArray=new JSONArray(s);
            if(!jsonArray.isNull(0)){

                String[] jsonName = {"_id","_from", "_msg", "_date_time","_to","_stat"};    //json key
                String[][] parseredData = new String[jsonArray.length()][jsonName.length];

                for (int i = 0; i < jsonArray.length(); i++) {

                    getJSON = jsonArray.getJSONObject(i);

                    if(getJSON != null) {
                        for(int j = 0; j < jsonName.length; j++) {

                            parseredData[i][j] = getJSON.getString(jsonName[j]);
                        }
                    }
                }
                for(int i=0; i<parseredData.length; i++){

                    Log.e("JSON을 분석한 데이터 "+i+" : ", parseredData[i][1]);


                    AlarmItem temp=new AlarmItem();
                    temp.setId(Integer.parseInt(parseredData[i][0]));
                    temp.setFrom(parseredData[i][1]);
                    temp.setMsg(parseredData[i][2]);
                    temp.setDate_time(parseredData[i][3]);
                    temp.setTo(parseredData[i][4]);
                    temp.setStat(Integer.parseInt(parseredData[i][5]));

                    dbManager.insertData(temp);

                }
            }





        }catch (Exception e){
            e.printStackTrace();

        }


    }





}


