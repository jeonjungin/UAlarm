package com.god.jungin.ualarmaws;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.god.jungin.ualarmaws.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AlarmSet extends AppCompatActivity implements TelListFragment.OnClickFragment, TelListFragment.GetClickedTel {

    String str_date;
    String str_time;
    String str_to;
    String str_from;

    TextView tv_cancel;
    TextView tv_submit;
    TextView tv_to;

    EditText edit_msg;
    EditText edit_from;

    EditText edit_date;
    EditText edit_time;

    ImageView set_img_date;
    ImageView set_img_time;

    TelListFragment telListFragment;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_set_layout);

        final Calendar cal = Calendar.getInstance();


        SimpleDateFormat dt= new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //Date date=dt.parse(str_date+str_time);

        //취소, 보내기 버튼
        tv_cancel=(TextView)findViewById(R.id.tv_cancel);
        tv_submit=(TextView)findViewById(R.id.tv_submit);
        tv_to=(TextView)findViewById(R.id.set_tv_to);

        //에딧박스
        edit_msg=(EditText)findViewById(R.id.set_edit_msg);


        edit_date=(EditText)findViewById(R.id.set_edit_date);
        edit_time=(EditText)findViewById(R.id.set_edit_time);

        //주소록, 달력, 시계 이미지 버튼
        LinearLayout search_tel=(LinearLayout)findViewById(R.id.search_tel);
        set_img_date=(ImageView)findViewById(R.id.set_img_date);
        set_img_time=(ImageView)findViewById(R.id.set_img_time);


        //키보드 포커스 없애기
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit_msg.getWindowToken(), 0);


        telListFragment=new TelListFragment();

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.temp,telListFragment);

        fragmentTransaction.hide(telListFragment);
        fragmentTransaction.commit();


        /*
        * DB
        * */
//        dbManager=new DBManager(this,dbName,null,dbVersion);

        //내 번호
        str_from=new PhoneNum().getPhoneNum(this);

        /*
        * edit_to
        * 프래그먼트 주소록
        * */
        search_tel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.show(telListFragment);
                        fragmentTransaction.commit();
                    }
                }
        );



        /*
        * datepicker dialog
        * str_date에 년-월-일 넣음.
        * */
        set_img_date.setOnClickListener(

                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        DatePickerDialog dialog = new DatePickerDialog(AlarmSet.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int date) {

                                String msg = String.format("%d 년 %d 월 %d 일", year, month+1, date);
                                Toast.makeText(AlarmSet.this, msg, Toast.LENGTH_SHORT).show();
                                String _y= String.valueOf(year);
                                String _m= String.valueOf(month+1);
                                String _d= String.valueOf(date);
                                str_date=_y+"-"+_m+"-"+_d;
                                edit_date.setText(str_date);


                            }
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));


                        dialog.getDatePicker().setMinDate(System.currentTimeMillis());

                        dialog.show();

                    }
                });

        /*
         * str_time에 시:분
         */
        set_img_time.setOnClickListener(

                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        TimePickerDialog dialog= new TimePickerDialog(AlarmSet.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hour, int min) {
                                String msg=String.format("%d 시 %d 분", hour, min);
                                Toast.makeText(AlarmSet.this, msg, Toast.LENGTH_SHORT).show();
                                String _h=String.valueOf(hour);
                                String _m=String.valueOf(min);
                                str_time=" "+_h+":"+_m;
                                edit_time.setText(str_time);

                            }
                        },cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);


                        dialog.show();

                    }
                });


        /*
         * AlarmSubmit-->
         * 알람 값 입력
         * 서버로 보냄
         * DB에도 보냄
         * AlarmServerList로 intent
         */
        tv_submit.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //받는 번호가 입력되어 있으면
                        if(tv_to.getText().toString().charAt(0)>='0'&&tv_to.getText().toString().charAt(0)<='9') {

                            //서버에 값을 보냄
                            SendAlarm conn = new SendAlarm(getApplicationContext(),str_from,edit_msg.getText().toString(),str_date + str_time,tv_to.getText().toString(),0);
                            conn.execute("http://ec2-13-125-104-87.ap-northeast-2.compute.amazonaws.com:9000/reciveAlarm");

                        } else{
                            Toast.makeText(AlarmSet.this,"입력 값 확인 해주세요",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        //뒤로가기 버튼
        tv_cancel.setOnClickListener(

                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent back = new Intent(AlarmSet.this, AlarmServerList.class);
                        startActivity(back);
                        finish();
                    }
                }


        );

    }

    //뒤로가기 버튼 막음
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    //권한요청 콜백 메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.READ_PHONE_STATE)) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "read_phone_state permission authorized", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "read_phone_state permission denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;


        }
    }





    /*
     * 폰 번호 return, 권한 획득
     * return null 이면 검사 해야함.
     * */
    String getPhoneNum() {

        TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        int phoneStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (phoneStatePermission == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 1);

        } else {
            Toast.makeText(this, "READ_PHONE_STATE permission grant", Toast.LENGTH_SHORT);
            String PhoneNum = telManager.getLine1Number();
            if (PhoneNum.startsWith("+82")) {
                PhoneNum = PhoneNum.replace("+82", "0");
            }
            return PhoneNum;

        }

        return null;



    }

    //Fragment와 이벤트를 공유하기 위한 OnClickOutSide 인터페이스 구현
    //Fragment의 onClick 이벤트에서 호출함.
    //호출시 Fragment를 숨김
    @Override
    public void onClickOutSide(boolean clicked) {
        if (clicked) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide(telListFragment);
            fragmentTransaction.commit();

        }
    }


    @Override
    public void getClickedTel(TelItem item) {
        String tel=item.getTel().replace("-","");
        tv_to.setText(tel);
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(telListFragment);
        fragmentTransaction.commit();
    }
}

