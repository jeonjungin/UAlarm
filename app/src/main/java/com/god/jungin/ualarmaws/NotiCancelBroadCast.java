package com.god.jungin.ualarmaws;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//노티피케이션 취소버튼 클릭시..
public class NotiCancelBroadCast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent alarmSevStop = new Intent(context, AlarmService.class);
        context.stopService(alarmSevStop);
        Intent serverSevStop = new Intent(context, AlarmGetService.class);
        context.stopService(serverSevStop);


    }
}
