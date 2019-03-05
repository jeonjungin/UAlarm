package com.god.jungin.ualarmaws;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.god.jungin.ualarmaws.R;

import static android.app.PendingIntent.FLAG_ONE_SHOT;


public class AlarmPlay extends AppCompatActivity implements SensorEventListener{

        ImageView stop_btn;

        int requestCode;    //requestCode == DB row id(inc)

        String dbName="test.db";
        int dbVersion=1;
        DBManager dbManager;
        Vibrator vib;
        SensorManager sensorManager;
        Sensor mSensor;
        int shakeCount;
        final int SHAKE_SKIP_TIME=300;  //동안 지연되면 무시
        final float SHAKE_THRESHOLD_GRAVITY=1.3F; //중력가속도
        long mShakeTime;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.alarm_play_layout);

            //흔들림 감지
            sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
            mSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            vib=startVib();



            dbManager=new DBManager(this, dbName, null, dbVersion);

            /*알람 끄기 버튼*/
            stop_btn=(ImageView)findViewById(R.id.play_stop_btn);
            stop_btn.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent r=getIntent();
                            requestCode=r.getIntExtra("requestCode",10000);// 받아온 requestCode로 식별
                            //AlarmService에서 만든 intent와 pendingIntent를 requestCode를 이용해 똑같이 만들어 AlarmManager에서 취소시킨다.
                            Log.e("AlarmPlay",String.valueOf(requestCode));


                            Intent cancel_intent = new Intent(getApplicationContext(),AlarmReceiver.class);
                            AlarmManager am=(AlarmManager) getSystemService(ALARM_SERVICE);
                            PendingIntent cancel_pi =PendingIntent.getBroadcast(AlarmPlay.this,requestCode,cancel_intent,FLAG_ONE_SHOT);
                            am.cancel(cancel_pi);

                            stopVib(vib);

                            Intent back_1=new Intent(AlarmPlay.this,MainActivity.class);
                            //startActivity(back_1);
                            finish();

                            Toast.makeText(AlarmPlay.this,"알람 취소",Toast.LENGTH_SHORT).show();

                        }
                    }
            );

            Toast.makeText(this,"알람 울린다!",Toast.LENGTH_SHORT).show();
            Log.e("alarmplay","알람 울림");
            Intent r=getIntent();
            requestCode=r.getIntExtra("requestCode",10000);// 받아온 requestCode로 식별
            //AlarmService에서 만든 intent와 pendingIntent를 requestCode를 이용해 똑같이 만들어 AlarmManager에서 취소시킨다.
//            Intent cancel_intent = new Intent(getApplicationContext(),AlarmReceiver.class);
//            AlarmManager am=(AlarmManager) getSystemService(ALARM_SERVICE);
//            PendingIntent cancel_pi =PendingIntent.getBroadcast(AlarmPlay.this,requestCode,cancel_intent,FLAG_ONE_SHOT);
//            am.cancel(cancel_pi);
//            Toast.makeText(AlarmPlay.this,"알람 취소",Toast.LENGTH_SHORT).show();
//            Log.e("alarmPlay","알람 취소");

            /*DB*/

            dbManager.setAlarmStat(String.valueOf(requestCode), "1");






        }

    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(vib!=null)
            stopVib(vib);
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(vib!=null)
            stopVib(vib);

        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(vib!=null)
            stopVib(vib);

        sensorManager.unregisterListener(this);
    }

    @Override
    public void onBackPressed() {
//            super.onBackPressed();
    }

    public Vibrator startVib(){
        Vibrator vib=(Vibrator)getSystemService(VIBRATOR_SERVICE);

        // 무한 진동 0
        // 홀수 인덱스:진동
        // 짝수 인덱스:대기
        vib.vibrate(new long[]{100,1000,100,500,100,500,100,1000} , 0);

        return vib;
    }
    public void stopVib(Vibrator vib){
        if(vib!=null)
            vib.cancel();
    }


    //흔들어서 끄기
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

            Log.e("Shake","active");
            if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
                float x=sensorEvent.values[0];
                float y=sensorEvent.values[1];
                float z=sensorEvent.values[2];

                float gravityX=x/SensorManager.GRAVITY_EARTH;
                float gravityY=y/SensorManager.GRAVITY_EARTH;
                float gravityZ=z/SensorManager.GRAVITY_EARTH;

                Float f = gravityX * gravityX + gravityY * gravityY + gravityZ * gravityZ;
                double squaredD = Math.sqrt(f.doubleValue());
                float gForce=(float)squaredD;
                if(gForce>SHAKE_THRESHOLD_GRAVITY){
                    long currentTime = System.currentTimeMillis();
                    if(mShakeTime+SHAKE_SKIP_TIME>currentTime){
                        return;
                    }
                    mShakeTime=currentTime;
                    shakeCount++;
                    Log.e("Shake","Shaked: " +shakeCount);

                }


            }
            if(shakeCount>=10){
                finish();
            }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
