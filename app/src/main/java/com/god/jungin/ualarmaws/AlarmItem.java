package com.god.jungin.ualarmaws;

import java.util.Date;

public class AlarmItem {


    private int id;                 //식별
    private String from;            //보낸이
    private String msg;             //메세지
    private String date_time;      //알람 시간
    private String to;              //받는이
    private int stat;              //알람 상태  0 설정됨, 1 해제됨

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }
}
