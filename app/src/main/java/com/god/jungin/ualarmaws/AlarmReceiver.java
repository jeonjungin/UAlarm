package com.god.jungin.ualarmaws;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        try{
            //서비스에서 받아온 requestCode
            int requestCode=intent.getIntExtra("requestCode",10000);
            Log.e("alarmReceiver",String.valueOf(requestCode));


            Intent i = new Intent(context, AlarmPlay.class);
            i.putExtra("requestCode",requestCode);
            PendingIntent pi = PendingIntent.getActivity(context,requestCode,i,PendingIntent.FLAG_ONE_SHOT);

            Log.e("alarmReceiver","리시버 동작");
            pi.send();

        }catch(PendingIntent.CanceledException e){
            Log.e("alarmReceiver","error");
        }

    }
}
