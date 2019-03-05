package com.god.jungin.ualarmaws;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/*
* 테이블
* int   text    text    text        text    int
* _id   _from   _msg    _date_time  _to     _stat
* auto
* p_k
*
* */

public class DBManager {

    private Context mContext;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private String dbName;
    private String tableName="alarms";
    private int dbVersion;


    private String _id="_id";
    private String _from="_from";
    private String _msg="_msg";
    private String _date_time="_date_time";
    private String _to="_to";
    private String _stat="_stat";   //0->stop, 1->exe





    //생성자
    public DBManager(Context context, String dbName, SQLiteDatabase.CursorFactory factory, int dbVersion){

        this.mContext=context;
        this.dbHelper=new DBHelper(context,dbName,factory,dbVersion);
        this.dbName=dbName;
        this.dbVersion=dbVersion;
        this.db=dbHelper.getWritableDatabase();

    }

    //내부 클래쓰
    private class DBHelper extends SQLiteOpenHelper{


        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, null, version);

        }

        /*
        * 테이블 생성
        *
        * */
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
                String q="create table "+tableName+"("+
                         _id+" integer primary key autoincrement," +
                        _from+" text," +
                        _msg+" text," +
                        _date_time+" text," +
                        _to+" text," +
                        _stat+" integer);";

            sqLiteDatabase.execSQL(q);



        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }
    /*
    * dbName 테이블의 모든 데이터
    * ArrayList로 반환
    *
    * */
    public ArrayList<AlarmItem> getAllData(){
        int cnt=0;
        String s_cnt;
        ArrayList<AlarmItem> data=new ArrayList<AlarmItem>();

        String _q="select * from "+tableName+";";

        Cursor cursor = db.rawQuery(_q,null);


        while(cursor.moveToNext()){
            AlarmItem temp = new AlarmItem();
            temp.setId(cursor.getInt(0));
            temp.setFrom(cursor.getString(1));
            temp.setMsg(cursor.getString(2));
            temp.setDate_time(cursor.getString(3));
            temp.setTo(cursor.getString(4));
            temp.setStat(cursor.getInt(5));
            cnt++;
            data.add(temp);
        }
        s_cnt=String.valueOf(cnt);
        Log.e("DBManager",tableName+" 테이블의 데이터 가져옴: "+s_cnt+"low");
        return data;
    }

    public ArrayList<AlarmItem> getMyData(){

        int cnt=0;
        String s_cnt;
        ArrayList<AlarmItem> data=new ArrayList<AlarmItem>();
        String myPhone = new PhoneNum().getPhoneNum(mContext);

        String _q="select * from "+tableName+" where "+_to+"='"+myPhone+"';";

        Cursor cursor = db.rawQuery(_q,null);


        while(cursor.moveToNext()){
            AlarmItem temp = new AlarmItem();
            temp.setId(cursor.getInt(0));
            temp.setFrom(cursor.getString(1));
            temp.setMsg(cursor.getString(2));
            temp.setDate_time(cursor.getString(3));
            temp.setTo(cursor.getString(4));
            temp.setStat(cursor.getInt(5));
            cnt++;
            data.add(temp);
        }
        s_cnt=String.valueOf(cnt);
        Log.e("DBManager",tableName+" 테이블의 데이터 가져옴: "+s_cnt+"low");
        return data;
    }


    /*
    * AlarmItem 하나씩 insert
    *
    *
    * */
    public void insertData(AlarmItem input){

            try{

                String _q = "insert into " + tableName + " values(" +
                        ""+input.getId()+","
                        +"'"+input.getFrom()+"',"
                        +"'"+input.getMsg()+"',"
                        +"'"+input.getDate_time()+"',"
                        +"'"+input.getTo()+"',"
                        +""+input.getStat()+");";

                db.execSQL(_q);


            }catch (SQLException e){
                e.printStackTrace();
            }



    }

    /*
    *
    * */
    public void deleteData(int id){

        String _q="delete from "+tableName+" where "+_id+"="+String.valueOf(id)+";";
        db.execSQL(_q);

    }
    public void dropTable(){
        String _q="drop table "+tableName+";";
        db.execSQL(_q);
    }


    /*
    * stat = 0 동작전
    * stat = 1 동작된 알람 or 취소된 알람
    *
    * */
    public void setAlarmStat(String id,String stat){


        String _q="update "+tableName+" set "+_stat+"="+String.valueOf(stat)+" "+
           "where "+_id+"="+String.valueOf(id)+";";
        db.execSQL(_q);

        Log.e("DBManager","update");


    }
    public void selectAll(){

        int cnt=0;
        String s_cnt;
        ArrayList<AlarmItem> data=new ArrayList<AlarmItem>();
        String myPhone = new PhoneNum().getPhoneNum(mContext);

        String _q="select * from "+tableName+" where "+_to+"='"+myPhone+"';";

        Cursor cursor = db.rawQuery(_q,null);


        while(cursor.moveToNext()){
            AlarmItem temp = new AlarmItem();
            temp.setId(cursor.getInt(0));
            temp.setFrom(cursor.getString(1));
            temp.setMsg(cursor.getString(2));
            temp.setDate_time(cursor.getString(3));
            temp.setTo(cursor.getString(4));
            temp.setStat(cursor.getInt(5));
            cnt++;
            data.add(temp);
            Log.e("DBManager",String.valueOf(temp.getId()) +" : " +String.valueOf(temp.getStat()));
        }



    }






}
