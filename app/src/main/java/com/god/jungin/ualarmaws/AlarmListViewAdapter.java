package com.god.jungin.ualarmaws;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.god.jungin.ualarmaws.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AlarmListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemMoveListener{

    ArrayList<AlarmItem> data = new ArrayList<AlarmItem>();


    DBManager dbManager;

    Context mContext;

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    //스와이프
    //알람에서 삭제
    //DB에서 삭제
    //어댑터 데이터에서 삭제
    @Override
    public void onItemRemove(int position) {

        dbManager=new DBManager(mContext,"test.db",null,1);

        AlarmManager am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(mContext.getApplicationContext(), AlarmReceiver.class);
        PendingIntent pi= PendingIntent.getBroadcast(mContext,data.get(position).getId(), i, PendingIntent.FLAG_NO_CREATE);
        if(pi!=null) {
            am.cancel(pi);
            pi.cancel();
        }
        dbManager.deleteData(data.get(position).getId());
        data.remove(position);

        notifyItemRemoved(position);

    }


    public static class AlarmViewHolder extends RecyclerView.ViewHolder{
        TextView tv_id;
        TextView tv_from;
        TextView tv_to;
        TextView tv_msg;
        TextView tv_time;
        TextView tv_stat;
        ImageView lv_bell;

        AlarmViewHolder(View view){
            super(view);
//            tv_id=(TextView)view.findViewById(R.id.lv_id);

            /*보낸 사람*/
            tv_from=(TextView)view.findViewById(R.id.lv_from);

            /*받은 사람*/
//            tv_to=(TextView)view.findViewById(R.id.lv_to);

            /*메세지*/
            tv_msg=(TextView)view.findViewById(R.id.lv_msg);

            /*날짜*/
            tv_time = (TextView)view.findViewById(R.id.lv_time);

            /*스텟*/
//            tv_stat=(TextView)view.findViewById(R.id.lv_stat);

            lv_bell=(ImageView)view.findViewById(R.id.lv_bell);

        }
    }


    AlarmListViewAdapter(Context mContext){

        this.mContext=mContext;
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listview_form, viewGroup, false);
        return new AlarmViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {

        final AlarmViewHolder alarmViewHolder = (AlarmViewHolder)viewHolder;
        final int pos=position;
        final AlarmItem item=data.get(pos);

        SimpleDateFormat df_date= new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Date temp_d=null;

        try {
            temp_d=df_date.parse(item.getDate_time());


        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("date parse","null err");

        }
        String parse_dateTime=df_date.format(temp_d).replace(" ","\n" );





//        String parse_dateTime=item.getDate_time().substring(0,4)+"-"+
//                item.getDate_time().substring(4,6)+"-"+
//                item.getDate_time().substring(6,8)+" "+
//                item.getDate_time().substring(8,10)+":"+
//                item.getDate_time().substring(10,12);




//        alarmViewHolder.tv_id.setText(String.valueOf(item.getId()));
        alarmViewHolder.tv_from.setText(item.getFrom());
//        alarmViewHolder.tv_to.setText(item.getTo());
        alarmViewHolder.tv_msg.setText(item.getMsg());
        alarmViewHolder.tv_time.setText(parse_dateTime);
//        alarmViewHolder.tv_stat.setText(String.valueOf(item.getStat()));

        if(item.getStat()==0){
            alarmViewHolder.lv_bell.setImageResource(R.drawable.alarm_stat_0);
        }else{
            alarmViewHolder.lv_bell.setImageResource(R.drawable.alarm_stat_1);
        }


        //알람클릭
        alarmViewHolder.lv_bell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(item.getStat()==0){
                    Log.e("listview","bell img 변경1");
                    dbManager=new DBManager(mContext,"test.db",null,1);
                    dbManager.setAlarmStat(String.valueOf(item.getId()),"1");
                    AlarmItem temp=data.get(pos);
                    temp.setStat(1);
                    data.set(pos,temp);
                    item.setStat(1);
                    alarmViewHolder.lv_bell.setImageResource(R.drawable.alarm_stat_1);
                    notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public void addItem(ArrayList<AlarmItem> data){

        if(this.data!=null){
            this.data.clear();
            this.data.addAll(data);

        }else{
            this.data=data;
        }
        notifyDataSetChanged();


    }

    public void clearItem(){

        this.data=null;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }
}