//서버에 알람을 보내는 비동기 테스크
class SendAlarm extends AsyncTask<String, String, String> {


    private String from;
    private String msg;
    private String date_time;
    private String to;
    private int stat;
    private Context mContext=null;

    public SendAlarm(Context mContext,String from,  String msg, String date_time, String to, int stat){
        this.mContext=mContext;
        this.from=from;
        this.msg=msg;
        this.date_time=date_time;
        this.to=to;
        this.stat=stat;
    }

    @Override
    protected String doInBackground(String... urls) {

        JSONObject jsonObject = new JSONObject();
        HttpURLConnection con=null;
        BufferedReader reader = null;
        BufferedWriter write =null;

        try{
            //json 오브젝트
            jsonObject.accumulate("from",from);
            jsonObject.accumulate("msg",msg);
            jsonObject.accumulate("date_time",date_time);
            jsonObject.accumulate("to",to);
            jsonObject.accumulate("stat",stat);


            //입력된 url로 연결
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

            //서버에 값 보냄
            OutputStream out= con.getOutputStream();
            write = new BufferedWriter(new OutputStreamWriter(out));
            write.write(jsonObject.toString());
            write.flush();
            write.close();

            //응답 받음
            InputStream in = con.getInputStream();
            reader=new BufferedReader(new InputStreamReader(in));

            StringBuffer buffer = new StringBuffer();
            String line ="";

            while((line=reader.readLine())!=null){
                buffer.append(line);
            }

            return buffer.toString();


        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            if(con!=null){
                con.disconnect(); //연결 끊기

            }
            if(reader!=null){
                try{
                    reader.close(); //버퍼스트림 닫음
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }


        return null;
    }
    /*
    * onBack.. 메소드 실행완료 후 실행
    * param onBack..메소드 리턴값
    * AlarmServerList로 인텐트 보냄..
    * */
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(s==null){
            Toast.makeText(mContext, "서버 연결 오류", Toast.LENGTH_SHORT).show();
        }else{

            Log.e("conn:",s);


            Intent alarmListIntent=new Intent(mContext,AlarmServerList.class);
            alarmListIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(alarmListIntent);


        }
    }
}
